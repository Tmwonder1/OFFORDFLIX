import express from 'express';
import { authenticateToken, users } from './auth.js';
import { ErrorObject } from '../helpers/ErrorObject.js';
import { handleErrorResponse } from '../helpers/helper.js';

const router = express.Router();

// In-memory storage for user data
const userWatchlists = new Map();
const userHistory = new Map();
const userContinueWatching = new Map();
const userRatings = new Map();

// Available avatars
const avatars = [
    { id: 1, url: '/avatars/avatar1.png', name: 'Default' },
    { id: 2, url: '/avatars/avatar2.png', name: 'Cool' },
    { id: 3, url: '/avatars/avatar3.png', name: 'Happy' },
    { id: 4, url: '/avatars/avatar4.png', name: 'Smart' },
    { id: 5, url: '/avatars/avatar5.png', name: 'Fun' }
];

// GET /api/user/profile
router.get('/profile', authenticateToken, (req, res) => {
    const user = req.user;
    
    res.status(200).json({
        success: true,
        user: {
            id: user.id,
            name: user.name,
            email: user.email,
            avatar: user.avatar,
            created_at: user.createdAt,
            email_verified: user.emailVerified || false,
            stats: {
                watchlist_count: (userWatchlists.get(user.id) || []).length,
                history_count: (userHistory.get(user.id) || []).length,
                continue_watching_count: (userContinueWatching.get(user.id) || []).length
            }
        }
    });
});

// PUT /api/user/profile
router.put('/profile', authenticateToken, (req, res) => {
    const user = req.user;
    const { name, avatar } = req.body;

    if (name && name.trim().length < 2) {
        return handleErrorResponse(res, new ErrorObject(
            'Invalid name',
            'user', 400,
            'Name must be at least 2 characters long',
            true, false
        ));
    }

    if (avatar && (!Number.isInteger(avatar) || avatar < 1 || avatar > 5)) {
        return handleErrorResponse(res, new ErrorObject(
            'Invalid avatar',
            'user', 400,
            'Avatar must be a number between 1 and 5',
            true, false
        ));
    }

    // Update user
    if (name) user.name = name.trim();
    if (avatar) user.avatar = avatar;
    
    users.set(user.id, user);

    res.status(200).json({
        success: true,
        message: 'Profile updated successfully',
        user: {
            id: user.id,
            name: user.name,
            email: user.email,
            avatar: user.avatar,
            created_at: user.createdAt,
            email_verified: user.emailVerified
        }
    });
});

// GET /api/user/avatars
router.get('/avatars', authenticateToken, (req, res) => {
    res.status(200).json({
        success: true,
        avatars: avatars
    });
});

// POST /api/user/change-password
router.post('/change-password', authenticateToken, (req, res) => {
    const user = req.user;
    const { current_password, new_password } = req.body;

    if (!current_password || !new_password) {
        return handleErrorResponse(res, new ErrorObject(
            'Current and new passwords are required',
            'user', 400,
            'Please provide both current and new passwords',
            true, false
        ));
    }

    if (user.password && user.password !== current_password) {
        return handleErrorResponse(res, new ErrorObject(
            'Current password is incorrect',
            'user', 401,
            'The current password you entered is incorrect',
            true, false
        ));
    }

    if (new_password.length < 6) {
        return handleErrorResponse(res, new ErrorObject(
            'New password too short',
            'user', 400,
            'New password must be at least 6 characters long',
            true, false
        ));
    }

    // Update password
    user.password = new_password;
    users.set(user.id, user);

    res.status(200).json({
        success: true,
        message: 'Password changed successfully'
    });
});

// GET /api/user/watchlist
router.get('/watchlist', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const watchlist = userWatchlists.get(userId) || [];

    res.status(200).json({
        success: true,
        watchlist: watchlist,
        count: watchlist.length
    });
});

// POST /api/user/watchlist
router.post('/watchlist', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const { movie_id } = req.body;

    if (!movie_id) {
        return handleErrorResponse(res, new ErrorObject(
            'Movie ID is required',
            'user', 400,
            'Please provide a movie ID',
            true, false
        ));
    }

    const watchlist = userWatchlists.get(userId) || [];
    
    // Check if already in watchlist
    if (watchlist.find(item => item.movie_id === movie_id)) {
        return handleErrorResponse(res, new ErrorObject(
            'Already in watchlist',
            'user', 409,
            'This movie is already in your watchlist',
            true, false
        ));
    }

    // Add to watchlist
    watchlist.push({
        movie_id: movie_id,
        added_at: new Date().toISOString()
    });

    userWatchlists.set(userId, watchlist);

    res.status(201).json({
        success: true,
        message: 'Added to watchlist',
        in_watchlist: true
    });
});

// DELETE /api/user/watchlist/:movieId
router.delete('/watchlist/:movieId', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const movieId = parseInt(req.params.movieId);

    const watchlist = userWatchlists.get(userId) || [];
    const filteredWatchlist = watchlist.filter(item => item.movie_id !== movieId);

    if (watchlist.length === filteredWatchlist.length) {
        return handleErrorResponse(res, new ErrorObject(
            'Movie not in watchlist',
            'user', 404,
            'This movie is not in your watchlist',
            true, false
        ));
    }

    userWatchlists.set(userId, filteredWatchlist);

    res.status(200).json({
        success: true,
        message: 'Removed from watchlist',
        in_watchlist: false
    });
});

