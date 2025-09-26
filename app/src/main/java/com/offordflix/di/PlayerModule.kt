package com.offordflix.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.offordflix.data.repository.VideoPlayerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for video player dependencies.
 * 
 * Provides ExoPlayer, VideoPlayerRepository, and related
 * dependencies for video playback functionality.
 */
@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {
    
    /**
     * Provide VideoPlayerRepository singleton.
     */
    @Provides
    @Singleton
    fun provideVideoPlayerRepository(): VideoPlayerRepository {
        return VideoPlayerRepository()
    }
    
    /**
     * Provide ExoPlayer factory.
     * Note: ExoPlayer instances should be created per-screen for proper lifecycle management.
     */
    @Provides
    fun provideExoPlayerFactory(
        @ApplicationContext context: Context
    ): () -> ExoPlayer {
        return { ExoPlayer.Builder(context).build() }
    }
}

