# API Specification

This is the API specification for the spec detailed in @.agent-os/specs/2025-09-26-android-tv-app-foundation/spec.md

## API Integration Requirements

The Android TV app will integrate with the existing Offordflix backend API, with some potential enhancements for profile management and kids content filtering.

## Existing Backend Endpoints (Already Available)

### Authentication Endpoints
- **POST** `/api/auth/login` - User authentication for profile login
- **POST** `/api/auth/refresh` - Token refresh for maintaining sessions
- **POST** `/api/auth/logout` - Profile session cleanup

### Content Discovery Endpoints
- **GET** `/api/content/popular/movies` - Popular movies for home screen
- **GET** `/api/content/trending/movies` - Trending movies
- **GET** `/api/content/popular/tv` - Popular TV shows
- **GET** `/api/content/search` - Content search functionality
- **GET** `/api/content/genres` - Genre categories for browsing

### Streaming Endpoints
- **GET** `/movie/{tmdbId}` - Movie streaming sources
- **GET** `/tv/{tmdbId}?s={season}&e={episode}` - TV episode streaming sources

### User Data Endpoints
- **GET** `/api/user/watchlist` - User's saved content
- **GET** `/api/user/continue-watching` - Resume watching list
- **POST** `/api/user/watch-progress` - Update viewing progress
- **GET** `/api/user/settings` - User preferences and settings

## New API Endpoints (To Be Added)

### Profile Management Endpoints

#### GET /api/profiles/device/{deviceId}
**Purpose:** Retrieve all profiles associated with a device
**Parameters:**
- `deviceId` (path): Unique Android TV device identifier
**Response:**
```json
{
  "profiles": [
    {
      "profileId": "uuid",
      "profileName": "John",
      "avatarId": 5,
      "isKidsProfile": false,
      "backendUserId": 123,
      "lastUsed": "2025-09-26T10:30:00Z"
    }
  ],
  "maxProfiles": 5,
  "currentCount": 3
}
```
**Errors:** 404 if device not found, 401 if unauthorized

#### POST /api/profiles/device/{deviceId}
**Purpose:** Create new profile on device
**Parameters:**
- `deviceId` (path): Device identifier
- Request body:
```json
{
  "profileName": "Sarah",
  "avatarId": 12,
  "isKidsProfile": true,
  "userCredentials": {
    "email": "sarah@family.com",
    "password": "password"
  }
}
```
**Response:**
```json
{
  "profileId": "new-uuid",
  "backendUserId": 456,
  "authToken": "jwt-token",
  "created": true
}
```
**Errors:** 400 if profile limit exceeded, 409 if name exists, 401 if auth fails

#### PUT /api/profiles/{profileId}
**Purpose:** Update profile information
**Parameters:**
- `profileId` (path): Profile to update
- Request body:
```json
{
  "profileName": "Sarah Updated",
  "avatarId": 15,
  "lastUsed": "2025-09-26T11:00:00Z"
}
```
**Response:** Updated profile object
**Errors:** 404 if profile not found, 400 if invalid data

#### DELETE /api/profiles/{profileId}
**Purpose:** Remove profile from device
**Parameters:**
- `profileId` (path): Profile to delete
**Response:** 204 No Content
**Errors:** 404 if profile not found, 403 if not authorized

### Kids Content Filtering Endpoints

#### GET /api/content/kids/movies
**Purpose:** Get age-appropriate movies for kids profiles
**Parameters:**
- `page` (query): Page number for pagination
- `genre` (query): Optional genre filter
**Response:** Filtered movie list with G, PG ratings only
**Errors:** 400 if invalid parameters

#### GET /api/content/kids/tv
**Purpose:** Get age-appropriate TV shows for kids profiles
**Parameters:**
- `page` (query): Page number for pagination
- `genre` (query): Optional genre filter
**Response:** Filtered TV shows with Y, TV-Y7 ratings only
**Errors:** 400 if invalid parameters

### Enhanced User Tracking Endpoints

#### POST /api/user/profile-watch-session
**Purpose:** Track watch session with profile context
**Parameters:**
- Request body:
```json
{
  "profileId": "uuid",
  "movieId": 12345,
  "episodeId": 67890,
  "sessionStart": "2025-09-26T20:00:00Z",
  "sessionEnd": "2025-09-26T21:30:00Z",
  "watchDuration": 5400,
  "playbackPosition": 5400,
  "completed": true,
  "qualityUsed": "1080p",
  "subtitleLanguage": "en"
}
```
**Response:**
```json
{
  "sessionId": "session-uuid",
  "continueWatching": true,
  "progressPercent": 100
}
```
**Errors:** 400 if invalid data, 404 if content not found

## Avatar Management

### GET /api/avatars
**Purpose:** Get available avatar options
**Response:**
```json
{
  "avatars": [
    {
      "id": 1,
      "name": "Avatar 1",
      "imageUrl": "/avatars/avatar-1.png",
      "category": "default"
    },
    {
      "id": 15,
      "name": "Kids Avatar 1", 
      "imageUrl": "/avatars/kids-avatar-1.png",
      "category": "kids"
    }
  ],
  "totalCount": 24
}
```

## Content Filtering Implementation

### Kids Profile Content Rules
- **Movies**: MPAA ratings G, PG only
- **TV Shows**: TV-Y, TV-Y7, TV-G ratings only
- **Exclude**: Any content marked as adult, horror, or explicit
- **Genre Filtering**: Remove thriller, horror, adult genres from kids profiles

### API Response Modifications
When `isKidsProfile: true` is detected:
1. Apply rating filters to all content endpoints
2. Remove inappropriate categories from genre lists
3. Filter search results by age-appropriate content
4. Hide adult-themed trailers and artwork

## Authentication Flow

### Profile-Based Authentication
1. User selects profile on Android TV
2. App retrieves stored `backendUserId` and `authToken`
3. If token expired, refresh using refresh token
4. All subsequent API calls include profile context in headers:
   ```
   Authorization: Bearer {authToken}
   X-Profile-Id: {profileId}
   X-Kids-Profile: {true/false}
   ```

## Error Handling

### Profile-Specific Errors
- **PROFILE_LIMIT_EXCEEDED**: Maximum 5 profiles reached
- **KIDS_CONTENT_BLOCKED**: Content not available for kids profile
- **PROFILE_AUTH_EXPIRED**: Profile session expired, re-authentication needed
- **DEVICE_NOT_REGISTERED**: Device needs initial setup

### Graceful Degradation
- If profile sync fails, use local profile data
- If kids filtering fails, default to safe content only
- Cache content responses for offline profile switching


