import express from 'express';
import { scrapeMedia } from './src/api.js';
import {
    createProxyRoutes,
    processApiResponse
} from './src/proxy/proxyserver.js';
import { 
    getMovieFromTmdb, 
    getTvFromTmdb, 
    searchContent, 
    getTrendingContent 
} from './src/helpers/tmdb.js';
import cors from 'cors';
import { strings } from './src/strings.js';
import {
    checkIfPossibleTmdbId,
    handleErrorResponse
} from './src/helpers/helper.js';
import { ErrorObject } from './src/helpers/ErrorObject.js';
import { getCacheStats } from './src/cache/cache.js';
import { 
    apiLimiter, 
    scrapingLimiter, 
    metadataLimiter 
} from './src/middleware/rateLimiter.js';
import { addCacheHeaders, noCacheHeaders } from './src/middleware/cacheHeaders.js';
import { preCacheWorker } from './src/workers/preCache.js';
import { videoProxy } from './src/proxy/videoProxy.js';
import { userAnalytics } from './src/analytics/userAnalytics.js';
import { redisCache } from './src/cache/redisCache.js';

const PORT = process.env.PORT;
const allowedOrigins = ['https://cinepro.mintlify.app/']; // localhost is also allowed. (from any localhost port)
const app = express();

app.use(
    cors({
        origin: (origin, callback) => {
            !origin ||
            allowedOrigins.includes(origin) ||
            /^http:\/\/localhost/.test(origin)
                ? callback(null, true)
                : callback(new Error('Not allowed by CORS'));
        }
    })
);

// Apply global rate limiting
app.use(apiLimiter);

createProxyRoutes(app);

app.get('/', addCacheHeaders('static'), (req, res) => {
    res.status(200).json({
        home: strings.HOME_NAME,
        routes: {
            ...strings.ROUTES,
            SEARCH: '/search?q=query&page=1',
            TRENDING: '/trending?type=all&time_window=week&page=1',
            MOVIE_DETAILS: '/movie/:tmdbID/details (fast - no scraping)',
            MOVIE_SOURCES: '/movie/:tmdbID/sources (slow - with scraping)',
            TV_DETAILS: '/tv/:tmdbID/details (fast - no scraping)',
            TV_SOURCES: '/tv/:tmdbID/sources?s=season&e=episode (slow - with scraping)'
        },
        information: strings.INFORMATION,
        license: strings.LICENSE,
        source: strings.SOURCE
    });
});

// NEW: Fast movie details endpoint (no scraping)
app.get('/movie/:tmdbId/details', metadataLimiter, addCacheHeaders('metadata'), async (req, res) => {
    if (!checkIfPossibleTmdbId(req.params.tmdbId)) {
        return handleErrorResponse(
            res,
            new ErrorObject(
                strings.INVALID_MOVIE_ID,
                'user',
                405,
                strings.INVALID_MOVIE_ID_HINT,
                true,
                false
            )
        );
    }

    try {
        const media = await getMovieFromTmdb(req.params.tmdbId);
        if (media instanceof ErrorObject) {
            return handleErrorResponse(res, media);
        }

        // Track analytics for details view
        const userId = userAnalytics.getUserId(req);
        const deviceType = userAnalytics.getDeviceType(req.headers['user-agent']);
        await userAnalytics.trackContentRequest(
            req.params.tmdbId, 
            'movie', 
            media.title, 
            userId, 
            { deviceType, requestType: 'details' }
        );

        // Return just the movie details (fast)
        res.status(200).json({
            ...media,
            sources_status: 'loading',
            sources_url: `/movie/${req.params.tmdbId}/sources`
        });
    } catch (error) {
        return handleErrorResponse(res, new ErrorObject(
            'Failed to fetch movie details',
            'server',
            500,
            'Try again later',
            true,
            true
        ));
    }
});

