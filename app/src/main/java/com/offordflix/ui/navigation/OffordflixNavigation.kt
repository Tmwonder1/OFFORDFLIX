package com.offordflix.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.offordflix.ui.screens.splash.SplashScreen
import com.offordflix.ui.screens.profile.ProfileSelectionScreen
import com.offordflix.ui.screens.home.HomeScreen
import com.offordflix.ui.screens.player.PlayerScreen

/**
 * Navigation routes for the Offordflix Android TV app.
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object ProfileSelection : Screen("profile_selection")
    object Home : Screen("home/{profileId}") {
        fun createRoute(profileId: String) = "home/$profileId"
    }
    object Player : Screen("player/{contentId}") {
        fun createRoute(contentId: String) = "player/$contentId"
    }
}

/**
 * Main navigation composable for the Offordflix app.
 * 
 * This sets up the navigation graph with all screens and handles
 * navigation between splash screen, profile selection, home, and player.
 */
@Composable
fun OffordflixNavigation(
    navController: NavHostController = rememberNavController()
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
            HomeScreen(
                profileId = profileId,
                onNavigateToPlayer = { content: com.offordflix.domain.model.VideoContent ->
                    navController.navigate(Screen.Player.createRoute(content.id))
                },
                onNavigateToDetails = { content: com.offordflix.domain.model.VideoContent ->
                    // TODO: Navigate to content details screen
                }
            )
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


