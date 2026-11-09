package com.zenith.player.domain.repository

import com.zenith.player.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    /** Canciones ordenadas por título; vuelve a emitir cuando cambia la biblioteca del teléfono. */
    fun observeSongs(): Flow<List<Song>>
}
