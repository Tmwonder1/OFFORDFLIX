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
import com.google.jetstream.data.entities.MovieCategory
import com.google.jetstream.data.models.tmdb.toMovieCategory
import com.google.jetstream.data.util.ApiConfig
import javax.inject.Inject

class MovieCategoryDataSource @Inject constructor(
    private val tmdbApiService: TmdbApiService
) {

    private val apiKey = ApiConfig.TMDB_API_KEY

    suspend fun getMovieCategoryList(): List<MovieCategory> {
        return try {
            val response = tmdbApiService.getMovieGenres(apiKey)
            response.genres.map { it.toMovieCategory() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
