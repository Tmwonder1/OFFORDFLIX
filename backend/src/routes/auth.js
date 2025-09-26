import express from 'express';
import { ErrorObject } from '../helpers/ErrorObject.js';
import { handleErrorResponse } from '../helpers/helper.js';

const router = express.Router();

// In-memory storage for demo (replace with database in production)
const users = new Map();
const sessions = new Map();

// Generate JWT-like token (simplified for demo)
function generateToken(userId) {
    return Buffer.from(`${userId}:${Date.now()}:${Math.random()}`).toString('base64');
}

// Generate refresh token
function generateRefreshToken(userId) {
    return Buffer.from(`refresh:${userId}:${Date.now()}:${Math.random()}`).toString('base64');
}

// Validate email format
function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// POST /api/auth/login
router.post('/login', (req, res) => {
    const { email, password } = req.body;

    if (!email || !password) {
        return handleErrorResponse(res, new ErrorObject(
            'Email and password are required',
            'user', 400,
            'Please provide both email and password',
            true, false
        ));
    }

    if (!isValidEmail(email)) {
        return handleErrorResponse(res, new ErrorObject(
            'Invalid email format',
            'user', 400,
            'Please provide a valid email address',
            true, false
        ));
    }

    // Find user
    const user = Array.from(users.values()).find(u => u.email === email);
    
    if (!user || user.password !== password) {
        return handleErrorResponse(res, new ErrorObject(
            'Invalid credentials',
            'user', 401,
            'Email or password is incorrect',
            true, false
        ));
    }

    // Generate tokens
    const accessToken = generateToken(user.id);
    const refreshToken = generateRefreshToken(user.id);

    // Store session
    sessions.set(accessToken, {
        userId: user.id,
        createdAt: new Date(),
        expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000) // 24 hours
    });

    res.status(200).json({
        success: true,
        user: {
            id: user.id,
            name: user.name,
            email: user.email,
            avatar: user.avatar || 1,
            created_at: user.createdAt,
            email_verified: user.emailVerified || false
        },
        tokens: {
            access: {
                token: accessToken,
                expires: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
            },
            refresh: {
                token: refreshToken,
                expires: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString()
            }
        }
    });
});

// POST /api/auth/register
router.post('/register', (req, res) => {
    const { email, name, password } = req.body;

    if (!email || !name || !password) {
        return handleErrorResponse(res, new ErrorObject(
            'All fields are required',
            'user', 400,
            'Please provide email, name, and password',
            true, false
        ));
    }

    if (!isValidEmail(email)) {
        return handleErrorResponse(res, new ErrorObject(
            'Invalid email format',
            'user', 400,
            'Please provide a valid email address',
            true, false
        ));
    }

    if (password.length < 6) {
        return handleErrorResponse(res, new ErrorObject(
            'Password too short',
            'user', 400,
            'Password must be at least 6 characters long',
            true, false
        ));
    }

    // Check if user already exists
    const existingUser = Array.from(users.values()).find(u => u.email === email);
    if (existingUser) {
        return handleErrorResponse(res, new ErrorObject(
            'Email already registered',
            'user', 409,
            'An account with this email already exists',
            true, false
        ));
    }

    // Create new user
    const userId = Date.now().toString();
    const user = {
        id: userId,
        email,
        name,
        password, // In production, hash this!
        avatar: 1,
        createdAt: new Date().toISOString(),
        emailVerified: false
    };

    users.set(userId, user);

    // Generate tokens
    const accessToken = generateToken(userId);
    const refreshToken = generateRefreshToken(userId);

    // Store session
    sessions.set(accessToken, {
        userId: userId,
        createdAt: new Date(),
        expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000)
    });

    res.status(201).json({
        success: true,
        message: 'Account created successfully',
        user: {
            id: user.id,
            name: user.name,
            email: user.email,
            avatar: user.avatar,
            created_at: user.createdAt,
            email_verified: user.emailVerified
        },
        tokens: {
            access: {
                token: accessToken,
                expires: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
            },
            refresh: {
                token: refreshToken,
                expires: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString()
            }
        }
    });
});

