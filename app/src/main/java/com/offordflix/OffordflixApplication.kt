package com.offordflix

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Main application class for Offordflix Android TV app.
 * 
 * This class serves as the entry point for the application and sets up
 * Hilt dependency injection for the entire app.
 */
@HiltAndroidApp
class OffordflixApplication : Application(), ImageLoaderFactory {
    
    override fun onCreate() {
        super.onCreate()
        // Application initialization will go here
    }
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Use 25% of available memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100 * 1024 * 1024) // 100MB disk cache
                    .build()
            }
            .okHttpClient {
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false) // Ignore server cache headers for better caching
            .allowHardware(true) // Use hardware bitmaps when possible
            .crossfade(200) // Smooth crossfade animation
            .logger(DebugLogger()) // Enable logging for debugging
            .build()
    }
}


