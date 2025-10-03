package com.offordflix.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.offordflix.domain.model.VideoContent
import com.offordflix.domain.model.ContentType

/**
 * Enhanced content response DTOs for our backend API.
 */

/**
 * Content section representing a horizontal row of content.
 */
@Serializable
data class ContentSection(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("type") val type: String,
    @SerialName("data") val data: List<TmdbContentDto>
)

/**
 * Home content response with enhanced content rows.
 */
@Serializable
data class HomeContentResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("id") val id: String,
    @SerialName("type") val type: Int,
    @SerialName("title") val title: String,
    @SerialName("slide") val slide: List<TmdbContentDto>,
    @SerialName("sections") val sections: List<ContentSection>,
    @SerialName("data") val data: List<TmdbContentDto>
)

/**
 * Movies content response with movie-only content rows.
 */
@Serializable
data class MoviesContentResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("id") val id: String,
    @SerialName("type") val type: Int,
    @SerialName("title") val title: String,
    @SerialName("slide") val slide: List<TmdbContentDto>,
    @SerialName("sections") val sections: List<ContentSection>,
    @SerialName("data") val data: List<TmdbContentDto>
)

/**
 * TV Shows content response with TV show-only content rows.
 */
@Serializable
data class TVShowsContentResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("id") val id: String,
    @SerialName("type") val type: Int,
    @SerialName("title") val title: String,
    @SerialName("slide") val slide: List<TmdbContentDto>,
    @SerialName("sections") val sections: List<ContentSection>,
    @SerialName("data") val data: List<TmdbContentDto>
)

/**
 * Generic TMDB content DTO that can represent both movies and TV shows.
 */
@Serializable
data class TmdbContentDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("overview") val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("runtime") val runtime: Int? = null,
    @SerialName("episode_run_time") val episodeRunTime: List<Int>? = null,
    @SerialName("genre_ids") val genreIds: List<Int>? = null,
    @SerialName("genres") val genres: List<TmdbGenreDto>? = null,
    @SerialName("media_type") val mediaType: String? = null,
    @SerialName("adult") val adult: Boolean = false,
    @SerialName("original_language") val originalLanguage: String? = null,
    @SerialName("popularity") val popularity: Double = 0.0,
    @SerialName("logo_path") val logoPath: String? = null
) {
    /**
     * Convert to VideoContent domain model.
     */
    fun toVideoContent(): VideoContent {
        val contentTitle = title ?: name ?: "Unknown Title"
        val contentType = when (mediaType) {
            "movie" -> ContentType.MOVIE
            "tv" -> ContentType.TV_SHOW
            else -> if (title != null) ContentType.MOVIE else ContentType.TV_SHOW
        }
        val releaseYear = (releaseDate ?: firstAirDate)?.take(4) ?: ""
        val contentRuntime = runtime ?: episodeRunTime?.firstOrNull() ?: 0
        val contentReleaseDate = releaseDate ?: firstAirDate ?: ""
        
        // Map genre IDs to genre names
        val genreNames = genres?.map { it.name } ?: genreIds?.mapNotNull { genreId ->
            when (genreId) {
                28 -> "Action"
                12 -> "Adventure"
                16 -> "Animation"
                35 -> "Comedy"
                80 -> "Crime"
                99 -> "Documentary"
                18 -> "Drama"
                10751 -> "Family"
                14 -> "Fantasy"
                36 -> "History"
                27 -> "Horror"
                10402 -> "Music"
                9648 -> "Mystery"
                10749 -> "Romance"
                878 -> "Science Fiction"
                10770 -> "TV Movie"
                53 -> "Thriller"
                10752 -> "War"
                37 -> "Western"
                10759 -> "Action & Adventure"
                10762 -> "Kids"
                10763 -> "News"
                10764 -> "Reality"
                10765 -> "Sci-Fi & Fantasy"
                10766 -> "Soap"
                10767 -> "Talk"
                10768 -> "War & Politics"
                else -> null
            }
        } ?: emptyList()
        
        return VideoContent(
            id = id.toString(),
            title = contentTitle,
            type = contentType,
            tmdbId = id.toString(),
            overview = overview,
            posterUrl = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
            backdropUrl = backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" },
            logoUrl = logoPath?.let { "https://image.tmdb.org/t/p/w500$it" },
            voteAverage = voteAverage,
            releaseDate = contentReleaseDate,
            runtime = contentRuntime,
            genres = genreNames,
            popularity = popularity
        )
    }
}
