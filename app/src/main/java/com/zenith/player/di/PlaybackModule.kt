package com.zenith.player.di

import com.zenith.player.domain.player.MusicPlayer
import com.zenith.player.playback.PlayerController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PlaybackModule {
    @Binds
    abstract fun bindMusicPlayer(impl: PlayerController): MusicPlayer
}
