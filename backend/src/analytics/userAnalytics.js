import { redisCache } from '../cache/redisCache.js';
import { createHash } from 'crypto';

/**
 * User Analytics and Preference Learning System
 * Tracks user behavior to optimize caching and recommendations
 */
class UserAnalytics {
    constructor() {
        this.analytics = {
            totalRequests: 0,
            uniqueUsers: new Set(),
            popularContent: new Map(),
            searchTerms: new Map(),
            deviceTypes: new Map(),
            hourlyStats: new Array(24).fill(0),
            qualityPreferences: new Map(),
            providerPreferences: new Map()
        };

        // Cache keys
        this.POPULAR_CONTENT_KEY = 'analytics:popular_content';
        this.SEARCH_TRENDS_KEY = 'analytics:search_trends';
        this.USER_PREFERENCES_KEY = 'analytics:user_preferences';
        
        // Load existing analytics on startup
        this.loadAnalytics();
        
        // Save analytics periodically
        setInterval(() => this.saveAnalytics(), 5 * 60 * 1000); // Every 5 minutes
    }

    /**
     * Track movie/TV show request
     */
    async trackContentRequest(mediaId, mediaType, title, userId = null, metadata = {}) {
        this.analytics.totalRequests++;
        
        if (userId) {
            this.analytics.uniqueUsers.add(userId);
        }

        const contentKey = `${mediaType}_${mediaId}`;
        const currentCount = this.analytics.popularContent.get(contentKey) || 0;
        this.analytics.popularContent.set(contentKey, currentCount + 1);

        // Track hourly distribution
        const hour = new Date().getHours();
        this.analytics.hourlyStats[hour]++;

        // Track device type if available
        if (metadata.deviceType) {
            const deviceCount = this.analytics.deviceTypes.get(metadata.deviceType) || 0;
            this.analytics.deviceTypes.set(metadata.deviceType, deviceCount + 1);
        }

        // Track user preferences
        if (userId) {
            await this.trackUserPreference(userId, {
                mediaId,
                mediaType,
                title,
                timestamp: Date.now(),
                ...metadata
            });
        }

        console.log(`[ANALYTICS] Tracked: ${mediaType} ${title} (requests: ${currentCount + 1})`);
    }

    /**
     * Track search query
     */
    async trackSearch(query, results = 0, userId = null) {
        const normalizedQuery = query.toLowerCase().trim();
        const currentCount = this.analytics.searchTerms.get(normalizedQuery) || 0;
        this.analytics.searchTerms.set(normalizedQuery, currentCount + 1);

        // Track search success rate
        const searchKey = `search:${normalizedQuery}`;
        const searchData = await redisCache.get(searchKey) || { 
            count: 0, 
            totalResults: 0, 
            lastSearched: 0 
        };
        
        searchData.count++;
        searchData.totalResults += results;
        searchData.lastSearched = Date.now();
        
        await redisCache.set(searchKey, searchData, 86400); // 24 hours

        console.log(`[ANALYTICS] Search tracked: "${query}" (${results} results)`);
    }

    /**
     * Track user preferences and behavior patterns
     */
    async trackUserPreference(userId, preference) {
        const userKey = `${this.USER_PREFERENCES_KEY}:${userId}`;
        const userData = await redisCache.get(userKey) || {
            requests: [],
            preferences: {
                genres: new Map(),
                qualities: new Map(),
                providers: new Map(),
                timeOfDay: new Array(24).fill(0)
            },
            firstSeen: Date.now(),
            lastSeen: Date.now()
        };

        // Add to request history (keep last 100 requests)
        userData.requests.push(preference);
        if (userData.requests.length > 100) {
            userData.requests = userData.requests.slice(-100);
        }

        // Update time-based preferences
        const hour = new Date().getHours();
        userData.preferences.timeOfDay[hour]++;

        // Track quality preferences
        if (preference.quality) {
            const qualityCount = userData.preferences.qualities.get(preference.quality) || 0;
            userData.preferences.qualities.set(preference.quality, qualityCount + 1);
        }

        // Track provider preferences
        if (preference.provider) {
            const providerCount = userData.preferences.providers.get(preference.provider) || 0;
            userData.preferences.providers.set(preference.provider, providerCount + 1);
        }

        userData.lastSeen = Date.now();
        
        // Convert Maps to Objects for Redis storage
        const storageData = {
            ...userData,
            preferences: {
                genres: userData.preferences.genres instanceof Map ? 
                    Object.fromEntries(userData.preferences.genres) : userData.preferences.genres,
                qualities: userData.preferences.qualities instanceof Map ? 
                    Object.fromEntries(userData.preferences.qualities) : userData.preferences.qualities,
                providers: userData.preferences.providers instanceof Map ? 
                    Object.fromEntries(userData.preferences.providers) : userData.preferences.providers,
                timeOfDay: userData.preferences.timeOfDay
            }
        };

        await redisCache.set(userKey, storageData, 30 * 24 * 3600); // 30 days
    }

    /**
     * Get personalized recommendations for pre-caching
     */
    async getPersonalizedRecommendations(userId, limit = 20) {
        const userKey = `${this.USER_PREFERENCES_KEY}:${userId}`;
        const userData = await redisCache.get(userKey);
        
        if (!userData) {
            return this.getPopularContent(limit);
        }

        // Analyze user patterns
        const recommendations = [];
        
        // Get user's preferred time of day
        const currentHour = new Date().getHours();
        const timePreference = userData.preferences.timeOfDay[currentHour];
        
        // Get user's preferred qualities and providers
        const preferredQualities = Object.entries(userData.preferences.qualities)
            .sort(([,a], [,b]) => b - a)
            .slice(0, 3)
            .map(([quality]) => quality);
            
        const preferredProviders = Object.entries(userData.preferences.providers)
            .sort(([,a], [,b]) => b - a)
            .slice(0, 5)
            .map(([provider]) => provider);

        return {
            userId,
            timePreference,
            preferredQualities,
            preferredProviders,
            popularContent: this.getPopularContent(limit),
            recommendation: 'Cache content matching user preferences'
        };
    }

