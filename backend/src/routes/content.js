import express from 'express';
import { authenticateToken } from './auth.js';
import { ErrorObject } from '../helpers/ErrorObject.js';
import { handleErrorResponse } from '../helpers/helper.js';
import { 
    getMovieFromTmdb, 
    getTvFromTmdb, 
    searchMovies, 
    getPopularMovies, 
    getTrendingMovies, 
    getTopRatedMovies, 
    getUpcomingMovies,
    getNowPlayingMovies,
    getPopularTvShows,
    getTrendingTvShows,
    getTopRatedTvShows,
    getOnAirTvShows,
    getGenres,
    getMoviesByGenre,
    getTvShowsByGenre,
    getMovieDetails,
    getTvShowDetails,
    getTvSeasonDetails,
    getSimilarContent,
    getRecommendedContent
} from '../helpers/tmdb.js';

const router = express.Router();

// Mock data for home screen
const homeShortcuts = [
    { id: 1, title: "Movies", type: "MOVIE", icon: "ic_movies", action: "/movies" },
    { id: 2, title: "TV Series", type: "TV", icon: "ic_tv_series", action: "/tv-series" },
    { id: 3, title: "Genres", type: "GENRE", icon: "ic_genres", action: "/genres" },
    { id: 4, title: "My List", type: "WATCHLIST", icon: "ic_my_list", action: "/my-list" }
];

const homeFilters = [
    { id: 1, title: "All", value: "all", selected: true },
    { id: 2, title: "Movies", value: "movie", selected: false },
    { id: 3, title: "TV Shows", value: "tv", selected: false },
    { id: 4, title: "Recently Added", value: "recent", selected: false }
];

const homeTabs = [
    { id: 1, title: "For You", value: "for_you", selected: true },
    { id: 2, title: "Trending", value: "trending", selected: false },
    { id: 3, title: "Popular", value: "popular", selected: false },
    { id: 4, title: "Top Rated", value: "top_rated", selected: false }
];

// GET /api/content/home
router.get('/home', authenticateToken, async (req, res) => {
    try {
        // Get comprehensive content for home screen
        const [
            trendingMovies,
            popularMovies, 
            topRatedMovies,
            upcomingMovies,
            nowPlayingMovies,
            trendingTvShows,
            popularTvShows,
            topRatedTvShows
        ] = await Promise.all([
            getTrendingMovies(),
            getPopularMovies(),
            getTopRatedMovies(), 
            getUpcomingMovies(),
            getNowPlayingMovies(),
            getTrendingTvShows(),
            getPopularTvShows(),
            getTopRatedTvShows()
        ]);

        // Get user's continue watching list (from user data)
        const continueWatching = []; // This would come from user-specific data

        // Create rich home content structure
        const homeResponse = {
            id: "home_main",
            type: 1,
            title: "Welcome to OnStream TV",
            shortcut: homeShortcuts,
            slide: [
                ...trendingMovies.slice(0, 3),
                ...trendingTvShows.slice(0, 2)
            ], // Top 5 for hero slider
            filter: homeFilters,
            sections: [
                {
                    id: "trending_movies",
                    title: "Trending Movies",
                    type: "horizontal_list",
                    data: trendingMovies.slice(0, 20)
                },
                {
                    id: "popular_movies", 
                    title: "Popular Movies",
                    type: "horizontal_list",
                    data: popularMovies.slice(0, 20)
                },
                {
                    id: "trending_tv",
                    title: "Trending TV Shows", 
                    type: "horizontal_list",
                    data: trendingTvShows.slice(0, 20)
                },
                {
                    id: "top_rated_movies",
                    title: "Top Rated Movies",
                    type: "horizontal_list", 
                    data: topRatedMovies.slice(0, 15)
                },
                {
                    id: "now_playing",
                    title: "Now Playing in Theaters",
                    type: "horizontal_list",
                    data: nowPlayingMovies.slice(0, 15)
                },
                {
                    id: "popular_tv",
                    title: "Popular TV Shows",
                    type: "horizontal_list",
                    data: popularTvShows.slice(0, 15)
                },
                {
                    id: "upcoming_movies",
                    title: "Coming Soon",
                    type: "horizontal_list",
                    data: upcomingMovies.slice(0, 10)
                }
            ],
            data: [
                ...trendingMovies.slice(0, 10),
                ...popularMovies.slice(0, 10),
                ...trendingTvShows.slice(0, 10)
            ],
            tabs: homeTabs,
            "continue-watch": continueWatching
        };

        res.status(200).json({
            success: true,
            ...homeResponse
        });
    } catch (error) {
        console.error('Home content error:', error);
        handleErrorResponse(res, new ErrorObject(
            'Failed to load home content',
            'server', 500,
            'Unable to fetch home screen data',
            true, false
        ));
    }
});

