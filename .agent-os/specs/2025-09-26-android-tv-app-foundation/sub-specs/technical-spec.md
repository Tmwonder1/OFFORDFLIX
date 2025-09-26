# Technical Specification

This is the technical specification for the spec detailed in @.agent-os/specs/2025-09-26-android-tv-app-foundation/spec.md

## Technical Requirements

### Android TV Project Structure
- **Target SDK**: Android TV API 33+ (backward compatible to API 28)
- **Build System**: Gradle with Android Gradle Plugin 8.0+
- **Architecture**: Single-activity with Navigation Compose and MVVM pattern
- **Kotlin Version**: 1.9+ with JDK 17 toolchain
- **Compose BOM**: 2024.02+ for Jetpack Compose for TV

### UI Framework Requirements
- **Jetpack Compose for TV**: Core UI framework with TV-specific components
- **Material 3 for TV**: Design system with 10-foot UI specifications
- **Navigation Compose**: Type-safe navigation between screens (Splash → Profile Selection → Home → Player)
- **Focus Management**: Compose TV focus APIs with D-pad semantic handling
- **Accessibility**: TalkBack support with proper content descriptions

### Profile System Implementation
- **Local Storage**: DataStore (Proto) for profile data persistence
- **Profile Limit**: Maximum 5 profiles with validation and UI constraints
- **Kids Profile**: Special profile type with content filtering flags
- **Avatar System**: Pre-loaded avatar resources (24 avatar options minimum)
- **Profile State**: StateFlow-based profile management in shared ViewModel

### Video Playback Requirements
- **ExoPlayer**: Latest stable version with HLS/DASH adaptive streaming support
- **Player Controls**: TV-optimized overlay with D-pad navigation
- **Integration**: Connect to existing backend streaming endpoints
- **Progress Tracking**: Real-time playback position updates to user profile
- **Error Handling**: Network error recovery with retry mechanisms and user-friendly messages

### Animation & Performance
- **Splash Animation**: Lottie animation or Compose Animation for brand introduction (2-3 second duration)
- **Transitions**: Smooth screen transitions with 300ms max duration
- **Focus Animations**: Scale and elevation effects for focused elements
- **Image Loading**: Coil integration with memory/disk caching for avatar and content images

### State Management & Architecture
- **ViewModels**: Separate ViewModels for ProfileSelection, Home, and Player screens
- **Repository Pattern**: ProfileRepository for local profile management
- **Dependency Injection**: Hilt for compile-time DI setup
- **Navigation State**: NavHost with proper back stack management

## External Dependencies

### Core Android TV Dependencies
- **androidx.tv:tv-foundation** - Core TV UI components and utilities
- **androidx.tv:tv-material** - Material 3 for TV design components
- **androidx.compose.tv:tv-foundation** - Compose for TV foundation library
- **Justification**: Essential for proper Android TV development with Compose

### ExoPlayer Video Dependencies
- **androidx.media3:media3-exoplayer** - Core video playback functionality
- **androidx.media3:media3-ui** - Player UI components optimized for TV
- **androidx.media3:media3-exoplayer-hls** - HLS streaming support for backend integration
- **Justification**: Industry standard for video playback with comprehensive format support

### Networking & Serialization
- **com.squareup.retrofit2:retrofit** - HTTP client for backend API communication
- **com.squareup.okhttp3:logging-interceptor** - Request/response logging for debugging
- **org.jetbrains.kotlinx:kotlinx-serialization-json** - JSON parsing for API responses
- **Justification**: Proven networking stack compatible with existing backend

### Image Loading & Caching
- **io.coil-kt:coil-compose** - Compose-native image loading for avatars and content artwork
- **Justification**: Optimized for Compose with excellent caching and performance

### Animation & UI Enhancement
- **com.airbnb.android:lottie-compose** - Lottie animation support for splash screen
- **androidx.compose.animation:animation** - Core Compose animations for transitions
- **Justification**: Professional animation capabilities for polished user experience

### Dependency Injection & Architecture
- **com.google.dagger:hilt-android** - Compile-time dependency injection
- **androidx.hilt:hilt-navigation-compose** - Hilt integration with Navigation Compose
- **Justification**: Type-safe dependency injection reducing runtime errors

### Local Data & Preferences
- **androidx.datastore:datastore** - Modern preference storage for profile data
- **org.jetbrains.kotlinx:kotlinx-serialization-protobuf** - Efficient binary serialization for DataStore
- **Justification**: Replaces SharedPreferences with async, type-safe storage


