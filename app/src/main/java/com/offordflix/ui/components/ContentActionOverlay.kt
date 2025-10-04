package com.offordflix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offordflix.domain.model.VideoContent
import com.offordflix.domain.model.ContentType

/**
 * Gradient scrim overlay that appears under movie/show synopsis with action buttons.
 * Shows a horizontal list of actions: Play, Play from Beginning, Seasons, Add to List, Remove from Continue Watch
 */
@Composable
fun ContentActionOverlay(
    content: VideoContent,
    isInWatchlist: Boolean = false,
    isInContinueWatching: Boolean = false,
    onPlay: () -> Unit,
    onPlayFromBeginning: () -> Unit,
    onSeasonsClick: () -> Unit,
    onToggleWatchlist: () -> Unit,
    onRemoveFromContinueWatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.7f),
                        Color.Black.copy(alpha = 0.9f)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play button
            ActionButton(
                icon = Icons.Filled.PlayArrow,
                text = "Play",
                onClick = onPlay
            )
            
            // Play from beginning button
            ActionButton(
                icon = Icons.Filled.Refresh,
                text = "From Start",
                onClick = onPlayFromBeginning
            )
            
            // Seasons button (only show for TV shows)
            if (content.type == ContentType.TV_SHOW) {
                ActionButton(
                    icon = Icons.Filled.List,
                    text = "Seasons",
                    onClick = onSeasonsClick
                )
            }
            
            // Add to/Remove from watchlist button
            ActionButton(
                icon = if (isInWatchlist) Icons.Filled.Check else Icons.Filled.Add,
                text = if (isInWatchlist) "In List" else "My List",
                onClick = onToggleWatchlist
            )
            
            // Remove from continue watching (only show if in continue watching)
            if (isInContinueWatching) {
                ActionButton(
                    icon = Icons.Filled.Close,
                    text = "Remove",
                    onClick = onRemoveFromContinueWatch
                )
            }
        }
    }
}

/**
 * Individual action button with icon and text
 */
@Composable
private fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .clickable { onClick() }
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .background(
                color = if (isFocused) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
