package com.offordflix.domain.model

/**
 * Domain model for video content.
 * 
 * Represents movies and TV shows with streaming information
 * and metadata for playback.
 */
data class VideoContent(
    val id: String,
    val title: String,
    val type: ContentType,
    val tmdbId: String,
    val backdropUrl: String? = null,
    val posterUrl: String? = null,
    val logoUrl: String? = null,
    val overview: String? = null,
    val releaseDate: String? = null,
    val runtime: Int? = null, // Duration in minutes
    val genres: List<String> = emptyList(),
    val rating: String? = null,
    val voteAverage: Double = 0.0,
    val popularity: Double = 0.0,
    
    // TV Show specific fields
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val seasonCount: Int? = null,
    val episodeCount: Int? = null,
    
    // Streaming information
    val streamingUrl: String? = null,
    val availableQualities: List<VideoQuality> = emptyList(),
    val subtitleTracks: List<SubtitleTrack> = emptyList(),
    val audioTracks: List<AudioTrack> = emptyList()
)

/**
 * Content type enumeration.
 */
enum class ContentType {
    MOVIE,
    TV_SHOW
}

/**
 * Video quality options.
 */
enum class VideoQuality(val displayName: String, val value: String) {
    AUTO("Auto", "auto"),
    HD_1080P("1080p", "1080p"),
    HD_720P("720p", "720p"),
    SD_480P("480p", "480p"),
    SD_360P("360p", "360p")
}

/**
 * Subtitle track information.
 */
data class SubtitleTrack(
    val language: String,
    val displayName: String,
    val url: String,
    val isDefault: Boolean = false
)

/**
 * Audio track information.
 */
data class AudioTrack(
    val language: String,
    val displayName: String,
    val isDefault: Boolean = false
)

/**
 * Playback state for video content.
 */
data class PlaybackState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentPosition: Long = 0L, // Position in milliseconds
    val duration: Long = 0L, // Total duration in milliseconds
    val bufferedPosition: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isFullscreen: Boolean = true, // Always true for TV
    val showControls: Boolean = false,
    val subtitlesEnabled: Boolean = false,
    val selectedSubtitleTrack: SubtitleTrack? = null,
    val selectedAudioTrack: AudioTrack? = null,
    val selectedQuality: VideoQuality = VideoQuality.AUTO,
    val error: String? = null
)

/**
 * Video metadata from backend.
 */
data class VideoMetadata(
    val title: String,
    val duration: Long, // Duration in milliseconds
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val format: StreamFormat,
    val size: Long? = null, // File size in bytes
    val bitrate: Int? = null // Bitrate in kbps
)

/**
 * Streaming format types.
 */
enum class StreamFormat {
    HLS,     // HTTP Live Streaming (.m3u8)
    DASH,    // Dynamic Adaptive Streaming (.mpd)
    MP4,     // Progressive MP4
    WEBM,    // WebM format
    OTHER    // Other/unknown format
}

/**
 * Playback error types.
 */
enum class PlaybackError(val message: String) {
    NETWORK_ERROR("Network connection error"),
    SOURCE_ERROR("Video source not available"),
    CODEC_ERROR("Unsupported video format"),
    PERMISSION_ERROR("Playback permission denied"),
    UNKNOWN_ERROR("Unknown playback error")
}

/**
 * Watch history entry for user profiles.
 */
data class WatchHistoryEntry(
    val contentId: String,
    val profileId: String,
    val watchedAt: Long, // Timestamp
    val position: Long, // Position in milliseconds
    val duration: Long, // Total duration
    val completed: Boolean = false // Whether fully watched
)

/**
 * Continue watching item.
 */
data class ContinueWatchingItem(
    val content: VideoContent,
    val position: Long,
    val duration: Long,
    val watchedAt: Long,
    val progressPercentage: Float = if (duration > 0) position.toFloat() / duration else 0f
) {
    val isNearCompletion: Boolean
        get() = progressPercentage > 0.9f
}

