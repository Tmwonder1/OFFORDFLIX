import dotenv from 'dotenv';
import { strings } from '../strings.js';
import { ErrorObject } from './ErrorObject.js';

dotenv.config();
const apiKey = process.env.TMDB_API_KEY || 'demo_key_for_testing';

/**
 * Fetches movie information from TMDB API using the movie ID
 * @param {string|number} tmdb_id - The TMDB ID of the movie
 * @returns {Promise<Object|ErrorObject>} Object containing movie information or Error if any part of the request fails
 * @property {string} type - Always "movie"
 * @property {string} title - Original title of the movie
 * @property {string} name - Original title of the movie
 * @property {number} releaseYear - Year the movie was released
 * @property {string|number} tmdb - TMDB ID of the movie
 * @property {string} imdb - IMDB ID of the movie
 */
export async function getMovieFromTmdb(tmdb_id) {
    try {
        // Check if API key is properly configured
        if (!apiKey || apiKey === 'demo_key_for_testing' || apiKey === 'your_tmdb_api_key_here') {
            return new ErrorObject(
                'TMDB API key not configured',
                'server',
                503,
                'Please set TMDB_API_KEY in your environment variables',
                true,
                false
            );
        }

        const url = `https://api.themoviedb.org/3/movie/${tmdb_id}?api_key=${apiKey}`;
        const response = await fetch(url);
        if (response.status !== 200) {
            return new ErrorObject(
                strings.INVALID_MOVIE_ID,
                'user',
                404,
                strings.INVALID_MOVIE_ID_HINT,
                true,
                false
            );
        }
        const data = await response.json();
        if (new Date(data.release_date) > new Date().getTime()) {
            return new ErrorObject(
                'This movie has not been released.',
                'user',
                400,
                strings.INVALID_MOVIE_ID_HINT,
                true,
                false
            );
        }

        let secondData = await fetch(
            `https://api.themoviedb.org/3/movie/${tmdb_id}/external_ids?api_key=${apiKey}`
        );
        if (secondData.status !== 200) {
            return new ErrorObject(
                strings.INVALID_MOVIE_ID,
                'user',
                404,
                strings.INVALID_MOVIE_ID_HINT,
                true,
                false
            );
        }
        secondData = await secondData.json();

        return {
            type: 'movie',
            title: data.original_title,
            name: data.original_title,
            releaseYear: Number(data.release_date.split('-')[0]),
            tmdb: tmdb_id,
            imdb: secondData.imdb_id
        };
    } catch (e) {
        return new ErrorObject(
            'An error occurred' + e,
            'backend',
            500,
            undefined,
            true,
            true
        );
    }
}

/**
 * Fetches TV show episode information from TMDB API
 * @param {string|number} tmdb_id - The TMDB ID of the TV show
 * @param {string|number} season - Season number
 * @param {string|number} episode - Episode number
 * @returns {Promise<Object|ErrorObject>} Object containing episode information or Error if any part of the request fails
 * @property {string} type - Always "tv"
 * @property {string|number} releaseYear - Year the episode was aired
 * @property {string|number} tmdb - TMDB ID of the show (duplicate)
 * @property {string} imdb - IMDB ID of the show (duplicate)
 * @property {string|number} season - Season number
 * @property {string|number} episode - Episode number
 * @property {string|number} episodeid - Episode number (duplicate)
 * @property {string} episodeName - Name of the episode
 */
// Enhanced TMDB API functions for complete content discovery

/**
 * Get trending movies from TMDB
 */
export async function getTrendingMovies(timeWindow = 'day', page = 1) {
    try {
        const url = `https://api.themoviedb.org/3/trending/movie/${timeWindow}?api_key=${apiKey}&page=${page}`;
        const response = await fetch(url);
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }
        const data = await response.json();
        return data.results.map(formatMovieData);
    } catch (error) {
        console.error('Error fetching trending movies:', error);
        return [];
    }
}

/**
 * Get popular movies from TMDB
 */
export async function getPopularMovies(page = 1) {
    try {
        const url = `https://api.themoviedb.org/3/movie/popular?api_key=${apiKey}&page=${page}`;
        const response = await fetch(url);
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }
        const data = await response.json();
        return data.results.map(formatMovieData);
    } catch (error) {
        console.error('Error fetching popular movies:', error);
        return [];
    }
}

/**
 * Get top rated movies from TMDB
 */
export async function getTopRatedMovies(page = 1) {
    try {
        const url = `https://api.themoviedb.org/3/movie/top_rated?api_key=${apiKey}&page=${page}`;
        const response = await fetch(url);
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }
        const data = await response.json();
        return data.results.map(formatMovieData);
    } catch (error) {
        console.error('Error fetching top rated movies:', error);
        return [];
    }
}

