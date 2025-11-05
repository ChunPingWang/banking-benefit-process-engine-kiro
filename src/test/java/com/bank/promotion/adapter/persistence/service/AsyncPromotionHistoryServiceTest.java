package com.bank.promotion.adapter.persistence.service;

import com.bank.promotion.adapter.persistence.entity.PromotionHistoryEntity;
import com.bank.promotion.adapter.persistence.repository.PromotionHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AsyncPromotionHistoryService
 */
@ExtendWith(MockitoExtension.class)
class AsyncPromotionHistoryServiceTest {

    @Mock
    private PromotionHistoryRepository promotionHistoryRepository;

    @InjectMocks
    private AsyncPromotionHistoryService asyncPromotionHistoryService;

    private PromotionHistoryEntity testPromotionHistory;

    @BeforeEach
    void setUp() {
        testPromotionHistory = new PromotionHistoryEntity(
                "history-1",
                "CUST001",
                "promotion-1",
                "{\"promotionType\":\"VIP\",\"discount\":\"10%\"}"
        );
    }

    @Test
    void shouldSavePromotionHistoryAsync() throws ExecutionException, InterruptedException {
        // Given
        when(promotionHistoryRepository.save(any(PromotionHistoryEntity.class))).thenReturn(testPromotionHistory);

        // When
        CompletableFuture<PromotionHistoryEntity> future = asyncPromotionHistoryService.saveAsync(testPromotionHistory);
        PromotionHistoryEntity result = future.get();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("history-1");
        assertThat(result.getCustomerId()).isEqualTo("CUST001");
        
        verify(promotionHistoryRepository).save(testPromotionHistory);
    }

    @Test
    void shouldHandleExceptionInSaveAsync() {
        // Given
        when(promotionHistoryRepository.save(any(PromotionHistoryEntity.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When
        CompletableFuture<PromotionHistoryEntity> future = asyncPromotionHistoryService.saveAsync(testPromotionHistory);

        // Then
        assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");
        
        verify(promotionHistoryRepository).save(testPromotionHistory);
    }

    @Test
    void shouldRecordPromotionEvaluationAsync() throws ExecutionException, InterruptedException {
        // Given
        when(promotionHistoryRepository.save(any(PromotionHistoryEntity.class))).thenReturn(testPromotionHistory);

        // When
        CompletableFuture<Void> future = asyncPromotionHistoryService.recordPromotionEvaluationAsync(
                "CUST001",
                "promotion-1",
                "{\"promotionType\":\"VIP\",\"discount\":\"10%\"}"
        );
        future.get(); // Wait for completion

        // Then
        verify(promotionHistoryRepository).save(any(PromotionHistoryEntity.class));
    }

    @Test
    void shouldHandleExceptionInRecordPromotionEvaluationAsync() {
        // Given
        when(promotionHistoryRepository.save(any(PromotionHistoryEntity.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When
        CompletableFuture<Void> future = asyncPromotionHistoryService.recordPromotionEvaluationAsync(
                "CUST001",
                "promotion-1",
                "{\"promotionType\":\"VIP\",\"discount\":\"10%\"}"
        );

        // Then
        assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");
        
        verify(promotionHistoryRepository).save(any(PromotionHistoryEntity.class));
    }
}