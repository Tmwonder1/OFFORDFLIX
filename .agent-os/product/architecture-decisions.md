# Offordflix Architecture Decisions

## Overview

This document captures the key architectural decisions made for Offordflix, including both the existing backend implementation and planned Android TV frontend.

## Backend Architecture Decisions (✅ Implemented)

### Technology Choices

**Decision**: Node.js + Express.js for Backend
- **Rationale**: JavaScript ecosystem provides excellent libraries for web scraping and API integration
- **Alternatives Considered**: Python (Flask/FastAPI), PHP, Java Spring
- **Trade-offs**: Node.js excels at I/O-heavy operations like API calls and web scraping, which is core to our streaming provider integration

**Decision**: PostgreSQL for Primary Database
- **Rationale**: Robust relational database with excellent JSON support (JSONB) for flexible user settings
- **Alternatives Considered**: MongoDB, MySQL, SQLite
- **Trade-offs**: More complex setup than SQLite, but provides better scalability and data integrity

**Decision**: In-Memory + Database Dual Caching Strategy
- **Rationale**: Fast access to frequently requested content while maintaining persistence
- **Implementation**: node-cache for hot data, PostgreSQL for persistent cache
- **Trade-offs**: More complex cache invalidation, but significantly improved performance

### API Design Decisions

**Decision**: RESTful API with OpenAPI Documentation
- **Rationale**: Standard, well-understood API pattern with self-documenting specification
- **Alternatives Considered**: GraphQL, gRPC
- **Trade-offs**: More endpoints than GraphQL, but simpler to implement and debug

**Decision**: JWT-like Token Authentication
- **Rationale**: Stateless authentication suitable for mobile/TV apps
- **Implementation**: Custom token generation with refresh token support
- **Trade-offs**: Not using full JWT libraries for simplicity, but provides similar benefits

**Decision**: Multiple Streaming Provider Integration
- **Rationale**: Redundancy and improved content availability
- **Implementation**: Modular provider system with fallback support
- **Trade-offs**: More complex error handling, but much better user experience

### Data Architecture Decisions

**Decision**: TMDB as Primary Metadata Source
- **Rationale**: Comprehensive, reliable, and actively maintained movie/TV database
- **Alternatives Considered**: IMDb API, self-maintained database
- **Trade-offs**: Dependency on external service, but saves massive maintenance overhead

**Decision**: Comprehensive User Schema
- **Rationale**: Support for advanced features like watchlists, progress tracking, and social features
- **Implementation**: Normalized database design with proper indexing
- **Trade-offs**: More complex queries, but enables rich user experience

## Android TV Architecture Decisions (🎯 To Be Implemented)

### Framework Choices

**Decision**: Jetpack Compose for TV
- **Rationale**: Modern declarative UI framework optimized for Android TV
- **Alternatives Considered**: Traditional View system, Flutter TV
- **Trade-offs**: Newer technology with learning curve, but future-proof and more maintainable

**Decision**: Single-Activity + MVVM Architecture
- **Rationale**: Modern Android architecture pattern with clear separation of concerns
- **Implementation**: Activity hosts Navigation Compose, ViewModels manage state
- **Trade-offs**: More initial setup complexity, but better testability and maintainability

**Decision**: Kotlin with Coroutines for Concurrency
- **Rationale**: Type-safe, concise language with excellent async support
- **Alternatives Considered**: Java, Flutter/Dart
- **Trade-offs**: Steeper learning curve than Java, but much more productive

### UI/UX Architecture Decisions

**Decision**: Netflix-Style Hero Banner Experience
- **Rationale**: Familiar, proven UX pattern that users already understand
- **Implementation**: Focus-driven dynamic artwork changes with smooth transitions
- **Trade-offs**: More complex focus management, but significantly better user experience

**Decision**: No Page Navigation for Details
- **Rationale**: Maintains browsing context and reduces cognitive load
- **Implementation**: Inline expansion from hero banner to fullscreen details
- **Trade-offs**: More complex animation system, but much smoother user flow

**Decision**: Material 3 for TV Design System
- **Rationale**: Google's latest design system optimized for large screens
- **Alternatives Considered**: Custom design system, Material 2
- **Trade-offs**: Dependency on Google's design decisions, but ensures consistency and accessibility

### Performance Architecture Decisions

