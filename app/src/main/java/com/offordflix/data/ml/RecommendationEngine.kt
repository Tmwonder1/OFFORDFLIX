package com.offordflix.data.ml

import com.offordflix.domain.model.VideoContent
import com.offordflix.domain.model.ContentType
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * ML-powered recommendation engine.
 * 
 * Implements collaborative filtering, content-based filtering,
 * and hybrid approaches to generate personalized recommendations.
 */
@Singleton
class RecommendationEngine @Inject constructor(
    private val userBehaviorTracker: UserBehaviorTracker
) {
    
    /**
     * Generate personalized recommendations for a profile.
     */
    suspend fun getPersonalizedRecommendations(
        profileId: String,
        availableContent: List<VideoContent>,
        limit: Int = 20
    ): List<VideoContent> {
        val behavior = userBehaviorTracker.getUserBehavior(profileId).first()
        val genrePrefs = userBehaviorTracker.getGenrePreferences(profileId).first()
        val contentTypePrefs = userBehaviorTracker.getContentTypePreferences(profileId).first()
        val viewingPatterns = userBehaviorTracker.getViewingPatterns(profileId)
        
        // If no behavior data, return popular content
        if (behavior == null || behavior.interactions.isEmpty()) {
            return availableContent
                .sortedByDescending { it.voteAverage * it.popularity }
                .take(limit)
        }
        
        // Score each content item
        val scoredContent = availableContent.map { content ->
            val score = calculateContentScore(
                content = content,
                genrePreferences = genrePrefs,
                contentTypePreferences = contentTypePrefs,
                viewingPatterns = viewingPatterns,
                userInteractions = behavior.interactions
            )
            ScoredContent(content, score)
        }
        
        // Return top recommendations
        return scoredContent
            .sortedByDescending { it.score }
            .take(limit)
            .map { it.content }
    }
    
    /**
     * Get "Because You Watched X" recommendations.
     */
    suspend fun getSimilarContentRecommendations(
        profileId: String,
        baseContent: VideoContent,
        availableContent: List<VideoContent>,
        limit: Int = 10
    ): List<VideoContent> {
        return availableContent
            .filter { it.id != baseContent.id }
            .map { content ->
                val similarity = calculateContentSimilarity(baseContent, content)
                ScoredContent(content, similarity)
            }
            .sortedByDescending { it.score }
            .take(limit)
            .map { it.content }
    }
    
    /**
     * Get trending content with personalization boost.
     */
    suspend fun getPersonalizedTrending(
        profileId: String,
        trendingContent: List<VideoContent>,
        limit: Int = 15
    ): List<VideoContent> {
        val genrePrefs = userBehaviorTracker.getGenrePreferences(profileId).first()
        val contentTypePrefs = userBehaviorTracker.getContentTypePreferences(profileId).first()
        
        return trendingContent.map { content ->
            val personalizedScore = content.popularity + 
                calculateGenreBoost(content.genres, genrePrefs) * 0.3 +
                calculateContentTypeBoost(content.type, contentTypePrefs) * 0.2
            ScoredContent(content, personalizedScore)
        }
        .sortedByDescending { it.score }
        .take(limit)
        .map { it.content }
    }
    
    /**
     * Get genre-based recommendations.
     */
    suspend fun getGenreRecommendations(
        profileId: String,
        availableContent: List<VideoContent>,
        targetGenre: String,
        limit: Int = 15
    ): List<VideoContent> {
        val genrePrefs = userBehaviorTracker.getGenrePreferences(profileId).first()
        
        return availableContent
            .filter { content -> 
                content.genres.any { it.equals(targetGenre, ignoreCase = true) }
            }
            .map { content ->
                val score = content.voteAverage * 
                    (1 + calculateGenreBoost(content.genres, genrePrefs) * 0.5)
                ScoredContent(content, score)
            }
            .sortedByDescending { it.score }
            .take(limit)
            .map { it.content }
    }
    
    /**
     * Calculate comprehensive content score for recommendations.
     */
    private fun calculateContentScore(
        content: VideoContent,
        genrePreferences: Map<String, Float>,
        contentTypePreferences: Map<ContentType, Float>,
        viewingPatterns: ViewingPatterns,
        userInteractions: List<UserInteraction>
    ): Double {
        var score = 0.0
        
        // Base score from content quality
        score += content.voteAverage * 0.3
        score += (content.popularity / 1000.0).coerceAtMost(1.0) * 0.2
        
        // Genre preference boost
        score += calculateGenreBoost(content.genres, genrePreferences) * 0.3
        
        // Content type preference
        score += calculateContentTypeBoost(content.type, contentTypePreferences) * 0.15
        
        // Novelty boost (prefer content not seen recently)
        score += calculateNoveltyBoost(content, userInteractions) * 0.05
        
        return score.coerceIn(0.0, 10.0)
    }
    
    /**
     * Calculate content similarity for "Because You Watched" recommendations.
     */
    private fun calculateContentSimilarity(
        content1: VideoContent,
        content2: VideoContent
    ): Double {
        var similarity = 0.0
        
        // Genre similarity (Jaccard coefficient)
        val genres1 = content1.genres.toSet()
        val genres2 = content2.genres.toSet()
        val intersection = genres1.intersect(genres2).size
        val union = genres1.union(genres2).size
        
        if (union > 0) {
            similarity += (intersection.toDouble() / union) * 0.4
        }
        
        // Content type similarity
        if (content1.type == content2.type) {
            similarity += 0.2
        }
        
        // Rating similarity
        val ratingDiff = abs(content1.voteAverage - content2.voteAverage)
        val ratingSimilarity = (10.0 - ratingDiff) / 10.0
        similarity += ratingSimilarity * 0.2
        
        // Release date proximity (within 5 years)
        val date1 = content1.releaseDate
        val date2 = content2.releaseDate
        if (date1 != null && date2 != null) {
            try {
                val year1 = date1.substring(0, 4).toInt()
                val year2 = date2.substring(0, 4).toInt()
                val yearDiff = abs(year1 - year2)
                if (yearDiff <= 5) {
                    similarity += (5.0 - yearDiff) / 5.0 * 0.2
                }
            } catch (e: Exception) {
                // Ignore date parsing errors
            }
        }
        
        return similarity.coerceIn(0.0, 1.0)
    }
    
    /**
     * Calculate genre preference boost.
     */
    private fun calculateGenreBoost(
        contentGenres: List<String>,
        genrePreferences: Map<String, Float>
    ): Double {
        if (contentGenres.isEmpty() || genrePreferences.isEmpty()) {
            return 0.0
        }
        
        val genreScores = contentGenres.mapNotNull { genre ->
            genrePreferences[genre]?.toDouble()
        }
        
        return if (genreScores.isNotEmpty()) {
            genreScores.average()
        } else {
            0.0
        }
    }
    
    /**
     * Calculate content type preference boost.
     */
    private fun calculateContentTypeBoost(
        contentType: ContentType,
        contentTypePreferences: Map<ContentType, Float>
    ): Double {
        return contentTypePreferences[contentType]?.toDouble() ?: 0.5
    }
    
    /**
     * Calculate novelty boost to promote content discovery.
     */
    private fun calculateNoveltyBoost(
        content: VideoContent,
        userInteractions: List<UserInteraction>
    ): Double {
        // Check if user has interacted with this content recently
        val recentInteraction = userInteractions
            .filter { it.contentId == content.id }
            .maxByOrNull { it.timestamp }
        
        if (recentInteraction == null) {
            return 1.0 // New content gets full novelty boost
        }
        
        // Reduce boost based on how recent the interaction was
        val daysSinceInteraction = (System.currentTimeMillis() - recentInteraction.timestamp) / (24 * 60 * 60 * 1000)
        
        return when {
            daysSinceInteraction < 1 -> 0.0  // Watched today
            daysSinceInteraction < 7 -> 0.2  // Watched this week
            daysSinceInteraction < 30 -> 0.5 // Watched this month
            else -> 1.0 // Old enough to recommend again
        }
    }
    
    /**
     * Generate collaborative filtering recommendations.
     * (Simplified version - in production would use user similarity matrix)
     */
    suspend fun getCollaborativeRecommendations(
        profileId: String,
        allUserProfiles: List<String>,
        getUserBehaviorForProfile: suspend (String) -> UserBehaviorData?,
        availableContent: List<VideoContent>,
        limit: Int = 10
    ): List<VideoContent> {
        val currentUserBehavior = userBehaviorTracker.getUserBehavior(profileId).first()
            ?: return emptyList()
        
        // Find similar users based on genre preferences
        val currentGenrePrefs = userBehaviorTracker.getGenrePreferences(profileId).first()
        val similarUsers = mutableListOf<Pair<String, Double>>()
        
        for (otherProfileId in allUserProfiles) {
            if (otherProfileId == profileId) continue
            
            val otherGenrePrefs = userBehaviorTracker.getGenrePreferences(otherProfileId).first()
            val similarity = calculateUserSimilarity(currentGenrePrefs, otherGenrePrefs)
            
            if (similarity > 0.3) { // Threshold for similarity
                similarUsers.add(otherProfileId to similarity)
            }
        }
        
        // Get content liked by similar users but not seen by current user
        val currentUserContentIds = currentUserBehavior.interactions.map { it.contentId }.toSet()
        val recommendations = mutableMapOf<String, Double>()
        
        for ((similarUserId, similarity) in similarUsers.take(5)) { // Top 5 similar users
            val similarUserBehavior = getUserBehaviorForProfile(similarUserId) ?: continue
            
            val likedContent = similarUserBehavior.interactions
                .filter { it.interactionType == InteractionType.LIKE || it.interactionType == InteractionType.WATCH }
                .filter { it.contentId !in currentUserContentIds }
            
            for (interaction in likedContent) {
                val currentScore = recommendations[interaction.contentId] ?: 0.0
                recommendations[interaction.contentId] = currentScore + similarity
            }
        }
        
        // Convert to content list
        return recommendations.entries
            .sortedByDescending { it.value }
            .take(limit)
            .mapNotNull { entry ->
                availableContent.find { it.id == entry.key }
            }
    }
    
    /**
     * Calculate user similarity based on genre preferences.
     */
    private fun calculateUserSimilarity(
        prefs1: Map<String, Float>,
        prefs2: Map<String, Float>
    ): Double {
        val commonGenres = prefs1.keys.intersect(prefs2.keys)
        if (commonGenres.isEmpty()) return 0.0
        
        var similarity = 0.0
        for (genre in commonGenres) {
            val pref1 = prefs1[genre] ?: 0f
            val pref2 = prefs2[genre] ?: 0f
            similarity += 1.0 - abs(pref1 - pref2) / 2.0 // Normalize to 0-1
        }
        
        return similarity / commonGenres.size
    }
}

/**
 * Content with calculated recommendation score.
 */
private data class ScoredContent(
    val content: VideoContent,
    val score: Double
)

