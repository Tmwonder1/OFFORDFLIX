package com.offordflix.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.serialization.json.Json
import com.offordflix.domain.model.VideoContent
import com.offordflix.domain.model.ContentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore for watchlist and watch progress management.
 * 
 * Stores watchlist items and watch progress per profile using
 * Preferences DataStore with JSON serialization.
 */
@Singleton
class WatchlistDataStore @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private val Context.watchlistDataStore: DataStore<Preferences> by preferencesDataStore(
            name = "watchlist_preferences"
        )
    }
    
    private val dataStore = context.watchlistDataStore
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Get watchlist for a profile.
     */
    fun getWatchlist(profileId: String): Flow<List<VideoContent>> {
        val key = stringPreferencesKey("watchlist_$profileId")
        return dataStore.data.map { preferences ->
            val watchlistJson = preferences[key] ?: ""
            if (watchlistJson.isBlank()) {
                emptyList()
            } else {
                try {
                    val watchlistData = json.decodeFromString<WatchlistData>(watchlistJson)
                    watchlistData.items.map { it.toVideoContent() }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
    }
    
    /**
     * Add content to watchlist.
     */
    suspend fun addToWatchlist(profileId: String, content: VideoContent) {
        val key = stringPreferencesKey("watchlist_$profileId")
        dataStore.edit { preferences ->
            val currentWatchlistJson = preferences[key] ?: ""
            val currentWatchlist = if (currentWatchlistJson.isBlank()) {
                WatchlistData(emptyList())
            } else {
                try {
                    json.decodeFromString<WatchlistData>(currentWatchlistJson)
                } catch (e: Exception) {
                    WatchlistData(emptyList())
                }
            }
            
            // Add if not already present
            val contentItem = WatchlistItem.fromVideoContent(content)
            if (!currentWatchlist.items.any { it.id == content.id }) {
                val updatedWatchlist = WatchlistData(
                    currentWatchlist.items + contentItem
                )
                preferences[key] = json.encodeToString(updatedWatchlist)
            }
        }
    }
    
    /**
     * Remove content from watchlist.
     */
    suspend fun removeFromWatchlist(profileId: String, contentId: String) {
        val key = stringPreferencesKey("watchlist_$profileId")
        dataStore.edit { preferences ->
            val currentWatchlistJson = preferences[key] ?: ""
            if (currentWatchlistJson.isNotBlank()) {
                try {
                    val currentWatchlist = json.decodeFromString<WatchlistData>(currentWatchlistJson)
                    val updatedWatchlist = WatchlistData(
                        currentWatchlist.items.filter { it.id != contentId }
                    )
                    preferences[key] = json.encodeToString(updatedWatchlist)
                } catch (e: Exception) {
                    // Ignore parsing errors
                }
            }
        }
    }
    
    /**
     * Check if content is in watchlist.
     */
    suspend fun isInWatchlist(profileId: String, contentId: String): Boolean {
        val key = stringPreferencesKey("watchlist_$profileId")
        val preferences = dataStore.data.first()
        val watchlistJson = preferences[key] ?: ""
        
        return if (watchlistJson.isBlank()) {
            false
        } else {
            try {
                val watchlistData = json.decodeFromString<WatchlistData>(watchlistJson)
                watchlistData.items.any { it.id == contentId }
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Get continue watching items.
     */
    fun getContinueWatching(profileId: String): Flow<List<VideoContent>> {
        val key = stringPreferencesKey("continue_watching_$profileId")
        return dataStore.data.map { preferences ->
            val continueWatchingJson = preferences[key] ?: ""
            if (continueWatchingJson.isBlank()) {
                emptyList()
            } else {
                try {
                    val continueWatchingData = json.decodeFromString<ContinueWatchingData>(continueWatchingJson)
                    continueWatchingData.items.map { item ->
                        item.content.toVideoContent()
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
    }
    
    /**
     * Update watch progress.
     */
    suspend fun updateWatchProgress(
        profileId: String,
        contentId: String,
        position: Long,
        duration: Long
    ) {
        val key = stringPreferencesKey("continue_watching_$profileId")
        dataStore.edit { preferences ->
            val currentJson = preferences[key] ?: ""
            val currentData = if (currentJson.isBlank()) {
                ContinueWatchingData(emptyList())
            } else {
                try {
                    json.decodeFromString<ContinueWatchingData>(currentJson)
                } catch (e: Exception) {
                    ContinueWatchingData(emptyList())
                }
            }
            
            // Update existing or add new progress entry
            val updatedItems = currentData.items.toMutableList()
            val existingIndex = updatedItems.indexOfFirst { it.content.id == contentId }
            
            val progressItem = ContinueWatchingItem(
                content = if (existingIndex >= 0) {
                    updatedItems[existingIndex].content
                } else {
                    // Create minimal content item for progress tracking
                    WatchlistItem(
                        id = contentId,
                        title = "Unknown",
                        type = "movie",
                        tmdbId = "",
                        posterUrl = null,
                        backdropUrl = null,
                        overview = null,
                        releaseDate = null,
                        voteAverage = 0.0,
                        genres = emptyList()
                    )
                },
                position = position,
                duration = duration,
                lastWatched = System.currentTimeMillis()
            )
            
            if (existingIndex >= 0) {
                updatedItems[existingIndex] = progressItem
            } else {
                updatedItems.add(progressItem)
            }
            
            // Keep only recent items (max 20)
            val sortedItems = updatedItems.sortedByDescending { it.lastWatched }.take(20)
            val updatedData = ContinueWatchingData(sortedItems)
            
            preferences[key] = json.encodeToString(updatedData)
        }
    }
    
    /**
     * Get watch progress percentage.
     */
    suspend fun getWatchProgress(profileId: String, contentId: String): Float {
        val key = stringPreferencesKey("continue_watching_$profileId")
        val preferences = dataStore.data.first()
        val continueWatchingJson = preferences[key] ?: ""
        
        return if (continueWatchingJson.isBlank()) {
            0f
        } else {
            try {
                val continueWatchingData = json.decodeFromString<ContinueWatchingData>(continueWatchingJson)
                val item = continueWatchingData.items.find { it.content.id == contentId }
                if (item != null && item.duration > 0) {
                    (item.position.toFloat() / item.duration).coerceIn(0f, 1f)
                } else {
                    0f
                }
            } catch (e: Exception) {
                0f
            }
        }
    }
    
    /**
     * Clear watchlist for profile.
     */
    suspend fun clearWatchlist(profileId: String) {
        val watchlistKey = stringPreferencesKey("watchlist_$profileId")
        val continueKey = stringPreferencesKey("continue_watching_$profileId")
        
        dataStore.edit { preferences ->
            preferences.remove(watchlistKey)
            preferences.remove(continueKey)
        }
    }
}

/**
 * Serializable data classes for DataStore.
 */
@Serializable
data class WatchlistData(
    val items: List<WatchlistItem>
)

@Serializable
data class WatchlistItem(
    val id: String,
    val title: String,
    val type: String, // "movie" or "tv"
    val tmdbId: String,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val overview: String? = null,
    val releaseDate: String? = null,
    val voteAverage: Double = 0.0,
    val genres: List<String> = emptyList()
) {
    fun toVideoContent(): VideoContent {
        return VideoContent(
            id = id,
            title = title,
            type = if (type == "movie") ContentType.MOVIE else ContentType.TV_SHOW,
            tmdbId = tmdbId,
            posterUrl = posterUrl,
            backdropUrl = backdropUrl,
            overview = overview,
            releaseDate = releaseDate,
            voteAverage = voteAverage,
            genres = genres
        )
    }
    
    companion object {
        fun fromVideoContent(content: VideoContent): WatchlistItem {
            return WatchlistItem(
                id = content.id,
                title = content.title,
                type = if (content.type == ContentType.MOVIE) "movie" else "tv",
                tmdbId = content.tmdbId,
                posterUrl = content.posterUrl,
                backdropUrl = content.backdropUrl,
                overview = content.overview,
                releaseDate = content.releaseDate,
                voteAverage = content.voteAverage,
                genres = content.genres
            )
        }
    }
}

@Serializable
data class ContinueWatchingData(
    val items: List<ContinueWatchingItem>
)

@Serializable
data class ContinueWatchingItem(
    val content: WatchlistItem,
    val position: Long,
    val duration: Long,
    val lastWatched: Long
)

