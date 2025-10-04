package com.offordflix.data.repository

import com.offordflix.data.api.OffordflixApi
import com.offordflix.data.dto.StreamFileDto
import com.offordflix.data.dto.StreamingResponseDto
import com.offordflix.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for video player functionality.
 * 
 * Handles streaming URL generation, backend communication,
 * and video metadata management for ExoPlayer integration.
 */
@Singleton
class VideoPlayerRepository @Inject constructor(
    private val offordflixApi: OffordflixApi
) {
    
    private val backendBaseUrl = "http://192.168.1.17:3000" // Updated for network access
    
    /**
     * Get streaming URL for a movie.
     */
    fun getMovieStreamingUrl(tmdbId: String): String {
        return "$backendBaseUrl/movie/$tmdbId"
    }
    
    /**
     * Get streaming URL for a TV show episode.
     */
    fun getTvShowStreamingUrl(tmdbId: String, season: Int, episode: Int): String {
        return "$backendBaseUrl/tv/$tmdbId?s=$season&e=$episode"
    }

    /**
     * Fetch backend JSON and return the best playable stream with headers.
     */
    suspend fun fetchBestStream(
        contentType: ContentType,
        tmdbId: String,
        season: Int? = null,
        episode: Int? = null
    ): Result<ResolvedStream> {
        return try {
            val response = when (contentType) {
                ContentType.MOVIE -> offordflixApi.getMovieStreams(tmdbId)
                ContentType.TV_SHOW -> offordflixApi.getTvStreams(
                    tmdbId = tmdbId,
                    season = season ?: 1,
                    episode = episode ?: 1
                )
            }

            if (!response.isSuccessful) {
                return Result.failure(IllegalStateException("Backend error: ${response.code()}"))
            }

            val body: StreamingResponseDto = response.body()
                ?: return Result.failure(IllegalStateException("Empty streaming response"))

            val files: List<StreamFileDto> = body.data?.files.orEmpty()
            if (files.isEmpty()) {
                return Result.failure(IllegalStateException("No streams available"))
            }

            // Prefer local proxy links first, else pick first HLS with headers
            val best = files.firstOrNull { it.file.contains("/proxy/hls?") }
                ?: files.firstOrNull { (it.type?.equals("hls", true) == true) || it.file.endsWith(".m3u8") }
                ?: files.first()

            Result.success(
                ResolvedStream(
                    url = best.file,
                    headers = best.headers.orEmpty()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get streaming URL with specific quality.
     */
    fun getStreamingUrlWithQuality(contentPath: String, quality: String): String {
        return "$backendBaseUrl/$contentPath?quality=$quality"
    }
    
    /**
     * Get subtitle URL for content.
     */
    fun getSubtitleUrl(contentId: String, language: String): String {
        return "$backendBaseUrl/subtitles/$contentId/$language.vtt"
    }
    
    /**
     * Validate streaming URL format.
     */
    fun isValidStreamingUrl(url: String): Boolean {
        return try {
            url.isNotBlank() && 
            (url.startsWith("http://") || url.startsWith("https://")) &&
            !url.contains("javascript:") &&
            !url.contains("<script")
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Validate content ID format.
     */
    fun validateContentId(contentId: String): Boolean {
        return contentId.isNotBlank() && 
               contentId != "null" && 
               contentId != "undefined" &&
               contentId.matches(Regex("^[a-zA-Z0-9/_-]+$"))
    }
    
    /**
     * Check backend health status.
     */
    suspend fun checkBackendHealth(): Boolean {
        return try {
            // In real implementation, make HTTP request to health endpoint
            true // Mock implementation
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get content metadata from backend.
     */
    suspend fun getContentMetadata(contentId: String): VideoMetadata {
        // Mock implementation - in real app, fetch from backend
        return VideoMetadata(
            title = "Sample Video",
            duration = 7200000L, // 2 hours in milliseconds
            description = "Sample video description",
            format = detectStreamFormat("$backendBaseUrl/$contentId"),
            bitrate = 5000
        )
    }
    
    /**
     * Detect stream format from URL.
     */
    fun detectStreamFormat(url: String): StreamFormat {
        return when {
            url.contains(".m3u8") -> StreamFormat.HLS
            url.contains(".mpd") -> StreamFormat.DASH
            url.contains(".mp4") -> StreamFormat.MP4
            url.contains(".webm") -> StreamFormat.WEBM
            else -> StreamFormat.OTHER
        }
    }
    
    /**
     * Get available video qualities for content.
     */
    suspend fun getAvailableQualities(contentId: String): List<VideoQuality> {
        // Mock implementation - in real app, fetch from backend
        return listOf(
            VideoQuality.AUTO,
            VideoQuality.HD_1080P,
            VideoQuality.HD_720P,
            VideoQuality.SD_480P
        )
    }
    
    /**
     * Get available subtitle tracks for content.
     */
    suspend fun getSubtitleTracks(contentId: String): List<SubtitleTrack> {
        // Mock implementation - in real app, fetch from backend
        return listOf(
            SubtitleTrack(
                language = "en",
                displayName = "English",
                url = getSubtitleUrl(contentId, "en"),
                isDefault = true
            ),
            SubtitleTrack(
                language = "es",
                displayName = "Spanish",
                url = getSubtitleUrl(contentId, "es")
            )
        )
    }
    
    /**
     * Get available audio tracks for content.
     */
    suspend fun getAudioTracks(contentId: String): List<AudioTrack> {
        // Mock implementation - in real app, fetch from backend
        return listOf(
            AudioTrack(
                language = "en",
                displayName = "English",
                isDefault = true
            ),
            AudioTrack(
                language = "es",
                displayName = "Spanish (Dub)"
            )
        )
    }
    
    /**
     * Save watch progress for user profile.
     */
    suspend fun saveWatchProgress(
        profileId: String,
        contentId: String,
        position: Long,
        duration: Long
    ): Result<Unit> {
        return try {
            // In real implementation, save to backend/local storage
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get watch progress for user profile.
     */
    suspend fun getWatchProgress(profileId: String, contentId: String): WatchHistoryEntry? {
        // Mock implementation - in real app, fetch from storage
        return null
    }
    
    /**
     * Get continue watching items for profile.
     */
    fun getContinueWatching(profileId: String): Flow<List<ContinueWatchingItem>> = flow {
        // Mock implementation - in real app, fetch from backend
        emit(emptyList())
    }
    
    /**
     * Mark content as completed.
     */
    suspend fun markAsCompleted(profileId: String, contentId: String): Result<Unit> {
        return try {
            // In real implementation, update backend
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get recommended content based on watch history.
     */
    fun getRecommendations(profileId: String): Flow<List<VideoContent>> = flow {
        // Mock implementation - in real app, use ML/backend recommendations
        emit(emptyList())
    }
}

data class ResolvedStream(
    val url: String,
    val headers: Map<String, String> = emptyMap()
)

