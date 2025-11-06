package com.bank.promotion.bdd;

import com.bank.promotion.adapter.persistence.repository.*;
import com.bank.promotion.application.service.PerformanceMonitoringService;
import com.bank.promotion.application.service.audit.AuditService;
import com.bank.promotion.application.service.audit.AuditTrail;
import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.PromotionResult;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.Collections;

/**
 * BDD 測試配置
 * 提供測試所需的 Bean 定義和 Mock 實作
 */
@TestConfiguration
public class BddTestConfiguration {
    
    @Bean
    @Primary
    public PerformanceMonitoringService performanceMonitoringService() {
        return new PerformanceMonitoringService() {
            @Override
            public void recordOperationTime(String operationType, long executionTimeMs) {
                // Mock implementation for testing
            }
            
            @Override
            public void recordOperationError(String operationType, long executionTimeMs, Exception error) {
                // Mock implementation for testing
            }
        };
    }
    
    @Bean
    @Primary
    public AuditService auditService() {
        return Mockito.mock(AuditService.class);
    }
    
    // Mock Repository implementations using Mockito
    @Bean
    @Primary
    public DecisionTreeRepository decisionTreeRepository() {
        return Mockito.mock(DecisionTreeRepository.class);
    }
    
    @Bean
    @Primary
    public PromotionRuleRepository promotionRuleRepository() {
        return Mockito.mock(PromotionRuleRepository.class);
    }
    
    @Bean
    @Primary
    public PromotionHistoryRepository promotionHistoryRepository() {
        return Mockito.mock(PromotionHistoryRepository.class);
    }
    
    @Bean
    @Primary
    public AuditTrailRepository auditTrailRepository() {
        return Mockito.mock(AuditTrailRepository.class);
    }
    
    @Bean
    @Primary
    public RequestLogRepository requestLogRepository() {
        return Mockito.mock(RequestLogRepository.class);
    }
    
    @Bean
    @Primary
    public DecisionStepRepository decisionStepRepository() {
        return Mockito.mock(DecisionStepRepository.class);
    }
    
    @Bean
    @Primary
    public SystemEventRepository systemEventRepository() {
        return Mockito.mock(SystemEventRepository.class);
    }
    
    @Bean
    @Primary
    public DecisionNodeRepository decisionNodeRepository() {
        return Mockito.mock(DecisionNodeRepository.class);
    }
    
    @Bean
    @Primary
    public com.bank.promotion.domain.command.CommandFactory commandFactory() {
        return new com.bank.promotion.domain.command.CommandFactory();
    }
    
    @Bean
    @Primary
    public com.bank.promotion.domain.command.CommandRegistry commandRegistry() {
        return Mockito.mock(com.bank.promotion.domain.command.CommandRegistry.class);
    }
    
    @Bean
    @Primary
    public com.bank.promotion.command.mock.MockExternalSystemAdapter mockExternalSystemAdapter() {
        return new com.bank.promotion.command.mock.MockExternalSystemAdapter();
    }
    
    @Bean
    @Primary
    public com.bank.promotion.domain.state.PromotionStateManager promotionStateManager() {
        return new com.bank.promotion.domain.state.PromotionStateManager();
    }
    
    @Bean
    @Primary
    public com.bank.promotion.bdd.TestDataManager testDataManager() {
        return new com.bank.promotion.bdd.TestDataManager();
    }
    
    @Bean
    @Primary
    public com.bank.promotion.bdd.mock.MockExternalSystemService mockExternalSystemService() {
        return new com.bank.promotion.bdd.mock.MockExternalSystemService();
    }
}