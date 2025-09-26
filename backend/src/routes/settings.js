import express from 'express';
import { authenticateToken } from './auth.js';
import { ErrorObject } from '../helpers/ErrorObject.js';
import { handleErrorResponse } from '../helpers/helper.js';

const router = express.Router();

// Enhanced comprehensive app settings
const defaultSettings = {
    version: {
        app_version: "1.1.0",
        api_version: "1.0.0",
        min_supported_version: "1.0.0",
        build_number: 12,
        release_date: "2024-01-15T00:00:00Z",
        changelog_url: "https://github.com/onstream/releases"
    },
    player: {
        default_quality: "auto",
        auto_play_next: true,
        auto_play_next_delay: 5, // seconds
        subtitle_language: "en",
        subtitle_size: "medium",
        subtitle_color: "#ffffff",
        subtitle_background: true,
        subtitle_background_opacity: 0.8,
        playback_speed: 1.0,
        skip_intro_enabled: true,
        skip_credits_enabled: false,
        double_tap_seek_duration: 10, // seconds
        gesture_controls_enabled: true,
        volume_gesture_enabled: true,
        brightness_gesture_enabled: true,
        pip_mode_enabled: true,
        background_playback_enabled: false,
        hardware_acceleration: true,
        buffer_size: "medium",
        max_buffer_duration: 30000, // milliseconds
        min_buffer_duration: 2000,
        playback_position_save_interval: 10 // seconds
    },
    general: {
        theme: "dark",
        language: "en",
        auto_update: true,
        auto_update_wifi_only: true,
        analytics_enabled: false,
        crash_reports_enabled: true,
        usage_stats_enabled: true,
        performance_monitoring: true,
        debug_logging: false,
        notifications_enabled: true,
        notification_new_content: true,
        notification_download_complete: true,
        notification_recommendations: false,
        data_saver_mode: false,
        cellular_streaming_warning: true,
        download_wifi_only: true,
        cache_size_limit: 1024, // MB
        auto_cache_cleanup: true,
        keep_downloaded_content: 30 // days
    },
    streaming: {
        preferred_providers: ["vidsrc", "2embed", "autoembed", "superembed"],
        provider_timeout: 10, // seconds
        max_concurrent_streams: 2,
        adaptive_streaming: true,
        prefer_hd_quality: true,
        cellular_quality_limit: "720p",
        wifi_quality_limit: "1080p",
        buffer_size: "medium",
        connection_timeout: 30,
        retry_attempts: 3,
        retry_delay: 2000, // milliseconds
        prefer_ipv6: false,
        use_proxy: false,
        proxy_url: null,
        dns_over_https: false,
        bandwidth_monitoring: true
    },
    parental: {
        enabled: false,
        pin: null,
        max_rating: "R",
        content_warnings: true,
        block_explicit_content: false,
        safe_search: false,
        time_restrictions: {
            enabled: false,
            start_time: "22:00",
            end_time: "06:00"
        },
        allowed_genres: [],
        blocked_keywords: [],
        require_pin_for_rated_content: true
    },
    privacy: {
        watch_history_enabled: true,
        continue_watching_enabled: true,
        recommendations_enabled: true,
        share_usage_data: false,
        personalized_ads: false,
        location_sharing: false,
        social_features_enabled: false
    },
    accessibility: {
        high_contrast_mode: false,
        large_text_mode: false,
        screen_reader_support: false,
        audio_descriptions: false,
        closed_captions_default: false,
        reduce_motion: false,
        focus_indicators: true
    },
    advanced: {
        developer_mode: false,
        beta_features: false,
        experimental_player: false,
        debug_overlay: false,
        network_logging: false,
        performance_overlay: false,
        memory_management: "auto",
        gpu_acceleration: "auto",
        threading_mode: "auto"
    }
};

