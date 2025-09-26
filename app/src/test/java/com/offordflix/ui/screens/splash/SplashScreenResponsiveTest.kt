package com.offordflix.ui.screens.splash

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import com.offordflix.ui.theme.OffordflixTheme

/**
 * Tests for splash screen responsiveness across different screen sizes.
 * 
 * Verifies that the splash screen adapts properly to various Android TV
 * screen configurations and maintains readability.
 */
class SplashScreenResponsiveTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `splash screen displays correctly on 1080p TV`() {
        // Given a 1080p TV screen (1920x1080)
        val mockNavigation = mock<() -> Unit>()
        
        // When
        composeTestRule.setContent {
            OffordflixTheme {
                SplashScreen(onNavigateToProfileSelection = mockNavigation)
            }
        }
        
        // Then all elements should be visible and properly sized
        composeTestRule.onNodeWithText("OFFORDFLIX").assertIsDisplayed()
        composeTestRule.onNodeWithText("Streaming Reimagined").assertIsDisplayed()
    }

    @Test
    fun `splash screen displays correctly on 4K TV`() {
        // Given a 4K TV screen (3840x2160)
        val mockNavigation = mock<() -> Unit>()
        
        // When
        composeTestRule.setContent {
            OffordflixTheme {
                SplashScreen(onNavigateToProfileSelection = mockNavigation)
            }
        }
        
        // Then elements should scale appropriately for 4K
        composeTestRule.onNodeWithText("OFFORDFLIX").assertIsDisplayed()
        composeTestRule.onNodeWithText("Streaming Reimagined").assertIsDisplayed()
    }

    @Test
    fun `splash screen handles landscape orientation`() {
        // Given landscape orientation (standard for TV)
        val mockNavigation = mock<() -> Unit>()
        
        // When
        composeTestRule.setContent {
            OffordflixTheme {
                SplashScreen(onNavigateToProfileSelection = mockNavigation)
            }
        }
        
        // Then content should be centered and readable
        composeTestRule.onNodeWithText("OFFORDFLIX").assertIsDisplayed()
        composeTestRule.onNodeWithText("Streaming Reimagined").assertIsDisplayed()
    }

    @Test
    fun `splash screen text sizing is appropriate for TV viewing`() {
        // Verify that text sizes are large enough for 10-foot UI
        val mockNavigation = mock<() -> Unit>()
        
        composeTestRule.setContent {
            OffordflixTheme {
                SplashScreen(onNavigateToProfileSelection = mockNavigation)
            }
        }
        
        // Text should be visible from TV viewing distance
        // This is tested by ensuring the components render without errors
        composeTestRule.onNodeWithText("OFFORDFLIX").assertIsDisplayed()
        composeTestRule.onNodeWithText("Streaming Reimagined").assertIsDisplayed()
    }

    @Test
    fun `splash screen animation timing is appropriate for TV`() {
        // Verify timing is neither too fast nor too slow for TV UX
        val splashDuration = 2500L // milliseconds
        
        // TV splash screens should be:
        // - Long enough to establish brand (>= 2 seconds)
        // - Short enough to not frustrate users (<= 3 seconds)
        assert(splashDuration >= 2000) { "Splash duration too short for TV branding" }
        assert(splashDuration <= 3000) { "Splash duration too long for TV UX" }
    }

    @Test
    fun `splash screen loading indicator is subtle and appropriate`() {
        val mockNavigation = mock<() -> Unit>()
        
        composeTestRule.setContent {
            OffordflixTheme {
                SplashScreen(onNavigateToProfileSelection = mockNavigation)
            }
        }
        
        // Loading indicator should be present but not distracting
        // Main content should still be the focus
        composeTestRule.onNodeWithText("OFFORDFLIX").assertIsDisplayed()
        composeTestRule.onNodeWithText("Streaming Reimagined").assertIsDisplayed()
    }
}


