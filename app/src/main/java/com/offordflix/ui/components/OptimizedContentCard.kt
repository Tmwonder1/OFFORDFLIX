package com.offordflix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.offordflix.domain.model.VideoContent
import androidx.compose.foundation.border

/**
 * Optimized content card component for better scrolling performance.
 * 
 * Key optimizations:
 * - Removed all animations
 * - Simplified focus states
 * - Reduced recomposition triggers
 * - Optimized image loading
 */
@Composable
fun OptimizedContentCard(
    content: VideoContent,
    onClick: () -> Unit,
    onPlayClick: () -> Unit = onClick,
    onWatchlistClick: () -> Unit = {},
    isInWatchlist: Boolean = false,
    showProgress: Boolean = false,
    progress: Float = 0f,
    modifier: Modifier = Modifier,
    isManuallyFocused: Boolean = false
) {
    val isFocused = isManuallyFocused
    val showOverlay = isManuallyFocused
    
    Card(
        modifier = modifier
            .width(90.dp)
            .height(135.dp)
            .clickable(
                onClickLabel = "Play ${content.title}"
            ) { 
                onClick() 
            }
            .then(
                if (isFocused) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 4.dp else 2.dp
        ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Poster image with optimized loading
            AsyncImage(
                model = content.posterUrl,
                contentDescription = content.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Progress bar for continue watching
            if (showProgress && progress > 0) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
            
            // Overlay with content info and actions (shown on focus)
            if (showOverlay) {
                OptimizedContentOverlay(
                    content = content,
                    onPlayClick = onPlayClick,
                    onWatchlistClick = onWatchlistClick,
                    isInWatchlist = isInWatchlist,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
        
        // Title display below card when focused (simplified)
        if (isFocused) {
            Box(
                modifier = Modifier
                    .offset(y = 190.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .widthIn(max = 200.dp)
            ) {
                Column {
                    Text(
                        text = content.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (content.voteAverage > 0) {
                        Text(
                            text = "★ ${String.format("%.1f", content.voteAverage)}",
                            color = Color.Yellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Optimized content overlay with simplified animations.
 */
@Composable
private fun OptimizedContentOverlay(
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
            
            // Action buttons (simplified)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Play button
                OptimizedCompactButton(
                    text = "▶",
                    onClick = onPlayClick,
                    isPrimary = true
                )
                
                // Watchlist button
                OptimizedCompactButton(
                    text = if (isInWatchlist) "✓" else "+",
                    onClick = onWatchlistClick,
                    isActive = isInWatchlist
                )
            }
        }
    }
}

/**
 * Optimized compact action button without focus animations.
 */
@Composable
private fun OptimizedCompactButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isPrimary -> MaterialTheme.colorScheme.primary
                isActive -> MaterialTheme.colorScheme.secondary
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