/**
 * Get upcoming movies from TMDB
 */
export async function getUpcomingMovies(page = 1) {
    try {
        const url = `https://api.themoviedb.org/3/movie/upcoming?api_key=${apiKey}&page=${page}`;
        const response = await fetch(url);
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }
        const data = await response.json();
        return data.results.map(formatMovieData);
    } catch (error) {
        console.error('Error fetching upcoming movies:', error);
        return [];
    }
}

/**
 * Get now playing movies from TMDB
 */
export async function getNowPlayingMovies(page = 1) {
    try {
        const url = `https://api.themoviedb.org/3/movie/now_playing?api_key=${apiKey}&page=${page}`;
        const response = await fetch(url);
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }
        const data = await response.json();
        return data.results.map(formatMovieData);
    } catch (error) {
        console.error('Error fetching now playing movies:', error);
        return [];
    }
}

/**
 * Get trending TV shows from TMDB
 */
export async function getTrendingTvShows(timeWindow = 'day', page = 1) {
    try {
        const url = `https://api.themoviedb.org/3/trending/tv/${timeWindow}?api_key=${apiKey}&page=${page}`;
        const response = await fetch(url);
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }
        const data = await response.json();
        return data.results.map(formatTvData);
    } catch (error) {
        console.error('Error fetching trending TV shows:', error);
        return [];
    }
}

/**
 * Get popular TV shows from TMDB
 */
export async function getPopularTvShows(page = 1) {
    try {
        const url = `https://api.themoviedb.org/3/tv/popular?api_key=${apiKey}&page=${page}`;
        const response = await fetch(url);
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }
        const data = await response.json();
        return data.results.map(formatTvData);
    } catch (error) {
        console.error('Error fetching popular TV shows:', error);
        return [];
    }
}

/**
 * Get top rated TV shows from TMDB
 */
export async function getTopRatedTvShows(page = 1) {
    try {
        const url = `https://api.themoviedb.org/3/tv/top_rated?api_key=${apiKey}&page=${page}`;
        const response = await fetch(url);
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }
        const data = await response.json();
        return data.results.map(formatTvData);
    } catch (error) {
        console.error('Error fetching top rated TV shows:', error);
        return [];
    }
}

/**
 * Get on air TV shows from TMDB
 */
export async function getOnAirTvShows(page = 1) {
    try {
        const url = `https://api.themoviedb.org/3/tv/on_the_air?api_key=${apiKey}&page=${page}`;
        const response = await fetch(url);
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }
        const data = await response.json();
        return data.results.map(formatTvData);
    } catch (error) {
        console.error('Error fetching on air TV shows:', error);
        return [];
    }
}

/**
 * Search movies and TV shows from TMDB
 */
export async function searchMovies(query, page = 1) {
    try {
        const url = `https://api.themoviedb.org/3/search/multi?api_key=${apiKey}&query=${encodeURIComponent(query)}&page=${page}`;
        const response = await fetch(url);
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }
        const data = await response.json();
        return {
            results: data.results.map(item => {
                if (item.media_type === 'movie') {
                    return formatMovieData(item);
                } else if (item.media_type === 'tv') {
                    return formatTvData(item);
                } else {
                    return null;
                }
            }).filter(Boolean),
            total_pages: data.total_pages,
            total_results: data.total_results,
            page: data.page
        };
    } catch (error) {
        console.error('Error searching content:', error);
        return { results: [], total_pages: 0, total_results: 0, page: 1 };
    }
}

/**
 * Get movie genres from TMDB
 */
export async function getGenres() {
    try {
        const [movieGenres, tvGenres] = await Promise.all([
            fetch(`https://api.themoviedb.org/3/genre/movie/list?api_key=${apiKey}`),
            fetch(`https://api.themoviedb.org/3/genre/tv/list?api_key=${apiKey}`)
        ]);

        if (movieGenres.status !== 200 || tvGenres.status !== 200) {
            throw new Error('TMDB API error');
        }

        const [movieData, tvData] = await Promise.all([
            movieGenres.json(),
            tvGenres.json()
        ]);

        // Combine and deduplicate genres
        const allGenres = [...movieData.genres, ...tvData.genres];
        const uniqueGenres = allGenres.filter((genre, index, self) =>
            index === self.findIndex(g => g.id === genre.id)
        );

        return uniqueGenres;
    } catch (error) {
        console.error('Error fetching genres:', error);
        return [];
    }
}

/**
 * Get movies by genre from TMDB
 */