// GET /api/content/search
router.get('/search', authenticateToken, async (req, res) => {
    const { q: query, page = 1, type = 'all' } = req.query;

    if (!query || query.trim().length < 2) {
        return handleErrorResponse(res, new ErrorObject(
            'Search query too short',
            'user', 400,
            'Search query must be at least 2 characters long',
            true, false
        ));
    }

    try {
        const searchResults = await searchMovies(query.trim(), page);
        
        // Filter by type if specified
        let filteredResults = searchResults.results || [];
        if (type !== 'all') {
            filteredResults = filteredResults.filter(item => item.media_type === type);
        }

        res.status(200).json({
            success: true,
            query: query.trim(),
            results: filteredResults,
            pagination: {
                current_page: parseInt(page),
                total_pages: searchResults.total_pages || 1,
                total_results: searchResults.total_results || 0
            }
        });
    } catch (error) {
        console.error('Search error:', error);
        handleErrorResponse(res, new ErrorObject(
            'Search failed',
            'server', 500,
            'Unable to perform search',
            true, false
        ));
    }
});

// GET /api/content/genres
router.get('/genres', authenticateToken, async (req, res) => {
    try {
        const genres = await getGenres();
        
        res.status(200).json({
            success: true,
            genres: genres.map(genre => ({
                id: genre.id,
                name: genre.name,
                icon: `ic_genre_${genre.name.toLowerCase().replace(/\s+/g, '_')}`
            }))
        });
    } catch (error) {
        console.error('Genres error:', error);
        handleErrorResponse(res, new ErrorObject(
            'Failed to load genres',
            'server', 500,
            'Unable to fetch genre list',
            true, false
        ));
    }
});

// GET /api/content/movies
router.get('/movies', authenticateToken, async (req, res) => {
    const { 
        page = 1, 
        sort_by = 'popularity.desc', 
        genre = null,
        year = null 
    } = req.query;

    try {
        let movies;
        
        // Enhanced movie categories
        switch (sort_by) {
            case 'trending':
                movies = await getTrendingMovies('day', page);
                break;
            case 'trending_week':
                movies = await getTrendingMovies('week', page);
                break;
            case 'top_rated':
                movies = await getTopRatedMovies(page);
                break;
            case 'upcoming':
                movies = await getUpcomingMovies(page);
                break;
            case 'now_playing':
                movies = await getNowPlayingMovies(page);
                break;
            case 'popular':
            default:
                movies = await getPopularMovies(page);
        }

        // Apply genre filter using TMDB discover API for better results
        if (genre) {
            movies = await getMoviesByGenre(genre, page);
        }

        // Apply year filter
        let filteredMovies = movies;
        if (year) {
            filteredMovies = movies.filter(movie => 
                movie.release_date && movie.release_date.startsWith(year)
            );
        }

        res.status(200).json({
            success: true,
            movies: filteredMovies,
            filters: {
                sort_by,
                genre: genre ? parseInt(genre) : null,
                year,
                page: parseInt(page)
            },
            pagination: {
                current_page: parseInt(page),
                total_pages: 500, // TMDB limit
                has_next_page: parseInt(page) < 500
            }
        });
    } catch (error) {
        console.error('Movies error:', error);
        handleErrorResponse(res, new ErrorObject(
            'Failed to load movies',
            'server', 500,
            'Unable to fetch movie list',
            true, false
        ));
    }
});

