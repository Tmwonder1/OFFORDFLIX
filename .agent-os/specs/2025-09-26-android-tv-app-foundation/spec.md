# Spec Requirements Document

> Spec: Android TV App Foundation
> Created: 2025-09-26

## Overview

Create the foundational Android TV application for Offordflix with multi-user profile support, splash screen, and video playback capabilities. This establishes the core Netflix-style streaming experience with Kotlin + Jetpack Compose for TV, including a dedicated kids profile, avatar selection, and complete app navigation flow from launch to content consumption.

## User Stories

### Family Profile Management

As a family using Offordflix on Android TV, I want to have up to 5 user profiles with distinct viewing preferences and history, so that each family member can have their personalized streaming experience with appropriate content filtering for children.

The app should launch with a beautiful splash animation showing the Offordflix brand, then present a profile selection screen where users can choose from existing profiles or create new ones. Each profile maintains its own watch history, continue watching list, and playback preferences. The 5th profile is specifically designed for kids with parental controls and age-appropriate content filtering.

### Personalized Avatar Experience

As a user setting up my profile, I want to choose from a variety of avatar images to represent my profile, so that I can easily identify my account and personalize my streaming experience.

Users can select from pre-designed avatar options during profile creation or modify their avatar later in profile settings. The avatar appears on the profile selection screen and throughout the app to maintain visual identity and personalization.

### Seamless Video Playback

As a viewer, I want to play movies and TV shows directly within the app with smooth playback controls, so that I can enjoy content without leaving the Offordflix experience.

When a user selects content to watch, the app should seamlessly transition to a full-screen video player with standard TV remote controls (play, pause, fast-forward, rewind). The player integrates with the user's profile to track watch progress, update continue watching lists, and maintain viewing history for personalized recommendations.

## Spec Scope

1. **Android TV Project Setup** - Complete project initialization with Kotlin, Jetpack Compose for TV, Material 3 for TV design system, and single-activity architecture with Navigation Compose
2. **Splash Screen Animation** - Branded app launch experience with smooth animation displaying the Offordflix name and logo before transitioning to profile selection
3. **Multi-User Profile System** - Support for up to 5 user profiles with the 5th profile specifically configured for kids, including profile creation, selection, and management
4. **Avatar Selection System** - Pre-designed avatar gallery allowing users to choose and customize their profile representation with easy switching capabilities
5. **Video Playback Integration** - ExoPlayer implementation with full-screen playback, TV remote controls, and integration with user profile watch tracking

## Out of Scope

- Backend user profile API modifications (will use existing user management system)
- Advanced parental control settings beyond kids profile designation
- Profile sharing or family account management features
- Custom avatar upload functionality
- Advanced video player features like picture-in-picture or advanced subtitle customization
- Content recommendation algorithms based on viewing history

## Expected Deliverable

1. **Functional Android TV Application** - Complete app that launches with splash screen, shows profile selection, and navigates to home screen based on selected profile
2. **Working Video Playback** - Users can select any movie or TV show and watch it in full-screen mode with standard playback controls and progress tracking
3. **Profile Management System** - Users can create up to 5 profiles, select avatars, and maintain separate viewing histories with the kids profile properly filtered for age-appropriate content


