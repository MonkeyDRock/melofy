package com.example.melofy.di

import com.example.melofy.data.repository.AuthRepositoryImpl
import com.example.melofy.data.repository.MusicRepositoryImpl
import com.example.melofy.data.repository.PlaylistRepositoryImpl
import com.example.melofy.data.repository.AiRepositoryImpl
import com.example.melofy.domain.repository.AuthRepository
import com.example.melofy.domain.repository.MusicRepository
import com.example.melofy.domain.repository.PlaylistRepository
import com.example.melofy.domain.repository.AiRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        musicRepositoryImpl: MusicRepositoryImpl
    ): MusicRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        playlistRepositoryImpl: PlaylistRepositoryImpl
    ): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(
        aiRepositoryImpl: AiRepositoryImpl
    ): AiRepository
}