// GET /api/content/tv-series
router.get('/tv-series', authenticateToken, async (req, res) => {
    const { 
        page = 1, 
        sort_by = 'popularity.desc',
        genre = null 
    } = req.query;

    try {
        let tvSeries;
        
        // Enhanced TV series categories
        switch (sort_by) {
            case 'trending':
                tvSeries = await getTrendingTvShows('day', page);
                break;
            case 'trending_week':
                tvSeries = await getTrendingTvShows('week', page);
                break;
            case 'top_rated':
                tvSeries = await getTopRatedTvShows(page);
                break;
            case 'on_air':
                tvSeries = await getOnAirTvShows(page);
                break;
            case 'popular':
            default:
                tvSeries = await getPopularTvShows(page);
        }

        // Apply genre filter using TMDB discover API
        if (genre) {
            tvSeries = await getTvShowsByGenre(genre, page);
        }

        res.status(200).json({
            success: true,
            tv_series: tvSeries,
            filters: {
                sort_by,
                genre: genre ? parseInt(genre) : null,
                page: parseInt(page)
            },
            pagination: {
                current_page: parseInt(page),
                total_pages: 500, // TMDB limit
                has_next_page: parseInt(page) < 500
            }
        });
    } catch (error) {
        console.error('TV Series error:', error);
        handleErrorResponse(res, new ErrorObject(
            'Failed to load TV series',
            'server', 500,
            'Unable to fetch TV series list',
            true, false
        ));
    }
});

// GET /api/content/movie/:id
router.get('/movie/:id', authenticateToken, async (req, res) => {
    const { id } = req.params;

    try {
        // Get comprehensive movie details
        const movieDetails = await getMovieDetails(id);
        
        // Get similar and recommended movies
        const [similarMovies, recommendedMovies] = await Promise.all([
            getSimilarContent(id, 'movie').catch(() => []),
            getRecommendedContent(id, 'movie').catch(() => [])
        ]);

        // Enhanced movie details response
        const enhancedMovieDetails = {
            ...movieDetails,
            similar: similarMovies.slice(0, 10),
            recommendations: recommendedMovies.slice(0, 10),
            watched_count: Math.floor(Math.random() * 100000), // Mock data
            user_rating: null, // Would come from user data
            in_watchlist: false, // Would come from user data
            watch_progress: 0, // Would come from user data
            providers: [
                { name: "Netflix", logo: "/netflix.png", available: true },
                { name: "Prime Video", logo: "/prime.png", available: false },
                { name: "Disney+", logo: "/disney.png", available: true }
            ]
        };

        res.status(200).json({
            success: true,
            movie: enhancedMovieDetails
        });
    } catch (error) {
        console.error('Movie details error:', error);
        handleErrorResponse(res, new ErrorObject(
            'Failed to load movie details',
            'server', 500,
            'Unable to fetch movie information',
            true, false
        ));
    }
});

// GET /api/content/tv/:id (TV show details)
router.get('/tv/:id', authenticateToken, async (req, res) => {
    const { id } = req.params;

    try {
        // Get comprehensive TV show details
        const tvDetails = await getTvShowDetails(id);
        
        // Get similar and recommended TV shows
        const [similarShows, recommendedShows] = await Promise.all([
            getSimilarContent(id, 'tv').catch(() => []),
            getRecommendedContent(id, 'tv').catch(() => [])
        ]);

        // Enhanced TV show details response
        const enhancedTvDetails = {
            ...tvDetails,
            similar: similarShows.slice(0, 10),
            recommendations: recommendedShows.slice(0, 10),
            watched_count: Math.floor(Math.random() * 100000), // Mock data
            user_rating: null, // Would come from user data
            in_watchlist: false, // Would come from user data
            watch_progress: {}, // Would come from user data per episode
            providers: [
                { name: "Netflix", logo: "/netflix.png", available: true },
                { name: "Prime Video", logo: "/prime.png", available: false },
                { name: "Disney+", logo: "/disney.png", available: true }
            ]
        };

        res.status(200).json({
            success: true,
            tv_show: enhancedTvDetails
        });
    } catch (error) {
        console.error('TV show details error:', error);
        handleErrorResponse(res, new ErrorObject(
            'Failed to load TV show details',
            'server', 500,
            'Unable to fetch TV show information',
            true, false
        ));
    }
});

