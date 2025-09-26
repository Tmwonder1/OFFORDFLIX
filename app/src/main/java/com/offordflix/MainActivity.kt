package com.offordflix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import com.offordflix.ui.theme.OffordflixTheme
import com.offordflix.ui.navigation.OffordflixNavigation

/**
 * Main activity for Offordflix Android TV app.
 * 
 * This is the single activity that hosts all navigation and screens
 * using Jetpack Compose for TV with Navigation Compose.
 * 
 * Features Android 12+ splash screen API integration for optimal
 * launch experience across different Android versions.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Android 12+ splash screen before calling super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Configure splash screen behavior
        configureSplashScreen(splashScreen)
        
        setContent {
            OffordflixTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OffordflixNavigation()
                }
            }
        }
    }
    
    /**
     * Configure splash screen behavior for optimal Android TV experience.
     */
    private fun configureSplashScreen(splashScreen: androidx.core.splashscreen.SplashScreen) {
        // Keep splash screen visible until our custom splash screen is ready
        var keepSplashScreen = true
        
        splashScreen.setKeepOnScreenCondition {
            keepSplashScreen
        }
        
        // Hide splash screen after a short delay to show our custom animation
        window.decorView.post {
            android.os.Handler(mainLooper).postDelayed({
                keepSplashScreen = false
            }, 500) // Brief delay to ensure smooth transition
        }
        
        // Optional: Listen for splash screen exit animation
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // Custom exit animation can be added here if needed
            splashScreenView.remove()
        }
    }
}
