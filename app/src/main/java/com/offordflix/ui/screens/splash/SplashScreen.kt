package com.offordflix.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Enhanced splash screen with smooth animations for Android TV.
 * 
 * Features:
 * - Gradient background optimized for TV viewing
 * - Smooth fade-in and scale animations
 * - Proper timing for 10-foot UI experience
 * - Automatic navigation to profile selection
 */
@Composable
fun SplashScreen(
    onNavigateToProfileSelection: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    // Animation states
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "logoAlpha"
    )
    
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(
            durationMillis = 1200,
            delayMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "logoScale"
    )
    
    val taglineAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 0.8f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "taglineAlpha"
    )
    
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500) // Total splash duration: 2.5 seconds
        onNavigateToProfileSelection()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0A), // Very dark at top
                        Color(0xFF1A1A1A), // Slightly lighter at bottom
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main logo text with animation
            Text(
                text = "OFFORDFLIX",
                fontSize = 56.sp, // Larger for TV viewing
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 6.sp,
                modifier = Modifier
                    .alpha(logoAlpha)
                    .scale(logoScale)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tagline with delayed animation
            Text(
                text = "Streaming Reimagined",
                fontSize = 20.sp, // Slightly larger for TV
                fontWeight = FontWeight.Light,
                color = Color.White,
                letterSpacing = 3.sp,
                modifier = Modifier.alpha(taglineAlpha)
            )
        }
        
        // Optional: Add a subtle loading indicator at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        ) {
            LoadingIndicator(
                modifier = Modifier.alpha(logoAlpha * 0.6f)
            )
        }
    }
}

/**
 * Simple loading indicator for splash screen.
 */
@Composable
private fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loadingAlpha"
    )
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = alpha * (1f - index * 0.2f)
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
}
