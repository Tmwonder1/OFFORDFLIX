# Offordflix Tech Stack

## Overview

Offordflix uses a **Node.js backend** with a **Kotlin Android TV frontend**, designed for a Netflix-style streaming experience with focus-driven navigation and inline detail expansion.

## Backend (Production Ready ✅)

### Core Framework
- **Runtime**: Node.js (v18+)
- **Framework**: Express.js
- **Language**: JavaScript (ES modules)
- **Database**: PostgreSQL with comprehensive schema

### API & Integration
- **API Style**: RESTful with OpenAPI 3.0 documentation
- **Content Metadata**: TMDB (The Movie Database) API
- **Streaming Sources**: Multiple provider scrapers
  - VidSrc, AutoEmbed, PrimeWire, EmbedSu, etc.
- **Authentication**: JWT-like tokens with refresh mechanism

### Infrastructure
- **Containerization**: Docker with docker-compose
- **Caching**: node-cache for in-memory + database caching
- **CORS**: Configured for local development and production
- **Proxy**: Built-in proxy server for streaming sources

### Dependencies
```json
{
  "express": "^4.18.2",
  "axios": "^1.7.9", 
  "cheerio": "^1.0.0",
  "cors": "^2.8.5",
  "dotenv": "^16.4.7",
  "node-cache": "^5.1.2",
  "node-fetch": "^3.3.2"
}
```

## Android TV Frontend (To Be Built 🎯)

### Platform & Language
- **Platform**: Android TV (API 28+, target latest stable)
- **Language**: Kotlin (JDK 17 toolchain)
- **Architecture**: Single-activity with MVVM

### UI Framework
- **UI**: Jetpack Compose for TV
- **Design System**: Material 3 for TV
- **Navigation**: Navigation Compose
- **Focus Management**: Compose TV focus APIs with D-pad semantics

### State & Data
- **State Management**: Kotlin Coroutines + Flow
- **Lists & Feeds**: Paging 3 for infinite scrolling
- **Local Storage**: DataStore for preferences
- **Caching**: Room (optional) for content cache

### Networking & Media
- **HTTP Client**: Retrofit + OkHttp
- **JSON**: kotlinx.serialization
- **Auth**: OkHttp interceptor for bearer tokens
- **Images**: Coil with memory/disk cache and prefetch
- **Video Player**: ExoPlayer (HLS/DASH, subtitle/track selection)
- **DRM**: Widevine L1/L3 support

### Performance & Quality
- **Animations**: Compose Animation (Crossfade, AnimatedContent)
- **Focus Behavior**: Focus-driven hero banner with debounced updates
- **Testing**: JUnit, Robolectric, Compose UI Tests
- **Linting**: Ktlint and Detekt
- **Logging**: Timber
- **Analytics**: Firebase Crashlytics & Analytics

### Build & CI/CD
- **Build System**: Gradle with latest Android Gradle Plugin
- **Packaging**: Android App Bundle (internal/production tracks)
- **CI/CD**: GitHub Actions (build and test on push)

## Architecture Patterns

### Backend Architecture
```
Backend API (Express.js)
├── Authentication Layer (JWT tokens)
├── Content Routes (/movie, /tv)
├── User Management (/api/user/*)
├── Streaming Provider Controllers
├── TMDB Integration Layer
├── Caching Layer (Memory + Database)
└── PostgreSQL Database
```

### Android TV Architecture
```
Android TV App (MVVM + Compose)
├── UI Layer (Compose for TV)
│   ├── Hero Banner System
│   ├── Focus-Driven Navigation
│   └── Inline Detail Expansion
├── Domain Layer (Use Cases)
├── Data Layer (Repository Pattern)
│   ├── Network (Retrofit + Backend API)
│   ├── Local Cache (DataStore + Room)
│   └── Image Cache (Coil)
└── Player Layer (ExoPlayer)
```

## Key Technical Decisions

### Netflix-Style UX Pattern
- **Hero Banner**: Dynamic artwork that changes based on current focus
- **Inline Details**: No page navigation - details expand from hero banner
- **Focus Transitions**: Smooth scale effects with 150-200ms debounced updates
- **Material 3 for TV**: Optimized spacing and typography for 10-foot UI

### Performance Optimizations
- **Image Preloading**: Next likely backdrop preloaded during navigation
- **Debounced Updates**: Hero changes throttled to prevent visual thrashing
- **Immutable State**: `@Immutable` data classes for Compose stability
- **Structured Concurrency**: ViewModelScope and lifecycleScope usage

### Development Standards
- **Code Style**: 4-space indentation, 120-char lines, trailing commas
- **Testing**: `should_action_when_condition` naming convention
- **Dependencies**: Pinned versions, no dynamic `+` versions
- **Reviews**: Small PRs (<400 LOC) with screenshots for UI changes

## Environment Configuration

### Backend Environment
```bash
# Required
TMDB_API_KEY=your_tmdb_api_key
PORT=3000

# Optional
NODE_ENV=production
DATABASE_URL=postgresql://...
CACHE_TTL=3600
```

### Android Development
```bash
# Android SDK 34+
# Kotlin 1.9+
# Compose BOM 2024.02+
# Target Android TV API 33+
```

## Deployment Strategy

### Backend Deployment
- **Container**: Docker with multi-stage builds
- **Database**: PostgreSQL with connection pooling
- **Monitoring**: Server logs + health check endpoints
- **Scaling**: Horizontal scaling with load balancer

### Android TV Distribution
- **Google Play Console**: Internal testing → Production
- **APK Signing**: App signing by Google Play
- **Target Devices**: Android TV and Google TV devices
- **Requirements**: Leanback launcher intent, media keys support


