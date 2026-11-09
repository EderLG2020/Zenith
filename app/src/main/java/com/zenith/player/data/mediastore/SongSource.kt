package com.zenith.player.data.mediastore

import com.zenith.player.domain.model.Song
import kotlinx.coroutines.flow.Flow

/** Origen de las canciones del teléfono. Existe para poder probar el repositorio sin MediaStore. */
interface SongSource {
    /** Consulta bloqueante: llamar fuera del hilo principal. */
    fun query(): List<Song>

    /** Emite cada vez que cambia la música del teléfono (archivo añadido, borrado o editado). */
    fun changes(): Flow<Unit>
}
