import cors from 'cors';
import fetch from 'node-fetch';

// Add as needed the orbit proxy and proxy-uira.live I saw in another issue
const PROXY_DOMAINS = [
    'hls1.vid1.site',
    'orbitproxy.cc',
    'hls3.vid1.site',
    'hls2.vid1.site',
    'proxy-m3u8.uira.live'
];

// defaultt user agent i think adding the user agent in the url it self wil mess things up
const DEFAULT_USER_AGENT =
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// We will at first Check if url needs proxying
function needsProxy(url) {
    try {
        const urlObj = new URL(url);
        return PROXY_DOMAINS.some((domain) => urlObj.hostname.includes(domain));
    } catch {
        return false;
    }
}

function extractOriginalUrl(proxyUrl) {
    try {
        const url = new URL(proxyUrl);

        // Generalize: many upstream proxies use either path-based or query param based embedding.
        // 1) Path-based patterns like /proxy/<encodedTarget>
        const pathParts = url.pathname.split('/').filter(Boolean);
        const proxyIndex = pathParts.findIndex((p) => p.toLowerCase() === 'proxy');
        if (proxyIndex !== -1 && pathParts[proxyIndex + 1]) {
            const encoded = pathParts.slice(proxyIndex + 1).join('/');
            try {
                return decodeURIComponent(encoded);
            } catch {
                // fall through if malformed
            }
        }

        // 2) Common query param keys that contain the upstream URL
        for (const key of ['url', 'link', 'target', 'u']) {
            if (url.searchParams.has(key)) {
                try {
                    return decodeURIComponent(url.searchParams.get(key));
                } catch {
                    return url.searchParams.get(key);
                }
            }
        }

        return proxyUrl;
    } catch {
        return proxyUrl;
    }
}