export async function getMoviesByGenre(genreId, page = 1) {
    try {
        const url = `https://api.themoviedb.org/3/discover/movie?api_key=${apiKey}&with_genres=${genreId}&page=${page}&sort_by=popularity.desc`;
        const response = await fetch(url);
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }
        const data = await response.json();
        return data.results.map(formatMovieData);
    } catch (error) {
        console.error('Error fetching movies by genre:', error);
        return [];
    }
}

/**
 * Get TV shows by genre from TMDB
 */
export async function getTvShowsByGenre(genreId, page = 1) {
    try {
        const url = `https://api.themoviedb.org/3/discover/tv?api_key=${apiKey}&with_genres=${genreId}&page=${page}&sort_by=popularity.desc`;
        const response = await fetch(url);
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }
        const data = await response.json();
        return data.results.map(formatTvData);
    } catch (error) {
        console.error('Error fetching TV shows by genre:', error);
        return [];
    }
}

/**
 * Get detailed movie information including credits
 */
export async function getMovieDetails(tmdbId) {
    try {
        const [movieResponse, creditsResponse] = await Promise.all([
            fetch(`https://api.themoviedb.org/3/movie/${tmdbId}?api_key=${apiKey}`),
            fetch(`https://api.themoviedb.org/3/movie/${tmdbId}/credits?api_key=${apiKey}`)
        ]);

        if (movieResponse.status !== 200) {
            throw new Error(`Movie not found: ${movieResponse.status}`);
        }

        const [movieData, creditsData] = await Promise.all([
            movieResponse.json(),
            creditsResponse.json()
        ]);

        return {
            ...formatMovieData(movieData),
            overview: movieData.overview,
            runtime: movieData.runtime,
            budget: movieData.budget,
            revenue: movieData.revenue,
            production_companies: movieData.production_companies,
            production_countries: movieData.production_countries,
            spoken_languages: movieData.spoken_languages,
            cast: creditsData.cast?.slice(0, 20) || [],
            crew: creditsData.crew?.slice(0, 10) || [],
            director: creditsData.crew?.find(person => person.job === 'Director')?.name || null,
            writers: creditsData.crew?.filter(person => person.job === 'Writer' || person.job === 'Screenplay')?.map(p => p.name) || []
        };
    } catch (error) {
        console.error('Error fetching movie details:', error);
        throw error;
    }
}

/**
 * Get detailed TV show information including credits and seasons
 */
export async function getTvShowDetails(tmdbId) {
    try {
        const [tvResponse, creditsResponse] = await Promise.all([
            fetch(`https://api.themoviedb.org/3/tv/${tmdbId}?api_key=${apiKey}`),
            fetch(`https://api.themoviedb.org/3/tv/${tmdbId}/credits?api_key=${apiKey}`)
        ]);

        if (tvResponse.status !== 200) {
            throw new Error(`TV show not found: ${tvResponse.status}`);
        }

        const [tvData, creditsData] = await Promise.all([
            tvResponse.json(),
            creditsResponse.json()
        ]);

        return {
            ...formatTvData(tvData),
            overview: tvData.overview,
            number_of_seasons: tvData.number_of_seasons,
            number_of_episodes: tvData.number_of_episodes,
            episode_run_time: tvData.episode_run_time,
            seasons: tvData.seasons,
            networks: tvData.networks,
            production_companies: tvData.production_companies,
            production_countries: tvData.production_countries,
            spoken_languages: tvData.spoken_languages,
            cast: creditsData.cast?.slice(0, 20) || [],
            crew: creditsData.crew?.slice(0, 10) || [],
            creators: tvData.created_by || []
        };
    } catch (error) {
        console.error('Error fetching TV show details:', error);
        throw error;
    }
}

/**
 * Get TV show season details with episodes
 */
export async function getTvSeasonDetails(tmdbId, seasonNumber) {
    try {
        const url = `https://api.themoviedb.org/3/tv/${tmdbId}/season/${seasonNumber}?api_key=${apiKey}`;
        const response = await fetch(url);
        if (response.status !== 200) {
            throw new Error(`Season not found: ${response.status}`);
        }
        const data = await response.json();
        return {
            id: data.id,
            season_number: data.season_number,
            name: data.name,
            overview: data.overview,
            air_date: data.air_date,
            poster_path: data.poster_path,
            episodes: data.episodes.map(episode => ({
                id: episode.id,
                episode_number: episode.episode_number,
                season_number: episode.season_number,
                name: episode.name,
                overview: episode.overview,
                air_date: episode.air_date,
                runtime: episode.runtime,
                still_path: episode.still_path,
                vote_average: episode.vote_average,
                vote_count: episode.vote_count
            }))
        };
    } catch (error) {
        console.error('Error fetching season details:', error);
        throw error;
    }
}

/**
 * Get similar movies/shows
 */
