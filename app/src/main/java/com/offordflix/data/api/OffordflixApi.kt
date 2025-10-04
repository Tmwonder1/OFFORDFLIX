package com.offordflix.data.api

import com.offordflix.data.dto.HomeContentResponse
import com.offordflix.data.dto.MoviesContentResponse
import com.offordflix.data.dto.TVShowsContentResponse
import com.offordflix.data.dto.StreamingResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Offordflix Backend API service interface.
 * 
 * Provides access to our enhanced backend endpoints with rich content rows.
 */
interface OffordflixApi {
    
    companion object {
        const val BASE_URL = "http://192.168.1.17:3000/"
    }
    
    /**
     * Get enhanced home screen content with all content rows.
     */
    @GET("api/content/home")
    suspend fun getHomeContent(
        @Header("Authorization") token: String = "Bearer dev-bypass-token"
    ): Response<HomeContentResponse>
    
    /**
     * Get enhanced movies screen content with movie-only rows.
     */
    @GET("api/content/movies/home")
    suspend fun getMoviesContent(
        @Header("Authorization") token: String = "Bearer dev-bypass-token"
    ): Response<MoviesContentResponse>
    
    /**
     * Get enhanced TV shows screen content with TV show-only rows.
     */
    @GET("api/content/tv-shows/home")
    suspend fun getTVShowsContent(
        @Header("Authorization") token: String = "Bearer dev-bypass-token"
    ): Response<TVShowsContentResponse>

    // Streaming endpoints return JSON with files and subtitles
    @GET("movie/{tmdbId}")
    suspend fun getMovieStreams(
        @Path("tmdbId") tmdbId: String,
        @Header("Authorization") token: String = "Bearer dev-bypass-token"
    ): Response<StreamingResponseDto>

    @GET("tv/{tmdbId}")
    suspend fun getTvStreams(
        @Path("tmdbId") tmdbId: String,
        @Query("s") season: Int,
        @Query("e") episode: Int,
        @Header("Authorization") token: String = "Bearer dev-bypass-token"
    ): Response<StreamingResponseDto>
}
