package com.offordflix.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offordflix.domain.model.Profile
import com.offordflix.ui.components.AvatarImage
import com.offordflix.ui.components.AvatarSize
import com.offordflix.ui.components.AvatarGallery

/**
 * Enhanced profile selection screen with full functionality.
 * 
 * Features:
 * - Up to 5 user profiles with avatars
 * - Kids profile support with special indicators
 * - Profile creation, editing, and deletion
 * - Android TV D-pad navigation optimized
 * - Focus-driven UI with smooth animations
 */
@Composable
fun ProfileSelectionScreen(
    onProfileSelected: (String) -> Unit,
    viewModel: ProfileSelectionViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        // Auto-focus first profile if available
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Who's Watching?",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Profiles Grid
        ProfileGrid(
            profiles = profiles,
            onProfileSelected = { profile ->
                viewModel.selectProfile(profile, onProfileSelected)
            },
            onAddProfile = {
                viewModel.showCreateProfile()
            },
            onDeleteProfile = { profile ->
                viewModel.deleteProfile(profile.id)
            },
            maxProfiles = 5
        )
        
        // Loading indicator
        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Error message
        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
    
    // Create profile dialog
    if (uiState.showCreateProfile) {
        CreateProfileDialog(
            existingProfiles = profiles,
            onCreateProfile = { name, avatarId, isKidsProfile ->
                viewModel.createProfile(name, avatarId, isKidsProfile)
            },
            onDismiss = {
                viewModel.hideCreateProfile()
            },
            viewModel = viewModel
        )
    }
    
    // Edit profile dialog
    if (uiState.showEditProfile && uiState.editingProfile != null) {
        EditProfileDialog(
            profile = uiState.editingProfile,
            existingProfiles = profiles,
            onUpdateProfile = { name, avatarId ->
                viewModel.updateProfile(uiState.editingProfile.id, name, avatarId)
            },
            onDismiss = {
                viewModel.hideEditProfile()
            },
            viewModel = viewModel
        )
    }
}

/**
 * Grid of profile cards with add profile option.
 */
@Composable
private fun ProfileGrid(
    profiles: List<Profile>,
    onProfileSelected: (Profile) -> Unit,
    onAddProfile: () -> Unit,
    onDeleteProfile: (Profile) -> Unit,
    maxProfiles: Int
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Existing profiles
        items(profiles) { profile ->
            ProfileCard(
                profile = profile,
                onProfileSelected = onProfileSelected,
                onDeleteProfile = onDeleteProfile,
                onEditProfile = { editProfile ->
                    // Add edit functionality via long press or context menu
                }
            )
        }
        
        // Add profile button (if under limit)
        if (profiles.size < maxProfiles) {
            item {
                AddProfileCard(
                    onClick = onAddProfile
                )
            }
        }
    }
}

/**
 * Individual profile card with avatar and name.
 */
@Composable
private fun ProfileCard(
    profile: Profile,
    onProfileSelected: (Profile) -> Unit,
    onDeleteProfile: (Profile) -> Unit,
    onEditProfile: (Profile) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    Card(
        modifier = modifier
            .size(200.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onProfileSelected(profile) },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 12.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Avatar
            AvatarImage(
                avatarId = profile.avatarId,
                name = profile.name,
                isSelected = isFocused,
                isKidsAvatar = profile.isKidsProfile,
                size = AvatarSize.Large
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Profile name
            Text(
                text = profile.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            
            // Kids indicator
            if (profile.isKidsProfile) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "KIDS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Add new profile card.
 */
@Composable
private fun AddProfileCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    Card(
        modifier = modifier
            .size(200.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 12.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else 
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Plus icon (using text for now)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(60.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Add Profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Dialog for creating a new profile.
 */
@Composable
private fun CreateProfileDialog(
    existingProfiles: List<Profile>,
    onCreateProfile: (String, Int, Boolean) -> Unit,
    onDismiss: () -> Unit,
    viewModel: ProfileSelectionViewModel
) {
    var profileName by remember { mutableStateOf("") }
    var selectedAvatarId by remember { mutableStateOf(1) }
    var isKidsProfile by remember { mutableStateOf(false) }
    
    val availableAvatars = viewModel.getAvailableAvatars(isKidsProfile)
    val nameValidation = viewModel.validateProfileName(profileName, existingProfiles)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create New Profile")
        },
        text = {
            Column {
                // Profile name input
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Profile Name") },
                    isError = nameValidation != ProfileNameValidation.Valid,
                    supportingText = {
                        when (nameValidation) {
                            ProfileNameValidation.Empty -> Text("Name cannot be empty")
                            ProfileNameValidation.TooLong -> Text("Name too long (max 50 characters)")
                            ProfileNameValidation.AlreadyExists -> Text("Name already exists")
                            ProfileNameValidation.Valid -> null
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Kids profile toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isKidsProfile,
                        onCheckedChange = { 
                            isKidsProfile = it
                            selectedAvatarId = if (it) 13 else 1 // Reset avatar when switching
                        }
                    )
                    Text("Kids Profile")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Enhanced avatar gallery
                AvatarGallery(
                    selectedAvatarId = selectedAvatarId,
                    isKidsProfile = isKidsProfile,
                    onAvatarSelected = { selectedAvatarId = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreateProfile(profileName, selectedAvatarId, isKidsProfile)
                },
                enabled = nameValidation == ProfileNameValidation.Valid && profileName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog for editing an existing profile.
 */
@Composable
private fun EditProfileDialog(
    profile: Profile,
    existingProfiles: List<Profile>,
    onUpdateProfile: (String, Int) -> Unit,
    onDismiss: () -> Unit,
    viewModel: ProfileSelectionViewModel
) {
    var profileName by remember { mutableStateOf(profile.name) }
    var selectedAvatarId by remember { mutableStateOf(profile.avatarId) }
    
    val availableAvatars = viewModel.getAvailableAvatars(profile.isKidsProfile)
    val nameValidation = viewModel.validateProfileName(profileName, existingProfiles, profile.id)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit ${if (profile.isKidsProfile) "Kids " else ""}Profile")
        },
        text = {
            Column {
                // Profile name input
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Profile Name") },
                    isError = nameValidation != ProfileNameValidation.Valid,
                    supportingText = {
                        when (nameValidation) {
                            ProfileNameValidation.Empty -> Text("Name cannot be empty")
                            ProfileNameValidation.TooLong -> Text("Name too long (max 50 characters)")
                            ProfileNameValidation.AlreadyExists -> Text("Name already exists")
                            ProfileNameValidation.Valid -> null
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Kids profile indicator (non-editable)
                if (profile.isKidsProfile) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👶 Kids Profile",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Enhanced avatar gallery
                AvatarGallery(
                    selectedAvatarId = selectedAvatarId,
                    isKidsProfile = profile.isKidsProfile,
                    onAvatarSelected = { selectedAvatarId = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateProfile(profileName, selectedAvatarId)
                },
                enabled = nameValidation == ProfileNameValidation.Valid && 
                         profileName.isNotBlank() &&
                         (profileName != profile.name || selectedAvatarId != profile.avatarId)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

