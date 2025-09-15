/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jetstream.data.models.tmdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbMovieResponse(
    val page: Int,
    val results: List<TmdbMovie>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int
)

@Serializable
data class TmdbMovie(
    val id: Int,
    val title: String,
    @SerialName("original_title") val originalTitle: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("release_date") val releaseDate: String,
    @SerialName("genre_ids") val genreIds: List<Int>,
    @SerialName("vote_average") val voteAverage: Double,
    @SerialName("vote_count") val voteCount: Int,
    val popularity: Double,
    val adult: Boolean,
    val video: Boolean,
    @SerialName("original_language") val originalLanguage: String
)

@Serializable
data class TmdbMovieDetails(
    val id: Int,
    val title: String,
    @SerialName("original_title") val originalTitle: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("release_date") val releaseDate: String,
    val genres: List<TmdbGenre>,
    @SerialName("vote_average") val voteAverage: Double,
    @SerialName("vote_count") val voteCount: Int,
    val popularity: Double,
    val runtime: Int?,
    val budget: Long,
    val revenue: Long,
    val status: String,
    @SerialName("original_language") val originalLanguage: String,
    @SerialName("production_companies") val productionCompanies: List<TmdbProductionCompany>,
    @SerialName("production_countries") val productionCountries: List<TmdbProductionCountry>,
    @SerialName("spoken_languages") val spokenLanguages: List<TmdbSpokenLanguage>,
    val credits: TmdbCredits?,
    val videos: TmdbVideos?,
    val similar: TmdbMovieResponse?
)

@Serializable
data class TmdbGenre(
    val id: Int,
    val name: String
)

@Serializable
data class TmdbGenreResponse(
    val genres: List<TmdbGenre>
)

@Serializable
data class TmdbProductionCompany(
    val id: Int,
    val name: String,
    @SerialName("logo_path") val logoPath: String?,
    @SerialName("origin_country") val originCountry: String
)

@Serializable
data class TmdbProductionCountry(
    @SerialName("iso_3166_1") val iso31661: String,
    val name: String
)

@Serializable
data class TmdbSpokenLanguage(
    @SerialName("english_name") val englishName: String,
    @SerialName("iso_639_1") val iso6391: String,
    val name: String
)

@Serializable
data class TmdbCredits(
    val cast: List<TmdbCastMember>,
    val crew: List<TmdbCrewMember>
)

@Serializable
data class TmdbCastMember(
    val id: Int,
    val name: String,
    val character: String,
    @SerialName("profile_path") val profilePath: String?,
    val order: Int
)

@Serializable
data class TmdbCrewMember(
    val id: Int,
    val name: String,
    val job: String,
    val department: String,
    @SerialName("profile_path") val profilePath: String?
)

@Serializable
data class TmdbVideos(
    val results: List<TmdbVideo>
)

@Serializable
data class TmdbVideo(
    val id: String,
    @SerialName("iso_639_1") val iso6391: String,
    @SerialName("iso_3166_1") val iso31661: String,
    val key: String,
    val name: String,
    val site: String,
    val size: Int,
    val type: String,
    val official: Boolean,
    @SerialName("published_at") val publishedAt: String
)

// TV Show models
@Serializable
data class TmdbTvResponse(
    val page: Int,
    val results: List<TmdbTvShow>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int
)

@Serializable
data class TmdbTvShow(
    val id: Int,
    val name: String,
    @SerialName("original_name") val originalName: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("first_air_date") val firstAirDate: String,
    @SerialName("genre_ids") val genreIds: List<Int>,
    @SerialName("vote_average") val voteAverage: Double,
    @SerialName("vote_count") val voteCount: Int,
    val popularity: Double,
    @SerialName("original_language") val originalLanguage: String,
    @SerialName("origin_country") val originCountry: List<String>
)

@Serializable
data class TmdbTvDetails(
    val id: Int,
    val name: String,
    @SerialName("original_name") val originalName: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("first_air_date") val firstAirDate: String,
    @SerialName("last_air_date") val lastAirDate: String?,
    val genres: List<TmdbGenre>,
    @SerialName("vote_average") val voteAverage: Double,
    @SerialName("vote_count") val voteCount: Int,
    val popularity: Double,
    @SerialName("number_of_seasons") val numberOfSeasons: Int,
    @SerialName("number_of_episodes") val numberOfEpisodes: Int,
    val status: String,
    @SerialName("original_language") val originalLanguage: String,
    @SerialName("production_companies") val productionCompanies: List<TmdbProductionCompany>,
    @SerialName("production_countries") val productionCountries: List<TmdbProductionCountry>,
    @SerialName("spoken_languages") val spokenLanguages: List<TmdbSpokenLanguage>,
    val seasons: List<TmdbSeason>,
    val credits: TmdbCredits?,
    val videos: TmdbVideos?,
    val similar: TmdbTvResponse?
)

@Serializable
data class TmdbSeason(
    val id: Int,
    @SerialName("season_number") val seasonNumber: Int,
    val name: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("air_date") val airDate: String?,
    @SerialName("episode_count") val episodeCount: Int
)