// POST /api/auth/login-code (for TV code login)
router.post('/login-code', (req, res) => {
    const { code } = req.body;

    if (!code) {
        return handleErrorResponse(res, new ErrorObject(
            'Login code is required',
            'user', 400,
            'Please provide a login code',
            true, false
        ));
    }

    // For demo, accept any 6-digit code
    if (!/^\d{6}$/.test(code)) {
        return handleErrorResponse(res, new ErrorObject(
            'Invalid code format',
            'user', 400,
            'Login code must be 6 digits',
            true, false
        ));
    }

    // Create a demo user for code login
    const userId = `code_${Date.now()}`;
    const user = {
        id: userId,
        email: `user${code}@demo.com`,
        name: `TV User ${code}`,
        password: null,
        avatar: Math.floor(Math.random() * 5) + 1,
        createdAt: new Date().toISOString(),
        emailVerified: true
    };

    users.set(userId, user);

    const accessToken = generateToken(userId);
    const refreshToken = generateRefreshToken(userId);

    sessions.set(accessToken, {
        userId: userId,
        createdAt: new Date(),
        expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000)
    });

    res.status(200).json({
        success: true,
        user: {
            id: user.id,
            name: user.name,
            email: user.email,
            avatar: user.avatar,
            created_at: user.createdAt,
            email_verified: user.emailVerified
        },
        tokens: {
            access: {
                token: accessToken,
                expires: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
            },
            refresh: {
                token: refreshToken,
                expires: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString()
            }
        }
    });
});

// POST /api/auth/forgot-password
router.post('/forgot-password', (req, res) => {
    const { email } = req.body;

    if (!email || !isValidEmail(email)) {
        return handleErrorResponse(res, new ErrorObject(
            'Valid email is required',
            'user', 400,
            'Please provide a valid email address',
            true, false
        ));
    }

    // In a real app, you'd send an email here
    res.status(200).json({
        success: true,
        message: 'Password reset instructions sent to your email',
        email: email
    });
});

// POST /api/auth/refresh-token
router.post('/refresh-token', (req, res) => {
    const { refresh_token } = req.body;

    if (!refresh_token) {
        return handleErrorResponse(res, new ErrorObject(
            'Refresh token is required',
            'user', 400,
            'Please provide a refresh token',
            true, false
        ));
    }

    // In a real app, you'd validate the refresh token properly
    try {
        const decoded = Buffer.from(refresh_token, 'base64').toString();
        const [type, userId] = decoded.split(':');
        
        if (type !== 'refresh' || !users.has(userId)) {
            throw new Error('Invalid token');
        }

        const newAccessToken = generateToken(userId);
        const newRefreshToken = generateRefreshToken(userId);

        sessions.set(newAccessToken, {
            userId: userId,
            createdAt: new Date(),
            expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000)
        });

        res.status(200).json({
            success: true,
            tokens: {
                access: {
                    token: newAccessToken,
                    expires: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
                },
                refresh: {
                    token: newRefreshToken,
                    expires: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString()
                }
            }
        });
    } catch (error) {
        return handleErrorResponse(res, new ErrorObject(
            'Invalid refresh token',
            'user', 401,
            'The provided refresh token is invalid or expired',
            true, false
        ));
    }
});

// POST /api/auth/logout
router.post('/logout', (req, res) => {
    const authHeader = req.headers.authorization;
    const token = authHeader && authHeader.split(' ')[1];

    if (token && sessions.has(token)) {
        sessions.delete(token);
    }

    res.status(200).json({
        success: true,
        message: 'Logged out successfully'
    });
});

// Middleware to authenticate requests
export function authenticateToken(req, res, next) {
    const authHeader = req.headers.authorization;
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        return handleErrorResponse(res, new ErrorObject(
            'Access token required',
            'user', 401,
            'Please provide an access token in Authorization header',
            true, false
        ));
    }

    const session = sessions.get(token);
    if (!session || session.expiresAt < new Date()) {
        if (session) sessions.delete(token);
        return handleErrorResponse(res, new ErrorObject(
            'Invalid or expired token',
            'user', 401,
            'Your session has expired, please login again',
            true, false
        ));
    }

    const user = users.get(session.userId);
    if (!user) {
        sessions.delete(token);
        return handleErrorResponse(res, new ErrorObject(
            'User not found',
            'user', 401,
            'Associated user account not found',
            true, false
        ));
    }

    req.user = user;
    req.session = session;
    next();
}

export { router as authRoutes, users, sessions };


