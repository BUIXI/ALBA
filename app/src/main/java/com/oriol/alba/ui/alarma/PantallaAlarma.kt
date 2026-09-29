package com.oriol.alba.ui.alarma

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import android.text.format.DateFormat
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.oriol.alba.datos.TipoTarea
import com.oriol.alba.dominio.ObjetoFoto
import com.oriol.alba.dominio.OperacionesParaApagar
import com.oriol.alba.ui.componentes.BotonTexto
import com.oriol.alba.ui.tareas.CamaraReconocedora
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.oriol.alba.R
import com.oriol.alba.alarma.Modo
import com.oriol.alba.alarma.ServicioAlarma
import com.oriol.alba.alarma.SesionAlarma
import com.oriol.alba.datos.Alarma
import com.oriol.alba.dominio.Operacion
import com.oriol.alba.theme.Alba
import com.oriol.alba.theme.trackingInter
import com.oriol.alba.ui.componentes.BotonPrincipal
import com.oriol.alba.ui.componentes.Teclado
import com.oriol.alba.ui.componentes.formatoHora
import com.oriol.alba.ui.componentes.rememberAhora
import com.oriol.alba.ui.componentes.textoCuando
import com.oriol.alba.ui.tareas.Tecla
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

// La pantalla de la alarma. Siempre en claro (la pone ActividadAlarma con
// PaletaClara), sobre un degradado cálido de amanecer.

/** Degradado de fondo: melocotón arriba, casi blanco abajo. */
private val FondoAmanecer = Brush.verticalGradient(listOf(Color(0xFFFFDDB0), Color(0xFFFFF1E0), Color(0xFFF8F5F1)))

private val Negro = Color(0xFF000000)
private val Blanco = Color(0xFFFFFFFF)

/** La pantalla según el momento: sonando, tarea, comprobación o hecho. */
@Composable
fun PantallaAlarma(vm: AlarmaViewModel, onCerrar: () -> Unit) {
  val sesion = vm.sesion
  val fase = vm.fase
  when {
    fase is Fase.Hecho -> AlarmaHecha(fase, onCerrar)
    sesion == null -> FondoAlarma {}
    sesion.modo == Modo.COMPROBACION -> {
      val limite = sesion.inicioModo + SesionAlarma.Ajustes().esperaComprobacion
      AlarmaComprobacion(segundosHasta(limite) ?: 0, vm::responderComprobacion)
    }
    fase == Fase.Tarea && vm.tareaActual == TipoTarea.FOTO -> TareaFoto(vm, segundosHasta(sesion.silenciadaHasta))
    fase == Fase.Tarea ->
      AlarmaCalculo(
        operacion = vm.calculo.operacion,
        respuesta = vm.calculo.respuesta,
        aciertos = vm.calculo.aciertos,
        total = vm.calculo.total,
        fallos = vm.calculo.fallos,
        segundosSilencio = segundosHasta(sesion.silenciadaHasta),
        onTecla = { vm.tecla(it) },
      )
    else -> AlarmaSonando(sesion.alarma, rememberAhora(), sesion.prueba, vm::despierto, tarea = vm.tareaActual)
  }
}

/**
 * La tarea de foto con su cámara. Sin permiso de cámara, o si la cámara falla, pasa a
 * cálculo: la alarma siempre se tiene que poder apagar.
 */
@Composable
private fun TareaFoto(vm: AlarmaViewModel, segundosSilencio: Int?) {
  val context = LocalContext.current
  val hayPermiso =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
  if (!hayPermiso) {
    LaunchedEffect(Unit) { vm.pasarACalculo() }
    return
  }
  // Buscando no se toca la pantalla: durante un minuto y medio la búsqueda pide
  // silencio; después, si aún no está, la alarma vuelve a sonar hasta encontrarlo.
  LaunchedEffect(Unit) {
    repeat(9) {
      vm.seguirBuscando()
      delay(10_000)
    }
  }
  // Si en 45 s no lo reconoce (luz, un objeto raro...), aparece la salida al cálculo.
  var mostrarCalculo by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    delay(45_000)
    mostrarCalculo = true
  }
  AlarmaFoto(
    objeto = vm.objeto,
    vistosSeguidos = vm.vistosSeguidos,
    segundosSilencio = segundosSilencio,
    cambiosRestantes = vm.cambiosRestantes,
    mostrarCalculo = mostrarCalculo,
    onCambiar = vm::cambiarObjeto,
    onCalculo = vm::pasarACalculo,
  ) { modifier ->
    CamaraReconocedora(onEtiquetas = vm::etiquetas, onError = vm::pasarACalculo, modifier = modifier)
  }
}

