package com.offordflix.ui.screens.home

// Main HomeScreen is now NetflixStyleHomeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
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
    backStackEntry: androidx.navigation.NavBackStackEntry? = null,
    viewModel: HomeViewModel = if (backStackEntry != null) {
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
    var pinnedHeroContent by remember { mutableStateOf<VideoContent?>(null) }
    
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
            Log.d("NetflixHome", "Using enhanced content with ${enhancedContent.sections.size} sections")
            // Convert enhanced content sections to content rows
            enhancedContent.sections.map { section ->
                section.title to section.data.map { it.toVideoContent() }
            }
        } else {
            Log.d("NetflixHome", "Using legacy content fallback")
            // Legacy content rows for fallback
            listOfNotNull(
                if (uiState.trendingContent.isNotEmpty()) "Trending Now" to uiState.trendingContent else null,
                if (uiState.popularMovies.isNotEmpty()) "Popular Movies" to uiState.popularMovies else null,
                if (uiState.popularTvShows.isNotEmpty()) "Popular TV Shows" to uiState.popularTvShows else null,
                if (uiState.topRatedMovies.isNotEmpty()) "Top Rated Movies" to uiState.topRatedMovies else null,
                if (uiState.topRatedTvShows.isNotEmpty()) "Top Rated TV Shows" to uiState.topRatedTvShows else null
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Dynamic Hero Banner - changes based on focused content
    val heroContent = remember(currentRowIndex, currentItemIndex, uiState, pinnedHeroContent) {
            pinnedHeroContent?.let { return@remember it }
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
                    ?: uiState.trendingContent.firstOrNull()
                    ?: uiState.popularMovies.firstOrNull()
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
                    isInWatchlist = uiState.watchlist.any { it.id == targetContent.id },
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
                        Log.d("HomeScreen", "Initial focus requested for content rows (${contentRows.size} rows available)")
                    } catch (e: Exception) {
                        Log.e("HomeScreen", "Failed to request initial focus", e)
                    }
                }
            }
            
            // Listen for external focus requests and ensure immediate focus
            LaunchedEffect(externalFocusRequester) {
                externalFocusRequester?.let {
                    Log.d("HomeScreen", "External focus requester provided, requesting focus immediately")
                    delay(50) // Small delay to ensure the requester is ready
                    try {
                        it.requestFocus()
                        Log.d("HomeScreen", "External focus requested successfully")
                    } catch (e: Exception) {
                        Log.e("HomeScreen", "Failed to request external focus", e)
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
                            Log.d("HomeScreen", "Key event: ${keyEvent.key}, currentRow=$currentRowIndex, currentItem=$currentItemIndex")
                            when (keyEvent.key) {
                                Key.DirectionUp -> {
                                    if (currentRowIndex > 0) {
                                        currentRowIndex--
                                        currentItemIndex = 0 // Reset to first item in new row
                                        Log.d("HomeScreen", "Moved up to row $currentRowIndex")
                                        true
                                    } else {
                                        Log.d("HomeScreen", "Already at top row")
                                        false
                                    }
                                }
                                Key.DirectionDown -> {
                                    if (currentRowIndex < contentRows.size - 1) {
                                        currentRowIndex++
                                        currentItemIndex = 0 // Reset to first item in new row
                                        Log.d("HomeScreen", "Moved down to row $currentRowIndex")
                                        true
                                    } else {
                                        Log.d("HomeScreen", "Already at bottom row")
                                        false
                                    }
                                }
                                Key.DirectionLeft -> {
                                    if (currentItemIndex > 0) {
                                        currentItemIndex--
                                        Log.d("HomeScreen", "Moved left to item $currentItemIndex")
                                        true
                                    } else {
                                        Log.d("HomeScreen", "At leftmost item, expanding drawer")
                                        onExpandDrawer?.invoke()
                                        true
                                    }
                                }
                                Key.DirectionRight -> {
                                    val currentRow = contentRows.getOrNull(currentRowIndex)
                                    val maxItems = currentRow?.second?.take(10)?.size ?: 0
                                    if (currentItemIndex < maxItems - 1) {
                                        currentItemIndex++
                                        Log.d("HomeScreen", "Moved right to item $currentItemIndex")
                                        true
                                    } else {
                                        Log.d("HomeScreen", "Already at rightmost item (max: $maxItems)")
                                        false
                                    }
                                }
                                Key.DirectionCenter, Key.Enter -> {
                                    // Handle click/select action manually
                                    val currentRow = contentRows.getOrNull(currentRowIndex)
                                    val currentRowContent = currentRow?.second?.take(10)
                                    if (currentRowContent != null && currentItemIndex < currentRowContent.size) {
                                        val clickedContent = currentRowContent[currentItemIndex]
                                        Log.d("HomeScreen", "Selected content: ${clickedContent.title}")
                                        // Open inline details overlay
                                        viewModel.onContentSelected(clickedContent)
                                        selectedContent = clickedContent
                                        isDetailsVisible = true
                                        // Fetch details (credits, similar) immediately
                                        coroutineScope.launch {
                                            val detailedContent = viewModel.fetchDetailedContent(clickedContent)
                                            selectedContent = detailedContent
                                        }
                                    }
                                    true
                                }
                                else -> {
                                    Log.d("HomeScreen", "Unhandled key: ${keyEvent.key}")
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
                        Log.d("NetflixHome", "Rendering ContentRowOverlay: title='$title', rowIndex=$targetIndex, focusedItem=$currentItemIndex, contentSize=${content.size}")
                        ContentRowOverlay(
                            title = title,
                            content = content,
                            currentIndex = targetIndex,
                            totalRows = contentRows.size,
                            focusedItemIndex = currentItemIndex,
                            onContentClick = { content ->
                                // Open inline details overlay
                                Log.d("NetflixHome", "onContentClick handler called with title='${content.title}', id=${content.id}")
                                viewModel.onContentSelected(content)
                                selectedContent = content
                                isDetailsVisible = true
                                
                                // Fetch detailed content with cast and similar data
                                coroutineScope.launch {
                                    val detailedContent = viewModel.fetchDetailedContent(content)
                                    selectedContent = detailedContent
                                    Log.d("NetflixHome", "Detailed content fetched with ${detailedContent.cast.size} cast members and ${detailedContent.similarContent.size} similar items")
                                }
                                Log.d("NetflixHome", "selectedContent updated to title='${selectedContent?.title}', id=${selectedContent?.id}")
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
        
        // In-place Details Overlay - covers content rows area while keeping hero visible
        val details = selectedContent
        if (isDetailsVisible && details != null) {
            Log.d("NetflixHome", "Rendering ContentDetailsOverlay for title='${details.title}', id=${details.id}")
            ContentDetailsOverlay(
                content = details,
                isInWatchlist = uiState.watchlist.any { it.id == details.id },
                hasResume = false,
                onPlay = {
                    viewModel.trackContentInteraction(details, InteractionType.WATCH)
                    onNavigateToPlayer(details)
                },
                onToggleWatchlist = {
                    if (uiState.watchlist.any { it.id == details.id }) {
                        viewModel.removeFromWatchlist(details)
                    } else {
                        viewModel.addToWatchlist(details)
                    }
                },
                onDismiss = {
                    isDetailsVisible = false
                    pinnedHeroContent = null
                },
                onSimilarClick = { similar ->
                    // Update hero selection and open overlay for the clicked similar item
                    viewModel.onContentSelected(similar)
                    selectedContent = similar
                    pinnedHeroContent = similar
                    isDetailsVisible = true
                    coroutineScope.launch {
                        val detailed = viewModel.fetchDetailedContent(similar)
                        selectedContent = detailed
                        pinnedHeroContent = detailed
                    }
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
 */
@Composable
private fun ContentDetailsOverlay(
    content: VideoContent,
    isInWatchlist: Boolean,
    hasResume: Boolean,
    onPlay: () -> Unit,
    onToggleWatchlist: () -> Unit,
    onDismiss: () -> Unit,
    onSimilarClick: (VideoContent) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var isExpanded by remember { mutableStateOf(false) }
    var similarFocusIndex by remember { mutableStateOf(0) }
    var selectedActionIndex by remember { mutableStateOf(0) }

    // Build actions list like the reference UI
    val actionLabels = remember(content.id, isInWatchlist, hasResume) {
        buildList {
            add(if (hasResume) "Resume" else "Play")
            add("Play from beginning")
            if (content.type == com.offordflix.domain.model.ContentType.TV_SHOW) add("Seasons")
            add("Remove from watch history")
            add(if (isInWatchlist) "Remove from My List" else "+ Add to My List")
        }
    }

    // Ensure collapsed by default whenever a new content is shown in the overlay
    LaunchedEffect(content.id) {
        isExpanded = false
        similarFocusIndex = 0
    }
    
    // Request initial focus so D-pad works inside the panel
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Action list under synopsis (approximate placement below synopsis)
        val listState = rememberLazyListState()
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 48.dp)
                .offset(y = 100.dp)
                .widthIn(max = 520.dp)
        ) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.heightIn(max = 180.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                itemsIndexed(actionLabels) { index, label ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (index == selectedActionIndex) Color.White.copy(alpha = 0.12f) else Color.Transparent)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            // Fade peeks top/bottom
            if (listState.canScrollBackward) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                            )
                        )
                )
            }
            if (listState.canScrollForward) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(if (isExpanded) 400.dp else 72.dp)
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
                                    if (selectedActionIndex < actionLabels.lastIndex) {
                                        selectedActionIndex++
                                        true
                                    } else {
                                        isExpanded = true
                                        similarFocusIndex = 0
                                        true
                                    }
                                } else false
                            }
                            Key.DirectionUp -> {
                                if (isExpanded) {
                                    isExpanded = false
                                    true
                                } else if (selectedActionIndex > 0) {
                                    selectedActionIndex--
                                    true
                                } else false
                            }
                            Key.DirectionLeft -> {
                                if (isExpanded) {
                                    similarFocusIndex = (similarFocusIndex - 1).coerceAtLeast(0)
                                    true
                                } else false
                            }
                            Key.DirectionRight -> {
                                if (isExpanded) {
                                    val max = content.similarContent.take(10).size
                                    if (max > 0) {
                                        similarFocusIndex = (similarFocusIndex + 1).coerceAtMost(max - 1)
                                    }
                                    true
                                } else false
                            }
                            Key.DirectionCenter, Key.Enter -> {
                                if (isExpanded) {
                                    val list = content.similarContent.take(10)
                                    if (list.isNotEmpty()) {
                                        val selected = list[similarFocusIndex]
                                        onSimilarClick(selected)
                                        true
                                    } else false
                                } else {
                                    when (actionLabels[selectedActionIndex]) {
                                        "Resume" -> { onPlay(); true }
                                        "Play from beginning" -> { onPlay(); true }
                                        "Seasons" -> { /* TODO: navigate to seasons */ true }
                                        "Remove from watch history" -> { /* TODO: hook to history removal */ true }
                                        "+ Add to My List", "Remove from My List" -> { onToggleWatchlist(); true }
                                        else -> false
                                    }
                                }
                            }
                            else -> false
                        }
                    } else false
                }
                .padding(horizontal = 48.dp, vertical = 16.dp)
        ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isExpanded) {
                Text(
                    text = "Cast",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Press DOWN to expand",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            } else {
            Text(
                text = "Cast",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                    val castList = content.cast
                items(castList.take(8)) { castMember ->
                    CastTile(castMember = castMember)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "More Like This",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                val similarList = content.similarContent.take(10)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                    items(similarList.size) { index ->
                        val similarContent = similarList[index]
                        ContentCard(
                            content = similarContent,
                            onClick = { onSimilarClick(similarContent) },
                            onPlayClick = { onSimilarClick(similarContent) },
                            onWatchlistClick = {},
                            isInWatchlist = false,
                            isManuallyFocused = index == similarFocusIndex
                        )
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
        }
        }
    }
}