// Enhanced contact links
const contactLinks = [
    {
        id: 1,
        title: "Telegram Channel",
        url: "https://t.me/onstream_tv",
        icon: "ic_telegram",
        description: "Get updates and support",
        type: "social",
        priority: 1
    },
    {
        id: 2,
        title: "GitHub Repository",
        url: "https://github.com/onstream/android-tv",
        icon: "ic_github", 
        description: "Source code and issues",
        type: "development",
        priority: 2
    },
    {
        id: 3,
        title: "Email Support",
        url: "mailto:support@onstream.tv",
        icon: "ic_email",
        description: "Direct email support",
        type: "support",
        priority: 3
    },
    {
        id: 4,
        title: "Discord Community",
        url: "https://discord.gg/onstream",
        icon: "ic_discord",
        description: "Join our community chat",
        type: "social",
        priority: 4
    },
    {
        id: 5,
        title: "Twitter",
        url: "https://twitter.com/onstream_tv",
        icon: "ic_twitter",
        description: "Follow for news and updates",
        type: "social",
        priority: 5
    },
    {
        id: 6,
        title: "Bug Reports",
        url: "https://github.com/onstream/android-tv/issues",
        icon: "ic_bug_report",
        description: "Report bugs and issues",
        type: "support",
        priority: 6
    },
    {
        id: 7,
        title: "Feature Requests",
        url: "https://github.com/onstream/android-tv/discussions",
        icon: "ic_feature_request",
        description: "Suggest new features",
        type: "feedback",
        priority: 7
    },
    {
        id: 8,
        title: "Privacy Policy",
        url: "https://onstream.tv/privacy",
        icon: "ic_privacy",
        description: "Our privacy policy",
        type: "legal",
        priority: 8
    },
    {
        id: 9,
        title: "Terms of Service",
        url: "https://onstream.tv/terms",
        icon: "ic_terms",
        description: "Terms and conditions",
        type: "legal",
        priority: 9
    },
    {
        id: 10,
        title: "FAQ & Help",
        url: "https://onstream.tv/help",
        icon: "ic_help",
        description: "Frequently asked questions",
        type: "support",
        priority: 10
    }
];

// App configuration
const appConfig = {
    app_name: "OnStream TV",
    app_id: "com.onstream.tv",
    version: defaultSettings.version,
    features: {
        downloads: true,
        offline_mode: true,
        chromecast: true,
        airplay: false,
        pip_mode: true,
        background_playback: false,
        live_tv: false,
        premium_features: false
    },
    limits: {
        max_download_quality: "1080p",
        max_streaming_quality: "4K",
        concurrent_streams: 3,
        download_limit: 100, // GB
        offline_retention: 30 // days
    },
    servers: {
        api_endpoint: "https://api.onstream.tv",
        cdn_endpoint: "https://cdn.onstream.tv",
        websocket_endpoint: "wss://ws.onstream.tv",
        analytics_endpoint: "https://analytics.onstream.tv"
    },
    external_services: {
        tmdb_enabled: true,
        chromecast_app_id: "12345678",
        firebase_project_id: "onstream-tv",
        sentry_dsn: null
    }
};

// User settings storage (in-memory for demo)
const userSettings = new Map();

// Helper functions
function deepMerge(target, source) {
    const output = { ...target };
    for (const key in source) {
        if (source[key] && typeof source[key] === 'object' && !Array.isArray(source[key])) {
            output[key] = deepMerge(target[key] || {}, source[key]);
        } else {
            output[key] = source[key];
        }
    }
    return output;
}

// Helper to get user settings, merging with defaults
function getUserSettings(userId) {
    const userCustomSettings = userSettings.get(userId) || {};
    return deepMerge(defaultSettings, userCustomSettings);
}

// GET /api/settings/all
router.get('/all', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const settings = getUserSettings(userId);

    res.status(200).json({
        success: true,
        settings: settings,
        contact_links: contactLinks
    });
});

// GET /api/settings/version
router.get('/version', (req, res) => {
    res.status(200).json({
        success: true,
        version: defaultSettings.version,
        server_time: new Date().toISOString()
    });
});

