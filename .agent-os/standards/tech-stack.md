# Tech Stack

## Context

Defaults for the Android TV app that uses TMDB for metadata and plays M3U streams from your existing backend. Overridable in project-specific `.agent-os/product/tech-stack.md`.


- Platform: Android TV (API 28+; target latest stable)

- Language: Kotlin (JDK 17 toolchain)

- App Architecture: Single-activity with MVVM

- UI Framework: Jetpack Compose for TV

- Design System: Material 3 for TV

- UI Pattern: Focus-driven hero banner with inline details expansion

- Navigation: Navigation Compose

- State Management: Kotlin Coroutines + Flow

- Lists & Feeds: Paging 3

- Image Loading: Coil with memory/disk cache and prefetch

- Video Player: ExoPlayer (HLS/DASH, subtitle/track selection)

- DRM: Widevine L1/L3

- Networking: Retrofit + OkHttp

- JSON: kotlinx.serialization

- Auth Headers: OkHttp interceptor for bearer tokens

- Local Storage: DataStore for preferences; Room optional for caching

- Focus/Remote: Compose TV focus APIs and d-pad semantics

- Animations: Compose Animation (Crossfade, AnimatedContent)

- Accessibility: TalkBack labels, clear focus ring, safe contrast

- Logging: Timber

- Crash Reporting: Firebase Crashlytics

- Analytics: Firebase Analytics

- Testing: JUnit, Robolectric, Compose UI Tests

- Linting: Ktlint and Detekt

- Build Tooling: Gradle with latest Android Gradle Plugin

- App Packaging: Android App Bundle; internal and production tracks

- CI/CD: GitHub Actions build and test on push to staging/main

- TV Requirements: Leanback launcher intent, media keys support

- TMDB Compliance: In-app attribution and store listing credit