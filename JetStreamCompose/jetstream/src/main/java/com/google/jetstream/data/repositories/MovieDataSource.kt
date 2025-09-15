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

package com.google.jetstream.data.repositories

import com.google.jetstream.data.api.TmdbApiService
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.ThumbnailType
import com.google.jetstream.data.models.tmdb.toMovie
import com.google.jetstream.data.util.ApiConfig
import javax.inject.Inject

class MovieDataSource @Inject constructor(
    private val tmdbApiService: TmdbApiService
) {

    private val apiKey = ApiConfig.TMDB_API_KEY

    suspend fun getMovieList(thumbnailType: ThumbnailType = ThumbnailType.Standard): List<Movie> {
        return try {
            val response = tmdbApiService.getPopularMovies(apiKey)
            response.results.map { it.toMovie(thumbnailType) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFeaturedMovieList(): List<Movie> {
        return try {
            val response = tmdbApiService.getPopularMovies(apiKey)
            response.results.take(5).map { it.toMovie(ThumbnailType.Long) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTrendingMovieList(): List<Movie> {
        return try {
            val response = tmdbApiService.getTrendingMovies(apiKey)
            response.results.take(10).map { it.toMovie() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTop10MovieList(): List<Movie> {
        return try {
            val response = tmdbApiService.getTopRatedMovies(apiKey)
            response.results.take(10).map { it.toMovie(ThumbnailType.Long) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getNowPlayingMovieList(): List<Movie> {
        return try {
            val response = tmdbApiService.getNowPlayingMovies(apiKey)
            response.results.take(10).map { it.toMovie() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPopularFilmThisWeek(): List<Movie> {
        return try {
            val response = tmdbApiService.getPopularMovies(apiKey, page = 2)
            response.results.take(10).map { it.toMovie() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFavoriteMovieList(): List<Movie> {
        return try {
            val response = tmdbApiService.getTopRatedMovies(apiKey)
            response.results.take(28).map { it.toMovie() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