// POST /api/settings/update
router.post('/update', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const { category, settings: newSettings } = req.body;

    if (!category || !newSettings) {
        return handleErrorResponse(res, new ErrorObject(
            'Category and settings are required',
            'user', 400,
            'Please provide category and settings to update',
            true, false
        ));
    }

    const validCategories = ['player', 'general', 'streaming', 'parental'];
    if (!validCategories.includes(category)) {
        return handleErrorResponse(res, new ErrorObject(
            'Invalid settings category',
            'user', 400,
            `Category must be one of: ${validCategories.join(', ')}`,
            true, false
        ));
    }

    // Get current user settings or default
    const currentSettings = userSettings.get(userId) || JSON.parse(JSON.stringify(defaultSettings));
    
    // Update specific category
    if (currentSettings[category]) {
        currentSettings[category] = { ...currentSettings[category], ...newSettings };
    } else {
        currentSettings[category] = newSettings;
    }

    // Validate settings
    if (category === 'player') {
        if (newSettings.default_quality && !['auto', 'low', 'medium', 'high', 'ultra'].includes(newSettings.default_quality)) {
            return handleErrorResponse(res, new ErrorObject(
                'Invalid quality setting',
                'user', 400,
                'Quality must be one of: auto, low, medium, high, ultra',
                true, false
            ));
        }
        if (newSettings.subtitle_size && !['small', 'medium', 'large', 'xl'].includes(newSettings.subtitle_size)) {
            return handleErrorResponse(res, new ErrorObject(
                'Invalid subtitle size',
                'user', 400,
                'Subtitle size must be one of: small, medium, large, xl',
                true, false
            ));
        }
    }

    if (category === 'general') {
        if (newSettings.theme && !['dark', 'light'].includes(newSettings.theme)) {
            return handleErrorResponse(res, new ErrorObject(
                'Invalid theme',
                'user', 400,
                'Theme must be either dark or light',
                true, false
            ));
        }
        if (newSettings.language && !/^[a-z]{2}$/.test(newSettings.language)) {
            return handleErrorResponse(res, new ErrorObject(
                'Invalid language code',
                'user', 400,
                'Language must be a 2-letter ISO code (e.g., en, es, fr)',
                true, false
            ));
        }
    }

    userSettings.set(userId, currentSettings);

    res.status(200).json({
        success: true,
        message: 'Settings updated successfully',
        settings: currentSettings
    });
});

// GET /api/settings/player
router.get('/player', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const settings = userSettings.get(userId) || defaultSettings;

    res.status(200).json({
        success: true,
        player_settings: settings.player
    });
});

// POST /api/settings/player
router.post('/player', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const newPlayerSettings = req.body;

    const currentSettings = userSettings.get(userId) || JSON.parse(JSON.stringify(defaultSettings));
    currentSettings.player = { ...currentSettings.player, ...newPlayerSettings };
    
    userSettings.set(userId, currentSettings);

    res.status(200).json({
        success: true,
        message: 'Player settings updated',
        player_settings: currentSettings.player
    });
});

// GET /api/settings/contact
router.get('/contact', (req, res) => {
    res.status(200).json({
        success: true,
        contact_links: contactLinks
    });
});

// POST /api/settings/reset
router.post('/reset', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const { category } = req.body;

    if (category) {
        // Reset specific category
        const currentSettings = userSettings.get(userId) || JSON.parse(JSON.stringify(defaultSettings));
        if (defaultSettings[category]) {
            currentSettings[category] = JSON.parse(JSON.stringify(defaultSettings[category]));
            userSettings.set(userId, currentSettings);
        }
    } else {
        // Reset all settings
        userSettings.delete(userId);
    }

    const resetSettings = userSettings.get(userId) || defaultSettings;

    res.status(200).json({
        success: true,
        message: category ? `${category} settings reset to default` : 'All settings reset to default',
        settings: resetSettings
    });
});

// GET /api/settings/config (Raw config for advanced users)
router.get('/config', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const settings = userSettings.get(userId) || defaultSettings;

    // Raw config with additional technical settings
    const rawConfig = {
        l: "en", // language
        m: "dark", // theme mode  
        p: settings.player.default_quality, // player quality
        v: 12 // version code
    };

    res.status(200).json({
        success: true,
        config: rawConfig,
        settings: settings
    });
});

// POST /api/settings/import
router.post('/import', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const { settings: importedSettings } = req.body;

    if (!importedSettings || typeof importedSettings !== 'object') {
        return handleErrorResponse(res, new ErrorObject(
            'Invalid settings format',
            'user', 400,
            'Settings must be a valid JSON object',
            true, false
        ));
    }

    try {
        // Validate and merge with defaults
        const mergedSettings = JSON.parse(JSON.stringify(defaultSettings));
        
        // Only import known categories
        ['player', 'general', 'streaming', 'parental'].forEach(category => {
            if (importedSettings[category]) {
                mergedSettings[category] = { ...mergedSettings[category], ...importedSettings[category] };
            }
        });

        userSettings.set(userId, mergedSettings);

        res.status(200).json({
            success: true,
            message: 'Settings imported successfully',
            settings: mergedSettings
        });
    } catch (error) {
        return handleErrorResponse(res, new ErrorObject(
            'Failed to import settings',
            'server', 500,
            'Unable to process imported settings',
            true, false
        ));
    }
});

