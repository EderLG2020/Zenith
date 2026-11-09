package com.zenith.player.playback

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.zenith.player.domain.model.NowPlaying
import com.zenith.player.domain.model.Song

/**
 * Canción → elemento de Media3. La URI va también en `requestMetadata`, porque el `MediaController`
 * no envía la URI de reproducción a la sesión (ver [resolveMediaItems]).
 */
fun Song.toMediaItem(): MediaItem {
    val songUri = Uri.parse(uri)
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(songUri)
        .setRequestMetadata(MediaItem.RequestMetadata.Builder().setMediaUri(songUri).build())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(Uri.parse(artworkUri))
                .setTrackNumber(trackNumber)
                .setIsPlayable(true)
                .setIsBrowsable(false)
                .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                .build(),
        )
        .build()
}

/** Lado de la sesión: vuelve a poner la URI de reproducción a partir de `requestMetadata`. */
fun resolveMediaItems(items: List<MediaItem>): List<MediaItem> = items.map { item ->
    val uri = item.requestMetadata.mediaUri ?: return@map item
    item.buildUpon().setUri(uri).build()
}

/** Lo que suena, leído de los metadatos (sirve aunque la cola venga de una sesión anterior de la app). */
fun MediaItem.toNowPlaying(): NowPlaying? {
    val songId = mediaId.toLongOrNull() ?: return null
    return NowPlaying(
        songId = songId,
        title = mediaMetadata.title?.toString().orEmpty(),
        artist = mediaMetadata.artist?.toString().orEmpty(),
        album = mediaMetadata.albumTitle?.toString().orEmpty(),
        artworkUri = mediaMetadata.artworkUri?.toString().orEmpty(),
    )
}
