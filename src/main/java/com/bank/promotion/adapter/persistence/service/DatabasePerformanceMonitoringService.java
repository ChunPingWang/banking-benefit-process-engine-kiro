package com.bank.promotion.adapter.persistence.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for monitoring database and cache performance
 * Provides metrics and health information
 */
@Service
public class DatabasePerformanceMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(DatabasePerformanceMonitoringService.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CacheManager cacheManager;

    /**
     * Get database connection pool metrics
     */
    public Map<String, Object> getDatabaseMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Test database connection
            try (Connection connection = dataSource.getConnection()) {
                metrics.put("database_connection_valid", connection.isValid(5));
                metrics.put("database_connection_timeout", 5);
                
                // Get connection metadata
                metrics.put("database_url", connection.getMetaData().getURL());
                metrics.put("database_product_name", connection.getMetaData().getDatabaseProductName());
                metrics.put("database_product_version", connection.getMetaData().getDatabaseProductVersion());
            }
            
            // HikariCP specific metrics (if available)
            if (dataSource.getClass().getName().contains("HikariDataSource")) {
                try {
                    // Use reflection to get HikariCP metrics
                    Object hikariPoolMXBean = dataSource.getClass().getMethod("getHikariPoolMXBean").invoke(dataSource);
                    if (hikariPoolMXBean != null) {
                        metrics.put("active_connections", 
                                  hikariPoolMXBean.getClass().getMethod("getActiveConnections").invoke(hikariPoolMXBean));
                        metrics.put("idle_connections", 
                                  hikariPoolMXBean.getClass().getMethod("getIdleConnections").invoke(hikariPoolMXBean));
                        metrics.put("total_connections", 
                                  hikariPoolMXBean.getClass().getMethod("getTotalConnections").invoke(hikariPoolMXBean));
                        metrics.put("threads_awaiting_connection", 
                                  hikariPoolMXBean.getClass().getMethod("getThreadsAwaitingConnection").invoke(hikariPoolMXBean));
                    }
                } catch (Exception e) {
                    logger.debug("Could not retrieve HikariCP metrics: {}", e.getMessage());
                }
            }
            
        } catch (SQLException e) {
            logger.error("Failed to get database metrics", e);
            metrics.put("database_connection_valid", false);
            metrics.put("database_error", e.getMessage());
        }
        
        return metrics;
    }

    /**
     * Get cache performance metrics
     */
    public Map<String, Object> getCacheMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Get cache names and basic info
            metrics.put("cache_manager_type", cacheManager.getClass().getSimpleName());
            metrics.put("cache_names", cacheManager.getCacheNames());
            
            // Get cache statistics for each cache
            Map<String, Map<String, Object>> cacheStats = new HashMap<>();
            for (String cacheName : cacheManager.getCacheNames()) {
                Map<String, Object> stats = new HashMap<>();
                
                org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    stats.put("cache_name", cacheName);
                    stats.put("cache_type", cache.getClass().getSimpleName());
                    
                    // Try to get Caffeine cache statistics
                    try {
                        Object nativeCache = cache.getNativeCache();
                        if (nativeCache.getClass().getName().contains("caffeine")) {
                            Object stats_obj = nativeCache.getClass().getMethod("stats").invoke(nativeCache);
                            if (stats_obj != null) {
                                stats.put("hit_count", stats_obj.getClass().getMethod("hitCount").invoke(stats_obj));
                                stats.put("miss_count", stats_obj.getClass().getMethod("missCount").invoke(stats_obj));
                                stats.put("hit_rate", stats_obj.getClass().getMethod("hitRate").invoke(stats_obj));
                                stats.put("eviction_count", stats_obj.getClass().getMethod("evictionCount").invoke(stats_obj));
                            }
                        }
                    } catch (Exception e) {
                        logger.debug("Could not retrieve cache statistics for {}: {}", cacheName, e.getMessage());
                    }
                }
                
                cacheStats.put(cacheName, stats);
            }
            
            metrics.put("cache_statistics", cacheStats);
            
        } catch (Exception e) {
            logger.error("Failed to get cache metrics", e);
            metrics.put("cache_error", e.getMessage());
        }
        
        return metrics;
    }

    /**
     * Get overall performance summary
     */
    public Map<String, Object> getPerformanceSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Database metrics
        Map<String, Object> dbMetrics = getDatabaseMetrics();
        summary.put("database", dbMetrics);
        
        // Cache metrics
        Map<String, Object> cacheMetrics = getCacheMetrics();
        summary.put("cache", cacheMetrics);
        
        // System metrics
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> systemMetrics = new HashMap<>();
        systemMetrics.put("max_memory_mb", runtime.maxMemory() / 1024 / 1024);
        systemMetrics.put("total_memory_mb", runtime.totalMemory() / 1024 / 1024);
        systemMetrics.put("free_memory_mb", runtime.freeMemory() / 1024 / 1024);
        systemMetrics.put("used_memory_mb", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024);
        systemMetrics.put("available_processors", runtime.availableProcessors());
        
        summary.put("system", systemMetrics);
        summary.put("timestamp", System.currentTimeMillis());
        
        return summary;
    }

    /**
     * Clear all caches
     */
    public void clearAllCaches() {
        logger.info("Clearing all caches");
        
        for (String cacheName : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                logger.debug("Cleared cache: {}", cacheName);
            }
        }
    }

    /**
     * Clear specific cache
     */
    public void clearCache(String cacheName) {
        logger.info("Clearing cache: {}", cacheName);
        
        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            logger.debug("Cleared cache: {}", cacheName);
        } else {
            logger.warn("Cache not found: {}", cacheName);
        }
    }
}