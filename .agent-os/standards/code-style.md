# Code Style Guide

## Context

Global code style rules for Agent OS projects.


<conditional-block context-check="general-formatting">
IF this General Formatting section already read in current context:
  SKIP: Re-reading this section
  NOTE: "Using General Formatting rules already in context"
ELSE:
  READ: The following formatting rules

## General Formatting

### Indentation
- Use 4 spaces for indentation (never tabs)
- Keep line length to 120 characters max
- No trailing whitespace; end files with a newline

### Naming Conventions
- **Functions/Properties/Variables**: lowerCamelCase (e.g., `loadHeroArt`, `playbackState`)
- **Classes/Interfaces/Objects**: PascalCase (e.g., `HomeViewModel`, `PlaybackRepository`)
- **Constants**: UPPER_SNAKE_CASE with `const val` when possible (e.g., `DEFAULT_PAGE_SIZE`)
- **Enums**: PascalCase for type, UPPER_SNAKE_CASE for entries (e.g., `PlayerState.PLAYING`)
- **Composable functions**: PascalCase; suffix with type when helpful (e.g., `HeroBanner`, `ShelfRow`)
- **Test methods**: `should_doSomething_when_condition`

### String Formatting
- Use double quotes for strings and interpolation (Kotlin uses single quotes only for `Char`)
- Prefer string templates: `"Hello, $name"`
- For multi-line strings use `trimIndent()` or `trimMargin()` and keep indentation readable

### Imports & Files
- No wildcard imports; one class per import
- Group imports by Kotlin, AndroidX, third-party, then project
- One top-level public class or object per file; keep files focused

### Comments & Documentation
- Use KDoc for public classes, functions, and complex logic
- Explain “why,” not “what,” when code is non-obvious
- Keep comments up to date with code changes

### Nullability & Errors
- Model absence with nullable types when it’s a valid state
- Avoid `!!`; prefer safe calls, `require/check`, and early returns
- Wrap fallible calls; return `Result<T>` or a sealed type for domain errors

### Collections & Immutability
- Prefer immutable `val` and read-only `List/Map/Set`
- Expose `List<T>` publicly even if stored as `MutableList<T>`

### Formatting Options
- Enable trailing commas in Kotlin where supported
- Keep braces on the same line; always use braces for conditionals
</conditional-block>

<conditional-block task-condition="kotlin-android" context-check="kotlin-style">
IF current task involves Kotlin/Android app code:
  IF kotlin-style.md already in context:
    SKIP: Re-reading this file
    NOTE: "Using Kotlin/Android style guide already in context"
  ELSE:
    READ: The following Kotlin/Android rules

## Kotlin & Android

### Project Structure
- **Layers**: `ui/`, `domain/`, `data/`
- **UI**: Compose screens + components
- **Domain**: use-cases and models (pure Kotlin)
- **Data**: repositories, network, local cache

### Architecture
- MVVM with unidirectional data flow
- ViewModels expose `StateFlow`/`Immutable` UI state
- One source of truth per screen; hoist state out of Composables

### Coroutines
- Use structured concurrency; no global scope
- Dispatchers: `IO` for I/O, `Default` for CPU, main-safe collectors in UI
- Cancel jobs in `onCleared()` or `viewModelScope`

### Dependency Injection
- Hilt for DI; constructor inject where possible
- Provide singletons for Retrofit/OkHttp/ExoPlayer/Coil

### Networking
- Retrofit + OkHttp with interceptors for auth/logging
- JSON: `kotlinx.serialization` with explicit `@SerialName`
- Timeouts: read/write/connect set explicitly

### Caching & Preferences
- DataStore (Proto) for settings; Room optional for content cache
- Avoid blocking the UI thread; use suspend DAO methods

### Logging/Telemetry
- Timber for logs (no logs in production beyond WARN/ERROR)
- Crashlytics for crashes; analytics events via a small facade
</conditional-block>

