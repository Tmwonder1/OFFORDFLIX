package com.offordflix.ui.screens.shows

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
 * ViewModel for TV shows screen content discovery.
 * 
 * Manages TV show-specific content loading, watchlist operations, and user interactions
 * for the Netflix-style TV shows screen experience.
 */
@HiltViewModel
class TVShowsViewModel @Inject constructor(
    private val contentDiscoveryRepository: ContentDiscoveryRepository,
    private val watchlistRepository: WatchlistRepository,
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {

    // Internal state
    private val _uiState = MutableStateFlow(TVShowsUiState())
    val uiState: StateFlow<TVShowsUiState> = _uiState.asStateFlow()
    
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
            android.util.Log.d("TVShowsViewModel", "Initializing with profile: ${profile.id}")
            currentProfile = profile
            isInitialized = true
            loadContentIfNeeded()
        } else {
            android.util.Log.d("TVShowsViewModel", "Already initialized with profile: ${profile.id}, skipping initialization")
        }
    }
    
    /**
     * Load content only if cache is expired or content is empty.
     */
    private fun loadContentIfNeeded() {
        val currentTime = System.currentTimeMillis()
        val hasContent = uiState.value.enhancedContent != null || 
                        uiState.value.trendingTvShows.isNotEmpty()
        val isCacheValid = (currentTime - contentLoadTime) < cacheValidityDuration
        
        if (!hasContent || !isCacheValid) {
            android.util.Log.d("TVShowsViewModel", "Loading content - hasContent: $hasContent, cacheValid: $isCacheValid")
            loadContent()
        } else {
            android.util.Log.d("TVShowsViewModel", "Using cached content - age: ${(currentTime - contentLoadTime) / 1000}s")
        }
    }
    
    /**
     * Load all TV show content for the TV shows screen.
     */
    fun loadContent() {
        // Prevent concurrent loading
        if (isContentLoading) {
            android.util.Log.d("TVShowsViewModel", "Content already loading, skipping duplicate request")
            return
        }
        
        viewModelScope.launch {
            isContentLoading = true
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Try to load enhanced TV shows content first
                launch { loadEnhancedTVShowsContent() }
                
                // Load fallback content in parallel (for when backend is unavailable)
                launch { loadTrendingTvShows() }
                launch { loadPopularTvShows() }
                launch { loadTopRatedTvShows() }
                launch { loadOnAirTvShows() }
                launch { loadDramaTvShows() }
                launch { loadComedyTvShows() }
                launch { loadActionTvShows() }
                launch { loadSciFiTvShows() }
                launch { loadWatchlistTvShows() }
                
                // Only load ML features if profile is set
                if (currentProfile != null) {
                    launch { loadRecommendedTvShows() }
                    launch { loadPersonalizedTvShowTrending() }
                }
                
                // Update cache timestamp on successful load
                contentLoadTime = System.currentTimeMillis()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to load TV show content"
                    )
                }
            } finally {
                isContentLoading = false
            }
        }
    }
    
    /**
     * Load enhanced TV shows content with all TV show rows from backend.
     */
    private suspend fun loadEnhancedTVShowsContent() {
        // Check cache first
        val cachedContent = ContentCacheManager.getCachedTVShowsContent()
        if (cachedContent != null) {
            android.util.Log.d("TVShowsViewModel", "Using cached TV shows content with ${cachedContent.sections.size} sections")
            _uiState.update { 
                it.copy(
                    enhancedContent = cachedContent,
                    featuredContent = cachedContent.slide.firstOrNull()?.toVideoContent()
                )
            }
            return
        }
        
        contentDiscoveryRepository.getEnhancedTVShowsContent(currentProfile)
            .catch { e -> 
                // Don't fail the whole screen if enhanced content fails
                println("Enhanced TV shows content loading failed: ${e.message}")
            }
            .collect { enhancedContent ->
                if (enhancedContent != null) {
                    android.util.Log.d("TVShowsViewModel", "Enhanced TV shows content loaded successfully with ${enhancedContent.sections.size} sections")

                    // Immediately use raw content for instant draw
                    _uiState.update {
                        it.copy(
                            enhancedContent = enhancedContent,
                            featuredContent = enhancedContent.slide.firstOrNull()?.toVideoContent()
                        )
                    }

                    // Enrich in background
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
                            ContentCacheManager.setCachedTVShowsContent(enrichedEnhancedContent)
                            _uiState.update {
                                it.copy(
                                    enhancedContent = enrichedEnhancedContent,
                                    featuredContent = enrichedSlide.firstOrNull()
                                )
                            }
                        } catch (_: Exception) { }
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
     * Load trending TV shows and set featured content.
     */
    private suspend fun loadTrendingTvShows() {
        contentDiscoveryRepository.getTrendingTvShows(currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { 
                    it.copy(
                        trendingTvShows = content,
                        featuredContent = content.firstOrNull() // Use first trending as featured
                    ) 
                }
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
     * Load on-air TV shows.
     */
    private suspend fun loadOnAirTvShows() {
        contentDiscoveryRepository.getOnAirTvShows(currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { it.copy(onAirTvShows = content) }
            }
    }
    
    /**
     * Load drama TV shows.
     */
    private suspend fun loadDramaTvShows() {
        contentDiscoveryRepository.getTvShowsByGenre("Drama", currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { it.copy(dramaTvShows = content) }
            }
    }
    
    /**
     * Load comedy TV shows.
     */
    private suspend fun loadComedyTvShows() {
        contentDiscoveryRepository.getTvShowsByGenre("Comedy", currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { it.copy(comedyTvShows = content) }
            }
    }
    
    /**
     * Load action TV shows.
     */
    private suspend fun loadActionTvShows() {
        contentDiscoveryRepository.getTvShowsByGenre("Action & Adventure", currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { it.copy(actionTvShows = content) }
            }
    }
    
    /**
     * Load sci-fi TV shows.
     */
    private suspend fun loadSciFiTvShows() {
        contentDiscoveryRepository.getTvShowsByGenre("Sci-Fi & Fantasy", currentProfile)
            .catch { e -> 
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { content ->
                _uiState.update { it.copy(sciFiTvShows = content) }
            }
    }
    
    /**
     * Load user's watchlist TV shows only.
     */
    private suspend fun loadWatchlistTvShows() {
        currentProfile?.let { profile ->
            watchlistRepository.getWatchlistTvShows(profile.id)
                .catch { e -> 
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { content ->
                    _uiState.update { it.copy(watchlistTvShows = content) }
                }
        }
    }
    
    /**
     * Add TV show to watchlist with ML tracking.
     */
    fun addToWatchlist(content: VideoContent) {
        viewModelScope.launch {
            try {
                currentProfile?.let { profile ->
                    watchlistRepository.addToWatchlist(profile.id, content)
                    _uiState.update { state ->
                        state.copy(watchlistTvShows = state.watchlistTvShows + content)
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
     * Remove TV show from watchlist with ML tracking.
     */
    fun removeFromWatchlist(content: VideoContent) {
        viewModelScope.launch {
            try {
                currentProfile?.let { profile ->
                    watchlistRepository.removeFromWatchlist(profile.id, content.id)
                    _uiState.update { state ->
                        state.copy(watchlistTvShows = state.watchlistTvShows.filterNot { it.id == content.id })
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    /**
     * Search for TV shows.
     */
    fun searchTvShows(query: String) {
        viewModelScope.launch {
            contentDiscoveryRepository.searchTvShows(query, currentProfile)
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
     * Load ML-powered TV show recommendations.
     */
    private suspend fun loadRecommendedTvShows() {
        currentProfile?.let { profile ->
            try {
                recommendationRepository.getRecommendedTvShowsForYou(profile.id)
                    .catch { e ->
                        // ML features are optional - don't fail the whole screen
                        println("ML TV Show Recommendations failed: ${e.message}")
                    }
                    .collect { recommendations ->
                        _uiState.update { it.copy(recommendedTvShows = recommendations) }
                    }
            } catch (e: Exception) {
                // Silently fail ML features to prevent white screen
                println("ML TV Show Recommendations error: ${e.message}")
            }
        }
    }
    
    /**
     * Load personalized trending TV shows.
     */
    private suspend fun loadPersonalizedTvShowTrending() {
        currentProfile?.let { profile ->
            try {
                recommendationRepository.getPersonalizedTrendingTvShows(profile.id)
                    .catch { e ->
                        // ML features are optional - don't fail the whole screen
                        println("Personalized TV Show Trending failed: ${e.message}")
                    }
                    .collect { trending ->
                        _uiState.update { it.copy(personalizedTrendingTvShows = trending) }
                    }
            } catch (e: Exception) {
                // Silently fail ML features to prevent white screen
                println("Personalized TV Show Trending error: ${e.message}")
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
                    response.getOrNull() ?: content
                }
                com.offordflix.domain.model.ContentType.TV_SHOW -> {
                    val response = contentDiscoveryRepository.getTvShowDetails(tmdbId)
                    response.getOrNull() ?: content
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TVShowsViewModel", "Failed to fetch detailed content", e)
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
 * UI state for TV shows screen.
 */
data class TVShowsUiState(
    val isLoading: Boolean = false,
    val featuredContent: VideoContent? = null,
    
    // Enhanced content from backend
    val enhancedContent: com.offordflix.data.dto.TVShowsContentResponse? = null,
    
    // Legacy individual content lists (for fallback)
    val trendingTvShows: List<VideoContent> = emptyList(),
    val popularTvShows: List<VideoContent> = emptyList(),
    val topRatedTvShows: List<VideoContent> = emptyList(),
    val onAirTvShows: List<VideoContent> = emptyList(),
    val dramaTvShows: List<VideoContent> = emptyList(),
    val comedyTvShows: List<VideoContent> = emptyList(),
    val actionTvShows: List<VideoContent> = emptyList(),
    val sciFiTvShows: List<VideoContent> = emptyList(),
    val watchlistTvShows: List<VideoContent> = emptyList(),
    val searchResults: List<VideoContent> = emptyList(),
    
    // ML-powered TV show recommendations
    val recommendedTvShows: List<VideoContent> = emptyList(),
    val personalizedTrendingTvShows: List<VideoContent> = emptyList(),
    
    val error: String? = null
)
