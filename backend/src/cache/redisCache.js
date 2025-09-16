import Redis from 'ioredis';

/**
 * Advanced Redis cache implementation with fallback to in-memory cache
 * Provides persistence, clustering support, and better performance
 */
class RedisCache {
    constructor() {
        this.isRedisAvailable = false;
        this.memoryCache = new Map(); // Fallback cache
        this.stats = {
            hits: 0,
            misses: 0,
            redisHits: 0,
            memoryHits: 0,
            errors: 0
        };
        
        this.initRedis();
    }

    /**
     * Initialize Redis connection with fallback
     */
    async initRedis() {
        // Skip Redis if explicitly disabled
        if (process.env.DISABLE_REDIS === 'true') {
            console.log('[REDIS] ⚠️ Redis disabled via environment variable');
            return;
        }
        
        try {
            // Try to connect to Redis (default: localhost:6379)
            this.redis = new Redis({
                host: process.env.REDIS_HOST || 'localhost',
                port: process.env.REDIS_PORT || 6379,
                password: process.env.REDIS_PASSWORD || undefined,
                db: process.env.REDIS_DB || 0,
                retryDelayOnFailover: 100,
                maxRetriesPerRequest: 3,
                lazyConnect: true,
                enableReadyCheck: true,
                connectTimeout: 5000,
                commandTimeout: 3000
            });

            // Test connection
            await this.redis.ping();
            this.isRedisAvailable = true;
            console.log('[REDIS] ✅ Connected successfully');
            
            // Set up error handling
            this.redis.on('error', (err) => {
                // Only log if not a connection refused error (which is expected when Redis isn't installed)
                if (!err.message.includes('ECONNREFUSED')) {
                    console.log('[REDIS] ⚠️ Connection error:', err.message);
                }
                this.isRedisAvailable = false;
                this.stats.errors++;
            });

            this.redis.on('connect', () => {
                console.log('[REDIS] 🔄 Reconnected');
                this.isRedisAvailable = true;
            });

        } catch (error) {
            console.log('[REDIS] ❌ Failed to connect, using memory cache:', error.message);
            this.isRedisAvailable = false;
        }
    }

    /**
     * Get value from cache (Redis first, then memory fallback)
     */
    async get(key) {
        try {
            // Try Redis first
            if (this.isRedisAvailable) {
                const value = await this.redis.get(key);
                if (value !== null) {
                    this.stats.hits++;
                    this.stats.redisHits++;
                    return JSON.parse(value);
                }
            }

            // Fallback to memory cache
            if (this.memoryCache.has(key)) {
                const item = this.memoryCache.get(key);
                if (Date.now() < item.expiry) {
                    this.stats.hits++;
                    this.stats.memoryHits++;
                    return item.value;
                } else {
                    this.memoryCache.delete(key);
                }
            }

            this.stats.misses++;
            return undefined;

        } catch (error) {
            console.log(`[CACHE] Error getting ${key}:`, error.message);
            this.stats.errors++;
            
            // Try memory cache as fallback
            if (this.memoryCache.has(key)) {
                const item = this.memoryCache.get(key);
                if (Date.now() < item.expiry) {
                    this.stats.memoryHits++;
                    return item.value;
                }
            }
            return undefined;
        }
    }

    /**
     * Set value in cache (both Redis and memory)
     */
    async set(key, value, ttlSeconds = 10800) { // Default 3 hours
        try {
            const serializedValue = JSON.stringify(value);
            
            // Set in Redis
            if (this.isRedisAvailable) {
                await this.redis.setex(key, ttlSeconds, serializedValue);
            }

            // Also set in memory cache as backup
            const expiry = Date.now() + (ttlSeconds * 1000);
            this.memoryCache.set(key, { value, expiry });

            // Clean up old memory cache entries occasionally
            if (Math.random() < 0.1) { // 10% chance
                this.cleanupMemoryCache();
            }

        } catch (error) {
            console.log(`[CACHE] Error setting ${key}:`, error.message);
            this.stats.errors++;
            
            // Fallback to memory only
            const expiry = Date.now() + (ttlSeconds * 1000);
            this.memoryCache.set(key, { value, expiry });
        }
    }

