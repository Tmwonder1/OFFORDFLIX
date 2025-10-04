package com.offordflix.ui.screens.shows

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
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
import android.util.Log
import kotlinx.coroutines.delay
import com.offordflix.ui.theme.SynopsisFontFamily
import com.offordflix.ui.utils.ContentMetadataUtils

/**
 * Netflix-style TV shows screen with hero banner and TV show content rows.
 * Matches the exact structure and behavior of the home screen.
 */
@Composable
fun ShowsScreen(
    onNavigateToPlayer: (VideoContent) -> Unit,
    onNavigateToDetails: (VideoContent) -> Unit = {},
    profileId: String? = null,
    viewModel: TVShowsViewModel = hiltViewModel(),
    externalFocusRequester: FocusRequester? = null,
    onExpandDrawer: (() -> Unit)? = null
) {
    NetflixStyleTVShowsScreen(
        onNavigateToPlayer = onNavigateToPlayer,
        onNavigateToDetails = onNavigateToDetails,
        profileId = profileId,
        viewModel = viewModel,
        externalFocusRequester = externalFocusRequester,
        onExpandDrawer = onExpandDrawer
    )
}

@Composable
fun NetflixStyleTVShowsScreen(
    onNavigateToPlayer: (VideoContent) -> Unit,
    onNavigateToDetails: (VideoContent) -> Unit = {},
    profileId: String? = null,
    backStackEntry: androidx.navigation.NavBackStackEntry? = null,
    viewModel: TVShowsViewModel = if (backStackEntry != null) {
        hiltViewModel(backStackEntry)
    } else {
        hiltViewModel()
    },
    externalFocusRequester: FocusRequester? = null,
    onExpandDrawer: (() -> Unit)? = null,
    onOverlayVisibilityChanged: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Local UI state for in-place details overlay
    var selectedContent by remember { mutableStateOf<VideoContent?>(null) }
    var isDetailsVisible by remember { mutableStateOf(false) }
    
    // Notify navigation about overlay visibility changes
    LaunchedEffect(isDetailsVisible) {
        onOverlayVisibilityChanged(isDetailsVisible)
    }
    
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
        // Use enhanced content if available, otherwise fallback to legacy content
        val enhancedContent = uiState.enhancedContent
        if (enhancedContent != null) {
            // Convert enhanced content sections to content rows
            enhancedContent.sections.map { section ->
                section.title to section.data.map { it.toVideoContent() }
            }
        } else {
            // Legacy content rows for fallback
            listOfNotNull(
                if (uiState.trendingTvShows.isNotEmpty()) "Trending TV Shows" to uiState.trendingTvShows else null,
                if (uiState.popularTvShows.isNotEmpty()) "Popular TV Shows" to uiState.popularTvShows else null,
                if (uiState.topRatedTvShows.isNotEmpty()) "Top Rated TV Shows" to uiState.topRatedTvShows else null,
                if (uiState.onAirTvShows.isNotEmpty()) "Currently Airing" to uiState.onAirTvShows else null,
                if (uiState.dramaTvShows.isNotEmpty()) "Drama Series" to uiState.dramaTvShows else null,
                if (uiState.comedyTvShows.isNotEmpty()) "Comedy Series" to uiState.comedyTvShows else null,
                if (uiState.actionTvShows.isNotEmpty()) "Action & Adventure" to uiState.actionTvShows else null,
                if (uiState.sciFiTvShows.isNotEmpty()) "Sci-Fi & Fantasy" to uiState.sciFiTvShows else null,
                if (uiState.watchlistTvShows.isNotEmpty()) "My TV Shows List" to uiState.watchlistTvShows else null
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Dynamic Hero Banner - changes based on focused content
        val heroContent = remember(currentRowIndex, currentItemIndex, uiState, selectedContent, isDetailsVisible) {
            // When details overlay is visible, pin hero to the selected content
            if (isDetailsVisible && selectedContent != null) {
                return@remember selectedContent
            }
            if (contentRows.isNotEmpty() && currentRowIndex < contentRows.size) {
                val currentRowContent = contentRows[currentRowIndex].second.take(10)
                if (currentItemIndex < currentRowContent.size) {
                    currentRowContent[currentItemIndex]
                } else {
                    // Fallback to first item in current row
                    currentRowContent.firstOrNull()
                }
            } else {
                // Fallback to featured content
                uiState.featuredContent 
                    ?: uiState.trendingTvShows.firstOrNull()
                    ?: uiState.popularTvShows.firstOrNull()
            }
        }
        
        heroContent?.let { content ->
            // Add smooth transition when hero content changes
            AnimatedContent(
                targetState = content,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith 
                    fadeOut(animationSpec = tween(200))
                },
                modifier = Modifier.fillMaxSize()
            ) { targetContent ->
                HeroBanner(
                    content = targetContent,
                    onPlay = { 
                        viewModel.trackContentInteraction(targetContent, InteractionType.WATCH)
                        onNavigateToPlayer(targetContent) 
                    },
                    onMoreInfo = {
                        // Show in-place details overlay instead of navigation
                        selectedContent = targetContent
                        isDetailsVisible = true
                        viewModel.onContentSelected(targetContent)
                    },
                    onAddToWatchlist = { viewModel.addToWatchlist(targetContent) },
                    isInWatchlist = uiState.watchlistTvShows.any { it.id == targetContent.id },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Animated Content Rows Overlay with Global Key Handler
        if (!isDetailsVisible && contentRows.isNotEmpty() && currentRowIndex < contentRows.size) {
            val focusRequester = externalFocusRequester ?: remember { FocusRequester() }
            
            // Request focus for key event handling - ensure focus is established properly
            LaunchedEffect(contentRows.isNotEmpty()) {
                if (externalFocusRequester == null && contentRows.isNotEmpty()) {
                    delay(200) // Increased delay to ensure UI is fully ready
                    try {
                        focusRequester.requestFocus()
                        Log.d("TVShowsScreen", "Initial focus requested for content rows (${contentRows.size} rows available)")
                    } catch (e: Exception) {
                        Log.e("TVShowsScreen", "Failed to request initial focus", e)
                    }
                }
            }
            
            // Listen for external focus requests and ensure immediate focus
            LaunchedEffect(externalFocusRequester) {
                externalFocusRequester?.let {
                    Log.d("TVShowsScreen", "External focus requester provided, requesting focus immediately")
                    delay(50) // Small delay to ensure the requester is ready
                    try {
                        it.requestFocus()
                        Log.d("TVShowsScreen", "External focus requested successfully")
                    } catch (e: Exception) {
                        Log.e("TVShowsScreen", "Failed to request external focus", e)
                    }
                }
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
                        // Handle D-pad navigation manually with improved logging
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            Log.d("TVShowsScreen", "Key event: ${keyEvent.key}, currentRow=$currentRowIndex, currentItem=$currentItemIndex")
                            when (keyEvent.key) {
                                Key.DirectionUp -> {
                                    if (currentRowIndex > 0) {
                                        currentRowIndex--
                                        currentItemIndex = 0 // Reset to first item in new row
                                        Log.d("TVShowsScreen", "Moved up to row $currentRowIndex")
                                        true
                                    } else {
                                        Log.d("TVShowsScreen", "Already at top row")
                                        false
                                    }
                                }
                                Key.DirectionDown -> {
                                    if (currentRowIndex < contentRows.size - 1) {
                                        currentRowIndex++
                                        currentItemIndex = 0 // Reset to first item in new row
                                        Log.d("TVShowsScreen", "Moved down to row $currentRowIndex")
                                        true
                                    } else {
                                        Log.d("TVShowsScreen", "Already at bottom row")
                                        false
                                    }
                                }
                                Key.DirectionLeft -> {
                                    if (currentItemIndex > 0) {
                                        currentItemIndex--
                                        Log.d("TVShowsScreen", "Moved left to item $currentItemIndex")
                                        true
                                    } else {
                                        Log.d("TVShowsScreen", "At leftmost item, expanding drawer")
                                        onExpandDrawer?.invoke()
                                        true
                                    }
                                }
                                Key.DirectionRight -> {
                                    val currentRow = contentRows.getOrNull(currentRowIndex)
                                    val maxItems = currentRow?.second?.take(10)?.size ?: 0
                                    if (currentItemIndex < maxItems - 1) {
                                        currentItemIndex++
                                        Log.d("TVShowsScreen", "Moved right to item $currentItemIndex")
                                        true
                                    } else {
                                        Log.d("TVShowsScreen", "Already at rightmost item (max: $maxItems)")
                                        false
                                    }
                                }
                                Key.DirectionCenter, Key.Enter -> {
                                    // Handle click/select action manually
                                    val currentRow = contentRows.getOrNull(currentRowIndex)
                                    val currentRowContent = currentRow?.second?.take(10)
                                    if (currentRowContent != null && currentItemIndex < currentRowContent.size) {
                                        val clickedContent = currentRowContent[currentItemIndex]
                                        Log.d("TVShowsScreen", "Selected content: ${clickedContent.title}")
                                        // Open inline details overlay
                                        viewModel.onContentSelected(clickedContent)
                                        selectedContent = clickedContent
                                        isDetailsVisible = true
                                    }
                                    true
                                }
                                else -> {
                                    Log.d("TVShowsScreen", "Unhandled key: ${keyEvent.key}")
                                    false
                                }
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
                        Log.d("TVShowsScreen", "Rendering ContentRowOverlay: title='$title', rowIndex=$targetIndex, focusedItem=$currentItemIndex, contentSize=${content.size}")
                        ContentRowOverlay(
                            title = title,
                            content = content,
                            currentIndex = targetIndex,
                            totalRows = contentRows.size,
                            focusedItemIndex = currentItemIndex,
                            onContentClick = { content ->
                                // Open inline details overlay
                                Log.d("TVShowsScreen", "onContentClick handler called with title='${content.title}', id=${content.id}")
                                viewModel.onContentSelected(content)
                                selectedContent = content
                                isDetailsVisible = true
                                Log.d("TVShowsScreen", "selectedContent updated to title='${selectedContent?.title}', id=${selectedContent?.id}")
                            },
                            onPlayClick = { content ->
                                viewModel.trackContentInteraction(content, InteractionType.WATCH)
                                onNavigateToPlayer(content)
                            },
                            onAddToWatchlist = viewModel::addToWatchlist,
                            onRemoveFromWatchlist = viewModel::removeFromWatchlist,
                            watchlist = uiState.watchlistTvShows
                        )
                    }
                }
            }
        }
        
        // In-place Details Overlay - covers content rows area while keeping hero visible
        val details = selectedContent
        if (isDetailsVisible && details != null) {
            Log.d("TVShowsScreen", "Rendering ContentDetailsOverlay for title='${details.title}', id=${details.id}")
            ContentDetailsOverlay(
                content = details,
                isInWatchlist = uiState.watchlistTvShows.any { it.id == details.id },
                onPlay = {
                    viewModel.trackContentInteraction(details, InteractionType.WATCH)
                    onNavigateToPlayer(details)
                },
                onToggleWatchlist = {
                    if (uiState.watchlistTvShows.any { it.id == details.id }) {
                        viewModel.removeFromWatchlist(details)
                    } else {
                        viewModel.addToWatchlist(details)
                    }
                },
                onDismiss = {
                    isDetailsVisible = false
                }
            )
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
                        text = "Loading TV shows...",
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
                        text = "⚠️ TV Shows Loading Failed",
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
 * Identical to home screen implementation but for TV shows only.
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
        
        // Removed auto-scroll animations for better performance
        // Only scroll to focused item without animation for smoother experience
        LaunchedEffect(focusedItemIndex) {
            if (content.isNotEmpty() && focusedItemIndex < content.take(10).size) {
                // Use scrollToItem instead of animateScrollToItem for instant, smooth scrolling
                listState.scrollToItem(focusedItemIndex)
            }
        }
        
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Reset to original spacing
        ) {
            val limited = content.take(10)
            itemsIndexed(
                items = limited,
                key = { _, item -> item.id }
            ) { index, videoContent ->
                
                ContentCard(
                    content = videoContent,
                    onClick = { 
                        // Clicks are now handled manually via D-pad center button
                    },
                    onPlayClick = { onPlayClick(limited[index]) }, // Use index-based lookup
                    onWatchlistClick = {
                        val indexContent = limited[index]
                        if (watchlist.any { it.id == indexContent.id }) {
                            onRemoveFromWatchlist(indexContent)
                        } else {
                            onAddToWatchlist(indexContent)
                        }
                    },
                    isInWatchlist = watchlist.any { it.id == limited[index].id },
                    isManuallyFocused = index == focusedItemIndex, // Use direct index comparison
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * In-place content details overlay shown over the bottom area.
 * Keeps the hero backdrop visible while hiding the content rows.
 * Identical to home screen implementation.
 */
@Composable
private fun ContentDetailsOverlay(
    content: VideoContent,
    isInWatchlist: Boolean,
    onPlay: () -> Unit,
    onToggleWatchlist: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    
    // Request initial focus so D-pad works inside the panel
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown && (keyEvent.key == Key.Back || keyEvent.key == Key.Escape)) {
                        onDismiss()
                        true
                    } else false
                }
                .padding(horizontal = 48.dp, vertical = 16.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Title and overview
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = content.title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                
                // All metadata in single continuous line with bullet separators
                val metadata = ContentMetadataUtils.getFormattedMetadata(content)
                if (metadata.isNotEmpty()) {
                    Text(
                        text = ContentMetadataUtils.joinMetadata(metadata),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                
                if (!content.overview.isNullOrBlank()) {
                    Text(
                        text = content.overview,
                        color = Color.White.copy(alpha = 0.9f),
                        fontFamily = SynopsisFontFamily,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        maxLines = 4
                    )
                }
            }
            
            // Right: Actions
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = onPlay) {
                    Text("Play")
                }
                Button(onClick = onToggleWatchlist) {
                    Text(if (isInWatchlist) "Remove from My List" else "+ My List")
                }
                Button(onClick = onDismiss) {
                    Text("Back")
                }
            }
            }
        }
    }
}
