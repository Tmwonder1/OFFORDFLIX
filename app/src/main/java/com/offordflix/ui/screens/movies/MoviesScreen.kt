package com.offordflix.ui.screens.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.offordflix.domain.model.VideoContent
import com.offordflix.ui.components.ContentCard
import com.offordflix.ui.components.HeroBanner
import com.offordflix.ui.components.CastTile
import com.offordflix.data.ml.InteractionType
import android.util.Log
import kotlinx.coroutines.delay
import com.offordflix.ui.theme.SynopsisFontFamily
import com.offordflix.ui.utils.ContentMetadataUtils

/**
 * Netflix-style movies screen with hero banner and movie content rows.
 * Matches the exact structure and behavior of the home screen.
 */
@Composable
fun MoviesScreen(
    onNavigateToPlayer: (VideoContent) -> Unit,
    onNavigateToDetails: (VideoContent) -> Unit = {},
    profileId: String? = null,
    viewModel: MoviesViewModel = hiltViewModel(),
    externalFocusRequester: FocusRequester? = null,
    onExpandDrawer: (() -> Unit)? = null
) {
    NetflixStyleMoviesScreen(
        onNavigateToPlayer = onNavigateToPlayer,
        onNavigateToDetails = onNavigateToDetails,
        profileId = profileId,
        viewModel = viewModel,
        externalFocusRequester = externalFocusRequester,
        onExpandDrawer = onExpandDrawer
    )
}

