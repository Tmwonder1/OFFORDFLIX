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

package com.google.jetstream.presentation.screens.videoPlayer

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.repositories.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class VideoPlayerScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: MovieRepository,
) : ViewModel() {
    val uiState = savedStateHandle
        .getStateFlow<String?>(VideoPlayerScreen.MovieIdBundleKey, null)
        .map { id ->
            if (id == null) {
                VideoPlayerScreenUiState.Error
            } else {
                try {
                    println("VideoPlayerScreenViewModel: Processing ID: $id")
                    val details = if (id.contains("_S") && id.contains("E")) {
                        // This is a TV show with season/episode format: "tvShowId_S1E1"
                        val parts = id.split("_S")
                        val tvShowId = parts[0]
                        val seasonEpisodePart = parts[1] // "1E1"
                        val seasonEpisodeParts = seasonEpisodePart.split("E")
                        val season = seasonEpisodeParts[0].toIntOrNull() ?: 1
                        val episode = seasonEpisodeParts[1].toIntOrNull() ?: 1
                        
                        println("VideoPlayerScreenViewModel: TV Show - ID: $tvShowId, Season: $season, Episode: $episode")
                        repository.getTvShowDetails(tvShowId, season, episode)
                    } else {
                        // Regular movie or TV show (S01E01 by default)
                        println("VideoPlayerScreenViewModel: Movie/TV Show - ID: $id")
                        repository.getMovieDetails(movieId = id)
                    }
                    println("VideoPlayerScreenViewModel: Successfully loaded details for: ${details.name}")
                    VideoPlayerScreenUiState.Done(movieDetails = details)
                } catch (e: Exception) {
                    println("VideoPlayer ViewModel Error: ${e.message}")
                    e.printStackTrace()
                    VideoPlayerScreenUiState.Error
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VideoPlayerScreenUiState.Loading
        )
}

@Immutable
sealed class VideoPlayerScreenUiState {
    data object Loading : VideoPlayerScreenUiState()
    data object Error : VideoPlayerScreenUiState()
    data class Done(val movieDetails: MovieDetails) : VideoPlayerScreenUiState()
}
