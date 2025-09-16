import dotenv from 'dotenv';
import { strings } from '../strings.js';
import { ErrorObject } from './ErrorObject.js';

dotenv.config();
const apiKey = process.env.TMDB_API_KEY;

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

/**
 * Search for movies and TV shows using TMDB API
 * @param {string} query - Search query
 * @param {number} page - Page number (default: 1)
 * @returns {Promise<Object|ErrorObject>} Search results or Error
 */
export async function searchContent(query, page = 1) {
    if (!query || query.trim().length === 0) {
        return new ErrorObject(
            'Search query is required',
            'user',
            400,
            'Please provide a search query',
            true,
            false
        );
    }

    try {
        // Search both movies and TV shows
        const [movieResponse, tvResponse] = await Promise.all([
            fetch(`https://api.themoviedb.org/3/search/movie?api_key=${apiKey}&query=${encodeURIComponent(query)}&page=${page}`),
            fetch(`https://api.themoviedb.org/3/search/tv?api_key=${apiKey}&query=${encodeURIComponent(query)}&page=${page}`)
        ]);

        if (movieResponse.status !== 200 || tvResponse.status !== 200) {
            return new ErrorObject(
                'Search failed',
                'backend',
                500,
                'Unable to search TMDB',
                true,
                false
            );
        }

        const movieData = await movieResponse.json();
        const tvData = await tvResponse.json();

        // Format results
        const movies = movieData.results.map(movie => ({
            id: movie.id,
            type: 'movie',
            title: movie.title,
            name: movie.title,
            overview: movie.overview,
            poster_path: movie.poster_path,
            backdrop_path: movie.backdrop_path,
            release_date: movie.release_date,
            vote_average: movie.vote_average,
            genre_ids: movie.genre_ids
        }));

        const tvShows = tvData.results.map(tv => ({
            id: tv.id,
            type: 'tv',
            title: tv.name,
            name: tv.name,
            overview: tv.overview,
            poster_path: tv.poster_path,
            backdrop_path: tv.backdrop_path,
            first_air_date: tv.first_air_date,
            vote_average: tv.vote_average,
            genre_ids: tv.genre_ids
        }));

        return {
            query: query,
            page: page,
            total_results: movieData.total_results + tvData.total_results,
            total_pages: Math.max(movieData.total_pages, tvData.total_pages),
            results: [...movies, ...tvShows]
        };
    } catch (e) {
        return new ErrorObject(
            'Search error: ' + e.message,
            'backend',
            500,
            undefined,
            true,
            true
        );
    }
}

/**
 * Get trending content from TMDB API
 * @param {string} type - 'movie' or 'tv' or 'all' (default: 'all')
 * @param {string} timeWindow - 'day' or 'week' (default: 'week')
 * @param {number} page - Page number (default: 1)
 * @returns {Promise<Object|ErrorObject>} Trending results or Error
 */
export async function getTrendingContent(type = 'all', timeWindow = 'week', page = 1) {
    try {
        const url = `https://api.themoviedb.org/3/trending/${type}/${timeWindow}?api_key=${apiKey}&page=${page}`;
        const response = await fetch(url);
        
        if (response.status !== 200) {
            return new ErrorObject(
                'Failed to get trending content',
                'backend',
                500,
                'Unable to fetch trending data from TMDB',
                true,
                false
            );
        }

        const data = await response.json();

        // Format results with consistent structure
        const results = data.results.map(item => ({
            id: item.id,
            type: item.media_type || type,
            title: item.title || item.name,
            name: item.title || item.name,
            overview: item.overview,
            poster_path: item.poster_path,
            backdrop_path: item.backdrop_path,
            release_date: item.release_date || item.first_air_date,
            vote_average: item.vote_average,
            genre_ids: item.genre_ids,
            popularity: item.popularity
        }));

        return {
            type: type,
            time_window: timeWindow,
            page: page,
            total_results: data.total_results,
            total_pages: data.total_pages,
            results: results
        };
    } catch (e) {
        return new ErrorObject(
            'Trending error: ' + e.message,
            'backend',
            500,
            undefined,
            true,
            true
        );
    }
}