/** Segundos que faltan hasta [limite] (en `elapsedRealtime`), redondeando hacia arriba. */
@Composable
private fun segundosHasta(limite: Long?): Int? {
  val ahora by
    produceState(SystemClock.elapsedRealtime()) {
      while (true) {
        value = SystemClock.elapsedRealtime()
        delay(250)
      }
    }
  return limite?.let { ((it - ahora + 999) / 1000).toInt().coerceAtLeast(0) }
}

/** Fondo y márgenes comunes a todos los estados. */
@Composable
private fun FondoAlarma(contenido: @Composable ColumnScope.() -> Unit) {
  Column(
    Modifier.fillMaxSize().background(FondoAmanecer).windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    content = contenido,
  )
}

/** Botón negro y ancho: el único de cada estado. */
@Composable
private fun BotonAlarma(texto: String, onClick: () -> Unit) {
  BotonPrincipal(texto, onClick, Modifier.fillMaxWidth(), fondo = Negro, colorTexto = Blanco, alto = 60.dp)
}

/** Un sol que respira despacio, con su halo. */
@Composable
private fun SolAmanecer(modifier: Modifier = Modifier) {
  val acento = Alba.colores.acento
  val transicion = rememberInfiniteTransition(label = "sol")
  val pulso by
    transicion.animateFloat(
      initialValue = 0.94f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
      label = "pulso",
    )
  Canvas(modifier) {
    val radio = size.minDimension / 2
    drawCircle(Brush.radialGradient(listOf(acento.copy(alpha = 0.32f), acento.copy(alpha = 0f)), center, radio), radio)
    drawCircle(acento, radio * 0.42f * pulso)
  }
}

private val EstiloHoraGrande
  @Composable get() = Alba.tipos.horaLista.copy(fontSize = 88.sp, lineHeight = 96.sp, letterSpacing = trackingInter(88f).em)

/** Suena: la hora de ahora, grande, y el botón para empezar la tarea. */
@Composable
fun AlarmaSonando(
  alarma: Alarma,
  ahora: LocalDateTime,
  prueba: Boolean,
  onDespierto: () -> Unit,
  tarea: TipoTarea = alarma.tarea,
) {
  val idioma = LocalConfiguration.current.locales[0]
  val fecha = remember(ahora.toLocalDate(), idioma) {
    DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(idioma, "EEEEdMMMM"), idioma).format(ahora)
  }
  FondoAlarma {
    if (prueba) {
      Spacer(Modifier.height(12.dp))
      Text(
        stringResource(R.string.prueba),
        style = Alba.tipos.nota,
        color = Alba.colores.textoSecundario,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(Blanco.copy(alpha = 0.6f)).padding(horizontal = 12.dp, vertical = 4.dp),
      )
    }
    Spacer(Modifier.weight(1f))
    SolAmanecer(Modifier.size(150.dp))
    Spacer(Modifier.height(20.dp))
    Text(fecha, style = Alba.tipos.cabecera, color = Alba.colores.textoSecundario)
    Text(formatoHora(ahora.hour, ahora.minute), style = EstiloHoraGrande, color = Alba.colores.texto)
    if (alarma.etiqueta.isNotBlank()) {
      Text(alarma.etiqueta, style = Alba.tipos.titulo, color = Alba.colores.texto, textAlign = TextAlign.Center)
    }
    Spacer(Modifier.weight(1.3f))
    BotonAlarma(stringResource(R.string.estoy_despierto), onDespierto)
    Spacer(Modifier.height(12.dp))
    Text(
      if (tarea == TipoTarea.FOTO) stringResource(R.string.pista_foto)
      else pluralStringResource(R.plurals.pista_calculo, OperacionesParaApagar, OperacionesParaApagar),
      style = Alba.tipos.nota,
      color = Alba.colores.textoSecundario,
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(20.dp))
  }
}

