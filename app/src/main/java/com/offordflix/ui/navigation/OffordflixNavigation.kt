package com.offordflix.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import kotlinx.coroutines.delay
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.offordflix.ui.screens.splash.SplashScreen
import com.offordflix.ui.screens.profile.ProfileSelectionScreen
import com.offordflix.ui.screens.home.HomeScreen
import com.offordflix.ui.screens.home.NetflixStyleHomeScreen
import com.offordflix.ui.screens.player.PlayerScreen
import com.offordflix.ui.screens.search.SearchScreen
import com.offordflix.ui.screens.movies.MoviesScreen
import com.offordflix.ui.screens.shows.ShowsScreen
import com.offordflix.ui.screens.categories.CategoriesScreen
import com.offordflix.ui.screens.mylist.MyListScreen
import com.offordflix.ui.components.ModalNavigationDrawer
import com.offordflix.ui.components.rememberNavigationDrawerState

/**
 * Navigation routes for the Offordflix Android TV app.
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object ProfileSelection : Screen("profile_selection")
    object Home : Screen("home/{profileId}") {
        fun createRoute(profileId: String) = "home/$profileId"
    }
    object Search : Screen("search")
    object Movies : Screen("movies")
    object Shows : Screen("shows")
    object Categories : Screen("categories")
    object MyList : Screen("mylist")
    object Settings : Screen("settings")
    object Player : Screen("player/{contentId}") {
        fun createRoute(contentId: String) = "player/$contentId"
    }
}

/**
 * Main navigation composable for the Offordflix app.
 * 
 * This sets up the navigation graph with all screens and handles
 * navigation between all app destinations with integrated navigation drawer.
 */
@Composable
fun OffordflixNavigation(
    navController: NavHostController = rememberNavController()
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: Screen.Splash.route
    val drawerState = rememberNavigationDrawerState()
    
    // Track overlay visibility state across all screens
    var isOverlayVisible by remember { mutableStateOf(false) }
    
    // Determine if drawer should be shown (not on splash, profile selection, or player screens)
    val showDrawer = when {
        currentRoute.startsWith(Screen.Splash.route) -> false
        currentRoute.startsWith(Screen.ProfileSelection.route) -> false
        currentRoute.startsWith(Screen.Player.route) -> false
        else -> true
    }
    
    if (showDrawer) {
        // Let content handle navigation first, drawer expansion is handled by content when at leftmost position
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                selectedRoute = getSelectedRoute(currentRoute),
                hideDrawer = isOverlayVisible, // Hide drawer when overlay is visible
                onNavigateToRoute = { route ->
                    Log.d("OffordflixNavigation", "Navigating to route: $route")
                    when (route) {
                        "search" -> {
                            Log.d("OffordflixNavigation", "Navigating to Search screen")
                            navController.navigate(Screen.Search.route) {
                                launchSingleTop = true
                            }
                        }
                        "home" -> {
                            Log.d("OffordflixNavigation", "Navigating to Home screen")
                            // Navigate to home with current profile
                            val profileId = getCurrentProfileId(currentRoute)
                            navController.navigate(Screen.Home.createRoute(profileId)) {
                                launchSingleTop = true
                            }
                        }
                        "movies" -> {
                            Log.d("OffordflixNavigation", "Navigating to Movies screen")
                            navController.navigate(Screen.Movies.route) {
                                launchSingleTop = true
                            }
                        }
                        "shows" -> {
                            Log.d("OffordflixNavigation", "Navigating to Shows screen")
                            navController.navigate(Screen.Shows.route) {
                                launchSingleTop = true
                            }
                        }
                        "categories" -> {
                            Log.d("OffordflixNavigation", "Navigating to Categories screen")
                            navController.navigate(Screen.Categories.route) {
                                launchSingleTop = true
                            }
                        }
                        "mylist" -> {
                            Log.d("OffordflixNavigation", "Navigating to MyList screen")
                            navController.navigate(Screen.MyList.route) {
                                launchSingleTop = true
                            }
                        }
                        "settings" -> {
                            Log.d("OffordflixNavigation", "Navigating to Settings screen")
                            navController.navigate(Screen.Settings.route) {
                                launchSingleTop = true
                            }
                        }
                        else -> {
                            Log.w("OffordflixNavigation", "Unknown route: $route")
                        }
                    }
                }
            ) { isDrawerExpanded, onExpandDrawer ->
                NavigationContent(
                    navController = navController, 
                    isDrawerExpanded = isDrawerExpanded,
                    onExpandDrawer = onExpandDrawer,
                    onOverlayVisibilityChanged = { isVisible -> isOverlayVisible = isVisible }
                )
            }
        }
    } else {
        NavigationContent(navController = navController, isDrawerExpanded = false, onExpandDrawer = {}, onOverlayVisibilityChanged = {})
    }
}

