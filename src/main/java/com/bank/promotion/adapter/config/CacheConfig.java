package com.bank.promotion.adapter.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration for the promotion system
 * Uses Caffeine cache for high performance caching
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache manager for development environment (simple cache)
     */
    @Bean
    @Profile("dev")
    public CacheManager devCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats());
        
        // Define cache names
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "decisionTrees",
                "promotionRules",
                "nodeConfigurations",
                "customerProfiles"
        ));
        
        return cacheManager;
    }

    /**
     * Cache manager for production environments (optimized cache)
     */
    @Bean
    @Profile({"sit", "uat", "prod"})
    public CacheManager prodCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .refreshAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());
        
        // Define cache names
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "decisionTrees",
                "promotionRules",
                "nodeConfigurations",
                "customerProfiles",
                "promotionHistory",
                "auditTrails"
        ));
        
        return cacheManager;
    }
}