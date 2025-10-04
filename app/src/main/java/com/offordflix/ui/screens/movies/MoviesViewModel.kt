package com.offordflix.ui.screens.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offordflix.data.repository.ContentDiscoveryRepository
import com.offordflix.data.repository.WatchlistRepository
import com.offordflix.data.repository.RecommendationRepository
import com.offordflix.data.ml.InteractionType
import com.offordflix.ui.cache.ContentCacheManager
import com.offordflix.domain.model.Profile
import com.offordflix.domain.model.VideoContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for movies screen content discovery.
 * 
 * Manages movie-specific content loading, watchlist operations, and user interactions
 * for the Netflix-style movies screen experience.
 */
@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val contentDiscoveryRepository: ContentDiscoveryRepository,
    private val watchlistRepository: WatchlistRepository,
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {

    // Internal state
    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()
    
    // Current active profile
    private var currentProfile: Profile? = null
    private var isInitialized = false
    
    // Content caching
    private var contentLoadTime: Long = 0
    private val cacheValidityDuration = 5 * 60 * 1000L // 5 minutes cache
    private var isContentLoading = false
    
    /**
     * Initialize with profile and load content.
     */
    fun initializeWithProfile(profile: Profile) {
        if (!isInitialized || currentProfile?.id != profile.id) {
            android.util.Log.d("MoviesViewModel", "Initializing with profile: ${profile.id}")
            currentProfile = profile
            isInitialized = true
            loadContentIfNeeded()
        } else {
            android.util.Log.d("MoviesViewModel", "Already initialized with profile: ${profile.id}, skipping initialization")
        }
    }
    
    /**
     * Load content only if cache is expired or content is empty.
     */
    private fun loadContentIfNeeded() {
        val currentTime = System.currentTimeMillis()
        val hasContent = uiState.value.enhancedContent != null || 
                        uiState.value.trendingMovies.isNotEmpty()
        val isCacheValid = (currentTime - contentLoadTime) < cacheValidityDuration
        
        if (!hasContent || !isCacheValid) {
            android.util.Log.d("MoviesViewModel", "Loading content - hasContent: $hasContent, cacheValid: $isCacheValid")
            loadContent()
        } else {
            android.util.Log.d("MoviesViewModel", "Using cached content - age: ${(currentTime - contentLoadTime) / 1000}s")
        }
    }
    
    /**
     * Load all movie content for the movies screen.
     */
    fun loadContent() {
        // Prevent concurrent loading
        if (isContentLoading) {
            android.util.Log.d("MoviesViewModel", "Content already loading, skipping duplicate request")
            return
        }
        
        viewModelScope.launch {
            isContentLoading = true
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Try to load enhanced movies content first
                launch { loadEnhancedMoviesContent() }
                
                // Load fallback content in parallel (for when backend is unavailable)
                launch { loadTrendingMovies() }
                launch { loadPopularMovies() }
                launch { loadTopRatedMovies() }
                launch { loadUpcomingMovies() }
                launch { loadNowPlayingMovies() }
                launch { loadActionMovies() }
                launch { loadComedyMovies() }
                launch { loadDramaMovies() }
                launch { loadWatchlistMovies() }
                
                // Only load ML features if profile is set
                if (currentProfile != null) {
                    launch { loadRecommendedMovies() }
                    launch { loadPersonalizedMovieTrending() }
                }
                
                // Update cache timestamp on successful load
                contentLoadTime = System.currentTimeMillis()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to load movie content"
                    )
                }
            } finally {
                isContentLoading = false
            }
        }
    }
    
    /**
     * Load enhanced movies content with all movie rows from backend.
     */
    private suspend fun loadEnhancedMoviesContent() {
        // Check cache first
        val cachedContent = ContentCacheManager.getCachedMoviesContent()
        if (cachedContent != null) {
            android.util.Log.d("MoviesViewModel", "Using cached movies content with ${cachedContent.sections.size} sections")
            _uiState.update { 
                it.copy(
                    enhancedContent = cachedContent,
                    featuredContent = cachedContent.slide.firstOrNull()?.toVideoContent()
                )
            }
            return
        }
        
        contentDiscoveryRepository.getEnhancedMoviesContent(currentProfile)
            .catch { e -> 
                // Don't fail the whole screen if enhanced content fails
                println("Enhanced movies content loading failed: ${e.message}")
            }
            .collect { enhancedContent ->
                if (enhancedContent != null) {
                    android.util.Log.d("MoviesViewModel", "Enhanced movies content loaded successfully with ${enhancedContent.sections.size} sections")

                    // Immediately use the raw content (no logos yet) for instant UI
                    _uiState.update {
                        it.copy(
                            enhancedContent = enhancedContent,
                            featuredContent = enhancedContent.slide.firstOrNull()?.toVideoContent()
                        )
                    }

                    // Enrich logos in background and update/cache when ready
                    viewModelScope.launch {
                        try {
                            val enrichedSlide = contentDiscoveryRepository.enrichContentWithLogos(
                                enhancedContent.slide.map { it.toVideoContent() }
                            )
                            val enrichedSections = enhancedContent.sections.map { section ->
                                val enrichedSectionData = contentDiscoveryRepository.enrichContentWithLogos(
                                    section.data.map { it.toVideoContent() }
                                )
                                val enrichedDtoData = enrichedSectionData.mapIndexed { index, videoContent ->
                                    section.data[index].copy(logoPath = extractLogoPath(videoContent.logoUrl))
                                }
                                section.copy(data = enrichedDtoData)
                            }
                            val enrichedEnhancedContent = enhancedContent.copy(
                                slide = enrichedSlide.mapIndexed { index, videoContent ->
                                    enhancedContent.slide[index].copy(logoPath = extractLogoPath(videoContent.logoUrl))
                                },
                                sections = enrichedSections
                            )
                            ContentCacheManager.setCachedMoviesContent(enrichedEnhancedContent)
                            _uiState.update {
                                it.copy(
                                    enhancedContent = enrichedEnhancedContent,
                                    featuredContent = enrichedSlide.firstOrNull()
                                )
                            }
                        } catch (e: Exception) {
                            // Ignore enrichment failure to keep UI responsive
                        }
                    }
                }
            }
    }
    
    /**
     * Extract logo path from full TMDB logo URL.
     */
    private fun extractLogoPath(logoUrl: String?): String? {
        return logoUrl?.substringAfter("https://image.tmdb.org/t/p/w500")
    }
    
    /**
     * Set the current active profile.
     */
    fun setProfile(profile: Profile) {
        currentProfile = profile
        loadContent() // Reload content with profile filtering
    }
    
    /**
     * Load trending movies and set featured content.
     */
    private suspend fun loadTrendingMovies() {
        contentDiscoveryRepository.getTrendingMovies(currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { 
                    it.copy(
                        trendingMovies = content,
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
     * Load upcoming movies.
     */
    private suspend fun loadUpcomingMovies() {
        contentDiscoveryRepository.getUpcomingMovies(currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { it.copy(upcomingMovies = content) }
            }
    }
    
    /**
     * Load now playing movies.
     */
    private suspend fun loadNowPlayingMovies() {
        contentDiscoveryRepository.getNowPlayingMovies(currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { it.copy(nowPlayingMovies = content) }
            }
    }
    
    /**
     * Load action movies.
     */
    private suspend fun loadActionMovies() {
        contentDiscoveryRepository.getMoviesByGenre("Action", currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { it.copy(actionMovies = content) }
            }
    }
    
    /**
     * Load comedy movies.
     */
    private suspend fun loadComedyMovies() {
        contentDiscoveryRepository.getMoviesByGenre("Comedy", currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { it.copy(comedyMovies = content) }
            }
    }
    
    /**
     * Load drama movies.
     */
    private suspend fun loadDramaMovies() {
        contentDiscoveryRepository.getMoviesByGenre("Drama", currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { it.copy(dramaMovies = content) }
            }
    }
    
    /**
     * Load user's watchlist movies only.
     */
    private suspend fun loadWatchlistMovies() {
        currentProfile?.let { profile ->
            watchlistRepository.getWatchlistMovies(profile.id)
                .catch { e -> 
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { content ->
                    _uiState.update { it.copy(watchlistMovies = content) }
                }
        }
    }
    
    /**
     * Add movie to watchlist with ML tracking.
     */
    fun addToWatchlist(content: VideoContent) {
        viewModelScope.launch {
            try {
                currentProfile?.let { profile ->
                    watchlistRepository.addToWatchlist(profile.id, content)
                    _uiState.update { state ->
                        state.copy(watchlistMovies = state.watchlistMovies + content)
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
     * Remove movie from watchlist with ML tracking.
     */
    fun removeFromWatchlist(content: VideoContent) {
        viewModelScope.launch {
            try {
                currentProfile?.let { profile ->
                    watchlistRepository.removeFromWatchlist(profile.id, content.id)
                    _uiState.update { state ->
                        state.copy(watchlistMovies = state.watchlistMovies.filterNot { it.id == content.id })
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    /**
     * Search for movies.
     */
    fun searchMovies(query: String) {
        viewModelScope.launch {
            contentDiscoveryRepository.searchMovies(query, currentProfile)
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
     * Load ML-powered movie recommendations.
     */
    private suspend fun loadRecommendedMovies() {
        currentProfile?.let { profile ->
            try {
                recommendationRepository.getRecommendedMoviesForYou(profile.id)
                    .catch { e ->
                        // ML features are optional - don't fail the whole screen
                        println("ML Movie Recommendations failed: ${e.message}")
                    }
                    .collect { recommendations ->
                        _uiState.update { it.copy(recommendedMovies = recommendations) }
                    }
            } catch (e: Exception) {
                // Silently fail ML features to prevent white screen
                println("ML Movie Recommendations error: ${e.message}")
            }
        }
    }
    
    /**
     * Load personalized trending movies.
     */
    private suspend fun loadPersonalizedMovieTrending() {
        currentProfile?.let { profile ->
            try {
                recommendationRepository.getPersonalizedTrendingMovies(profile.id)
                    .catch { e ->
                        // ML features are optional - don't fail the whole screen
                        println("Personalized Movie Trending failed: ${e.message}")
                    }
                    .collect { trending ->
                        _uiState.update { it.copy(personalizedTrendingMovies = trending) }
                    }
            } catch (e: Exception) {
                // Silently fail ML features to prevent white screen
                println("Personalized Movie Trending error: ${e.message}")
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
    }
    
    /**
     * Fetch detailed content with cast and similar content.
     */
    suspend fun fetchDetailedContent(content: VideoContent): VideoContent {
        return try {
            val tmdbId = content.tmdbId.toIntOrNull() ?: return content
            
            when (content.type) {
                com.offordflix.domain.model.ContentType.MOVIE -> {
                    val response = contentDiscoveryRepository.getMovieDetails(tmdbId)
                    val details = response.getOrNull() ?: return content
                    try {
                        contentDiscoveryRepository.enrichContentWithLogos(listOf(details)).firstOrNull() ?: details
                    } catch (_: Exception) {
                        details
                    }
                }
                com.offordflix.domain.model.ContentType.TV_SHOW -> {
                    val response = contentDiscoveryRepository.getTvShowDetails(tmdbId)
                    val details = response.getOrNull() ?: return content
                    try {
                        contentDiscoveryRepository.enrichContentWithLogos(listOf(details)).firstOrNull() ?: details
                    } catch (_: Exception) {
                        details
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MoviesViewModel", "Failed to fetch detailed content", e)
            content
        }
    }
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for movies screen.
 */
data class MoviesUiState(
    val isLoading: Boolean = false,
    val featuredContent: VideoContent? = null,
    
    // Enhanced content from backend
    val enhancedContent: com.offordflix.data.dto.MoviesContentResponse? = null,
    
    // Legacy individual content lists (for fallback)
    val trendingMovies: List<VideoContent> = emptyList(),
    val popularMovies: List<VideoContent> = emptyList(),
    val topRatedMovies: List<VideoContent> = emptyList(),
    val upcomingMovies: List<VideoContent> = emptyList(),
    val nowPlayingMovies: List<VideoContent> = emptyList(),
    val actionMovies: List<VideoContent> = emptyList(),
    val comedyMovies: List<VideoContent> = emptyList(),
    val dramaMovies: List<VideoContent> = emptyList(),
    val watchlistMovies: List<VideoContent> = emptyList(),
    val searchResults: List<VideoContent> = emptyList(),
    
    // ML-powered movie recommendations
    val recommendedMovies: List<VideoContent> = emptyList(),
    val personalizedTrendingMovies: List<VideoContent> = emptyList(),
    
    val error: String? = null
)