/**
 * Navigation content without drawer wrapper
 */
@Composable
private fun NavigationContent(
    navController: NavHostController, 
    isDrawerExpanded: Boolean = false,
    onExpandDrawer: () -> Unit = {},
    onOverlayVisibilityChanged: (Boolean) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToProfileSelection = {
                    navController.navigate(Screen.ProfileSelection.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ProfileSelection.route) {
            ProfileSelectionScreen(
                onProfileSelected = { profileId ->
                    navController.navigate(Screen.Home.createRoute(profileId)) {
                        popUpTo(Screen.ProfileSelection.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: "default"
            HomeScreenWithDrawerState(
                profileId = profileId,
                isDrawerExpanded = isDrawerExpanded,
                backStackEntry = backStackEntry,
                onNavigateToPlayer = { content: com.offordflix.domain.model.VideoContent ->
                    navController.navigate(Screen.Player.createRoute(content.id))
                },
                onNavigateToDetails = { content: com.offordflix.domain.model.VideoContent ->
                    // TODO: Navigate to content details screen
                },
                onExpandDrawer = onExpandDrawer,
                onOverlayVisibilityChanged = onOverlayVisibilityChanged
            )
        }
        
        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToPlayer = { content: com.offordflix.domain.model.VideoContent ->
                    navController.navigate(Screen.Player.createRoute(content.id))
                },
                onNavigateToDetails = { content: com.offordflix.domain.model.VideoContent ->
                    // TODO: Navigate to content details screen
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                isDrawerExpanded = isDrawerExpanded,
                onExpandDrawer = onExpandDrawer,
                onOverlayVisibilityChanged = onOverlayVisibilityChanged
            )
        }
        
        composable(Screen.Movies.route) { backStackEntry ->
            MoviesScreenWithDrawerState(
                isDrawerExpanded = isDrawerExpanded,
                backStackEntry = backStackEntry,
                onNavigateToPlayer = { content: com.offordflix.domain.model.VideoContent ->
                    navController.navigate(Screen.Player.createRoute(content.id))
                },
                onNavigateToDetails = { content: com.offordflix.domain.model.VideoContent ->
                    // TODO: Navigate to content details screen
                },
                onExpandDrawer = onExpandDrawer,
                onOverlayVisibilityChanged = onOverlayVisibilityChanged
            )
        }
        
        composable(Screen.Shows.route) { backStackEntry ->
            TVShowsScreenWithDrawerState(
                isDrawerExpanded = isDrawerExpanded,
                backStackEntry = backStackEntry,
                onNavigateToPlayer = { content: com.offordflix.domain.model.VideoContent ->
                    navController.navigate(Screen.Player.createRoute(content.id))
                },
                onNavigateToDetails = { content: com.offordflix.domain.model.VideoContent ->
                    // TODO: Navigate to content details screen
                },
                onExpandDrawer = onExpandDrawer,
                onOverlayVisibilityChanged = onOverlayVisibilityChanged
            )
        }
        
        composable(Screen.Categories.route) {
            CategoriesScreen(
                onNavigateToCategory = { category ->
                    // TODO: Navigate to category-specific content screen
                }
            )
        }
        
        composable(Screen.MyList.route) {
            MyListScreen(
                onNavigateToPlayer = { content: com.offordflix.domain.model.VideoContent ->
                    navController.navigate(Screen.Player.createRoute(content.id))
                },
                onNavigateToDetails = { content: com.offordflix.domain.model.VideoContent ->
                    // TODO: Navigate to content details screen
                }
            )
        }
        
        composable(Screen.Settings.route) {
            // TODO: Implement Settings screen
            SettingsPlaceholderScreen()
        }
        
        composable(Screen.Player.route) { backStackEntry ->
            val contentId = backStackEntry.arguments?.getString("contentId") ?: ""
            PlayerScreen(
                contentId = contentId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Helper function to get the selected route for drawer highlighting
 */
private fun getSelectedRoute(currentRoute: String): String {
    return when {
        currentRoute.startsWith("home") -> "home"
        currentRoute == Screen.Search.route -> "search"
        currentRoute == Screen.Movies.route -> "movies"
        currentRoute == Screen.Shows.route -> "shows"
        currentRoute == Screen.Categories.route -> "categories"
        currentRoute == Screen.MyList.route -> "mylist"
        currentRoute == Screen.Settings.route -> "settings"
        else -> "home"
    }
}

/**
 * Helper function to extract profile ID from current route
 */
private fun getCurrentProfileId(currentRoute: String): String {
    return if (currentRoute.startsWith("home/")) {
        currentRoute.substringAfter("home/")
    } else {
        "default"
    }
}

/**
 * Home screen wrapper that handles drawer state changes
 */
@Composable
private fun HomeScreenWithDrawerState(
    profileId: String,
    isDrawerExpanded: Boolean,
    backStackEntry: androidx.navigation.NavBackStackEntry,
    onNavigateToPlayer: (com.offordflix.domain.model.VideoContent) -> Unit,
    onNavigateToDetails: (com.offordflix.domain.model.VideoContent) -> Unit,
    onExpandDrawer: (() -> Unit)? = null,
    onOverlayVisibilityChanged: (Boolean) -> Unit = {}
) {
    // Pass drawer state to HomeScreen so it can manage its own focus
    HomeScreenWithFocusManagement(
        profileId = profileId,
        isDrawerExpanded = isDrawerExpanded,
        backStackEntry = backStackEntry,
        onNavigateToPlayer = onNavigateToPlayer,
        onNavigateToDetails = onNavigateToDetails,
        onExpandDrawer = onExpandDrawer,
        onOverlayVisibilityChanged = onOverlayVisibilityChanged
    )
}

/**
 * HomeScreen with proper focus management for drawer state changes
 */
@Composable
private fun HomeScreenWithFocusManagement(
    profileId: String,
    isDrawerExpanded: Boolean,
    backStackEntry: androidx.navigation.NavBackStackEntry,
    onNavigateToPlayer: (com.offordflix.domain.model.VideoContent) -> Unit,
    onNavigateToDetails: (com.offordflix.domain.model.VideoContent) -> Unit,
    onExpandDrawer: (() -> Unit)? = null,
    onOverlayVisibilityChanged: (Boolean) -> Unit = {}
) {
    // Create a focus requester for the content rows
    val contentFocusRequester = remember { FocusRequester() }
    
    // Restore focus to content when drawer collapses
    LaunchedEffect(isDrawerExpanded) {
        Log.d("NavigationDrawer", "HomeScreen focus management: drawer expanded=$isDrawerExpanded")
        if (!isDrawerExpanded) {
            Log.d("NavigationDrawer", "Drawer collapsed, restoring focus to content rows")
            delay(350) // Wait for drawer animation and any other focus changes
            try {
                contentFocusRequester.requestFocus()
                Log.d("NavigationDrawer", "Content rows focus requested successfully")
            } catch (e: Exception) {
                Log.e("NavigationDrawer", "Failed to request content focus", e)
            }
        }
    }
    
    // Ensure initial focus is set when drawer is not expanded
    LaunchedEffect(Unit) {
        if (!isDrawerExpanded) {
            delay(500) // Wait for initial UI setup
            try {
                contentFocusRequester.requestFocus()
                Log.d("NavigationDrawer", "Initial content focus requested")
            } catch (e: Exception) {
                Log.e("NavigationDrawer", "Failed to request initial content focus", e)
            }
        }
    }
    
    // Use the modified NetflixStyleHomeScreen that accepts focus requester
    NetflixStyleHomeScreen(
        profileId = profileId,
        backStackEntry = backStackEntry,
        externalFocusRequester = contentFocusRequester,
        onNavigateToPlayer = onNavigateToPlayer,
        onNavigateToDetails = onNavigateToDetails,
        onExpandDrawer = onExpandDrawer,
        onOverlayVisibilityChanged = onOverlayVisibilityChanged
    )
}

/**
 * Movies screen wrapper that handles drawer state changes
 */
@Composable
private fun MoviesScreenWithDrawerState(
    isDrawerExpanded: Boolean,
    backStackEntry: androidx.navigation.NavBackStackEntry,
    onNavigateToPlayer: (com.offordflix.domain.model.VideoContent) -> Unit,
    onNavigateToDetails: (com.offordflix.domain.model.VideoContent) -> Unit,
    onExpandDrawer: (() -> Unit)? = null,
    onOverlayVisibilityChanged: (Boolean) -> Unit = {}
) {
    // Create a focus requester for the content rows
    val contentFocusRequester = remember { FocusRequester() }
    
    // Restore focus to content when drawer collapses
    LaunchedEffect(isDrawerExpanded) {
        Log.d("NavigationDrawer", "MoviesScreen focus management: drawer expanded=$isDrawerExpanded")
        if (!isDrawerExpanded) {
            Log.d("NavigationDrawer", "Drawer collapsed, restoring focus to movies content rows")
            delay(350) // Wait for drawer animation and any other focus changes
            try {
                contentFocusRequester.requestFocus()
                Log.d("NavigationDrawer", "Movies content rows focus requested successfully")
            } catch (e: Exception) {
                Log.e("NavigationDrawer", "Failed to request movies content focus", e)
            }
        }
    }
    
    // Ensure initial focus is set when drawer is not expanded
    LaunchedEffect(Unit) {
        if (!isDrawerExpanded) {
            delay(500) // Wait for initial UI setup
            try {
                contentFocusRequester.requestFocus()
                Log.d("NavigationDrawer", "Initial movies content focus requested")
            } catch (e: Exception) {
                Log.e("NavigationDrawer", "Failed to request initial movies content focus", e)
            }
        }
    }
    
    // Use the NetflixStyleMoviesScreen that accepts focus requester
    com.offordflix.ui.screens.movies.NetflixStyleMoviesScreen(
        profileId = "default", // TODO: Get actual profile ID
        backStackEntry = backStackEntry,
        externalFocusRequester = contentFocusRequester,
        onNavigateToPlayer = onNavigateToPlayer,
        onNavigateToDetails = onNavigateToDetails,
        onExpandDrawer = onExpandDrawer,
        onOverlayVisibilityChanged = onOverlayVisibilityChanged
    )
}

/**
 * TV shows screen wrapper that handles drawer state changes
 */
@Composable
private fun TVShowsScreenWithDrawerState(
    isDrawerExpanded: Boolean,
    backStackEntry: androidx.navigation.NavBackStackEntry,
    onNavigateToPlayer: (com.offordflix.domain.model.VideoContent) -> Unit,
    onNavigateToDetails: (com.offordflix.domain.model.VideoContent) -> Unit,
    onExpandDrawer: (() -> Unit)? = null,
    onOverlayVisibilityChanged: (Boolean) -> Unit = {}
) {
    // Create a focus requester for the content rows
    val contentFocusRequester = remember { FocusRequester() }
    
    // Restore focus to content when drawer collapses
    LaunchedEffect(isDrawerExpanded) {
        Log.d("NavigationDrawer", "TVShowsScreen focus management: drawer expanded=$isDrawerExpanded")
        if (!isDrawerExpanded) {
            Log.d("NavigationDrawer", "Drawer collapsed, restoring focus to TV shows content rows")
            delay(350) // Wait for drawer animation and any other focus changes
            try {
                contentFocusRequester.requestFocus()
                Log.d("NavigationDrawer", "TV shows content rows focus requested successfully")
            } catch (e: Exception) {
                Log.e("NavigationDrawer", "Failed to request TV shows content focus", e)
            }
        }
    }
    
    // Ensure initial focus is set when drawer is not expanded
    LaunchedEffect(Unit) {
        if (!isDrawerExpanded) {
            delay(500) // Wait for initial UI setup
            try {
                contentFocusRequester.requestFocus()
                Log.d("NavigationDrawer", "Initial TV shows content focus requested")
            } catch (e: Exception) {
                Log.e("NavigationDrawer", "Failed to request initial TV shows content focus", e)
            }
        }
    }
    
    // Use the NetflixStyleTVShowsScreen that accepts focus requester
    com.offordflix.ui.screens.shows.NetflixStyleTVShowsScreen(
        profileId = "default", // TODO: Get actual profile ID
        backStackEntry = backStackEntry,
        externalFocusRequester = contentFocusRequester,
        onNavigateToPlayer = onNavigateToPlayer,
        onNavigateToDetails = onNavigateToDetails,
        onExpandDrawer = onExpandDrawer,
        onOverlayVisibilityChanged = onOverlayVisibilityChanged
    )
}

/**
 * Placeholder for Settings screen
 */
@Composable
private fun SettingsPlaceholderScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Settings screen coming soon...",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}