<conditional-block task-condition="compose-tv" context-check="compose-style">
IF current task involves Jetpack Compose for TV:
  IF compose-style.md already in context:
    SKIP: Re-reading this file
    NOTE: "Using Compose style guide already in context"
  ELSE:
    READ: The following Compose for TV rules

## Jetpack Compose (TV)

### Composables
- Keep them small and stateless; pass all state and events in parameters
- Name previews with `Preview` suffix; provide light/dark and 1080p sizes
- Use `remember` only for UI-only state; persist app state in ViewModel

### Stability & Performance
- Prefer immutable data classes and `@Immutable` where appropriate
- Use `derivedStateOf` for computed values
- Avoid heavy work in composition; move to effects or ViewModel

### Effects
- `LaunchedEffect(key)` for one-off jobs tied to a key
- `DisposableEffect` for resource cleanup (e.g., focus listeners)

### Focus & D-pad
- Use `Modifier.focusTarget()` and `FocusRequester`
- Visual feedback: scale + focus ring; keep it consistent across cards
- Debounce hero background changes (≈150–200 ms) on focus change

### Layout & Them­ing
- Material 3 for TV; use spacing and typography tokens
- Don’t hardcode sizes; use `dp/sp` constants and theme

### Images & Media
- Coil for images with `AsyncImage`; enable crossfade for hero swaps
- Preload backdrops for the next focused item

### Animation
- `Crossfade`/`AnimatedContent` for hero-to-details expansion
- Keep durations under 300 ms; prefer easing that feels snappy on TV

### Accessibility
- Provide `contentDescription` where it adds value
- Respect focus order; ensure contrast for 10-foot UI
</conditional-block>

<conditional-block task-condition="media-playback" context-check="player-style">
IF current task involves media playback:
  IF player-style.md already in context:
    SKIP: Re-reading this file
    NOTE: "Using Player style guide already in context"
  ELSE:
    READ: The following player rules

## ExoPlayer

### Setup
- Single shared `ExoPlayer` per screen; release in `DisposableEffect`
- Provide custom `DataSource.Factory` for auth headers

### Streams & Tracks
- Prefer adaptive HLS/DASH; expose track selection in UI
- Subtitle styling in UI layer; default to user’s system language

### Errors & QoE
- Listen to analytics/events; surface meaningful error messages
- Retry with backoff for transient errors; no infinite loops
</conditional-block>

<conditional-block task-condition="testing" context-check="testing-style">
IF current task involves tests:
  IF testing-style.md already in context:
    SKIP: Re-reading this file
    NOTE: "Using Testing style guide already in context"
  ELSE:
    READ: The following testing rules

## Testing

### Unit & Integration
- JUnit4/5 for unit tests; run on JVM
- Turbine for `Flow` testing; use virtual time when helpful
- Mock only boundaries; prefer fakes for repositories

### UI Tests
- Compose UI Test for focus and navigation
- Test hero updates on focus and details expansion
- Avoid brittle pixel assertions; assert semantics and state

### Naming & Structure
- Test names: `should_ExpectedBehavior_when_Context`
- Arrange-Act-Assert in order; keep tests independent
</conditional-block>

<conditional-block task-condition="code-health" context-check="quality-style">
IF current task involves code quality:
  IF quality-style.md already in context:
    SKIP: Re-reading this file
    NOTE: "Using Code Quality guide already in context"
  ELSE:
    READ: The following quality rules

## Code Quality

### Lint & Style
- Ktlint and Detekt required; no warnings on main
- Enable Android Lint checks; treat critical as errors

### Reviews
- Small PRs (< 400 LOC changed) with clear descriptions
- Include screenshots or short clips for UI changes
- Address review comments or explain decisions briefly

### Dependencies
- Pin versions; avoid dynamic `+`
- Review third-party libs for maintenance and license
</conditional-block>
"""

with open("/mnt/data/code_style_guide.md", "w") as f:
    f.write(content)
