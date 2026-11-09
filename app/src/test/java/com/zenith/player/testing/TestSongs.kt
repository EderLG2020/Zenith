package com.zenith.player.testing

import com.zenith.player.domain.model.Song

/** Canción de ejemplo para los tests; solo se cambian los campos que importan en cada caso. */
fun testSong(
    id: Long = 1,
    title: String = "Canción $id",
    artist: String = "Budiarti",
    durationMs: Long = 200_000,
) = Song(
    id = id, title = title, artist = artist, album = "Noches", albumId = 1, durationMs = durationMs,
    uri = "content://media/external/audio/media/$id", artworkUri = "content://media/external/audio/albums/1",
    folder = "Music", dateAddedSec = 0, trackNumber = 1,
)
