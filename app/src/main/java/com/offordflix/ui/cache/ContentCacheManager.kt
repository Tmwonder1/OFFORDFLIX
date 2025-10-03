package com.offordflix.ui.cache

import com.offordflix.data.dto.HomeContentResponse
import com.offordflix.data.dto.MoviesContentResponse
import com.offordflix.data.dto.TVShowsContentResponse
import com.offordflix.domain.model.VideoContent

/**
 * Simple in-memory content cache to prevent reloading when navigating between screens.
 * This cache persists across ViewModel recreations.
 */
object ContentCacheManager {
    
    private const val CACHE_DURATION_MS = 20 * 60 * 1000L // 20 minutes
    
    // Home content cache
    private var homeContent: HomeContentResponse? = null
    private var homeContentTimestamp: Long = 0
    private var homeEnhancedContent: List<VideoContent>? = null
    
    // Movies content cache  
    private var moviesContent: MoviesContentResponse? = null
    private var moviesContentTimestamp: Long = 0
    private var moviesEnhancedContent: List<VideoContent>? = null
    
    // TV Shows content cache
    private var tvShowsContent: TVShowsContentResponse? = null
    private var tvShowsContentTimestamp: Long = 0
    private var tvShowsEnhancedContent: List<VideoContent>? = null
    
    private fun isValid(timestamp: Long): Boolean {
        return (System.currentTimeMillis() - timestamp) < CACHE_DURATION_MS
    }
    
    // Home content methods
    fun getCachedHomeContent(): HomeContentResponse? {
        return if (isValid(homeContentTimestamp)) homeContent else null
    }
    
    fun setCachedHomeContent(content: HomeContentResponse) {
        homeContent = content
        homeContentTimestamp = System.currentTimeMillis()
    }
    
    fun getCachedHomeEnhancedContent(): List<VideoContent>? {
        return if (isValid(homeContentTimestamp)) homeEnhancedContent else null
    }
    
    fun setCachedHomeEnhancedContent(content: List<VideoContent>) {
        homeEnhancedContent = content
        homeContentTimestamp = System.currentTimeMillis()
    }
    
    // Movies content methods
    fun getCachedMoviesContent(): MoviesContentResponse? {
        return if (isValid(moviesContentTimestamp)) moviesContent else null
    }
    
    fun setCachedMoviesContent(content: MoviesContentResponse) {
        moviesContent = content
        moviesContentTimestamp = System.currentTimeMillis()
    }
    
    fun getCachedMoviesEnhancedContent(): List<VideoContent>? {
        return if (isValid(moviesContentTimestamp)) moviesEnhancedContent else null
    }
    
    fun setCachedMoviesEnhancedContent(content: List<VideoContent>) {
        moviesEnhancedContent = content
        moviesContentTimestamp = System.currentTimeMillis()
    }
    
    // TV Shows content methods
    fun getCachedTVShowsContent(): TVShowsContentResponse? {
        return if (isValid(tvShowsContentTimestamp)) tvShowsContent else null
    }
    
    fun setCachedTVShowsContent(content: TVShowsContentResponse) {
        tvShowsContent = content
        tvShowsContentTimestamp = System.currentTimeMillis()
    }
    
    fun getCachedTVShowsEnhancedContent(): List<VideoContent>? {
        return if (isValid(tvShowsContentTimestamp)) tvShowsEnhancedContent else null
    }
    
    fun setCachedTVShowsEnhancedContent(content: List<VideoContent>) {
        tvShowsEnhancedContent = content
        tvShowsContentTimestamp = System.currentTimeMillis()
    }
    
    // Clear all cache
    fun clearAll() {
        homeContent = null
        homeContentTimestamp = 0
        homeEnhancedContent = null
        
        moviesContent = null
        moviesContentTimestamp = 0
        moviesEnhancedContent = null
        
        tvShowsContent = null
        tvShowsContentTimestamp = 0
        tvShowsEnhancedContent = null
    }
}
