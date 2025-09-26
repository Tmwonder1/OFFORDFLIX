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
     * Load all content for the home screen.
     */
    fun loadContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Load content in parallel
                launch { loadTrendingContent() }
                launch { loadPopularMovies() }
                launch { loadPopularTvShows() }
                launch { loadTopRatedMovies() }
                launch { loadTopRatedTvShows() }
                launch { loadWatchlist() }
                launch { loadContinueWatching() }
                launch { loadRecommendations() }
                launch { loadPersonalizedTrending() }
                launch { loadFavoriteGenreRecommendations() }
                
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
     * Add content to watchlist.
     */
    fun addToWatchlist(content: VideoContent) {
        viewModelScope.launch {
            currentProfile?.let { profile ->
                watchlistRepository.addToWatchlist(profile.id, content)
                    .onSuccess {
                        loadWatchlist() // Refresh watchlist
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(error = e.message) }
                    }
            }
        }
    }
    
    /**
     * Remove content from watchlist.
     */
    fun removeFromWatchlist(content: VideoContent) {
        viewModelScope.launch {
            currentProfile?.let { profile ->
                watchlistRepository.removeFromWatchlist(profile.id, content.id)
                    .onSuccess {
                        loadWatchlist() // Refresh watchlist
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(error = e.message) }
                    }
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
            recommendationRepository.getRecommendedForYou(profile.id)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { recommendations ->
                    _uiState.update { it.copy(recommendedForYou = recommendations) }
                }
        }
    }
    
    /**
     * Load personalized trending content.
     */
    private suspend fun loadPersonalizedTrending() {
        currentProfile?.let { profile ->
            recommendationRepository.getPersonalizedTrending(profile.id)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { trending ->
                    _uiState.update { it.copy(personalizedTrending = trending) }
                }
        }
    }
    
    /**
     * Load recommendations based on favorite genres.
     */
    private suspend fun loadFavoriteGenreRecommendations() {
        currentProfile?.let { profile ->
            recommendationRepository.getFavoriteGenres(profile.id)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { favoriteGenres ->
                    if (favoriteGenres.isNotEmpty()) {
                        val topGenre = favoriteGenres.first()
                        recommendationRepository.getMoreLikeGenre(profile.id, topGenre)
                            .catch { e ->
                                _uiState.update { it.copy(error = e.message) }
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
     * Enhanced watchlist operations with ML tracking.
     */
    override fun addToWatchlist(content: VideoContent) {
        super.addToWatchlist(content)
        trackContentInteraction(content, InteractionType.LIKE)
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
