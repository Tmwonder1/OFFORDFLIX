import { getTrendingContent } from '../helpers/tmdb.js';
import { scrapeMedia } from '../api.js';
import { getMovieFromTmdb, getTvFromTmdb } from '../helpers/tmdb.js';
import { getCacheKey, getFromCache, setToCache } from '../cache/cache.js';

/**
 * Background worker to pre-cache popular content for faster user experience
 */
class PreCacheWorker {
    constructor() {
        this.isRunning = false;
        this.lastRun = 0;
        this.runInterval = 30 * 60 * 1000; // Run every 30 minutes
        this.maxConcurrent = 3; // Limit concurrent scraping to avoid overwhelming providers
        this.queue = [];
        this.processing = new Set();
    }

    /**
     * Start the background pre-caching worker
     */
    start() {
        if (this.isRunning) {
            console.log('[PRECACHE] Worker already running');
            return;
        }

        this.isRunning = true;
        console.log('[PRECACHE] Starting background pre-cache worker');
        
        // Run immediately on start
        this.runPreCache();
        
        // Set up recurring runs
        this.intervalId = setInterval(() => {
            this.runPreCache();
        }, this.runInterval);
    }

    /**
     * Stop the background worker
     */
    stop() {
        if (this.intervalId) {
            clearInterval(this.intervalId);
            this.intervalId = null;
        }
        this.isRunning = false;
        console.log('[PRECACHE] Stopped background pre-cache worker');
    }

    /**
     * Main pre-cache execution
     */
    async runPreCache() {
        if (Date.now() - this.lastRun < this.runInterval / 2) {
            console.log('[PRECACHE] Skipping run, too soon since last execution');
            return;
        }

        this.lastRun = Date.now();
        console.log('[PRECACHE] Starting pre-cache run...');

        try {
            // Get trending content to pre-cache
            const popularItems = await this.getPopularItems();
            console.log(`[PRECACHE] Found ${popularItems.length} popular items to cache`);

            // Add to queue for processing
            for (const item of popularItems) {
                if (!this.isAlreadyCached(item) && !this.queue.includes(item.cacheKey)) {
                    this.queue.push(item);
                }
            }

            // Process queue with concurrency limit
            await this.processQueue();

        } catch (error) {
            console.error('[PRECACHE] Error during pre-cache run:', error.message);
        }
    }

    /**
     * Get list of popular items to pre-cache
     */
    async getPopularItems() {
        const items = [];

        try {
            // Get trending movies (current week)
            const trendingMovies = await getTrendingContent('movie', 'week', 1);
            if (trendingMovies && trendingMovies.results) {
                const topMovies = trendingMovies.results.slice(0, 20); // Top 20 trending movies
                for (const movie of topMovies) {
                    const media = await getMovieFromTmdb(movie.id);
                    if (media && !(media instanceof Error)) {
                        items.push({
                            media,
                            cacheKey: getCacheKey(media),
                            priority: movie.popularity || 0
                        });
                    }
                }
            }

            // Get trending TV shows (current week)
            const trendingTv = await getTrendingContent('tv', 'week', 1);
            if (trendingTv && trendingTv.results) {
                const topShows = trendingTv.results.slice(0, 10); // Top 10 trending shows
                for (const show of topShows) {
                    // Pre-cache first episode of trending shows
                    const media = await getTvFromTmdb(show.id, 1, 1);
                    if (media && !(media instanceof Error)) {
                        items.push({
                            media,
                            cacheKey: getCacheKey(media),
                            priority: show.popularity || 0
                        });
                    }
                }
            }

        } catch (error) {
            console.error('[PRECACHE] Error getting popular items:', error.message);
        }

        // Sort by popularity (highest first)
        return items.sort((a, b) => (b.priority || 0) - (a.priority || 0));
    }

    /**
     * Check if item is already cached
     */
    isAlreadyCached(item) {
        const cached = getFromCache(item.cacheKey);
        return cached !== undefined;
    }

    /**
     * Process the pre-cache queue with concurrency control
     */
    async processQueue() {
        const promises = [];

        while (this.queue.length > 0 && this.processing.size < this.maxConcurrent) {
            const item = this.queue.shift();
            if (!item) break;

            const promise = this.preCacheItem(item)
                .finally(() => {
                    this.processing.delete(promise);
                });

            this.processing.add(promise);
            promises.push(promise);

            // If we've reached max concurrent, wait for one to finish
            if (this.processing.size >= this.maxConcurrent) {
                await Promise.race(Array.from(this.processing));
            }
        }

        // Wait for all remaining items to complete
        if (promises.length > 0) {
            await Promise.all(promises);
        }

        console.log(`[PRECACHE] Completed processing queue. ${this.queue.length} items remaining.`);
    }

    /**
     * Pre-cache a single item
     */
    async preCacheItem(item) {
        const startTime = Date.now();
        console.log(`[PRECACHE] Caching ${item.media.type}: ${item.media.title || item.media.name}`);

        try {
            // Scrape and cache the media
            const result = await scrapeMedia(item.media);
            
            if (result && result.files && result.files.length > 0) {
                const duration = Date.now() - startTime;
                console.log(`[PRECACHE] ✅ Cached ${item.cacheKey} (${result.files.length} sources) in ${duration}ms`);
            } else {
                console.log(`[PRECACHE] ⚠️ No sources found for ${item.cacheKey}`);
            }

        } catch (error) {
            const duration = Date.now() - startTime;
            console.log(`[PRECACHE] ❌ Failed to cache ${item.cacheKey} after ${duration}ms:`, error.message);
        }
    }

    /**
     * Get worker statistics
     */
    getStats() {
        return {
            isRunning: this.isRunning,
            lastRun: this.lastRun,
            queueLength: this.queue.length,
            processing: this.processing.size,
            nextRun: this.lastRun + this.runInterval
        };
    }

    /**
     * Manually trigger a cache warm-up for specific content
     */
    async warmCache(contentList) {
        console.log(`[PRECACHE] Manual warm-up for ${contentList.length} items`);
        
        for (const content of contentList) {
            if (!this.queue.find(item => item.cacheKey === content.cacheKey)) {
                this.queue.push(content);
            }
        }

        await this.processQueue();
    }
}

// Create singleton instance
export const preCacheWorker = new PreCacheWorker();

// Auto-start the worker (only in production, not during debug)
if (!process.argv.includes('--debug')) {
    // Start after a short delay to allow server to fully initialize
    setTimeout(() => {
        preCacheWorker.start();
    }, 10000); // 10 seconds delay
}

