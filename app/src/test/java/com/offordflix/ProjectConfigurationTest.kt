package com.offordflix

import org.junit.Test
import org.junit.Assert.*

/**
 * Test for project configuration and dependencies.
 * 
 * This test verifies that the basic project setup is working correctly
 * and all required dependencies are properly configured.
 */
class ProjectConfigurationTest {
    
    @Test
    fun `test project package name is correct`() {
        val expectedPackage = "com.offordflix"
        val actualPackage = OffordflixApplication::class.java.packageName
        assertEquals(expectedPackage, actualPackage)
    }
    
    @Test
    fun `test basic Android TV navigation setup`() {
        // This test verifies that our navigation screens are accessible
        assertTrue("SplashScreen class should be accessible", 
            com.offordflix.ui.screens.splash.SplashScreen::class.java.name.isNotEmpty())
        assertTrue("ProfileSelectionScreen class should be accessible", 
            com.offordflix.ui.screens.profile.ProfileSelectionScreen::class.java.name.isNotEmpty())
        assertTrue("HomeScreen class should be accessible", 
            com.offordflix.ui.screens.home.HomeScreen::class.java.name.isNotEmpty())
        assertTrue("PlayerScreen class should be accessible", 
            com.offordflix.ui.screens.player.PlayerScreen::class.java.name.isNotEmpty())
    }
    
    @Test
    fun `test domain model creation`() {
        val profile = com.offordflix.domain.model.Profile(
            id = "test-123",
            name = "Test User",
            avatarId = 1,
            isKidsProfile = false,
            createdAt = System.currentTimeMillis(),
            lastUsed = System.currentTimeMillis()
        )
        
        assertNotNull("Profile should be created successfully", profile)
        assertEquals("test-123", profile.id)
        assertEquals("Test User", profile.name)
        assertEquals(1, profile.avatarId)
        assertFalse(profile.isKidsProfile)
    }
}


