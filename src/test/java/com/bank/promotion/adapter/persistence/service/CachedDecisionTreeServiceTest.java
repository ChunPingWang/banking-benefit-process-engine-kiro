package com.bank.promotion.adapter.persistence.service;

import com.bank.promotion.adapter.persistence.entity.DecisionTreeEntity;
import com.bank.promotion.adapter.persistence.repository.DecisionTreeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CachedDecisionTreeService
 */
@ExtendWith(MockitoExtension.class)
class CachedDecisionTreeServiceTest {

    @Mock
    private DecisionTreeRepository decisionTreeRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private CachedDecisionTreeService cachedDecisionTreeService;

    private DecisionTreeEntity testDecisionTree;

    @BeforeEach
    void setUp() {
        testDecisionTree = new DecisionTreeEntity("test-tree-1", "Test Decision Tree", "ACTIVE");
        testDecisionTree.setRootNodeId("root-node-1");
    }

    @Test
    void shouldFindDecisionTreeById() {
        // Given
        when(decisionTreeRepository.findById("test-tree-1")).thenReturn(Optional.of(testDecisionTree));

        // When
        Optional<DecisionTreeEntity> result = cachedDecisionTreeService.findById("test-tree-1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("test-tree-1");
        assertThat(result.get().getName()).isEqualTo("Test Decision Tree");
        
        verify(decisionTreeRepository).findById("test-tree-1");
    }

    @Test
    void shouldFindActiveDecisionTrees() {
        // Given
        DecisionTreeEntity activeTree1 = new DecisionTreeEntity("active-1", "Active Tree 1", "ACTIVE");
        DecisionTreeEntity activeTree2 = new DecisionTreeEntity("active-2", "Active Tree 2", "ACTIVE");
        List<DecisionTreeEntity> activeTrees = Arrays.asList(activeTree1, activeTree2);
        
        when(decisionTreeRepository.findActiveDecisionTrees()).thenReturn(activeTrees);

        // When
        List<DecisionTreeEntity> result = cachedDecisionTreeService.findActiveDecisionTrees();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(DecisionTreeEntity::getStatus)
                          .containsOnly("ACTIVE");
        
        verify(decisionTreeRepository).findActiveDecisionTrees();
    }

    @Test
    void shouldSaveDecisionTree() {
        // Given
        when(decisionTreeRepository.save(any(DecisionTreeEntity.class))).thenReturn(testDecisionTree);

        // When
        DecisionTreeEntity result = cachedDecisionTreeService.save(testDecisionTree);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("test-tree-1");
        
        verify(decisionTreeRepository).save(testDecisionTree);
    }

    @Test
    void shouldDeleteDecisionTreeById() {
        // Given
        when(decisionTreeRepository.findById("test-tree-1")).thenReturn(Optional.of(testDecisionTree));
        doNothing().when(decisionTreeRepository).deleteById("test-tree-1");

        // When
        cachedDecisionTreeService.deleteById("test-tree-1");

        // Then
        verify(decisionTreeRepository).findById("test-tree-1");
        verify(decisionTreeRepository).deleteById("test-tree-1");
    }

    @Test
    void shouldUpdateDecisionTreeStatus() {
        // Given
        when(decisionTreeRepository.findById("test-tree-1")).thenReturn(Optional.of(testDecisionTree));
        
        DecisionTreeEntity updatedTree = new DecisionTreeEntity("test-tree-1", "Test Decision Tree", "INACTIVE");
        when(decisionTreeRepository.save(any(DecisionTreeEntity.class))).thenReturn(updatedTree);

        // When
        DecisionTreeEntity result = cachedDecisionTreeService.updateStatus("test-tree-1", "INACTIVE");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("INACTIVE");
        
        verify(decisionTreeRepository).findById("test-tree-1");
        verify(decisionTreeRepository).save(any(DecisionTreeEntity.class));
    }

    @Test
    void shouldFindDecisionTreesByStatus() {
        // Given
        List<DecisionTreeEntity> activeTrees = Arrays.asList(testDecisionTree);
        when(decisionTreeRepository.findByStatus("ACTIVE")).thenReturn(activeTrees);

        // When
        List<DecisionTreeEntity> result = cachedDecisionTreeService.findByStatus("ACTIVE");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("ACTIVE");
        
        verify(decisionTreeRepository).findByStatus("ACTIVE");
    }

    @Test
    void shouldFindDecisionTreeByIdWithNodes() {
        // Given
        when(decisionTreeRepository.findByIdWithNodes("test-tree-1")).thenReturn(Optional.of(testDecisionTree));

        // When
        Optional<DecisionTreeEntity> result = cachedDecisionTreeService.findByIdWithNodes("test-tree-1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("test-tree-1");
        
        verify(decisionTreeRepository).findByIdWithNodes("test-tree-1");
    }
}