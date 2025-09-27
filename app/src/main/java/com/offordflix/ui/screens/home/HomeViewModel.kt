package com.offordflix.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offordflix.data.repository.ContentDiscoveryRepository
import com.offordflix.data.repository.WatchlistRepository
import com.offordflix.data.repository.RecommendationRepository
import com.offordflix.data.ml.InteractionType
import com.offordflix.data.repository.ViewingContext
import com.offordflix.domain.model.Profile
import com.offordflix.domain.model.VideoContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for home screen content discovery.
 * 
 * Manages content loading, watchlist operations, and user interactions
 * for the Netflix-style home screen experience.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentDiscoveryRepository: ContentDiscoveryRepository,
    private val watchlistRepository: WatchlistRepository,
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {

    // Internal state
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    // Current active profile
    private var currentProfile: Profile? = null
    
    /**
     * Initialize with profile and load content.
     */
    fun initializeWithProfile(profile: Profile) {
        currentProfile = profile
        loadContent()
    }
    
    /**
     * Load all content for the home screen.
     */
    fun loadContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Load trending content first for hero banner
                launch { loadTrendingContent() }
                
                // Load other content in parallel
                launch { loadPopularMovies() }
                launch { loadPopularTvShows() }
                launch { loadTopRatedMovies() }
                launch { loadTopRatedTvShows() }
                launch { loadWatchlist() }
                launch { loadContinueWatching() }
                
                // Only load ML features if profile is set
                if (currentProfile != null) {
                    launch { loadRecommendations() }
                    launch { loadPersonalizedTrending() }
                    launch { loadFavoriteGenreRecommendations() }
                }
                
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to load content"
                    )
                }
            }
        }
    }
    
    /**
     * Set the current active profile.
     */
    fun setProfile(profile: Profile) {
        currentProfile = profile
        loadContent() // Reload content with profile filtering
    }
    
    /**
     * Load trending content and set featured content.
     */
    private suspend fun loadTrendingContent() {
        contentDiscoveryRepository.getTrendingContent(currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { 
                    it.copy(
                        trendingContent = content,
                        featuredContent = content.firstOrNull() // Use first trending as featured
                    ) 
                }
            }
    }
    
    /**
     * Load popular movies.
     */
    private suspend fun loadPopularMovies() {
        contentDiscoveryRepository.getPopularMovies(currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { it.copy(popularMovies = content) }
            }
    }
    
    /**
     * Load popular TV shows.
     */
    private suspend fun loadPopularTvShows() {
        contentDiscoveryRepository.getPopularTvShows(currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { it.copy(popularTvShows = content) }
            }
    }
    
    /**
     * Load top-rated movies.
     */
    private suspend fun loadTopRatedMovies() {
        contentDiscoveryRepository.getTopRatedMovies(currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { it.copy(topRatedMovies = content) }
            }
    }
    
    /**
     * Load top-rated TV shows.
     */
    private suspend fun loadTopRatedTvShows() {
        contentDiscoveryRepository.getTopRatedTvShows(currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { it.copy(topRatedTvShows = content) }
            }
    }
    
    /**
     * Load user's watchlist.
     */
    private suspend fun loadWatchlist() {
        currentProfile?.let { profile ->
            watchlistRepository.getWatchlist(profile.id)
                .catch { e -> 
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { content ->
                    _uiState.update { it.copy(watchlist = content) }
                }
        }
    }
    
    /**
     * Load continue watching items.
     */
    private suspend fun loadContinueWatching() {
        currentProfile?.let { profile ->
            watchlistRepository.getContinueWatching(profile.id)
                .catch { e -> 
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { content ->
                    _uiState.update { it.copy(continueWatching = content) }
                }
        }
    }
    
    /**
     * Add content to watchlist with ML tracking.
     */
    fun addToWatchlist(content: VideoContent) {
        viewModelScope.launch {
            try {
                currentProfile?.let { profile ->
                    watchlistRepository.addToWatchlist(profile.id, content)
                    _uiState.update { state ->
                        state.copy(watchlist = state.watchlist + content)
                    }
                    // Track for ML learning
                    trackContentInteraction(content, InteractionType.LIKE)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    /**
     * Remove content from watchlist with ML tracking.
     */
    fun removeFromWatchlist(content: VideoContent) {
        viewModelScope.launch {
            try {
                currentProfile?.let { profile ->
                    watchlistRepository.removeFromWatchlist(profile.id, content.id)
                    _uiState.update { state ->
                        state.copy(watchlist = state.watchlist.filterNot { it.id == content.id })
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    /**
     * Search for content.
     */
    fun searchContent(query: String) {
        viewModelScope.launch {
            contentDiscoveryRepository.searchContent(query, currentProfile)
                .catch { e -> 
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { content ->
                    _uiState.update { it.copy(searchResults = content) }
                }
        }
    }
    
    /**
     * Clear search results.
     */
    fun clearSearch() {
        _uiState.update { it.copy(searchResults = emptyList()) }
    }
    
    /**
     * Load ML-powered recommendations.
     */
    private suspend fun loadRecommendations() {
        currentProfile?.let { profile ->
            try {
                recommendationRepository.getRecommendedForYou(profile.id)
                    .catch { e ->
                        // ML features are optional - don't fail the whole screen
                        println("ML Recommendations failed: ${e.message}")
                    }
                    .collect { recommendations ->
                        _uiState.update { it.copy(recommendedForYou = recommendations) }
                    }
            } catch (e: Exception) {
                // Silently fail ML features to prevent white screen
                println("ML Recommendations error: ${e.message}")
            }
        }
    }
    
    /**
     * Load personalized trending content.
     */
    private suspend fun loadPersonalizedTrending() {
        currentProfile?.let { profile ->
            try {
                recommendationRepository.getPersonalizedTrending(profile.id)
                    .catch { e ->
                        // ML features are optional - don't fail the whole screen
                        println("Personalized Trending failed: ${e.message}")
                    }
                    .collect { trending ->
                        _uiState.update { it.copy(personalizedTrending = trending) }
                    }
            } catch (e: Exception) {
                // Silently fail ML features to prevent white screen
                println("Personalized Trending error: ${e.message}")
            }
        }
    }
    
    /**
     * Load recommendations based on favorite genres.
     */
    private suspend fun loadFavoriteGenreRecommendations() {
        currentProfile?.let { profile ->
            try {
                recommendationRepository.getFavoriteGenres(profile.id)
                    .catch { e ->
                        // ML features are optional - don't fail the whole screen
                        println("Favorite Genres failed: ${e.message}")
                    }
                    .collect { favoriteGenres ->
                        if (favoriteGenres.isNotEmpty()) {
                            val topGenre = favoriteGenres.first()
                            recommendationRepository.getMoreLikeGenre(profile.id, topGenre)
                                .catch { e ->
                                    println("Genre Recommendations failed: ${e.message}")
                                }
                                .collect { genreRecommendations ->
                                    _uiState.update { 
                                        it.copy(
                                            favoriteGenreRecommendations = genreRecommendations,
                                            favoriteGenre = topGenre
                                        ) 
                                    }
                                }
                        }
                    }
            } catch (e: Exception) {
                // Silently fail ML features to prevent white screen
                println("Favorite Genre Recommendations error: ${e.message}")
            }
        }
    }
    
    /**
     * Get "Because You Watched" recommendations for a specific content.
     */
    fun getBecauseYouWatched(content: VideoContent) {
        viewModelScope.launch {
            currentProfile?.let { profile ->
                recommendationRepository.getBecauseYouWatched(profile.id, content)
                    .catch { e ->
                        _uiState.update { it.copy(error = e.message) }
                    }
                    .collect { recommendations ->
                        _uiState.update { 
                            it.copy(
                                becauseYouWatchedContent = content,
                                becauseYouWatchedRecommendations = recommendations
                            ) 
                        }
                    }
            }
        }
    }
    
    /**
     * Get contextual recommendations based on time of day.
     */
    fun loadContextualRecommendations() {
        viewModelScope.launch {
            currentProfile?.let { profile ->
                val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val context = when (currentHour) {
                    in 6..11 -> ViewingContext.MORNING
                    in 12..17 -> ViewingContext.AFTERNOON
                    in 18..22 -> ViewingContext.EVENING
                    else -> ViewingContext.LATE_NIGHT
                }
                
                recommendationRepository.getContextualRecommendations(profile.id, context)
                    .catch { e ->
                        _uiState.update { it.copy(error = e.message) }
                    }
                    .collect { contextualRecs ->
                        _uiState.update { 
                            it.copy(
                                contextualRecommendations = contextualRecs,
                                viewingContext = context
                            ) 
                        }
                    }
            }
        }
    }
    
    /**
     * Track user interaction for ML learning.
     */
    fun trackContentInteraction(content: VideoContent, interactionType: InteractionType) {
        viewModelScope.launch {
            currentProfile?.let { profile ->
                recommendationRepository.trackUserInteraction(
                    profileId = profile.id,
                    content = content,
                    interactionType = interactionType
                )
            }
        }
    }
    
    /**
     * Enhanced content selection with ML tracking.
     */
    fun onContentSelected(content: VideoContent) {
        trackContentInteraction(content, InteractionType.VIEW)
        getBecauseYouWatched(content)
    }
    
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for home screen.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val featuredContent: VideoContent? = null,
    val trendingContent: List<VideoContent> = emptyList(),
    val popularMovies: List<VideoContent> = emptyList(),
    val popularTvShows: List<VideoContent> = emptyList(),
    val topRatedMovies: List<VideoContent> = emptyList(),
    val topRatedTvShows: List<VideoContent> = emptyList(),
    val watchlist: List<VideoContent> = emptyList(),
    val continueWatching: List<VideoContent> = emptyList(),
    val searchResults: List<VideoContent> = emptyList(),
    
    // ML-powered recommendations
    val recommendedForYou: List<VideoContent> = emptyList(),
    val personalizedTrending: List<VideoContent> = emptyList(),
    val favoriteGenreRecommendations: List<VideoContent> = emptyList(),
    val favoriteGenre: String? = null,
    val becauseYouWatchedContent: VideoContent? = null,
    val becauseYouWatchedRecommendations: List<VideoContent> = emptyList(),
    val contextualRecommendations: List<VideoContent> = emptyList(),
    val viewingContext: ViewingContext? = null,
    
    val error: String? = null
)
