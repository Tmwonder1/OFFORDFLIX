# Offordflix Development Roadmap

## Phase 0: Already Completed ✅

The backend foundation is solid and production-ready:

- **✅ Streaming Backend API**
  - Multiple provider integration (VidSrc, AutoEmbed, PrimeWire, etc.)
  - TMDB API integration for rich metadata
  - Movie and TV show support with episode-level granularity

- **✅ User Management System**
  - JWT-based authentication with refresh tokens
  - User profiles with avatar selection
  - PostgreSQL schema for scalable user data

- **✅ Content Discovery Features**
  - Popular, trending, top-rated content endpoints
  - Genre-based browsing and filtering
  - Search functionality with TMDB integration

- **✅ User Experience Features**
  - Watchlist management
  - Continue watching with progress tracking
  - User ratings and watch history
  - Comprehensive user settings

- **✅ Technical Infrastructure**
  - Caching system for performance optimization
  - Docker containerization for deployment
  - OpenAPI documentation
  - Error handling and logging

- **✅ Development Standards**
  - Code style guides for Kotlin/Android
  - Tech stack specifications for Android TV
  - Best practices documentation

## Phase 1: Android TV App Foundation 🎯 **CURRENT FOCUS**

**Goal**: Build core Android TV application with Netflix-style UX

### Core Navigation & UI Framework
- [ ] **Project Setup**
  - Kotlin + Jetpack Compose for TV
  - Material 3 for TV design system
  - Navigation Compose with single-activity architecture

- [ ] **Hero Banner System**
  - Dynamic hero artwork that updates on focus change
  - Smooth crossfade transitions between content
  - Debounced updates (150-200ms) to prevent thrashing

- [ ] **Focus-Driven Navigation**
  - D-pad optimized browsing with focus ring indicators
  - Smooth focus transitions with scale effects
  - Focus memory when returning to previous screens

- [ ] **Inline Detail Expansion**
  - Hero banner expands to fullscreen with details
  - No page navigation - everything happens in-place
  - Smooth AnimatedContent transitions

### Content Integration
- [ ] **Backend API Integration**
  - Retrofit + OkHttp with authentication interceptors
  - kotlinx.serialization for JSON parsing
  - Connection to existing Offordflix backend

- [ ] **Content Browsing**
  - Home screen with multiple content rows
  - Genre-based content discovery
  - Search functionality with TMDB metadata

- [ ] **User Features**
  - Login/authentication flow
  - Watchlist management
  - Continue watching row

## Phase 2: Enhanced Experience

**Goal**: Polish the user experience and add advanced features

### Advanced UI Features
- [ ] **Enhanced Hero Experience**
  - Auto-playing trailers when available
  - Rich metadata display (cast, ratings, synopsis)
  - Related content suggestions

- [ ] **Content Organization**
  - My List management
  - Recently watched
  - Personalized recommendations

- [ ] **User Preferences**
  - Settings screen for playback preferences
  - Theme customization
  - Language/subtitle preferences

### Performance & Polish
- [ ] **Image Optimization**
  - Coil integration with preloading
  - Memory and disk caching strategies
  - Optimized image sizes for TV

- [ ] **Animation Polish**
  - Micro-interactions for better feedback
  - Loading states with skeleton screens
  - Error state handling with retry options

## Phase 3: Video Playback

**Goal**: Integrate video playback with the hero expansion model

### ExoPlayer Integration
- [ ] **Player Setup**
  - ExoPlayer with HLS/DASH support
  - Custom UI overlays for TV
  - Subtitle and audio track selection

- [ ] **Playback Experience**
  - Seamless transition from hero to fullscreen playback
  - Picture-in-picture when browsing
  - Resume playback from continue watching

### Streaming Integration
- [ ] **Source Selection**
  - Multiple quality options
  - Fallback source handling
  - Error recovery and retry logic

## Phase 4: Advanced Features

**Goal**: Add premium features and optimizations

### Social & Engagement
- [ ] **User Ratings**
  - In-app rating system
  - Rating display in content details

- [ ] **Watch History**
  - Detailed viewing history
  - Watch progress tracking

### Technical Excellence
- [ ] **Analytics Integration**
  - Firebase Analytics for usage tracking
  - Crashlytics for error monitoring
  - Performance monitoring

- [ ] **Testing & Quality**
  - Comprehensive UI tests
  - Focus navigation testing
  - Performance benchmarking

## Phase 5: Platform Expansion

**Goal**: Expand beyond basic Android TV

### Device Support
- [ ] **Multiple Form Factors**
  - Android TV boxes and built-in smart TVs
  - Fire TV compatibility
  - Chromecast with Google TV optimization

### Future Considerations
- [ ] **Offline Support**
  - Download for offline viewing (if legally compliant)
  - Offline content management

- [ ] **Social Features**
  - Watch parties or shared viewing
  - Social ratings and reviews

---

## Notes

- **Backend Adjustments**: While the backend is complete, minor adjustments may be needed to optimize for the Android TV experience
- **No Major Refactoring Planned**: Current architecture is solid for the intended use case
- **Focus on UX**: Primary emphasis on creating a smooth, Netflix-like navigation experience


