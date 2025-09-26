package com.offordflix.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * TMDB API Data Transfer Objects.
 * 
 * Response models for The Movie Database API integration.
 */

@Serializable
data class TmdbMovieResponse(
    val page: Int,
    val results: List<TmdbMovieDto>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int
)

@Serializable
data class TmdbTvResponse(
    val page: Int,
    val results: List<TmdbTvDto>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int
)

@Serializable
data class TmdbMultiResponse(
    val page: Int,
    val results: List<TmdbMultiDto>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int
)

@Serializable
data class TmdbMovieDto(
    val id: Int,
    val title: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList(),
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    val popularity: Double = 0.0,
    val adult: Boolean = false,
    @SerialName("original_language") val originalLanguage: String = "en",
    @SerialName("original_title") val originalTitle: String = title
)

@Serializable
data class TmdbTvDto(
    val id: Int,
    val name: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList(),
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    val popularity: Double = 0.0,
    @SerialName("original_language") val originalLanguage: String = "en",
    @SerialName("original_name") val originalName: String = name,
    @SerialName("origin_country") val originCountry: List<String> = emptyList()
)

@Serializable
data class TmdbMultiDto(
    val id: Int,
    @SerialName("media_type") val mediaType: String, // "movie" or "tv"
    val title: String? = null, // For movies
    val name: String? = null, // For TV shows
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null, // For movies
    @SerialName("first_air_date") val firstAirDate: String? = null, // For TV shows
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList(),
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    val popularity: Double = 0.0,
    val adult: Boolean = false,
    @SerialName("original_language") val originalLanguage: String = "en"
)

@Serializable
data class TmdbMovieDetailsDto(
    val id: Int,
    val title: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val genres: List<TmdbGenreDto> = emptyList(),
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    val popularity: Double = 0.0,
    val runtime: Int? = null,
    val budget: Long = 0,
    val revenue: Long = 0,
    @SerialName("imdb_id") val imdbId: String? = null,
    val tagline: String? = null,
    val status: String = "",
    @SerialName("original_language") val originalLanguage: String = "en",
    @SerialName("original_title") val originalTitle: String = title,
    @SerialName("production_companies") val productionCompanies: List<TmdbCompanyDto> = emptyList(),
    @SerialName("production_countries") val productionCountries: List<TmdbCountryDto> = emptyList(),
    @SerialName("spoken_languages") val spokenLanguages: List<TmdbLanguageDto> = emptyList(),
    val videos: TmdbVideosResponse? = null,
    val credits: TmdbCreditsDto? = null,
    val similar: TmdbMovieResponse? = null
)

@Serializable
data class TmdbTvDetailsDto(
    val id: Int,
    val name: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("last_air_date") val lastAirDate: String? = null,
    val genres: List<TmdbGenreDto> = emptyList(),
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    val popularity: Double = 0.0,
    @SerialName("number_of_episodes") val numberOfEpisodes: Int = 0,
    @SerialName("number_of_seasons") val numberOfSeasons: Int = 0,
    @SerialName("episode_run_time") val episodeRunTime: List<Int> = emptyList(),
    val status: String = "",
    val type: String = "",
    @SerialName("original_language") val originalLanguage: String = "en",
    @SerialName("original_name") val originalName: String = name,
    @SerialName("created_by") val createdBy: List<TmdbCreatorDto> = emptyList(),
    @SerialName("production_companies") val productionCompanies: List<TmdbCompanyDto> = emptyList(),
    @SerialName("production_countries") val productionCountries: List<TmdbCountryDto> = emptyList(),
    @SerialName("spoken_languages") val spokenLanguages: List<TmdbLanguageDto> = emptyList(),
    val networks: List<TmdbNetworkDto> = emptyList(),
    val seasons: List<TmdbSeasonDto> = emptyList(),
    val videos: TmdbVideosResponse? = null,
    val credits: TmdbCreditsDto? = null,
    val similar: TmdbTvResponse? = null
)

@Serializable
data class TmdbGenreDto(
    val id: Int,
    val name: String
)

@Serializable
data class TmdbGenreResponse(
    val genres: List<TmdbGenreDto>
)

@Serializable
data class TmdbCompanyDto(
    val id: Int,
    val name: String,
    @SerialName("logo_path") val logoPath: String? = null,
    @SerialName("origin_country") val originCountry: String = ""
)

@Serializable
data class TmdbCountryDto(
    @SerialName("iso_3166_1") val iso31661: String,
    val name: String
)

@Serializable
data class TmdbLanguageDto(
    @SerialName("iso_639_1") val iso6391: String,
    @SerialName("english_name") val englishName: String,
    val name: String
)

@Serializable
data class TmdbCreatorDto(
    val id: Int,
    val name: String,
    @SerialName("profile_path") val profilePath: String? = null
)

@Serializable
data class TmdbNetworkDto(
    val id: Int,
    val name: String,
    @SerialName("logo_path") val logoPath: String? = null,
    @SerialName("origin_country") val originCountry: String = ""
)

@Serializable
data class TmdbSeasonDto(
    val id: Int,
    @SerialName("season_number") val seasonNumber: Int,
    val name: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("air_date") val airDate: String? = null,
    @SerialName("episode_count") val episodeCount: Int = 0,
    val episodes: List<TmdbEpisodeDto> = emptyList()
)

@Serializable
data class TmdbEpisodeDto(
    val id: Int,
    @SerialName("episode_number") val episodeNumber: Int,
    val name: String,
    val overview: String,
    @SerialName("still_path") val stillPath: String? = null,
    @SerialName("air_date") val airDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    val runtime: Int? = null
)

@Serializable
data class TmdbVideosResponse(
    val results: List<TmdbVideoDto>
)

@Serializable
data class TmdbVideoDto(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String,
    val official: Boolean = false,
    @SerialName("published_at") val publishedAt: String? = null
)

@Serializable
data class TmdbCreditsDto(
    val cast: List<TmdbCastDto> = emptyList(),
    val crew: List<TmdbCrewDto> = emptyList()
)

@Serializable
data class TmdbCastDto(
    val id: Int,
    val name: String,
    val character: String,
    @SerialName("profile_path") val profilePath: String? = null,
    val order: Int = 0
)

@Serializable
data class TmdbCrewDto(
    val id: Int,
    val name: String,
    val job: String,
    val department: String,
    @SerialName("profile_path") val profilePath: String? = null
)

