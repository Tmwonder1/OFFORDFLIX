-- OnStream TV Backend Database Schema
-- This schema supports all Android TV app features
-- Compatible with PostgreSQL and MySQL

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    name VARCHAR(100) NOT NULL,
    avatar INTEGER DEFAULT 1 CHECK (avatar >= 1 AND avatar <= 5),
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- User sessions
CREATE TABLE user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    access_token VARCHAR(500) UNIQUE NOT NULL,
    refresh_token VARCHAR(500),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_agent TEXT,
    ip_address INET
);

-- User watchlist
CREATE TABLE user_watchlist (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    movie_id BIGINT NOT NULL,
    movie_type VARCHAR(10) DEFAULT 'movie' CHECK (movie_type IN ('movie', 'tv')),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, movie_id)
);

-- Continue watching
CREATE TABLE user_continue_watching (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    movie_id BIGINT NOT NULL,
    episode_id BIGINT,
    episode_number INTEGER,
    season_id BIGINT,
    season_number INTEGER,
    time_watched INTEGER NOT NULL, -- in seconds
    total_time INTEGER, -- in seconds
    watch_percent INTEGER CHECK (watch_percent >= 0 AND watch_percent <= 100),
    last_watched TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, movie_id, episode_id)
);

-- Watch history
CREATE TABLE user_watch_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    movie_id BIGINT NOT NULL,
    episode_id BIGINT,
    watched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    watch_duration INTEGER, -- in seconds
    completed BOOLEAN DEFAULT FALSE
);

-- User ratings
CREATE TABLE user_ratings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    movie_id BIGINT NOT NULL,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    rated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, movie_id)
);

-- User settings
CREATE TABLE user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE UNIQUE,
    player_settings JSONB DEFAULT '{}',
    general_settings JSONB DEFAULT '{}',
    streaming_settings JSONB DEFAULT '{}',
    parental_settings JSONB DEFAULT '{}',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Content reports
CREATE TABLE content_reports (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    movie_id BIGINT NOT NULL,
    episode_id BIGINT,
    report_type VARCHAR(50) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'reviewed', 'resolved', 'dismissed')),
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

-- App analytics (optional)
CREATE TABLE app_analytics (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    event_type VARCHAR(50) NOT NULL,
    event_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address INET,
    user_agent TEXT
);

-- Content cache (for TMDB data)
CREATE TABLE content_cache (
    id BIGSERIAL PRIMARY KEY,
    cache_key VARCHAR(255) UNIQUE NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    tmdb_id BIGINT,
    data JSONB NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Streaming sources cache
CREATE TABLE streaming_cache (
    id BIGSERIAL PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    episode_id BIGINT,
    provider VARCHAR(50) NOT NULL,
    sources JSONB NOT NULL,
    quality_options JSONB,
    subtitles JSONB,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(movie_id, episode_id, provider)
);

-- Password reset tokens
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- TV login codes
CREATE TABLE tv_login_codes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(6) UNIQUE NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_user_sessions_token ON user_sessions(access_token);
CREATE INDEX idx_user_sessions_user ON user_sessions(user_id);
CREATE INDEX idx_watchlist_user ON user_watchlist(user_id);
CREATE INDEX idx_continue_watching_user ON user_continue_watching(user_id);
CREATE INDEX idx_continue_watching_last_watched ON user_continue_watching(last_watched DESC);
CREATE INDEX idx_watch_history_user ON user_watch_history(user_id);
CREATE INDEX idx_watch_history_watched_at ON user_watch_history(watched_at DESC);
CREATE INDEX idx_ratings_user ON user_ratings(user_id);
CREATE INDEX idx_content_cache_key ON content_cache(cache_key);
CREATE INDEX idx_content_cache_expires ON content_cache(expires_at);
CREATE INDEX idx_streaming_cache_movie ON streaming_cache(movie_id, episode_id);
CREATE INDEX idx_streaming_cache_expires ON streaming_cache(expires_at);
CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_tv_login_code ON tv_login_codes(code);

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to relevant tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_settings_updated_at BEFORE UPDATE ON user_settings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Sample data for development
INSERT INTO users (email, password_hash, name, avatar, email_verified) VALUES
('admin@onstream.tv', '$2b$10$example_hash_here', 'Admin User', 1, true),
('demo@onstream.tv', '$2b$10$example_hash_here', 'Demo User', 2, true),
('test@onstream.tv', '$2b$10$example_hash_here', 'Test User', 3, false);

-- Sample settings
INSERT INTO user_settings (user_id, player_settings, general_settings) VALUES
(1, '{"default_quality": "auto", "auto_play_next": true, "subtitle_language": "en"}', '{"theme": "dark", "language": "en", "auto_update": true}'),
(2, '{"default_quality": "high", "auto_play_next": false, "subtitle_language": "es"}', '{"theme": "dark", "language": "es", "auto_update": false}');

-- Views for common queries
CREATE VIEW user_stats AS
SELECT 
    u.id,
    u.name,
    u.email,
    COUNT(DISTINCT uw.id) as watchlist_count,
    COUNT(DISTINCT ucw.id) as continue_watching_count,
    COUNT(DISTINCT uwh.id) as history_count,
    COUNT(DISTINCT ur.id) as ratings_count,
    u.created_at,
    u.last_login
FROM users u
LEFT JOIN user_watchlist uw ON u.id = uw.user_id
LEFT JOIN user_continue_watching ucw ON u.id = ucw.user_id
LEFT JOIN user_watch_history uwh ON u.id = uwh.user_id
LEFT JOIN user_ratings ur ON u.id = ur.user_id
GROUP BY u.id, u.name, u.email, u.created_at, u.last_login;

-- Cleanup procedures
CREATE OR REPLACE FUNCTION cleanup_expired_sessions()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM user_sessions WHERE expires_at < CURRENT_TIMESTAMP;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION cleanup_expired_cache()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM content_cache WHERE expires_at < CURRENT_TIMESTAMP;
    DELETE FROM streaming_cache WHERE expires_at < CURRENT_TIMESTAMP;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Schedule cleanup (requires pg_cron extension)
-- SELECT cron.schedule('cleanup-sessions', '0 */6 * * *', 'SELECT cleanup_expired_sessions();');
-- SELECT cron.schedule('cleanup-cache', '0 2 * * *', 'SELECT cleanup_expired_cache();');