// GET /api/settings/export
router.get('/export', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const settings = userSettings.get(userId) || defaultSettings;

    res.status(200).json({
        success: true,
        export_data: {
            version: "1.0",
            exported_at: new Date().toISOString(),
            user_id: userId,
            settings: settings
        }
    });
});

// GET /api/settings/player
router.get('/player', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const settings = userSettings.get(userId) || defaultSettings;

    res.status(200).json({
        success: true,
        player_settings: settings.player
    });
});

// POST /api/settings/player
router.post('/player', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const playerSettings = req.body;

    // Validate player settings
    const validatedSettings = validatePlayerSettings(playerSettings);
    if (!validatedSettings.valid) {
        return handleErrorResponse(res, new ErrorObject(
            'Invalid player settings',
            'user', 400,
            validatedSettings.error,
            true, false
        ));
    }

    // Get current settings and update player section
    const currentSettings = userSettings.get(userId) || JSON.parse(JSON.stringify(defaultSettings));
    currentSettings.player = { ...currentSettings.player, ...validatedSettings.data };
    userSettings.set(userId, currentSettings);

    res.status(200).json({
        success: true,
        message: 'Player settings updated successfully',
        player_settings: currentSettings.player
    });
});

// GET /api/settings/contact
router.get('/contact', (req, res) => {
    res.status(200).json({
        success: true,
        contact_links: contactLinks,
        support_email: "support@onstream.tv",
        community_links: contactLinks.filter(link => link.type === 'social'),
        legal_links: contactLinks.filter(link => link.type === 'legal')
    });
});

// POST /api/settings/reset
router.post('/reset', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const { category } = req.body;

    if (category) {
        // Reset specific category
        const currentSettings = userSettings.get(userId) || JSON.parse(JSON.stringify(defaultSettings));
        
        if (defaultSettings[category]) {
            currentSettings[category] = JSON.parse(JSON.stringify(defaultSettings[category]));
            userSettings.set(userId, currentSettings);
            
            res.status(200).json({
                success: true,
                message: `${category} settings reset to default`,
                settings: currentSettings
            });
        } else {
            return handleErrorResponse(res, new ErrorObject(
                'Invalid category',
                'user', 400,
                'The specified category does not exist',
                true, false
            ));
        }
    } else {
        // Reset all settings
        userSettings.set(userId, JSON.parse(JSON.stringify(defaultSettings)));
        
        res.status(200).json({
            success: true,
            message: 'All settings reset to default',
            settings: defaultSettings
        });
    }
});

// GET /api/settings/config
router.get('/config', (req, res) => {
    res.status(200).json({
        success: true,
        config: appConfig,
        settings: defaultSettings,
        server_info: {
            version: "1.1.0",
            environment: process.env.NODE_ENV || "development",
            uptime: process.uptime(),
            memory_usage: process.memoryUsage(),
            timestamp: new Date().toISOString()
        }
    });
});

// POST /api/settings/import
router.post('/import', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const { settings: importedSettings, merge = false } = req.body;

    if (!importedSettings) {
        return handleErrorResponse(res, new ErrorObject(
            'Settings data is required',
            'user', 400,
            'Please provide settings data to import',
            true, false
        ));
    }

    try {
        // Validate imported settings structure
        const validatedSettings = validateSettingsStructure(importedSettings);
        if (!validatedSettings.valid) {
            return handleErrorResponse(res, new ErrorObject(
                'Invalid settings format',
                'user', 400,
                validatedSettings.error,
                true, false
            ));
        }

        let finalSettings;
        if (merge) {
            // Merge with existing settings
            const currentSettings = userSettings.get(userId) || JSON.parse(JSON.stringify(defaultSettings));
            finalSettings = deepMerge(currentSettings, validatedSettings.data);
        } else {
            // Replace all settings
            finalSettings = validatedSettings.data;
        }

        userSettings.set(userId, finalSettings);

        res.status(200).json({
            success: true,
            message: 'Settings imported successfully',
            settings: finalSettings,
            imported_at: new Date().toISOString()
        });
    } catch (error) {
        handleErrorResponse(res, new ErrorObject(
            'Import failed',
            'server', 500,
            'Failed to import settings: ' + error.message,
            true, false
        ));
    }
});

