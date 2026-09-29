package com.oriol.alba.alarma

import android.app.AlarmManager
import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oriol.alba.datos.Alarma
import com.oriol.alba.datos.Sonido
import com.oriol.alba.datos.TipoTarea
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Las piezas que tocan Android (AlarmManager, receptor, servicio, notificación) contra
 * el Android simulado de Robolectric.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class AlarmaSistemaTest {

  private val app: Application = ApplicationProvider.getApplicationContext()
  private val alarmManager = app.getSystemService(AlarmManager::class.java)
  private val alarma =
    Alarma(id = 5, hora = 6, minuto = 30, dias = setOf(MONDAY, FRIDAY), etiqueta = "Trabajo", sonido = Sonido.SISTEMA, comprobar = false)

  @After fun limpiar() = CentralAlarma.publicar(null)

  @Test
  fun extras_idaYVuelta() {
    val intent = Intent().ponerAlarma(alarma).ponerModo(Modo.COMPROBACION).ponerPrueba(true)
    assertEquals(alarma, intent.leerAlarma())
    assertEquals(Modo.COMPROBACION, intent.leerModo())
    assertTrue(intent.esPrueba())
    assertNull(Intent().leerAlarma())
  }

  @Test
  fun programador_usaSetAlarmClock_aSuHoraExacta() {
    val momento = LocalDateTime.of(2026, 10, 2, 6, 30)
    ProgramadorSistema(app).programar(alarma, momento)
    val siguiente = alarmManager.nextAlarmClock
    assertNotNull(siguiente)
    assertEquals(momento.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), siguiente.triggerTime)
  }

  @Test
  fun programador_cadaAlarmaLaSuya_yCancelarQuitaSoloEsa() {
    val programador = ProgramadorSistema(app)
    val otra = alarma.copy(id = 6)
    programador.programar(alarma, LocalDateTime.of(2026, 10, 2, 6, 30))
    programador.programar(otra, LocalDateTime.of(2026, 10, 2, 8, 0))
    assertEquals(2, shadowOf(alarmManager).scheduledAlarms.size)
    programador.cancelar(alarma.id)
    val quedan = shadowOf(alarmManager).scheduledAlarms
    assertEquals(1, quedan.size)
    assertEquals(
      LocalDateTime.of(2026, 10, 2, 8, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
      quedan.single().triggerAtTime,
    )
  }

  @Test
  fun receptor_arrancaElServicio_conLaAlarmaDentro() {
    val disparo = Intent(app, ReceptorAlarma::class.java).setAction(ReceptorAlarma.ACCION_DISPARAR).ponerAlarma(alarma)
    ReceptorAlarma().onReceive(app, disparo)
    val servicio = shadowOf(app).nextStartedService
    assertEquals(ServicioAlarma::class.java.name, servicio.component?.className)
    assertEquals(ServicioAlarma.ACCION_SONAR, servicio.action)
    assertEquals(alarma, servicio.leerAlarma())
  }

  @Test
  fun servicio_suena_seSilencia_yTermina() {
    val controlador =
      Robolectric.buildService(ServicioAlarma::class.java, ServicioAlarma.intentSonar(app, alarma, Modo.ALARMA)).create()
    val servicio = controlador.get()
    servicio.onStartCommand(ServicioAlarma.intentSonar(app, alarma, Modo.ALARMA), 0, 1)
    shadowOf(Looper.getMainLooper()).idle()

    // Suena: sesión publicada y notificación de alarma a pantalla completa.
    val sesion = CentralAlarma.sesion.value
    assertNotNull(sesion)
    assertEquals(alarma, sesion!!.alarma)
    val notificacion = shadowOf(app.getSystemService(NotificationManager::class.java)).allNotifications.single()
    assertEquals(Notification.CATEGORY_ALARM, notificacion.category)
    assertNotNull(notificacion.fullScreenIntent)
    assertTrue(notificacion.flags and Notification.FLAG_ONGOING_EVENT != 0)

    // "Estoy despierto": silencio.
    servicio.onStartCommand(Intent(app, ServicioAlarma::class.java).setAction(ServicioAlarma.ACCION_SILENCIAR), 0, 2)
    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(300))
    assertNotNull(CentralAlarma.sesion.value!!.silenciadaHasta)

    // Tarea hecha: se acaba y se va la notificación.
    servicio.onStartCommand(Intent(app, ServicioAlarma::class.java).setAction(ServicioAlarma.ACCION_TERMINAR), 0, 3)
    shadowOf(Looper.getMainLooper()).idle()
    assertNull(CentralAlarma.sesion.value)
    assertTrue(shadowOf(servicio).isStoppedBySelf)
    controlador.destroy()
  }

  @Test
  fun servicio_alTerminarConComprobacion_programaLaPregunta() {
    val conComprobacion = alarma.copy(comprobar = true, tareas = setOf(TipoTarea.CALCULO))
    val controlador = Robolectric.buildService(ServicioAlarma::class.java).create()
    val servicio = controlador.get()
    servicio.onStartCommand(ServicioAlarma.intentSonar(app, conComprobacion, Modo.ALARMA), 0, 1)
    shadowOf(Looper.getMainLooper()).idle()
    val antes = System.currentTimeMillis()
    servicio.onStartCommand(Intent(app, ServicioAlarma::class.java).setAction(ServicioAlarma.ACCION_TERMINAR), 0, 2)
    shadowOf(Looper.getMainLooper()).idle()
    // La pregunta, unos 10 minutos después.
    val pregunta = alarmManager.nextAlarmClock
    assertNotNull(pregunta)
    val minutos = (pregunta.triggerTime - antes) / 60_000.0
    assertTrue("a los $minutos min", minutos in 9.9..10.1)
    controlador.destroy()
  }

  @Test
  fun servicio_unaPrueba_noProgramaNada() {
    val conComprobacion = alarma.copy(comprobar = true)
    val controlador = Robolectric.buildService(ServicioAlarma::class.java).create()
    val servicio = controlador.get()
    servicio.onStartCommand(ServicioAlarma.intentSonar(app, conComprobacion, Modo.ALARMA, prueba = true), 0, 1)
    shadowOf(Looper.getMainLooper()).idle()
    assertTrue(CentralAlarma.sesion.value!!.prueba)
    servicio.onStartCommand(Intent(app, ServicioAlarma::class.java).setAction(ServicioAlarma.ACCION_TERMINAR), 0, 2)
    shadowOf(Looper.getMainLooper()).idle()
    assertNull(alarmManager.nextAlarmClock)
    controlador.destroy()
  }
}