/**
 * La tarea de cálculo: la operación, la respuesta que se va escribiendo y el teclado.
 * Al fallar, la operación tiembla (como el código de desbloqueo de iOS).
 */
@Composable
fun AlarmaCalculo(
  operacion: Operacion,
  respuesta: String,
  aciertos: Int,
  total: Int,
  fallos: Int,
  segundosSilencio: Int?,
  onTecla: (Tecla) -> Unit,
) {
  val haptica = LocalHapticFeedback.current
  val sacudida = remember { Animatable(0f) }
  LaunchedEffect(fallos) {
    if (fallos == 0) return@LaunchedEffect
    haptica.performHapticFeedback(HapticFeedbackType.Reject)
    for (x in listOf(-18f, 15f, -11f, 7f, -3f, 0f)) sacudida.animateTo(x, tween(55))
  }
  LaunchedEffect(aciertos) { if (aciertos > 0) haptica.performHapticFeedback(HapticFeedbackType.Confirm) }

  FondoAlarma {
    Spacer(Modifier.height(28.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      repeat(total) { i ->
        Box(
          Modifier.size(10.dp).clip(CircleShape).background(if (i < aciertos) Alba.colores.acento else Negro.copy(alpha = 0.12f))
        )
      }
    }
    Spacer(Modifier.height(10.dp))
    EstadoSonido(segundosSilencio, R.string.sonando_pulsa)
    Spacer(Modifier.weight(1f))
    Column(
      Modifier.offset { IntOffset(sacudida.value.dp.roundToPx(), 0) },
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(operacion.texto, style = Alba.tipos.horaLista, color = Alba.colores.texto)
      Text(
        "= " + respuesta.ifEmpty { "?" },
        style = Alba.tipos.horaLista,
        color = if (respuesta.isEmpty()) Alba.colores.textoTerciario else Alba.colores.texto,
      )
    }
    Spacer(Modifier.weight(1f))
    Teclado(onTecla)
    Spacer(Modifier.height(28.dp))
  }
}

/** Línea de estado del sonido durante la tarea. */
@Composable
private fun EstadoSonido(segundosSilencio: Int?, textoSonando: Int) {
  Text(
    if (segundosSilencio != null) stringResource(R.string.silencio_quedan, segundosSilencio) else stringResource(textoSonando),
    style = Alba.tipos.nota,
    color = Alba.colores.textoSecundario,
  )
}

/**
 * La tarea de foto: qué enseñar, la cámara en una tarjeta redondeada (con el borde en
 * ámbar mientras reconoce el objeto) y las salidas: otro objeto o el cálculo.
 * [camara] es la vista de la cámara; en las capturas, un hueco.
 */
@Composable
fun AlarmaFoto(
  objeto: ObjetoFoto,
  vistosSeguidos: Int,
  segundosSilencio: Int?,
  cambiosRestantes: Int,
  mostrarCalculo: Boolean,
  onCambiar: () -> Unit,
  onCalculo: () -> Unit,
  camara: @Composable (Modifier) -> Unit,
) {
  val viendo = vistosSeguidos > 0
  val borde by animateColorAsState(if (viendo) Alba.colores.acento else Color.Transparent, tween(150), label = "borde")
  FondoAlarma {
    Spacer(Modifier.height(20.dp))
    EstadoSonido(segundosSilencio, R.string.sonando_foto)
    Spacer(Modifier.height(18.dp))
    Text(stringResource(R.string.ensenale), style = Alba.tipos.cabecera, color = Alba.colores.textoSecundario)
    Text(
      stringResource(nombreObjeto(objeto)),
      style = Alba.tipos.tituloGrande,
      color = Alba.colores.texto,
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(20.dp))
    Box(
      Modifier.fillMaxWidth()
        .weight(1f)
        .clip(RoundedCornerShape(28.dp))
        .background(Negro)
        .border(4.dp, borde, RoundedCornerShape(28.dp))
    ) {
      camara(Modifier.fillMaxSize())
    }
    Spacer(Modifier.height(12.dp))
    Text(
      stringResource(if (viendo) R.string.lo_estoy_viendo else R.string.buscando),
      style = Alba.tipos.secundario,
      color = if (viendo) Alba.colores.texto else Alba.colores.textoSecundario,
    )
    Spacer(Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      if (cambiosRestantes > 0) {
        BotonTexto(
          pluralStringResource(R.plurals.otro_objeto, cambiosRestantes, cambiosRestantes),
          onCambiar,
          color = Alba.colores.texto,
        )
      }
      if (mostrarCalculo) BotonTexto(stringResource(R.string.mejor_calculo), onCalculo, color = Alba.colores.texto)
    }
    Spacer(Modifier.height(12.dp))
  }
}

private fun nombreObjeto(objeto: ObjetoFoto): Int =
  when (objeto) {
    ObjetoFoto.FREGADERO -> R.string.objeto_fregadero
    ObjetoFoto.TAZA -> R.string.objeto_taza
    ObjetoFoto.SOFA -> R.string.objeto_sofa
    ObjetoFoto.TELE -> R.string.objeto_tele
    ObjetoFoto.ZAPATOS -> R.string.objeto_zapatos
    ObjetoFoto.PLANTA -> R.string.objeto_planta
    ObjetoFoto.CUBIERTOS -> R.string.objeto_cubiertos
    ObjetoFoto.COCINA -> R.string.objeto_cocina
  }

/** Pregunta de comprobación, con la cuenta atrás hasta que vuelva a sonar. */
@Composable
fun AlarmaComprobacion(segundos: Int, onSi: () -> Unit) {
  FondoAlarma {
    Spacer(Modifier.weight(1f))
    SolAmanecer(Modifier.size(120.dp))
    Spacer(Modifier.height(20.dp))
    Text(stringResource(R.string.sigues_despierto), style = Alba.tipos.tituloGrande, color = Alba.colores.texto)
    Spacer(Modifier.height(10.dp))
    Text(
      stringResource(R.string.comprobacion_quedan, segundos),
      style = Alba.tipos.cuerpo,
      color = Alba.colores.textoSecundario,
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.weight(1.3f))
    BotonAlarma(stringResource(R.string.si_despierto), onSi)
    Spacer(Modifier.height(36.dp))
  }
}

/** Apagada: el saludo, a qué hora te has levantado y cuándo es la próxima. */
@Composable
fun AlarmaHecha(fase: Fase.Hecho, onCerrar: () -> Unit) {
  val ahora = remember { LocalDateTime.now() }
  FondoAlarma {
    Spacer(Modifier.weight(1f))
    SolAmanecer(Modifier.size(120.dp))
    Spacer(Modifier.height(20.dp))
    Text(
      if (fase.fueComprobacion) stringResource(R.string.perfecto) else stringResource(saludo(fase.hora)),
      style = Alba.tipos.tituloGrande.copy(fontSize = 40.sp, lineHeight = 46.sp),
      color = Alba.colores.texto,
    )
    Spacer(Modifier.height(10.dp))
    val lineas = buildList {
      if (fase.fueComprobacion) add(stringResource(R.string.buen_dia))
      else add(stringResource(R.string.te_has_levantado, formatoHora(fase.hora.hour, fase.hora.minute)))
      fase.proxima?.let { add(stringResource(R.string.proxima_cuando, textoCuando(it, ahora))) }
    }
    lineas.forEach { Text(it, style = Alba.tipos.cuerpo, color = Alba.colores.textoSecundario, textAlign = TextAlign.Center) }
    if (fase.comprobaraDespues) {
      Spacer(Modifier.height(18.dp))
      val minutos = ServicioAlarma.MinutosComprobacion.toInt()
      Text(
        pluralStringResource(R.plurals.comprobare_en, minutos, minutos),
        style = Alba.tipos.nota,
        color = Alba.colores.textoSecundario,
        textAlign = TextAlign.Center,
      )
    }
    Spacer(Modifier.weight(1.3f))
    BotonAlarma(stringResource(R.string.cerrar), onCerrar)
    Spacer(Modifier.height(36.dp))
  }
}

/** "Buenos días" hasta las 14:00, "Buenas tardes" hasta las 21:00 y luego "Buenas noches". */
private fun saludo(hora: LocalTime): Int =
  when {
    hora.hour in 5..13 -> R.string.buenos_dias
    hora.hour in 14..20 -> R.string.buenas_tardes
    else -> R.string.buenas_noches
  }
