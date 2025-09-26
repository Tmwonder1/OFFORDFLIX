package com.offordflix.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.offordflix.data.repository.VideoPlayerRepository
import com.offordflix.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * ViewModel for video player screen.
 * 
 * Manages ExoPlayer integration, playback state, and user interactions
 * for the Android TV video player experience.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val videoPlayerRepository: VideoPlayerRepository
) : ViewModel() {

    // Internal mutable state
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    
    // ExoPlayer instance (will be injected in real implementation)
    private var exoPlayer: ExoPlayer? = null
    
    // Current content being played
    private var currentContent: VideoContent? = null
    
    // Auto-hide controls timer
    private var controlsHideJob: kotlinx.coroutines.Job? = null
    
    init {
        // Start position tracking
        startPositionUpdates()
    }
    
    /**
     * Initialize player with content.
     */
    fun initializePlayer(player: ExoPlayer, content: VideoContent) {
        exoPlayer = player
        currentContent = content
        
        setupPlayerListeners()
        loadContent(content)
    }
    
    /**
     * Load video content into player.
     */
    private fun loadContent(content: VideoContent) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Generate streaming URL
                val streamingUrl = when (content.type) {
                    ContentType.MOVIE -> videoPlayerRepository.getMovieStreamingUrl(content.tmdbId)
                    ContentType.TV_SHOW -> videoPlayerRepository.getTvShowStreamingUrl(
                        content.tmdbId,
                        content.seasonNumber ?: 1,
                        content.episodeNumber ?: 1
                    )
                }
                
                // Validate URL
                if (!videoPlayerRepository.isValidStreamingUrl(streamingUrl)) {
                    throw Exception("Invalid streaming URL")
                }
                
                // Create media item and prepare player
                val mediaItem = MediaItem.fromUri(streamingUrl)
                exoPlayer?.apply {
                    setMediaItem(mediaItem)
                    prepare()
                }
                
                // Load additional metadata
                loadMetadata(content)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        content = content,
                        streamingUrl = streamingUrl
                    ) 
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to load content"
                    ) 
                }
            }
        }
    }
    
    /**
     * Load content metadata (subtitles, audio tracks, etc.).
     */
    private suspend fun loadMetadata(content: VideoContent) {
        try {
            val subtitles = videoPlayerRepository.getSubtitleTracks(content.id)
            val audioTracks = videoPlayerRepository.getAudioTracks(content.id)
            val qualities = videoPlayerRepository.getAvailableQualities(content.id)
            
            _uiState.update { 
                it.copy(
                    availableSubtitles = subtitles,
                    availableAudioTracks = audioTracks,
                    availableQualities = qualities
                ) 
            }
        } catch (e: Exception) {
            // Metadata loading failure shouldn't stop playback
        }
    }
    
    /**
     * Setup ExoPlayer event listeners.
     */
    private fun setupPlayerListeners() {
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                _uiState.update { 
                    it.copy(
                        isPlaying = exoPlayer?.isPlaying == true,
                        isLoading = playbackState == Player.STATE_BUFFERING
                    ) 
                }
            }
            
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _uiState.update { 
                    it.copy(
                        error = error.message ?: "Playback error occurred",
                        isLoading = false
                    ) 
                }
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                
                if (isPlaying) {
                    startControlsAutoHide()
                } else {
                    cancelControlsAutoHide()
                }
            }
        })
    }
    
    /**
     * Toggle play/pause state.
     */
    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
        showControlsTemporarily()
    }
    
    /**
     * Seek to specific position.
     */
    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        showControlsTemporarily()
    }
    
    /**
     * Seek forward by specified amount.
     */
    fun seekForward(amountMs: Long = 10000L) {
        val currentPosition = exoPlayer?.currentPosition ?: 0L
        val newPosition = (currentPosition + amountMs).coerceAtMost(
            exoPlayer?.duration ?: 0L
        )
        seekTo(newPosition)
    }
    
    /**
     * Seek backward by specified amount.
     */
    fun seekBackward(amountMs: Long = 10000L) {
        val currentPosition = exoPlayer?.currentPosition ?: 0L
        val newPosition = (currentPosition - amountMs).coerceAtLeast(0L)
        seekTo(newPosition)
    }
    
    /**
     * Set playback volume.
     */
    fun setVolume(volume: Float) {
        exoPlayer?.volume = volume.coerceIn(0f, 1f)
        _uiState.update { it.copy(volume = volume) }
        showControlsTemporarily()
    }
    
    /**
     * Set playback speed.
     */
    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
        _uiState.update { it.copy(playbackSpeed = speed) }
        showControlsTemporarily()
    }
    
    /**
     * Toggle subtitle visibility.
     */
    fun toggleSubtitles() {
        val currentState = _uiState.value.subtitlesEnabled
        _uiState.update { it.copy(subtitlesEnabled = !currentState) }
        
        // In real implementation, configure ExoPlayer subtitle tracks
        showControlsTemporarily()
    }
    
    /**
     * Select subtitle track.
     */
    fun selectSubtitleTrack(track: SubtitleTrack?) {
        _uiState.update { 
            it.copy(
                selectedSubtitleTrack = track,
                subtitlesEnabled = track != null
            ) 
        }
        
        // In real implementation, set ExoPlayer subtitle track
        showControlsTemporarily()
    }
    
    /**
     * Select audio track.
     */
    fun selectAudioTrack(track: AudioTrack) {
        _uiState.update { it.copy(selectedAudioTrack = track) }
        
        // In real implementation, set ExoPlayer audio track
        showControlsTemporarily()
    }
    
    /**
     * Select video quality.
     */
    fun selectQuality(quality: VideoQuality) {
        _uiState.update { it.copy(selectedQuality = quality) }
        
        // In real implementation, switch to different quality stream
        showControlsTemporarily()
    }
    
    /**
     * Show player controls.
     */
    fun showControls() {
        _uiState.update { it.copy(showControls = true) }
        startControlsAutoHide()
    }
    
    /**
     * Hide player controls.
     */
    fun hideControls() {
        _uiState.update { it.copy(showControls = false) }
        cancelControlsAutoHide()
    }
    
    /**
     * Toggle fullscreen mode (always fullscreen on TV).
     */
    fun toggleFullscreen() {
        // Android TV is always fullscreen
        _uiState.update { it.copy(isFullscreen = true) }
    }
    
    /**
     * Set error state.
     */
    fun setError(error: String) {
        _uiState.update { it.copy(error = error, isLoading = false) }
    }
    
    /**
     * Set loading state.
     */
    fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(isLoading = loading) }
    }
    
    /**
     * Clear error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Show controls temporarily and start auto-hide timer.
     */
    private fun showControlsTemporarily() {
        showControls()
    }
    
    /**
     * Start auto-hide timer for controls.
     */
    private fun startControlsAutoHide() {
        cancelControlsAutoHide()
        controlsHideJob = viewModelScope.launch {
            delay(5000) // Hide after 5 seconds
            hideControls()
        }
    }
    
    /**
     * Cancel auto-hide timer for controls.
     */
    private fun cancelControlsAutoHide() {
        controlsHideJob?.cancel()
        controlsHideJob = null
    }
    
    /**
     * Start periodic position updates.
     */
    private fun startPositionUpdates() {
        viewModelScope.launch {
            while (true) {
                delay(1000) // Update every second
                
                exoPlayer?.let { player ->
                    _uiState.update { 
                        it.copy(
                            currentPosition = player.currentPosition,
                            duration = player.duration,
                            bufferedPosition = player.bufferedPosition
                        ) 
                    }
                    
                    // Save progress periodically
                    saveProgress()
                }
            }
        }
    }
    
    /**
     * Save watch progress to repository.
     */
    private suspend fun saveProgress() {
        val currentState = _uiState.value
        val content = currentContent
        
        if (content != null && currentState.currentPosition > 0) {
            // Save every 30 seconds of playback
            if (currentState.currentPosition % 30000 < 1000) {
                videoPlayerRepository.saveWatchProgress(
                    profileId = "current-profile", // TODO: Get from profile manager
                    contentId = content.id,
                    position = currentState.currentPosition,
                    duration = currentState.duration
                )
            }
        }
    }
    
    /**
     * Release player resources.
     */
    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
        cancelControlsAutoHide()
    }
    
    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}

/**
 * UI state for player screen.
 */
data class PlayerUiState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val bufferedPosition: Long = 0L,
    val volume: Float = 1.0f,
    val playbackSpeed: Float = 1.0f,
    val isFullscreen: Boolean = true,
    val showControls: Boolean = false,
    val subtitlesEnabled: Boolean = false,
    val selectedSubtitleTrack: SubtitleTrack? = null,
    val selectedAudioTrack: AudioTrack? = null,
    val selectedQuality: VideoQuality = VideoQuality.AUTO,
    val error: String? = null,
    val content: VideoContent? = null,
    val streamingUrl: String? = null,
    val availableSubtitles: List<SubtitleTrack> = emptyList(),
    val availableAudioTracks: List<AudioTrack> = emptyList(),
    val availableQualities: List<VideoQuality> = emptyList()
)