// NEW: Movie sources endpoint (with scraping)
app.get('/movie/:tmdbId/sources', scrapingLimiter, addCacheHeaders('scraping'), async (req, res) => {
    if (!checkIfPossibleTmdbId(req.params.tmdbId)) {
        return handleErrorResponse(
            res,
            new ErrorObject(
                strings.INVALID_MOVIE_ID,
                'user',
                405,
                strings.INVALID_MOVIE_ID_HINT,
                true,
                false
            )
        );
    }

    try {
        const media = await getMovieFromTmdb(req.params.tmdbId);
        if (media instanceof ErrorObject) {
            return handleErrorResponse(res, media);
        }

        // Track analytics for sources request
        const userId = userAnalytics.getUserId(req);
        const deviceType = userAnalytics.getDeviceType(req.headers['user-agent']);
        await userAnalytics.trackContentRequest(
            req.params.tmdbId, 
            'movie', 
            media.title, 
            userId, 
            { deviceType, requestType: 'sources' }
        );

        // Scrape video sources (heavy operation)
        const output = await scrapeMedia(media);
        if (output instanceof ErrorObject) {
            return handleErrorResponse(res, output);
        }
        
        // Process video sources through proxy
        if (output.files) {
            output.files = await videoProxy.processVideoSources(output.files, req.params.tmdbId);
        }
        
        const serverUrl = `${req.protocol}://${req.get('host')}`;
        const processedOutput = processApiResponse(output, serverUrl);

        res.status(200).json({
            tmdb_id: req.params.tmdbId,
            ...processedOutput
        });
    } catch (error) {
        return handleErrorResponse(res, new ErrorObject(
            'Failed to fetch video sources',
            'server',
            500,
            'Try again later',
            true,
            true
        ));
    }
});

// LEGACY: Combined endpoint (for backward compatibility)
app.get('/movie/:tmdbId', scrapingLimiter, addCacheHeaders('scraping'), async (req, res) => {
    if (!checkIfPossibleTmdbId(req.params.tmdbId)) {
        return handleErrorResponse(
            res,
            new ErrorObject(
                strings.INVALID_MOVIE_ID,
                'user',
                405,
                strings.INVALID_MOVIE_ID_HINT,
                true,
                false
            )
        );
    }

    const media = await getMovieFromTmdb(req.params.tmdbId);
    if (media instanceof ErrorObject) {
        return handleErrorResponse(res, media);
    }

    // Track analytics
    const userId = userAnalytics.getUserId(req);
    const deviceType = userAnalytics.getDeviceType(req.headers['user-agent']);
    await userAnalytics.trackContentRequest(
        req.params.tmdbId, 
        'movie', 
        media.title, 
        userId, 
        { deviceType }
    );

    const output = await scrapeMedia(media);
    if (output instanceof ErrorObject) {
        return handleErrorResponse(res, output);
    }
    
    // Process video sources through proxy
    if (output.files) {
        output.files = await videoProxy.processVideoSources(output.files, req.params.tmdbId);
    }
    
    const serverUrl = `${req.protocol}://${req.get('host')}`;
    const processedOutput = processApiResponse(output, serverUrl);

    res.status(200).json(processedOutput);
});

// NEW: Fast TV show details endpoint (no scraping)
app.get('/tv/:tmdbId/details', metadataLimiter, addCacheHeaders('metadata'), async (req, res) => {
    if (!checkIfPossibleTmdbId(req.params.tmdbId)) {
        return handleErrorResponse(
            res,
            new ErrorObject(
                strings.INVALID_TV_ID,
                'user',
                405,
                strings.INVALID_TV_ID_HINT,
                true,
                false
            )
        );
    }

    try {
        // For TV shows, we can return show info without specific episode details
        const media = await getMovieFromTmdb(req.params.tmdbId); // This gets TV show info
        if (media instanceof ErrorObject) {
            return handleErrorResponse(res, media);
        }

        // Track analytics for details view
        const userId = userAnalytics.getUserId(req);
        const deviceType = userAnalytics.getDeviceType(req.headers['user-agent']);
        await userAnalytics.trackContentRequest(
            req.params.tmdbId, 
            'tv', 
            media.title || media.name, 
            userId, 
            { deviceType, requestType: 'details' }
        );

        // Return just the TV show details (fast)
        res.status(200).json({
            ...media,
            type: 'tv',
            sources_status: 'requires_episode',
            sources_url: `/tv/${req.params.tmdbId}/sources?s={season}&e={episode}`
        });
    } catch (error) {
        return handleErrorResponse(res, new ErrorObject(
            'Failed to fetch TV show details',
            'server',
            500,
            'Try again later',
            true,
            true
        ));
    }
});

