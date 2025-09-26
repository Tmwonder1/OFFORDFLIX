#!/usr/bin/env node

/**
 * OnStream TV Backend API Test Suite
 * Simple test script to verify all endpoints work correctly
 */

import fetch from 'node-fetch';

const BASE_URL = process.env.API_URL || 'http://localhost:3000';
let ACCESS_TOKEN = null;

// Test utilities
function log(message, type = 'info') {
    const colors = {
        info: '\x1b[36m',    // Cyan
        success: '\x1b[32m', // Green  
        error: '\x1b[31m',   // Red
        warning: '\x1b[33m'  // Yellow
    };
    console.log(`${colors[type]}[${type.toUpperCase()}]\x1b[0m ${message}`);
}

async function apiRequest(endpoint, options = {}) {
    const url = `${BASE_URL}${endpoint}`;
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    if (ACCESS_TOKEN && !options.skipAuth) {
        headers.Authorization = `Bearer ${ACCESS_TOKEN}`;
    }

    try {
        const response = await fetch(url, {
            ...options,
            headers
        });
        
        const data = await response.json();
        return { status: response.status, data };
    } catch (error) {
        return { status: 0, error: error.message };
    }
}

// Test functions
async function testHealthCheck() {
    log('Testing health check endpoint...');
    const { status, data } = await apiRequest('/health', { skipAuth: true });
    
    if (status === 200 && data.status === 'healthy') {
        log('✓ Health check passed', 'success');
        return true;
    } else {
        log('✗ Health check failed', 'error');
        return false;
    }
}

async function testAuthentication() {
    log('Testing authentication...');
    
    // Test registration
    const registerData = {
        email: `test${Date.now()}@example.com`,
        name: 'Test User',
        password: 'testpass123'
    };
    
    const { status: regStatus, data: regData } = await apiRequest('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(registerData),
        skipAuth: true
    });
    
    if (regStatus === 201 && regData.success) {
        ACCESS_TOKEN = regData.tokens.access.token;
        log('✓ Registration successful', 'success');
        log('✓ Access token obtained', 'success');
        return true;
    } else {
        log('✗ Registration failed', 'error');
        console.log(regData);
        return false;
    }
}

async function testUserProfile() {
    log('Testing user profile endpoints...');
    
    // Get profile
    const { status, data } = await apiRequest('/api/user/profile');
    
    if (status === 200 && data.success) {
        log('✓ Profile retrieval successful', 'success');
        
        // Update profile
        const updatePayload = { name: 'Updated Test User', avatar: 2 };
        const { status: updateStatus, data: updateResult } = await apiRequest('/api/user/profile', {
            method: 'PUT',
            body: JSON.stringify(updatePayload)
        });
        
        if (updateStatus === 200 && updateResult.success) {
            log('✓ Profile update successful', 'success');
            return true;
        } else {
            log('✗ Profile update failed', 'error');
            return false;
        }
    } else {
        log('✗ Profile retrieval failed', 'error');
        return false;
    }
}

async function testWatchlist() {
    log('Testing watchlist functionality...');
    
    const movieId = 550; // Fight Club
    
    // Add to watchlist
    const { status: addStatus, data: addData } = await apiRequest('/api/user/watchlist', {
        method: 'POST',
        body: JSON.stringify({ movie_id: movieId })
    });
    
    if (addStatus === 201 && addData.success) {
        log('✓ Add to watchlist successful', 'success');
        
        // Get watchlist
        const { status: getStatus, data: getData } = await apiRequest('/api/user/watchlist');
        
        if (getStatus === 200 && getData.success && getData.watchlist.length > 0) {
            log('✓ Get watchlist successful', 'success');
            
            // Remove from watchlist
            const { status: delStatus, data: delData } = await apiRequest(`/api/user/watchlist/${movieId}`, {
                method: 'DELETE'
            });
            
            if (delStatus === 200 && delData.success) {
                log('✓ Remove from watchlist successful', 'success');
                return true;
            } else {
                log('✗ Remove from watchlist failed', 'error');
                return false;
            }
        } else {
            log('✗ Get watchlist failed', 'error');
            return false;
        }
    } else {
        log('✗ Add to watchlist failed', 'error');
        return false;
    }
}

