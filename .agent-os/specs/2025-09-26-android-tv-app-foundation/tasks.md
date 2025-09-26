# Spec Tasks

## Tasks

- [ ] 1. Android TV Project Setup and Configuration
  - [ ] 1.1 Write tests for project configuration and dependencies
  - [ ] 1.2 Create new Android TV module with proper manifest and permissions
  - [ ] 1.3 Configure Gradle build files with Compose for TV and required dependencies
  - [ ] 1.4 Set up Hilt dependency injection with application and module setup
  - [ ] 1.5 Configure Material 3 for TV theme and design tokens
  - [ ] 1.6 Set up Navigation Compose with single-activity architecture
  - [ ] 1.7 Create base project structure (ui, domain, data layers)
  - [ ] 1.8 Verify all tests pass and project builds successfully

- [ ] 2. Splash Screen and App Launch Experience
  - [ ] 2.1 Write tests for splash screen animation and navigation flow
  - [ ] 2.2 Create splash screen composable with Offordflix branding
  - [ ] 2.3 Implement Lottie animation or Compose animation for brand introduction
  - [ ] 2.4 Set up splash screen timer and automatic navigation to profile selection
  - [ ] 2.5 Configure app launch behavior and splash screen theming
  - [ ] 2.6 Test splash screen on different screen sizes and orientations
  - [ ] 2.7 Verify all tests pass and smooth transition to profile selection

- [ ] 3. Profile Management System and Local Storage
  - [ ] 3.1 Write tests for profile creation, storage, and validation logic
  - [ ] 3.2 Set up DataStore Proto with ProfileList schema for local storage
  - [ ] 3.3 Create ProfileRepository with CRUD operations for local profiles
  - [ ] 3.4 Implement profile validation (max 5 profiles, unique names, kids profile logic)
  - [ ] 3.5 Create ProfileViewModel with StateFlow for profile state management
  - [ ] 3.6 Implement profile creation, selection, and deletion functionality
  - [ ] 3.7 Add kids profile specific logic and content filtering flags
  - [ ] 3.8 Verify all tests pass and profile data persists correctly

- [ ] 4. Avatar Selection and Profile UI
  - [ ] 4.1 Write tests for avatar selection UI and profile creation flow
  - [ ] 4.2 Create avatar resource assets (24+ avatar images) in drawable folders
  - [ ] 4.3 Build profile selection screen with grid layout and focus navigation
  - [ ] 4.4 Create profile creation screen with name input and avatar selection
  - [ ] 4.5 Implement avatar gallery with D-pad navigation and focus indicators
  - [ ] 4.6 Add profile editing functionality for name and avatar changes
  - [ ] 4.7 Style profile UI with Material 3 for TV components and proper spacing
  - [ ] 4.8 Verify all tests pass and profile UI works smoothly with remote control

- [ ] 5. Video Playback Integration with ExoPlayer
  - [ ] 5.1 Write tests for ExoPlayer setup and playback functionality
  - [ ] 5.2 Set up ExoPlayer with HLS/DASH support and TV-optimized UI
  - [ ] 5.3 Create VideoPlayerRepository for backend streaming source integration
  - [ ] 5.4 Implement video player screen with fullscreen controls and D-pad navigation
  - [ ] 5.5 Add playback progress tracking and integration with profile watch history
  - [ ] 5.6 Implement video player error handling and retry mechanisms
  - [ ] 5.7 Connect video player to existing backend streaming endpoints (/movie, /tv)
  - [ ] 5.8 Verify all tests pass and video playback works with backend sources