// NEW: TV episode sources endpoint (with scraping)
app.get('/tv/:tmdbId/sources', scrapingLimiter, addCacheHeaders('scraping'), async (req, res) => {
    if (
        !checkIfPossibleTmdbId(req.params.tmdbId) ||
        !checkIfPossibleTmdbId(req.query.s) ||
        !checkIfPossibleTmdbId(req.query.e)
    ) {
        return handleErrorResponse(
            res,
            new ErrorObject(
                strings.INVALID_TV_ID,
                'user',
                405,
                strings.INVALID_TV_ID_HINT,
                true,
                false
            )
        );
    }

    try {
        const media = await getTvFromTmdb(
            req.params.tmdbId,
            req.query.s,
            req.query.e
        );
        if (media instanceof ErrorObject) {
            return handleErrorResponse(res, media);
        }

        // Track analytics for sources request
        const userId = userAnalytics.getUserId(req);
        const deviceType = userAnalytics.getDeviceType(req.headers['user-agent']);
        await userAnalytics.trackContentRequest(
            req.params.tmdbId, 
            'tv', 
            media.title || media.name, 
            userId, 
            { 
                deviceType, 
                requestType: 'sources',
                season: req.query.s,
                episode: req.query.e
            }
        );

        // Scrape video sources (heavy operation)
        const output = await scrapeMedia(media);
        if (output instanceof ErrorObject) {
            return handleErrorResponse(res, output);
        }
        
        // Process video sources through proxy
        if (output.files) {
            const episodeId = `${req.params.tmdbId}_s${req.query.s}e${req.query.e}`;
            output.files = await videoProxy.processVideoSources(output.files, episodeId);
        }
        
        const serverUrl = `${req.protocol}://${req.get('host')}`;
        const processedOutput = processApiResponse(output, serverUrl);

        res.status(200).json({
            tmdb_id: req.params.tmdbId,
            season: req.query.s,
            episode: req.query.e,
            ...processedOutput
        });
    } catch (error) {
        return handleErrorResponse(res, new ErrorObject(
            'Failed to fetch episode sources',
            'server',
            500,
            'Try again later',
            true,
            true
        ));
    }
});

// LEGACY: Combined TV endpoint (for backward compatibility)
app.get('/tv/:tmdbId', scrapingLimiter, addCacheHeaders('scraping'), async (req, res) => {
    if (
        !checkIfPossibleTmdbId(req.params.tmdbId) ||
        !checkIfPossibleTmdbId(req.query.s) ||
        !checkIfPossibleTmdbId(req.query.e)
    ) {
        return handleErrorResponse(
            res,
            new ErrorObject(
                strings.INVALID_TV_ID,
                'user',
                405,
                strings.INVALID_TV_ID_HINT,
                true,
                false
            )
        );
    }

    const media = await getTvFromTmdb(
        req.params.tmdbId,
        req.query.s,
        req.query.e
    );
    if (media instanceof ErrorObject) {
        return handleErrorResponse(res, media);
    }

    const output = await scrapeMedia(media);
    if (output instanceof ErrorObject) {
        return handleErrorResponse(res, output);
    }
    const serverUrl = `${req.protocol}://${req.get('host')}`;
    const processedOutput = processApiResponse(output, serverUrl);

    res.status(200).json(processedOutput);
});

app.get('/movie/', (req, res) => {
    handleErrorResponse(
        res,
        new ErrorObject(
            strings.INVALID_MOVIE_ID,
            'user',
            405,
            strings.INVALID_MOVIE_ID_HINT,
            true,
            false
        )
    );
});

app.get('/tv/', (req, res) => {
    handleErrorResponse(
        res,
        new ErrorObject(
            strings.INVALID_TV_ID,
            'user',
            405,
            strings.INVALID_TV_ID_HINT,
            true,
            false
        )
    );
});

// NEW: Search endpoint
app.get('/search', metadataLimiter, addCacheHeaders('metadata'), async (req, res) => {
    const query = req.query.q;
    const page = parseInt(req.query.page) || 1;

    if (!query) {
        return handleErrorResponse(
            res,
            new ErrorObject(
                'Search query is required',
                'user',
                400,
                'Please provide a search query using ?q=your_search_term',
                true,
                false
            )
        );
    }

    const searchResults = await searchContent(query, page);
    if (searchResults instanceof ErrorObject) {
        return handleErrorResponse(res, searchResults);
    }

    res.status(200).json(searchResults);
});

