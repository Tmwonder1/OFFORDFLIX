package com.offordflix.data.repository

import com.offordflix.data.local.WatchlistDataStore
import com.offordflix.domain.model.VideoContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing user watchlists and continue watching.
 * 
 * Handles adding/removing content from watchlist, continue watching
 * functionality, and watch progress tracking per profile.
 */
@Singleton
class WatchlistRepository @Inject constructor(
    private val watchlistDataStore: WatchlistDataStore
) {
    
    /**
     * Get watchlist for a profile.
     */
    fun getWatchlist(profileId: String): Flow<List<VideoContent>> {
        return watchlistDataStore.getWatchlist(profileId)
    }
    
    /**
     * Add content to watchlist.
     */
    suspend fun addToWatchlist(profileId: String, content: VideoContent): Result<Unit> {
        return try {
            watchlistDataStore.addToWatchlist(profileId, content)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove content from watchlist.
     */
    suspend fun removeFromWatchlist(profileId: String, contentId: String): Result<Unit> {
        return try {
            watchlistDataStore.removeFromWatchlist(profileId, contentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if content is in watchlist.
     */
    suspend fun isInWatchlist(profileId: String, contentId: String): Boolean {
        return try {
            watchlistDataStore.isInWatchlist(profileId, contentId)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get continue watching items for profile.
     */
    fun getContinueWatching(profileId: String): Flow<List<VideoContent>> {
        return watchlistDataStore.getContinueWatching(profileId)
    }
    
    /**
     * Update watch progress for content.
     */
    suspend fun updateWatchProgress(
        profileId: String,
        contentId: String,
        position: Long,
        duration: Long
    ): Result<Unit> {
        return try {
            watchlistDataStore.updateWatchProgress(profileId, contentId, position, duration)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get watch progress for content.
     */
    suspend fun getWatchProgress(profileId: String, contentId: String): Float {
        return try {
            watchlistDataStore.getWatchProgress(profileId, contentId)
        } catch (e: Exception) {
            0f
        }
    }
    
    /**
     * Clear all watchlist data for profile.
     */
    suspend fun clearWatchlist(profileId: String): Result<Unit> {
        return try {
            watchlistDataStore.clearWatchlist(profileId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get watchlist movies only for a profile.
     */
    fun getWatchlistMovies(profileId: String): Flow<List<VideoContent>> {
        return watchlistDataStore.getWatchlist(profileId).map { watchlist ->
            watchlist.filter { it.type == com.offordflix.domain.model.ContentType.MOVIE }
        }
    }
    
    /**
     * Get watchlist TV shows only for a profile.
     */
    fun getWatchlistTvShows(profileId: String): Flow<List<VideoContent>> {
        return watchlistDataStore.getWatchlist(profileId).map { watchlist ->
            watchlist.filter { it.type == com.offordflix.domain.model.ContentType.TV_SHOW }
        }
    }
}

