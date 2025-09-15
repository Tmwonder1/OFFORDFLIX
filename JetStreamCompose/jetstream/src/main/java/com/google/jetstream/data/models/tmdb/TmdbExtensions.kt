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

import com.google.jetstream.data.entities.ContentType
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieCast
import com.google.jetstream.data.entities.MovieCategory
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.entities.ThumbnailType
import com.google.jetstream.data.util.ApiConfig

fun TmdbMovie.toMovie(thumbnailType: ThumbnailType = ThumbnailType.Standard): Movie {
    val imageUrl = when (thumbnailType) {
        ThumbnailType.Standard -> posterPath?.let { "${ApiConfig.TMDB_IMAGE_BASE_URL}${ApiConfig.POSTER_SIZE}$it" } ?: ""
        ThumbnailType.Long -> backdropPath?.let { "${ApiConfig.TMDB_IMAGE_BASE_URL}${ApiConfig.BACKDROP_SIZE}$it" } ?: ""
    }
    
    return Movie(
        id = id.toString(),
        videoUri = "", // Will be populated from CinePro API when playing
        subtitleUri = null, // Will be populated from CinePro API when playing
        posterUri = imageUrl,
        name = title,
        description = overview,
        contentType = ContentType.MOVIE
    )
}

fun TmdbTvShow.toMovie(thumbnailType: ThumbnailType = ThumbnailType.Standard): Movie {
    val imageUrl = when (thumbnailType) {
        ThumbnailType.Standard -> posterPath?.let { "${ApiConfig.TMDB_IMAGE_BASE_URL}${ApiConfig.POSTER_SIZE}$it" } ?: ""
        ThumbnailType.Long -> backdropPath?.let { "${ApiConfig.TMDB_IMAGE_BASE_URL}${ApiConfig.BACKDROP_SIZE}$it" } ?: ""
    }
    
    return Movie(
        id = id.toString(),
        videoUri = "", // Will be populated from CinePro API when playing
        subtitleUri = null, // Will be populated from CinePro API when playing
        posterUri = imageUrl,
        name = name,
        description = overview,
        contentType = ContentType.TV_SHOW
    )
}

fun TmdbMovieDetails.toMovieDetails(videoUri: String = "", subtitleUri: String? = null): MovieDetails {
    val posterUrl = posterPath?.let { "${ApiConfig.TMDB_IMAGE_BASE_URL}${ApiConfig.POSTER_SIZE}$it" } ?: ""
    val backdropUrl = backdropPath?.let { "${ApiConfig.TMDB_IMAGE_BASE_URL}${ApiConfig.BACKDROP_SIZE}$it" } ?: ""
    
    val directors = credits?.crew?.filter { it.job == "Director" }?.map { it.name }?.joinToString(", ") ?: ""
    val writers = credits?.crew?.filter { it.department == "Writing" }?.take(3)?.map { it.name }?.joinToString(", ") ?: ""
    
    return MovieDetails(
        id = id.toString(),
        videoUri = videoUri,
        subtitleUri = subtitleUri,
        posterUri = posterUrl,
        name = title,
        description = overview,
        pgRating = "PG-13", // TMDB doesn't provide this directly
        releaseDate = releaseDate,
        categories = genres.map { it.name },
        duration = runtime?.let { "${it}m" } ?: "",
        director = directors,
        screenplay = writers,
        music = "", // Would need additional API call to get composer info
        castAndCrew = credits?.cast?.take(10)?.map { castMember ->
            MovieCast(
                id = castMember.id.toString(),
                characterName = castMember.character,
                realName = castMember.name,
                avatarUrl = castMember.profilePath?.let { "${ApiConfig.TMDB_IMAGE_BASE_URL}${ApiConfig.PROFILE_SIZE}$it" } ?: ""
            )
        } ?: emptyList(),
        status = status,
        originalLanguage = originalLanguage,
        budget = if (budget > 0) "$${budget / 1000000}M" else "",
        revenue = if (revenue > 0) "$${revenue / 1000000}M" else "",
        similarMovies = similar?.results?.take(5)?.map { it.toMovie() } ?: emptyList(),
        reviewsAndRatings = emptyList(), // Would need additional API calls for reviews
        contentType = ContentType.MOVIE
    )
}

fun TmdbTvDetails.toMovieDetails(videoUri: String = "", subtitleUri: String? = null): MovieDetails {
    val posterUrl = posterPath?.let { "${ApiConfig.TMDB_IMAGE_BASE_URL}${ApiConfig.POSTER_SIZE}$it" } ?: ""
    val backdropUrl = backdropPath?.let { "${ApiConfig.TMDB_IMAGE_BASE_URL}${ApiConfig.BACKDROP_SIZE}$it" } ?: ""
    
    val creators = credits?.crew?.filter { it.job == "Creator" || it.job == "Executive Producer" }?.take(3)?.map { it.name }?.joinToString(", ") ?: ""
    val writers = credits?.crew?.filter { it.department == "Writing" }?.take(3)?.map { it.name }?.joinToString(", ") ?: ""
    
    return MovieDetails(
        id = id.toString(),
        videoUri = videoUri,
        subtitleUri = subtitleUri,
        posterUri = posterUrl,
        name = name,
        description = overview,
        pgRating = "TV-PG", // Default TV rating
        releaseDate = firstAirDate,
        categories = genres.map { it.name },
        duration = "$numberOfSeasons Season${if (numberOfSeasons > 1) "s" else ""} • $numberOfEpisodes Episodes",
        director = creators,
        screenplay = writers,
        music = "", // Would need additional API call to get composer info
        castAndCrew = credits?.cast?.take(10)?.map { castMember ->
            MovieCast(
                id = castMember.id.toString(),
                characterName = castMember.character,
                realName = castMember.name,
                avatarUrl = castMember.profilePath?.let { "${ApiConfig.TMDB_IMAGE_BASE_URL}${ApiConfig.PROFILE_SIZE}$it" } ?: ""
            )
        } ?: emptyList(),
        status = status,
        originalLanguage = originalLanguage,
        budget = "", // TV shows don't typically have public budget info
        revenue = "", // TV shows don't typically have public revenue info
        similarMovies = similar?.results?.take(5)?.map { it.toMovie() } ?: emptyList(),
        reviewsAndRatings = emptyList(), // Would need additional API calls for reviews
        contentType = ContentType.TV_SHOW
    )
}

fun TmdbGenre.toMovieCategory(): MovieCategory {
    return MovieCategory(
        id = id.toString(),
        name = name
    )
}
