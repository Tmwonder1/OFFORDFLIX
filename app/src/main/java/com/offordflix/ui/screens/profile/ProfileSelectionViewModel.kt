package com.offordflix.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offordflix.data.repository.ProfileRepository
import com.offordflix.data.proto.CreateProfileRequest
import com.offordflix.data.proto.ProfilePreferences
import com.offordflix.domain.model.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for profile selection and management.
 * 
 * Manages profile creation, selection, deletion, and validation
 * with proper state management for Android TV UI.
 */
@HiltViewModel
class ProfileSelectionViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    // State for UI
    private val _uiState = MutableStateFlow(ProfileSelectionUiState())
    val uiState: StateFlow<ProfileSelectionUiState> = _uiState.asStateFlow()
    
    // Profiles from repository
    val profiles: StateFlow<List<Profile>> = profileRepository.profiles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        // Initialize device and load profiles
        initializeProfiles()
    }
    
    /**
     * Initialize profiles and device if needed.
     */
    private fun initializeProfiles() {
        viewModelScope.launch {
            profileRepository.initializeDevice()
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }
    
    /**
     * Create a new profile.
     */
    fun createProfile(
        name: String,
        avatarId: Int,
        isKidsProfile: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val preferences = ProfilePreferences.newBuilder()
                .setPreferredLanguage("en")
                .setAutoPlayNext(!isKidsProfile) // Kids profiles default to manual play
                .setDefaultQuality("auto")
                .setSubtitleEnabled(isKidsProfile) // Kids profiles default with subtitles
                .setSubtitleLanguage("en")
                .setParentalControlsEnabled(isKidsProfile)
                .build()
            
            val request = CreateProfileRequest.newBuilder()
                .setProfileName(name)
                .setAvatarId(avatarId)
                .setIsKidsProfile(isKidsProfile)
                .setPreferences(preferences)
                .build()
            
            profileRepository.createProfile(request)
                .onSuccess { profile ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            showCreateProfile = false,
                            error = null
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = error.message
                        ) 
                    }
                }
        }
    }
    
    /**
     * Select a profile and navigate to home.
     */
    fun selectProfile(profile: Profile, onProfileSelected: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            profileRepository.setActiveProfile(profile.id)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    onProfileSelected(profile.id)
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = error.message
                        ) 
                    }
                }
        }
    }
    
    /**
     * Delete a profile.
     */
    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            profileRepository.deleteProfile(profileId)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = error.message
                        ) 
                    }
                }
        }
    }
    
    /**
     * Show create profile dialog.
     */
    fun showCreateProfile() {
        _uiState.update { it.copy(showCreateProfile = true) }
    }
    
    /**
     * Hide create profile dialog.
     */
    fun hideCreateProfile() {
        _uiState.update { it.copy(showCreateProfile = false) }
    }
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Get available avatar IDs based on profile type.
     */
    fun getAvailableAvatars(isKidsProfile: Boolean): List<Int> {
        return if (isKidsProfile) {
            // Kids avatars (second half of avatar range)
            (13..24).toList()
        } else {
            // Regular avatars (first half of avatar range)
            (1..12).toList()
        }
    }
    
    /**
     * Check if we can create more profiles.
     */
    suspend fun canCreateProfile(): Boolean {
        return profileRepository.getProfileCount() < 5
    }
    
    /**
     * Update an existing profile.
     */
    fun updateProfile(
        profileId: String,
        newName: String? = null,
        newAvatarId: Int? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            profileRepository.updateProfile(profileId, newName, newAvatarId)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            showEditProfile = false,
                            error = null
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = error.message
                        ) 
                    }
                }
        }
    }
    
    /**
     * Show edit profile dialog.
     */
    fun showEditProfile(profile: Profile) {
        _uiState.update { 
            it.copy(
                showEditProfile = true,
                editingProfile = profile
            ) 
        }
    }
    
    /**
     * Hide edit profile dialog.
     */
    fun hideEditProfile() {
        _uiState.update { 
            it.copy(
                showEditProfile = false,
                editingProfile = null
            ) 
        }
    }
    
    /**
     * Validate profile name.
     */
    fun validateProfileName(name: String, existingProfiles: List<Profile>, excludeProfileId: String? = null): ProfileNameValidation {
        val filteredProfiles = if (excludeProfileId != null) {
            existingProfiles.filter { it.id != excludeProfileId }
        } else {
            existingProfiles
        }
        
        return when {
            name.isBlank() -> ProfileNameValidation.Empty
            name.length > 50 -> ProfileNameValidation.TooLong
            filteredProfiles.any { it.name.equals(name, ignoreCase = true) } -> ProfileNameValidation.AlreadyExists
            else -> ProfileNameValidation.Valid
        }
    }
}

/**
 * UI state for profile selection screen.
 */
data class ProfileSelectionUiState(
    val isLoading: Boolean = false,
    val showCreateProfile: Boolean = false,
    val showEditProfile: Boolean = false,
    val editingProfile: Profile? = null,
    val error: String? = null
)

/**
 * Profile name validation result.
 */
sealed class ProfileNameValidation {
    object Valid : ProfileNameValidation()
    object Empty : ProfileNameValidation()
    object TooLong : ProfileNameValidation()
    object AlreadyExists : ProfileNameValidation()
}
