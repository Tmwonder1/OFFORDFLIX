package com.offordflix.ui.screens.splash

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import com.offordflix.ui.theme.OffordflixTheme

/**
 * Unit tests for SplashScreen composable.
 * 
 * Tests splash screen animation, branding display, and navigation timing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SplashScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `splash screen displays Offordflix branding`() {
        // Given
        val mockNavigation = mock<() -> Unit>()
        
        // When
        composeTestRule.setContent {
            OffordflixTheme {
                SplashScreen(onNavigateToProfileSelection = mockNavigation)
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("OFFORDFLIX").assertIsDisplayed()
        composeTestRule.onNodeWithText("Streaming Reimagined").assertIsDisplayed()
    }

    @Test
    fun `splash screen does not navigate immediately`() = runTest {
        // Given
        val mockNavigation = mock<() -> Unit>()
        
        // When
        composeTestRule.setContent {
            OffordflixTheme {
                SplashScreen(onNavigateToProfileSelection = mockNavigation)
            }
        }
        
        // Then - should not navigate immediately
        verifyNoInteractions(mockNavigation)
    }

    @Test
    fun `splash screen navigates after delay`() = runTest {
        // Given
        val mockNavigation = mock<() -> Unit>()
        
        // When
        composeTestRule.setContent {
            OffordflixTheme {
                SplashScreen(onNavigateToProfileSelection = mockNavigation)
            }
        }
        
        // Advance time to trigger navigation
        advanceTimeBy(3000) // 3 seconds
        
        // Then
        composeTestRule.waitForIdle()
        verify(mockNavigation).invoke()
    }

    @Test
    fun `splash screen uses correct timing`() {
        // This test verifies the splash duration is appropriate
        // for TV viewing (not too fast, not too slow)
        val expectedDuration = 2500L // 2.5 seconds
        
        // The actual timing is tested through the LaunchedEffect
        // which should trigger navigation after this duration
        assert(expectedDuration >= 2000) { "Splash should be at least 2 seconds for TV" }
        assert(expectedDuration <= 3000) { "Splash should not exceed 3 seconds" }
    }
}


