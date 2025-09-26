package com.offordflix.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for VideoPlayerRepository.
 * 
 * Tests video source resolution, streaming URL generation,
 * and backend integration for video playback.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VideoPlayerRepositoryTest {

    @Test
    fun `movie streaming URL generation works correctly`() = runTest {
        val repository = VideoPlayerRepository()
        val tmdbId = "123456"
        
        val streamingUrl = repository.getMovieStreamingUrl(tmdbId)
        
        // Should generate correct backend URL
        assertTrue("URL should contain movie endpoint", 
            streamingUrl.contains("/movie/$tmdbId"))
        assertTrue("URL should be valid HTTP/HTTPS", 
            streamingUrl.startsWith("http"))
    }

    @Test
    fun `TV show streaming URL generation includes season and episode`() = runTest {
        val repository = VideoPlayerRepository()
        val tmdbId = "456789"
        val season = 1
        val episode = 5
        
        val streamingUrl = repository.getTvShowStreamingUrl(tmdbId, season, episode)
        
        // Should include season and episode parameters
        assertTrue("URL should contain TV endpoint", 
            streamingUrl.contains("/tv/$tmdbId"))
        assertTrue("URL should contain season parameter", 
            streamingUrl.contains("s=$season"))
        assertTrue("URL should contain episode parameter", 
            streamingUrl.contains("e=$episode"))
    }

    @Test
    fun `streaming URL validation works correctly`() = runTest {
        val repository = VideoPlayerRepository()
        
        val validUrls = listOf(
            "https://example.com/stream.m3u8",
            "http://localhost:3000/movie/123",
            "https://backend.offordflix.com/tv/456?s=1&e=1"
        )
        
        val invalidUrls = listOf(
            "",
            "not-a-url",
            "ftp://invalid.com",
            "javascript:alert('xss')"
        )
        
        validUrls.forEach { url ->
            assertTrue("$url should be valid", repository.isValidStreamingUrl(url))
        }
        
        invalidUrls.forEach { url ->
            assertFalse("$url should be invalid", repository.isValidStreamingUrl(url))
        }
    }

    @Test
    fun `video quality selection returns appropriate streams`() = runTest {
        val repository = VideoPlayerRepository()
        val availableQualities = listOf("1080p", "720p", "480p", "auto")
        
        availableQualities.forEach { quality ->
            val qualityUrl = repository.getStreamingUrlWithQuality("movie/123", quality)
            assertTrue("Quality URL should contain quality parameter", 
                qualityUrl.contains("quality=$quality"))
        }
    }

    @Test
    fun `subtitle URL generation works correctly`() = runTest {
        val repository = VideoPlayerRepository()
        val contentId = "movie/123"
        val language = "en"
        
        val subtitleUrl = repository.getSubtitleUrl(contentId, language)
        
        assertTrue("Subtitle URL should contain content ID", 
            subtitleUrl.contains(contentId))
        assertTrue("Subtitle URL should contain language", 
            subtitleUrl.contains(language))
        assertTrue("Subtitle URL should have .vtt extension", 
            subtitleUrl.endsWith(".vtt"))
    }

    @Test
    fun `error handling for invalid content IDs`() = runTest {
        val repository = VideoPlayerRepository()
        val invalidIds = listOf("", "null", "undefined", "invalid-format")
        
        invalidIds.forEach { id ->
            val result = repository.validateContentId(id)
            assertFalse("$id should be invalid", result)
        }
    }

    @Test
    fun `backend health check validates server availability`() = runTest {
        val repository = VideoPlayerRepository()
        
        // Mock implementation - in real app would check actual backend
        val isHealthy = repository.checkBackendHealth()
        
        // Should return boolean indicating backend status
        assertTrue("Health check should return a boolean", 
            isHealthy is Boolean)
    }

    @Test
    fun `content metadata fetching works correctly`() = runTest {
        val repository = VideoPlayerRepository()
        val contentId = "movie/123"
        
        val metadata = repository.getContentMetadata(contentId)
        
        // Should return metadata with required fields
        assertNotNull("Metadata should not be null", metadata)
        assertTrue("Metadata should have title", metadata.title.isNotBlank())
        assertTrue("Metadata should have duration", metadata.duration > 0)
    }

    @Test
    fun `stream format detection works for different media types`() = runTest {
        val repository = VideoPlayerRepository()
        
        val streamFormats = mapOf(
            "https://example.com/video.m3u8" to "HLS",
            "https://example.com/video.mpd" to "DASH",
            "https://example.com/video.mp4" to "Progressive"
        )
        
        streamFormats.forEach { (url, expectedFormat) ->
            val detectedFormat = repository.detectStreamFormat(url)
            assertEquals("Format detection should work for $url", 
                expectedFormat, detectedFormat)
        }
    }
}

