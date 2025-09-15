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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.JetStreamButtonShape

object TvShowSeasonsScreen {
    const val TvShowIdBundleKey = "tvShowId"
}

data class Season(
    val number: Int,
    val name: String,
    val episodeCount: Int
)

data class Episode(
    val number: Int,
    val name: String,
    val overview: String,
    val stillPath: String?
)

@Composable
fun TvShowSeasonsScreen(
    onBackPressed: () -> Unit,
    onEpisodeSelected: (season: Int, episode: Int) -> Unit,
    tvShowSeasonsScreenViewModel: TvShowSeasonsScreenViewModel = hiltViewModel()
) {
    val uiState by tvShowSeasonsScreenViewModel.uiState.collectAsStateWithLifecycle()

    when (val s = uiState) {
        is TvShowSeasonsScreenUiState.Loading -> {
            Loading(modifier = Modifier.fillMaxSize())
        }

        is TvShowSeasonsScreenUiState.Error -> {
            Error(modifier = Modifier.fillMaxSize())
        }

        is TvShowSeasonsScreenUiState.Done -> {
            SeasonsContent(
                tvShowName = s.tvShowName,
                seasons = s.seasons,
                episodes = s.episodes,
                selectedSeason = s.selectedSeason,
                onSeasonSelected = tvShowSeasonsScreenViewModel::selectSeason,
                onEpisodeSelected = onEpisodeSelected,
                onBackPressed = onBackPressed,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeasonsContent(
    tvShowName: String,
    seasons: List<Season>,
    episodes: List<Episode>,
    selectedSeason: Int,
    onSeasonSelected: (Int) -> Unit,
    onEpisodeSelected: (season: Int, episode: Int) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val childPadding = rememberChildPadding()

    BackHandler(onBack = onBackPressed)
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = childPadding.start,
            end = childPadding.end,
            top = childPadding.top,
            bottom = 135.dp
        )
    ) {
        item {
            Text(
                text = tvShowName,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        item {
            Text(
                text = "Seasons",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                itemsIndexed(seasons) { index, season ->
                    SeasonCard(
                        season = season,
                        isSelected = season.number == selectedSeason,
                        onClick = { onSeasonSelected(season.number) }
                    )
                }
            }
        }

        item {
            Text(
                text = "Season $selectedSeason Episodes",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(episodes) { episode ->
            EpisodeCard(
                episode = episode,
                seasonNumber = selectedSeason,
                onPlayClick = { 
                    println("EpisodeCard: Play clicked for S${selectedSeason}E${episode.number}")
                    onEpisodeSelected(selectedSeason, episode.number)
                },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeasonCard(
    season: Season,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(120.dp),
        colors = if (isSelected) {
            CardDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            CardDefaults.colors()
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = season.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${season.episodeCount} episodes",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun EpisodeCard(
    episode: Episode,
    seasonNumber: Int,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isButtonFocused by remember { mutableStateOf(false) }

    // Make the entire card clickable and focusable for better TV UX
    Card(
        onClick = {
            println("EpisodeCard: Card clicked for S${seasonNumber}E${episode.number}")
            onPlayClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                isButtonFocused = focusState.isFocused || focusState.hasFocus
                println("EpisodeCard: Focus changed - isFocused: ${focusState.isFocused}, hasFocus: ${focusState.hasFocus}")
            },
        colors = CardDefaults.colors(
            containerColor = if (isButtonFocused) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "S${seasonNumber}E${episode.number} • ${episode.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isButtonFocused) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = episode.overview,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = if (isButtonFocused) 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Keep the button for visual consistency but make it non-focusable
            Button(
                onClick = {
                    println("EpisodeCard Button: Play clicked for S${seasonNumber}E${episode.number}")
                    onPlayClick()
                },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                shape = ButtonDefaults.shape(shape = JetStreamButtonShape),
                enabled = false // Make it non-focusable since the card handles the click
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = "Play Episode"
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "Play",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
