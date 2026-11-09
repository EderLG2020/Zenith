package com.zenith.player.di

import com.zenith.player.data.mediastore.MediaStoreSongSource
import com.zenith.player.data.mediastore.SongSource
import com.zenith.player.data.repository.SongRepositoryImpl
import com.zenith.player.domain.repository.SongRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindSongRepository(impl: SongRepositoryImpl): SongRepository

    @Binds
    abstract fun bindSongSource(impl: MediaStoreSongSource): SongSource
}
