package com.offordflix.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offordflix.data.repository.ContentDiscoveryRepository
import com.offordflix.data.repository.WatchlistRepository
import com.offordflix.data.repository.RecommendationRepository
import com.offordflix.data.ml.InteractionType
import com.offordflix.data.repository.ViewingContext
import com.offordflix.domain.model.Profile
import com.offordflix.ui.cache.ContentCacheManager
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
            Log.d("HomeViewModel", "Initializing with profile: ${profile.id}")
            currentProfile = profile
            isInitialized = true
            loadContentIfNeeded()
        } else {
            Log.d("HomeViewModel", "Already initialized with profile: ${profile.id}, skipping initialization")
        }
    }
    
    /**
     * Load content only if cache is expired or content is empty.
     */
    private fun loadContentIfNeeded() {
        val currentTime = System.currentTimeMillis()
        val hasContent = uiState.value.enhancedContent != null || 
                        uiState.value.trendingContent.isNotEmpty()
        val isCacheValid = (currentTime - contentLoadTime) < cacheValidityDuration
        
        if (!hasContent || !isCacheValid) {
            Log.d("HomeViewModel", "Loading content - hasContent: $hasContent, cacheValid: $isCacheValid")
            loadContent()
        } else {
            Log.d("HomeViewModel", "Using cached content - age: ${(currentTime - contentLoadTime) / 1000}s")
        }
    }
    
    /**
     * Load all content for the home screen.
     */
    fun loadContent() {
        // Prevent concurrent loading
        if (isContentLoading) {
            Log.d("HomeViewModel", "Content already loading, skipping duplicate request")
            return
        }
        
        viewModelScope.launch {
            isContentLoading = true
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Try to load enhanced content first
                launch { loadEnhancedContent() }
                
                // Load fallback content in parallel (for when backend is unavailable)
                launch { loadTrendingContent() }
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
                
                // Update cache timestamp on successful load
                contentLoadTime = System.currentTimeMillis()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to load content"
                    )
                }
            } finally {
                isContentLoading = false
            }
        }
    }
    
    /**
     * Load enhanced content with all content rows from backend.
     */
    private suspend fun loadEnhancedContent() {
        // Check cache first
        val cachedContent = ContentCacheManager.getCachedHomeContent()
        if (cachedContent != null) {
            Log.d("HomeViewModel", "Using cached home content with ${cachedContent.sections.size} sections")
            _uiState.update { 
                it.copy(
                    enhancedContent = cachedContent,
                    featuredContent = cachedContent.slide.firstOrNull()?.toVideoContent()
                )
            }
            return
        }
        
        contentDiscoveryRepository.getEnhancedHomeContent(currentProfile)
            .catch { e -> 
                // Don't fail the whole screen if enhanced content fails
                Log.d("HomeViewModel", "Enhanced content loading failed: ${e.message}")
            }
            .collect { enhancedContent ->
                if (enhancedContent != null) {
                    Log.d("HomeViewModel", "Enhanced content loaded successfully with ${enhancedContent.sections.size} sections")
                    
                    // 1) Immediately show server content (without logos) to avoid blank UI
                    _uiState.update {
                        it.copy(
                            enhancedContent = enhancedContent,
                            featuredContent = enhancedContent.slide.firstOrNull()?.toVideoContent()
                        )
                    }

                    // 2) Enrich logos in background and update when ready
                    viewModelScope.launch {
                        try {
                            val enrichedSlide = contentDiscoveryRepository.enrichContentWithLogos(
                                enhancedContent.slide.map { it.toVideoContent() }
                            )
                            
                            val enrichedSections = enhancedContent.sections.map { section ->
                                Log.d("HomeViewModel", "Enriching section '${section.title}' with ${section.data.size} items")
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
                            ContentCacheManager.setCachedHomeContent(enrichedEnhancedContent)
                            _uiState.update {
                                it.copy(
                                    enhancedContent = enrichedEnhancedContent,
                                    featuredContent = enrichedSlide.firstOrNull()
                                )
                            }
                        } catch (e: Exception) {
                            Log.d("HomeViewModel", "Logo enrichment failed: ${e.message}")
                        }
                    }
                } else {
                    Log.d("HomeViewModel", "Enhanced content was null, falling back to legacy content")
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
     * Fetch detailed content with cast and similar content.
     */
    suspend fun fetchDetailedContent(content: VideoContent): VideoContent {
        return try {
            val tmdbId = content.tmdbId.toIntOrNull() ?: return content
            
            when (content.type) {
                com.offordflix.domain.model.ContentType.MOVIE -> {
                    val response = contentDiscoveryRepository.getMovieDetails(tmdbId)
                    val details = response.getOrNull() ?: return content
                    // Enrich with TMDB logo so hero shows original logo
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
            Log.e("HomeViewModel", "Failed to fetch detailed content", e)
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
 * UI state for home screen.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val featuredContent: VideoContent? = null,
    
    // Enhanced content from backend
    val enhancedContent: com.offordflix.data.dto.HomeContentResponse? = null,
    
    // Legacy individual content lists (for fallback)
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
