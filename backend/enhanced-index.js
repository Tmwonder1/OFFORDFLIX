import express from 'express';
import cors from 'cors';
import { scrapeMedia } from './src/api.js';
import { createProxyRoutes, processApiResponse } from './src/proxy/proxyserver.js';
import { getMovieFromTmdb, getTvFromTmdb, searchMovies } from './src/helpers/tmdb.js';
import { strings } from './src/strings.js';
import { checkIfPossibleTmdbId, handleErrorResponse } from './src/helpers/helper.js';
import { ErrorObject } from './src/helpers/ErrorObject.js';
import { getCacheStats } from './src/cache/cache.js';

// Import new modules we'll create
import { authRoutes } from './src/routes/auth.js';
import { userRoutes } from './src/routes/user.js';
import { contentRoutes } from './src/routes/content.js';
import { settingsRoutes } from './src/routes/settings.js';

const PORT = process.env.PORT || 3000;
const allowedOrigins = ['https://cinepro.mintlify.app/', 'http://localhost:*'];
const app = express();

// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use(cors({
    origin: (origin, callback) => {
        !origin || 
        allowedOrigins.some(allowed => origin.match(allowed.replace('*', '.*'))) ||
        /^http:\/\/localhost/.test(origin)
            ? callback(null, true)
            : callback(new Error('Not allowed by CORS'));
    }
}));

createProxyRoutes(app);

// Basic API info
app.get('/', (req, res) => {
    res.status(200).json({
        name: "OnStream TV Backend API",
        version: "1.1.0",
        description: "Complete backend for OnStream Android TV app",
        endpoints: {
            auth: "/api/auth/*",
            user: "/api/user/*",
            content: "/api/content/*",
            settings: "/api/settings/*",
            streaming: {
                movie: "/movie/:tmdbId",
                tv: "/tv/:tmdbId?s=season&e=episode"
            }
        },
        home: strings.HOME_NAME,
        routes: strings.ROUTES,
        information: strings.INFORMATION,
        license: strings.LICENSE,
        source: strings.SOURCE
    });
});

// Health check endpoint
app.get('/health', (req, res) => {
    res.status(200).json({
        status: 'healthy',
        timestamp: new Date().toISOString(),
        uptime: process.uptime(),
        cache: getCacheStats()
    });
});

// Use route modules
app.use('/api/auth', authRoutes);
app.use('/api/user', userRoutes);
app.use('/api/content', contentRoutes);
app.use('/api/settings', settingsRoutes);

// Original movie/TV streaming endpoints (enhanced)
app.get('/movie/:tmdbId', async (req, res) => {
    if (!checkIfPossibleTmdbId(req.params.tmdbId)) {
        return handleErrorResponse(res, new ErrorObject(
            strings.INVALID_MOVIE_ID,
            'user', 405,
            strings.INVALID_MOVIE_ID_HINT,
            true, false
        ));
    }

    try {
        const media = await getMovieFromTmdb(req.params.tmdbId);
        if (media instanceof ErrorObject) {
            return handleErrorResponse(res, media);
        }

        const output = await scrapeMedia(media);
        if (output instanceof ErrorObject) {
            return handleErrorResponse(res, output);
        }

        const serverUrl = `${req.protocol}://${req.get('host')}`;
        const processedOutput = processApiResponse(output, serverUrl);

        res.status(200).json({
            success: true,
            data: processedOutput,
            movie: media,
            timestamp: new Date().toISOString()
        });
    } catch (error) {
        handleErrorResponse(res, new ErrorObject(
            'Internal server error',
            'server', 500,
            'An unexpected error occurred while processing your request',
            true, false
        ));
    }
});

app.get('/tv/:tmdbId', async (req, res) => {
    const { tmdbId } = req.params;
    const { s: season, e: episode } = req.query;

    if (!checkIfPossibleTmdbId(tmdbId) || !checkIfPossibleTmdbId(season) || !checkIfPossibleTmdbId(episode)) {
        return handleErrorResponse(res, new ErrorObject(
            strings.INVALID_TV_ID,
            'user', 405,
            strings.INVALID_TV_ID_HINT,
            true, false
        ));
    }

    try {
        const media = await getTvFromTmdb(tmdbId, season, episode);
        if (media instanceof ErrorObject) {
            return handleErrorResponse(res, media);
        }

        const output = await scrapeMedia(media);
        if (output instanceof ErrorObject) {
            return handleErrorResponse(res, output);
        }

        const serverUrl = `${req.protocol}://${req.get('host')}`;
        const processedOutput = processApiResponse(output, serverUrl);

        res.status(200).json({
            success: true,
            data: processedOutput,
            show: media,
            season: season,
            episode: episode,
            timestamp: new Date().toISOString()
        });
    } catch (error) {
        handleErrorResponse(res, new ErrorObject(
            'Internal server error',
            'server', 500,
            'An unexpected error occurred while processing your request',
            true, false
        ));
    }
});

// Cache stats endpoint
app.get('/cache-stats', (req, res) => {
    const stats = getCacheStats();
    res.status(200).json({
        ...stats,
        cacheEnabled: true,
        ttl: '3 hours (10800 seconds)',
        timestamp: new Date().toISOString()
    });
});

// Catch-all error handler
app.get('*', (req, res) => {
    handleErrorResponse(res, new ErrorObject(
        strings.ROUTE_NOT_FOUND,
        'user', 404,
        strings.ROUTE_NOT_FOUND_HINT,
        true, false
    ));
});

// Global error handler
app.use((error, req, res, next) => {
    console.error('Global error handler:', error);
    handleErrorResponse(res, new ErrorObject(
        'Internal Server Error',
        'server', 500,
        'An unexpected error occurred',
        true, false
    ));
});

app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 OnStream TV Backend running on http://localhost:${PORT}`);
    console.log(`🌐 Network access: http://192.168.1.17:${PORT}`);
    console.log(`📱 Ready to serve Android TV app`);
    if (process.argv.includes('--debug')) {
        console.log(`🔧 Debug mode enabled - Cache disabled`);
    } else {
        console.log(`⚡ Production mode - Cache enabled`);
    }
    console.log(`📊 Health check: http://localhost:${PORT}/health`);
    console.log(`📖 API docs: http://localhost:${PORT}/`);
});

export default app;