    /**
     * Delete key from cache
     */
    async delete(key) {
        try {
            if (this.isRedisAvailable) {
                await this.redis.del(key);
            }
            this.memoryCache.delete(key);
        } catch (error) {
            console.log(`[CACHE] Error deleting ${key}:`, error.message);
            this.stats.errors++;
        }
    }

    /**
     * Check if key exists
     */
    async exists(key) {
        try {
            if (this.isRedisAvailable) {
                return await this.redis.exists(key) === 1;
            }
            
            if (this.memoryCache.has(key)) {
                const item = this.memoryCache.get(key);
                return Date.now() < item.expiry;
            }
            return false;
        } catch (error) {
            console.log(`[CACHE] Error checking ${key}:`, error.message);
            return false;
        }
    }

    /**
     * Get cache statistics
     */
    async getStats() {
        let redisInfo = {};
        let redisKeys = 0;
        
        try {
            if (this.isRedisAvailable) {
                const info = await this.redis.info('memory');
                const keyspace = await this.redis.info('keyspace');
                redisKeys = await this.redis.dbsize();
                
                redisInfo = {
                    connected: true,
                    keys: redisKeys,
                    memory: this.parseRedisMemory(info),
                    keyspace: keyspace
                };
            } else {
                redisInfo = { connected: false };
            }
        } catch (error) {
            redisInfo = { connected: false, error: error.message };
        }

        return {
            ...this.stats,
            hitRate: this.stats.hits / (this.stats.hits + this.stats.misses) * 100 || 0,
            memoryCache: {
                size: this.memoryCache.size,
                active: this.getActiveMemoryCacheSize()
            },
            redis: redisInfo
        };
    }

    /**
     * Clean up expired memory cache entries
     */
    cleanupMemoryCache() {
        const now = Date.now();
        let cleaned = 0;
        
        for (const [key, item] of this.memoryCache.entries()) {
            if (now >= item.expiry) {
                this.memoryCache.delete(key);
                cleaned++;
            }
        }
        
        if (cleaned > 0) {
            console.log(`[CACHE] Cleaned up ${cleaned} expired memory cache entries`);
        }
    }

    /**
     * Get active (non-expired) memory cache size
     */
    getActiveMemoryCacheSize() {
        const now = Date.now();
        let activeCount = 0;
        
        for (const [key, item] of this.memoryCache.entries()) {
            if (now < item.expiry) {
                activeCount++;
            }
        }
        
        return activeCount;
    }

    /**
     * Parse Redis memory info
     */
    parseRedisMemory(info) {
        const lines = info.split('\r\n');
        const memory = {};
        
        for (const line of lines) {
            if (line.includes(':')) {
                const [key, value] = line.split(':');
                if (key.includes('memory')) {
                    memory[key] = value;
                }
            }
        }
        
        return memory;
    }

    /**
     * Warm up cache with popular content
     */
    async warmupCache(items) {
        console.log(`[CACHE] Warming up cache with ${items.length} items`);
        
        for (const item of items) {
            try {
                await this.set(item.key, item.value, item.ttl || 10800);
            } catch (error) {
                console.log(`[CACHE] Failed to warm up ${item.key}:`, error.message);
            }
        }
    }

    /**
     * Close Redis connection
     */
    async close() {
        if (this.redis && this.isRedisAvailable) {
            await this.redis.quit();
            console.log('[REDIS] Connection closed');
        }
    }
}

// Create singleton instance
export const redisCache = new RedisCache();

// Graceful shutdown
process.on('SIGINT', async () => {
    await redisCache.close();
    process.exit(0);
});

process.on('SIGTERM', async () => {
    await redisCache.close();
    process.exit(0);
});
