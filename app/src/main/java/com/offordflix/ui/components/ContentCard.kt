package com.offordflix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.offordflix.domain.model.VideoContent
import com.offordflix.domain.model.ContentType

/**
 * Content card component for displaying movies and TV shows.
 * 
 * Features:
 * - Poster image with loading states
 * - Content information overlay
 * - Focus-driven interactions for Android TV
 * - Quick action buttons (Play, Watchlist)
 * - Progress indicator for continue watching
 */
@Composable
fun ContentCard(
    content: VideoContent,
    onClick: () -> Unit,
    onPlayClick: () -> Unit = onClick,
    onWatchlistClick: () -> Unit = {},
    isInWatchlist: Boolean = false,
    showProgress: Boolean = false,
    progress: Float = 0f,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    var showOverlay by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    Card(
        modifier = modifier
            .width(200.dp)
            .height(300.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { 
                isFocused = it.isFocused
                showOverlay = it.isFocused
            }
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 12.dp else 4.dp
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Poster image
            AsyncImage(
                model = content.posterUrl,
                contentDescription = content.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Progress bar for continue watching
            if (showProgress && progress > 0) {
                LinearProgressIndicator(
                    progress = progress.coerceIn(0f, 1f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
            
            // Content type indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Badge(
                    modifier = Modifier.background(
                        Color.Black.copy(alpha = 0.7f),
                        RoundedCornerShape(4.dp)
                    )
                ) {
                    Text(
                        text = if (content.type == ContentType.MOVIE) "MOVIE" else "TV",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            // Watchlist indicator
            if (isInWatchlist) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Favorite,
                        contentDescription = "In Watchlist",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                Color.Black.copy(alpha = 0.7f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(4.dp)
                    )
                }
            }
            
            // Overlay with content info and actions (shown on focus)
            if (showOverlay) {
                ContentOverlay(
                    content = content,
                    onPlayClick = onPlayClick,
                    onWatchlistClick = onWatchlistClick,
                    isInWatchlist = isInWatchlist,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

/**
 * Content overlay with information and actions.
 */
@Composable
private fun ContentOverlay(
    content: VideoContent,
    onPlayClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    isInWatchlist: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.9f)
                    )
                )
            )
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            Text(
                text = content.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Rating
            if (content.voteAverage > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "⭐",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "%.1f".format(content.voteAverage),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Play button
                CompactButton(
                    text = "▶",
                    onClick = onPlayClick,
                    isPrimary = true
                )
                
                // Watchlist button
                CompactButton(
                    text = if (isInWatchlist) "✓" else "+",
                    onClick = onWatchlistClick,
                    isActive = isInWatchlist
                )
            }
        }
    }
}

/**
 * Compact action button for overlays.
 */
@Composable
private fun CompactButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Button(
        onClick = onClick,
        modifier = modifier
            .size(32.dp)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused },
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isPrimary -> MaterialTheme.colorScheme.primary
                isActive -> MaterialTheme.colorScheme.secondary
                isFocused -> Color.White.copy(alpha = 0.3f)
                else -> Color.Black.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

