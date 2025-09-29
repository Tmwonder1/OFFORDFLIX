package com.offordflix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A reusable left scrim gradient overlay component for hero/banner artwork in Android TV apps.
 * This ensures text and buttons on the left remain readable over busy background images.
 */
@Composable
fun LeftScrimOverlay(
    modifier: Modifier = Modifier,
    width: Dp = 400.dp,
    intensity: ScrimIntensity = ScrimIntensity.Medium
) {
    val gradient = when (intensity) {
        ScrimIntensity.Subtle -> Brush.horizontalGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.5f),
                Color.Black.copy(alpha = 0.25f),
                Color.Transparent
            )
        )
        ScrimIntensity.Medium -> Brush.horizontalGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.8f),
                Color.Black.copy(alpha = 0.4f),
                Color.Transparent
            )
        )
        ScrimIntensity.Strong -> Brush.horizontalGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.9f),
                Color.Black.copy(alpha = 0.6f),
                Color.Transparent
            )
        )
    }

    Box(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .background(gradient),
        contentAlignment = Alignment.CenterStart
    ) {
        // Content can be placed here if needed
    }
}

/**
 * A full-width version that covers the entire container with left-aligned scrim
 */
@Composable
fun FullWidthLeftScrimOverlay(
    modifier: Modifier = Modifier,
    intensity: ScrimIntensity = ScrimIntensity.Medium,
    scrimWidthRatio: Float = 0.6f // How much of the width should have the scrim effect
) {
    val (startAlpha, midAlpha) = when (intensity) {
        ScrimIntensity.Subtle -> 0.5f to 0.25f
        ScrimIntensity.Medium -> 0.8f to 0.4f
        ScrimIntensity.Strong -> 0.9f to 0.6f
    }
    
    val gradient = Brush.horizontalGradient(
        colorStops = arrayOf(
            0.0f to Color.Black.copy(alpha = startAlpha),
            scrimWidthRatio * 0.7f to Color.Black.copy(alpha = midAlpha),
            scrimWidthRatio to Color.Transparent,
            1.0f to Color.Transparent
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
    )
}

/**
 * Scrim intensity levels for different use cases
 */
enum class ScrimIntensity {
    Subtle,   // For less busy backgrounds
    Medium,   // Default, works for most cases
    Strong    // For very busy or bright backgrounds
}

/**
 * A composable that wraps content with a left scrim overlay
 */
@Composable
fun ScrimContainer(
    modifier: Modifier = Modifier,
    scrimIntensity: ScrimIntensity = ScrimIntensity.Medium,
    scrimWidth: Dp = 400.dp,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        // Background content (image, etc.)
        content()
        
        // Scrim overlay
        LeftScrimOverlay(
            width = scrimWidth,
            intensity = scrimIntensity,
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}