// GET /api/content/movie/:id/episodes (for TV shows)
router.get('/movie/:id/episodes', authenticateToken, async (req, res) => {
    const { id } = req.params;
    const { season = 1 } = req.query;

    try {
        // Get real season/episode data from TMDB
        const seasonDetails = await getTvSeasonDetails(id, season);
        
        // Format episodes for app consumption
        const formattedEpisodes = seasonDetails.episodes.map(episode => ({
            ...episode,
            still_path: episode.still_path ? `https://image.tmdb.org/t/p/w300${episode.still_path}` : null,
            user_progress: 0, // Would come from user data
            watched: false, // Would come from user data
            available: true // Mock availability
        }));

        res.status(200).json({
            success: true,
            movie_id: parseInt(id),
            season_number: parseInt(season),
            season_info: {
                id: seasonDetails.id,
                name: seasonDetails.name,
                overview: seasonDetails.overview,
                air_date: seasonDetails.air_date,
                poster_path: seasonDetails.poster_path ? `https://image.tmdb.org/t/p/w500${seasonDetails.poster_path}` : null
            },
            episodes: formattedEpisodes,
            total_episodes: formattedEpisodes.length
        });
    } catch (error) {
        console.error('Episodes error:', error);
        // Fallback to mock data if TMDB fails
        const episodes = Array.from({ length: 10 }, (_, i) => ({
            id: i + 1,
            episode_number: i + 1,
            season_number: parseInt(season),
            name: `Episode ${i + 1}`,
            overview: `Overview for episode ${i + 1}`,
            still_path: null,
            air_date: new Date(2024, 0, i + 1).toISOString().split('T')[0],
            runtime: 45 + Math.floor(Math.random() * 15),
            vote_average: 7.0 + Math.random() * 2,
            user_progress: 0,
            watched: false,
            available: true
        }));

        res.status(200).json({
            success: true,
            movie_id: parseInt(id),
            season_number: parseInt(season),
            episodes: episodes,
            total_episodes: episodes.length
        });
    }
});

// GET /api/content/tv/:id/season/:season (detailed season info)
router.get('/tv/:id/season/:season', authenticateToken, async (req, res) => {
    const { id, season } = req.params;

    try {
        const seasonDetails = await getTvSeasonDetails(id, season);
        
        res.status(200).json({
            success: true,
            tv_id: parseInt(id),
            season: seasonDetails
        });
    } catch (error) {
        console.error('Season details error:', error);
        handleErrorResponse(res, new ErrorObject(
            'Failed to load season details',
            'server', 500,
            'Unable to fetch season information',
            true, false
        ));
    }
});

// GET /api/content/latest-version (for app updates)
router.get('/latest-version', (req, res) => {
    res.status(200).json({
        success: true,
        version: {
            version_name: "1.1.0",
            version_code: 12,
            download_url: "https://example.com/onstream-tv-1.1.0.apk",
            changelog: [
                "Improved video playback quality",
                "Added new content providers",
                "Bug fixes and performance improvements",
                "Enhanced TV remote navigation"
            ],
            required: false,
            release_date: "2024-01-15T00:00:00Z"
        }
    });
});

// POST /api/content/report
router.post('/report', authenticateToken, (req, res) => {
    const { movie_id, episode_id, reason, description } = req.body;

    if (!movie_id || !reason) {
        return handleErrorResponse(res, new ErrorObject(
            'Movie ID and reason are required',
            'user', 400,
            'Please provide movie ID and reason for report',
            true, false
        ));
    }

    // In a real app, you'd save this to a database
    console.log('Content report:', {
        user_id: req.user.id,
        movie_id,
        episode_id,
        reason,
        description,
        reported_at: new Date().toISOString()
    });

    res.status(200).json({
        success: true,
        message: 'Report submitted successfully',
        report_id: Date.now().toString()
    });
});

// GET /api/content/report-topics
router.get('/report-topics', authenticateToken, (req, res) => {
    const topics = [
        { id: 1, title: "Video not working", description: "Video fails to load or play" },
        { id: 2, title: "Wrong content", description: "Video doesn't match title/description" },
        { id: 3, title: "Poor quality", description: "Video quality is too low" },
        { id: 4, title: "Audio issues", description: "Audio is out of sync or missing" },
        { id: 5, title: "Subtitle problems", description: "Subtitles are incorrect or missing" },
        { id: 6, title: "Other", description: "Other technical issues" }
    ];

    res.status(200).json({
        success: true,
        topics: topics
    });
});