export async function getSimilarContent(tmdbId, mediaType = 'movie', page = 1) {
    try {
        const url = `https://api.themoviedb.org/3/${mediaType}/${tmdbId}/similar?api_key=${apiKey}&page=${page}`;
        const response = await fetch(url);
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }
        const data = await response.json();
        return data.results.map(item => 
            mediaType === 'movie' ? formatMovieData(item) : formatTvData(item)
        );
    } catch (error) {
        console.error('Error fetching similar content:', error);
        return [];
    }
}

/**
 * Get recommended movies/shows
 */
export async function getRecommendedContent(tmdbId, mediaType = 'movie', page = 1) {
    try {
        const url = `https://api.themoviedb.org/3/${mediaType}/${tmdbId}/recommendations?api_key=${apiKey}&page=${page}`;
        const response = await fetch(url);
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }
        const data = await response.json();
        return data.results.map(item => 
            mediaType === 'movie' ? formatMovieData(item) : formatTvData(item)
        );
    } catch (error) {
        console.error('Error fetching recommended content:', error);
        return [];
    }
}

/**
 * Format movie data to consistent structure
 */
function formatMovieData(movie) {
    return {
        id: movie.id,
        title: movie.title,
        original_title: movie.original_title,
        overview: movie.overview,
        release_date: movie.release_date,
        poster_path: movie.poster_path ? `https://image.tmdb.org/t/p/w500${movie.poster_path}` : null,
        backdrop_path: movie.backdrop_path ? `https://image.tmdb.org/t/p/w1280${movie.backdrop_path}` : null,
        vote_average: movie.vote_average,
        vote_count: movie.vote_count,
        popularity: movie.popularity,
        adult: movie.adult,
        genre_ids: movie.genre_ids || [],
        original_language: movie.original_language,
        media_type: 'movie',
        tmdb_id: movie.id
    };
}

/**
 * Format TV show data to consistent structure
 */
function formatTvData(tv) {
    return {
        id: tv.id,
        name: tv.name,
        title: tv.name, // For consistency with movie format
        original_name: tv.original_name,
        overview: tv.overview,
        first_air_date: tv.first_air_date,
        release_date: tv.first_air_date, // For consistency with movie format
        poster_path: tv.poster_path ? `https://image.tmdb.org/t/p/w500${tv.poster_path}` : null,
        backdrop_path: tv.backdrop_path ? `https://image.tmdb.org/t/p/w1280${tv.backdrop_path}` : null,
        vote_average: tv.vote_average,
        vote_count: tv.vote_count,
        popularity: tv.popularity,
        genre_ids: tv.genre_ids || [],
        original_language: tv.original_language,
        origin_country: tv.origin_country,
        media_type: 'tv',
        tmdb_id: tv.id
    };
}

export async function getTvFromTmdb(tmdb_id, season, episode) {
    try {
        const url = `https://api.themoviedb.org/3/tv/${tmdb_id}/season/${season}/episode/${episode}?api_key=${apiKey}&append_to_response=external_ids`;
        const response = await fetch(url);
        if (response.status !== 200) {
            return new ErrorObject(
                strings.INVALID_TV_ID,
                'user',
                404,
                strings.INVALID_TV_ID_HINT,
                true,
                false
            );
        }
        const data = await response.json();
        if (new Date(data.air_date) > new Date().getTime()) {
            return new ErrorObject(
                'This episode has not been released yet.',
                'user',
                405,
                undefined,
                true,
                false
            );
        }
        let secondData = await fetch(
            `https://api.themoviedb.org/3/tv/${tmdb_id}?api_key=${apiKey}`
        );
        if (secondData.status !== 200) {
            return new ErrorObject(
                strings.INVALID_TV_ID,
                'user',
                404,
                strings.INVALID_TV_ID_HINT,
                true,
                false
            );
        }
        secondData = await secondData.json();
        let title = secondData.name;

        let thirdData = await fetch(
            `https://api.themoviedb.org/3/tv/${tmdb_id}/external_ids?api_key=${apiKey}`
        );
        if (thirdData.status !== 200) {
            return new ErrorObject(
                strings.INVALID_TV_ID,
                'user',
                404,
                strings.INVALID_TV_ID_HINT,
                true,
                false
            );
        }
        thirdData = await thirdData.json();

        return {
            type: 'tv',
            name: title,
            releaseYear: data.air_date.split('-')[0],
            tmdb: tmdb_id,
            imdb: thirdData.imdb_id,
            season: season,
            episode: episode,
            episodeName: data.name
        };
    } catch (e) {
        return new ErrorObject(
            'An error occurred' + e,
            'backend',
            500,
            undefined,
            true,
            true
        );
    }
}
