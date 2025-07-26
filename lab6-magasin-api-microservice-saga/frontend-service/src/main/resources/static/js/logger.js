/**
 * Frontend Console Logging Utility
 * Provides consistent browser console logging across all frontend pages
 * for debugging microservices interactions.
 */

window.FrontendLogger = {
    /**
     * Log levels for filtering
     */
    levels: {
        DEBUG: 0,
        INFO: 1,
        WARN: 2,
        ERROR: 3
    },

    /**
     * Current log level (set to INFO by default)
     */
    currentLevel: 1,

    /**
     * Set the minimum log level
     */
    setLevel: function(level) {
        this.currentLevel = typeof level === 'string' ? this.levels[level.toUpperCase()] : level;
        this.info(`Log level set to: ${Object.keys(this.levels)[this.currentLevel]}`);
    },

    /**
     * Generic logging function
     */
    log: function(level, category, message, ...args) {
        if (level < this.currentLevel) return;
        
        const timestamp = new Date().toISOString().substr(11, 12);
        const levelName = Object.keys(this.levels)[level];
        const prefix = `[${timestamp}] [${levelName}] [${category}]`;
        
        const consoleMethod = level === 3 ? 'error' : level === 2 ? 'warn' : 'log';
        console[consoleMethod](prefix, message, ...args);
    },

    /**
     * Debug level logging
     */
    debug: function(category, message, ...args) {
        this.log(this.levels.DEBUG, category, message, ...args);
    },

    /**
     * Info level logging
     */
    info: function(category, message, ...args) {
        this.log(this.levels.INFO, category, message, ...args);
    },

    /**
     * Warning level logging
     */
    warn: function(category, message, ...args) {
        this.log(this.levels.WARN, category, message, ...args);
    },

    /**
     * Error level logging
     */
    error: function(category, message, ...args) {
        this.log(this.levels.ERROR, category, message, ...args);
    },

    /**
     * API call logging with request/response details
     */
    api: function(method, url, data, response) {
        const apiData = {
            method: method,
            url: url,
            requestData: data,
            responseStatus: response?.status,
            responseData: response?.data
        };
        
        if (response && response.status >= 400) {
            this.error('API', `${method} ${url} failed`, apiData);
        } else {
            this.info('API', `${method} ${url}`, apiData);
        }
    },

    /**
     * User interaction logging
     */
    interaction: function(action, element, data) {
        this.debug('UI', `User ${action}`, { element, data });
    },

    /**
     * Navigation logging
     */
    navigation: function(from, to) {
        this.info('NAV', `Navigation: ${from} -> ${to}`);
    },

    /**
     * Performance timing
     */
    time: function(label) {
        console.time(`[PERF] ${label}`);
        this.debug('PERF', `Timer started: ${label}`);
    },

    timeEnd: function(label) {
        console.timeEnd(`[PERF] ${label}`);
        this.debug('PERF', `Timer ended: ${label}`);
    },

    /**
     * Track page load events
     */
    pageLoad: function(pageName) {
        this.info('PAGE', `Page loaded: ${pageName}`);
        this.debug('PAGE', 'Page load details', {
            url: window.location.href,
            userAgent: navigator.userAgent,
            timestamp: new Date().toISOString()
        });
    },

    /**
     * Track errors and exceptions
     */
    exception: function(error, context) {
        this.error('EXCEPTION', error.message || 'Unknown error', {
            error: error,
            context: context,
            stack: error.stack,
            url: window.location.href
        });
    },

    /**
     * Feature flags for debugging specific areas
     */
    features: {
        API_CALLS: true,
        USER_INTERACTIONS: true,
        PERFORMANCE: true,
        CART_OPERATIONS: true,
        FORM_VALIDATION: true
    },

    /**
     * Enable/disable specific logging features
     */
    enableFeature: function(feature) {
        this.features[feature] = true;
        this.info('CONFIG', `Enabled logging feature: ${feature}`);
    },

    disableFeature: function(feature) {
        this.features[feature] = false;
        this.info('CONFIG', `Disabled logging feature: ${feature}`);
    },

    /**
     * Conditional logging based on features
     */
    logIf: function(feature, level, category, message, ...args) {
        if (this.features[feature]) {
            this[level](category, message, ...args);
        }
    }
};

// Initialize global error handler
window.addEventListener('error', function(event) {
    window.FrontendLogger.exception(event.error || new Error(event.message), {
        filename: event.filename,
        lineno: event.lineno,
        colno: event.colno
    });
});

// Initialize unhandled promise rejection handler
window.addEventListener('unhandledrejection', function(event) {
    window.FrontendLogger.exception(new Error('Unhandled Promise Rejection: ' + event.reason), {
        promise: event.promise,
        reason: event.reason
    });
});

// Log script initialization
window.FrontendLogger.info('INIT', 'Frontend logging utility initialized');

// Create simple aliases for common use cases
window.Logger = {
    debug: (msg, ...args) => window.FrontendLogger.debug('APP', msg, ...args),
    info: (msg, ...args) => window.FrontendLogger.info('APP', msg, ...args),
    warn: (msg, ...args) => window.FrontendLogger.warn('APP', msg, ...args),
    error: (msg, ...args) => window.FrontendLogger.error('APP', msg, ...args),
    api: (method, url, data, response) => window.FrontendLogger.api(method, url, data, response),
    interaction: (action, element, data) => window.FrontendLogger.logIf('USER_INTERACTIONS', 'debug', 'UI', `User ${action}`, { element, data }),
    performance: (label, action = 'time') => {
        if (window.FrontendLogger.features.PERFORMANCE) {
            if (action === 'time') window.FrontendLogger.time(label);
            else if (action === 'timeEnd') window.FrontendLogger.timeEnd(label);
        }
    }
};
