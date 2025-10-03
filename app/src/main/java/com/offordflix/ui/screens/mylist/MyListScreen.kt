package com.offordflix.ui.screens.mylist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offordflix.domain.model.VideoContent
import com.offordflix.domain.model.ContentType
import com.offordflix.ui.components.ContentCard

/**
 * My List screen displaying user's saved content
 */
@Composable
fun MyListScreen(
    onNavigateToPlayer: (VideoContent) -> Unit,
    onNavigateToDetails: (VideoContent) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Mock watchlist data - replace with actual data from ViewModel
    val watchlist = remember {
        listOf(
            VideoContent(
                id = "saved1",
                title = "Saved Movie 1",
                type = ContentType.MOVIE,
                tmdbId = "saved1",
                overview = "A movie saved to watch later",
                posterUrl = "",
                backdropUrl = "",
                voteAverage = 8.5,
                releaseDate = "2023-01-01",
                runtime = 120,
                genres = listOf("Action", "Adventure")
            ),
            VideoContent(
                id = "saved2",
                title = "Saved Show 1",
                type = ContentType.TV_SHOW,
                tmdbId = "saved2",
                overview = "A TV show saved to watch later",
                posterUrl = "",
                backdropUrl = "",
                voteAverage = 8.2,
                releaseDate = "2022-01-01",
                runtime = 45,
                genres = listOf("Drama", "Mystery")
            )
            // Add more saved content as needed
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        // Header
        Text(
            text = "My List",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        if (watchlist.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your list is empty",
                        color = Color.Gray,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add movies and shows you want to watch later",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Content grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 200.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(watchlist) { content ->
                    ContentCard(
                        content = content,
                        onClick = { onNavigateToPlayer(content) },
                        modifier = Modifier.height(300.dp)
                    )
                }
            }
        }
    }
}
