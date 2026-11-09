package com.zenith.player.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.zenith.player.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Lee las canciones de `MediaStore.Audio`. Solo lectura: nunca modifica los archivos. */
class MediaStoreSongSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : SongSource {

    override fun query(): List<Song> {
        val selection = "${Media.IS_MUSIC} != 0 AND ${Media.DURATION} >= ?"
        val args = arrayOf(MIN_DURATION_MS.toString())
        val sort = "${Media.TITLE} COLLATE NOCASE ASC"
        return context.contentResolver.query(AUDIO_URI, PROJECTION, selection, args, sort)
            ?.use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.toSong()) } }
            .orEmpty()
    }

    override fun changes(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        context.contentResolver.registerContentObserver(AUDIO_URI, true, observer)
        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }

    private fun Cursor.toSong(): Song {
        val id = long(Media._ID)
        val albumId = long(Media.ALBUM_ID)
        val displayName = string(Media.DISPLAY_NAME).orEmpty()
        return Song(
            id = id,
            title = string(Media.TITLE)?.takeIf { it.isNotBlank() } ?: displayName.substringBeforeLast('.'),
            artist = string(Media.ARTIST).orUnknown("Artista desconocido"),
            album = string(Media.ALBUM).orUnknown("Álbum desconocido"),
            albumId = albumId,
            durationMs = long(Media.DURATION),
            uri = ContentUris.withAppendedId(AUDIO_URI, id).toString(),
            // Coil reconoce esta forma de URI y usa loadThumbnail en Android 10+.
            artworkUri = ContentUris.withAppendedId(ALBUMS_URI, albumId).toString(),
            folder = folder(),
            dateAddedSec = long(Media.DATE_ADDED),
            trackNumber = getInt(getColumnIndexOrThrow(Media.TRACK)),
        )
    }

    @Suppress("DEPRECATION") // DATA solo se usa en Android 9 o anterior, donde no existe RELATIVE_PATH.
    private fun Cursor.folder(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            string(Media.RELATIVE_PATH).orEmpty().trimEnd('/')
        } else {
            string(Media.DATA)?.let { File(it).parent }.orEmpty()
        }

    private fun Cursor.string(column: String): String? = getString(getColumnIndexOrThrow(column))
    private fun Cursor.long(column: String): Long = getLong(getColumnIndexOrThrow(column))

    private fun String?.orUnknown(fallback: String): String =
        if (this.isNullOrBlank() || this == MediaStore.UNKNOWN_STRING) fallback else this

    private companion object {
        /** Ignora notas de voz y tonos (diseño §5.4). Será configurable en la fase 8. */
        const val MIN_DURATION_MS = 30_000L

        val AUDIO_URI: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val ALBUMS_URI: Uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI

        @Suppress("DEPRECATION")
        val PROJECTION: Array<String> = buildList {
            addAll(
                listOf(
                    Media._ID, Media.TITLE, Media.ARTIST, Media.ALBUM, Media.ALBUM_ID, Media.DURATION,
                    Media.DISPLAY_NAME, Media.DATE_ADDED, Media.TRACK,
                ),
            )
            add(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Media.RELATIVE_PATH else Media.DATA)
        }.toTypedArray()
    }
}

private typealias Media = MediaStore.Audio.Media
