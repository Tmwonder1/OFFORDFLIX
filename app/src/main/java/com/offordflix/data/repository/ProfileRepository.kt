package com.offordflix.data.repository

import com.offordflix.data.local.ProfileDataStore
import com.offordflix.data.proto.ProfileList
import com.offordflix.data.proto.UserProfile
import com.offordflix.data.proto.ProfilePreferences
import com.offordflix.data.proto.CreateProfileRequest
import com.offordflix.data.proto.ProfileValidationResult
import com.offordflix.domain.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing user profiles with local DataStore persistence.
 * 
 * Implements business rules:
 * - Maximum 5 profiles per device
 * - Unique profile names
 * - 5th profile can be designated as kids profile
 * - Profile data validation and persistence
 */
@Singleton
class ProfileRepository @Inject constructor(
    private val profileDataStore: ProfileDataStore
) {
    
    /**
     * Flow of all profiles, sorted by last used.
     */
    val profiles: Flow<List<Profile>> = profileDataStore.dataStore.data.map { profileList ->
        profileList.profilesList.map { protoProfile ->
            protoProfile.toDomainModel()
        }.sortedByDescending { it.lastUsed }
    }
    
    /**
     * Flow of current active profile.
     */
    val activeProfile: Flow<Profile?> = profileDataStore.dataStore.data.map { profileList ->
        profileList.profilesList.find { it.profileId == profileList.activeProfileId }?.toDomainModel()
    }
    
    /**
     * Get profile count.
     */
    suspend fun getProfileCount(): Int {
        return profileDataStore.dataStore.data.first().profilesCount
    }
    
    /**
     * Create a new profile with validation.
     */
    suspend fun createProfile(request: CreateProfileRequest): Result<Profile> {
        return try {
            val currentData = profileDataStore.dataStore.data.first()
            
            // Validate profile creation
            val validation = validateProfileCreation(request, currentData)
            if (!validation.isValid) {
                return Result.failure(ProfileValidationException(validation.validationErrorsList))
            }
            
            // Create new profile
            val newProfile = UserProfile.newBuilder()
                .setProfileId(UUID.randomUUID().toString())
                .setProfileName(request.profileName)
                .setAvatarId(request.avatarId)
                .setIsKidsProfile(request.isKidsProfile)
                .setCreatedAt(System.currentTimeMillis())
                .setLastUsed(System.currentTimeMillis())
                .setPreferences(request.preferences)
                .build()
            
            // Update profile list
            val updatedProfileList = currentData.toBuilder()
                .addProfiles(newProfile)
                .setProfileCount(currentData.profilesCount + 1)
                .setLastModified(System.currentTimeMillis())
                .build()
            
            profileDataStore.dataStore.updateData { updatedProfileList }
            
            Result.success(newProfile.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update an existing profile.
     */
    suspend fun updateProfile(profileId: String, name: String? = null, avatarId: Int? = null): Result<Profile> {
        return try {
            val currentData = profileDataStore.dataStore.data.first()
            val profileIndex = currentData.profilesList.indexOfFirst { it.profileId == profileId }
            
            if (profileIndex == -1) {
                return Result.failure(ProfileNotFoundException("Profile not found: $profileId"))
            }
            
            val existingProfile = currentData.profilesList[profileIndex]
            val updatedProfile = existingProfile.toBuilder().apply {
                name?.let { setProfileName(it) }
                avatarId?.let { setAvatarId(it) }
                setLastUsed(System.currentTimeMillis())
            }.build()
            
            val updatedProfileList = currentData.toBuilder()
                .setProfiles(profileIndex, updatedProfile)
                .setLastModified(System.currentTimeMillis())
                .build()
            
            profileDataStore.dataStore.updateData { updatedProfileList }
            
            Result.success(updatedProfile.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a profile.
     */
    suspend fun deleteProfile(profileId: String): Result<Unit> {
        return try {
            val currentData = profileDataStore.dataStore.data.first()
            val profileToDelete = currentData.profilesList.find { it.profileId == profileId }
                ?: return Result.failure(ProfileNotFoundException("Profile not found: $profileId"))
            
            val updatedProfiles = currentData.profilesList.filter { it.profileId != profileId }
            val updatedProfileList = currentData.toBuilder()
                .clearProfiles()
                .addAllProfiles(updatedProfiles)
                .setProfileCount(updatedProfiles.size)
                .setLastModified(System.currentTimeMillis())
                .build()
            
            profileDataStore.dataStore.updateData { updatedProfileList }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Set active profile.
     */
    suspend fun setActiveProfile(profileId: String): Result<Unit> {
        return try {
            val currentData = profileDataStore.dataStore.data.first()
            
            // Verify profile exists
            val profile = currentData.profilesList.find { it.profileId == profileId }
                ?: return Result.failure(ProfileNotFoundException("Profile not found: $profileId"))
            
            // Update last used time and set as active
            val profileIndex = currentData.profilesList.indexOfFirst { it.profileId == profileId }
            val updatedProfile = profile.toBuilder()
                .setLastUsed(System.currentTimeMillis())
                .build()
            
            val updatedProfileList = currentData.toBuilder()
                .setProfiles(profileIndex, updatedProfile)
                .setActiveProfileId(profileId)
                .setLastModified(System.currentTimeMillis())
                .build()
            
            profileDataStore.dataStore.updateData { updatedProfileList }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate profile creation request.
     */
    private fun validateProfileCreation(request: CreateProfileRequest, currentData: ProfileList): ProfileValidationResult {
        val errors = mutableListOf<String>()
        
        // Check profile limit
        if (currentData.profilesCount >= 5) {
            return ProfileValidationResult.newBuilder()
                .setIsValid(false)
                .setMaxProfilesReached(true)
                .addValidationErrors("Maximum 5 profiles allowed")
                .build()
        }
        
        // Check name uniqueness
        val nameExists = currentData.profilesList.any { it.profileName.equals(request.profileName, ignoreCase = true) }
        if (nameExists) {
            return ProfileValidationResult.newBuilder()
                .setIsValid(false)
                .setNameAlreadyExists(true)
                .addValidationErrors("Profile name already exists")
                .build()
        }
        
        // Validate name
        if (request.profileName.isBlank()) {
            errors.add("Profile name cannot be empty")
        }
        if (request.profileName.length > 50) {
            errors.add("Profile name too long (max 50 characters)")
        }
        
        // Validate avatar ID
        if (request.avatarId < 1 || request.avatarId > 24) {
            errors.add("Avatar ID must be between 1 and 24")
        }
        
        // Kids profile validation
        if (request.isKidsProfile) {
            val existingKidsProfile = currentData.profilesList.any { it.isKidsProfile }
            if (existingKidsProfile) {
                errors.add("Only one kids profile allowed per device")
            }
        }
        
        return ProfileValidationResult.newBuilder()
            .setIsValid(errors.isEmpty())
            .addAllValidationErrors(errors)
            .setNameAlreadyExists(nameExists)
            .setMaxProfilesReached(currentData.profilesCount >= 5)
            .build()
    }
    
    /**
     * Initialize device with empty profile list if needed.
     */
    suspend fun initializeDevice(): Result<Unit> {
        return try {
            val currentData = profileDataStore.dataStore.data.first()
            
            // Initialize if no device ID is set
            if (currentData.deviceId.isEmpty()) {
                val deviceId = profileDataStore.generateDeviceId()
                val initialProfileList = ProfileList.newBuilder()
                    .setDeviceId(deviceId)
                    .setProfileCount(0)
                    .setLastModified(System.currentTimeMillis())
                    .build()
                
                profileDataStore.dataStore.updateData { initialProfileList }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Extension to convert proto UserProfile to domain Profile.
 */
private fun UserProfile.toDomainModel(): Profile {
    return Profile(
        id = this.profileId,
        name = this.profileName,
        avatarId = this.avatarId,
        isKidsProfile = this.isKidsProfile,
        createdAt = this.createdAt,
        lastUsed = this.lastUsed
    )
}

/**
 * Custom exceptions for profile operations.
 */
class ProfileValidationException(val errors: List<String>) : Exception("Profile validation failed: ${errors.joinToString(", ")}")
class ProfileNotFoundException(message: String) : Exception(message)
class ProfileLimitExceededException(message: String) : Exception(message)

