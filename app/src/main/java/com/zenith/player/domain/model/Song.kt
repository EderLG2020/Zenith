package com.zenith.player.domain.model

/**
 * Una canción guardada en el teléfono. Las URIs van como texto para que el dominio no dependa de Android.
 *
 * @property artworkUri URI de la portada del álbum; puede no tener imagen (la UI muestra un placeholder).
 * @property folder ruta relativa de la carpeta, por ejemplo `Music/Rock`.
 * @property dateAddedSec fecha en que el archivo entró al teléfono, en segundos desde 1970.
 */
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val uri: String,
    val artworkUri: String,
    val folder: String,
    val dateAddedSec: Long,
    val trackNumber: Int,
)
