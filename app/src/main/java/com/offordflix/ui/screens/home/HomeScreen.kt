package com.offordflix.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offordflix.domain.model.VideoContent
import com.offordflix.ui.components.ContentCard
import com.offordflix.ui.components.HeroBanner
import com.offordflix.ui.components.RecommendedForYouRow
import com.offordflix.ui.components.BecauseYouWatchedRow
import com.offordflix.ui.components.MoreLikeGenreRow
import com.offordflix.ui.components.PersonalizedTrendingRow
import com.offordflix.ui.components.ContextualRecommendationRow
import com.offordflix.data.ml.InteractionType

/**
 * Enhanced home screen with Netflix-style content discovery.
 * 
 * Features:
 * - Hero banner with featured content
 * - Horizontal scrollable content rows
 * - Popular movies, TV shows, trending content
 * - Profile-based content filtering
 * - Smooth D-pad navigation for Android TV
 */
@Composable
fun HomeScreen(
    onNavigateToPlayer: (VideoContent) -> Unit,
    onNavigateToDetails: (VideoContent) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadContent()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.isLoading && uiState.featuredContent == null) {
            // Loading state
            LoadingScreen()
        } else {
            // Content loaded
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Hero banner
                item {
                    uiState.featuredContent?.let { content ->
                        HeroBanner(
                            content = content,
                            onPlay = { onNavigateToPlayer(content) },
                            onMoreInfo = { onNavigateToDetails(content) },
                            onAddToWatchlist = { viewModel.addToWatchlist(content) },
                            isInWatchlist = uiState.watchlist.any { it.id == content.id }
                        )
                    }
                }
                
                // Content rows
                item {
                    ContentDiscoverySection(
                        uiState = uiState,
                        onContentClick = { content ->
                            viewModel.onContentSelected(content)
                            onNavigateToDetails(content)
                        },
                        onPlayClick = { content ->
                            viewModel.trackContentInteraction(content, InteractionType.WATCH)
                            onNavigateToPlayer(content)
                        },
                        onAddToWatchlist = viewModel::addToWatchlist,
                        onRemoveFromWatchlist = viewModel::removeFromWatchlist
                    )
                }
            }
        }
        
        // Error state
        uiState.error?.let { error ->
            ErrorMessage(
                error = error,
                onRetry = { viewModel.loadContent() },
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

/**
 * Content discovery section with multiple rows.
 */
@Composable
private fun ContentDiscoverySection(
    uiState: HomeUiState,
    onContentClick: (VideoContent) -> Unit,
    onPlayClick: (VideoContent) -> Unit,
    onAddToWatchlist: (VideoContent) -> Unit,
    onRemoveFromWatchlist: (VideoContent) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        // Continue Watching (if available)
        if (uiState.continueWatching.isNotEmpty()) {
            ContentRow(
                title = "Continue Watching",
                content = uiState.continueWatching,
                onContentClick = onContentClick,
                onPlayClick = onPlayClick,
                onAddToWatchlist = onAddToWatchlist,
                onRemoveFromWatchlist = onRemoveFromWatchlist,
                watchlist = uiState.watchlist,
                showProgress = true
            )
        }
        
        // ML-Powered: Recommended for You
        if (uiState.recommendedForYou.isNotEmpty()) {
            RecommendedForYouRow(
                content = uiState.recommendedForYou,
                onContentClick = onContentClick,
                onPlayClick = onPlayClick,
                onAddToWatchlist = onAddToWatchlist,
                onRemoveFromWatchlist = onRemoveFromWatchlist,
                watchlist = uiState.watchlist
            )
        }
        
        // ML-Powered: Because You Watched
        if (uiState.becauseYouWatchedContent != null && uiState.becauseYouWatchedRecommendations.isNotEmpty()) {
            BecauseYouWatchedRow(
                title = "Because You Watched ${uiState.becauseYouWatchedContent.title}",
                content = uiState.becauseYouWatchedRecommendations,
                onContentClick = onContentClick,
                onPlayClick = onPlayClick,
                onAddToWatchlist = onAddToWatchlist,
                onRemoveFromWatchlist = onRemoveFromWatchlist,
                watchlist = uiState.watchlist
            )
        }
        
        // ML-Powered: Contextual Recommendations
        if (uiState.contextualRecommendations.isNotEmpty() && uiState.viewingContext != null) {
            val (contextTitle, contextSubtitle) = when (uiState.viewingContext) {
                com.offordflix.data.repository.ViewingContext.MORNING -> 
                    "Good Morning Picks" to "Light and uplifting content to start your day"
                com.offordflix.data.repository.ViewingContext.AFTERNOON -> 
                    "Afternoon Entertainment" to "Perfect for your lunch break or downtime"
                com.offordflix.data.repository.ViewingContext.EVENING -> 
                    "Prime Time Favorites" to "Popular picks for your evening viewing"
                com.offordflix.data.repository.ViewingContext.LATE_NIGHT -> 
                    "Late Night Thrills" to "Intense content for night owls"
                com.offordflix.data.repository.ViewingContext.WEEKEND -> 
                    "Weekend Binge" to "Perfect for longer weekend viewing sessions"
                else -> "Smart Picks" to "Curated just for you"
            }
            
            ContextualRecommendationRow(
                title = contextTitle,
                subtitle = contextSubtitle,
                content = uiState.contextualRecommendations,
                onContentClick = onContentClick,
                onPlayClick = onPlayClick,
                onAddToWatchlist = onAddToWatchlist,
                onRemoveFromWatchlist = onRemoveFromWatchlist,
                watchlist = uiState.watchlist
            )
        }
        
        // My List / Watchlist
        if (uiState.watchlist.isNotEmpty()) {
            ContentRow(
                title = "My List",
                content = uiState.watchlist,
                onContentClick = onContentClick,
                onPlayClick = onPlayClick,
                onAddToWatchlist = onAddToWatchlist,
                onRemoveFromWatchlist = onRemoveFromWatchlist,
                watchlist = uiState.watchlist
            )
        }
        
        // ML-Powered: Personalized Trending
        if (uiState.personalizedTrending.isNotEmpty()) {
            PersonalizedTrendingRow(
                content = uiState.personalizedTrending,
                onContentClick = onContentClick,
                onPlayClick = onPlayClick,
                onAddToWatchlist = onAddToWatchlist,
                onRemoveFromWatchlist = onRemoveFromWatchlist,
                watchlist = uiState.watchlist
            )
        } else {
            // Fallback to regular trending
            ContentRow(
                title = "Trending Now",
                content = uiState.trendingContent,
                onContentClick = onContentClick,
                onPlayClick = onPlayClick,
                onAddToWatchlist = onAddToWatchlist,
                onRemoveFromWatchlist = onRemoveFromWatchlist,
                watchlist = uiState.watchlist
            )
        }
        
        // ML-Powered: More Like Your Favorite Genre
        if (uiState.favoriteGenreRecommendations.isNotEmpty() && uiState.favoriteGenre != null) {
            MoreLikeGenreRow(
                genre = uiState.favoriteGenre,
                content = uiState.favoriteGenreRecommendations,
                onContentClick = onContentClick,
                onPlayClick = onPlayClick,
                onAddToWatchlist = onAddToWatchlist,
                onRemoveFromWatchlist = onRemoveFromWatchlist,
                watchlist = uiState.watchlist
            )
        }
        
        // Popular Movies
        ContentRow(
            title = "Popular Movies",
            content = uiState.popularMovies,
            onContentClick = onContentClick,
            onPlayClick = onPlayClick,
            onAddToWatchlist = onAddToWatchlist,
            onRemoveFromWatchlist = onRemoveFromWatchlist,
            watchlist = uiState.watchlist
        )
        
        // Popular TV Shows
        ContentRow(
            title = "Popular TV Shows",
            content = uiState.popularTvShows,
            onContentClick = onContentClick,
            onPlayClick = onPlayClick,
            onAddToWatchlist = onAddToWatchlist,
            onRemoveFromWatchlist = onRemoveFromWatchlist,
            watchlist = uiState.watchlist
        )
        
        // Top Rated Movies
        ContentRow(
            title = "Top Rated Movies",
            content = uiState.topRatedMovies,
            onContentClick = onContentClick,
            onPlayClick = onPlayClick,
            onAddToWatchlist = onAddToWatchlist,
            onRemoveFromWatchlist = onRemoveFromWatchlist,
            watchlist = uiState.watchlist
        )
        
        // Top Rated TV Shows
        ContentRow(
            title = "Top Rated TV Shows",
            content = uiState.topRatedTvShows,
            onContentClick = onContentClick,
            onPlayClick = onPlayClick,
            onAddToWatchlist = onAddToWatchlist,
            onRemoveFromWatchlist = onRemoveFromWatchlist,
            watchlist = uiState.watchlist
        )
    }
}

/**
 * Individual content row with horizontal scrolling.
 */
@Composable
private fun ContentRow(
    title: String,
    content: List<VideoContent>,
    onContentClick: (VideoContent) -> Unit,
    onPlayClick: (VideoContent) -> Unit,
    onAddToWatchlist: (VideoContent) -> Unit,
    onRemoveFromWatchlist: (VideoContent) -> Unit,
    watchlist: List<VideoContent>,
    showProgress: Boolean = false
) {
    if (content.isEmpty()) return
    
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        // Row title
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp)
        )
        
        // Horizontal content list
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 48.dp)
        ) {
            items(content) { videoContent ->
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
                    showProgress = showProgress
                )
            }
        }
    }
}

/**
 * Loading screen for initial content load.
 */
@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading content...",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Error message with retry option.
 */
@Composable
private fun ErrorMessage(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Something went wrong",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = error,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
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
}

/**
 * Fallback HomeScreen for backward compatibility.
 */
@Composable
fun HomeScreen(
    onNavigateToPlayer: (String) -> Unit
) {
    // Convert to new signature
    HomeScreen(
        onNavigateToPlayer = { content ->
            onNavigateToPlayer(content.id)
        }
    )
}