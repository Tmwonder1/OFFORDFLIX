package com.offordflix.data.repository

import com.offordflix.domain.model.ContentFilter
import com.offordflix.domain.model.ContentFilterUtils
import com.offordflix.domain.model.ContentRating
import com.offordflix.domain.model.Profile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for content filtering based on profile settings.
 * 
 * Provides content filtering logic for kids profiles and
 * parental controls to ensure age-appropriate content.
 */
@Singleton
class ContentFilterRepository @Inject constructor() {
    
    /**
     * Get content filter for a specific profile.
     */
    fun getContentFilter(profile: Profile): ContentFilter {
        return if (profile.isKidsProfile) {
            ContentFilter.forKidsProfile()
        } else {
            ContentFilter.forAdultProfile()
        }
    }
    
    /**
     * Filter content list based on profile restrictions.
     */
    fun <T> filterContent(
        profile: Profile,
        content: List<T>,
        getRating: (T) -> ContentRating?,
        getGenres: (T) -> List<String>
    ): List<T> {
        if (!profile.isKidsProfile) {
            return content // No filtering for adult profiles
        }
        
        return content.filter { item ->
            ContentFilterUtils.isContentAllowed(
                profile = profile,
                contentRating = getRating(item),
                genres = getGenres(item)
            )
        }
    }
    
    /**
     * Check if specific content item is allowed for profile.
     */
    fun isContentAllowed(
        profile: Profile,
        contentRating: ContentRating?,
        genres: List<String>
    ): Boolean {
        return ContentFilterUtils.isContentAllowed(profile, contentRating, genres)
    }
    
    /**
     * Get warning message for restricted content.
     */
    fun getContentWarning(
        profile: Profile,
        contentRating: ContentRating?,
        genres: List<String>
    ): String? {
        return ContentFilterUtils.getContentWarning(profile, contentRating, genres)
    }
    
    /**
     * Get API filters for content discovery based on profile.
     */
    fun getApiFilters(profile: Profile): Map<String, String> {
        return ContentFilterUtils.getSearchFilters(profile)
    }
    
    /**
     * Parse content rating from string (TMDB format).
     */
    fun parseContentRating(ratingString: String?): ContentRating? {
        if (ratingString.isNullOrBlank()) return null
        
        return when (ratingString.uppercase()) {
            "G" -> ContentRating.G
            "PG" -> ContentRating.PG
            "PG-13" -> ContentRating.PG_13
            "R" -> ContentRating.R
            "NC-17" -> ContentRating.NC_17
            "TV-Y" -> ContentRating.TV_Y
            "TV-Y7" -> ContentRating.TV_Y7
            "TV-G" -> ContentRating.TV_G
            "TV-PG" -> ContentRating.TV_PG
            "TV-14" -> ContentRating.TV_14
            "TV-MA" -> ContentRating.TV_MA
            "U" -> ContentRating.U
            "12A" -> ContentRating.TWELVE_A
            "15" -> ContentRating.FIFTEEN
            "18" -> ContentRating.EIGHTEEN
            "NR", "NOT RATED" -> ContentRating.NR
            "UNRATED" -> ContentRating.UNRATED
            else -> null
        }
    }
    
    /**
     * Get recommended watch time limits for kids profiles.
     */
    fun getWatchTimeLimits(profile: Profile): WatchTimeLimits? {
        return if (profile.isKidsProfile) {
            WatchTimeLimits(
                dailyLimitMinutes = 120, // 2 hours
                sessionLimitMinutes = 60, // 1 hour per session
                bedtimeStartHour = 20, // 8 PM
                bedtimeEndHour = 7 // 7 AM
            )
        } else {
            null
        }
    }
}

/**
 * Watch time limits for parental controls.
 */
data class WatchTimeLimits(
    val dailyLimitMinutes: Int,
    val sessionLimitMinutes: Int,
    val bedtimeStartHour: Int, // 24-hour format
    val bedtimeEndHour: Int // 24-hour format
)

