import rateLimit from 'express-rate-limit';

// Rate limiter for general API endpoints
export const apiLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100, // Limit each IP to 100 requests per windowMs
    message: {
        error: 'Too many requests from this IP, please try again later.',
        hint: 'Rate limit: 100 requests per 15 minutes'
    },
    standardHeaders: true, // Return rate limit info in the `RateLimit-*` headers
    legacyHeaders: false, // Disable the `X-RateLimit-*` headers
});

// Stricter rate limiter for scraping endpoints (movie/tv endpoints)
export const scrapingLimiter = rateLimit({
    windowMs: 10 * 60 * 1000, // 10 minutes
    max: 30, // Limit each IP to 30 requests per windowMs for scraping
    message: {
        error: 'Too many scraping requests from this IP, please try again later.',
        hint: 'Rate limit: 30 scraping requests per 10 minutes'
    },
    standardHeaders: true,
    legacyHeaders: false,
});

// More lenient rate limiter for search/trending endpoints
export const metadataLimiter = rateLimit({
    windowMs: 5 * 60 * 1000, // 5 minutes
    max: 50, // Limit each IP to 50 requests per windowMs for metadata
    message: {
        error: 'Too many search/trending requests from this IP, please try again later.',
        hint: 'Rate limit: 50 metadata requests per 5 minutes'
    },
    standardHeaders: true,
    legacyHeaders: false,
});

// Optimized rate limiter for scraping providers with speed prioritization
class ProviderRateLimiter {
    constructor() {
        this.providers = new Map();
        this.providerDelays = new Map();
        
        // Optimized delays per provider based on speed/reliability
        this.setProviderDelays();
    }

    setProviderDelays() {
        // Fast providers - shorter delays
        this.providerDelays.set('getVidSrc', 500);      // Very reliable, fast
        this.providerDelays.set('getVidSrcCC', 600);    // Fast, good success rate
        this.providerDelays.set('getTwoEmbed', 700);     // Good speed
        
        // Medium providers - standard delays  
        this.providerDelays.set('getAutoembed', 1000);
        this.providerDelays.set('getVidsrcWtf', 1000);
        this.providerDelays.set('getVidZee', 1000);
        
        // Slow providers - longer delays to avoid timeouts
        this.providerDelays.set('getPrimewire', 1500);  // Often slow
        this.providerDelays.set('getVidrock', 1200);
        this.providerDelays.set('getXprime', 1200);
        this.providerDelays.set('getWyzie', 800);        // Subtitles, can be fast
    }

    async waitForProvider(providerName) {
        const now = Date.now();
        const lastRequest = this.providers.get(providerName) || 0;
        const delay = this.providerDelays.get(providerName) || 1000;
        const timeSinceLastRequest = now - lastRequest;
        
        if (timeSinceLastRequest < delay) {
            const waitTime = delay - timeSinceLastRequest;
            if (waitTime > 50) { // Only log significant delays
                console.log(`[RATE] ${providerName}: waiting ${waitTime}ms`);
            }
            await new Promise(resolve => setTimeout(resolve, waitTime));
        }
        
        this.providers.set(providerName, Date.now());
    }

    // Get provider performance stats
    getProviderStats() {
        return Array.from(this.providerDelays.entries()).map(([provider, delay]) => ({
            provider,
            delay,
            lastUsed: this.providers.get(provider) || 0
        }));
    }

    // Dynamically adjust provider delays based on performance
    adjustProviderDelay(providerName, successTime) {
        const currentDelay = this.providerDelays.get(providerName) || 1000;
        
        if (successTime < 2000) {
            // Fast response, can reduce delay slightly
            const newDelay = Math.max(200, currentDelay - 100);
            this.providerDelays.set(providerName, newDelay);
        } else if (successTime > 8000) {
            // Slow response, increase delay
            const newDelay = Math.min(2000, currentDelay + 200);
            this.providerDelays.set(providerName, newDelay);
        }
    }
}

export const providerRateLimiter = new ProviderRateLimiter();
