package com.offordflix.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.offordflix.domain.model.VideoContent
import com.offordflix.ui.components.ContentCard
import com.offordflix.ui.components.FullWidthLeftScrimOverlay
import com.offordflix.ui.components.ScrimIntensity
import com.offordflix.ui.components.CastTile
import com.offordflix.ui.theme.SynopsisFontFamily
import com.offordflix.ui.utils.ContentMetadataUtils

/**
 * Search screen with virtual keyboard and content grid.
 * 
 * Features:
 * - Virtual keyboard layout for Android TV
 * - Text search with real-time results
 * - Grid layout for search results
 * - D-pad navigation optimized
 */
@Composable
fun SearchScreen(
    onNavigateToPlayer: (VideoContent) -> Unit,
    onNavigateToDetails: (VideoContent) -> Unit = {},
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
    isDrawerExpanded: Boolean = false,
    onExpandDrawer: (() -> Unit)? = null,
    onOverlayVisibilityChanged: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    
    // Local UI state for details overlay
    var selectedContent by remember { mutableStateOf<VideoContent?>(null) }
    var isDetailsVisible by remember { mutableStateOf(false) }
    
    // Notify navigation about overlay visibility changes
    LaunchedEffect(isDetailsVisible) {
        onOverlayVisibilityChanged(isDetailsVisible)
    }
    
    // Create a gradient background similar to the image
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1a1a2e),
            Color(0xFF16213e),
            Color(0xFF0f3460)
        )
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
                .padding(
                    start = if (isDrawerExpanded) 352.dp else 32.dp, // Add padding when drawer is expanded (320dp drawer + 32dp original padding)
                    top = 32.dp,
                    end = 32.dp,
                    bottom = 32.dp
                )
        ) {
            // Left side - Virtual Keyboard
    Column(
        modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
    ) {
                // Search input field
                SearchInputField(
            query = uiState.query,
            onQueryChange = viewModel::updateQuery,
                    onSearch = viewModel::search,
                    modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
                // Virtual keyboard
                VirtualKeyboard(
                    onKeyPress = { key ->
                        when (key) {
                            "⌫" -> {
                                if (uiState.query.isNotEmpty()) {
                                    viewModel.updateQuery(uiState.query.dropLast(1))
                                }
                            }
                            "Search" -> {
                                viewModel.search()
                            }
                            else -> {
                                viewModel.updateQuery(uiState.query + key)
                            }
                        }
                    },
                    onExpandDrawer = onExpandDrawer
                )
            }
            
            Spacer(modifier = Modifier.width(32.dp))
            
            // Right side - Search Results
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
            ) {
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
                    uiState.searchResults.isEmpty() && uiState.query.isNotBlank() -> {
                NoResultsState(query = uiState.query)
            }
            else -> {
                SearchResults(
                    results = uiState.filteredResults,
                            onContentClick = { content ->
                                selectedContent = content
                                isDetailsVisible = true
                                // Fetch full details (credits, similar) asynchronously
                                coroutineScope.launch {
                                    val detailed = viewModel.fetchDetailedContent(content)
                                    selectedContent = detailed
                                }
                            },
                    onPlayClick = onNavigateToPlayer,
                    onAddToWatchlist = viewModel::addToWatchlist,
                    onRemoveFromWatchlist = viewModel::removeFromWatchlist,
                    watchlist = uiState.watchlist
                )
            }
        }
    }
}

        // Details Overlay - Full screen without any padding
        val details = selectedContent
        if (isDetailsVisible && details != null) {
            ContentDetailsOverlay(
                content = details,
                isInWatchlist = uiState.watchlist.any { it.id == details.id },
                onPlay = {
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
                    selectedContent = null
                }
            )
        }
    }
}

/**
 * Search input field with modern styling.
 */
@Composable
private fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    Box(
        modifier = modifier
            .height(56.dp)
            .background(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = if (isFocused) Color(0xFF00D4AA) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused },
        textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
        ),
                cursorBrush = SolidColor(Color(0xFF00D4AA)),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() }
        ),
        decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                    Text(
                                text = "Search",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                    )
                }
                innerTextField()
            }
        }
    )
        }
    }
}

/**
 * Virtual keyboard component with alphabet and number keys.
 */
@Composable
private fun VirtualKeyboard(
    onKeyPress: (String) -> Unit,
    onExpandDrawer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val keyboardRows = listOf(
        listOf("a", "b", "c", "d", "e", "f"),
        listOf("g", "h", "i", "j", "k", "l"),
        listOf("m", "n", "o", "p", "q", "r"),
        listOf("s", "t", "u", "v", "w", "x"),
        listOf("y", "z", "1", "2", "3", "4"),
        listOf("5", "6", "7", "8", "9", "0"),
        listOf("#+", "Search")
    )
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keyboardRows.forEachIndexed { rowIndex, row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEachIndexed { keyIndex, key ->
                    VirtualKeyButton(
                        key = key,
                        onClick = { onKeyPress(key) },
                        onExpandDrawer = if (keyIndex == 0) onExpandDrawer else null, // Only leftmost keys can expand drawer
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Add backspace button to the last row
                if (row == keyboardRows.last()) {
                    VirtualKeyButton(
                        key = "⌫",
                        onClick = { onKeyPress("⌫") },
                        modifier = Modifier.weight(1f),
                        isSpecial = true
                    )
                }
            }
        }
    }
}

/**
 * Individual virtual keyboard button.
 */
@Composable
private fun VirtualKeyButton(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSpecial: Boolean = false,
    onExpandDrawer: (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isFocused -> Color(0xFF00D4AA)
                    isSpecial -> Color.White.copy(alpha = 0.15f)
                    key == "Search" -> Color(0xFF00D4AA)
                    else -> Color.White.copy(alpha = 0.1f)
                }
            )
            .border(
                width = 1.dp,
                color = if (isFocused) Color(0xFF00D4AA) else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.Enter -> {
                            onClick()
                            true
                        }
                        Key.DirectionLeft -> {
                            // Only expand drawer if this is a leftmost key and has onExpandDrawer callback
                            if (onExpandDrawer != null) {
                                onExpandDrawer()
                                true
                            } else {
                                false
                            }
                        }
                        else -> false
                    }
                } else {
                    false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = key.uppercase(),
            color = Color.White,
            fontSize = if (key.length > 1) 12.sp else 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
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
        columns = GridCells.Fixed(4), // Fixed 4 columns to match the image layout
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
                color = Color(0xFF00D4AA),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Searching...",
            fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
        )
        }
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Search failed",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B6B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00D4AA)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Retry",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Empty search state (no query entered).
 */
@Composable
private fun EmptySearchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
    Column(
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
                color = Color.White,
                fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
                text = "Use the virtual keyboard to start typing",
            fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
        )
        }
    }
}

/**
 * No results found state.
 */
@Composable
private fun NoResultsState(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
    Column(
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
                color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No results for \"$query\"",
            fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try a different search term or check your spelling",
            fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Content details overlay shown over the search results.
 * Includes full hero backdrop image like the home screen.
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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
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
    ) {
        // Bottom area for cast tiles and expandable content only (keep underlying screen intact)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(if (isExpanded) 300.dp else 180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(horizontal = 48.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cast section
                Text(
                    text = "Cast",
                    color = Color.White,
                    fontSize = 18.sp,
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
                
                // More Like This section (shown when expanded)
                if (isExpanded) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "More Like This",
                            color = Color.White,
                            fontSize = 16.sp,
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
                                            .width(50.dp)
                                            .height(75.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = "Press UP to collapse",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // Show expansion hint when not expanded
                    Text(
                        text = "Press DOWN for more like this",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}