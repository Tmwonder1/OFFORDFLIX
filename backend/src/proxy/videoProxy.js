import { createHash } from 'crypto';
import { redisCache } from '../cache/redisCache.js';

/**
 * Video proxy system for CDN-style delivery and provider load reduction
 * Caches video URLs, provides load balancing, and handles provider rotation
 */
class VideoProxy {
    constructor() {
        this.proxyStats = {
            requests: 0,
            cacheHits: 0,
            proxyHits: 0,
            errors: 0
        };
        
        // Video URL cache (shorter TTL than metadata)
        this.videoUrlTTL = 1800; // 30 minutes
        this.metadataTTL = 3600;  // 1 hour
        
        // Provider health tracking
        this.providerHealth = new Map();
        this.initProviderHealth();
    }

    /**
     * Initialize provider health tracking
     */
    initProviderHealth() {
        const providers = [
            'tmstr1.shadowlandschronicles.com',
            'tmstr2.shadowlandschronicles.com', 
            'tmstr3.shadowlandschronicles.com',
            'tmstr4.shadowlandschronicles.com',
            'premilkyway.com',
            'streamta.site',
            'queenselti.me',
            'vidrock.net',
            'vdrk.site'
        ];

        providers.forEach(provider => {
            this.providerHealth.set(provider, {
                status: 'healthy',
                lastCheck: Date.now(),
                responseTime: 0,
                errorCount: 0,
                successCount: 0
            });
        });
    }

    /**
     * Process video sources and add proxy URLs
     */
    async processVideoSources(sources, mediaId) {
        const processedSources = [];
        
        for (const source of sources) {
            try {
                const processed = await this.processSource(source, mediaId);
                processedSources.push(processed);
            } catch (error) {
                console.log(`[PROXY] Error processing source:`, error.message);
                // Include original source as fallback
                processedSources.push(source);
            }
        }

        return processedSources;
    }

    /**
     * Process individual video source
     */
    async processSource(source, mediaId) {
        if (!source.file || typeof source.file !== 'string') {
            return source;
        }

        const sourceUrl = source.file;
        const urlHash = this.generateUrlHash(sourceUrl);
        const cacheKey = `video:${mediaId}:${urlHash}`;

        // Check if we have cached metadata for this video URL
        const cachedData = await redisCache.get(cacheKey);
        if (cachedData) {
            this.proxyStats.cacheHits++;
            return {
                ...source,
                file: cachedData.proxyUrl || sourceUrl,
                originalUrl: sourceUrl,
                cached: true,
                quality: cachedData.quality,
                fileSize: cachedData.fileSize
            };
        }

        // Generate proxy URL for this source
        const proxyUrl = this.generateProxyUrl(sourceUrl, mediaId, urlHash);
        
        // Cache the processed source
        const sourceMetadata = {
            originalUrl: sourceUrl,
            proxyUrl: proxyUrl,
            quality: this.detectQuality(sourceUrl),
            provider: this.extractProvider(sourceUrl),
            timestamp: Date.now()
        };

        await redisCache.set(cacheKey, sourceMetadata, this.metadataTTL);

        return {
            ...source,
            file: proxyUrl,
            originalUrl: sourceUrl,
            cached: false,
            quality: sourceMetadata.quality,
            provider: sourceMetadata.provider
        };
    }

    /**
     * Generate proxy URL for video source
     */
    generateProxyUrl(originalUrl, mediaId, urlHash) {
        // Create a secure proxy URL that routes through our server
        const baseUrl = process.env.PROXY_BASE_URL || 'http://localhost:3000';
        const token = this.generateProxyToken(originalUrl, mediaId);
        
        return `${baseUrl}/proxy/video/${mediaId}/${urlHash}?token=${token}`;
    }

    /**
     * Generate secure token for proxy access
     */
    generateProxyToken(url, mediaId) {
        const secret = process.env.PROXY_SECRET || 'default-proxy-secret';
        const timestamp = Math.floor(Date.now() / 60000); // 1-minute resolution
        const data = `${url}:${mediaId}:${timestamp}`;
        
        return createHash('sha256')
            .update(data + secret)
            .digest('hex')
            .substring(0, 16);
    }