export function createProxyRoutes(app) {
    // M3U8 Proxy endpoint main this is main thing which I am scared of...
    app.get('/m3u8-proxy', cors(), async (req, res) => {
        const targetUrl = req.query.url;
        let headers = {};

        try {
            headers = JSON.parse(req.query.headers || '{}');
        } catch (e) {
            console.log('Invalid headers JSON:', req.query.headers);
        }

        if (!targetUrl) {
            return res.status(400).json({ error: 'URL parameter is required' });
        }

        try {
            console.log(`[M3U8 Proxy] Fetching: ${targetUrl}`);

            const response = await fetch(targetUrl, {
                headers: {
                    'User-Agent': DEFAULT_USER_AGENT,
                    ...headers
                }
            });

            if (!response.ok) {
                return res.status(response.status).json({
                    error: `Failed to fetch M3U8: ${response.status}`
                });
            }

            let m3u8Content = await response.text();

            const lines = m3u8Content.split('\n');
            const newLines = [];

            for (const line of lines) {
                if (line.startsWith('#')) {
                    // encryption keys sooo this is a bit tricky
                    if (line.startsWith('#EXT-X-KEY:')) {
                        const regex = /https?:\/\/[^""\s]+/g;
                        const keyUrl = regex.exec(line)?.[0];
                        if (keyUrl) {
                            const proxyUrl = `/ts-proxy?url=${encodeURIComponent(
                                keyUrl
                            )}&headers=${encodeURIComponent(JSON.stringify(headers))}`;
                            newLines.push(line.replace(keyUrl, proxyUrl));
                        } else {
                            newLines.push(line);
                        }
                    } else if (line.startsWith('#EXT-X-MAP:')) {
                        // initialization segment for fMP4
                        const mapMatch = line.match(/URI="([^"]+)"/);
                        if (mapMatch && mapMatch[1]) {
                            const mapUrl = new URL(mapMatch[1], targetUrl).href;
                            const proxyUrl = `/ts-proxy?url=${encodeURIComponent(
                                mapUrl
                            )}&headers=${encodeURIComponent(JSON.stringify(headers))}`;
                            newLines.push(line.replace(mapMatch[1], proxyUrl));
                        } else {
                            newLines.push(line);
                        }
                    } else if (line.startsWith('#EXT-X-I-FRAME-STREAM-INF:')) {
                        // iframe playlists should go through m3u8-proxy
                        const iframeMatch = line.match(/URI="([^"]+)"/);
                        if (iframeMatch && iframeMatch[1]) {
                            const iframeUrl = new URL(iframeMatch[1], targetUrl).href;
                            const proxyUrl = `/m3u8-proxy?url=${encodeURIComponent(
                                iframeUrl
                            )}&headers=${encodeURIComponent(JSON.stringify(headers))}`;
                            newLines.push(line.replace(iframeMatch[1], proxyUrl));
                        } else {
                            newLines.push(line);
                        }
                    } else {
                        newLines.push(line);
                    }
                } else if (line.trim()) {
                    // Variant playlist vs segment determination
                    const trimmed = line.trim();
                    try {
                        const absoluteUrl = new URL(trimmed, targetUrl).href;
                        const isPlaylist = /\.m3u8(\?|$)/i.test(absoluteUrl);
                        const proxyPath = isPlaylist ? '/m3u8-proxy' : '/ts-proxy';
                        const proxyUrl = `${proxyPath}?url=${encodeURIComponent(
                            absoluteUrl
                        )}&headers=${encodeURIComponent(JSON.stringify(headers))}`;
                        newLines.push(proxyUrl);
                    } catch {
                        newLines.push(trimmed);
                    }
                } else {
                    newLines.push(line); // Keep empty lines
                }
            }

            // We will also need to Set response headers to add proper content support for HLS

            res.setHeader('Content-Type', 'application/vnd.apple.mpegurl');
            res.setHeader('Access-Control-Allow-Origin', '*');
            res.setHeader('Access-Control-Allow-Headers', '*');
            res.setHeader('Access-Control-Allow-Methods', '*');

            res.send(newLines.join('\n'));
        } catch (error) {
            console.error('[M3U8 Proxy Error]:', error.message);
            res.status(500).json({ error: error.message });
        }
    });

    // TS/Segment Proxy endpoint
    app.get('/ts-proxy', cors(), async (req, res) => {
        const targetUrl = req.query.url;
        let headers = {};

        try {
            headers = JSON.parse(req.query.headers || '{}');
        } catch (e) {
            console.log('Invalid headers JSON:', req.query.headers);
        }

        if (!targetUrl) {
            return res.status(400).json({ error: 'URL parameter is required' });
        }

        try {
            console.log(`[TS Proxy] Fetching: ${targetUrl}`);

            const upstreamHeaders = {
                'User-Agent': DEFAULT_USER_AGENT,
                ...headers
            };
            if (req.headers.range) {
                upstreamHeaders.Range = req.headers.range;
            }

            const response = await fetch(targetUrl, {
                headers: upstreamHeaders
            });

            if (!response.ok) {
                return res.status(response.status).json({
                    error: `Failed to fetch segment: ${response.status}`
                });
            }

            // Mirror upstream status and critical headers
            res.status(response.status);
            const upstreamContentType = response.headers.get('content-type') || 'application/octet-stream';
            res.setHeader('Content-Type', upstreamContentType);
            const passHeaders = ['content-length', 'content-range', 'accept-ranges'];
            for (const h of passHeaders) {
                const v = response.headers.get(h);
                if (v) res.setHeader(h, v);
            }
            res.setHeader('Access-Control-Allow-Origin', '*');
            res.setHeader('Access-Control-Allow-Headers', '*');
            res.setHeader('Access-Control-Allow-Methods', '*');

            // Stream the response
            response.body.pipe(res);
        } catch (error) {
            console.error('[TS Proxy Error]:', error.message);
            res.status(500).json({ error: error.message });
        }
    });

    // HLS Proxy endpoint it will get url as query parameter and headers as well
    app.get('/proxy/hls', cors(), async (req, res) => {
        const targetUrl = req.query.link;
        let headers = {};

        try {
            headers = JSON.parse(req.query.headers || '{}');
        } catch (e) {
            console.log(
                'Invalid headers JSON for HLS proxy:',
                req.query.headers
            );
        }

        if (!targetUrl) {
            return res
                .status(400)
                .json({ error: 'Link parameter is required' });
        }

        try {
            console.log(`[HLS Proxy] Fetching: ${targetUrl}`);
            console.log(`[HLS Proxy] Headers: ${JSON.stringify(headers)}`);

            const response = await fetch(targetUrl, {
                headers: {
                    'User-Agent': DEFAULT_USER_AGENT,
                    ...headers
                }
            });

            if (!response.ok) {
                console.log(
                    `[HLS Proxy] Error: ${response.status} for ${targetUrl}`
                );
                return res.status(response.status).json({
                    error: `Failed to fetch HLS: ${response.status}`
                });
            }

            let m3u8Content = await response.text();

            const lines = m3u8Content.split('\n');
            const newLines = [];

            for (const line of lines) {
                if (line.startsWith('#')) {
                    // Handle encryption keys
                    if (line.startsWith('#EXT-X-KEY:')) {
                        const regex = /https?:\/\/[^""\s]+/g;
                        const keyUrl = regex.exec(line)?.[0];
                        if (keyUrl) {
                            const proxyUrl = `/ts-proxy?url=${encodeURIComponent(
                                keyUrl
                            )}&headers=${encodeURIComponent(JSON.stringify(headers))}`;
                            newLines.push(line.replace(keyUrl, proxyUrl));
                        } else {
                            newLines.push(line);
                        }
                    } else {
                        newLines.push(line);
                    }
                } else if (line.trim()) {
                    // Handle segment URLs
                    try {
                        const segmentUrl = new URL(line, targetUrl).href;
                        const proxyUrl = `/ts-proxy?url=${encodeURIComponent(
                            segmentUrl
                        )}&headers=${encodeURIComponent(JSON.stringify(headers))}`;
                        newLines.push(proxyUrl);
                    } catch {
                        newLines.push(line); // Keep original if URL parsing fails
                    }
                } else {
                    newLines.push(line); // Keep empty lines
                }
            }

            res.setHeader('Content-Type', 'application/vnd.apple.mpegurl');
            res.setHeader('Access-Control-Allow-Origin', '*');
            res.setHeader('Access-Control-Allow-Headers', '*');
            res.setHeader('Access-Control-Allow-Methods', '*');

            console.log(
                `[HLS Proxy] Successfully proxied HLS for: ${targetUrl}`
            );
            res.send(newLines.join('\n'));
        } catch (error) {
            console.error('[HLS Proxy Error]:', error.message);
            res.status(500).json({ error: error.message });
        }
    });
}

