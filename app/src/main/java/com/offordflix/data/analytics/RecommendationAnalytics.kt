package com.offordflix.data.analytics

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
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
 * Analytics and performance tracking for ML recommendations.
 * 
 * Tracks recommendation effectiveness, user engagement rates,
 * and algorithm performance metrics for continuous improvement.
 */
@Singleton
class RecommendationAnalytics @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private val Context.analyticsDataStore: DataStore<Preferences> by preferencesDataStore(
            name = "recommendation_analytics"
        )
    }
    
    private val dataStore = context.analyticsDataStore
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Track recommendation impression (shown to user).
     */
    suspend fun trackRecommendationImpression(
        profileId: String,
        contentId: String,
        recommendationType: RecommendationType,
        position: Int
    ) {
        val key = stringPreferencesKey("impressions_$profileId")
        dataStore.edit { preferences ->
            val currentJson = preferences[key] ?: ""
            val currentData = if (currentJson.isBlank()) {
                RecommendationImpressions()
            } else {
                try {
                    json.decodeFromString<RecommendationImpressions>(currentJson)
                } catch (e: Exception) {
                    RecommendationImpressions()
                }
            }
            
            val impression = RecommendationImpression(
                contentId = contentId,
                recommendationType = recommendationType,
                position = position,
                timestamp = System.currentTimeMillis()
            )
            
            val updatedData = currentData.copy(
                impressions = (currentData.impressions + impression)
                    .sortedByDescending { it.timestamp }
                    .take(500) // Keep last 500 impressions
            )
            
            preferences[key] = json.encodeToString(updatedData)
        }
    }
    
    /**
     * Track recommendation click/interaction.
     */
    suspend fun trackRecommendationClick(
        profileId: String,
        contentId: String,
        recommendationType: RecommendationType,
        position: Int
    ) {
        val key = stringPreferencesKey("clicks_$profileId")
        dataStore.edit { preferences ->
            val currentJson = preferences[key] ?: ""
            val currentData = if (currentJson.isBlank()) {
                RecommendationClicks()
            } else {
                try {
                    json.decodeFromString<RecommendationClicks>(currentJson)
                } catch (e: Exception) {
                    RecommendationClicks()
                }
            }
            
            val click = RecommendationClick(
                contentId = contentId,
                recommendationType = recommendationType,
                position = position,
                timestamp = System.currentTimeMillis()
            )
            
            val updatedData = currentData.copy(
                clicks = (currentData.clicks + click)
                    .sortedByDescending { it.timestamp }
                    .take(500) // Keep last 500 clicks
            )
            
            preferences[key] = json.encodeToString(updatedData)
        }
    }
    
    /**
     * Track recommendation conversion (user watched content).
     */
    suspend fun trackRecommendationConversion(
        profileId: String,
        contentId: String,
        recommendationType: RecommendationType,
        watchDurationMs: Long
    ) {
        val key = stringPreferencesKey("conversions_$profileId")
        dataStore.edit { preferences ->
            val currentJson = preferences[key] ?: ""
            val currentData = if (currentJson.isBlank()) {
                RecommendationConversions()
            } else {
                try {
                    json.decodeFromString<RecommendationConversions>(currentJson)
                } catch (e: Exception) {
                    RecommendationConversions()
                }
            }
            
            val conversion = RecommendationConversion(
                contentId = contentId,
                recommendationType = recommendationType,
                watchDurationMs = watchDurationMs,
                timestamp = System.currentTimeMillis()
            )
            
            val updatedData = currentData.copy(
                conversions = (currentData.conversions + conversion)
                    .sortedByDescending { it.timestamp }
                    .take(200) // Keep last 200 conversions
            )
            
            preferences[key] = json.encodeToString(updatedData)
        }
    }
    
    /**
     * Get recommendation performance metrics.
     */
    suspend fun getPerformanceMetrics(profileId: String): RecommendationMetrics {
        val impressionsKey = stringPreferencesKey("impressions_$profileId")
        val clicksKey = stringPreferencesKey("clicks_$profileId")
        val conversionsKey = stringPreferencesKey("conversions_$profileId")
        
        val preferences = dataStore.data.first()
        
        val impressions = try {
            val json = preferences[impressionsKey] ?: ""
            if (json.isBlank()) emptyList() else 
                this@RecommendationAnalytics.json.decodeFromString<RecommendationImpressions>(json).impressions
        } catch (e: Exception) {
            emptyList()
        }
        
        val clicks = try {
            val json = preferences[clicksKey] ?: ""
            if (json.isBlank()) emptyList() else 
                this@RecommendationAnalytics.json.decodeFromString<RecommendationClicks>(json).clicks
        } catch (e: Exception) {
            emptyList()
        }
        
        val conversions = try {
            val json = preferences[conversionsKey] ?: ""
            if (json.isBlank()) emptyList() else 
                this@RecommendationAnalytics.json.decodeFromString<RecommendationConversions>(json).conversions
        } catch (e: Exception) {
            emptyList()
        }
        
        // Calculate metrics
        val totalImpressions = impressions.size
        val totalClicks = clicks.size
        val totalConversions = conversions.size
        
        val clickThroughRate = if (totalImpressions > 0) {
            totalClicks.toFloat() / totalImpressions
        } else 0f
        
        val conversionRate = if (totalClicks > 0) {
            totalConversions.toFloat() / totalClicks
        } else 0f
        
        val avgWatchDuration = if (conversions.isNotEmpty()) {
            conversions.map { it.watchDurationMs }.average()
        } else 0.0
        
        // Performance by recommendation type
        val performanceByType = RecommendationType.values().associateWith { type ->
            val typeImpressions = impressions.count { it.recommendationType == type }
            val typeClicks = clicks.count { it.recommendationType == type }
            val typeConversions = conversions.count { it.recommendationType == type }
            
            RecommendationTypeMetrics(
                impressions = typeImpressions,
                clicks = typeClicks,
                conversions = typeConversions,
                clickThroughRate = if (typeImpressions > 0) typeClicks.toFloat() / typeImpressions else 0f,
                conversionRate = if (typeClicks > 0) typeConversions.toFloat() / typeClicks else 0f
            )
        }
        
        return RecommendationMetrics(
            totalImpressions = totalImpressions,
            totalClicks = totalClicks,
            totalConversions = totalConversions,
            clickThroughRate = clickThroughRate,
            conversionRate = conversionRate,
            averageWatchDurationMs = avgWatchDuration,
            performanceByType = performanceByType,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    /**
     * Get trending recommendation types for optimization.
     */
    fun getTopPerformingRecommendationTypes(profileId: String): Flow<List<RecommendationType>> {
        return dataStore.data.map { preferences ->
            val clicksKey = stringPreferencesKey("clicks_$profileId")
            val clicksJson = preferences[clicksKey] ?: ""
            
            if (clicksJson.isBlank()) {
                RecommendationType.values().toList()
            } else {
                try {
                    val clicksData = this@RecommendationAnalytics.json.decodeFromString<RecommendationClicks>(clicksJson)
                    clicksData.clicks
                        .groupBy { it.recommendationType }
                        .mapValues { it.value.size }
                        .toList()
                        .sortedByDescending { it.second }
                        .map { it.first }
                } catch (e: Exception) {
                    RecommendationType.values().toList()
                }
            }
        }
    }
    
    /**
     * Clear analytics data for profile.
     */
    suspend fun clearAnalyticsData(profileId: String) {
        val impressionsKey = stringPreferencesKey("impressions_$profileId")
        val clicksKey = stringPreferencesKey("clicks_$profileId")
        val conversionsKey = stringPreferencesKey("conversions_$profileId")
        
        dataStore.edit { preferences ->
            preferences.remove(impressionsKey)
            preferences.remove(clicksKey)
            preferences.remove(conversionsKey)
        }
    }
}

/**
 * Types of ML recommendations for analytics tracking.
 */
enum class RecommendationType {
    PERSONALIZED,           // Recommended for You
    COLLABORATIVE,          // Based on similar users
    CONTENT_BASED,          // Similar content
    TRENDING_PERSONALIZED,  // Trending with personal boost
    GENRE_BASED,           // More like favorite genre
    CONTEXTUAL,            // Time/context based
    BECAUSE_YOU_WATCHED    // Because you watched X
}

/**
 * Serializable data classes for analytics storage.
 */
@Serializable
data class RecommendationImpressions(
    val impressions: List<RecommendationImpression> = emptyList()
)

@Serializable
data class RecommendationImpression(
    val contentId: String,
    val recommendationType: RecommendationType,
    val position: Int,
    val timestamp: Long
)

@Serializable
data class RecommendationClicks(
    val clicks: List<RecommendationClick> = emptyList()
)

@Serializable
data class RecommendationClick(
    val contentId: String,
    val recommendationType: RecommendationType,
    val position: Int,
    val timestamp: Long
)

@Serializable
data class RecommendationConversions(
    val conversions: List<RecommendationConversion> = emptyList()
)

@Serializable
data class RecommendationConversion(
    val contentId: String,
    val recommendationType: RecommendationType,
    val watchDurationMs: Long,
    val timestamp: Long
)

/**
 * Recommendation performance metrics.
 */
data class RecommendationMetrics(
    val totalImpressions: Int,
    val totalClicks: Int,
    val totalConversions: Int,
    val clickThroughRate: Float,
    val conversionRate: Float,
    val averageWatchDurationMs: Double,
    val performanceByType: Map<RecommendationType, RecommendationTypeMetrics>,
    val lastUpdated: Long
)

data class RecommendationTypeMetrics(
    val impressions: Int,
    val clicks: Int,
    val conversions: Int,
    val clickThroughRate: Float,
    val conversionRate: Float
)