    /**
     * Verify proxy token
     */
    verifyProxyToken(token, url, mediaId) {
        const currentTime = Math.floor(Date.now() / 60000);
        
        // Check current and previous minute tokens (for clock skew)
        for (let i = 0; i <= 1; i++) {
            const testTime = currentTime - i;
            const expectedToken = this.generateProxyToken(url, mediaId);
            if (token === expectedToken) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Handle proxy request
     */
    async handleProxyRequest(req, res) {
        this.proxyStats.requests++;
        
        try {
            const { mediaId, urlHash } = req.params;
            const token = req.query.token;
            
            if (!token || !mediaId || !urlHash) {
                return res.status(400).json({ error: 'Missing required parameters' });
            }

            // Get original URL from cache
            const cacheKey = `video:${mediaId}:${urlHash}`;
            const cachedData = await redisCache.get(cacheKey);
            
            if (!cachedData || !cachedData.originalUrl) {
                return res.status(404).json({ error: 'Video source not found' });
            }

            const originalUrl = cachedData.originalUrl;
            
            // Verify token
            if (!this.verifyProxyToken(token, originalUrl, mediaId)) {
                return res.status(403).json({ error: 'Invalid or expired token' });
            }

            // Check provider health
            const provider = this.extractProvider(originalUrl);
            const health = this.providerHealth.get(provider);
            
            if (health && health.status === 'unhealthy') {
                return res.status(503).json({ 
                    error: 'Provider temporarily unavailable',
                    retryAfter: 300 
                });
            }

            // Proxy the request
            await this.proxyVideoRequest(originalUrl, req, res);
            this.proxyStats.proxyHits++;
            
            // Update provider health
            this.updateProviderHealth(provider, true);

        } catch (error) {
            console.log('[PROXY] Error handling request:', error.message);
            this.proxyStats.errors++;
            
            const provider = req.headers.referer ? this.extractProvider(req.headers.referer) : 'unknown';
            this.updateProviderHealth(provider, false);
            
            res.status(500).json({ error: 'Proxy error' });
        }
    }

    /**
     * Proxy video request to original source
     */
    async proxyVideoRequest(originalUrl, req, res) {
        const { default: fetch } = await import('node-fetch');
        
        const startTime = Date.now();
        
        // Prepare headers for the upstream request
        const headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
            'Accept': '*/*',
            'Accept-Language': 'en-US,en;q=0.9',
            'Accept-Encoding': 'gzip, deflate, br',
            'Connection': 'keep-alive',
            'Sec-Fetch-Dest': 'video',
            'Sec-Fetch-Mode': 'cors',
            'Sec-Fetch-Site': 'cross-site'
        };

        // Add range support for video streaming
        if (req.headers.range) {
            headers.Range = req.headers.range;
        }

        // Add referer based on provider
        const provider = this.extractProvider(originalUrl);
        headers.Referer = this.getProviderReferer(provider);

        try {
            const response = await fetch(originalUrl, {
                method: req.method,
                headers: headers,
                timeout: 30000
            });

            // Update response time
            const responseTime = Date.now() - startTime;
            this.updateProviderResponseTime(provider, responseTime);

            // Copy response headers
            const responseHeaders = {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Headers': 'Range',
                'Accept-Ranges': 'bytes'
            };

            ['content-type', 'content-length', 'content-range', 'accept-ranges'].forEach(header => {
                if (response.headers.get(header)) {
                    responseHeaders[header] = response.headers.get(header);
                }
            });

            // Set cache headers for video content
            responseHeaders['Cache-Control'] = 'public, max-age=3600, stale-while-revalidate=7200';
            responseHeaders['CDN-Cache-Control'] = 'public, max-age=86400';
            
            res.writeHead(response.status, responseHeaders);
            response.body.pipe(res);

        } catch (error) {
            console.log(`[PROXY] Error fetching from ${provider}:`, error.message);
            throw error;
        }
    }

    /**
     * Get appropriate referer for provider
     */
    getProviderReferer(provider) {
        const refererMap = {
            'shadowlandschronicles.com': 'https://tmstr1.shadowlandschronicles.com',
            'premilkyway.com': 'https://znomc6azq2dc.premilkyway.com',
            'streamta.site': 'https://streamta.site',
            'queenselti.me': 'https://queenselti.me',
            'vidrock.net': 'https://vidrock.net',
            'vdrk.site': 'https://hls1.vdrk.site'
        };

        for (const [domain, referer] of Object.entries(refererMap)) {
            if (provider.includes(domain)) {
                return referer;
            }
        }

        return `https://${provider}`;
    }

    /**
     * Extract provider domain from URL
     */
    extractProvider(url) {
        try {
            const urlObj = new URL(url);
            return urlObj.hostname;
        } catch {
            return 'unknown';
        }
    }

    /**
     * Detect video quality from URL
     */
    detectQuality(url) {
        const lowerUrl = url.toLowerCase();
        
        if (lowerUrl.includes('4k') || lowerUrl.includes('2160p')) return '4K';
        if (lowerUrl.includes('1440p')) return '1440p';
        if (lowerUrl.includes('1080p') || lowerUrl.includes('fhd')) return '1080p';
        if (lowerUrl.includes('720p') || lowerUrl.includes('hd')) return '720p';
        if (lowerUrl.includes('480p')) return '480p';
        if (lowerUrl.includes('360p')) return '360p';
        
        return 'Unknown';
    }

    /**
     * Generate hash for URL
     */
    generateUrlHash(url) {
        return createHash('md5').update(url).digest('hex').substring(0, 12);
    }

    /**
     * Update provider health status
     */
    updateProviderHealth(provider, success) {
        if (!this.providerHealth.has(provider)) {
            this.initProviderHealth();
        }

        const health = this.providerHealth.get(provider);
        
        if (success) {
            health.successCount++;
            health.errorCount = Math.max(0, health.errorCount - 1);
        } else {
            health.errorCount++;
        }

        // Update status based on error rate
        const totalRequests = health.successCount + health.errorCount;
        const errorRate = health.errorCount / totalRequests;
        
        if (totalRequests >= 10) {
            health.status = errorRate > 0.5 ? 'unhealthy' : 'healthy';
        }

        health.lastCheck = Date.now();
        this.providerHealth.set(provider, health);
    }

    /**
     * Update provider response time
     */
    updateProviderResponseTime(provider, responseTime) {
        const health = this.providerHealth.get(provider);
        if (health) {
            health.responseTime = responseTime;
        }
    }

    /**
     * Get proxy statistics
     */
    getProxyStats() {
        const providerStats = {};
        
        for (const [provider, health] of this.providerHealth.entries()) {
            providerStats[provider] = {
                status: health.status,
                responseTime: health.responseTime,
                errorRate: health.errorCount / (health.successCount + health.errorCount) || 0,
                lastCheck: health.lastCheck
            };
        }

        return {
            ...this.proxyStats,
            hitRate: this.proxyStats.cacheHits / this.proxyStats.requests * 100 || 0,
            providers: providerStats
        };
    }
}

export const videoProxy = new VideoProxy();

