package com.offordflix.data.repository

import com.offordflix.data.ml.RecommendationEngine
import com.offordflix.data.ml.UserBehaviorTracker
import com.offordflix.data.ml.InteractionType
import com.offordflix.domain.model.VideoContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for ML-powered content recommendations.
 * 
 * Integrates recommendation engine with content discovery to provide
 * personalized viewing suggestions across the app.
 */
@Singleton
class RecommendationRepository @Inject constructor(
    private val recommendationEngine: RecommendationEngine,
    private val userBehaviorTracker: UserBehaviorTracker,
    private val contentDiscoveryRepository: ContentDiscoveryRepository
) {
    
    /**
     * Get personalized recommendations for profile.
     */
    fun getRecommendedForYou(
        profileId: String,
        limit: Int = 20
    ): Flow<List<VideoContent>> = flow {
        try {
            // Get available content from multiple sources
            val popularMovies = contentDiscoveryRepository.getPopularMovies().take(1).first()
            val popularTvShows = contentDiscoveryRepository.getPopularTvShows().take(1).first()
            val topRatedMovies = contentDiscoveryRepository.getTopRatedMovies().take(1).first()
            val topRatedTvShows = contentDiscoveryRepository.getTopRatedTvShows().take(1).first()
            
            val availableContent = (popularMovies + popularTvShows + topRatedMovies + topRatedTvShows)
                .distinctBy { it.id }
            
            val recommendations = recommendationEngine.getPersonalizedRecommendations(
                profileId = profileId,
                availableContent = availableContent,
                limit = limit
            )
            
            emit(recommendations)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get "Because You Watched X" recommendations.
     */
    fun getBecauseYouWatched(
        profileId: String,
        baseContent: VideoContent,
        limit: Int = 10
    ): Flow<List<VideoContent>> = flow {
        try {
            // Get content from same genre and similar properties
            val genreContent = if (baseContent.genres.isNotEmpty()) {
                contentDiscoveryRepository.getContentByGenre(
                    genreId = getGenreId(baseContent.genres.first()),
                    profile = null // Get unfiltered content for similarity matching
                ).take(1).first()
            } else {
                emptyList()
            }
            
            val recommendations = recommendationEngine.getSimilarContentRecommendations(
                profileId = profileId,
                baseContent = baseContent,
                availableContent = genreContent,
                limit = limit
            )
            
            emit(recommendations)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get personalized trending content.
     */
    fun getPersonalizedTrending(
        profileId: String,
        limit: Int = 15
    ): Flow<List<VideoContent>> = flow {
        try {
            val trendingContent = contentDiscoveryRepository.getTrendingContent().take(1).first()
            
            val personalizedTrending = recommendationEngine.getPersonalizedTrending(
                profileId = profileId,
                trendingContent = trendingContent,
                limit = limit
            )
            
            emit(personalizedTrending)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get recommendations based on favorite genre.
     */
    fun getMoreLikeGenre(
        profileId: String,
        genre: String,
        limit: Int = 15
    ): Flow<List<VideoContent>> = flow {
        try {
            val genreContent = contentDiscoveryRepository.getContentByGenre(
                genreId = getGenreId(genre),
                profile = null
            ).take(1).first()
            
            val recommendations = recommendationEngine.getGenreRecommendations(
                profileId = profileId,
                availableContent = genreContent,
                targetGenre = genre,
                limit = limit
            )
            
            emit(recommendations)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Track user interaction for ML learning.
     */
    suspend fun trackUserInteraction(
        profileId: String,
        content: VideoContent,
        interactionType: InteractionType,
        durationMs: Long = 0L
    ) {
        userBehaviorTracker.trackContentView(
            profileId = profileId,
            content = content,
            durationMs = durationMs,
            interactionType = interactionType
        )
    }
    
    /**
     * Track search behavior.
     */
    suspend fun trackSearch(profileId: String, query: String, resultsCount: Int) {
        userBehaviorTracker.trackSearch(profileId, query, resultsCount)
    }
    
    /**
     * Get user's favorite genres for UI customization.
     */
    fun getFavoriteGenres(profileId: String): Flow<List<String>> = flow {
        val viewingPatterns = userBehaviorTracker.getViewingPatterns(profileId)
        emit(viewingPatterns.favoriteGenres.take(3))
    }
    
    /**
     * Get user viewing statistics.
     */
    suspend fun getViewingStats(profileId: String): ViewingStats {
        val patterns = userBehaviorTracker.getViewingPatterns(profileId)
        val genrePrefs = userBehaviorTracker.getGenrePreferences(profileId).take(1).first()
        val contentTypePrefs = userBehaviorTracker.getContentTypePreferences(profileId).take(1).first()
        
        return ViewingStats(
            totalWatchTimeHours = patterns.totalWatchTimeMs / (1000 * 60 * 60),
            favoriteGenres = patterns.favoriteGenres,
            preferredContentType = contentTypePrefs.maxByOrNull { it.value }?.key?.name ?: "Movies",
            averageRatingPreference = patterns.averageRatingPreference,
            profileMaturity = if (patterns.averageRatingPreference > 7.0) "High Quality" else "Mainstream"
        )
    }
    
    /**
     * Generate smart recommendations based on time of day and viewing context.
     */
    fun getContextualRecommendations(
        profileId: String,
        context: ViewingContext,
        limit: Int = 15
    ): Flow<List<VideoContent>> = flow {
        try {
            val baseRecommendations = getRecommendedForYou(profileId, limit * 2).take(1).first()
            
            val contextualFilter: (VideoContent) -> Boolean = when (context) {
                ViewingContext.MORNING -> { content ->
                    // Light content for morning
                    content.genres.any { it in listOf("Comedy", "Animation", "Family") }
                }
                ViewingContext.AFTERNOON -> { content ->
                    // Diverse content for afternoon
                    true
                }
                ViewingContext.EVENING -> { content ->
                    // Popular and engaging content for prime time
                    content.voteAverage > 6.5
                }
                ViewingContext.LATE_NIGHT -> { content ->
                    // Intense content for late night
                    content.genres.any { it in listOf("Thriller", "Horror", "Mystery", "Crime") }
                }
                ViewingContext.WEEKEND -> { content ->
                    // Longer content suitable for weekends
                    content.runtime == null || content.runtime!! > 90
                }
            }
            
            val contextualRecommendations = baseRecommendations
                .filter(contextualFilter)
                .take(limit)
            
            // Fill remaining slots with general recommendations if needed
            val finalRecommendations = if (contextualRecommendations.size < limit) {
                contextualRecommendations + baseRecommendations
                    .filterNot { it in contextualRecommendations }
                    .take(limit - contextualRecommendations.size)
            } else {
                contextualRecommendations
            }
            
            emit(finalRecommendations)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get movie-specific recommendations for profile.
     */
    fun getRecommendedMoviesForYou(
        profileId: String,
        limit: Int = 20
    ): Flow<List<VideoContent>> = flow {
        try {
            // Get available movie content from multiple sources
            val popularMovies = contentDiscoveryRepository.getPopularMovies().take(1).first()
            val topRatedMovies = contentDiscoveryRepository.getTopRatedMovies().take(1).first()
            val trendingMovies = contentDiscoveryRepository.getTrendingMovies().take(1).first()
            
            val availableMovies = (popularMovies + topRatedMovies + trendingMovies)
                .distinctBy { it.id }
                .filter { it.type == com.offordflix.domain.model.ContentType.MOVIE }
            
            val recommendations = recommendationEngine.getPersonalizedRecommendations(
                profileId = profileId,
                availableContent = availableMovies,
                limit = limit
            )
            
            emit(recommendations)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get personalized trending movies.
     */
    fun getPersonalizedTrendingMovies(
        profileId: String,
        limit: Int = 15
    ): Flow<List<VideoContent>> = flow {
        try {
            val trendingMovies = contentDiscoveryRepository.getTrendingMovies().take(1).first()
            
            val personalizedTrending = recommendationEngine.getPersonalizedTrending(
                profileId = profileId,
                trendingContent = trendingMovies,
                limit = limit
            )
            
            emit(personalizedTrending)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get TV show-specific recommendations for profile.
     */
    fun getRecommendedTvShowsForYou(
        profileId: String,
        limit: Int = 20
    ): Flow<List<VideoContent>> = flow {
        try {
            // Get available TV show content from multiple sources
            val popularTvShows = contentDiscoveryRepository.getPopularTvShows().take(1).first()
            val topRatedTvShows = contentDiscoveryRepository.getTopRatedTvShows().take(1).first()
            val trendingTvShows = contentDiscoveryRepository.getTrendingTvShows().take(1).first()
            
            val availableTvShows = (popularTvShows + topRatedTvShows + trendingTvShows)
                .distinctBy { it.id }
                .filter { it.type == com.offordflix.domain.model.ContentType.TV_SHOW }
            
            val recommendations = recommendationEngine.getPersonalizedRecommendations(
                profileId = profileId,
                availableContent = availableTvShows,
                limit = limit
            )
            
            emit(recommendations)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get personalized trending TV shows.
     */
    fun getPersonalizedTrendingTvShows(
        profileId: String,
        limit: Int = 15
    ): Flow<List<VideoContent>> = flow {
        try {
            val trendingTvShows = contentDiscoveryRepository.getTrendingTvShows().take(1).first()
            
            val personalizedTrending = recommendationEngine.getPersonalizedTrending(
                profileId = profileId,
                trendingContent = trendingTvShows,
                limit = limit
            )
            
            emit(personalizedTrending)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Helper function to get genre ID (simplified mapping).
     */
    private fun getGenreId(genreName: String): Int {
        return when (genreName.lowercase()) {
            "action" -> 28
            "adventure" -> 12
            "animation" -> 16
            "comedy" -> 35
            "crime" -> 80
            "documentary" -> 99
            "drama" -> 18
            "family" -> 10751
            "fantasy" -> 14
            "history" -> 36
            "horror" -> 27
            "music" -> 10402
            "mystery" -> 9648
            "romance" -> 10749
            "science fiction" -> 878
            "thriller" -> 53
            "war" -> 10752
            "western" -> 37
            else -> 28 // Default to Action
        }
    }
}

/**
 * User viewing statistics for insights.
 */
data class ViewingStats(
    val totalWatchTimeHours: Long,
    val favoriteGenres: List<String>,
    val preferredContentType: String,
    val averageRatingPreference: Double,
    val profileMaturity: String
)

/**
 * Viewing context for contextual recommendations.
 */
enum class ViewingContext {
    MORNING,
    AFTERNOON, 
    EVENING,
    LATE_NIGHT,
    WEEKEND
}

