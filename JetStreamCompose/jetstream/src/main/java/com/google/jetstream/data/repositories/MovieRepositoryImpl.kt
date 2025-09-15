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

package com.google.jetstream.data.repositories

import com.google.jetstream.data.api.CineProApiService
import com.google.jetstream.data.api.TmdbApiService
import com.google.jetstream.data.entities.ContentFilter
import com.google.jetstream.data.entities.ContentType
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieCast
import com.google.jetstream.data.entities.MovieCategoryDetails
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.entities.MovieReviewsAndRatings
import com.google.jetstream.data.entities.ThumbnailType
import com.google.jetstream.data.models.tmdb.toMovieDetails
import com.google.jetstream.data.models.tmdb.toMovie
import com.google.jetstream.data.util.ApiConfig
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.data.util.StringConstants.Movie.Reviewer.DefaultCount
import com.google.jetstream.data.util.StringConstants.Movie.Reviewer.DefaultRating
import com.google.jetstream.data.util.StringConstants.Movie.Reviewer.FreshTomatoes
import com.google.jetstream.data.util.StringConstants.Movie.Reviewer.ReviewerName
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val movieDataSource: MovieDataSource,
    private val tvDataSource: TvDataSource,
    private val movieCastDataSource: MovieCastDataSource,
    private val movieCategoryDataSource: MovieCategoryDataSource,
    private val tmdbApiService: TmdbApiService,
    private val cineProApiService: CineProApiService,
) : MovieRepository {

    private val apiKey = ApiConfig.TMDB_API_KEY

    override fun getFeaturedMovies() = flow {
        val list = movieDataSource.getFeaturedMovieList()
        emit(list)
    }

    override fun getTrendingMovies(): Flow<MovieList> = flow {
        val list = movieDataSource.getTrendingMovieList()
        emit(list)
    }

    override fun getTop10Movies(): Flow<MovieList> = flow {
        val list = movieDataSource.getTop10MovieList()
        emit(list)
    }

    override fun getNowPlayingMovies(): Flow<MovieList> = flow {
        val list = movieDataSource.getNowPlayingMovieList()
        emit(list)
    }

    override fun getMovieCategories() = flow {
        val list = movieCategoryDataSource.getMovieCategoryList()
        emit(list)
    }

    override suspend fun getMovieCategoryDetails(
        categoryId: String, 
        contentFilter: ContentFilter, 
        maxItems: Int
    ): MovieCategoryDetails {
        println("=== Starting getMovieCategoryDetails ===")
        println("CategoryId: $categoryId, Filter: $contentFilter, MaxItems: $maxItems")
        val startTime = System.currentTimeMillis()
        
        val categoryList = movieCategoryDataSource.getMovieCategoryList()
        val category = categoryList.find { categoryId == it.id } ?: categoryList.first()
        println("Found category: ${category.name} (ID: ${category.id})")
        
        val genreId = category.id.toIntOrNull()
        
        val movieList = if (genreId != null) {
            // Get actual genre-based content from TMDB API
            val movies = mutableListOf<Movie>()
            
            // Fetch movies by genre if requested
            if (contentFilter == ContentFilter.ALL || contentFilter == ContentFilter.MOVIES_ONLY) {
                try {
                    var page = 1
                    while (movies.size < maxItems && page <= 10) { // Limit to 10 pages for better performance (200 items)
                        println("Fetching movies page $page for genre $genreId")
                        val movieResponse = tmdbApiService.discoverMoviesByGenre(apiKey, genreId, page)
                        val genreMovies = movieResponse.results.map { it.toMovie() }
                        movies.addAll(genreMovies)
                        println("Added ${genreMovies.size} movies from page $page")
                        
                        if (movieResponse.results.isEmpty()) break
                        page++
                    }
                    println("Total fetched ${movies.size} movies for genre $genreId")
                } catch (e: Exception) {
                    println("Error fetching movies by genre: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            // Fetch TV shows by genre if requested
            if (contentFilter == ContentFilter.ALL || contentFilter == ContentFilter.SHOWS_ONLY) {
                try {
                    println("Starting TV shows fetch for genre $genreId...")
                    
                    val tvShows = mutableListOf<Movie>()
                    
                    // Try genre-specific TV shows first
                    try {
                        var page = 1
                        while (tvShows.size < maxItems && page <= 5) { // Try 5 pages for genre-specific
                            println("Fetching TV shows page $page for genre $genreId")
                            val tvResponse = tmdbApiService.discoverTvShowsByGenre(apiKey, genreId, page)
                            println("TV API Response: page=$page, total_results=${tvResponse.totalResults}, results_count=${tvResponse.results.size}")
                            
                            val genreTvShows = tvResponse.results.map { it.toMovie() }
                            tvShows.addAll(genreTvShows)
                            println("Added ${genreTvShows.size} TV shows from page $page")
                            
                            if (tvResponse.results.isEmpty()) {
                                println("No more TV shows available for genre $genreId")
                                break
                            }
                            page++
                        }
                    } catch (e: Exception) {
                        println("Genre-specific TV shows failed: ${e.message}")
                    }
                    
                    // If no genre-specific TV shows found, fallback to popular TV shows
                    if (tvShows.isEmpty()) {
                        println("No genre-specific TV shows found, using popular TV shows as fallback...")
                        try {
                            var page = 1
                            while (tvShows.size < maxItems.coerceAtMost(100) && page <= 5) { // Limit fallback to 100 items
                                val popularResponse = when (page) {
                                    1 -> tmdbApiService.getPopularTvShows(apiKey, page)
                                    else -> tmdbApiService.getTopRatedTvShows(apiKey, page - 1) // Mix popular and top rated
                                }
                                
                                val popularTvShows = popularResponse.results.map { it.toMovie() }
                                tvShows.addAll(popularTvShows)
                                println("Added ${popularTvShows.size} popular TV shows from page $page")
                                
                                if (popularResponse.results.isEmpty()) break
                                page++
                            }
                        } catch (fallbackException: Exception) {
                            println("Popular TV shows fallback failed: ${fallbackException.message}")
                        }
                    }
                    
                    movies.addAll(tvShows)
                    println("Total fetched ${tvShows.size} TV shows for genre $genreId")
                    tvShows.take(3).forEach { show ->
                        println("TV Show: ${show.name} (ID: ${show.id}, ContentType: ${show.contentType})")
                    }
                    println("Combined movies+shows list now has ${movies.size} items")
                } catch (e: Exception) {
                    println("Error fetching TV shows by genre $genreId: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            // Take only the requested number of items and shuffle for variety
            movies.shuffled().take(maxItems)
        } else {
            // Fallback to random movies if genre ID parsing fails
            movieDataSource.getMovieList().shuffled().take(maxItems)
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        println("=== Completed getMovieCategoryDetails in ${duration}ms ===")
        println("Final result: ${movieList.size} items for category ${category.name}")
        
        return MovieCategoryDetails(
            id = category.id,
            name = category.name,
            movies = movieList
        )
    }

    override suspend fun getMovieDetails(movieId: String): MovieDetails {
        val tmdbId = movieId.toIntOrNull() ?: return getDefaultMovieDetails(movieId)
        
        // First, try to determine if this ID belongs to a TV show by checking our TV show lists
        val isLikelyTvShow = try {
            val tvShowList = tvDataSource.getTvShowList() + tvDataSource.getBingeWatchDramaList()
            tvShowList.any { it.id == movieId }
        } catch (e: Exception) {
            false
        }
        
        return try {
            if (isLikelyTvShow) {
                // This is likely a TV show, try TV API first
                println("Detected as TV show from lists, using TV API for ID: $movieId")
                return getTvShowDetails(movieId, 1, 1)
            }
            
            // Try TV show API first to check if this ID exists as a TV show
            try {
                println("Checking if ID $movieId exists as TV show...")
                val tvShowDetails = tmdbApiService.getTvShowDetails(tmdbId, apiKey)
                println("Found TV show: ${tvShowDetails.name}, using TV API")
                return getTvShowDetails(movieId, 1, 1)
            } catch (tvException: Exception) {
                println("Not found as TV show: ${tvException.message}, trying movie API...")
            }
            
            // Try movie API first for non-TV show content
            try {
                val movieDetails = tmdbApiService.getMovieDetails(tmdbId, apiKey)
                
                // Fetch streaming sources from CinePro backend for movies
                val streamingSources = try {
                    val sources = cineProApiService.getMovieStreamingSources(tmdbId)
                    println("CinePro Movie API Response: ${sources.files.size} files, ${sources.subtitles.size} subtitles")
                    sources.files.forEachIndexed { index, file -> 
                        println("Movie File $index: ${file.type} - ${file.file}")
                    }
                    sources
                } catch (e: Exception) {
                    println("CinePro Movie API Error: ${e.message}")
                    e.printStackTrace()
                    null
                }
                
                // Convert TMDB details to MovieDetails with streaming URLs
                val videoUri = streamingSources?.files?.let { files ->
                    // Prefer HLS streams for better Android TV compatibility
                    val rawUrl = files.find { it.type == "hls" }?.file 
                        ?: files.find { it.type == "mp4" }?.file 
                        ?: files.firstOrNull()?.file
                    // Clean malformed URLs that contain escaped JSON
                    rawUrl?.let { cleanVideoUrl(it) }
                } ?: run {
                    // Log when falling back to bunny video
                    println("WARNING: No streaming sources found for movie ${movieDetails.title} (ID: $tmdbId), using fallback video")
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                }
                val subtitleUri = streamingSources?.subtitles?.firstOrNull()?.url
                
                println("Final Movie videoUri for ${movieDetails.title}: $videoUri")
                println("Final Movie subtitleUri: $subtitleUri")
                
                movieDetails.toMovieDetails(
                    videoUri = videoUri,
                    subtitleUri = subtitleUri
                )
            } catch (movieException: Exception) {
                println("Movie API failed, trying TV show API: ${movieException.message}")
                
                // If movie fails, try TV show as fallback
                return getTvShowDetails(movieId, 1, 1)
            }
        } catch (e: Exception) {
            println("Both Movie and TV API failed: ${e.message}")
            e.printStackTrace()
            getDefaultMovieDetails(movieId)
        }
    }

    private fun cleanVideoUrl(rawUrl: String): String {
        // Remove escaped JSON data from malformed URLs
        val cleanUrl = rawUrl.substringBefore("\",\"")
            .substringBefore("\"}")
            .substringBefore(",\"")
        
        println("Cleaned URL: $rawUrl -> $cleanUrl")
        return cleanUrl
    }

    private suspend fun getDefaultMovieDetails(movieId: String): MovieDetails {
        val movieList = movieDataSource.getMovieList()
        val movie = movieList.find { it.id == movieId } ?: movieList.firstOrNull() ?: return MovieDetails(
            id = movieId,
            videoUri = "",
            subtitleUri = null,
            posterUri = "",
            name = "Unknown Movie",
            description = "Movie details not available",
            pgRating = "Not Rated",
            releaseDate = "",
            categories = emptyList(),
            duration = "",
            director = "",
            screenplay = "",
            music = "",
            castAndCrew = emptyList(),
            status = "Unknown",
            originalLanguage = "Unknown",
            budget = "",
            revenue = "",
            similarMovies = emptyList(),
            reviewsAndRatings = emptyList(),
            contentType = ContentType.MOVIE
        )
        
        val similarMovieList = movieList.take(4)
        val castList = movieCastDataSource.getMovieCastList()

        return MovieDetails(
            id = movie.id,
            videoUri = movie.videoUri,
            subtitleUri = movie.subtitleUri,
            posterUri = movie.posterUri,
            name = movie.name,
            description = movie.description,
            pgRating = "PG-13",
            releaseDate = "2021 (US)",
            categories = listOf("Action", "Adventure", "Fantasy", "Comedy"),
            duration = "1h 59m",
            director = "Larry Page",
            screenplay = "Sundai Pichai",
            music = "Sergey Brin",
            castAndCrew = castList,
            status = "Released",
            originalLanguage = "English",
            budget = "$15M",
            revenue = "$40M",
            similarMovies = similarMovieList,
            reviewsAndRatings = listOf(
                MovieReviewsAndRatings(
                    reviewerName = FreshTomatoes,
                    reviewerIconUri = StringConstants.Movie.Reviewer.FreshTomatoesImageUrl,
                    reviewCount = "22",
                    reviewRating = "89%"
                ),
                MovieReviewsAndRatings(
                    reviewerName = ReviewerName,
                    reviewerIconUri = StringConstants.Movie.Reviewer.ImageUrl,
                    reviewCount = DefaultCount,
                    reviewRating = DefaultRating
                ),
            ),
            contentType = movie.contentType
        )
    }

    override suspend fun searchMovies(query: String): MovieList {
        return try {
            // Search both movies and TV shows
            val movieResponse = tmdbApiService.searchMovies(apiKey, query)
            val tvResponse = tmdbApiService.searchTvShows(apiKey, query)
            
            // Combine results - movies first, then TV shows
            val movieResults = movieResponse.results.map { it.toMovie() }
            val tvResults = tvResponse.results.map { it.toMovie() }
            
            println("Search results: ${movieResults.size} movies, ${tvResults.size} TV shows")
            
            movieResults + tvResults
        } catch (e: Exception) {
            println("Search API error: ${e.message}")
            // Fallback to local search if API fails
            movieDataSource.getMovieList().filter {
                it.name.contains(other = query, ignoreCase = true)
            }
        }
    }

    override fun getMoviesWithLongThumbnail() = flow {
        val list = movieDataSource.getMovieList(ThumbnailType.Long)
        emit(list)
    }

    override fun getMovies(): Flow<MovieList> = flow {
        val list = movieDataSource.getMovieList()
        emit(list)
    }

    override fun getPopularFilmsThisWeek(): Flow<MovieList> = flow {
        val list = movieDataSource.getPopularFilmThisWeek()
        emit(list)
    }

    override fun getTVShows(): Flow<MovieList> = flow {
        val list = tvDataSource.getTvShowList()
        emit(list)
    }

    override fun getBingeWatchDramas(): Flow<MovieList> = flow {
        val list = tvDataSource.getBingeWatchDramaList()
        emit(list)
    }

    override fun getFavouriteMovies(): Flow<MovieList> = flow {
        val list = movieDataSource.getFavoriteMovieList()
        emit(list)
    }

    override suspend fun getContentDetails(contentId: String, contentType: ContentType): MovieDetails {
        return when (contentType) {
            ContentType.MOVIE -> getMovieDetails(contentId)
            ContentType.TV_SHOW -> getTvShowDetails(contentId)
        }
    }

    override suspend fun getTvShowDetails(tvShowId: String, season: Int, episode: Int): MovieDetails {
        val tmdbId = tvShowId.toIntOrNull() ?: return getDefaultMovieDetails(tvShowId)
        
        return try {
            val tvShowDetails = tmdbApiService.getTvShowDetails(tmdbId, apiKey)
            
            // Fetch streaming sources from CinePro backend for specific season/episode
            val streamingSources = try {
                println("CinePro TV API: Requesting S${season}E${episode} for TMDB ID: $tmdbId")
                val sources = cineProApiService.getTvShowStreamingSources(tmdbId, season, episode)
                println("CinePro TV API Response: ${sources.files.size} files, ${sources.subtitles.size} subtitles")
                sources.files.forEachIndexed { index, file -> 
                    println("TV File $index: ${file.type} - ${file.file}")
                }
                sources
            } catch (e: Exception) {
                println("CinePro TV API Error for S${season}E${episode}: ${e.message}")
                e.printStackTrace()
                
                // Fallback: Try S1E1 if the specific episode fails
                if (season != 1 || episode != 1) {
                    println("CinePro TV API: Falling back to S1E1 for TMDB ID: $tmdbId")
                    try {
                        val fallbackSources = cineProApiService.getTvShowStreamingSources(tmdbId, 1, 1)
                        println("CinePro TV API Fallback Response: ${fallbackSources.files.size} files, ${fallbackSources.subtitles.size} subtitles")
                        fallbackSources.files.forEachIndexed { index, file -> 
                            println("TV Fallback File $index: ${file.type} - ${file.file}")
                        }
                        fallbackSources
                    } catch (fallbackException: Exception) {
                        println("CinePro TV API Fallback also failed: ${fallbackException.message}")
                        null
                    }
                } else {
                    null
                }
            }
            
            val videoUri = streamingSources?.files?.let { files ->
                // Prefer HLS streams for better Android TV compatibility
                val rawUrl = files.find { it.type == "hls" }?.file 
                    ?: files.find { it.type == "mp4" }?.file 
                    ?: files.firstOrNull()?.file
                // Clean malformed URLs that contain escaped JSON
                rawUrl?.let { cleanVideoUrl(it) }
            } ?: 
                // Fallback to test video for TV shows
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            val subtitleUri = streamingSources?.subtitles?.firstOrNull()?.url
            
            println("Final TV videoUri for ${tvShowDetails.name} S${season}E${episode}: $videoUri")
            println("Final TV subtitleUri: $subtitleUri")
            
            // Convert TV show details to MovieDetails format using the extension function
            tvShowDetails.toMovieDetails(videoUri, subtitleUri)
        } catch (e: Exception) {
            println("TV Show API failed: ${e.message}")
            e.printStackTrace()
            getDefaultMovieDetails(tvShowId)
        }
    }

    // Overload method that calls the main implementation with default season/episode
    suspend fun getTvShowDetails(tvShowId: String): MovieDetails {
        return getTvShowDetails(tvShowId, 1, 1)
    }
}
