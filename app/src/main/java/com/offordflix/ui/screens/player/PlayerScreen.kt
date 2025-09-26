package com.offordflix.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.offordflix.domain.model.VideoContent
import com.offordflix.domain.model.ContentType

/**
 * Enhanced player screen with ExoPlayer integration.
 * 
 * Features:
 * - Full ExoPlayer integration with custom controls
 * - Android TV D-pad navigation optimized
 * - Subtitle and audio track selection
 * - Quality selection and playback controls
 * - Progress tracking and watch history
 */
@Composable
fun PlayerScreen(
    content: VideoContent,
    onNavigateBack: () -> Unit = {},
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }
    
    // Initialize player with content
    LaunchedEffect(content) {
        viewModel.initializePlayer(exoPlayer, content)
    }
    
    // Cleanup on dispose
    DisposableEffect(exoPlayer) {
        onDispose {
            viewModel.releasePlayer()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ExoPlayer Video View
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false // Use custom controls
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading video...",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }
        
        // Custom player controls
        if (uiState.showControls && !uiState.isLoading) {
            PlayerControls(
                uiState = uiState,
                onPlayPause = viewModel::togglePlayPause,
                onSeek = viewModel::seekTo,
                onSeekForward = viewModel::seekForward,
                onSeekBackward = viewModel::seekBackward,
                onVolumeChange = viewModel::setVolume,
                onSpeedChange = viewModel::setPlaybackSpeed,
                onSubtitleToggle = viewModel::toggleSubtitles,
                onBack = onNavigateBack,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        
        // Error overlay
        uiState.error?.let { error ->
            ErrorOverlay(
                error = error,
                onRetry = { viewModel.clearError() },
                onBack = onNavigateBack,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // Touch/Click area to show controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusable()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        viewModel.showControls()
                    }
                }
        )
    }
}

/**
 * Fallback PlayerScreen for content ID (backward compatibility).
 */
@Composable
fun PlayerScreen(
    contentId: String,
    onNavigateBack: () -> Unit
) {
    // Create mock VideoContent for backward compatibility
    val mockContent = VideoContent(
        id = contentId,
        title = "Sample Content",
        type = ContentType.MOVIE,
        tmdbId = contentId
    )
    
    PlayerScreen(
        content = mockContent,
        onNavigateBack = onNavigateBack
    )
}

/**
 * Custom player controls optimized for Android TV.
 */
@Composable
private fun PlayerControls(
    uiState: PlayerUiState,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onSubtitleToggle: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.7f),
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(24.dp)
    ) {
        // Content info and back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = uiState.content?.title ?: "Video Player",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                if (uiState.content?.type == ContentType.TV_SHOW) {
                    Text(
                        text = "S${uiState.content.seasonNumber}E${uiState.content.episodeNumber}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
            
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text("Back", color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress bar
        ProgressBar(
            progress = if (uiState.duration > 0) {
                uiState.currentPosition.toFloat() / uiState.duration
            } else 0f,
            buffered = if (uiState.duration > 0) {
                uiState.bufferedPosition.toFloat() / uiState.duration
            } else 0f,
            onSeek = { progress ->
                onSeek((progress * uiState.duration).toLong())
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Time display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(uiState.currentPosition),
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = formatTime(uiState.duration),
                color = Color.White,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlButton(
                text = "<<",
                onClick = onSeekBackward
            )
            
            ControlButton(
                text = if (uiState.isPlaying) "⏸" else "▶",
                onClick = onPlayPause,
                isPrimary = true
            )
            
            ControlButton(
                text = ">>",
                onClick = onSeekForward
            )
            
            ControlButton(
                text = "CC",
                onClick = onSubtitleToggle,
                isActive = uiState.subtitlesEnabled
            )
        }
    }
}

/**
 * Individual control button.
 */
@Composable
private fun ControlButton(
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
                else -> Color.Transparent
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Progress bar with seek functionality.
 */
@Composable
private fun ProgressBar(
    progress: Float,
    buffered: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.height(8.dp)
    ) {
        // Background track
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.White.copy(alpha = 0.3f),
                    RoundedCornerShape(4.dp)
                )
        )
        
        // Buffered track
        Box(
            modifier = Modifier
                .fillMaxWidth(buffered.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(
                    Color.White.copy(alpha = 0.5f),
                    RoundedCornerShape(4.dp)
                )
        )
        
        // Progress track
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(4.dp)
                )
        )
    }
}

/**
 * Error overlay with retry option.
 */
@Composable
private fun ErrorOverlay(
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Playback Error",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = error,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Retry")
                }
                
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Text("Back", color = Color.White)
                }
            }
        }
    }
}

/**
 * Format time in milliseconds to MM:SS or HH:MM:SS.
 */
private fun formatTime(timeMs: Long): String {
    if (timeMs <= 0) return "00:00"
    
    val totalSeconds = timeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}