// GET /api/user/continue-watching
router.get('/continue-watching', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const continueWatching = userContinueWatching.get(userId) || [];

    // Sort by last watched (most recent first)
    const sorted = continueWatching
        .sort((a, b) => new Date(b.last_watched) - new Date(a.last_watched))
        .slice(0, 20); // Limit to 20 items

    res.status(200).json({
        success: true,
        continue_watching: sorted,
        count: sorted.length
    });
});

// POST /api/user/continue-watching
router.post('/continue-watching', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const { 
        movie_id, 
        episode_id, 
        episode_number, 
        season_id, 
        season_number, 
        time, 
        percent 
    } = req.body;

    if (!movie_id || time === undefined || percent === undefined) {
        return handleErrorResponse(res, new ErrorObject(
            'Required fields missing',
            'user', 400,
            'movie_id, time, and percent are required',
            true, false
        ));
    }

    const continueWatching = userContinueWatching.get(userId) || [];
    
    // Find existing entry
    const existingIndex = continueWatching.findIndex(item => 
        item.movie_id === movie_id && 
        item.episode_id === episode_id
    );

    const watchEntry = {
        movie_id,
        episode_id: episode_id || null,
        episode_number: episode_number || null,
        season_id: season_id || null,
        season_number: season_number || null,
        time,
        percent,
        last_watched: new Date().toISOString()
    };

    if (existingIndex >= 0) {
        // Update existing entry
        continueWatching[existingIndex] = watchEntry;
    } else {
        // Add new entry
        continueWatching.push(watchEntry);
    }

    // Remove completed items (>95% watched)
    const filtered = continueWatching.filter(item => item.percent < 95);
    
    userContinueWatching.set(userId, filtered);

    res.status(200).json({
        success: true,
        message: 'Continue watching updated'
    });
});

// DELETE /api/user/continue-watching/:movieId
router.delete('/continue-watching/:movieId', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const movieId = parseInt(req.params.movieId);
    const { episode_id } = req.query;

    const continueWatching = userContinueWatching.get(userId) || [];
    const filtered = continueWatching.filter(item => 
        !(item.movie_id === movieId && 
          (episode_id ? item.episode_id === parseInt(episode_id) : !item.episode_id))
    );

    userContinueWatching.set(userId, filtered);

    res.status(200).json({
        success: true,
        message: 'Removed from continue watching'
    });
});

// GET /api/user/history
router.get('/history', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const { page = 1, limit = 20 } = req.query;
    
    const history = userHistory.get(userId) || [];
    const sorted = history.sort((a, b) => new Date(b.watched_at) - new Date(a.watched_at));
    
    const startIndex = (page - 1) * limit;
    const endIndex = startIndex + parseInt(limit);
    const paginatedHistory = sorted.slice(startIndex, endIndex);

    res.status(200).json({
        success: true,
        history: paginatedHistory,
        pagination: {
            current_page: parseInt(page),
            total_items: history.length,
            total_pages: Math.ceil(history.length / limit),
            items_per_page: parseInt(limit)
        }
    });
});

// POST /api/user/history
router.post('/history', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const { movie_id, episode_id } = req.body;

    if (!movie_id) {
        return handleErrorResponse(res, new ErrorObject(
            'Movie ID is required',
            'user', 400,
            'Please provide a movie ID',
            true, false
        ));
    }

    const history = userHistory.get(userId) || [];
    
    // Add to history (allow duplicates for rewatches)
    history.push({
        movie_id,
        episode_id: episode_id || null,
        watched_at: new Date().toISOString()
    });

    // Keep only last 1000 history items
    if (history.length > 1000) {
        history.splice(0, history.length - 1000);
    }

    userHistory.set(userId, history);

    res.status(201).json({
        success: true,
        message: 'Added to history'
    });
});

// DELETE /api/user/history
router.delete('/history', authenticateToken, (req, res) => {
    const userId = req.user.id;
    
    userHistory.set(userId, []);

    res.status(200).json({
        success: true,
        message: 'History cleared'
    });
});

// POST /api/user/rating
router.post('/rating', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const { movie_id, rating } = req.body;

    if (!movie_id || !rating) {
        return handleErrorResponse(res, new ErrorObject(
            'Movie ID and rating are required',
            'user', 400,
            'Please provide both movie ID and rating',
            true, false
        ));
    }

    if (!Number.isInteger(rating) || rating < 1 || rating > 5) {
        return handleErrorResponse(res, new ErrorObject(
            'Invalid rating',
            'user', 400,
            'Rating must be a number between 1 and 5',
            true, false
        ));
    }

    const ratings = userRatings.get(userId) || new Map();
    ratings.set(movie_id, {
        rating,
        rated_at: new Date().toISOString()
    });
    userRatings.set(userId, ratings);

    res.status(200).json({
        success: true,
        message: 'Rating saved',
        rating: rating
    });
});

// GET /api/user/rating/:movieId
router.get('/rating/:movieId', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const movieId = parseInt(req.params.movieId);
    
    const ratings = userRatings.get(userId) || new Map();
    const userRating = ratings.get(movieId);

    res.status(200).json({
        success: true,
        movie_id: movieId,
        user_rating: userRating ? userRating.rating : null,
        rated_at: userRating ? userRating.rated_at : null
    });
});

export { router as userRoutes };