export function processApiResponse(apiResponse, serverUrl) {
    if (!apiResponse.files) return apiResponse;

    // Extract first http(s) URL from potentially malformed strings
    function extractFirstHttpUrl(value) {
        try {
            if (!value || typeof value !== 'string') return value;
            const match = value.match(/https?:\/\/[^\s"']+/);
            return match ? match[0] : value;
        } catch {
            return value;
        }
    }

    const processedFiles = apiResponse.files.map((file) => {
        if (!file.file || typeof file.file !== 'string') return file;

        // Check if this is an external proxy URL that we want to replace
        if (needsProxy(file.file)) {
            const originalUrl = extractFirstHttpUrl(extractOriginalUrl(file.file));
            try {
                const m3u8Origin = new URL(originalUrl).origin;
                const proxyHeaders = {
                    Referer: m3u8Origin,
                    Origin: m3u8Origin
                };
                const localProxyUrl = `${serverUrl}/proxy/hls?link=${encodeURIComponent(
                    originalUrl
                )}&headers=${encodeURIComponent(JSON.stringify(proxyHeaders))}`;

                console.log(
                    `[HLS Proxy Replacement] ${file.file} -> ${localProxyUrl}`
                );
                console.log(
                    `[HLS Proxy Headers] ${JSON.stringify(proxyHeaders)}`
                );

                return {
                    ...file,
                    file: localProxyUrl,
                    type: 'hls',
                    headers: proxyHeaders
                };
            } catch (e) {
                console.log(`[HLS Proxy Replacement] Skipped invalid URL: ${originalUrl}`);
            }
        }

        // For ANY direct M3U8 URL, prefer routing via our m3u8 proxy so we can send the correct headers
        // and rewrite segments/keys through our ts proxy. This avoids Referer/Origin rejections upstream.
        if (file.file && file.file.includes('.m3u8')) {
            try {
                const cleaned = extractFirstHttpUrl(file.file);
                const m3u8Url = new URL(cleaned);
                const m3u8Origin = m3u8Url.origin;

                const proxyHeaders = {
                    ...(file.headers || {}),
                    Referer: m3u8Origin,
                    Origin: m3u8Origin
                };

                const localProxyUrl = `${serverUrl}/m3u8-proxy?url=${encodeURIComponent(
                    cleaned
                )}&headers=${encodeURIComponent(JSON.stringify(proxyHeaders))}`;

                console.log(`[Direct M3U8] Proxied via /m3u8-proxy: ${file.file}`);
                return {
                    ...file,
                    file: localProxyUrl,
                    type: 'hls',
                    headers: proxyHeaders
                };
            } catch (error) {
                // If URL parsing fails, keep the original file
                console.log(
                    `[Direct M3U8] URL parsing failed for: ${file.file}`
                );
            }
        }

        // For direct MP4 or other media (non-m3u8), proxy through ts-proxy with proper Referer/Origin
        try {
            const cleaned = extractFirstHttpUrl(file.file);
            const urlObj = new URL(cleaned);
            const isM3u8 = /\.m3u8(\?|$)/i.test(cleaned);
            if (!isM3u8 && (cleaned.startsWith('http://') || cleaned.startsWith('https://'))) {
                const origin = urlObj.origin;
                const proxyHeaders = {
                    Referer: origin,
                    Origin: origin
                };
                const localProxyUrl = `${serverUrl}/ts-proxy?url=${encodeURIComponent(
                    cleaned
                )}&headers=${encodeURIComponent(JSON.stringify(proxyHeaders))}`;
                console.log(`[Direct Media] Proxied via /ts-proxy: ${file.file}`);
                return {
                    ...file,
                    file: localProxyUrl,
                    headers: proxyHeaders
                };
            }
        } catch {
            // ignore
        }

        return file; // Return unchanged if no proxy needed
    });

    return {
        ...apiResponse,
        files: processedFiles
    };
}

export { needsProxy, extractOriginalUrl };
