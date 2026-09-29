package com.oriol.alba.dominio

import kotlin.random.Random

// Tarea de foto: enseñarle a la cámara algo de casa que obligue a salir de la cama.
// El reconocedor es el modelo base de ML Kit (va dentro de la app, sin internet).
// Solo conoce unas 400 etiquetas en inglés: no sabe qué es una nevera ni un
// microondas, pero sí un fregadero, una taza o un sofá. Lista completa:
// https://developers.google.com/ml-kit/vision/image-labeling/label-map

/**
 * Lo que se puede pedir, con las etiquetas de ML Kit que lo confirman. Todo está
 * fuera del dormitorio (nada de almohadas ni cortinas).
 */
enum class ObjetoFoto(val etiquetas: Set<String>) {
  FREGADERO(setOf("Sink")),
  TAZA(setOf("Cup")),
  SOFA(setOf("Couch", "Loveseat")),
  TELE(setOf("Television")),
  ZAPATOS(setOf("Shoe", "Sneakers")),
  PLANTA(setOf("Plant", "Flowerpot", "Flora")),
  CUBIERTOS(setOf("Cutlery", "Tableware", "Saucer")),
  COCINA(setOf("Kitchen", "Countertop", "Cookware and bakeware")),
}

/** Una etiqueta que ve el reconocedor, con su confianza (0-1). */
data class Etiqueta(val texto: String, val confianza: Float)

/** Etiquetas que delatan una pantalla o una foto de una foto: si salen, no vale. */
private val etiquetasTrampa = setOf("Screenshot", "Web page")

/** Confianza mínima para dar por visto el objeto. */
const val ConfianzaMinima = 0.55f

/** Si en este fotograma se ve [objeto] (y no parece una pantalla). */
fun seVe(objeto: ObjetoFoto, etiquetas: List<Etiqueta>, umbral: Float = ConfianzaMinima): Boolean {
  if (etiquetas.any { it.texto in etiquetasTrampa && it.confianza >= 0.5f }) return false
  return etiquetas.any { it.texto in objeto.etiquetas && it.confianza >= umbral }
}

/**
 * Pide ver el objeto en varios fotogramas seguidos antes de darlo por bueno: un
 * acierto suelto puede ser casualidad.
 */
class DetectorSeguido(private val necesarios: Int = 3) {
  var seguidos = 0
    private set

  /** Añade un fotograma. Devuelve true cuando ya se ha visto las veces necesarias. */
  fun fotograma(visto: Boolean): Boolean {
    seguidos = if (visto) seguidos + 1 else 0
    return seguidos >= necesarios
  }
}

/** Un objeto al azar, distinto de [excepto]. */
fun objetoAlAzar(azar: Random = Random.Default, excepto: Set<ObjetoFoto> = emptySet()): ObjetoFoto {
  val posibles = ObjetoFoto.entries.filterNot { it in excepto }.ifEmpty { ObjetoFoto.entries }
  return posibles[azar.nextInt(posibles.size)]
}

/** Veces que se puede pedir otro objeto (por si no tienes tele o planta). */
const val CambiosDeObjeto = 2
