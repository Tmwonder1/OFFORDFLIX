package com.offordflix.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import android.util.Log
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

/**
 * Navigation drawer state management
 */
@Stable
class NavigationDrawerState {
    private var _isExpanded by mutableStateOf(false)
    val isExpanded: Boolean get() = _isExpanded
    
    fun expand() {
        _isExpanded = true
    }
    
    fun collapse() {
        _isExpanded = false
    }
    
    fun toggle() {
        _isExpanded = !_isExpanded
    }
}

/**
 * Remember navigation drawer state
 */
@Composable
fun rememberNavigationDrawerState(): NavigationDrawerState {
    return remember { NavigationDrawerState() }
}

/**
 * Navigation item data class
 */
data class NavigationItem(
    val icon: ImageVector,
    val label: String,
    val route: String,
    val badge: String? = null
)

/**
 * Modal Navigation Drawer for Android TV
 * 
 * Features:
 * - Modal overlay design that appears on top of content
 * - Edge-less collapsed rail design
 * - Enhanced gradient scrim for expanded state
 * - Expand/collapse with smooth animations
 * - D-pad navigation support
 * - Badge support for notifications
 * - Focus management for TV navigation
 */
@Composable
fun ModalNavigationDrawer(
    drawerState: NavigationDrawerState,
    selectedRoute: String,
    onNavigateToRoute: (String) -> Unit,
    onDrawerStateChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable (Boolean, () -> Unit) -> Unit // Pass drawer expanded state and expand function to content
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    // Navigation items
    val navigationItems = remember {
        val items = listOf(
            NavigationItem(Icons.Filled.Search, "Search", "search"),
            NavigationItem(Icons.Filled.Home, "Home", "home"),
            NavigationItem(Icons.Filled.PlayArrow, "Movies", "movies", badge = "NEW"),
            NavigationItem(Icons.Filled.Star, "TV Shows", "shows"),
            NavigationItem(Icons.Filled.Menu, "Categories", "categories"),
            NavigationItem(Icons.Filled.List, "My List", "mylist")
        )
        Log.d("NavigationDrawer", "Navigation items created:")
        items.forEachIndexed { index, item ->
            Log.d("NavigationDrawer", "  [$index] route=${item.route}, label=${item.label}")
        }
        items
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .onKeyEvent { keyEvent ->
                // Disable global left arrow trigger - let content handle it specifically
                false
            }
    ) {
        // Main content - pass drawer state and expand function so it can manage focus and expansion
        content(drawerState.isExpanded) {
            drawerState.expand()
            onDrawerStateChanged(true)
        }
        
        // Modal overlay with enhanced scrim when expanded
        androidx.compose.animation.AnimatedVisibility(
            visible = drawerState.isExpanded,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            ),
            modifier = Modifier.zIndex(10f)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Expanded navigation drawer with key event handling
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight()
                        .focusable()
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown) {
                                when (keyEvent.key) {
                                    Key.DirectionRight, Key.Back -> {
                                        Log.d("NavigationDrawer", "Right D-pad/Back pressed in drawer, collapsing")
                                        drawerState.collapse()
                                        onDrawerStateChanged(false)
                                        true
                                    }
                                    else -> false
                                }
                            } else false
                        }
                ) {
                    ExpandedNavigationDrawer(
                        navigationItems = navigationItems,
                        selectedRoute = selectedRoute,
                        onNavigateToRoute = onNavigateToRoute,
                        onCollapseRequested = { 
                            drawerState.collapse()
                            onDrawerStateChanged(false)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Enhanced gradient scrim overlay - clicking it collapses drawer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.9f),
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Transparent
                                ),
                                startX = 0f,
                                endX = screenWidth.value * 0.8f
                            )
                        )
                        .clickable {
                            drawerState.collapse()
                            onDrawerStateChanged(false)
                        }
                )
            }
        }
        
        // Always visible collapsed navigation rail at screen edge
        if (!drawerState.isExpanded) {
            CollapsedNavigationRail(
                navigationItems = navigationItems,
                selectedRoute = selectedRoute,
                isDrawerExpanded = drawerState.isExpanded,
                onNavigateToRoute = onNavigateToRoute,
                onExpandRequested = { 
                    drawerState.expand()
                    onDrawerStateChanged(true)
                },
                drawerState = drawerState,
                modifier = Modifier
                    .align(Alignment.CenterStart) // Position at left edge of screen
                    .zIndex(5f)
            )
        }
    }
}

/**
 * Collapsed navigation rail - always visible
 */
