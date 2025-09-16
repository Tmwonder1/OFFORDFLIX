import { getTwoEmbed } from './controllers/providers/2Embed/2embed.js';
import { getAutoembed } from './controllers/providers/AutoEmbed/autoembed.js';
import { getPrimewire } from './controllers/providers/PrimeWire/primewire.js';
import { getVidSrcCC } from './controllers/providers/VidSrcCC/vidsrccc.js';
import { getVidSrc } from './controllers/providers/VidSrc/VidSrc.js';
import { getVidRock } from './controllers/providers/VidRock/Vidrock.js';
import { getXprime } from './controllers/providers/xprime/xprime.js';
import { ErrorObject } from './helpers/ErrorObject.js';
import { getVidsrcWtf } from './controllers/providers/VidSrcWtf/VidSrcWtf.js';
import { getVidZee } from './controllers/providers/VidZee/VidZee.js';
import { getWyzie } from './controllers/subs/wyzie.js';
import { getCacheKey, getFromCache, setToCache } from './cache/cache.js';
import { providerRateLimiter } from './middleware/rateLimiter.js';

const shouldDebug = process.argv.includes('--debug');

export async function scrapeMedia(media) {
    // First thing - check if we already have this data cached (unless you're debugging and want fresh data)
    const cacheKey = getCacheKey(media);
    console.time(`scrapeMedia_${cacheKey}`);

    if (!shouldDebug) {
        const cachedResult = await getFromCache(cacheKey);

        if (cachedResult) {
            // Found it in cache, then we don't need to scrape again
            if (shouldDebug) {
                console.log(
                    `[CACHE] Cache for ${cacheKey} - serving from memory instead of scraping`
                );
            }
            console.timeEnd(`scrapeMedia_${cacheKey}`);
            return cachedResult;
        }
    }

    // If no cache or bypassed, time to do the actual workkkk
    if (shouldDebug) {
        console.log(
            `${shouldDebug ? 'Cache bypassed' : 'No cache Found'} for ${cacheKey}, work starts now...`
        );
    }
    const providers = [
        { getTwoEmbed: () => getTwoEmbed(media) },
        { getAutoembed: () => getAutoembed(media) },
        { getPrimewire: () => getPrimewire(media) },
        { getVidSrcCC: () => getVidSrcCC(media) },
        { getVidSrc: () => getVidSrc(media) },
        { getVidrock: () => getVidRock(media) },
        { getXprime: () => getXprime(media) },
        { getVidsrcWtf: () => getVidsrcWtf(media) },
        { getVidZee: () => getVidZee(media) },
        { getWyzie: () => getWyzie(media) }
    ];

    // Fast parallel scraping with timeouts and priority providers
    const fastProviders = [
        { getVidSrc: () => getVidSrc(media) },
        { getVidSrcCC: () => getVidSrcCC(media) },
        { getTwoEmbed: () => getTwoEmbed(media) }
    ];
    
    const slowProviders = [
        { getAutoembed: () => getAutoembed(media) },
        { getPrimewire: () => getPrimewire(media) },
        { getVidrock: () => getVidRock(media) },
        { getXprime: () => getXprime(media) },
        { getVidsrcWtf: () => getVidsrcWtf(media) },
        { getVidZee: () => getVidZee(media) },
        { getWyzie: () => getWyzie(media) }
    ];

    // Helper function to scrape with timeout
    const scrapeWithTimeout = async (provider, timeout = 8000) => {
        const providerName = Object.keys(provider)[0];
        
        return Promise.race([
            (async () => {
                try {
                    // Apply rate limiting to prevent provider bans
                    await providerRateLimiter.waitForProvider(providerName);
                    
                    const result = await provider[providerName]();
                    return {
                        data: result,
                        provider: providerName,
                        timing: 'success'
                    };
                } catch (e) {
                    console.log(`Provider ${providerName} failed:`, e.message);
                    return { data: null, provider: providerName, timing: 'error' };
                }
            })(),
            new Promise(resolve => setTimeout(() => {
                console.log(`Provider ${providerName} timed out after ${timeout}ms`);
                resolve({ data: null, provider: providerName, timing: 'timeout' });
            }, timeout))
        ]);
    };

    // Step 1: Try fast providers first (parallel, 5-second timeout)
    console.log(`[SPEED] Starting fast providers for ${cacheKey}`);
    const fastResults = await Promise.all(
        fastProviders.map(provider => scrapeWithTimeout(provider, 5000))
    );
    
    // Check if we got enough sources from fast providers
    const fastSources = fastResults
        .filter(({ data }) => data && !(data instanceof Error || data instanceof ErrorObject))
        .flatMap(({ data }) => Array.isArray(data.files) ? data.files : [data.files])
        .filter(file => file && file.file && typeof file.file === 'string' && file.file.includes('https://'));
    
    console.log(`[SPEED] Fast providers found ${fastSources.length} sources`);
    
    let results = fastResults;
    
    // Step 2: If we need more sources, try slow providers (parallel, 8-second timeout)
    if (fastSources.length < 3) {
        console.log(`[SPEED] Need more sources, trying slow providers...`);
        const slowResults = await Promise.all(
            slowProviders.map(provider => scrapeWithTimeout(provider, 8000))
        );
        results = [...fastResults, ...slowResults];
    } else {
        console.log(`[SPEED] Got enough sources from fast providers, skipping slow ones`);
    }

    const files = results
        .filter(
            ({ data }) =>
                data && !(data instanceof Error || data instanceof ErrorObject)
        )
        .flatMap(({ data }) =>
            Array.isArray(data.files) ? data.files : [data.files]
        )
        .filter(
            (file, index, self) =>
                file &&
                file.file &&
                typeof file.file === 'string' &&
                file.file.includes('https://') &&
                self.findIndex((f) => f.file === file.file) === index
        );

    const subtitles = results
        .filter(
            ({ data }) =>
                data && !(data instanceof Error || data instanceof ErrorObject)
        )
        .flatMap(({ data }) => data.subtitles)
        .filter(
            (sub, index, self) =>
                sub.url && self.findIndex((s) => s.url === sub.url) === index
        );
    // Here comes the big boy to loook for nothing okay here you go
    // We need finalResult coz you can't cache what doesn't exist yet - lowkey just consolidating the return logic
    // Build it once, cache it, return it - way cleaner than scattered returns everywhere

    let finalResult;
    if (shouldDebug) {
        results
            .filter(
                ({ data }) =>
                    data instanceof Error || data instanceof ErrorObject
            )
            .forEach(({ data }) => {
                if (data instanceof ErrorObject) console.error(data.toString());
                else console.error(data);
            });

        let errors = results
            .filter(
                ({ data }) =>
                    data instanceof Error || data instanceof ErrorObject
            )
            .map(({ data }) => data);

        finalResult = { files, subtitles, errors };
    } else {
        finalResult = { files, subtitles };
    }

    // Only cache if we actually found some streams and we're not bypassing cache
    if (files.length > 0 && !shouldDebug) {
        await setToCache(cacheKey, finalResult);
        if (shouldDebug) {
            console.log(
                `Cached result for ${cacheKey}, next request will be much faster`
            );
        }
    } else if (shouldDebug) {
        console.log(
            `Not caching result for ${cacheKey} - cache is bypassed for debugging`
        );
    }

    console.timeEnd(`scrapeMedia_${cacheKey}`);
    return finalResult;
}
