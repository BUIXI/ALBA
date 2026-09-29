package com.oriol.alba.ui.tareas

import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.oriol.alba.dominio.Etiqueta
import java.util.concurrent.Executors

/**
 * Vista de la cámara trasera que, a la vez, pasa los fotogramas por el reconocedor de
 * ML Kit (en el móvil, sin internet). Avisa con [onEtiquetas] de lo que ve, unas
 * cuatro veces por segundo. Si la cámara no arranca, llama a [onError].
 */
@Composable
fun CamaraReconocedora(onEtiquetas: (List<Etiqueta>) -> Unit, onError: () -> Unit, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val ciclo = LocalLifecycleOwner.current
  val onEtiquetasActual by rememberUpdatedState(onEtiquetas)
  val onErrorActual by rememberUpdatedState(onError)
  val vista = remember {
    PreviewView(context).apply {
      scaleType = PreviewView.ScaleType.FILL_CENTER
      // Compatible: se puede recortar con esquinas redondeadas desde Compose.
      implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    }
  }

  DisposableEffect(ciclo) {
    val etiquetador = ImageLabeling.getClient(ImageLabelerOptions.Builder().setConfidenceThreshold(0.4f).build())
    val hilo = Executors.newSingleThreadExecutor()
    val futuro = ProcessCameraProvider.getInstance(context)
    var proveedor: ProcessCameraProvider? = null
    var activo = true
    futuro.addListener(
      {
        if (!activo) return@addListener
        try {
          val p = futuro.get()
          proveedor = p
          val previa = Preview.Builder().build().also { it.setSurfaceProvider(vista.surfaceProvider) }
          val analisis =
            ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also {
              it.setAnalyzer(hilo, Analizador(etiquetador) { etiquetas -> if (activo) onEtiquetasActual(etiquetas) })
            }
          p.unbindAll()
          p.bindToLifecycle(ciclo, CameraSelector.DEFAULT_BACK_CAMERA, previa, analisis)
        } catch (e: Exception) {
          onErrorActual()
        }
      },
      ContextCompat.getMainExecutor(context),
    )
    onDispose {
      activo = false
      proveedor?.unbindAll()
      etiquetador.close()
      hilo.shutdown()
    }
  }

  AndroidView(factory = { vista }, modifier = modifier)
}

/** Pasa un fotograma de cada 250 ms por el reconocedor; el resto se descarta. */
private class Analizador(
  private val etiquetador: ImageLabeler,
  private val alResultado: (List<Etiqueta>) -> Unit,
) : ImageAnalysis.Analyzer {
  private var ultimo = 0L

  @OptIn(ExperimentalGetImage::class)
  override fun analyze(imagen: ImageProxy) {
    val ahora = SystemClock.elapsedRealtime()
    val media = imagen.image
    if (media == null || ahora - ultimo < 250) {
      imagen.close()
      return
    }
    ultimo = ahora
    try {
      val entrada = InputImage.fromMediaImage(media, imagen.imageInfo.rotationDegrees)
      etiquetador
        .process(entrada)
        // Los resultados llegan en el hilo principal.
        .addOnSuccessListener { etiquetas -> alResultado(etiquetas.map { Etiqueta(it.text, it.confidence) }) }
        .addOnCompleteListener { imagen.close() }
    } catch (e: Exception) {
      // El reconocedor ya se ha cerrado (la tarea acaba de terminar) o ha fallado un
      // fotograma: se descarta. Nunca debe tumbar la pantalla de la alarma.
      imagen.close()
    }
  }
}
