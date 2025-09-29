package com.offordflix.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * SCRIM OVERLAY USAGE EXAMPLES
 * 
 * This file demonstrates various ways to use the ScrimOverlay components
 * for Android TV apps to ensure text readability over busy images.
 */

/**
 * Example 1: Hero Banner with Full-Width Left Scrim
 * 
 * Use this for main hero banners where you want maximum text readability
 * on the left side while preserving the full image visibility on the right.
 */
@Composable
fun HeroBannerWithScrimExample(
    imageUrl: String,
    title: String,
    description: String,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(600.dp)
    ) {
        // Background image
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Full-width left scrim overlay
        FullWidthLeftScrimOverlay(
            intensity = ScrimIntensity.Strong,
            scrimWidthRatio = 0.6f,
            modifier = Modifier.fillMaxSize()
        )
        
        // Content positioned on the left
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(48.dp)
                .fillMaxWidth(0.5f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = description,
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Button(
                onClick = onPlayClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("▶ Play", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Example 2: Content Card with Fixed-Width Left Scrim
 * 
 * Use this for smaller content cards where you want a specific scrim width
 * to ensure text readability without covering too much of the image.
 */
@Composable
fun ContentCardWithScrimExample(
    imageUrl: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(400.dp)
            .height(225.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Fixed-width left scrim
            LeftScrimOverlay(
                width = 200.dp,
                intensity = ScrimIntensity.Medium,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            
            // Content positioned on the left
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .width(180.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Example 3: Using ScrimContainer for Automatic Layout
 * 
 * Use ScrimContainer when you want the scrim to be automatically applied
 * to any content you place inside it.
 */
@Composable
fun ScrimContainerExample(
    imageUrl: String,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    ScrimContainer(
        modifier = modifier.size(600.dp, 400.dp),
        scrimIntensity = ScrimIntensity.Medium,
        scrimWidth = 300.dp
    ) {
        // Background image
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Your content will automatically be overlaid with scrim protection
        // Note: Content positioning should be handled by the caller
    }
}

/**
 * Example 4: Different Scrim Intensities
 * 
 * Demonstrates when to use different scrim intensities based on your content needs.
 */
@Composable
fun ScrimIntensityExamples(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Subtle - for less busy backgrounds or when you want minimal overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            FullWidthLeftScrimOverlay(
                intensity = ScrimIntensity.Subtle,
                scrimWidthRatio = 0.5f
            )
            
            Text(
                text = "Subtle Scrim - Good for calm backgrounds",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(16.dp)
            )
        }
        
        // Medium - default, works for most cases
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            FullWidthLeftScrimOverlay(
                intensity = ScrimIntensity.Medium,
                scrimWidthRatio = 0.5f
            )
            
            Text(
                text = "Medium Scrim - Default choice for most content",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(16.dp)
            )
        }
        
        // Strong - for very busy or bright backgrounds
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            FullWidthLeftScrimOverlay(
                intensity = ScrimIntensity.Strong,
                scrimWidthRatio = 0.5f
            )
            
            Text(
                text = "Strong Scrim - For very busy or bright images",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(16.dp)
            )
        }
    }
}

/**
 * USAGE GUIDELINES:
 * 
 * 1. Choose the right component:
 *    - FullWidthLeftScrimOverlay: For hero banners and full-width components
 *    - LeftScrimOverlay: For cards and components with specific width needs
 *    - ScrimContainer: For automatic scrim application with any content
 * 
 * 2. Select appropriate intensity:
 *    - Subtle: Calm, dark, or low-contrast backgrounds
 *    - Medium: Most general use cases (default recommendation)
 *    - Strong: Bright, busy, or high-contrast backgrounds
 * 
 * 3. Adjust scrim width/ratio:
 *    - Hero banners: 0.6-0.7 ratio (60-70% of width)
 *    - Content cards: Fixed width (150-300dp typically)
 *    - Consider your text content width when setting scrim dimensions
 * 
 * 4. XML drawable alternatives:
 *    - scrim_gradient_left.xml: Medium intensity
 *    - scrim_gradient_left_strong.xml: Strong intensity  
 *    - scrim_gradient_left_subtle.xml: Subtle intensity
 */
