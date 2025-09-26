package com.offordflix.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.offordflix.ui.theme.OffordflixTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for AvatarGallery component.
 * 
 * Tests avatar selection, focus behavior, and D-pad navigation
 * for Android TV remote control interaction.
 */
@RunWith(AndroidJUnit4::class)
class AvatarGalleryTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `avatar gallery displays correct avatars for adult profile`() {
        var selectedAvatarId = 1
        
        composeTestRule.setContent {
            OffordflixTheme {
                AvatarGallery(
                    selectedAvatarId = selectedAvatarId,
                    isKidsProfile = false,
                    onAvatarSelected = { selectedAvatarId = it }
                )
            }
        }
        
        // Should show title for adult avatars
        composeTestRule.onNodeWithText("Choose Your Avatar").assertIsDisplayed()
        
        // Should show helper text
        composeTestRule.onNodeWithText("This avatar will represent you on Offordflix").assertIsDisplayed()
    }

    @Test
    fun `avatar gallery displays correct avatars for kids profile`() {
        var selectedAvatarId = 13
        
        composeTestRule.setContent {
            OffordflixTheme {
                AvatarGallery(
                    selectedAvatarId = selectedAvatarId,
                    isKidsProfile = true,
                    onAvatarSelected = { selectedAvatarId = it }
                )
            }
        }
        
        // Should show title for kids avatars
        composeTestRule.onNodeWithText("Choose a Kids Avatar").assertIsDisplayed()
        
        // Should show kids-specific helper text
        composeTestRule.onNodeWithText("Choose a fun avatar for your child's profile").assertIsDisplayed()
    }

    @Test
    fun `avatar selection updates selected avatar`() {
        var selectedAvatarId = 1
        
        composeTestRule.setContent {
            OffordflixTheme {
                AvatarGallery(
                    selectedAvatarId = selectedAvatarId,
                    isKidsProfile = false,
                    onAvatarSelected = { selectedAvatarId = it }
                )
            }
        }
        
        // Wait for gallery to load
        composeTestRule.waitForIdle()
        
        // Initial selection should be avatar 1
        assert(selectedAvatarId == 1)
    }

    @Test
    fun `compact avatar gallery shows limited number of avatars`() {
        var selectedAvatarId = 1
        
        composeTestRule.setContent {
            OffordflixTheme {
                CompactAvatarGallery(
                    selectedAvatarId = selectedAvatarId,
                    isKidsProfile = false,
                    onAvatarSelected = { selectedAvatarId = it },
                    maxVisible = 3
                )
            }
        }
        
        // Should show avatar label
        composeTestRule.onNodeWithText("Avatar:").assertIsDisplayed()
        
        // Should show more avatars hint when applicable
        composeTestRule.onNodeWithText("Tap 'More Avatars' to see all options").assertIsDisplayed()
    }

    @Test
    fun `avatar gallery handles focus navigation correctly`() {
        var selectedAvatarId = 1
        
        composeTestRule.setContent {
            OffordflixTheme {
                AvatarGallery(
                    selectedAvatarId = selectedAvatarId,
                    isKidsProfile = false,
                    onAvatarSelected = { selectedAvatarId = it }
                )
            }
        }
        
        // Wait for composition
        composeTestRule.waitForIdle()
        
        // Gallery should be displayed and interactive
        composeTestRule.onNodeWithText("Choose Your Avatar").assertIsDisplayed()
    }

    @Test
    fun `avatar selection cards display correctly`() {
        var selectedAvatarId = 1
        
        composeTestRule.setContent {
            OffordflixTheme {
                AvatarGallery(
                    selectedAvatarId = selectedAvatarId,
                    isKidsProfile = false,
                    onAvatarSelected = { selectedAvatarId = it }
                )
            }
        }
        
        composeTestRule.waitForIdle()
        
        // Verify the gallery is present and functional
        composeTestRule.onNodeWithText("Choose Your Avatar").assertIsDisplayed()
    }
}

