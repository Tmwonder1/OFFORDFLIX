import NodeCache from 'node-cache';
import { redisCache } from './redisCache.js';

// Here comes the big boy to loook for nothing okay here you go if you do something you better do it right
// Setting up cache for 3 hours because lowkey's attention span is short
const cache = new NodeCache({ stdTTL: 10800, checkperiod: 600 });

export function getCacheKey(media) {
    // TV shows need season and episode info, movies just need the basic ID
    if (media.type === 'tv') {
        return `${media.type}_${media.tmdb}_${media.season}_${media.episode}`;
    }
    return `${media.type}_${media.tmdb}`;
}

export async function getFromCache(key) {
    // Try Redis first, then fallback to in-memory cache
    try {
        const redisValue = await redisCache.get(key);
        if (redisValue !== undefined) {
            return redisValue;
        }
    } catch (error) {
        console.log('[CACHE] Redis get error, using memory cache:', error.message);
    }
    
    // Simple wrapper to grab stuff from cache
    return cache.get(key);
}

export async function setToCache(key, data, ttl = 10800) {
    // Set in both Redis and memory cache for redundancy
    try {
        await redisCache.set(key, data, ttl);
    } catch (error) {
        console.log('[CACHE] Redis set error:', error.message);
    }
    
    // Store the scraped data so we don't have to fetch it again
    return cache.set(key, data, ttl);
}

export function getCacheStats() {
    // Useful for debugging and seeing how well our cache is performing
    return cache.getStats();
}
