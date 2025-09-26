package com.offordflix.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offordflix.data.repository.ContentDiscoveryRepository
import com.offordflix.data.repository.WatchlistRepository
import com.offordflix.domain.model.ContentType
import com.offordflix.domain.model.Profile
import com.offordflix.domain.model.VideoContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for search functionality.
 * 
 * Manages search queries, filters, voice search, and watchlist
 * operations for the search screen.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val contentDiscoveryRepository: ContentDiscoveryRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    // Internal state
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    // Current active profile
    private var currentProfile: Profile? = null
    
    init {
        // Load watchlist when ViewModel is created
        loadWatchlist()
    }
    
    /**
     * Set the current active profile.
     */
    fun setProfile(profile: Profile) {
        currentProfile = profile
        loadWatchlist()
        
        // Re-search if there's a query
        if (_uiState.value.query.isNotBlank()) {
            search()
        }
    }
    
    /**
     * Update search query.
     */
    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        
        // Auto-search if query is long enough
        if (query.length >= 3) {
            search()
        } else if (query.isEmpty()) {
            clearSearchResults()
        }
    }
    
    /**
     * Perform search.
     */
    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                contentDiscoveryRepository.searchContent(query, currentProfile)
                    .catch { e ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = e.message ?: "Search failed"
                            ) 
                        }
                    }
                    .collect { results ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                searchResults = results,
                                error = null
                            ) 
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Search failed"
                    ) 
                }
            }
        }
    }
    
    /**
     * Start voice search.
     */
    fun startVoiceSearch() {
        // In a real implementation, this would trigger Android's voice recognition
        // For now, we'll simulate it or show a placeholder
        _uiState.update { it.copy(isVoiceSearchActive = true) }
        
        // TODO: Integrate with Android Speech-to-Text API
        // This would typically launch an intent for voice recognition
        // and handle the result in an activity result callback
    }
    
    /**
     * Handle voice search result.
     */
    fun handleVoiceSearchResult(spokenText: String) {
        _uiState.update { 
            it.copy(
                isVoiceSearchActive = false,
                query = spokenText
            ) 
        }
        search()
    }
    
    /**
     * Cancel voice search.
     */
    fun cancelVoiceSearch() {
        _uiState.update { it.copy(isVoiceSearchActive = false) }
    }
    
    /**
     * Set search filter.
     */
    fun setFilter(filter: SearchFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }
    
    /**
     * Clear search query and results.
     */
    fun clearSearch() {
        _uiState.update { 
            it.copy(
                query = "",
                searchResults = emptyList(),
                error = null
            ) 
        }
    }
    
    /**
     * Clear search results only.
     */
    private fun clearSearchResults() {
        _uiState.update { 
            it.copy(
                searchResults = emptyList(),
                error = null
            ) 
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
                        loadWatchlist()
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
                        loadWatchlist()
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(error = e.message) }
                    }
            }
        }
    }
    
    /**
     * Load user's watchlist.
     */
    private fun loadWatchlist() {
        viewModelScope.launch {
            currentProfile?.let { profile ->
                watchlistRepository.getWatchlist(profile.id)
                    .catch { e ->
                        _uiState.update { it.copy(error = e.message) }
                    }
                    .collect { watchlist ->
                        _uiState.update { it.copy(watchlist = watchlist) }
                    }
            }
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
 * UI state for search screen.
 */
data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val searchResults: List<VideoContent> = emptyList(),
    val selectedFilter: SearchFilter = SearchFilter.ALL,
    val watchlist: List<VideoContent> = emptyList(),
    val isVoiceSearchActive: Boolean = false,
    val error: String? = null
) {
    /**
     * Get filtered search results based on selected filter.
     */
    val filteredResults: List<VideoContent>
        get() = when (selectedFilter) {
            SearchFilter.ALL -> searchResults
            SearchFilter.MOVIES -> searchResults.filter { it.type == ContentType.MOVIE }
            SearchFilter.TV_SHOWS -> searchResults.filter { it.type == ContentType.TV_SHOW }
        }
}

/**
 * Search filter options.
 */
enum class SearchFilter(val displayName: String) {
    ALL("All"),
    MOVIES("Movies"),
    TV_SHOWS("TV Shows")
}

