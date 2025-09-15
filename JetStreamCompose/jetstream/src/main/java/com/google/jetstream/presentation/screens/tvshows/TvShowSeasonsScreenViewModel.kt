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

package com.google.jetstream.presentation.screens.tvshows

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.api.TmdbApiService
import com.google.jetstream.data.util.ApiConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TvShowSeasonsScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tmdbApiService: TmdbApiService
) : ViewModel() {

    private val tvShowId = savedStateHandle.get<String>(TvShowSeasonsScreen.TvShowIdBundleKey) ?: ""
    private val _selectedSeason = MutableStateFlow(1)
    private val _tvShowData = MutableStateFlow<TvShowData?>(null)

    init {
        loadTvShowData()
    }

    val uiState: StateFlow<TvShowSeasonsScreenUiState> = combine(
        _tvShowData,
        _selectedSeason
    ) { tvShowData, selectedSeason ->
        if (tvShowData == null) {
            TvShowSeasonsScreenUiState.Loading
        } else {
            val episodes = tvShowData.seasons.find { it.number == selectedSeason }?.let { season ->
                // Generate mock episodes for now - in a real app you'd call TMDB season details API
                (1..season.episodeCount).map { episodeNumber ->
                    Episode(
                        number = episodeNumber,
                        name = "Episode $episodeNumber",
                        overview = "Episode $episodeNumber of ${season.name}. Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                        stillPath = null
                    )
                }
            } ?: emptyList()

            TvShowSeasonsScreenUiState.Done(
                tvShowName = tvShowData.name,
                seasons = tvShowData.seasons,
                episodes = episodes,
                selectedSeason = selectedSeason
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TvShowSeasonsScreenUiState.Loading
    )

    fun selectSeason(seasonNumber: Int) {
        _selectedSeason.value = seasonNumber
    }

    private fun loadTvShowData() {
        viewModelScope.launch {
            try {
                println("TvShowSeasonsScreenViewModel: Loading TV show data for ID: $tvShowId")
                val tmdbId = tvShowId.toIntOrNull() ?: run {
                    println("TvShowSeasonsScreenViewModel: Invalid TMDB ID: $tvShowId")
                    return@launch
                }
                val tvShowDetails = tmdbApiService.getTvShowDetails(tmdbId, ApiConfig.TMDB_API_KEY)
                
                // Generate mock seasons data - in a real app you'd get this from TMDB
                val seasons = (1..tvShowDetails.numberOfSeasons).map { seasonNumber ->
                    Season(
                        number = seasonNumber,
                        name = "Season $seasonNumber",
                        episodeCount = when (seasonNumber) {
                            1 -> 10
                            2 -> 12
                            3 -> 8
                            else -> 10
                        }
                    )
                }

                _tvShowData.value = TvShowData(
                    name = tvShowDetails.name,
                    seasons = seasons
                )
                println("TvShowSeasonsScreenViewModel: Successfully loaded ${tvShowDetails.name} with ${seasons.size} seasons")
            } catch (e: Exception) {
                println("TvShowSeasonsScreenViewModel: Failed to load TV show data: ${e.message}")
                e.printStackTrace()
                // Fallback to mock data
                _tvShowData.value = TvShowData(
                    name = "Unknown TV Show",
                    seasons = listOf(
                        Season(1, "Season 1", 10),
                        Season(2, "Season 2", 12)
                    )
                )
                println("TvShowSeasonsScreenViewModel: Using fallback data")
            }
        }
    }
}

data class TvShowData(
    val name: String,
    val seasons: List<Season>
)

sealed class TvShowSeasonsScreenUiState {
    data object Loading : TvShowSeasonsScreenUiState()
    data object Error : TvShowSeasonsScreenUiState()
    data class Done(
        val tvShowName: String,
        val seasons: List<Season>,
        val episodes: List<Episode>,
        val selectedSeason: Int
    ) : TvShowSeasonsScreenUiState()
}