async function testContentDiscovery() {
    log('Testing content discovery...');
    
    // Test home endpoint
    const { status: homeStatus, data: homeData } = await apiRequest('/api/content/home');
    
    if (homeStatus === 200 && homeData.success) {
        log('✓ Home content retrieval successful', 'success');
        
        // Test search
        const { status: searchStatus, data: searchData } = await apiRequest('/api/content/search?q=fight%20club');
        
        if (searchStatus === 200 && searchData.success) {
            log('✓ Search functionality working', 'success');
            
            // Test genres
            const { status: genreStatus, data: genreData } = await apiRequest('/api/content/genres');
            
            if (genreStatus === 200 && genreData.success) {
                log('✓ Genres retrieval successful', 'success');
                return true;
            } else {
                log('✗ Genres retrieval failed', 'error');
                return false;
            }
        } else {
            log('✗ Search failed', 'error');
            return false;
        }
    } else {
        log('✗ Home content retrieval failed', 'error');
        return false;
    }
}

async function testStreaming() {
    log('Testing streaming endpoints...');
    
    // Test movie streaming
    const { status, data } = await apiRequest('/movie/550', { skipAuth: true });
    
    if (status === 200 && data.success) {
        log('✓ Movie streaming sources retrieval successful', 'success');
        
        // Test TV streaming
        const { status: tvStatus, data: tvData } = await apiRequest('/tv/1399?s=1&e=1', { skipAuth: true });
        
        if (tvStatus === 200 && tvData.success) {
            log('✓ TV streaming sources retrieval successful', 'success');
            return true;
        } else {
            log('✗ TV streaming sources failed', 'error');
            return false;
        }
    } else {
        log('✗ Movie streaming sources failed', 'error');
        return false;
    }
}

async function testSettings() {
    log('Testing settings management...');
    
    // Get all settings
    const { status, data } = await apiRequest('/api/settings/all');
    
    if (status === 200 && data.success) {
        log('✓ Settings retrieval successful', 'success');
        
        // Update settings
        const settingsPayload = {
            category: 'player',
            settings: {
                default_quality: 'high',
                auto_play_next: false
            }
        };
        
        const { status: updateStatus, data: updateResult } = await apiRequest('/api/settings/update', {
            method: 'POST',
            body: JSON.stringify(settingsPayload)
        });
        
        if (updateStatus === 200 && updateResult.success) {
            log('✓ Settings update successful', 'success');
            return true;
        } else {
            log('✗ Settings update failed', 'error');
            return false;
        }
    } else {
        log('✗ Settings retrieval failed', 'error');
        return false;
    }
}

// Main test runner
async function runTests() {
    log('🚀 Starting OnStream TV Backend API Tests', 'info');
    log(`Testing against: ${BASE_URL}`, 'info');
    
    const tests = [
        { name: 'Health Check', fn: testHealthCheck },
        { name: 'Authentication', fn: testAuthentication },
        { name: 'User Profile', fn: testUserProfile },
        { name: 'Watchlist', fn: testWatchlist },
        { name: 'Content Discovery', fn: testContentDiscovery },
        { name: 'Streaming', fn: testStreaming },
        { name: 'Settings', fn: testSettings }
    ];
    
    let passed = 0;
    let failed = 0;
    
    for (const test of tests) {
        log(`\n--- Testing ${test.name} ---`, 'info');
        try {
            const result = await test.fn();
            if (result) {
                passed++;
            } else {
                failed++;
            }
        } catch (error) {
            log(`✗ ${test.name} threw error: ${error.message}`, 'error');
            failed++;
        }
    }
    
    log(`\n📊 Test Results:`, 'info');
    log(`✓ Passed: ${passed}`, 'success');
    log(`✗ Failed: ${failed}`, failed > 0 ? 'error' : 'info');
    log(`📈 Success Rate: ${((passed / (passed + failed)) * 100).toFixed(1)}%`, 'info');
    
    if (failed === 0) {
        log('\n🎉 All tests passed! Backend is ready for Android TV app integration.', 'success');
        process.exit(0);
    } else {
        log('\n⚠️  Some tests failed. Please check the backend configuration.', 'warning');
        process.exit(1);
    }
}

// Handle command line arguments
if (process.argv.includes('--help') || process.argv.includes('-h')) {
    console.log(`
OnStream TV Backend API Test Suite

Usage: node test-api.js [options]

Options:
  --help, -h     Show this help message
  --url URL      Set API base URL (default: http://localhost:3000)

Environment Variables:
  API_URL        API base URL

Examples:
  node test-api.js
  node test-api.js --url http://localhost:3000
  API_URL=https://api.onstream.tv node test-api.js
`);
    process.exit(0);
}

// Parse URL argument
const urlIndex = process.argv.indexOf('--url');
if (urlIndex > -1 && process.argv[urlIndex + 1]) {
    BASE_URL = process.argv[urlIndex + 1];
}

// Run tests
runTests().catch(error => {
    log(`Fatal error: ${error.message}`, 'error');
    process.exit(1);
});

