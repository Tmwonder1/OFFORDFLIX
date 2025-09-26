package com.offordflix

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class for Offordflix Android TV app.
 * 
 * This class serves as the entry point for the application and sets up
 * Hilt dependency injection for the entire app.
 */
@HiltAndroidApp
class OffordflixApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Application initialization will go here
    }
}


