# TMDB Logo Integration Guide

## Overview

Yes, it's absolutely possible to change movie/show titles to their original logos using TMDB! This implementation adds comprehensive logo support to your Offordflix app.

## What Was Implemented

### 1. TMDB Images API Integration

**Added new API endpoints:**
- `GET /movie/{movie_id}/images` - Fetches movie images including logos
- `GET /tv/{tv_id}/images` - Fetches TV show images including logos

**New DTOs:**
- `TmdbImagesResponse` - Contains backdrops, logos, and posters
- `TmdbImageDto` - Individual image details with file path and metadata

### 2. Enhanced Logo System

**Frontend (Android):**
- Enhanced `ContentDiscoveryRepository` with TMDB logo fetching functions
- `getMovieLogoFromTmdb()` and `getTvLogoFromTmdb()` functions
- `enrichContentWithLogos()` - Automatically enriches content lists with logos
- Fallback system: TMDB logos → static logo mapping → text titles

**Backend (Node.js):**
- Updated TMDB requests to include `images` in `append_to_response`
- Added `getLogoFromImages()` helper function
- Automatic logo extraction from TMDB images data

### 3. UI Components Already Support Logos

Your app already has excellent logo support in place:
- `HeroBanner` component displays logos when available
- Fallback to text titles when logos aren't found
- Smooth error handling for failed logo loads

## How It Works

### 1. Content Loading Flow

```kotlin
// 1. Fetch popular movies from TMDB
val movies = tmdbApi.getPopularMovies()

// 2. Convert to VideoContent objects
val videoContent = movies.map { it.toVideoContent() }

// 3. Enrich with logos from TMDB images API
val enrichedContent = enrichContentWithLogos(videoContent)

// 4. Display in UI with logos or fallback to text
```

### 2. Logo Priority System

1. **TMDB Official Logos** (Primary)
   - Fetched from `/movie/{id}/images` or `/tv/{id}/images`
   - Best quality logo selected based on vote average

2. **Static Logo Mapping** (Fallback)
   - Hardcoded URLs for popular content
   - Covers major titles like "Breaking Bad", "Stranger Things", etc.

3. **Text Title** (Final Fallback)
   - Clean text display when no logo is available
   - Maintains consistent UI experience

### 3. Logo URL Construction

TMDB logos are accessed using:
```
https://image.tmdb.org/t/p/w500/{logo_file_path}
```

Example: `https://image.tmdb.org/t/p/w500/3pK1u9LfNLM0gWLhf3vn3tN5DYf.png`

## Key Benefits

✅ **Authentic Branding** - Original movie/show logos for better recognition  
✅ **Comprehensive Coverage** - TMDB has logos for thousands of titles  
✅ **Fallback System** - Graceful degradation when logos aren't available  
✅ **Performance Optimized** - Async logo fetching with caching  
✅ **Error Resilient** - Handles network issues and missing images  

## Usage Examples

### Get Content with Logos

```kotlin
// Popular movies with logos
contentDiscoveryRepository.getPopularMovies().collect { movies ->
    // Each movie may now have a logoUrl populated
    movies.forEach { movie ->
        if (movie.logoUrl != null) {
            // Display logo
        } else {
            // Display text title
        }
    }
}
```

### Manual Logo Fetching

```kotlin
// Get logo for specific movie
val logoUrl = contentDiscoveryRepository.getMovieLogoFromTmdb(550) // Fight Club
// Returns: "https://image.tmdb.org/t/p/w500/..." or null

// Get logo for specific TV show  
val tvLogoUrl = contentDiscoveryRepository.getTvLogoFromTmdb(1399) // Game of Thrones
```

## Testing

To test the logo integration:

1. **Run the app** and navigate to the home screen
2. **Check popular content** - Many titles should now display logos instead of text
3. **Hero banner** - Featured content should show logos prominently
4. **Fallback testing** - Titles without TMDB logos will use static mappings or text

## Coverage Examples

Movies/shows that should now display logos:
- Marvel movies (Spider-Man, Avengers, etc.)
- Popular franchises (Harry Potter, Star Wars, etc.)
- TV shows (Game of Thrones, Breaking Bad, etc.)
- Recent releases and trending content

## Performance Notes

- Logo fetching is asynchronous and doesn't block content loading
- TMDB API caching reduces redundant requests
- Fallback system ensures UI remains responsive
- Images are cached by Coil for optimal performance

## Future Enhancements

Consider these potential improvements:
- Local logo caching for offline support
- Logo size optimization based on device/context
- User preference for logo vs. text display
- Custom logo upload for missing content

## Troubleshooting

**No logos appearing?**
- Check TMDB API key validity
- Verify network connectivity
- Check logs for API errors

**Some logos missing?**
- Not all content has logos in TMDB
- Static mapping provides backup for popular titles
- Text fallback ensures all content remains accessible

**Performance issues?**
- Logo fetching is optimized but can be disabled if needed
- Consider reducing concurrent logo requests
- Monitor TMDB API rate limits

---

This implementation provides a robust, production-ready logo system that enhances your app's visual appeal while maintaining reliability and performance.
