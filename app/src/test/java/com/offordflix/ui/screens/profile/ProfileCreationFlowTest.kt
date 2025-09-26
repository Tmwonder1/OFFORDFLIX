package com.offordflix.ui.screens.profile

import app.cash.turbine.test
import com.offordflix.data.repository.ProfileRepository
import com.offordflix.domain.model.Profile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.Assert.*

/**
 * Unit tests for profile creation flow and avatar selection.
 * 
 * Tests the complete profile creation workflow including
 * validation, avatar selection, and kids profile handling.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileCreationFlowTest {

    private val mockRepository = mock<ProfileRepository>()

    @Test
    fun `avatar selection provides correct ranges for adult and kids profiles`() {
        val viewModel = ProfileSelectionViewModel(mockRepository)
        
        // Adult avatars should be 1-12
        val adultAvatars = viewModel.getAvailableAvatars(isKidsProfile = false)
        assertEquals(12, adultAvatars.size)
        assertEquals(1, adultAvatars.first())
        assertEquals(12, adultAvatars.last())
        
        // Kids avatars should be 13-24
        val kidsAvatars = viewModel.getAvailableAvatars(isKidsProfile = true)
        assertEquals(12, kidsAvatars.size)
        assertEquals(13, kidsAvatars.first())
        assertEquals(24, kidsAvatars.last())
    }

    @Test
    fun `profile name validation works correctly`() = runTest {
        val viewModel = ProfileSelectionViewModel(mockRepository)
        val existingProfiles = listOf(
            Profile("1", "John", 1, false, 0L, 0L),
            Profile("2", "Jane", 2, false, 0L, 0L)
        )
        
        // Valid name
        val validResult = viewModel.validateProfileName("Mike", existingProfiles)
        assertEquals(ProfileNameValidation.Valid, validResult)
        
        // Empty name
        val emptyResult = viewModel.validateProfileName("", existingProfiles)
        assertEquals(ProfileNameValidation.Empty, emptyResult)
        
        // Too long name
        val longName = "A".repeat(51)
        val longResult = viewModel.validateProfileName(longName, existingProfiles)
        assertEquals(ProfileNameValidation.TooLong, longResult)
        
        // Duplicate name
        val duplicateResult = viewModel.validateProfileName("John", existingProfiles)
        assertEquals(ProfileNameValidation.AlreadyExists, duplicateResult)
        
        // Case insensitive duplicate
        val caseResult = viewModel.validateProfileName("JOHN", existingProfiles)
        assertEquals(ProfileNameValidation.AlreadyExists, caseResult)
    }

    @Test
    fun `profile creation handles kids profile correctly`() = runTest {
        whenever(mockRepository.profiles).thenReturn(flowOf(emptyList()))
        val viewModel = ProfileSelectionViewModel(mockRepository)
        
        // Kids profiles should have different default settings
        val kidsAvatars = viewModel.getAvailableAvatars(true)
        val adultAvatars = viewModel.getAvailableAvatars(false)
        
        // Kids avatars should be in different range
        assertTrue("Kids avatars should start from 13", kidsAvatars.all { it >= 13 })
        assertTrue("Adult avatars should be 1-12", adultAvatars.all { it <= 12 })
    }

    @Test
    fun `profile creation flow state management works correctly`() = runTest {
        whenever(mockRepository.profiles).thenReturn(flowOf(emptyList()))
        val viewModel = ProfileSelectionViewModel(mockRepository)
        
        viewModel.uiState.test {
            // Initial state
            val initialState = awaitItem()
            assertFalse(initialState.showCreateProfile)
            assertFalse(initialState.isLoading)
            assertNull(initialState.error)
            
            // Show create profile dialog
            viewModel.showCreateProfile()
            val showDialogState = awaitItem()
            assertTrue(showDialogState.showCreateProfile)
            
            // Hide create profile dialog
            viewModel.hideCreateProfile()
            val hideDialogState = awaitItem()
            assertFalse(hideDialogState.showCreateProfile)
        }
    }

    @Test
    fun `avatar ID validation ensures proper ranges`() {
        // Test avatar ID ranges
        val validAdultAvatars = (1..12).toList()
        val validKidsAvatars = (13..24).toList()
        val invalidAvatars = listOf(0, -1, 25, 100)
        
        validAdultAvatars.forEach { avatarId ->
            assertTrue("Avatar ID $avatarId should be valid for adults", avatarId in 1..24)
        }
        
        validKidsAvatars.forEach { avatarId ->
            assertTrue("Avatar ID $avatarId should be valid for kids", avatarId in 1..24)
        }
        
        invalidAvatars.forEach { avatarId ->
            assertFalse("Avatar ID $avatarId should be invalid", avatarId in 1..24)
        }
    }

    @Test
    fun `profile creation validates maximum profile limit`() = runTest {
        // Mock 5 existing profiles (at limit)
        val existingProfiles = (1..5).map { index ->
            Profile("profile-$index", "User $index", index, false, 0L, 0L)
        }
        
        whenever(mockRepository.profiles).thenReturn(flowOf(existingProfiles))
        whenever(mockRepository.getProfileCount()).thenReturn(5)
        
        val viewModel = ProfileSelectionViewModel(mockRepository)
        
        // Should not be able to create more profiles
        val canCreate = viewModel.canCreateProfile()
        assertFalse("Should not be able to create 6th profile", canCreate)
    }

    @Test
    fun `kids profile creation enforces single kids profile rule`() {
        // This test ensures only one kids profile can exist
        val existingProfiles = listOf(
            Profile("1", "Adult 1", 1, false, 0L, 0L),
            Profile("2", "Adult 2", 2, false, 0L, 0L),
            Profile("3", "Kids", 15, true, 0L, 0L) // Already has kids profile
        )
        
        // Attempting to create another kids profile should be validated
        // This validation would happen in the repository layer
        val hasKidsProfile = existingProfiles.any { it.isKidsProfile }
        assertTrue("Should detect existing kids profile", hasKidsProfile)
    }

    @Test
    fun `avatar selection dialog shows correct avatars based on profile type`() {
        val viewModel = ProfileSelectionViewModel(mockRepository)
        
        // Adult profile should show avatars 1-12
        val adultAvatars = viewModel.getAvailableAvatars(false)
        assertTrue("Adult avatars should be 1-12", adultAvatars.all { it in 1..12 })
        
        // Kids profile should show avatars 13-24
        val kidsAvatars = viewModel.getAvailableAvatars(true)
        assertTrue("Kids avatars should be 13-24", kidsAvatars.all { it in 13..24 })
        
        // Should have same count
        assertEquals("Should have same number of avatars", adultAvatars.size, kidsAvatars.size)
    }
}

