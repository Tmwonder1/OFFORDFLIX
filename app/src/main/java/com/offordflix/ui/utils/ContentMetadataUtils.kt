package com.offordflix.ui.utils

import com.offordflix.domain.model.VideoContent
import com.offordflix.domain.model.ContentType

/**
 * Utility functions for formatting content metadata consistently across the UI.
 */
object ContentMetadataUtils {
    
    /**
     * Formats runtime in minutes to a human-readable string.
     * Examples: "2h 14m", "45m", "1h 30m"
     */
    fun formatRuntime(runtimeMinutes: Int?): String? {
        if (runtimeMinutes == null || runtimeMinutes <= 0) return null
        
        val hours = runtimeMinutes / 60
        val minutes = runtimeMinutes % 60
        
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }
    
    /**
     * Gets a fallback runtime based on content type and genres when no runtime is available.
     */
    fun getFallbackRuntime(content: VideoContent): String? {
        // If we already have runtime, use it
        content.runtime?.let { return formatRuntime(it) }
        
        // Generate reasonable fallback based on content type and genres
        val genres = content.genres.map { it.lowercase() }
        
        return when (content.type) {
            ContentType.MOVIE -> {
                when {
                    genres.any { it in listOf("action", "adventure", "sci-fi", "fantasy") } -> "2h 15m"
                    genres.any { it in listOf("drama", "thriller", "crime") } -> "2h 5m"
                    genres.any { it in listOf("comedy", "romance") } -> "1h 45m"
                    genres.any { it in listOf("animation", "family") } -> "1h 30m"
                    genres.any { it in listOf("horror") } -> "1h 35m"
                    genres.any { it in listOf("documentary") } -> "1h 50m"
                    else -> "2h"
                }
            }
            ContentType.TV_SHOW -> {
                when {
                    genres.any { it in listOf("drama", "crime", "thriller") } -> "55m"
                    genres.any { it in listOf("action", "adventure", "sci-fi", "fantasy") } -> "50m"
                    genres.any { it in listOf("comedy") } -> "25m"
                    genres.any { it in listOf("animation", "kids") } -> "22m"
                    genres.any { it in listOf("documentary", "reality") } -> "45m"
                    else -> "45m"
                }
            }
        }
    }
    
    /**
     * Extracts year from release date string.
     * Examples: "2023-12-25" -> "2023", "2023" -> "2023"
     */
    fun formatReleaseYear(releaseDate: String?): String? {
        if (releaseDate.isNullOrBlank()) return null
        return releaseDate.take(4).takeIf { it.length == 4 && it.all { char -> char.isDigit() } }
    }
    
    /**
     * Formats genres list to a readable string.
     * Examples: ["Action", "Adventure", "Sci-Fi"] -> "Action • Adventure • Sci-Fi"
     */
    fun formatGenres(genres: List<String>): String? {
        if (genres.isEmpty()) return null
        return genres.take(3).joinToString(" • ") // Limit to 3 genres to avoid overcrowding
    }
    
    /**
     * Formats season count for TV shows.
     * Examples: 1 -> "1 Season", 5 -> "5 Seasons"
     */
    fun formatSeasonCount(seasonCount: Int?): String? {
        if (seasonCount == null || seasonCount <= 0) return null
        return if (seasonCount == 1) "1 Season" else "$seasonCount Seasons"
    }
    
    /**
     * Gets a fallback season count for TV shows when not available.
     * Only provides fallback if real TMDB data is not available.
     */
    fun getFallbackSeasonCount(content: VideoContent): String? {
        // Only for TV shows
        if (content.type != ContentType.TV_SHOW) return null
        
        // If we already have season count, use it (REAL TMDB DATA)
        content.seasonCount?.let { return formatSeasonCount(it) }
        
        // If no real data available, don't show fake data
        // This prevents showing misleading information
        return null
        
        // Note: The fallback logic below is commented out to avoid showing fake data
        // Real season count should come from TMDB TV show details API
        /*
        // Generate reasonable fallback based on genres and popularity
        val genres = content.genres.map { it.lowercase() }
        
        return when {
            // Long-running series genres typically have more seasons
            genres.any { it in listOf("drama", "crime", "soap") } -> "5 Seasons"
            genres.any { it in listOf("comedy", "sitcom") } -> "4 Seasons"
            genres.any { it in listOf("action", "adventure", "sci-fi", "fantasy") } -> "3 Seasons"
            genres.any { it in listOf("reality", "documentary") } -> "6 Seasons"
            genres.any { it in listOf("animation", "kids") } -> "2 Seasons"
            // Default fallback
            else -> "3 Seasons"
        }
        */
    }
    
    /**
     * Formats MPA rating for display.
     * Examples: "PG-13", "R", "TV-MA"
     */
    fun formatRating(rating: String?): String? {
        if (rating.isNullOrBlank()) return null
        return rating.uppercase()
    }
    
    /**
     * Gets a fallback rating based on content characteristics when no rating is available.
     * This provides reasonable defaults based on genres and content type.
     */
    fun getFallbackRating(content: VideoContent): String? {
        // If we already have a rating, use it
        content.rating?.let { return formatRating(it) }
        
        // Generate fallback based on genres and content type
        val genres = content.genres.map { it.lowercase() }
        
        return when {
            // Adult content indicators
            genres.any { it in listOf("horror", "thriller", "crime") } -> {
                if (content.type == ContentType.TV_SHOW) "TV-14" else "PG-13"
            }
            // Family-friendly indicators
            genres.any { it in listOf("animation", "family", "comedy") } -> {
                if (content.type == ContentType.TV_SHOW) "TV-PG" else "PG"
            }
            // Action/Adventure content
            genres.any { it in listOf("action", "adventure", "sci-fi", "fantasy") } -> {
                if (content.type == ContentType.TV_SHOW) "TV-14" else "PG-13"
            }
            // Drama content
            genres.any { it in listOf("drama", "romance") } -> {
                if (content.type == ContentType.TV_SHOW) "TV-PG" else "PG-13"
            }
            // Default fallback
            else -> if (content.type == ContentType.TV_SHOW) "TV-PG" else "PG-13"
        }
    }
    
    /**
     * Gets all available metadata for a content item as a list of formatted strings.
     * Returns non-null metadata items in a consistent order, including content type and TMDB rating first.
     */
    fun getFormattedMetadata(content: VideoContent): List<String> {
        val metadata = mutableListOf<String>()
        
        // Content type badge (first)
        metadata.add(if (content.type == ContentType.MOVIE) "MOVIE" else "TV SHOW")
        
        // TMDB Rating (second, if available)
        if (content.voteAverage > 0) {
            metadata.add("⭐ %.1f".format(content.voteAverage))
        }
        
        // Rating/Age restriction (with fallback)
        getFallbackRating(content)?.let { metadata.add(it) }
        
        // Runtime or episode length (with fallback)
        getFallbackRuntime(content)?.let { metadata.add(it) }
        
        // Year of release
        formatReleaseYear(content.releaseDate)?.let { metadata.add(it) }
        
        // Genres
        formatGenres(content.genres)?.let { metadata.add(it) }
        
        // Number of seasons (TV shows only, with fallback)
        if (content.type == ContentType.TV_SHOW) {
            getFallbackSeasonCount(content)?.let { metadata.add(it) }
        }
        
        return metadata
    }
    
    /**
     * Joins metadata items with a separator for display.
     * Examples: ["PG-13", "2h 14m", "2023"] -> "PG-13 • 2h 14m • 2023"
     */
    fun joinMetadata(metadata: List<String>, separator: String = " • "): String {
        return metadata.joinToString(separator)
    }
}
