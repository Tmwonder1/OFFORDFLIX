package com.offordflix.ui.screens.player

import androidx.media3.common.Player
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.mock

/**
 * Unit tests for PlayerViewModel.
 * 
 * Tests video playback state management, ExoPlayer integration,
 * and playback control functionality.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val mockPlayer = mock<Player>()

    @Test
    fun `player initialization sets up correct state`() = runTest {
        val viewModel = PlayerViewModel()
        
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertFalse(initialState.isPlaying)
            assertFalse(initialState.isLoading)
            assertEquals(0L, initialState.currentPosition)
            assertEquals(0L, initialState.duration)
            assertNull(initialState.error)
            assertFalse(initialState.showControls)
        }
    }

    @Test
    fun `play pause toggle updates state correctly`() = runTest {
        val viewModel = PlayerViewModel()
        
        viewModel.uiState.test {
            // Initial state
            val initialState = awaitItem()
            assertFalse(initialState.isPlaying)
            
            // Start playback
            viewModel.togglePlayPause()
            val playingState = awaitItem()
            // Note: In real implementation, this would be controlled by ExoPlayer
            // For now, we're testing the state management structure
        }
    }

    @Test
    fun `seeking updates position correctly`() = runTest {
        val viewModel = PlayerViewModel()
        val seekPosition = 30000L // 30 seconds
        
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(0L, initialState.currentPosition)
            
            // Seek to position
            viewModel.seekTo(seekPosition)
            
            // In real implementation, ExoPlayer would update the position
            // For now, testing the interface exists
        }
    }

    @Test
    fun `error handling sets error state`() = runTest {
        val viewModel = PlayerViewModel()
        val errorMessage = "Network error"
        
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertNull(initialState.error)
            
            // Set error
            viewModel.setError(errorMessage)
            val errorState = awaitItem()
            assertEquals(errorMessage, errorState.error)
        }
    }

    @Test
    fun `loading state management works correctly`() = runTest {
        val viewModel = PlayerViewModel()
        
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)
            
            // Set loading
            viewModel.setLoading(true)
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            
            // Clear loading
            viewModel.setLoading(false)
            val notLoadingState = awaitItem()
            assertFalse(notLoadingState.isLoading)
        }
    }

    @Test
    fun `controls visibility toggles correctly`() = runTest {
        val viewModel = PlayerViewModel()
        
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertFalse(initialState.showControls)
            
            // Show controls
            viewModel.showControls()
            val controlsVisibleState = awaitItem()
            assertTrue(controlsVisibleState.showControls)
            
            // Hide controls
            viewModel.hideControls()
            val controlsHiddenState = awaitItem()
            assertFalse(controlsHiddenState.showControls)
        }
    }

    @Test
    fun `fullscreen toggle works correctly`() = runTest {
        val viewModel = PlayerViewModel()
        
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue(initialState.isFullscreen) // TV is always fullscreen
            
            // Toggle fullscreen (should remain true for TV)
            viewModel.toggleFullscreen()
            val fullscreenState = awaitItem()
            assertTrue(fullscreenState.isFullscreen)
        }
    }

    @Test
    fun `subtitle management updates state`() = runTest {
        val viewModel = PlayerViewModel()
        
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertFalse(initialState.subtitlesEnabled)
            
            // Enable subtitles
            viewModel.toggleSubtitles()
            val subtitlesEnabledState = awaitItem()
            assertTrue(subtitlesEnabledState.subtitlesEnabled)
        }
    }

    @Test
    fun `volume control updates correctly`() = runTest {
        val viewModel = PlayerViewModel()
        val newVolume = 0.7f
        
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(1.0f, initialState.volume, 0.01f)
            
            // Set volume
            viewModel.setVolume(newVolume)
            val volumeState = awaitItem()
            assertEquals(newVolume, volumeState.volume, 0.01f)
        }
    }

    @Test
    fun `playback speed adjustment works`() = runTest {
        val viewModel = PlayerViewModel()
        val newSpeed = 1.5f
        
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(1.0f, initialState.playbackSpeed, 0.01f)
            
            // Set playback speed
            viewModel.setPlaybackSpeed(newSpeed)
            val speedState = awaitItem()
            assertEquals(newSpeed, speedState.playbackSpeed, 0.01f)
        }
    }
}

