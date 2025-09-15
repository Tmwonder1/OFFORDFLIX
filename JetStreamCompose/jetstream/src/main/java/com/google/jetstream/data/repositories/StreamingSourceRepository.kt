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

import com.google.jetstream.data.api.CineProApiService
import com.google.jetstream.data.models.cinepro.CineProResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamingSourceRepository @Inject constructor(
    private val cineProApiService: CineProApiService
) {

    suspend fun getMovieStreamingSources(tmdbId: Int): Result<CineProResponse> {
        return try {
            val response = cineProApiService.getMovieStreamingSources(tmdbId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTvShowStreamingSources(
        tmdbId: Int,
        season: Int,
        episode: Int
    ): Result<CineProResponse> {
        return try {
            val response = cineProApiService.getTvShowStreamingSources(tmdbId, season, episode)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get the best streaming source based on quality preference
     * Priority: mp4 > hls > embed
     */
    fun getBestStreamingSource(response: CineProResponse): String? {
        val files = response.files
        
        // Prefer MP4 files
        files.find { it.type == "mp4" }?.let { return it.file }
        
        // Then HLS files
        files.find { it.type == "hls" }?.let { return it.file }
        
        // Finally embed files
        files.find { it.type == "embed" }?.let { return it.file }
        
        return null
    }

    /**
     * Get subtitles for the content
     */
    fun getSubtitles(response: CineProResponse): List<String> {
        return response.subtitles.map { it.url }
    }
}