@Composable
private fun CollapsedNavigationRail(
    navigationItems: List<NavigationItem>,
    selectedRoute: String,
    isDrawerExpanded: Boolean,
    onNavigateToRoute: (String) -> Unit,
    onExpandRequested: () -> Unit,
    drawerState: NavigationDrawerState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isDrawerExpanded,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(300)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(300)
        ),
        modifier = modifier
    ) {
        val railFocusRequester = remember { FocusRequester() }
        
        // Don't auto-focus navigation rail, let content be focusable first
        // User can manually focus rail by pressing left D-pad
        
        var isRailFocused by remember { mutableStateOf(false) }
        
        Column(
            modifier = Modifier
                .width(64.dp) // Slightly narrower to create more space
                .fillMaxHeight()
                .background(
                    brush = if (isRailFocused) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.03f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = 48f // Shorter gradient fade
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.6f),
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = 48f // Shorter gradient fade
                        )
                    }
                )
                .padding(vertical = 24.dp)
                .focusRequester(railFocusRequester)
                .focusable()
                .onFocusChanged { isRailFocused = it.isFocused }
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.DirectionLeft, Key.Enter -> {
                                onExpandRequested()
                                true
                            }
                            else -> false
                        }
                    } else false
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App logo/profile section
            NavigationRailItem(
                icon = Icons.Filled.AccountCircle,
                isSelected = false,
                onClick = onExpandRequested,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Navigation items
            navigationItems.forEach { item ->
                NavigationRailItem(
                    icon = item.icon,
                    isSelected = selectedRoute == item.route,
                    badge = item.badge,
                    onClick = { 
                        onNavigateToRoute(item.route)
                        onExpandRequested()
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Settings at bottom
            NavigationRailItem(
                icon = Icons.Filled.Settings,
                isSelected = selectedRoute == "settings",
                onClick = { onNavigateToRoute("settings") },
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

/**
 * Expanded navigation drawer
 */
@Composable
private fun ExpandedNavigationDrawer(
    navigationItems: List<NavigationItem>,
    selectedRoute: String,
    onNavigateToRoute: (String) -> Unit,
    onCollapseRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focusedItemIndex by remember { mutableStateOf(0) }
    val totalItems = navigationItems.size + 2 // +2 for profile and settings
    val drawerFocusRequester = remember { FocusRequester() }
    
    // Auto-focus drawer when it opens
    LaunchedEffect(Unit) {
        Log.d("NavigationDrawer", "Drawer opened, requesting drawer focus...")
        delay(100)
        try {
            drawerFocusRequester.requestFocus()
            Log.d("NavigationDrawer", "Drawer focus requested successfully")
        } catch (e: Exception) {
            Log.e("NavigationDrawer", "Failed to request drawer focus", e)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.95f),
                        Color.Black.copy(alpha = 0.85f),
                        Color.Black.copy(alpha = 0.6f),
                        Color.Transparent
                    ),
                    startX = 0f,
                    endX = 320f
                )
            )
            .padding(vertical = 24.dp, horizontal = 16.dp)
            .focusRequester(drawerFocusRequester)
            .focusable()
            .onFocusChanged { focusState ->
                Log.d("NavigationDrawer", "Drawer focus changed: isFocused=${focusState.isFocused}, hasFocus=${focusState.hasFocus}")
            }
            .onKeyEvent { keyEvent ->
                Log.d("NavigationDrawer", "Drawer received key event: ${keyEvent.key}")
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionUp -> {
                            if (focusedItemIndex > 0) {
                                focusedItemIndex--
                                Log.d("NavigationDrawer", "Moved up to item $focusedItemIndex")
                            }
                            true
                        }
                        Key.DirectionDown -> {
                            if (focusedItemIndex < totalItems - 1) {
                                focusedItemIndex++
                                Log.d("NavigationDrawer", "Moved down to item $focusedItemIndex")
                            }
                            true
                        }
                        Key.Enter, Key.DirectionCenter -> {
                            Log.d("NavigationDrawer", "Enter/Center pressed on drawer, focusedItemIndex=$focusedItemIndex, totalItems=$totalItems")
                            // Handle selection based on focused item
                            when (focusedItemIndex) {
                                0 -> { 
                                    Log.d("NavigationDrawer", "Profile selected")
                                }
                                in 1..navigationItems.size -> {
                                    val itemIndex = focusedItemIndex - 1
                                    Log.d("NavigationDrawer", "Keyboard selection: focusedItemIndex=$focusedItemIndex, itemIndex=$itemIndex, navigationItems.size=${navigationItems.size}")
                                    
                                    if (itemIndex >= 0 && itemIndex < navigationItems.size) {
                                        val item = navigationItems[itemIndex]
                                        Log.d("NavigationDrawer", "Navigation item selected via keyboard: route=${item.route}, label=${item.label}")
                                        onNavigateToRoute(item.route)
                                    } else {
                                        Log.e("NavigationDrawer", "Invalid item index: $itemIndex for navigationItems.size=${navigationItems.size}")
                                    }
                                    onCollapseRequested()
                                }
                                totalItems - 1 -> {
                                    Log.d("NavigationDrawer", "Settings selected")
                                    onNavigateToRoute("settings")
                                    onCollapseRequested()
                                }
                            }
                            true
                        }
                        else -> {
                            Log.d("NavigationDrawer", "Unhandled key event: ${keyEvent.key}")
                            false
                        }
                    }
                } else false
            }
    ) {
        // Top section - Profile/App logo
        SimpleNavigationDrawerItem(
            icon = Icons.Filled.AccountCircle,
            label = "Ashley Miller",
            subtitle = "Switch account",
            isSelected = false,
            isFocused = focusedItemIndex == 0,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Navigation items
        navigationItems.forEachIndexed { index, item ->
            Log.d("NavigationDrawer", "Creating navigation item [$index]: route=${item.route}, label=${item.label}, isFocused=${focusedItemIndex == index + 1}")
            SimpleNavigationDrawerItem(
                icon = item.icon,
                label = item.label,
                badge = item.badge,
                isSelected = selectedRoute == item.route,
                isFocused = focusedItemIndex == index + 1,
                onClick = {
                    Log.d("NavigationDrawer", "Navigation item clicked via onClick: index=$index, route=${item.route}, label=${item.label}")
                    onNavigateToRoute(item.route)
                    onCollapseRequested()
                },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Bottom section - Settings
        SimpleNavigationDrawerItem(
            icon = Icons.Filled.Settings,
            label = "Settings",
            isSelected = selectedRoute == "settings",
            isFocused = focusedItemIndex == totalItems - 1,
            onClick = {
                Log.d("NavigationDrawer", "Settings clicked")
                onNavigateToRoute("settings")
                onCollapseRequested()
            },
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

/**
 * Navigation rail item for collapsed state
 */
@Composable
private fun NavigationRailItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> Color.White.copy(alpha = 0.2f) // Active indicator
                    isFocused -> Color.White.copy(alpha = 0.1f)
                    else -> Color.Transparent
                }
            )
            .focusRequester(focusRequester)
            .focusable()
            .clickable { onClick() }
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                    onClick()
                    true
                } else false
            },
        contentAlignment = Alignment.Center
    ) {
        // Active indicator dot
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-4).dp)
                    .size(4.dp)
                    .background(
                        Color.White,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        
        // Badge
        badge?.let {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
            ) {
                Text(
                    text = it,
                    fontSize = 8.sp,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Simple navigation drawer item that doesn't handle focus itself
 */
@Composable
private fun SimpleNavigationDrawerItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badge: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> Color.White.copy(alpha = 0.2f) // Active indicator
                    isFocused -> Color.White.copy(alpha = 0.15f) // Focus indicator
                    else -> Color.Transparent
                }
            )
            // Remove clickable to prevent focus issues - all navigation handled by parent drawer
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Active indicator line
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .background(
                        Color.White,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = if (isSelected) Color.White else Color.Gray,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            subtitle?.let {
                Text(
                    text = it,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
        
        // Badge
        badge?.let {
            Badge(
                containerColor = Color.Red,
                contentColor = Color.White
            ) {
                Text(
                    text = it,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Navigation drawer item for expanded state (original)
 */
@Composable
private fun NavigationDrawerItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badge: String? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> Color.White.copy(alpha = 0.2f) // Active indicator
                    isFocused -> Color.White.copy(alpha = 0.1f)
                    else -> Color.Transparent
                }
            )
            .focusRequester(focusRequester)
            .focusable()
            .clickable { onClick() }
            .onFocusChanged { 
                isFocused = it.isFocused
                onFocusChanged(it.isFocused)
            }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.Enter -> {
                            onClick()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Active indicator line
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .background(
                        Color.White,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = if (isSelected) Color.White else Color.Gray,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            subtitle?.let {
                Text(
                    text = it,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
        
        // Badge
        badge?.let {
            Badge(
                containerColor = Color.Red,
                contentColor = Color.White
            ) {
                Text(
                    text = it,
                    fontSize = 10.sp
                )
            }
        }
    }
}
