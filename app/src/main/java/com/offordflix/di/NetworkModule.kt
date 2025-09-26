package com.offordflix.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.offordflix.data.api.TmdbApi
import com.offordflix.data.local.WatchlistDataStore
import com.offordflix.data.repository.ContentDiscoveryRepository
import com.offordflix.data.repository.WatchlistRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module for network and content discovery dependencies.
 * 
 * Provides TMDB API, repositories, and related networking
 * components for content discovery functionality.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provide JSON configuration for serialization.
     */
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    /**
     * Provide OkHttpClient with logging.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provide Retrofit instance for TMDB API.
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(TmdbApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    /**
     * Provide TMDB API service.
     */
    @Provides
    @Singleton
    fun provideTmdbApi(retrofit: Retrofit): TmdbApi {
        return retrofit.create(TmdbApi::class.java)
    }

    /**
     * Provide ContentDiscoveryRepository.
     */
    @Provides
    @Singleton
    fun provideContentDiscoveryRepository(
        tmdbApi: TmdbApi,
        contentFilterRepository: com.offordflix.data.repository.ContentFilterRepository
    ): ContentDiscoveryRepository {
        return ContentDiscoveryRepository(tmdbApi, contentFilterRepository)
    }

    /**
     * Provide WatchlistRepository.
     */
    @Provides
    @Singleton
    fun provideWatchlistRepository(
        watchlistDataStore: WatchlistDataStore
    ): WatchlistRepository {
        return WatchlistRepository(watchlistDataStore)
    }

    /**
     * Provide WatchlistDataStore.
     */
    @Provides
    @Singleton
    fun provideWatchlistDataStore(
        context: android.content.Context
    ): WatchlistDataStore {
        return WatchlistDataStore(context)
    }
}

