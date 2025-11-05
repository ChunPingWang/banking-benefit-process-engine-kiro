package com.bank.promotion.adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration for audit functionality
 * Enables AOP and async processing for audit trail recording
 */
@Configuration
@EnableAspectJAutoProxy
@EnableAsync
public class AuditConfig {

    /**
     * Bean for audit aspect to intercept method calls
     */
    @Bean
    public AuditAspect auditAspect() {
        return new AuditAspect();
    }
}