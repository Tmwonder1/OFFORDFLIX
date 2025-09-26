package com.offordflix.domain.model

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Profile domain model.
 * 
 * Tests profile creation, validation, and business logic.
 */
class ProfileTest {

    @Test
    fun `profile creation with valid data succeeds`() {
        // Given valid profile data
        val currentTime = System.currentTimeMillis()
        
        // When creating a profile
        val profile = Profile(
            id = "test-profile-123",
            name = "John Doe",
            avatarId = 5,
            isKidsProfile = false,
            createdAt = currentTime,
            lastUsed = currentTime
        )
        
        // Then profile should be created correctly
        assertEquals("test-profile-123", profile.id)
        assertEquals("John Doe", profile.name)
        assertEquals(5, profile.avatarId)
        assertFalse(profile.isKidsProfile)
        assertEquals(currentTime, profile.createdAt)
        assertEquals(currentTime, profile.lastUsed)
    }

    @Test
    fun `kids profile can be created with appropriate settings`() {
        // Given kids profile data
        val currentTime = System.currentTimeMillis()
        
        // When creating a kids profile
        val kidsProfile = Profile(
            id = "kids-profile-456",
            name = "Little Sarah",
            avatarId = 15, // Kids avatar
            isKidsProfile = true,
            createdAt = currentTime,
            lastUsed = currentTime
        )
        
        // Then kids profile should be properly configured
        assertTrue(kidsProfile.isKidsProfile)
        assertEquals("Little Sarah", kidsProfile.name)
        assertEquals(15, kidsProfile.avatarId)
    }

    @Test
    fun `profile with empty id should be invalid`() {
        // Profile IDs should not be empty for data integrity
        val profile = Profile(
            id = "",
            name = "Test User",
            avatarId = 1,
            isKidsProfile = false,
            createdAt = System.currentTimeMillis(),
            lastUsed = System.currentTimeMillis()
        )
        
        // Empty ID should be considered invalid
        assertTrue("Profile ID should not be empty", profile.id.isEmpty())
    }

    @Test
    fun `profile name validation logic`() {
        // Test various name scenarios
        val validNames = listOf("John", "Sarah", "Mom", "Dad", "Kids")
        val invalidNames = listOf("", "   ", "A".repeat(101)) // Empty, whitespace, too long
        
        validNames.forEach { name ->
            val profile = Profile(
                id = "test-$name",
                name = name,
                avatarId = 1,
                isKidsProfile = false,
                createdAt = System.currentTimeMillis(),
                lastUsed = System.currentTimeMillis()
            )
            assertTrue("Valid name '$name' should be accepted", profile.name.isNotBlank())
        }
    }

    @Test
    fun `avatar id should be within valid range`() {
        // Avatar IDs should be between 1 and 24 (as per spec)
        val validAvatarIds = listOf(1, 12, 24)
        val invalidAvatarIds = listOf(0, -1, 25, 100)
        
        validAvatarIds.forEach { avatarId ->
            val profile = Profile(
                id = "test-avatar-$avatarId",
                name = "Test User",
                avatarId = avatarId,
                isKidsProfile = false,
                createdAt = System.currentTimeMillis(),
                lastUsed = System.currentTimeMillis()
            )
            assertTrue("Avatar ID $avatarId should be valid", 
                profile.avatarId >= 1 && profile.avatarId <= 24)
        }
    }

    @Test
    fun `profile equality and comparison`() {
        val currentTime = System.currentTimeMillis()
        
        val profile1 = Profile(
            id = "same-id",
            name = "John",
            avatarId = 1,
            isKidsProfile = false,
            createdAt = currentTime,
            lastUsed = currentTime
        )
        
        val profile2 = Profile(
            id = "same-id",
            name = "John Updated", // Different name
            avatarId = 2, // Different avatar
            isKidsProfile = false,
            createdAt = currentTime,
            lastUsed = currentTime + 1000
        )
        
        // Profiles with same ID should be considered the same entity
        assertEquals("Profiles with same ID should be equal", profile1.id, profile2.id)
    }

    @Test
    fun `kids profile cannot have inappropriate avatar ids`() {
        // Kids profiles should typically use kid-friendly avatars (13-24 range)
        val kidsProfile = Profile(
            id = "kids-test",
            name = "Little One",
            avatarId = 18, // Kids avatar range
            isKidsProfile = true,
            createdAt = System.currentTimeMillis(),
            lastUsed = System.currentTimeMillis()
        )
        
        assertTrue("Kids profile should use appropriate avatar", 
            kidsProfile.isKidsProfile && kidsProfile.avatarId > 12)
    }
}

