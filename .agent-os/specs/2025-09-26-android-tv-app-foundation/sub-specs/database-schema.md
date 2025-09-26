# Database Schema

This is the database schema implementation for the spec detailed in @.agent-os/specs/2025-09-26-android-tv-app-foundation/spec.md

## Schema Changes

### New Local Profile Storage (Android TV App)

Since the Android TV app needs to support multiple local profiles that map to backend user accounts, we need local storage for profile management.

#### Local Profile Data Structure (DataStore Proto)
```protobuf
syntax = "proto3";

message UserProfile {
  string profile_id = 1;
  string profile_name = 2;
  int32 avatar_id = 3;
  bool is_kids_profile = 4;
  string backend_user_token = 5;
  int64 created_at = 6;
  int64 last_used = 7;
  ProfilePreferences preferences = 8;
}

message ProfilePreferences {
  string preferred_language = 1;
  bool auto_play_next = 2;
  string default_quality = 3;
  bool subtitle_enabled = 4;
  string subtitle_language = 5;
}

message ProfileList {
  repeated UserProfile profiles = 1;
  string active_profile_id = 2;
  int32 profile_count = 3;
}
```

### Backend Schema Extensions (Optional Enhancement)

While the existing backend schema supports user profiles, we may need minor enhancements for the multi-profile Android TV experience:

#### Enhanced User Profiles Table
```sql
-- Add columns to existing users table for better TV app support
ALTER TABLE users 
ADD COLUMN profile_type VARCHAR(20) DEFAULT 'standard' CHECK (profile_type IN ('standard', 'kids', 'admin'));

ALTER TABLE users 
ADD COLUMN avatar_id INTEGER DEFAULT 1 CHECK (avatar_id >= 1 AND avatar_id <= 24);

ALTER TABLE users 
ADD COLUMN parent_profile_id BIGINT REFERENCES users(id) ON DELETE CASCADE;

-- Index for parent-child profile relationships
CREATE INDEX idx_users_parent_profile ON users(parent_profile_id);
```

#### Device Profile Mapping Table
```sql
-- New table to link device profiles to backend users
CREATE TABLE device_profiles (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL,
    local_profile_id VARCHAR(255) NOT NULL,
    backend_user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    profile_name VARCHAR(100) NOT NULL,
    avatar_id INTEGER DEFAULT 1 CHECK (avatar_id >= 1 AND avatar_id <= 24),
    is_kids_profile BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(device_id, local_profile_id)
);

-- Indexes for efficient profile lookups
CREATE INDEX idx_device_profiles_device ON device_profiles(device_id);
CREATE INDEX idx_device_profiles_user ON device_profiles(backend_user_id);
CREATE INDEX idx_device_profiles_last_used ON device_profiles(last_used DESC);
```

#### Profile Watch Sessions Table
```sql
-- Enhanced watch tracking for profile-specific viewing
CREATE TABLE profile_watch_sessions (
    id BIGSERIAL PRIMARY KEY,
    device_profile_id BIGINT REFERENCES device_profiles(id) ON DELETE CASCADE,
    movie_id BIGINT NOT NULL,
    episode_id BIGINT,
    session_start TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_end TIMESTAMP,
    watch_duration INTEGER, -- in seconds
    playback_position INTEGER, -- last position in seconds
    completed BOOLEAN DEFAULT FALSE,
    quality_used VARCHAR(20),
    subtitle_language VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for profile watch history
CREATE INDEX idx_profile_watch_sessions_profile ON profile_watch_sessions(device_profile_id);
CREATE INDEX idx_profile_watch_sessions_content ON profile_watch_sessions(movie_id, episode_id);
CREATE INDEX idx_profile_watch_sessions_start ON profile_watch_sessions(session_start DESC);
```

## Implementation Strategy

### Local Storage (Android TV App)
1. **Profile Data**: Use DataStore with Protocol Buffers for type-safe, async local storage
2. **Profile Limit**: Enforce maximum 5 profiles in app logic with UI validation
3. **Kids Profile**: Store `is_kids_profile` flag locally and apply content filtering
4. **Avatar Storage**: Bundle 24 avatar images in app resources, reference by ID

### Backend Integration (Optional)
1. **Profile Sync**: Optionally sync local profiles with backend for cross-device experience
2. **Kids Content Filtering**: Use existing content ratings and genre filtering for kids profiles
3. **Watch History**: Link device profile watch sessions to backend user accounts for recommendations

## Data Flow

### Profile Creation Flow
1. User creates profile on Android TV → Local DataStore
2. Profile authenticates with backend → Store backend user token
3. Watch activity tracked locally → Optionally sync to backend
4. Profile preferences stored locally → Applied during playback

### Kids Profile Restrictions
1. Profile marked as `is_kids_profile = true`
2. Content filtering applied at UI level using TMDB ratings
3. Restricted content types: R-rated movies, TV-MA shows, explicit content
4. Enhanced parental controls available in profile settings

## Migration Strategy

### Phase 1: Local-Only Implementation
- Implement local profile storage with DataStore
- No backend schema changes required
- Use existing backend authentication per profile

### Phase 2: Backend Enhancement (Future)
- Add device profile mapping for cross-device sync
- Enhanced watch session tracking
- Family account management features

## Data Integrity

### Profile Validation
- Maximum 5 profiles enforced in app logic
- Unique profile names within device
- Valid avatar ID range (1-24)
- Kids profile restrictions properly applied

### Data Cleanup
- Automatic cleanup of old watch sessions (90+ days)
- Profile data backup/restore on app reinstall
- Graceful handling of backend authentication failures


