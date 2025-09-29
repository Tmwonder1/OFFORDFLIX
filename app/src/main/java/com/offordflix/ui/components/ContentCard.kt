package com.offordflix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import com.offordflix.domain.model.ContentType
import android.util.Log

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
    modifier: Modifier = Modifier,
    initiallyRequestFocus: Boolean = false,
    isManuallyFocused: Boolean = false
) {
    // Use manual focus state instead of Android's focus system
    val isFocused = isManuallyFocused
    val showOverlay = isManuallyFocused
    
    // Focus is now working with manual state management
    
    // Disable scaling completely to prevent any overlap
    val scale by animateFloatAsState(
        targetValue = 1.0f, // No scaling at all
        animationSpec = tween(200, easing = EaseOutCubic),
        label = "card_scale"
    )
    
    // Simple focus state - no pulsing
    
    // Subtle glow colors
    val glowAmbientColor = Color.White.copy(alpha = 0.3f)
    val glowSpotColor = Color.White.copy(alpha = 0.5f)
    
    
    Card(
        modifier = modifier
            .width(90.dp)
            .height(135.dp)
            .clickable(
                onClickLabel = "Play ${content.title}"
            ) { 
                onClick() 
            }
            .graphicsLayer {
                if (isFocused) {
                    shadowElevation = 12f
                    shape = RoundedCornerShape(8.dp)
                    clip = false
                    ambientShadowColor = glowAmbientColor
                    spotShadowColor = glowSpotColor
                } else {
                    shadowElevation = 0f
                }
            }
            .then(
                if (isFocused) {
                    Modifier
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 8.dp else 2.dp
        ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
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
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
            
            // Clean poster display - no overlays when not focused
            
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
        
        // Prominent title display below card when focused
        if (isFocused) {
            Box(
                modifier = Modifier
                    .offset(y = 190.dp) // Position below the card
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

