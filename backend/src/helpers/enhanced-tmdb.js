import { ErrorObject } from './ErrorObject.js';
import { getCacheKey, getFromCache, setToCache } from '../cache/cache.js';

// TMDB API configuration
const TMDB_API_KEY = process.env.TMDB_API_KEY || 'your_tmdb_api_key_here';
const TMDB_BASE_URL = 'https://api.themoviedb.org/3';
const TMDB_IMAGE_BASE_URL = 'https://image.tmdb.org/t/p/w500';

// Common headers for TMDB requests
const headers = {
    'Authorization': `Bearer ${TMDB_API_KEY}`,
    'Content-Type': 'application/json'
};

// Helper function to make TMDB API requests
async function tmdbRequest(endpoint, params = {}) {
    const url = new URL(`${TMDB_BASE_URL}${endpoint}`);
    
    // Add common parameters
    params.api_key = TMDB_API_KEY;
    Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined) {
            url.searchParams.append(key, params[key]);
        }
    });

    const cacheKey = getCacheKey({ url: url.toString() });
    
    // Check cache first
    const cachedResult = getFromCache(cacheKey);
    if (cachedResult) {
        return cachedResult;
    }

    try {
        const response = await fetch(url.toString(), { headers });
        
        if (!response.ok) {
            throw new Error(`TMDB API error: ${response.status} ${response.statusText}`);
        }

        const data = await response.json();
        
        // Cache the result
        setToCache(cacheKey, data);
        
        return data;
    } catch (error) {
        console.error('TMDB request failed:', error);
        throw new ErrorObject(
            'Failed to fetch data from TMDB',
            'server',
            500,
            'Movie database is temporarily unavailable',
            true,
            false
        );
    }
}

// Helper function to extract logo from TMDB images
function getLogoFromImages(images) {
    if (!images || !images.logos || images.logos.length === 0) {
        return null;
    }
    
    // First, try to find logos with votes > 0 (higher quality/community approved)
    const votedLogos = images.logos.filter(logo => logo.vote_average > 0);
    if (votedLogos.length > 0) {
        const bestVotedLogo = votedLogos.reduce((best, current) => {
            return current.vote_average > best.vote_average ? current : best;
        });
        return `${TMDB_IMAGE_BASE_URL}${bestVotedLogo.file_path}`;
    }
    
    // If no voted logos, prefer English logos
    const englishLogos = images.logos.filter(logo => logo.iso_639_1 === 'en');
    if (englishLogos.length > 0) {
        return `${TMDB_IMAGE_BASE_URL}${englishLogos[0].file_path}`;
    }
    
    // Finally, return any available logo
    return `${TMDB_IMAGE_BASE_URL}${images.logos[0].file_path}`;
}

// Transform TMDB movie data to our format
function transformMovie(tmdbMovie) {
    return {
        id: tmdbMovie.id,
        title: tmdbMovie.title || tmdbMovie.name,
        original_title: tmdbMovie.original_title || tmdbMovie.original_name,
        overview: tmdbMovie.overview,
        poster_path: tmdbMovie.poster_path ? `${TMDB_IMAGE_BASE_URL}${tmdbMovie.poster_path}` : null,
        backdrop_path: tmdbMovie.backdrop_path ? `${TMDB_IMAGE_BASE_URL}${tmdbMovie.backdrop_path}` : null,
        logo_path: getLogoFromImages(tmdbMovie.images),
        release_date: tmdbMovie.release_date || tmdbMovie.first_air_date,
        vote_average: tmdbMovie.vote_average,
        vote_count: tmdbMovie.vote_count,
        popularity: tmdbMovie.popularity,
        genre_ids: tmdbMovie.genre_ids || [],
        adult: tmdbMovie.adult || false,
        video: tmdbMovie.video || false,
        original_language: tmdbMovie.original_language,
        media_type: tmdbMovie.media_type || (tmdbMovie.title ? 'movie' : 'tv')
    };
}

// Get movie details by TMDB ID
export async function getMovieFromTmdb(tmdbId) {
    try {
        const data = await tmdbRequest(`/movie/${tmdbId}`, {
            append_to_response: 'credits,videos,similar,reviews,images'
        });

        return {
            ...transformMovie(data),
            runtime: data.runtime,
            budget: data.budget,
            revenue: data.revenue,
            status: data.status,
            tagline: data.tagline,
            homepage: data.homepage,
            imdb_id: data.imdb_id,
            production_companies: data.production_companies,
            production_countries: data.production_countries,
            spoken_languages: data.spoken_languages,
            genres: data.genres,
            credits: data.credits,
            videos: data.videos,
            similar: data.similar,
            reviews: data.reviews
        };
    } catch (error) {
        if (error instanceof ErrorObject) {
            return error;
        }
        return new ErrorObject(
            'Movie not found',
            'user',
            404,
            'The requested movie could not be found',
            true,
            false
        );
    }
}