// GET /api/content/discover (advanced content discovery)
router.get('/discover', authenticateToken, async (req, res) => {
    const { 
        type = 'movie', // movie or tv
        sort_by = 'popularity.desc',
        with_genres = null,
        without_genres = null,
        year = null,
        rating_gte = null,
        rating_lte = null,
        page = 1
    } = req.query;

    try {
        let discoverUrl = `https://api.themoviedb.org/3/discover/${type}?api_key=${process.env.TMDB_API_KEY}`;
        discoverUrl += `&sort_by=${sort_by}&page=${page}`;
        
        if (with_genres) discoverUrl += `&with_genres=${with_genres}`;
        if (without_genres) discoverUrl += `&without_genres=${without_genres}`;
        if (year) discoverUrl += type === 'movie' ? `&year=${year}` : `&first_air_date_year=${year}`;
        if (rating_gte) discoverUrl += `&vote_average.gte=${rating_gte}`;
        if (rating_lte) discoverUrl += `&vote_average.lte=${rating_lte}`;

        const response = await fetch(discoverUrl);
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }

        const data = await response.json();
        const formatFunction = type === 'movie' ? formatMovieData : formatTvData;
        
        res.status(200).json({
            success: true,
            results: data.results.map(formatFunction),
            pagination: {
                current_page: data.page,
                total_pages: data.total_pages,
                total_results: data.total_results
            },
            filters: {
                type,
                sort_by,
                with_genres,
                without_genres,
                year,
                rating_gte,
                rating_lte
            }
        });
    } catch (error) {
        console.error('Discover error:', error);
        handleErrorResponse(res, new ErrorObject(
            'Failed to discover content',
            'server', 500,
            'Unable to fetch discover results',
            true, false
        ));
    }
});

// GET /api/content/trending (comprehensive trending endpoint)
router.get('/trending', authenticateToken, async (req, res) => {
    const { 
        media_type = 'all', // all, movie, tv
        time_window = 'day', // day, week
        page = 1 
    } = req.query;

    try {
        const url = `https://api.themoviedb.org/3/trending/${media_type}/${time_window}?api_key=${process.env.TMDB_API_KEY}&page=${page}`;
        const response = await fetch(url);
        
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }

        const data = await response.json();
        const formattedResults = data.results.map(item => {
            if (item.media_type === 'movie') {
                return formatMovieData(item);
            } else if (item.media_type === 'tv') {
                return formatTvData(item);
            }
            return null;
        }).filter(Boolean);

        res.status(200).json({
            success: true,
            trending: formattedResults,
            pagination: {
                current_page: data.page,
                total_pages: data.total_pages,
                total_results: data.total_results
            },
            filters: {
                media_type,
                time_window
            }
        });
    } catch (error) {
        console.error('Trending error:', error);
        handleErrorResponse(res, new ErrorObject(
            'Failed to load trending content',
            'server', 500,
            'Unable to fetch trending data',
            true, false
        ));
    }
});

// GET /api/content/collections/:id (movie collections)
router.get('/collections/:id', authenticateToken, async (req, res) => {
    const { id } = req.params;

    try {
        const url = `https://api.themoviedb.org/3/collection/${id}?api_key=${process.env.TMDB_API_KEY}`;
        const response = await fetch(url);
        
        if (response.status !== 200) {
            throw new Error(`Collection not found: ${response.status}`);
        }

        const data = await response.json();
        
        res.status(200).json({
            success: true,
            collection: {
                id: data.id,
                name: data.name,
                overview: data.overview,
                poster_path: data.poster_path ? `https://image.tmdb.org/t/p/w500${data.poster_path}` : null,
                backdrop_path: data.backdrop_path ? `https://image.tmdb.org/t/p/w1280${data.backdrop_path}` : null,
                parts: data.parts.map(formatMovieData)
            }
        });
    } catch (error) {
        console.error('Collection error:', error);
        handleErrorResponse(res, new ErrorObject(
            'Failed to load collection',
            'server', 500,
            'Unable to fetch collection data',
            true, false
        ));
    }
});

// GET /api/content/keywords/:id (content by keyword)
router.get('/keywords/:id', authenticateToken, async (req, res) => {
    const { id } = req.params;
    const { type = 'movie', page = 1 } = req.query;

    try {
        const url = `https://api.themoviedb.org/3/discover/${type}?api_key=${process.env.TMDB_API_KEY}&with_keywords=${id}&page=${page}`;
        const response = await fetch(url);
        
        if (response.status !== 200) {
            throw new Error(`TMDB API error: ${response.status}`);
        }

        const data = await response.json();
        const formatFunction = type === 'movie' ? formatMovieData : formatTvData;
        
        res.status(200).json({
            success: true,
            results: data.results.map(formatFunction),
            pagination: {
                current_page: data.page,
                total_pages: data.total_pages,
                total_results: data.total_results
            }
        });
    } catch (error) {
        console.error('Keywords error:', error);
        handleErrorResponse(res, new ErrorObject(
            'Failed to load keyword content',
            'server', 500,
            'Unable to fetch keyword results',
            true, false
        ));
    }
});

// Helper functions (moved to end of file for clarity)
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

export { router as contentRoutes };