    /**
     * Get trending search terms
     */
    getTrendingSearches(limit = 10) {
        return Array.from(this.analytics.searchTerms.entries())
            .sort(([,a], [,b]) => b - a)
            .slice(0, limit)
            .map(([term, count]) => ({ term, count }));
    }

    /**
     * Get most popular content
     */
    getPopularContent(limit = 20) {
        return Array.from(this.analytics.popularContent.entries())
            .sort(([,a], [,b]) => b - a)
            .slice(0, limit)
            .map(([contentKey, count]) => {
                const [type, id] = contentKey.split('_');
                return { type, id, requests: count };
            });
    }

    /**
     * Get content that should be pre-cached based on trends
     */
    async getPreCacheRecommendations() {
        const popular = this.getPopularContent(30);
        const trending = this.getTrendingSearches(20);
        
        // Get time-based recommendations
        const currentHour = new Date().getHours();
        const hourlyMultiplier = this.analytics.hourlyStats[currentHour] / 
                                Math.max(...this.analytics.hourlyStats);

        const recommendations = popular.map(item => ({
            ...item,
            priority: item.requests * hourlyMultiplier,
            reason: 'popular_content'
        }));

        // Add search-based recommendations
        for (const search of trending) {
            recommendations.push({
                type: 'search',
                query: search.term,
                requests: search.count,
                priority: search.count * 0.8, // Slightly lower priority than direct requests
                reason: 'trending_search'
            });
        }

        return recommendations
            .sort((a, b) => b.priority - a.priority)
            .slice(0, 50);
    }

    /**
     * Get analytics dashboard data
     */
    async getAnalyticsDashboard() {
        const popular = this.getPopularContent(10);
        const trending = this.getTrendingSearches(10);
        const recommendations = await this.getPreCacheRecommendations();

        return {
            overview: {
                totalRequests: this.analytics.totalRequests,
                uniqueUsers: this.analytics.uniqueUsers.size,
                popularContentCount: this.analytics.popularContent.size,
                searchTermsCount: this.analytics.searchTerms.size
            },
            hourlyDistribution: this.analytics.hourlyStats,
            deviceTypes: Object.fromEntries(this.analytics.deviceTypes),
            popularContent: popular,
            trendingSearches: trending,
            preCacheRecommendations: recommendations.slice(0, 20),
            qualityPreferences: Object.fromEntries(this.analytics.qualityPreferences),
            providerPreferences: Object.fromEntries(this.analytics.providerPreferences)
        };
    }

    /**
     * Load analytics from Redis
     */
    async loadAnalytics() {
        try {
            const popularData = await redisCache.get(this.POPULAR_CONTENT_KEY);
            if (popularData) {
                this.analytics.popularContent = new Map(Object.entries(popularData));
            }

            const searchData = await redisCache.get(this.SEARCH_TRENDS_KEY);
            if (searchData) {
                this.analytics.searchTerms = new Map(Object.entries(searchData));
            }

            console.log('[ANALYTICS] Loaded existing analytics data');
        } catch (error) {
            console.log('[ANALYTICS] Error loading analytics:', error.message);
        }
    }

    /**
     * Save analytics to Redis
     */
    async saveAnalytics() {
        try {
            // Save popular content
            const popularData = Object.fromEntries(this.analytics.popularContent);
            await redisCache.set(this.POPULAR_CONTENT_KEY, popularData, 7 * 24 * 3600); // 7 days

            // Save search trends
            const searchData = Object.fromEntries(this.analytics.searchTerms);
            await redisCache.set(this.SEARCH_TRENDS_KEY, searchData, 7 * 24 * 3600); // 7 days

            console.log('[ANALYTICS] Saved analytics data to Redis');
        } catch (error) {
            console.log('[ANALYTICS] Error saving analytics:', error.message);
        }
    }

    /**
     * Get user ID from request (IP-based fallback)
     */
    getUserId(req) {
        // Try to get user ID from various sources
        if (req.headers['x-user-id']) {
            return req.headers['x-user-id'];
        }
        
        if (req.headers['x-device-id']) {
            return `device_${req.headers['x-device-id']}`;
        }

        // Fallback to IP-based identification (hashed for privacy)
        const ip = req.ip || req.connection.remoteAddress;
        const userAgent = req.headers['user-agent'] || '';
        const identifier = `${ip}_${userAgent}`;
        
        return createHash('md5').update(identifier).digest('hex');
    }

    /**
     * Get device type from user agent
     */
    getDeviceType(userAgent) {
        if (!userAgent) return 'unknown';
        
        const ua = userAgent.toLowerCase();
        
        if (ua.includes('mobile') || ua.includes('android')) return 'mobile';
        if (ua.includes('tablet') || ua.includes('ipad')) return 'tablet';
        if (ua.includes('tv') || ua.includes('chromecast')) return 'tv';
        if (ua.includes('desktop') || ua.includes('windows') || ua.includes('macintosh')) return 'desktop';
        
        return 'unknown';
    }
}

export const userAnalytics = new UserAnalytics();