// Get TV show details by TMDB ID
export async function getTvFromTmdb(tmdbId, season, episode) {
    try {
        const showData = await tmdbRequest(`/tv/${tmdbId}`, {
            append_to_response: 'credits,videos,similar,reviews,images'
        });

        let episodeData = null;
        if (season && episode) {
            try {
                episodeData = await tmdbRequest(`/tv/${tmdbId}/season/${season}/episode/${episode}`);
            } catch (error) {
                console.warn('Episode data not found:', error);
            }
        }

        return {
            ...transformMovie(showData),
            number_of_episodes: showData.number_of_episodes,
            number_of_seasons: showData.number_of_seasons,
            episode_run_time: showData.episode_run_time,
            first_air_date: showData.first_air_date,
            last_air_date: showData.last_air_date,
            in_production: showData.in_production,
            languages: showData.languages,
            networks: showData.networks,
            seasons: showData.seasons,
            type: showData.type,
            current_episode: episodeData,
            season_number: season ? parseInt(season) : null,
            episode_number: episode ? parseInt(episode) : null
        };
    } catch (error) {
        if (error instanceof ErrorObject) {
            return error;
        }
        return new ErrorObject(
            'TV show not found',
            'user',
            404,
            'The requested TV show could not be found',
            true,
            false
        );
    }
}

// Search for movies and TV shows
export async function searchMovies(query, page = 1) {
    try {
        const data = await tmdbRequest('/search/multi', {
            query: query,
            page: page,
            include_adult: false
        });

        return {
            ...data,
            results: data.results.map(transformMovie)
        };
    } catch (error) {
        throw error;
    }
}

// Get popular movies
export async function getPopularMovies(page = 1) {
    try {
        const data = await tmdbRequest('/movie/popular', { page });
        return data.results.map(transformMovie);
    } catch (error) {
        throw error;
    }
}

// Get trending movies
export async function getTrendingMovies(timeWindow = 'week') {
    try {
        const data = await tmdbRequest(`/trending/movie/${timeWindow}`);
        return data.results.map(transformMovie);
    } catch (error) {
        throw error;
    }
}

// Get top rated movies
export async function getTopRatedMovies(page = 1) {
    try {
        const data = await tmdbRequest('/movie/top_rated', { page });
        return data.results.map(transformMovie);
    } catch (error) {
        throw error;
    }
}

// Get upcoming movies
export async function getUpcomingMovies(page = 1) {
    try {
        const data = await tmdbRequest('/movie/upcoming', { page });
        return data.results.map(transformMovie);
    } catch (error) {
        throw error;
    }
}

// Get now playing movies
export async function getNowPlayingMovies(page = 1) {
    try {
        const data = await tmdbRequest('/movie/now_playing', { page });
        return data.results.map(transformMovie);
    } catch (error) {
        throw error;
    }
}

// Get popular TV shows
export async function getPopularTvShows(page = 1) {
    try {
        const data = await tmdbRequest('/tv/popular', { page });
        return data.results.map(transformMovie);
    } catch (error) {
        throw error;
    }
}

// Get top rated TV shows
export async function getTopRatedTvShows(page = 1) {
    try {
        const data = await tmdbRequest('/tv/top_rated', { page });
        return data.results.map(transformMovie);
    } catch (error) {
        throw error;
    }
}

// Get TV shows airing today
export async function getAiringTodayTvShows(page = 1) {
    try {
        const data = await tmdbRequest('/tv/airing_today', { page });
        return data.results.map(transformMovie);
    } catch (error) {
        throw error;
    }
}

// Get genres
export async function getGenres(type = 'movie') {
    try {
        const data = await tmdbRequest(`/genre/${type}/list`);
        return data.genres;
    } catch (error) {
        throw error;
    }
}

// Get movies by genre
export async function getMoviesByGenre(genreId, page = 1) {
    try {
        const data = await tmdbRequest('/discover/movie', {
            with_genres: genreId,
            page: page,
            sort_by: 'popularity.desc'
        });
        return data.results.map(transformMovie);
    } catch (error) {
        throw error;
    }
}

// Get TV shows by genre
export async function getTvShowsByGenre(genreId, page = 1) {
    try {
        const data = await tmdbRequest('/discover/tv', {
            with_genres: genreId,
            page: page,
            sort_by: 'popularity.desc'
        });
        return data.results.map(transformMovie);
    } catch (error) {
        throw error;
    }
}

// Get season details
export async function getSeasonDetails(tvId, seasonNumber) {
    try {
        const data = await tmdbRequest(`/tv/${tvId}/season/${seasonNumber}`);
        return data;
    } catch (error) {
        throw error;
    }
}

// Get episode details
export async function getEpisodeDetails(tvId, seasonNumber, episodeNumber) {
    try {
        const data = await tmdbRequest(`/tv/${tvId}/season/${seasonNumber}/episode/${episodeNumber}`);
        return data;
    } catch (error) {
        throw error;
    }
}

// Get movie credits
export async function getMovieCredits(movieId) {
    try {
        const data = await tmdbRequest(`/movie/${movieId}/credits`);
        return data;
    } catch (error) {
        throw error;
    }
}

// Get similar movies
export async function getSimilarMovies(movieId, page = 1) {
    try {
        const data = await tmdbRequest(`/movie/${movieId}/similar`, { page });
        return data.results.map(transformMovie);
    } catch (error) {
        throw error;
    }
}

// Get movie recommendations
export async function getMovieRecommendations(movieId, page = 1) {
    try {
        const data = await tmdbRequest(`/movie/${movieId}/recommendations`, { page });
        return data.results.map(transformMovie);
    } catch (error) {
        throw error;
    }
}

// Get configuration
export async function getTmdbConfiguration() {
    try {
        const data = await tmdbRequest('/configuration');
        return data;
    } catch (error) {
        throw error;
    }
}