// GET /api/settings/export
router.get('/export', authenticateToken, (req, res) => {
    const userId = req.user.id;
    const settings = userSettings.get(userId) || defaultSettings;

    const exportData = {
        version: "1.1.0",
        exported_at: new Date().toISOString(),
        user_id: userId,
        app_version: defaultSettings.version.app_version,
        settings: settings,
        metadata: {
            export_format: "onstream_settings_v1",
            compatibility: ["1.0.0", "1.1.0"],
            checksum: generateSettingsChecksum(settings)
        }
    };

    res.status(200).json({
        success: true,
        export_data: exportData,
        download_filename: `onstream_settings_${userId}_${Date.now()}.json`
    });
});

// GET /api/settings/themes (additional endpoint for theme management)
router.get('/themes', (req, res) => {
    const availableThemes = [
        {
            id: "dark",
            name: "Dark Theme",
            description: "Perfect for low-light viewing",
            preview_url: "/themes/dark_preview.jpg",
            is_default: true
        },
        {
            id: "light",
            name: "Light Theme", 
            description: "Clean and bright interface",
            preview_url: "/themes/light_preview.jpg",
            is_default: false
        },
        {
            id: "oled",
            name: "OLED Black",
            description: "True black for OLED displays",
            preview_url: "/themes/oled_preview.jpg",
            is_default: false
        },
        {
            id: "cinema",
            name: "Cinema Mode",
            description: "Optimized for movie watching",
            preview_url: "/themes/cinema_preview.jpg",
            is_default: false
        }
    ];

    res.status(200).json({
        success: true,
        themes: availableThemes,
        current_theme: "dark"
    });
});

// GET /api/settings/languages (additional endpoint for language management)
router.get('/languages', (req, res) => {
    const supportedLanguages = [
        { code: "en", name: "English", native_name: "English", flag: "🇺🇸" },
        { code: "es", name: "Spanish", native_name: "Español", flag: "🇪🇸" },
        { code: "fr", name: "French", native_name: "Français", flag: "🇫🇷" },
        { code: "de", name: "German", native_name: "Deutsch", flag: "🇩🇪" },
        { code: "it", name: "Italian", native_name: "Italiano", flag: "🇮🇹" },
        { code: "pt", name: "Portuguese", native_name: "Português", flag: "🇵🇹" },
        { code: "ru", name: "Russian", native_name: "Русский", flag: "🇷🇺" },
        { code: "ja", name: "Japanese", native_name: "日本語", flag: "🇯🇵" },
        { code: "ko", name: "Korean", native_name: "한국어", flag: "🇰🇷" },
        { code: "zh", name: "Chinese", native_name: "中文", flag: "🇨🇳" },
        { code: "ar", name: "Arabic", native_name: "العربية", flag: "🇸🇦" },
        { code: "hi", name: "Hindi", native_name: "हिन्दी", flag: "🇮🇳" }
    ];

    res.status(200).json({
        success: true,
        languages: supportedLanguages,
        current_language: "en"
    });
});

// Validation helper functions
function validatePlayerSettings(settings) {
    const errors = [];
    
    if (settings.playback_speed && (settings.playback_speed < 0.25 || settings.playback_speed > 3.0)) {
        errors.push("Playback speed must be between 0.25 and 3.0");
    }
    
    if (settings.subtitle_size && !["small", "medium", "large", "extra_large"].includes(settings.subtitle_size)) {
        errors.push("Invalid subtitle size");
    }
    
    if (settings.default_quality && !["auto", "144p", "240p", "360p", "480p", "720p", "1080p", "4K"].includes(settings.default_quality)) {
        errors.push("Invalid default quality");
    }

    return {
        valid: errors.length === 0,
        error: errors.join(", "),
        data: settings
    };
}

function validateSettingsStructure(settings) {
    const requiredSections = ['version', 'player', 'general', 'streaming', 'parental'];
    const errors = [];
    
    for (const section of requiredSections) {
        if (!settings[section]) {
            errors.push(`Missing required section: ${section}`);
        }
    }
    
    return {
        valid: errors.length === 0,
        error: errors.join(", "),
        data: settings
    };
}

function generateSettingsChecksum(settings) {
    // Simple checksum for settings validation
    return Buffer.from(JSON.stringify(settings)).toString('base64').slice(0, 16);
}

export { router as settingsRoutes };

