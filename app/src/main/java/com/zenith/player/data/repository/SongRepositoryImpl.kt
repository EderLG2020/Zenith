package com.zenith.player.data.repository

import com.zenith.player.data.mediastore.SongSource
import com.zenith.player.di.IoDispatcher
import com.zenith.player.domain.model.Song
import com.zenith.player.domain.repository.SongRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class SongRepositoryImpl @Inject constructor(
    private val source: SongSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SongRepository {

    // conflate: si llegan muchos cambios seguidos (copiar un álbum entero), solo se vuelve a consultar una vez.
    override fun observeSongs(): Flow<List<Song>> = source.changes()
        .onStart { emit(Unit) }
        .conflate()
        .map { source.query() }
        .flowOn(ioDispatcher)
}
