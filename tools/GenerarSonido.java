import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Genera el sonido "Amanecer" de Alba: un carillón suave (arpegio de mi mayor con
 * timbre de campana) que se repite sin cortes. Es nuestro: no hay licencias.
 *
 * Uso (desde la raíz del proyecto):
 *   java tools/GenerarSonido.java app/src/main/res/raw/amanecer.wav
 *
 * El bucle se construye "en círculo": la cola de las últimas notas que se sale del
 * final se suma al principio, así al repetirse no hay salto.
 */
public class GenerarSonido {

  static final int FRECUENCIA_MUESTREO = 44_100;
  static final double DURACION_BUCLE = 2.4; // segundos

  // Notas: momento (s), frecuencia (Hz), intensidad
  static final double[][] NOTAS = {
    {0.00, 659.26, 0.80}, // mi5
    {0.18, 830.61, 0.70}, // sol#5
    {0.36, 987.77, 0.70}, // si5
    {0.54, 1318.51, 0.75}, // mi6
    {1.20, 987.77, 0.55}, // si5
    {1.38, 1318.51, 0.60}, // mi6
  };

  // Parciales de campana: múltiplo de la fundamental, intensidad y caída (s)
  static final double[][] PARCIALES = {
    {1.00, 1.00, 1.40},
    {2.00, 0.38, 0.80},
    {3.00, 0.16, 0.45},
    {4.07, 0.07, 0.25},
  };

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.err.println("Uso: java tools/GenerarSonido.java salida.wav");
      System.exit(1);
    }
    int n = (int) Math.round(DURACION_BUCLE * FRECUENCIA_MUESTREO);
    double[] mezcla = new double[n];
    // Cada nota suena 4 s como mucho; lo que pase del final vuelve al principio.
    int largoNota = 4 * FRECUENCIA_MUESTREO;
    for (double[] nota : NOTAS) {
      int inicio = (int) Math.round(nota[0] * FRECUENCIA_MUESTREO);
      for (int i = 0; i < largoNota; i++) {
        double t = (double) i / FRECUENCIA_MUESTREO;
        double ataque = Math.min(1.0, t / 0.004); // 4 ms: sin chasquido al empezar
        double valor = 0;
        for (double[] p : PARCIALES) {
          valor += p[1] * Math.exp(-t / p[2]) * Math.sin(2 * Math.PI * nota[1] * p[0] * t);
        }
        mezcla[(inicio + i) % n] += nota[2] * ataque * valor;
      }
    }
    // Normaliza a -1 dBFS.
    double pico = 0;
    for (double v : mezcla) pico = Math.max(pico, Math.abs(v));
    double escala = Math.pow(10, -1.0 / 20) / pico;

    ByteArrayOutputStream datos = new ByteArrayOutputStream();
    for (double v : mezcla) {
      short s = (short) Math.round(v * escala * Short.MAX_VALUE);
      datos.write(s & 0xff);
      datos.write((s >> 8) & 0xff);
    }
    byte[] pcm = datos.toByteArray();

    // Cabecera WAV: PCM 16 bits, mono.
    ByteBuffer cab = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN);
    cab.put("RIFF".getBytes()).putInt(36 + pcm.length).put("WAVE".getBytes());
    cab.put("fmt ".getBytes()).putInt(16).putShort((short) 1).putShort((short) 1);
    cab.putInt(FRECUENCIA_MUESTREO).putInt(FRECUENCIA_MUESTREO * 2).putShort((short) 2).putShort((short) 16);
    cab.put("data".getBytes()).putInt(pcm.length);

    try (FileOutputStream salida = new FileOutputStream(args[0])) {
      salida.write(cab.array());
      salida.write(pcm);
    }
    System.out.printf("Escrito %s: %.1f s, %d bytes%n", args[0], DURACION_BUCLE, 44 + pcm.length);
  }
}
