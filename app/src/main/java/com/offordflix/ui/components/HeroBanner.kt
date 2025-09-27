package com.offordflix.ui.components

import androidx.compose.foundation.background
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
 * Netflix-style hero banner component.
 * 
 * Features:
 * - Full-width backdrop image with gradient overlay
 * - Content information (title, overview, rating)
 * - Action buttons (Play, More Info, Watchlist)
 * - Android TV D-pad navigation optimized
 * - Smooth focus transitions and animations
 */
@Composable
fun HeroBanner(
    content: VideoContent,
    onPlay: () -> Unit,
    onMoreInfo: () -> Unit,
    onAddToWatchlist: () -> Unit,
    isInWatchlist: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(600.dp)
    ) {
        // Backdrop image
        AsyncImage(
            model = content.backdropUrl,
            contentDescription = content.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.8f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f)
                        ),
                        startX = 0f,
                        endX = 1000f
                    )
                )
        )
        
        // Content information - positioned higher within the hero area
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(48.dp)
                .offset(y = (-100).dp)
                .fillMaxWidth(0.5f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = content.title,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Type and rating
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Content type badge
                Badge(
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(4.dp)
                    )
                ) {
                    Text(
                        text = if (content.type == ContentType.MOVIE) "MOVIE" else "TV SHOW",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                // Rating
                if (content.voteAverage > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "⭐",
                            fontSize = 16.sp
                        )
                        Text(
                            text = "%.1f".format(content.voteAverage),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Overview
            Text(
                text = content.overview ?: "No description available.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 18.sp,
                lineHeight = 24.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Play button (primary focus)
                HeroBannerButton(
                    text = "▶ Play",
                    onClick = onPlay,
                    isPrimary = true,
                    modifier = Modifier.focusable()
                )
                
                // More info button
                HeroBannerButton(
                    text = "ℹ More Info",
                    onClick = onMoreInfo,
                    modifier = Modifier.focusable()
                )
                
                // Watchlist button
                HeroBannerButton(
                    text = if (isInWatchlist) "✓ In List" else "+ My List",
                    onClick = onAddToWatchlist,
                    isActive = isInWatchlist,
                    modifier = Modifier.focusable()
                )
            }
        }
    }
}

/**
 * Hero banner action button.
 */
@Composable
private fun HeroBannerButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    Button(
        onClick = onClick,
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused },
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isPrimary -> MaterialTheme.colorScheme.primary
                isActive -> MaterialTheme.colorScheme.secondary
                isFocused -> Color.White.copy(alpha = 0.2f)
                else -> Color.Black.copy(alpha = 0.6f)
            }
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isFocused) 8.dp else 4.dp
        )
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

