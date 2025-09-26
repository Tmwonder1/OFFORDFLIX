package com.offordflix.domain.model

/**
 * Content filtering rules for different profile types.
 * 
 * Implements age-appropriate content restrictions and
 * parental controls for kids profiles.
 */
data class ContentFilter(
    val isKidsProfile: Boolean,
    val allowedRatings: Set<ContentRating>,
    val blockedGenres: Set<String>,
    val maxWatchTimeMinutes: Int? = null,
    val requireParentalApproval: Boolean = false
) {
    companion object {
        /**
         * Create content filter for regular adult profiles.
         */
        fun forAdultProfile(): ContentFilter {
            return ContentFilter(
                isKidsProfile = false,
                allowedRatings = ContentRating.values().toSet(),
                blockedGenres = emptySet(),
                maxWatchTimeMinutes = null,
                requireParentalApproval = false
            )
        }
        
        /**
         * Create content filter for kids profiles.
         */
        fun forKidsProfile(): ContentFilter {
            return ContentFilter(
                isKidsProfile = true,
                allowedRatings = setOf(
                    ContentRating.G,
                    ContentRating.PG,
                    ContentRating.TV_Y,
                    ContentRating.TV_Y7,
                    ContentRating.TV_G
                ),
                blockedGenres = setOf(
                    "Horror",
                    "Thriller", 
                    "Crime",
                    "War",
                    "Adult",
                    "Erotica"
                ),
                maxWatchTimeMinutes = 120, // 2 hours daily limit
                requireParentalApproval = true
            )
        }
    }
}

/**
 * Content ratings for movies and TV shows.
 */
enum class ContentRating(val displayName: String, val ageGroup: AgeGroup) {
    // Movie ratings (MPAA)
    G("G", AgeGroup.ALL),
    PG("PG", AgeGroup.ALL),
    PG_13("PG-13", AgeGroup.TEEN),
    R("R", AgeGroup.ADULT),
    NC_17("NC-17", AgeGroup.ADULT),
    
    // TV ratings
    TV_Y("TV-Y", AgeGroup.KIDS),
    TV_Y7("TV-Y7", AgeGroup.KIDS),
    TV_G("TV-G", AgeGroup.ALL),
    TV_PG("TV-PG", AgeGroup.ALL),
    TV_14("TV-14", AgeGroup.TEEN),
    TV_MA("TV-MA", AgeGroup.ADULT),
    
    // International ratings
    U("U", AgeGroup.ALL), // UK Universal
    PG_UK("PG", AgeGroup.ALL), // UK Parental Guidance
    TWELVE_A("12A", AgeGroup.TEEN), // UK 12A
    FIFTEEN("15", AgeGroup.TEEN), // UK 15
    EIGHTEEN("18", AgeGroup.ADULT), // UK 18
    
    // Unrated or unknown
    NR("NR", AgeGroup.ADULT), // Not Rated
    UNRATED("Unrated", AgeGroup.ADULT)
}

/**
 * Age groups for content classification.
 */
enum class AgeGroup {
    KIDS,    // Under 13
    ALL,     // All ages
    TEEN,    // 13-17
    ADULT    // 18+
}

/**
 * Content filtering utilities.
 */
object ContentFilterUtils {
    
    /**
     * Check if content is appropriate for the given profile.
     */
    fun isContentAllowed(
        profile: Profile,
        contentRating: ContentRating?,
        genres: List<String>
    ): Boolean {
        val filter = if (profile.isKidsProfile) {
            ContentFilter.forKidsProfile()
        } else {
            ContentFilter.forAdultProfile()
        }
        
        return isContentAllowed(filter, contentRating, genres)
    }
    
    /**
     * Check if content is allowed based on filter rules.
     */
    fun isContentAllowed(
        filter: ContentFilter,
        contentRating: ContentRating?,
        genres: List<String>
    ): Boolean {
        // Check rating
        if (contentRating != null && contentRating !in filter.allowedRatings) {
            return false
        }
        
        // Check genres
        if (genres.any { genre -> 
            filter.blockedGenres.any { blockedGenre -> 
                genre.contains(blockedGenre, ignoreCase = true) 
            }
        }) {
            return false
        }
        
        return true
    }
    
    /**
     * Get appropriate search filters for profile.
     */
    fun getSearchFilters(profile: Profile): Map<String, String> {
        return if (profile.isKidsProfile) {
            mapOf(
                "certification_country" to "US",
                "certification.lte" to "PG",
                "with_genres_exclude" to "27,53,80,10752", // Horror, Thriller, Crime, War
                "sort_by" to "popularity.desc"
            )
        } else {
            mapOf(
                "sort_by" to "popularity.desc"
            )
        }
    }
    
    /**
     * Get content warning message for restricted content.
     */
    fun getContentWarning(
        profile: Profile,
        contentRating: ContentRating?,
        genres: List<String>
    ): String? {
        if (!profile.isKidsProfile) return null
        
        val filter = ContentFilter.forKidsProfile()
        
        return when {
            contentRating != null && contentRating !in filter.allowedRatings -> {
                "This content is rated ${contentRating.displayName} and not suitable for kids profiles."
            }
            genres.any { genre -> 
                filter.blockedGenres.any { blockedGenre -> 
                    genre.contains(blockedGenre, ignoreCase = true) 
                }
            } -> {
                "This content contains themes not suitable for kids profiles."
            }
            else -> null
        }
    }
}

