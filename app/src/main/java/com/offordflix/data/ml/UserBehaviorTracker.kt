package com.offordflix.data.ml

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
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ML-powered user behavior tracking system.
 * 
 * Tracks user interactions, viewing patterns, and preferences
 * to feed into recommendation algorithms for personalized content.
 */
@Singleton
class UserBehaviorTracker @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private val Context.behaviorDataStore: DataStore<Preferences> by preferencesDataStore(
            name = "user_behavior"
        )
    }
    
    private val dataStore = context.behaviorDataStore
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Track content view interaction.
     */
    suspend fun trackContentView(
        profileId: String,
        content: VideoContent,
        durationMs: Long = 0,
        interactionType: InteractionType = InteractionType.VIEW
    ) {
        val key = stringPreferencesKey("behavior_$profileId")
        dataStore.edit { preferences ->
            val currentBehaviorJson = preferences[key] ?: ""
            val currentBehavior = if (currentBehaviorJson.isBlank()) {
                UserBehaviorData()
            } else {
                try {
                    json.decodeFromString<UserBehaviorData>(currentBehaviorJson)
                } catch (e: Exception) {
                    UserBehaviorData()
                }
            }
            
            val interaction = UserInteraction(
                contentId = content.id,
                contentType = content.type,
                genres = content.genres,
                rating = content.voteAverage,
                interactionType = interactionType,
                timestamp = System.currentTimeMillis(),
                durationMs = durationMs
            )
            
            val updatedBehavior = currentBehavior.copy(
                interactions = (currentBehavior.interactions + interaction)
                    .sortedByDescending { it.timestamp }
                    .take(1000), // Keep last 1000 interactions
                lastUpdated = System.currentTimeMillis()
            )
            
            preferences[key] = json.encodeToString(updatedBehavior)
        }
        
        // Update genre preferences
        updateGenrePreferences(profileId, content.genres, interactionType)
        
        // Update content type preferences
        updateContentTypePreferences(profileId, content.type, interactionType)
    }
    
    /**
     * Track search behavior.
     */
    suspend fun trackSearch(profileId: String, query: String, resultsCount: Int) {
        val key = stringPreferencesKey("search_behavior_$profileId")
        dataStore.edit { preferences ->
            val currentSearchJson = preferences[key] ?: ""
            val currentSearchData = if (currentSearchJson.isBlank()) {
                SearchBehaviorData()
            } else {
                try {
                    json.decodeFromString<SearchBehaviorData>(currentSearchJson)
                } catch (e: Exception) {
                    SearchBehaviorData()
                }
            }
            
            val searchQuery = SearchQuery(
                query = query,
                timestamp = System.currentTimeMillis(),
                resultsCount = resultsCount
            )
            
            val updatedSearchData = currentSearchData.copy(
                queries = (currentSearchData.queries + searchQuery)
                    .sortedByDescending { it.timestamp }
                    .take(100), // Keep last 100 searches
                lastUpdated = System.currentTimeMillis()
            )
            
            preferences[key] = json.encodeToString(updatedSearchData)
        }
    }
    
    /**
     * Get user behavior data for recommendations.
     */
    fun getUserBehavior(profileId: String): Flow<UserBehaviorData?> {
        val key = stringPreferencesKey("behavior_$profileId")
        return dataStore.data.map { preferences ->
            val behaviorJson = preferences[key] ?: ""
            if (behaviorJson.isBlank()) {
                null
            } else {
                try {
                    json.decodeFromString<UserBehaviorData>(behaviorJson)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
    
    /**
     * Get genre preferences for a profile.
     */
    fun getGenrePreferences(profileId: String): Flow<Map<String, Float>> {
        val key = stringPreferencesKey("genre_prefs_$profileId")
        return dataStore.data.map { preferences ->
            val prefsJson = preferences[key] ?: ""
            if (prefsJson.isBlank()) {
                emptyMap()
            } else {
                try {
                    json.decodeFromString<Map<String, Float>>(prefsJson)
                } catch (e: Exception) {
                    emptyMap()
                }
            }
        }
    }
    
    /**
     * Get content type preferences.
     */
    fun getContentTypePreferences(profileId: String): Flow<Map<ContentType, Float>> {
        val key = stringPreferencesKey("content_type_prefs_$profileId")
        return dataStore.data.map { preferences ->
            val prefsJson = preferences[key] ?: ""
            if (prefsJson.isBlank()) {
                mapOf(ContentType.MOVIE to 0.5f, ContentType.TV_SHOW to 0.5f)
            } else {
                try {
                    val stringMap = json.decodeFromString<Map<String, Float>>(prefsJson)
                    stringMap.mapKeys { 
                        if (it.key == "MOVIE") ContentType.MOVIE else ContentType.TV_SHOW 
                    }
                } catch (e: Exception) {
                    mapOf(ContentType.MOVIE to 0.5f, ContentType.TV_SHOW to 0.5f)
                }
            }
        }
    }
    
    /**
     * Update genre preferences based on interaction.
     */
    private suspend fun updateGenrePreferences(
        profileId: String,
        genres: List<String>,
        interactionType: InteractionType
    ) {
        val key = stringPreferencesKey("genre_prefs_$profileId")
        dataStore.edit { preferences ->
            val currentPrefsJson = preferences[key] ?: ""
            val currentPrefs = if (currentPrefsJson.isBlank()) {
                mutableMapOf<String, Float>()
            } else {
                try {
                    json.decodeFromString<Map<String, Float>>(currentPrefsJson).toMutableMap()
                } catch (e: Exception) {
                    mutableMapOf()
                }
            }
            
            val weight = when (interactionType) {
                InteractionType.WATCH -> 1.0f
                InteractionType.LIKE -> 0.8f
                InteractionType.VIEW -> 0.3f
                InteractionType.SKIP -> -0.2f
                InteractionType.DISLIKE -> -0.5f
            }
            
            genres.forEach { genre ->
                val currentScore = currentPrefs[genre] ?: 0f
                val newScore = (currentScore + weight).coerceIn(-2f, 2f)
                currentPrefs[genre] = newScore
            }
            
            preferences[key] = json.encodeToString(currentPrefs)
        }
    }
    
    /**
     * Update content type preferences.
     */
    private suspend fun updateContentTypePreferences(
        profileId: String,
        contentType: ContentType,
        interactionType: InteractionType
    ) {
        val key = stringPreferencesKey("content_type_prefs_$profileId")
        dataStore.edit { preferences ->
            val currentPrefsJson = preferences[key] ?: ""
            val currentPrefs = if (currentPrefsJson.isBlank()) {
                mutableMapOf("MOVIE" to 0.5f, "TV_SHOW" to 0.5f)
            } else {
                try {
                    json.decodeFromString<Map<String, Float>>(currentPrefsJson).toMutableMap()
                } catch (e: Exception) {
                    mutableMapOf("MOVIE" to 0.5f, "TV_SHOW" to 0.5f)
                }
            }
            
            val weight = when (interactionType) {
                InteractionType.WATCH -> 0.1f
                InteractionType.LIKE -> 0.05f
                InteractionType.VIEW -> 0.02f
                InteractionType.SKIP -> -0.01f
                InteractionType.DISLIKE -> -0.02f
            }
            
            val typeKey = contentType.name
            val currentScore = currentPrefs[typeKey] ?: 0.5f
            val newScore = (currentScore + weight).coerceIn(0f, 1f)
            currentPrefs[typeKey] = newScore
            
            // Normalize to ensure they sum to 1
            val total = currentPrefs.values.sum()
            if (total > 0) {
                currentPrefs.replaceAll { _, value -> value / total }
            }
            
            preferences[key] = json.encodeToString(currentPrefs)
        }
    }
    
    /**
     * Get viewing time patterns for recommendations.
     */
    suspend fun getViewingPatterns(profileId: String): ViewingPatterns {
        val behavior = getUserBehavior(profileId).first()
        
        if (behavior == null || behavior.interactions.isEmpty()) {
            return ViewingPatterns()
        }
        
        val interactions = behavior.interactions
        val totalWatchTime = interactions
            .filter { it.interactionType == InteractionType.WATCH }
            .sumOf { it.durationMs }
        
        val favoriteGenres = getTopGenres(interactions, 5)
        val averageRating = interactions
            .filter { it.rating > 0 }
            .map { it.rating }
            .average()
            .takeIf { !it.isNaN() } ?: 0.0
        
        val contentTypeDistribution = interactions
            .groupBy { it.contentType }
            .mapValues { it.value.size.toFloat() / interactions.size }
        
        return ViewingPatterns(
            totalWatchTimeMs = totalWatchTime,
            favoriteGenres = favoriteGenres,
            averageRatingPreference = averageRating,
            contentTypeDistribution = contentTypeDistribution,
            lastActiveTimestamp = interactions.maxOfOrNull { it.timestamp } ?: 0L
        )
    }
    
    /**
     * Get top genres from interactions.
     */
    private fun getTopGenres(interactions: List<UserInteraction>, limit: Int): List<String> {
        return interactions
            .flatMap { it.genres }
            .groupBy { it }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }
    
    /**
     * Clear behavior data for profile.
     */
    suspend fun clearBehaviorData(profileId: String) {
        val behaviorKey = stringPreferencesKey("behavior_$profileId")
        val genreKey = stringPreferencesKey("genre_prefs_$profileId")
        val contentKey = stringPreferencesKey("content_type_prefs_$profileId")
        val searchKey = stringPreferencesKey("search_behavior_$profileId")
        
        dataStore.edit { preferences ->
            preferences.remove(behaviorKey)
            preferences.remove(genreKey)
            preferences.remove(contentKey)
            preferences.remove(searchKey)
        }
    }
}

/**
 * Types of user interactions for ML learning.
 */
enum class InteractionType {
    VIEW,     // Content was viewed/clicked
    WATCH,    // Content was actually watched
    LIKE,     // Content was added to watchlist
    DISLIKE,  // Content was explicitly disliked
    SKIP      // Content was skipped quickly
}

/**
 * Serializable data classes for behavior tracking.
 */
@Serializable
data class UserBehaviorData(
    val interactions: List<UserInteraction> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

@Serializable
data class UserInteraction(
    val contentId: String,
    val contentType: ContentType,
    val genres: List<String>,
    val rating: Double,
    val interactionType: InteractionType,
    val timestamp: Long,
    val durationMs: Long = 0
)

@Serializable
data class SearchBehaviorData(
    val queries: List<SearchQuery> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

@Serializable
data class SearchQuery(
    val query: String,
    val timestamp: Long,
    val resultsCount: Int
)

/**
 * User viewing patterns analysis.
 */
data class ViewingPatterns(
    val totalWatchTimeMs: Long = 0L,
    val favoriteGenres: List<String> = emptyList(),
    val averageRatingPreference: Double = 0.0,
    val contentTypeDistribution: Map<ContentType, Float> = emptyMap(),
    val lastActiveTimestamp: Long = 0L
)

