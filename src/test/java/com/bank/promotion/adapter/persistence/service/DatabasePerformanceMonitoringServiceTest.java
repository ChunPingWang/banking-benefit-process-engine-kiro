package com.bank.promotion.adapter.persistence.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for DatabasePerformanceMonitoringService
 */
@SpringBootTest
@ActiveProfiles("test")
class DatabasePerformanceMonitoringServiceTest {

    @Autowired
    private DatabasePerformanceMonitoringService performanceMonitoringService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldGetDatabaseMetrics() {
        // When
        Map<String, Object> metrics = performanceMonitoringService.getDatabaseMetrics();

        // Then
        assertThat(metrics).isNotEmpty();
        assertThat(metrics).containsKey("database_connection_valid");
        assertThat(metrics.get("database_connection_valid")).isEqualTo(true);
        
        if (metrics.containsKey("database_url")) {
            assertThat(metrics.get("database_url")).asString().contains("h2");
        }
    }

    @Test
    void shouldGetCacheMetrics() {
        // When
        Map<String, Object> metrics = performanceMonitoringService.getCacheMetrics();

        // Then
        assertThat(metrics).isNotEmpty();
        assertThat(metrics).containsKey("cache_manager_type");
        assertThat(metrics).containsKey("cache_names");
        
        assertThat(metrics.get("cache_manager_type")).isNotNull();
    }

    @Test
    void shouldGetPerformanceSummary() {
        // When
        Map<String, Object> summary = performanceMonitoringService.getPerformanceSummary();

        // Then
        assertThat(summary).isNotEmpty();
        assertThat(summary).containsKey("database");
        assertThat(summary).containsKey("cache");
        assertThat(summary).containsKey("system");
        assertThat(summary).containsKey("timestamp");
        
        // Check system metrics
        @SuppressWarnings("unchecked")
        Map<String, Object> systemMetrics = (Map<String, Object>) summary.get("system");
        assertThat(systemMetrics).containsKey("max_memory_mb");
        assertThat(systemMetrics).containsKey("total_memory_mb");
        assertThat(systemMetrics).containsKey("free_memory_mb");
        assertThat(systemMetrics).containsKey("used_memory_mb");
        assertThat(systemMetrics).containsKey("available_processors");
        
        assertThat(systemMetrics.get("available_processors")).asString().matches("\\d+");
    }

    @Test
    void shouldClearAllCaches() {
        // Given - populate cache first
        cacheManager.getCacheNames().forEach(cacheName -> {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put("test-key", "test-value");
            }
        });

        // When
        performanceMonitoringService.clearAllCaches();

        // Then - verify caches are cleared
        cacheManager.getCacheNames().forEach(cacheName -> {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                assertThat(cache.get("test-key")).isNull();
            }
        });
    }

    @Test
    void shouldClearSpecificCache() {
        // Given
        Collection<String> cacheNames = cacheManager.getCacheNames();
        if (cacheNames.isEmpty()) {
            // 如果沒有快取，跳過測試
            return;
        }
        
        String testCacheName = cacheNames.iterator().next();
        org.springframework.cache.Cache cache = cacheManager.getCache(testCacheName);
        
        if (cache != null) {
            cache.put("test-key", "test-value");
            assertThat(cache.get("test-key")).isNotNull();

            // When
            performanceMonitoringService.clearCache(testCacheName);

            // Then
            assertThat(cache.get("test-key")).isNull();
        }
    }

    @Test
    void shouldHandleNonExistentCache() {
        // When & Then - should not throw exception
        performanceMonitoringService.clearCache("non-existent-cache");
    }
}