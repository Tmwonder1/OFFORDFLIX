package com.offordflix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.core.*
import com.offordflix.domain.model.VideoContent
import com.offordflix.domain.model.ContentType
import com.offordflix.ui.theme.SynopsisFontFamily
import com.offordflix.ui.utils.ContentMetadataUtils

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
    onPlay: () -> Unit = {},
    onMoreInfo: () -> Unit = {},
    onAddToWatchlist: () -> Unit = {},
    isInWatchlist: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(600.dp)
    ) {
        // Enhanced backdrop image with progressive loading
        EnhancedBackdropImage(
            model = content.backdropUrl ?: content.posterUrl,
            contentDescription = content.title,
            modifier = Modifier.fillMaxSize()
        )
        
        // Left scrim overlay for text readability
        FullWidthLeftScrimOverlay(
            intensity = ScrimIntensity.Strong,
            scrimWidthRatio = 0.65f,
            modifier = Modifier.fillMaxSize()
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
            // Title - Use logo if available, otherwise text
            var logoLoadError by remember { mutableStateOf(false) }
            
            if (content.logoUrl != null && !logoLoadError) {
                EnhancedLogoImage(
                    model = content.logoUrl,
                    contentDescription = content.title,
                    modifier = Modifier
                        .height(80.dp)
                        .widthIn(max = 400.dp),
                    onError = {
                        logoLoadError = true
                    }
                )
            } else {
                Text(
                    text = content.title,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // All metadata in single continuous line with bullet separators
            val metadata = ContentMetadataUtils.getFormattedMetadata(content)
            if (metadata.isNotEmpty()) {
                Text(
                    text = ContentMetadataUtils.joinMetadata(metadata),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Overview
            Text(
                text = content.overview ?: "No description available.",
                color = Color.White,
                fontFamily = SynopsisFontFamily,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            // Buttons removed as per user request
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

/**
 * Enhanced backdrop image with shimmer loading and fade-in animation.
 */
@Composable
private fun EnhancedBackdropImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val painter = rememberAsyncImagePainter(model = model)
    val painterState = painter.state
    
    // Netflix-style shimmer animation for backdrop loading
    val netflixShimmerColors = listOf(
        Color(0xFF0A0A0A), // Darker Netflix base for backdrop
        Color(0xFF141414), // Netflix standard base
        Color(0xFF1F1F1F), // Mid tone
        Color(0xFF2A2A2A), // Highlight
        Color(0xFF1F1F1F), // Mid tone
        Color(0xFF141414), // Netflix standard base
        Color(0xFF0A0A0A)  // Darker base
    )
    
    val transition = rememberInfiniteTransition(label = "netflix_backdrop_shimmer")
    val shimmerOffset by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000, // Slower for backdrop
                easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f) // Netflix easing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "netflix_backdrop_shimmer_offset"
    )
    
    // Fade-in animation for loaded image
    val alpha by animateFloatAsState(
        targetValue = if (painterState is AsyncImagePainter.State.Success) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = EaseInOutCubic
        ),
        label = "backdrop_fade_in"
    )
    
    val shimmerBrush = Brush.linearGradient(
        colors = netflixShimmerColors,
        start = Offset(shimmerOffset - 600f, shimmerOffset - 600f),
        end = Offset(shimmerOffset + 600f, shimmerOffset + 600f)
    )
    
    Box(modifier = modifier) {
        // Show shimmer while loading
        if (painterState is AsyncImagePainter.State.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(shimmerBrush)
            )
        }
        
        // Show the actual backdrop with fade-in using the same painter we observe
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = alpha },
            contentScale = ContentScale.Crop
        )
        
        // Enhanced error state with fallback gradient
        if (painterState is AsyncImagePainter.State.Error) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1a1a1a),
                                Color(0xFF0d1117),
                                Color.Black
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Backdrop unavailable",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "Backdrop Image Unavailable",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * Enhanced logo image with shimmer loading and smooth transitions.
 */
@Composable
private fun EnhancedLogoImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onError: () -> Unit = {}
) {
    val painter = rememberAsyncImagePainter(
        model = model
    )
    val painterState = painter.state
    LaunchedEffect(painterState) {
        if (painterState is AsyncImagePainter.State.Error) {
            onError()
        }
    }
    
    // Shimmer animation for loading state
    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.1f),
        Color.White.copy(alpha = 0.3f),
        Color.White.copy(alpha = 0.1f)
    )
    
    val transition = rememberInfiniteTransition(label = "logo_shimmer")
    val shimmerOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "logo_shimmer_offset"
    )
    
    // Scale animation for loaded logo
    val scale by animateFloatAsState(
        targetValue = if (painterState is AsyncImagePainter.State.Success) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )
    
    // Alpha animation for loaded logo
    val alpha by animateFloatAsState(
        targetValue = if (painterState is AsyncImagePainter.State.Success) 1f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = EaseInOutCubic
        ),
        label = "logo_alpha"
    )
    
    val shimmerBrush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(shimmerOffset - 200f, shimmerOffset - 200f),
        end = Offset(shimmerOffset, shimmerOffset)
    )
    
    Box(modifier = modifier) {
        // Show shimmer while loading
        if (painterState is AsyncImagePainter.State.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        shimmerBrush,
                        RoundedCornerShape(8.dp)
                    )
            )
        }
        
        // Show the actual logo with animations using the same painter we observe
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.alpha = alpha
                    scaleX = scale
                    scaleY = scale
                },
            contentScale = ContentScale.Fit
        )
    }
}

