# Offordflix - Android TV Streaming App

Offordflix is an Android TV streaming application built with Jetpack Compose for TV, integrating with TMDB for movie metadata and CinePro backend for streaming sources.

## Features

- 📺 **Android TV Optimized** - Built specifically for TV interfaces with proper focus management
- 🎬 **Movie & TV Shows** - Browse popular, trending, and top-rated content
- 🔍 **Search Functionality** - Search for movies and TV shows
- 📂 **Categories** - Browse content by genre
- 🎥 **Video Player** - Built-in video player with subtitle support
- 🌟 **Modern Architecture** - MVVM, Repository pattern, Hilt DI

## Setup Instructions

### 1. TMDB API Key ✅ CONFIGURED

Your TMDB API key is already configured in `ApiConfig.kt`:
```kotlin
const val TMDB_API_KEY = "df26f7a61a2c85d4a80d5239f7192d70"
```

### 2. CinePro Backend Setup ✅ RUNNING

Your CinePro backend is running on `localhost:3000` and responding correctly!

**Current Configuration:**
```kotlin
const val CINEPRO_BASE_URL = "http://10.0.2.2:3000/" // For Android emulator
```

**For different devices:**
- **Android Emulator**: Use `http://10.0.2.2:3000/` (current setting)
- **Physical Android TV**: Replace with `http://YOUR_LOCAL_IP:3000/`
  - To find your local IP: `ip route get 1.1.1.1 | grep -oP 'src \K\S+'`

### 3. Build and Run

1. Open the project in Android Studio
2. Sync the project with Gradle files
3. Run the app on an Android TV device or emulator

## Architecture

The app follows modern Android architecture principles:

- **Data Layer**: API services, repositories, and data sources
- **Domain Layer**: Use cases and business logic (if needed)
- **Presentation Layer**: ViewModels and Compose UI

### Key Components

- **TMDB API**: Provides movie metadata, images, and details
- **CinePro API**: Provides streaming sources and subtitles
- **Retrofit**: HTTP client for API calls
- **Hilt**: Dependency injection
- **Jetpack Compose for TV**: UI framework optimized for TV

## API Integration

### TMDB Integration
- Movie lists (popular, trending, top-rated, now playing)
- TV show lists
- Search functionality
- Movie/TV show details with cast and crew
- Genre categories

### CinePro Integration
- Streaming sources for movies and TV shows
- Subtitle support
- Multiple source types (MP4, HLS, embed)

## Development Notes

- The app currently uses mock cast data for some screens
- Streaming integration is ready but requires proper CinePro backend setup
- All API keys should be moved to BuildConfig for production builds
- Error handling includes fallbacks to prevent crashes

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test on Android TV
5. Submit a pull request

## License

This project is licensed under the Apache License 2.0 - see the original Google sample license for details.
