package com.offordflix.data.api

import com.offordflix.data.dto.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * TMDB API service interface.
 * 
 * Provides access to The Movie Database (TMDB) API for content discovery,
 * including movies, TV shows, search, and metadata.
 */
interface TmdbApi {
    
    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
        const val BACKDROP_SIZE = "w1280"
        const val POSTER_SIZE = "w500"
        const val PROFILE_SIZE = "w185"
        const val LOGO_SIZE = "w500" // Standard size for logos
    }
    
    // Movies
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Response<TmdbMovieResponse>
    
    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Response<TmdbMovieResponse>
    
    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Response<TmdbMovieResponse>
    
    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Response<TmdbMovieResponse>
    
    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") appendToResponse: String = "videos,credits,similar"
    ): Response<TmdbMovieDetailsDto>
    
    // TV Shows
    @GET("tv/popular")
    suspend fun getPopularTvShows(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Response<TmdbTvResponse>
    
    @GET("tv/top_rated")
    suspend fun getTopRatedTvShows(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Response<TmdbTvResponse>
    
    @GET("tv/on_the_air")
    suspend fun getOnTheAirTvShows(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Response<TmdbTvResponse>
    
    @GET("tv/{tv_id}")
    suspend fun getTvShowDetails(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") appendToResponse: String = "videos,credits,similar"
    ): Response<TmdbTvDetailsDto>
    
    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getSeasonDetails(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): Response<TmdbSeasonDto>
    
    // Trending
    @GET("trending/all/day")
    suspend fun getTrendingDay(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): Response<TmdbMultiResponse>
    
    @GET("trending/all/week")
    suspend fun getTrendingWeek(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): Response<TmdbMultiResponse>
    
    // Search
    @GET("search/multi")
    suspend fun searchMulti(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
        @Query("include_adult") includeAdult: Boolean = false
    ): Response<TmdbMultiResponse>
    
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
        @Query("include_adult") includeAdult: Boolean = false
    ): Response<TmdbMovieResponse>
    
    @GET("search/tv")
    suspend fun searchTvShows(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
        @Query("include_adult") includeAdult: Boolean = false
    ): Response<TmdbTvResponse>
    
    // Genres
    @GET("genre/movie/list")
    suspend fun getMovieGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): Response<TmdbGenreResponse>
    
    @GET("genre/tv/list")
    suspend fun getTvGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): Response<TmdbGenreResponse>
    
    // Discover
    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_genres") withGenres: String? = null,
        @Query("without_genres") withoutGenres: String? = null,
        @Query("certification_country") certificationCountry: String? = null,
        @Query("certification.lte") certificationLte: String? = null
    ): Response<TmdbMovieResponse>
    
    @GET("discover/tv")
    suspend fun discoverTvShows(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_genres") withGenres: String? = null,
        @Query("without_genres") withoutGenres: String? = null
    ): Response<TmdbTvResponse>
    
    // Images
    @GET("movie/{movie_id}/images")
    suspend fun getMovieImages(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String? = null,
        @Query("include_image_language") includeImageLanguage: String? = "en,null"
    ): Response<TmdbImagesResponse>
    
    @GET("tv/{tv_id}/images")
    suspend fun getTvImages(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String? = null,
        @Query("include_image_language") includeImageLanguage: String? = "en,null"
    ): Response<TmdbImagesResponse>
}