**Decision**: Coil for Image Loading with Aggressive Caching
- **Rationale**: Compose-first image library with excellent caching capabilities
- **Implementation**: Memory cache + disk cache with preloading for next likely images
- **Trade-offs**: Higher memory usage, but much smoother browsing experience

**Decision**: Debounced Hero Updates (150-200ms)
- **Rationale**: Prevents visual thrashing while maintaining responsiveness
- **Implementation**: LaunchedEffect with delay cancellation on focus changes
- **Trade-offs**: Slight delay in updates, but much better visual stability

**Decision**: ExoPlayer for Video Playback
- **Rationale**: Industry-standard Android video player with excellent format support
- **Alternatives Considered**: MediaPlayer, third-party players
- **Trade-offs**: Larger app size, but much better playback experience and format support

### Data Layer Architecture Decisions

**Decision**: Repository Pattern with Multiple Data Sources
- **Rationale**: Clean abstraction over network, cache, and local storage
- **Implementation**: Repository coordinates between Retrofit (network) and Room (local)
- **Trade-offs**: More abstraction layers, but better testability and flexibility

**Decision**: Flow-Based Reactive Data Layer
- **Rationale**: Reactive programming model that naturally fits Compose state management
- **Implementation**: Repository exposes Flow, ViewModels collect and transform
- **Trade-offs**: More complex error handling, but better user experience with live updates

**Decision**: Paging 3 for Content Lists
- **Rationale**: Efficient infinite scrolling with built-in error handling and retry
- **Implementation**: PagingSource backed by backend API with cache integration
- **Trade-offs**: More complex setup, but much better performance with large content lists

## Cross-Cutting Architecture Decisions

### Development & Deployment

**Decision**: Docker Containerization for Backend
- **Rationale**: Consistent deployment environment and easy scaling
- **Implementation**: Multi-stage Docker build with production optimizations
- **Trade-offs**: Additional complexity, but much better deployment reliability

**Decision**: Modular Android App Architecture
- **Rationale**: Better separation of concerns and testability
- **Implementation**: Feature modules with shared core modules
- **Trade-offs**: More initial setup, but better long-term maintainability

### Testing Strategy

**Decision**: Comprehensive Testing Pyramid
- **Backend**: Unit tests for business logic, integration tests for API endpoints
- **Android**: Unit tests for ViewModels, UI tests for navigation and focus behavior
- **Trade-offs**: Significant test maintenance overhead, but much better code quality

### Error Handling & Monitoring

**Decision**: Structured Error Handling with User-Friendly Messages
- **Rationale**: Better user experience and easier debugging
- **Implementation**: Custom ErrorObject class with user/system error classification
- **Trade-offs**: More complex error handling code, but much better user experience

**Decision**: Firebase Integration for Analytics and Crash Reporting
- **Rationale**: Industry-standard monitoring tools with excellent Android integration
- **Alternatives Considered**: Custom analytics, third-party services
- **Trade-offs**: Google dependency, but proven reliability and comprehensive features

## Security & Privacy Decisions

**Decision**: No Personal Data Storage Beyond User Preferences
- **Rationale**: Minimizes privacy concerns and regulatory compliance requirements
- **Implementation**: Anonymous usage analytics only, no personal content tracking
- **Trade-offs**: Less personalization capability, but better privacy protection

**Decision**: Backend Rate Limiting and CORS Configuration
- **Rationale**: Protect against abuse while allowing legitimate client access
- **Implementation**: Express middleware with configurable origins
- **Trade-offs**: Some legitimate requests might be blocked, but much better security

## Future Architecture Considerations

### Scalability Decisions
- **Horizontal Scaling**: Backend designed to scale with load balancers
- **Database Scaling**: Schema designed for read replicas and sharding
- **CDN Integration**: Image and video content delivery optimization

### Platform Expansion
- **Fire TV Compatibility**: Architecture supports cross-platform Android TV variants
- **Chromecast Integration**: Player architecture allows for cast functionality
- **Web Frontend**: Backend API could support web client in the future

## Decision Review Process

These architectural decisions should be reviewed:
- **Quarterly**: For performance and scalability implications
- **Before Major Features**: To ensure consistency with established patterns
- **After Significant User Growth**: To validate scalability assumptions
- **When New Technologies Emerge**: To consider migration opportunities

Each decision includes rationale, alternatives considered, and trade-offs to support future decision-making and onboarding new team members.


