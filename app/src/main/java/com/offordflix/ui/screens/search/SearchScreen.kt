package com.offordflix.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offordflix.domain.model.VideoContent
import com.offordflix.ui.components.ContentCard

/**
 * Search screen with voice and text input support.
 * 
 * Features:
 * - Text search with real-time results
 * - Voice search integration (Android TV compatible)
 * - Filter options (Movies, TV Shows, All)
 * - Grid layout for search results
 * - D-pad navigation optimized
 */
@Composable
fun SearchScreen(
    onNavigateToPlayer: (VideoContent) -> Unit,
    onNavigateToDetails: (VideoContent) -> Unit = {},
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(48.dp)
    ) {
        // Search header
        SearchHeader(
            query = uiState.query,
            onQueryChange = viewModel::updateQuery,
            onSearch = { 
                viewModel.search()
                keyboardController?.hide()
            },
            onVoiceSearch = viewModel::startVoiceSearch,
            onClear = viewModel::clearSearch
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Filter chips
        SearchFilters(
            selectedFilter = uiState.selectedFilter,
            onFilterSelected = viewModel::setFilter
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Search results
        when {
            uiState.isLoading -> {
                LoadingResults()
            }
            uiState.error != null -> {
                val error = uiState.error!!
                ErrorResults(
                    error = error,
                    onRetry = viewModel::search
                )
            }
            uiState.query.isBlank() -> {
                EmptySearchState()
            }
            uiState.searchResults.isEmpty() -> {
                NoResultsState(query = uiState.query)
            }
            else -> {
                SearchResults(
                    results = uiState.filteredResults,
                    onContentClick = onNavigateToDetails,
                    onPlayClick = onNavigateToPlayer,
                    onAddToWatchlist = viewModel::addToWatchlist,
                    onRemoveFromWatchlist = viewModel::removeFromWatchlist,
                    watchlist = uiState.watchlist
                )
            }
        }
    }
}

/**
 * Search header with input field and voice search.
 */
@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onVoiceSearch: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search input field
        SearchTextField(
            value = query,
            onValueChange = onQueryChange,
            onSearch = onSearch,
            modifier = Modifier.weight(1f)
        )
        
        // Voice search button
        Button(
            onClick = onVoiceSearch,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "🎤",
                fontSize = 20.sp
            )
        }
        
        // Clear button
        if (query.isNotBlank()) {
            Button(
                onClick = onClear,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Clear")
            }
        }
    }
}

/**
 * Custom search text field optimized for TV.
 */
@Composable
private fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .background(
                if (isFocused) MaterialTheme.colorScheme.surface 
                else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        textStyle = TextStyle(
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() }
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "Search movies and TV shows...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 18.sp
                    )
                }
                innerTextField()
            }
        }
    )
}

/**
 * Search filter chips.
 */
@Composable
private fun SearchFilters(
    selectedFilter: SearchFilter,
    onFilterSelected: (SearchFilter) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SearchFilter.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter.displayName,
                        fontSize = 14.sp,
                        fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

/**
 * Search results grid.
 */
@Composable
private fun SearchResults(
    results: List<VideoContent>,
    onContentClick: (VideoContent) -> Unit,
    onPlayClick: (VideoContent) -> Unit,
    onAddToWatchlist: (VideoContent) -> Unit,
    onRemoveFromWatchlist: (VideoContent) -> Unit,
    watchlist: List<VideoContent>
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(200.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(results) { content ->
            ContentCard(
                content = content,
                onClick = { onContentClick(content) },
                onPlayClick = { onPlayClick(content) },
                onWatchlistClick = {
                    if (watchlist.any { it.id == content.id }) {
                        onRemoveFromWatchlist(content)
                    } else {
                        onAddToWatchlist(content)
                    }
                },
                isInWatchlist = watchlist.any { it.id == content.id }
            )
        }
    }
}

/**
 * Loading state for search results.
 */
@Composable
private fun LoadingResults() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Searching...",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Error state for search results.
 */
@Composable
private fun ErrorResults(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Search failed",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Retry")
        }
    }
}

/**
 * Empty search state (no query entered).
 */
@Composable
private fun EmptySearchState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🔍",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Search for movies and TV shows",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Use the search bar above or press the voice search button",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

/**
 * No results found state.
 */
@Composable
private fun NoResultsState(query: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "😔",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No results found",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No results for \"$query\"",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try a different search term or check your spelling",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

