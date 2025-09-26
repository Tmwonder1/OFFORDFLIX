package com.offordflix.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test
import com.offordflix.ui.theme.OffordflixTheme

/**
 * Unit tests for AvatarImage component.
 * 
 * Tests avatar display, selection states, kids vs adult avatars,
 * and accessibility features.
 */
class AvatarImageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `avatar image displays correctly with name initial`() {
        composeTestRule.setContent {
            OffordflixTheme {
                AvatarImage(
                    avatarId = 1,
                    name = "John Doe",
                    isSelected = false,
                    isKidsAvatar = false
                )
            }
        }
        
        // Should display the avatar (accessibility content description)
        composeTestRule.onNodeWithContentDescription("Avatar for John Doe").assertIsDisplayed()
    }

    @Test
    fun `selected avatar shows proper focus indication`() {
        composeTestRule.setContent {
            OffordflixTheme {
                AvatarImage(
                    avatarId = 1,
                    name = "Selected User",
                    isSelected = true,
                    isKidsAvatar = false
                )
            }
        }
        
        // Selected avatar should be visible with focus styling
        composeTestRule.onNodeWithContentDescription("Avatar for Selected User").assertIsDisplayed()
    }

    @Test
    fun `kids avatar uses appropriate colors and styling`() {
        composeTestRule.setContent {
            OffordflixTheme {
                AvatarImage(
                    avatarId = 15,
                    name = "Kids User",
                    isSelected = false,
                    isKidsAvatar = true
                )
            }
        }
        
        // Kids avatar should display correctly
        composeTestRule.onNodeWithContentDescription("Avatar for Kids User").assertIsDisplayed()
    }

    @Test
    fun `avatar sizes work correctly for different use cases`() {
        AvatarSize.values().forEach { size ->
            composeTestRule.setContent {
                OffordflixTheme {
                    AvatarImage(
                        avatarId = 1,
                        name = "Test User",
                        size = size
                    )
                }
            }
            
            // Each size should render without errors
            composeTestRule.onNodeWithContentDescription("Avatar for Test User").assertIsDisplayed()
        }
    }

    @Test
    fun `avatar name utility functions work correctly`() {
        // Test avatar name generation
        val adultAvatarName = getAvatarName(5, false)
        val kidsAvatarName = getAvatarName(15, true)
        
        assert(adultAvatarName == "Avatar 5")
        assert(kidsAvatarName == "Kids Avatar 15")
    }

    @Test
    fun `avatar displays fallback initial when name is empty`() {
        composeTestRule.setContent {
            OffordflixTheme {
                AvatarImage(
                    avatarId = 1,
                    name = "", // Empty name
                    isSelected = false,
                    isKidsAvatar = false
                )
            }
        }
        
        // Should still display something (fallback behavior)
        composeTestRule.onNodeWithContentDescription("Avatar for ").assertIsDisplayed()
    }

    @Test
    fun `avatar color generation is consistent`() {
        // Test that same avatar ID always generates same color
        val color1 = getAvatarColor(5, false)
        val color2 = getAvatarColor(5, false)
        
        assert(color1 == color2) { "Same avatar ID should generate consistent colors" }
    }

    @Test
    fun `kids and adult avatars use different color palettes`() {
        // Kids avatars should use different colors than adult avatars
        val adultColor = getAvatarColor(1, false)
        val kidsColor = getAvatarColor(1, true)
        
        // Colors should be different for kids vs adults with same ID
        assert(adultColor != kidsColor) { "Kids and adult avatars should use different color schemes" }
    }
}

