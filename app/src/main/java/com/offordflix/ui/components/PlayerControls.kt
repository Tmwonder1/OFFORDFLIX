package com.offordflix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.offordflix.domain.model.*
import com.offordflix.ui.screens.player.PlayerUiState

/**
 * Advanced player controls for Android TV.
 * 
 * Includes subtitle selection, audio track selection,
 * quality selection, and playback speed controls.
 */
@Composable
fun AdvancedPlayerControls(
    uiState: PlayerUiState,
    onSubtitleSelected: (SubtitleTrack?) -> Unit,
    onAudioTrackSelected: (AudioTrack) -> Unit,
    onQualitySelected: (VideoQuality) -> Unit,
    onSpeedSelected: (Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.8f),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.95f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Player Settings",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Subtitle selection
                    item {
                        SettingsSection(
                            title = "Subtitles",
                            currentValue = uiState.selectedSubtitleTrack?.displayName ?: "Off"
                        ) {
                            SubtitleSelection(
                                subtitles = uiState.availableSubtitles,
                                selectedTrack = uiState.selectedSubtitleTrack,
                                onTrackSelected = onSubtitleSelected
                            )
                        }
                    }
                    
                    // Audio track selection
                    item {
                        SettingsSection(
                            title = "Audio",
                            currentValue = uiState.selectedAudioTrack?.displayName ?: "Default"
                        ) {
                            AudioTrackSelection(
                                audioTracks = uiState.availableAudioTracks,
                                selectedTrack = uiState.selectedAudioTrack,
                                onTrackSelected = onAudioTrackSelected
                            )
                        }
                    }
                    
                    // Quality selection
                    item {
                        SettingsSection(
                            title = "Quality",
                            currentValue = uiState.selectedQuality.displayName
                        ) {
                            QualitySelection(
                                qualities = uiState.availableQualities,
                                selectedQuality = uiState.selectedQuality,
                                onQualitySelected = onQualitySelected
                            )
                        }
                    }
                    
                    // Playback speed selection
                    item {
                        SettingsSection(
                            title = "Speed",
                            currentValue = "${uiState.playbackSpeed}x"
                        ) {
                            SpeedSelection(
                                currentSpeed = uiState.playbackSpeed,
                                onSpeedSelected = onSpeedSelected
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

/**
 * Settings section with expandable content.
 */
@Composable
private fun SettingsSection(
    title: String,
    currentValue: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .focusable()
                .onFocusChanged { isFocused = it.isFocused }
                .clickable { expanded = !expanded }
                .background(
                    if (isFocused) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                    RoundedCornerShape(8.dp)
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = currentValue,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
        }
        
        if (expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                )
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}

/**
 * Subtitle track selection.
 */
@Composable
private fun SubtitleSelection(
    subtitles: List<SubtitleTrack>,
    selectedTrack: SubtitleTrack?,
    onTrackSelected: (SubtitleTrack?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Off option
        SelectionItem(
            text = "Off",
            isSelected = selectedTrack == null,
            onClick = { onTrackSelected(null) }
        )
        
        // Available subtitle tracks
        subtitles.forEach { track ->
            SelectionItem(
                text = track.displayName,
                isSelected = selectedTrack == track,
                onClick = { onTrackSelected(track) }
            )
        }
    }
}

/**
 * Audio track selection.
 */
@Composable
private fun AudioTrackSelection(
    audioTracks: List<AudioTrack>,
    selectedTrack: AudioTrack?,
    onTrackSelected: (AudioTrack) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        audioTracks.forEach { track ->
            SelectionItem(
                text = track.displayName,
                isSelected = selectedTrack == track,
                onClick = { onTrackSelected(track) }
            )
        }
    }
}

/**
 * Quality selection.
 */
@Composable
private fun QualitySelection(
    qualities: List<VideoQuality>,
    selectedQuality: VideoQuality,
    onQualitySelected: (VideoQuality) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        qualities.forEach { quality ->
            SelectionItem(
                text = quality.displayName,
                isSelected = selectedQuality == quality,
                onClick = { onQualitySelected(quality) }
            )
        }
    }
}

/**
 * Playback speed selection.
 */
@Composable
private fun SpeedSelection(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit
) {
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        speeds.forEach { speed ->
            SelectionItem(
                text = "${speed}x",
                isSelected = currentSpeed == speed,
                onClick = { onSpeedSelected(speed) }
            )
        }
    }
}

/**
 * Individual selection item.
 */
@Composable
private fun SelectionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onClick() }
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    isFocused -> Color.White.copy(alpha = 0.1f)
                    else -> Color.Transparent
                },
                RoundedCornerShape(4.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            Text(
                text = "✓",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            Spacer(modifier = Modifier.width(24.dp))
        }
        
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

/**
 * Volume control slider.
 */
@Composable
fun VolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🔊",
            color = Color.White,
            fontSize = 20.sp
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            modifier = Modifier.width(200.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "${(volume * 100).toInt()}%",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