// NEW: Trending endpoint
app.get('/trending', metadataLimiter, addCacheHeaders('metadata'), async (req, res) => {
    const type = req.query.type || 'all'; // 'movie', 'tv', or 'all'
    const timeWindow = req.query.time_window || 'week'; // 'day' or 'week'
    const page = parseInt(req.query.page) || 1;

    // Validate parameters
    if (!['movie', 'tv', 'all'].includes(type)) {
        return handleErrorResponse(
            res,
            new ErrorObject(
                'Invalid type parameter',
                'user',
                400,
                'Type must be "movie", "tv", or "all"',
                true,
                false
            )
        );
    }

    if (!['day', 'week'].includes(timeWindow)) {
        return handleErrorResponse(
            res,
            new ErrorObject(
                'Invalid time_window parameter',
                'user',
                400,
                'Time window must be "day" or "week"',
                true,
                false
            )
        );
    }

    const trendingResults = await getTrendingContent(type, timeWindow, page);
    if (trendingResults instanceof ErrorObject) {
        return handleErrorResponse(res, trendingResults);
    }

    res.status(200).json(trendingResults);
});

// Endpoint to flex how well our cache is doing - because who doesn't love stats
// Hell Yeah we love it, Because STONE COLD SAID SOOOOO
app.get('/cache-stats', noCacheHeaders(), (req, res) => {
    const stats = getCacheStats();
    const workerStats = preCacheWorker.getStats();
    res.status(200).json({
        cache: {
            ...stats,
            cacheEnabled: true,
            ttl: '3 hours (10800 seconds)'
        },
        preCache: workerStats
    });
});

// NEW: Manual cache warm-up endpoint for popular content
app.get('/warm-cache', metadataLimiter, noCacheHeaders(), async (req, res) => {
    try {
        console.log('[WARM-CACHE] Manual cache warm-up requested');
        
        // Trigger immediate pre-cache run
        preCacheWorker.runPreCache();
        
        res.status(200).json({
            message: 'Cache warm-up initiated',
            workerStats: preCacheWorker.getStats()
        });
    } catch (error) {
        res.status(500).json({
            error: 'Cache warm-up failed',
            message: error.message
        });
    }
});

// NEW: Video proxy endpoint
app.get('/proxy/video/:mediaId/:urlHash', async (req, res) => {
    await videoProxy.handleProxyRequest(req, res);
});

// NEW: Analytics dashboard endpoint
app.get('/analytics', metadataLimiter, noCacheHeaders(), async (req, res) => {
    try {
        const dashboard = await userAnalytics.getAnalyticsDashboard();
        res.status(200).json(dashboard);
    } catch (error) {
        res.status(500).json({
            error: 'Analytics unavailable',
            message: error.message
        });
    }
});

// NEW: Enhanced cache stats with Redis info
app.get('/system-stats', metadataLimiter, noCacheHeaders(), async (req, res) => {
    try {
        const cacheStats = getCacheStats();
        const redisStats = await redisCache.getStats();
        const proxyStats = videoProxy.getProxyStats();
        const workerStats = preCacheWorker.getStats();
        
        res.status(200).json({
            cache: {
                memory: cacheStats,
                redis: redisStats
            },
            proxy: proxyStats,
            preCache: workerStats,
            uptime: process.uptime(),
            memory: process.memoryUsage(),
            timestamp: Date.now()
        });
    } catch (error) {
        res.status(500).json({
            error: 'System stats unavailable',
            message: error.message
        });
    }
});

app.get('*', (req, res) => {
    handleErrorResponse(
        res,
        new ErrorObject(
            strings.ROUTE_NOT_FOUND,
            'user',
            404,
            strings.ROUTE_NOT_FOUND_HINT,
            true,
            false
        )
    );
});

app.listen(PORT, () => {
    console.log(`Server is running on port http://localhost:${PORT};`);
    if (process.argv.includes('--debug')) {
        console.log(`Debug mode is enabled.`);
        console.log('Cache is disabled.');
    } else {
        console.log('Debug mode is disabled.');
        console.log('Cache is enabled.');
    }
});
