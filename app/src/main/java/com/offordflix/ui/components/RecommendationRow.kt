package com.offordflix.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.itemsIndexed as tvItemsIndexed
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offordflix.domain.model.VideoContent

/**
 * Specialized recommendation row components for ML-powered suggestions.
 * 
 * Features different row types for various recommendation algorithms
 * with appropriate titles and context indicators.
 */

/**
 * "Recommended for You" row with personalization indicator.
 */
@Composable
fun RecommendedForYouRow(
    content: List<VideoContent>,
    onContentClick: (VideoContent) -> Unit,
    onPlayClick: (VideoContent) -> Unit,
    onAddToWatchlist: (VideoContent) -> Unit,
    onRemoveFromWatchlist: (VideoContent) -> Unit,
    watchlist: List<VideoContent>,
    modifier: Modifier = Modifier
) {
    if (content.isEmpty()) return
    
    Column(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        // Row title with personalization indicator
        Row(
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🎯 Recommended for You",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Badge(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "AI Powered",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Content row (TV-optimized)
        val listState = rememberTvLazyListState()
        TvLazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 48.dp)
        ) {
            tvItemsIndexed(content) { index, videoContent ->
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
                    initiallyRequestFocus = index == 0
                )
            }
        }
    }
}

/**
 * "Because You Watched X" row with base content reference.
 */
@Composable
fun BecauseYouWatchedRow(
    title: String, // e.g., "Because You Watched The Matrix"
    content: List<VideoContent>,
    onContentClick: (VideoContent) -> Unit,
    onPlayClick: (VideoContent) -> Unit,
    onAddToWatchlist: (VideoContent) -> Unit,
    onRemoveFromWatchlist: (VideoContent) -> Unit,
    watchlist: List<VideoContent>,
    modifier: Modifier = Modifier
) {
    if (content.isEmpty()) return
    
    Column(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        // Row title with similarity indicator
        Row(
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🔄 $title",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Badge(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "Similar",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        
        // Content row (TV-optimized)
        val listState = rememberTvLazyListState()
        TvLazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 48.dp)
        ) {
            tvItemsIndexed(content) { index, videoContent ->
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
                    initiallyRequestFocus = index == 0
                )
            }
        }
    }
}

/**
 * Genre-based recommendation row.
 */
@Composable
fun MoreLikeGenreRow(
    genre: String,
    content: List<VideoContent>,
    onContentClick: (VideoContent) -> Unit,
    onPlayClick: (VideoContent) -> Unit,
    onAddToWatchlist: (VideoContent) -> Unit,
    onRemoveFromWatchlist: (VideoContent) -> Unit,
    watchlist: List<VideoContent>,
    modifier: Modifier = Modifier
) {
    if (content.isEmpty()) return
    
    Column(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        // Row title with genre indicator
        Row(
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🎬 More $genre for You",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Badge(
                containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "Your Taste",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        
        // Content row (TV-optimized)
        val listState = rememberTvLazyListState()
        TvLazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 48.dp)
        ) {
            tvItemsIndexed(content) { index, videoContent ->
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
                    initiallyRequestFocus = index == 0
                )
            }
        }
    }
}

/**
 * Trending with personal boost row.
 */
@Composable
fun PersonalizedTrendingRow(
    content: List<VideoContent>,
    onContentClick: (VideoContent) -> Unit,
    onPlayClick: (VideoContent) -> Unit,
    onAddToWatchlist: (VideoContent) -> Unit,
    onRemoveFromWatchlist: (VideoContent) -> Unit,
    watchlist: List<VideoContent>,
    modifier: Modifier = Modifier
) {
    if (content.isEmpty()) return
    
    Column(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        // Row title with trending indicator
        Row(
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🔥 Trending for You",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Badge(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "Hot",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Content row (TV-optimized)
        val listState = rememberTvLazyListState()
        TvLazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 48.dp)
        ) {
            tvItemsIndexed(content) { index, videoContent ->
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
                    initiallyRequestFocus = index == 0
                )
            }
        }
    }
}

/**
 * Contextual recommendations row (time-based).
 */
@Composable
fun ContextualRecommendationRow(
    title: String,
    subtitle: String,
    content: List<VideoContent>,
    onContentClick: (VideoContent) -> Unit,
    onPlayClick: (VideoContent) -> Unit,
    onAddToWatchlist: (VideoContent) -> Unit,
    onRemoveFromWatchlist: (VideoContent) -> Unit,
    watchlist: List<VideoContent>,
    modifier: Modifier = Modifier
) {
    if (content.isEmpty()) return
    
    Column(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        // Row title with context
        Column(
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⏰ $title",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Badge(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Smart",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        
        // Content row
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
                    isInWatchlist = watchlist.any { it.id == videoContent.id }
                )
            }
        }
    }
}

