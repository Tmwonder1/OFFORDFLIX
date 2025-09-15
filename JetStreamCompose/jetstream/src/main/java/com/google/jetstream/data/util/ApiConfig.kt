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

package com.google.jetstream.data.util

import android.os.Build

object ApiConfig {
    // TMDB API key for fetching movie metadata
    const val TMDB_API_KEY = "df26f7a61a2c85d4a80d5239f7192d70"
    
    // CinePro backend URLs for different environments
    const val CINEPRO_BASE_URL_EMULATOR = "http://10.0.2.2:3000/"
    const val CINEPRO_BASE_URL_PHYSICAL = "http://192.168.1.17:3000/"
    
    // Automatically detect emulator vs physical device
    val CINEPRO_BASE_URL: String
        get() = if (isEmulator()) CINEPRO_BASE_URL_EMULATOR else CINEPRO_BASE_URL_PHYSICAL
    
    // TMDB configuration
    const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
    const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
    
    // Image sizes for TMDB
    const val POSTER_SIZE = "w500"
    const val BACKDROP_SIZE = "w1280"
    const val PROFILE_SIZE = "w185"
    
    // Helper function to detect if running on emulator
    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
    }
}