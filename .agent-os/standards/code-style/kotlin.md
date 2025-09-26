# Code Style Guide — Android TV App

## Context

Code style rules for this project’s app code. The app is written in **Kotlin** using **Jetpack Compose for TV**. If more languages are added later, extend this file with new sections.

---

## Kotlin

### Formatting
- Indentation: **4 spaces** (never tabs)
- Line length: **120 chars max**
- Trailing spaces: none; end files with a newline
- Braces: same line; always use braces for `if/else/when` branches
- Trailing commas: enabled where supported for cleaner diffs

### Naming
- **Classes/Interfaces/Objects**: `PascalCase` (e.g., `HomeViewModel`, `StreamRepository`)
- **Functions/Properties/Variables**: `lowerCamelCase` (e.g., `loadHeroArt`, `isFocused`)
- **Constants**: `UPPER_SNAKE_CASE` (`const val DEFAULT_PAGE_SIZE = 20`)
- **Enums**: `PascalCase` type, `UPPER_SNAKE_CASE` entries (`PlayerState.PLAYING`)
- **Tests**: `should_doSomething_when_condition()`

### Nullability & Errors
- Avoid `!!`. Prefer safe calls, `?:`, and early returns
- Use `require`/`check` for programmer errors; return `Result<T>` or sealed errors for domain failures
- Model absence with nullable types only when it’s a valid state

### Collections & Immutability
- Prefer `val` over `var`
- Expose read-only collections (`List`, `Map`, `Set`) from APIs
- Copy mutable inputs before storing if you must mutate internally

### Imports & Files
- No wildcard imports
- Order imports: Kotlin → AndroidX → third‑party → project
- One top-level public type per file; keep files focused

### Comments & Docs
- KDoc public APIs and non-obvious logic
- Explain the **why**, not the obvious **what**
- Keep comments updated with code changes

### Coroutines
- Use structured concurrency (`viewModelScope`, `lifecycleScope`); never `GlobalScope`
- Dispatchers: `IO` for I/O, `Default` for CPU-bound
- Prefer `suspend` over callbacks; expose `Flow` for streams
- Cancel child jobs explicitly when owning scope ends

### JSON & Time
- `kotlinx.serialization` with explicit `@SerialName` for external APIs (TMDB, backend)
- Use `Instant`, `Duration`, etc., from java.time; keep everything in UTC at the boundary

---

## Jetpack Compose for TV

### Component Design
- Small, **stateless** composables; pass state and events via parameters
- Hoist state to ViewModels; UI reads `StateFlow`/`Immutable` state
- Provide `@Preview` with light/dark and 1080p sizes; name previews with `Preview` suffix

### Focus & D‑Pad
- Use `Modifier.focusTarget()`, `FocusRequester`, and `onFocusChanged`
- Visual feedback: scale + focus ring; keep it consistent across all cards
- Debounce hero image swaps (≈150–200 ms) to avoid thrash while scrolling

### Layout & Theming
- Material 3 for TV tokens (typography, spacing)
- Don’t hardcode sizes; use `dp/sp` constants and theme values
- Keep 10‑foot UI readability: spacing, contrast, and large hit targets

### Images & Performance
- Coil for images with crossfade; preload the next likely backdrop
- Mark immutable models with `@Immutable`; use `derivedStateOf` for computed values
- Avoid heavy work in composition; move it to ViewModel or effects

### Effects & Lifecycles
- `LaunchedEffect(key)` for one‑off jobs tied to a key
- `DisposableEffect` for registering/unregistering listeners
- Keep side effects localized and predictable

### Animation
- `Crossfade`/`AnimatedContent` for hero‑to‑details expansion
- Keep durations ≤ 300 ms; prefer ease‑out for focus transitions

### Accessibility
- Provide `contentDescription` where it adds value
- Ensure focus order is logical; maintain adequate contrast

---

## ExoPlayer (Playback)

### Setup
- Single shared `ExoPlayer` per screen; release in `DisposableEffect`
- Custom `DataSource.Factory` for auth headers/tokens
- Prefer adaptive HLS/DASH; surface track/subtitle selection in UI

### Errors & QoE
- Listen to analytics to capture stalls, errors, bitrate changes
- Show clear user‑facing errors with retry/backoff for transient failures
- Never loop retries endlessly

---

## Testing

### Unit/Integration
- JUnit (4 or 5) for unit tests
- Use fakes for repositories and boundary layers; minimize mocking
- Test names: `should_action_when_condition`

### UI
- Compose UI tests for focus changes, hero updates, and details expansion
- Assert semantics/state, not pixels
- Keep tests independent and fast

---

## Code Quality

- Ktlint + Detekt: no warnings on main
- Android Lint enabled; critical issues are errors
- Small PRs with clear descriptions and screenshots/clips for UI changes
- Pin dependency versions; avoid dynamic `+` versions
