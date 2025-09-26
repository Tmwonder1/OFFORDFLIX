package com.offordflix.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Avatar image component for user profiles.
 * 
 * Displays avatar based on ID with fallback to colored circles
 * with initials. Optimized for Android TV focus and selection.
 */
@Composable
fun AvatarImage(
    avatarId: Int,
    name: String,
    isSelected: Boolean = false,
    isKidsAvatar: Boolean = false,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.Medium
) {
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    
    val backgroundColor = getAvatarColor(avatarId, isKidsAvatar)
    val initial = name.firstOrNull()?.uppercase() ?: "?"
    
    val context = LocalContext.current
    val avatarResource = getAvatarResource(avatarId)
    
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(
                width = if (isSelected) 4.dp else 2.dp,
                color = borderColor,
                shape = CircleShape
            )
            .semantics {
                contentDescription = "Avatar for $name"
            },
        contentAlignment = Alignment.Center
    ) {
        if (avatarResource != null) {
            // Use vector drawable avatar
            Image(
                painter = painterResource(id = avatarResource),
                contentDescription = getAvatarName(avatarId, isKidsAvatar),
                modifier = Modifier.size(size.dp)
            )
        } else {
            // Fallback to colored circle with initials
            Box(
                modifier = Modifier
                    .size(size.dp)
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontSize = (size.dp.value * 0.4).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Avatar sizes for different use cases.
 */
enum class AvatarSize(val dp: Int) {
    Small(48),
    Medium(80),
    Large(120),
    ExtraLarge(160)
}

/**
 * Get color for avatar based on ID and type.
 */
fun getAvatarColor(avatarId: Int, isKidsAvatar: Boolean): Color {
    val colors = if (isKidsAvatar) {
        // Brighter, more playful colors for kids
        listOf(
            Color(0xFF4CAF50), // Green
            Color(0xFF2196F3), // Blue
            Color(0xFFFF9800), // Orange
            Color(0xFFE91E63), // Pink
            Color(0xFF9C27B0), // Purple
            Color(0xFFFFEB3B), // Yellow
            Color(0xFF00BCD4), // Cyan
            Color(0xFFFF5722), // Deep Orange
            Color(0xFF8BC34A), // Light Green
            Color(0xFF3F51B5), // Indigo
            Color(0xFFFF4081), // Pink Accent
            Color(0xFF00E676)  // Green Accent
        )
    } else {
        // More sophisticated colors for adults
        listOf(
            Color(0xFF1976D2), // Blue
            Color(0xFF388E3C), // Green
            Color(0xFF7B1FA2), // Purple
            Color(0xFFD32F2F), // Red
            Color(0xFF303F9F), // Indigo
            Color(0xFF512DA8), // Deep Purple
            Color(0xFF1976D2), // Blue
            Color(0xFF0097A7), // Cyan
            Color(0xFF5D4037), // Brown
            Color(0xFF424242), // Grey
            Color(0xFF455A64), // Blue Grey
            Color(0xFF37474F)  // Dark Blue Grey
        )
    }
    
    return colors[(avatarId - 1) % colors.size]
}

/**
 * Get avatar name for accessibility and display.
 */
fun getAvatarName(avatarId: Int, isKidsAvatar: Boolean): String {
    return if (isKidsAvatar) {
        "Kids Avatar $avatarId"
    } else {
        "Avatar $avatarId"
    }
}

/**
 * Get drawable resource ID for avatar.
 */
fun getAvatarResource(avatarId: Int): Int? {
    return when (avatarId) {
        1 -> com.offordflix.R.drawable.avatar_1
        2 -> com.offordflix.R.drawable.avatar_2
        3 -> com.offordflix.R.drawable.avatar_3
        4 -> com.offordflix.R.drawable.avatar_4
        5 -> com.offordflix.R.drawable.avatar_5
        6 -> com.offordflix.R.drawable.avatar_6
        7 -> com.offordflix.R.drawable.avatar_7
        8 -> com.offordflix.R.drawable.avatar_8
        9 -> com.offordflix.R.drawable.avatar_9
        10 -> com.offordflix.R.drawable.avatar_10
        11 -> com.offordflix.R.drawable.avatar_11
        12 -> com.offordflix.R.drawable.avatar_12
        13 -> com.offordflix.R.drawable.avatar_13
        14 -> com.offordflix.R.drawable.avatar_14
        15 -> com.offordflix.R.drawable.avatar_15
        16 -> com.offordflix.R.drawable.avatar_16
        17 -> com.offordflix.R.drawable.avatar_17
        18 -> com.offordflix.R.drawable.avatar_18
        19 -> com.offordflix.R.drawable.avatar_19
        20 -> com.offordflix.R.drawable.avatar_20
        21 -> com.offordflix.R.drawable.avatar_21
        22 -> com.offordflix.R.drawable.avatar_22
        23 -> com.offordflix.R.drawable.avatar_23
        24 -> com.offordflix.R.drawable.avatar_24
        else -> null
    }
}
