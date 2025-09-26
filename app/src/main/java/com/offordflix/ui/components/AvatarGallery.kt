package com.offordflix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Avatar gallery component for profile creation and editing.
 * 
 * Features:
 * - Grid layout optimized for Android TV D-pad navigation
 * - Separate sections for adult and kids avatars
 * - Focus indicators and smooth transitions
 * - Accessibility support with proper focus management
 */
@Composable
fun AvatarGallery(
    selectedAvatarId: Int,
    isKidsProfile: Boolean,
    onAvatarSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val availableAvatars = if (isKidsProfile) {
        (13..24).toList() // Kids avatars
    } else {
        (1..12).toList() // Adult avatars
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section title
        Text(
            text = if (isKidsProfile) "Choose a Kids Avatar" else "Choose Your Avatar",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Avatar grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(6), // 6 avatars per row for TV
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(200.dp) // Fixed height for dialog
        ) {
            items(availableAvatars) { avatarId ->
                AvatarSelectionCard(
                    avatarId = avatarId,
                    isSelected = avatarId == selectedAvatarId,
                    isKidsAvatar = isKidsProfile,
                    onClick = { onAvatarSelected(avatarId) }
                )
            }
        }
        
        // Helper text
        Text(
            text = if (isKidsProfile) {
                "Choose a fun avatar for your child's profile"
            } else {
                "This avatar will represent you on Offordflix"
            },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Individual avatar selection card with focus behavior.
 */
@Composable
private fun AvatarSelectionCard(
    avatarId: Int,
    isSelected: Boolean,
    isKidsAvatar: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    Card(
        modifier = modifier
            .size(64.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = when {
                isSelected -> 8.dp
                isFocused -> 6.dp
                else -> 2.dp
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                isFocused -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AvatarImage(
                avatarId = avatarId,
                name = getAvatarName(avatarId, isKidsAvatar),
                isSelected = isSelected || isFocused,
                isKidsAvatar = isKidsAvatar,
                size = AvatarSize.Small
            )
            
            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(8.dp)
                        )
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

/**
 * Compact avatar gallery for inline use (e.g., in dialogs).
 */
@Composable
fun CompactAvatarGallery(
    selectedAvatarId: Int,
    isKidsProfile: Boolean,
    onAvatarSelected: (Int) -> Unit,
    maxVisible: Int = 6,
    modifier: Modifier = Modifier
) {
    val availableAvatars = if (isKidsProfile) {
        (13..24).toList().take(maxVisible)
    } else {
        (1..12).toList().take(maxVisible)
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Avatar:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableAvatars.forEach { avatarId ->
                AvatarImage(
                    avatarId = avatarId,
                    name = getAvatarName(avatarId, isKidsProfile),
                    isSelected = avatarId == selectedAvatarId,
                    isKidsAvatar = isKidsProfile,
                    size = AvatarSize.Small,
                    modifier = Modifier
                        .clickable { onAvatarSelected(avatarId) }
                        .padding(4.dp)
                )
            }
        }
        
        if (availableAvatars.size < (if (isKidsProfile) 12 else 12)) {
            Text(
                text = "Tap 'More Avatars' to see all options",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

