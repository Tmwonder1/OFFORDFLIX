package com.offordflix.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offordflix.domain.model.VideoContent

/**
 * Categories screen displaying content categories/genres
 */
@Composable
fun CategoriesScreen(
    onNavigateToCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Mock categories data
    val categories = remember {
        listOf(
            "Action", "Adventure", "Comedy", "Crime", "Documentary",
            "Drama", "Family", "Fantasy", "Horror", "Mystery",
            "Romance", "Sci-Fi", "Thriller", "War", "Western"
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
            text = "Categories",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Categories grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 200.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(categories) { category ->
                CategoryCard(
                    category = category,
                    onClick = { onNavigateToCategory(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) 
                Color.White.copy(alpha = 0.2f) 
            else 
                Color.Gray.copy(alpha = 0.2f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}




