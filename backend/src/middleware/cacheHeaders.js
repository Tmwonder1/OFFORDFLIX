import crypto from 'crypto';

/**
 * Middleware to add appropriate cache headers to responses
 */
export function addCacheHeaders(cacheType = 'default') {
    return (req, res, next) => {
        // Store original json method
        const originalJson = res.json;
        
        // Override json method to add headers before sending
        res.json = function(obj) {
            // Generate ETag based on response content
            const etag = generateETag(obj);
            
            // Check if client has the same version (If-None-Match header)
            const clientETag = req.headers['if-none-match'];
            if (clientETag === etag) {
                return res.status(304).end(); // Not Modified
            }
            
            // Set ETag header
            res.set('ETag', etag);
            
            // Set cache headers based on cache type
            switch (cacheType) {
                case 'scraping':
                    // Scraping results - cache for 3 hours (matches NodeCache TTL)
                    res.set('Cache-Control', 'public, max-age=10800, stale-while-revalidate=3600');
                    res.set('Vary', 'Accept-Encoding');
                    break;
                    
                case 'metadata':
                    // Search/trending results - cache for 10 minutes, allow stale for 5 minutes
                    res.set('Cache-Control', 'public, max-age=600, stale-while-revalidate=300');
                    res.set('Vary', 'Accept-Encoding');
                    break;
                    
                case 'static':
                    // Static data - cache for 1 hour
                    res.set('Cache-Control', 'public, max-age=3600, stale-while-revalidate=1800');
                    break;
                    
                default:
                    // Default - cache for 5 minutes
                    res.set('Cache-Control', 'public, max-age=300, stale-while-revalidate=150');
                    res.set('Vary', 'Accept-Encoding');
            }
            
            // Add Last-Modified header
            res.set('Last-Modified', new Date().toUTCString());
            
            // Call original json method
            return originalJson.call(this, obj);
        };
        
        next();
    };
}

/**
 * Generate ETag for response content
 */
function generateETag(data) {
    const content = typeof data === 'string' ? data : JSON.stringify(data);
    return `"${crypto.createHash('md5').update(content).digest('hex')}"`;
}

/**
 * Middleware specifically for API responses that should not be cached
 */
export function noCacheHeaders() {
    return (req, res, next) => {
        res.set('Cache-Control', 'no-cache, no-store, must-revalidate');
        res.set('Pragma', 'no-cache');
        res.set('Expires', '0');
        next();
    };
}

/**
 * Middleware for adding CORS headers with cache consideration
 */
export function corsWithCache() {
    return (req, res, next) => {
        // Add CORS headers if not already added
        if (!res.get('Access-Control-Allow-Origin')) {
            res.set('Access-Control-Allow-Origin', '*');
            res.set('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
            res.set('Access-Control-Allow-Headers', 'Content-Type, Authorization, If-None-Match');
            res.set('Access-Control-Expose-Headers', 'ETag, Cache-Control, Last-Modified');
        }
        
        // Handle preflight requests
        if (req.method === 'OPTIONS') {
            return res.status(200).end();
        }
        
        next();
    };
}

