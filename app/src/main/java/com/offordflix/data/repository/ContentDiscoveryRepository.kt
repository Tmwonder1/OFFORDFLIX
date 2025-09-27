package com.offordflix.data.repository

import com.offordflix.data.api.TmdbApi
import com.offordflix.data.dto.*
import com.offordflix.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for content discovery using TMDB API.
 * 
 * Provides access to movies, TV shows, search functionality,
 * and content metadata for the streaming app.
 */
@Singleton
class ContentDiscoveryRepository @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val contentFilterRepository: ContentFilterRepository
) {
    
    companion object {
        private const val API_KEY = "df26f7a61a2c85d4a80d5239f7192d70" // TMDB API Key
    }
    
    /**
     * Get popular movies with optional profile filtering.
     */
    fun getPopularMovies(profile: Profile? = null): Flow<List<VideoContent>> = flow {
        try {
            val response = tmdbApi.getPopularMovies(API_KEY)
            if (response.isSuccessful) {
                val movies = response.body()?.results?.map { it.toVideoContent() } ?: emptyList()
                val filteredMovies = profile?.let { 
                    contentFilterRepository.filterContent(
                        profile = it,
                        content = movies,
                        getRating = { null }, // TODO: Implement rating mapping
                        getGenres = { it.genres }
                    )
                } ?: movies
                emit(filteredMovies)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get top-rated movies.
     */
    fun getTopRatedMovies(profile: Profile? = null): Flow<List<VideoContent>> = flow {
        try {
            val response = tmdbApi.getTopRatedMovies(API_KEY)
            if (response.isSuccessful) {
                val movies = response.body()?.results?.map { it.toVideoContent() } ?: emptyList()
                val filteredMovies = profile?.let { 
                    contentFilterRepository.filterContent(
                        profile = it,
                        content = movies,
                        getRating = { null },
                        getGenres = { it.genres }
                    )
                } ?: movies
                emit(filteredMovies)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get now playing movies.
     */
    fun getNowPlayingMovies(profile: Profile? = null): Flow<List<VideoContent>> = flow {
        try {
            val response = tmdbApi.getNowPlayingMovies(API_KEY)
            if (response.isSuccessful) {
                val movies = response.body()?.results?.map { it.toVideoContent() } ?: emptyList()
                val filteredMovies = profile?.let { 
                    contentFilterRepository.filterContent(
                        profile = it,
                        content = movies,
                        getRating = { null },
                        getGenres = { it.genres }
                    )
                } ?: movies
                emit(filteredMovies)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get popular TV shows.
     */
    fun getPopularTvShows(profile: Profile? = null): Flow<List<VideoContent>> = flow {
        try {
            val response = tmdbApi.getPopularTvShows(API_KEY)
            if (response.isSuccessful) {
                val tvShows = response.body()?.results?.map { it.toVideoContent() } ?: emptyList()
                val filteredTvShows = profile?.let { 
                    contentFilterRepository.filterContent(
                        profile = it,
                        content = tvShows,
                        getRating = { null },
                        getGenres = { it.genres }
                    )
                } ?: tvShows
                emit(filteredTvShows)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get top-rated TV shows.
     */
    fun getTopRatedTvShows(profile: Profile? = null): Flow<List<VideoContent>> = flow {
        try {
            val response = tmdbApi.getTopRatedTvShows(API_KEY)
            if (response.isSuccessful) {
                val tvShows = response.body()?.results?.map { it.toVideoContent() } ?: emptyList()
                val filteredTvShows = profile?.let { 
                    contentFilterRepository.filterContent(
                        profile = it,
                        content = tvShows,
                        getRating = { null },
                        getGenres = { it.genres }
                    )
                } ?: tvShows
                emit(filteredTvShows)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get trending content (all types).
     */
    fun getTrendingContent(profile: Profile? = null): Flow<List<VideoContent>> = flow {
        try {
            val response = tmdbApi.getTrendingDay(API_KEY)
            if (response.isSuccessful) {
                val content = response.body()?.results?.map { it.toVideoContent() } ?: emptyList()
                val filteredContent = profile?.let { 
                    contentFilterRepository.filterContent(
                        profile = it,
                        content = content,
                        getRating = { null },
                        getGenres = { it.genres }
                    )
                } ?: content
                emit(filteredContent)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Search for content.
     */
    fun searchContent(query: String, profile: Profile? = null): Flow<List<VideoContent>> = flow {
        try {
            if (query.isBlank()) {
                emit(emptyList())
                return@flow
            }
            
            val response = tmdbApi.searchMulti(API_KEY, query)
            if (response.isSuccessful) {
                val content = response.body()?.results?.map { it.toVideoContent() } ?: emptyList()
                val filteredContent = profile?.let { 
                    contentFilterRepository.filterContent(
                        profile = it,
                        content = content,
                        getRating = { null },
                        getGenres = { it.genres }
                    )
                } ?: content
                emit(filteredContent)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get movie details.
     */
    suspend fun getMovieDetails(movieId: Int): Result<VideoContent> {
        return try {
            val response = tmdbApi.getMovieDetails(movieId, API_KEY)
            if (response.isSuccessful) {
                val movieDetails = response.body()?.toVideoContent()
                if (movieDetails != null) {
                    Result.success(movieDetails)
                } else {
                    Result.failure(Exception("Movie details not found"))
                }
            } else {
                Result.failure(Exception("Failed to fetch movie details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get TV show details.
     */
    suspend fun getTvShowDetails(tvId: Int): Result<VideoContent> {
        return try {
            val response = tmdbApi.getTvShowDetails(tvId, API_KEY)
            if (response.isSuccessful) {
                val tvDetails = response.body()?.toVideoContent()
                if (tvDetails != null) {
                    Result.success(tvDetails)
                } else {
                    Result.failure(Exception("TV show details not found"))
                }
            } else {
                Result.failure(Exception("Failed to fetch TV show details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get content by genre.
     */
    fun getContentByGenre(genreId: Int, profile: Profile? = null): Flow<List<VideoContent>> = flow {
        try {
            // Discover movies with genre
            val movieResponse = tmdbApi.discoverMovies(
                apiKey = API_KEY,
                withGenres = genreId.toString()
            )
            
            // Discover TV shows with genre
            val tvResponse = tmdbApi.discoverTvShows(
                apiKey = API_KEY,
                withGenres = genreId.toString()
            )
            
            val movies = if (movieResponse.isSuccessful) {
                movieResponse.body()?.results?.map { it.toVideoContent() } ?: emptyList()
            } else emptyList()
            
            val tvShows = if (tvResponse.isSuccessful) {
                tvResponse.body()?.results?.map { it.toVideoContent() } ?: emptyList()
            } else emptyList()
            
            val allContent = (movies + tvShows).sortedByDescending { it.voteAverage }
            val filteredContent = profile?.let { 
                contentFilterRepository.filterContent(
                    profile = it,
                    content = allContent,
                    getRating = { null },
                    getGenres = { it.genres }
                )
            } ?: allContent
            
            emit(filteredContent)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get available genres.
     */
    suspend fun getGenres(): Result<List<Genre>> {
        return try {
            val movieGenresResponse = tmdbApi.getMovieGenres(API_KEY)
            val tvGenresResponse = tmdbApi.getTvGenres(API_KEY)
            
            val movieGenres = if (movieGenresResponse.isSuccessful) {
                movieGenresResponse.body()?.genres?.map { it.toGenre() } ?: emptyList()
            } else emptyList()
            
            val tvGenres = if (tvGenresResponse.isSuccessful) {
                tvGenresResponse.body()?.genres?.map { it.toGenre() } ?: emptyList()
            } else emptyList()
            
            // Combine and deduplicate genres
            val allGenres = (movieGenres + tvGenres).distinctBy { it.id }
            Result.success(allGenres)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Extension functions to convert DTOs to domain models.
 */
private fun TmdbMovieDto.toVideoContent(): VideoContent {
    return VideoContent(
        id = "movie/$id",
        title = title,
        type = ContentType.MOVIE,
        tmdbId = id.toString(),
        backdropUrl = backdropPath?.let { "${TmdbApi.IMAGE_BASE_URL}${TmdbApi.BACKDROP_SIZE}$it" },
        posterUrl = posterPath?.let { "${TmdbApi.IMAGE_BASE_URL}${TmdbApi.POSTER_SIZE}$it" },
        overview = overview,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        genres = emptyList() // Genre names would need to be resolved from IDs
    )
}

private fun TmdbTvDto.toVideoContent(): VideoContent {
    return VideoContent(
        id = "tv/$id",
        title = name,
        type = ContentType.TV_SHOW,
        tmdbId = id.toString(),
        backdropUrl = backdropPath?.let { "${TmdbApi.IMAGE_BASE_URL}${TmdbApi.BACKDROP_SIZE}$it" },
        posterUrl = posterPath?.let { "${TmdbApi.IMAGE_BASE_URL}${TmdbApi.POSTER_SIZE}$it" },
        overview = overview,
        releaseDate = firstAirDate,
        voteAverage = voteAverage,
        genres = emptyList() // Genre names would need to be resolved from IDs
    )
}

private fun TmdbMultiDto.toVideoContent(): VideoContent {
    return VideoContent(
        id = "$mediaType/$id",
        title = title ?: name ?: "Unknown",
        type = if (mediaType == "movie") ContentType.MOVIE else ContentType.TV_SHOW,
        tmdbId = id.toString(),
        backdropUrl = backdropPath?.let { "${TmdbApi.IMAGE_BASE_URL}${TmdbApi.BACKDROP_SIZE}$it" },
        posterUrl = posterPath?.let { "${TmdbApi.IMAGE_BASE_URL}${TmdbApi.POSTER_SIZE}$it" },
        overview = overview,
        releaseDate = releaseDate ?: firstAirDate,
        voteAverage = voteAverage,
        genres = emptyList()
    )
}

private fun TmdbMovieDetailsDto.toVideoContent(): VideoContent {
    return VideoContent(
        id = "movie/$id",
        title = title,
        type = ContentType.MOVIE,
        tmdbId = id.toString(),
        backdropUrl = backdropPath?.let { "${TmdbApi.IMAGE_BASE_URL}${TmdbApi.BACKDROP_SIZE}$it" },
        posterUrl = posterPath?.let { "${TmdbApi.IMAGE_BASE_URL}${TmdbApi.POSTER_SIZE}$it" },
        overview = overview,
        releaseDate = releaseDate,
        runtime = runtime,
        voteAverage = voteAverage,
        genres = genres.map { it.name }
    )
}

private fun TmdbTvDetailsDto.toVideoContent(): VideoContent {
    return VideoContent(
        id = "tv/$id",
        title = name,
        type = ContentType.TV_SHOW,
        tmdbId = id.toString(),
        backdropUrl = backdropPath?.let { "${TmdbApi.IMAGE_BASE_URL}${TmdbApi.BACKDROP_SIZE}$it" },
        posterUrl = posterPath?.let { "${TmdbApi.IMAGE_BASE_URL}${TmdbApi.POSTER_SIZE}$it" },
        overview = overview,
        releaseDate = firstAirDate,
        voteAverage = voteAverage,
        genres = genres.map { it.name },
        seasonCount = numberOfSeasons,
        episodeCount = numberOfEpisodes
    )
}

private fun TmdbGenreDto.toGenre(): Genre {
    return Genre(
        id = id,
        name = name
    )
}

/**
 * Genre domain model.
 */
data class Genre(
    val id: Int,
    val name: String
)

