package com.offordflix.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.offordflix.data.api.TmdbApi
import com.offordflix.data.api.OffordflixApi
import com.offordflix.data.local.WatchlistDataStore
import com.offordflix.data.repository.ContentDiscoveryRepository
import com.offordflix.data.repository.WatchlistRepository
import com.offordflix.data.repository.ContentFilterRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Named
import android.content.Context
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
     * Provide Retrofit instance for Offordflix Backend API.
     */
    @Provides
    @Singleton
    @Named("offordflix")
    fun provideOffordflixRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(OffordflixApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    /**
     * Provide Offordflix Backend API service.
     */
    @Provides
    @Singleton
    fun provideOffordflixApi(@Named("offordflix") retrofit: Retrofit): OffordflixApi {
        return retrofit.create(OffordflixApi::class.java)
    }

    /**
     * Provide ContentFilterRepository.
     */
    @Provides
    @Singleton
    fun provideContentFilterRepository(): ContentFilterRepository {
        return ContentFilterRepository()
    }

    /**
     * Provide ContentDiscoveryRepository.
     */
    @Provides
    @Singleton
    fun provideContentDiscoveryRepository(
        tmdbApi: TmdbApi,
        offordflixApi: OffordflixApi,
        contentFilterRepository: ContentFilterRepository
    ): ContentDiscoveryRepository {
        return ContentDiscoveryRepository(tmdbApi, offordflixApi, contentFilterRepository)
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
        @ApplicationContext context: Context
    ): WatchlistDataStore {
        return WatchlistDataStore(context)
    }
}