@Composable
fun NetflixStyleMoviesScreen(
    onNavigateToPlayer: (VideoContent) -> Unit,
    onNavigateToDetails: (VideoContent) -> Unit = {},
    profileId: String? = null,
    backStackEntry: androidx.navigation.NavBackStackEntry? = null,
    viewModel: MoviesViewModel = if (backStackEntry != null) {
        hiltViewModel(backStackEntry)
    } else {
        hiltViewModel()
    },
    externalFocusRequester: FocusRequester? = null,
    onExpandDrawer: (() -> Unit)? = null,
    onOverlayVisibilityChanged: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    
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
                if (uiState.trendingMovies.isNotEmpty()) "Trending Movies" to uiState.trendingMovies else null,
                if (uiState.popularMovies.isNotEmpty()) "Popular Movies" to uiState.popularMovies else null,
                if (uiState.topRatedMovies.isNotEmpty()) "Top Rated Movies" to uiState.topRatedMovies else null,
                if (uiState.nowPlayingMovies.isNotEmpty()) "Now Playing" to uiState.nowPlayingMovies else null,
                if (uiState.upcomingMovies.isNotEmpty()) "Coming Soon" to uiState.upcomingMovies else null,
                if (uiState.actionMovies.isNotEmpty()) "Action Movies" to uiState.actionMovies else null,
                if (uiState.comedyMovies.isNotEmpty()) "Comedy Movies" to uiState.comedyMovies else null,
                if (uiState.dramaMovies.isNotEmpty()) "Drama Movies" to uiState.dramaMovies else null,
                if (uiState.watchlistMovies.isNotEmpty()) "My Movie List" to uiState.watchlistMovies else null
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Dynamic Hero Banner - changes based on focused content
        val heroContent = remember(currentRowIndex, currentItemIndex, uiState) {
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
                    ?: uiState.trendingMovies.firstOrNull()
                    ?: uiState.popularMovies.firstOrNull()
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
                        // Show in-place details overlay and fetch full details
                        selectedContent = targetContent
                        isDetailsVisible = true
                        viewModel.onContentSelected(targetContent)
                        coroutineScope.launch {
                            val detailed = viewModel.fetchDetailedContent(targetContent)
                            selectedContent = detailed
                        }
                    },
                    onAddToWatchlist = { viewModel.addToWatchlist(targetContent) },
                    isInWatchlist = uiState.watchlistMovies.any { it.id == targetContent.id },
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
                        Log.d("MoviesScreen", "Initial focus requested for content rows (${contentRows.size} rows available)")
                    } catch (e: Exception) {
                        Log.e("MoviesScreen", "Failed to request initial focus", e)
                    }
                }
            }
            
            // Listen for external focus requests and ensure immediate focus
            LaunchedEffect(externalFocusRequester) {
                externalFocusRequester?.let {
                    Log.d("MoviesScreen", "External focus requester provided, requesting focus immediately")
                    delay(50) // Small delay to ensure the requester is ready
                    try {
                        it.requestFocus()
                        Log.d("MoviesScreen", "External focus requested successfully")
                    } catch (e: Exception) {
                        Log.e("MoviesScreen", "Failed to request external focus", e)
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
                            Log.d("MoviesScreen", "Key event: ${keyEvent.key}, currentRow=$currentRowIndex, currentItem=$currentItemIndex")
                            when (keyEvent.key) {
                                Key.DirectionUp -> {
                                    if (currentRowIndex > 0) {
                                        currentRowIndex--
                                        currentItemIndex = 0 // Reset to first item in new row
                                        Log.d("MoviesScreen", "Moved up to row $currentRowIndex")
                                        true
                                    } else {
                                        Log.d("MoviesScreen", "Already at top row")
                                        false
                                    }
                                }
                                Key.DirectionDown -> {
                                    if (currentRowIndex < contentRows.size - 1) {
                                        currentRowIndex++
                                        currentItemIndex = 0 // Reset to first item in new row
                                        Log.d("MoviesScreen", "Moved down to row $currentRowIndex")
                                        true
                                    } else {
                                        Log.d("MoviesScreen", "Already at bottom row")
                                        false
                                    }
                                }
                                Key.DirectionLeft -> {
                                    if (currentItemIndex > 0) {
                                        currentItemIndex--
                                        Log.d("MoviesScreen", "Moved left to item $currentItemIndex")
                                        true
                                    } else {
                                        Log.d("MoviesScreen", "At leftmost item, expanding drawer")
                                        onExpandDrawer?.invoke()
                                        true
                                    }
                                }
                                Key.DirectionRight -> {
                                    val currentRow = contentRows.getOrNull(currentRowIndex)
                                    val maxItems = currentRow?.second?.take(10)?.size ?: 0
                                    if (currentItemIndex < maxItems - 1) {
                                        currentItemIndex++
                                        Log.d("MoviesScreen", "Moved right to item $currentItemIndex")
                                        true
                                    } else {
                                        Log.d("MoviesScreen", "Already at rightmost item (max: $maxItems)")
                                        false
                                    }
                                }
                                Key.DirectionCenter, Key.Enter -> {
                                    // Handle click/select action manually
                                    val currentRow = contentRows.getOrNull(currentRowIndex)
                                    val currentRowContent = currentRow?.second?.take(10)
                                    if (currentRowContent != null && currentItemIndex < currentRowContent.size) {
                                        val clickedContent = currentRowContent[currentItemIndex]
                                        Log.d("MoviesScreen", "Selected content: ${clickedContent.title}")
                                        // Open inline details overlay
                                        viewModel.onContentSelected(clickedContent)
                                        selectedContent = clickedContent
                                        isDetailsVisible = true
                                        // Fetch details (credits, similar) immediately
                                        coroutineScope.launch {
                                            val detailed = viewModel.fetchDetailedContent(clickedContent)
                                            selectedContent = detailed
                                            Log.d("MoviesScreen", "Fetched details for overlay: cast=${detailed.cast.size}, similar=${detailed.similarContent.size}")
                                        }
                                    }
                                    true
                                }
                                else -> {
                                    Log.d("MoviesScreen", "Unhandled key: ${keyEvent.key}")
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
                        Log.d("MoviesScreen", "Rendering ContentRowOverlay: title='$title', rowIndex=$targetIndex, focusedItem=$currentItemIndex, contentSize=${content.size}")
                        ContentRowOverlay(
                            title = title,
                            content = content,
                            currentIndex = targetIndex,
                            totalRows = contentRows.size,
                            focusedItemIndex = currentItemIndex,
                            onContentClick = { content ->
                                // Open inline details overlay
                                Log.d("MoviesScreen", "onContentClick handler called with title='${content.title}', id=${content.id}")
                                viewModel.onContentSelected(content)
                                selectedContent = content
                                isDetailsVisible = true
                                
                                // Fetch detailed content with cast and similar data
                                coroutineScope.launch {
                                    val detailedContent = viewModel.fetchDetailedContent(content)
                                    selectedContent = detailedContent
                                    Log.d("MoviesScreen", "Detailed content fetched with ${detailedContent.cast.size} cast members and ${detailedContent.similarContent.size} similar items")
                                }
                                Log.d("MoviesScreen", "selectedContent updated to title='${selectedContent?.title}', id=${selectedContent?.id}")
                            },
                            onPlayClick = { content ->
                                viewModel.trackContentInteraction(content, InteractionType.WATCH)
                                onNavigateToPlayer(content)
                            },
                            onAddToWatchlist = viewModel::addToWatchlist,
                            onRemoveFromWatchlist = viewModel::removeFromWatchlist,
                            watchlist = uiState.watchlistMovies
                        )
                    }
                }
            }
        }
        
        // In-place Details Overlay - covers content rows area while keeping hero visible
        val details = selectedContent
        if (isDetailsVisible && details != null) {
            Log.d("MoviesScreen", "Rendering ContentDetailsOverlay for title='${details.title}', id=${details.id}")
            ContentDetailsOverlay(
                content = details,
                isInWatchlist = uiState.watchlistMovies.any { it.id == details.id },
                onPlay = {
                    viewModel.trackContentInteraction(details, InteractionType.WATCH)
                    onNavigateToPlayer(details)
                },
                onToggleWatchlist = {
                    if (uiState.watchlistMovies.any { it.id == details.id }) {
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
                        text = "Loading movies...",
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
                        text = "⚠️ Movies Loading Failed",
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
 * Identical to home screen implementation but for movies only.
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
    var isExpanded by remember { mutableStateOf(false) }
    
    // Request initial focus so D-pad works inside the panel
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(if (isExpanded) 400.dp else 260.dp)
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
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.Back, Key.Escape -> {
                                onDismiss()
                                true
                            }
                            Key.DirectionDown -> {
                                if (!isExpanded) {
                                    isExpanded = true
                                    true
                                } else false
                            }
                            Key.DirectionUp -> {
                                if (isExpanded) {
                                    isExpanded = false
                                    true
                                } else false
                            }
                            else -> false
                        }
                    } else false
                }
                .padding(horizontal = 48.dp, vertical = 16.dp)
        ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cast section
            Text(
                text = "Cast",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Cast tiles row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                val castList = content.cast
                items(castList.take(8)) { castMember ->
                    CastTile(castMember = castMember)
                }
            }
            
            // Movie/Show information
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Genres
                if (content.genres.isNotEmpty()) {
                    Text(
                        text = "Genres: ${content.genres.joinToString(", ")}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
                
                // Release date and runtime
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    content.releaseDate?.let { date ->
                        Text(
                            text = "Released: $date",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                    
                    content.runtime?.let { runtime ->
                        val hours = runtime / 60
                        val minutes = runtime % 60
                        val runtimeText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                        Text(
                            text = "Runtime: $runtimeText",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Rating
                if (content.voteAverage > 0) {
                    Text(
                        text = "Rating: ★ ${String.format("%.1f", content.voteAverage)}/10",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
            
            // More Like This section (shown when expanded)
            if (isExpanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "More Like This",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Similar content row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        val similarList = content.similarContent.take(6)
                        if (similarList.isNotEmpty()) {
                            items(similarList) { similarContent ->
                                AsyncImage(
                                    model = similarContent.posterUrl,
                                    contentDescription = similarContent.title,
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = "Press UP to collapse",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // Show expansion hint when not expanded
                Text(
                    text = "Press DOWN for more like this",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        }
    }
}
