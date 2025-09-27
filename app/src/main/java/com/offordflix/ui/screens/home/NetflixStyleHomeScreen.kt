package com.offordflix.ui.screens.home

// Main HomeScreen is now NetflixStyleHomeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.itemsIndexed as tvItemsIndexed
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offordflix.domain.model.VideoContent
import com.offordflix.ui.components.ContentCard
import com.offordflix.ui.components.HeroBanner
import com.offordflix.data.ml.InteractionType

/**
 * Netflix-style home screen with fixed hero banner and animated content rows.
 */
@Composable
fun HomeScreen(
    onNavigateToPlayer: (VideoContent) -> Unit,
    onNavigateToDetails: (VideoContent) -> Unit = {},
    profileId: String? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    NetflixStyleHomeScreen(
        onNavigateToPlayer = onNavigateToPlayer,
        onNavigateToDetails = onNavigateToDetails,
        profileId = profileId,
        viewModel = viewModel
    )
}

@Composable
fun NetflixStyleHomeScreen(
    onNavigateToPlayer: (VideoContent) -> Unit,
    onNavigateToDetails: (VideoContent) -> Unit = {},
    profileId: String? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Initialize with profile and load content
    LaunchedEffect(profileId) {
        val profile = if (profileId != null) {
            com.offordflix.domain.model.Profile(
                id = profileId,
                name = "User",
                isKidsProfile = false,
                avatarId = 1,
                createdAt = System.currentTimeMillis(),
                lastUsed = System.currentTimeMillis()
            )
        } else {
            com.offordflix.domain.model.Profile(
                id = "default",
                name = "Default User", 
                isKidsProfile = false,
                avatarId = 1,
                createdAt = System.currentTimeMillis(),
                lastUsed = System.currentTimeMillis()
            )
        }
        
        viewModel.initializeWithProfile(profile)
    }
    
    var currentRowIndex by remember { mutableStateOf(0) }
    var currentItemIndex by remember { mutableStateOf(0) }
    val contentRows = remember(uiState) {
        listOfNotNull(
            if (uiState.trendingContent.isNotEmpty()) "Trending Now" to uiState.trendingContent else null,
            if (uiState.popularMovies.isNotEmpty()) "Popular Movies" to uiState.popularMovies else null,
            if (uiState.popularTvShows.isNotEmpty()) "Popular TV Shows" to uiState.popularTvShows else null,
            if (uiState.topRatedMovies.isNotEmpty()) "Top Rated Movies" to uiState.topRatedMovies else null,
            if (uiState.topRatedTvShows.isNotEmpty()) "Top Rated TV Shows" to uiState.topRatedTvShows else null
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Fixed Hero Banner Background
        val heroContent = uiState.featuredContent 
            ?: uiState.trendingContent.firstOrNull()
            ?: uiState.popularMovies.firstOrNull()
            ?: uiState.popularTvShows.firstOrNull()
        
        heroContent?.let { content ->
            HeroBanner(
                content = content,
                onPlay = { 
                    viewModel.trackContentInteraction(content, InteractionType.WATCH)
                    onNavigateToPlayer(content) 
                },
                onMoreInfo = { onNavigateToDetails(content) },
                onAddToWatchlist = { viewModel.addToWatchlist(content) },
                isInWatchlist = uiState.watchlist.any { it.id == content.id },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Animated Content Rows Overlay with Global Key Handler
        if (contentRows.isNotEmpty() && currentRowIndex < contentRows.size) {
            val focusRequester = remember { FocusRequester() }
            
            // Request focus for key event handling
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .focusRequester(focusRequester)
                    .focusable()
                    .onPreviewKeyEvent { keyEvent ->
                        // Handle D-pad navigation manually
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            when (keyEvent.key) {
                                Key.DirectionUp -> {
                                    if (currentRowIndex > 0) {
                                        currentRowIndex--
                                        currentItemIndex = 0 // Reset to first item in new row
                                        true
                                    } else false
                                }
                                Key.DirectionDown -> {
                                    if (currentRowIndex < contentRows.size - 1) {
                                        currentRowIndex++
                                        currentItemIndex = 0 // Reset to first item in new row
                                        true
                                    } else false
                                }
                                Key.DirectionLeft -> {
                                    if (currentItemIndex > 0) {
                                        currentItemIndex--
                                        true
                                    } else false
                                }
                                Key.DirectionRight -> {
                                    val currentRow = contentRows.getOrNull(currentRowIndex)
                                    val maxItems = currentRow?.second?.take(10)?.size ?: 0
                                    if (currentItemIndex < maxItems - 1) {
                                        currentItemIndex++
                                        true
                                    } else false
                                }
                                else -> false
                            }
                        } else false
                    }
            ) {
                AnimatedContent(
                    targetState = currentRowIndex,
                    transitionSpec = {
                        slideInVertically(
                            animationSpec = tween(400, easing = EaseInOutCubic),
                            initialOffsetY = { it / 3 }
                        ) + fadeIn(
                            animationSpec = tween(300, delayMillis = 50)
                        ) togetherWith slideOutVertically(
                            animationSpec = tween(400, easing = EaseInOutCubic),
                            targetOffsetY = { -it / 3 }
                        ) + fadeOut(
                            animationSpec = tween(200)
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { targetIndex ->
                    if (targetIndex < contentRows.size) {
                        val (title, content) = contentRows[targetIndex]
                        ContentRowOverlay(
                            title = title,
                            content = content,
                            currentIndex = targetIndex,
                            totalRows = contentRows.size,
                            focusedItemIndex = currentItemIndex,
                            onContentClick = { content ->
                                viewModel.onContentSelected(content)
                                onNavigateToDetails(content)
                            },
                            onPlayClick = { content ->
                                viewModel.trackContentInteraction(content, InteractionType.WATCH)
                                onNavigateToPlayer(content)
                            },
                            onAddToWatchlist = viewModel::addToWatchlist,
                            onRemoveFromWatchlist = viewModel::removeFromWatchlist,
                            watchlist = uiState.watchlist
                        )
                    }
                }
            }
        }
        
        // Loading State
        if (uiState.isLoading && contentRows.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Loading content...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }
        }
        
        // Error State
        uiState.error?.let { error ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "⚠️ Content Loading Failed",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Button(
                        onClick = { 
                            viewModel.clearError()
                            viewModel.loadContent()
                        }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

/**
 * Content row overlay with smaller cards optimized for TV layout.
 */
@Composable
private fun ContentRowOverlay(
    title: String,
    content: List<VideoContent>,
    currentIndex: Int,
    totalRows: Int,
    focusedItemIndex: Int,
    onContentClick: (VideoContent) -> Unit,
    onPlayClick: (VideoContent) -> Unit,
    onAddToWatchlist: (VideoContent) -> Unit,
    onRemoveFromWatchlist: (VideoContent) -> Unit,
    watchlist: List<VideoContent>
) {
    if (content.isEmpty()) return
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 48.dp, bottom = 16.dp, top = 8.dp)
    ) {
        // Row title with indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // Row indicator
            Text(
                text = "${currentIndex + 1} of $totalRows",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Horizontal content list with smaller cards (TV-optimized)
        val listState = rememberLazyListState()
        
        // Auto-scroll when row changes or focus moves
        LaunchedEffect(currentIndex) {
            if (content.isNotEmpty()) {
                listState.animateScrollToItem(0)
            }
        }
        
        // Auto-scroll to focused item when navigating horizontally
        LaunchedEffect(focusedItemIndex) {
            if (content.isNotEmpty() && focusedItemIndex < content.take(10).size) {
                listState.animateScrollToItem(focusedItemIndex)
            }
        }
        
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val limited = content.take(10)
            itemsIndexed(limited) { index, videoContent ->
                ContentCard(
                    content = videoContent,
                    onClick = { onContentClick(videoContent) },
                    onPlayClick = { onPlayClick(videoContent) },
                    onWatchlistClick = {
                        if (watchlist.any { it.id == videoContent.id }) {
                            onRemoveFromWatchlist(videoContent)
                        } else {
                            onAddToWatchlist(videoContent)
                        }
                    },
                    isInWatchlist = watchlist.any { it.id == videoContent.id },
                    isManuallyFocused = index == focusedItemIndex, // Manual focus state
                    modifier = Modifier.padding(vertical = 4.dp) // Reduced spacing for more compact layout
                )
            }
        }
    }
}
