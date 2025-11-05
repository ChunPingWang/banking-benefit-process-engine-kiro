package com.bank.promotion.adapter.persistence.repository;

import com.bank.promotion.adapter.persistence.entity.DecisionTreeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for DecisionTreeRepository
 */
@DataJpaTest
@ActiveProfiles("test")
class DecisionTreeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DecisionTreeRepository decisionTreeRepository;

    private DecisionTreeEntity testDecisionTree;

    @BeforeEach
    void setUp() {
        testDecisionTree = new DecisionTreeEntity("test-tree-1", "Test Decision Tree", "ACTIVE");
        testDecisionTree.setRootNodeId("root-node-1");
    }

    @Test
    void shouldSaveAndFindDecisionTree() {
        // When
        DecisionTreeEntity saved = decisionTreeRepository.save(testDecisionTree);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isEqualTo("test-tree-1");
        assertThat(saved.getName()).isEqualTo("Test Decision Tree");
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        Optional<DecisionTreeEntity> found = decisionTreeRepository.findById("test-tree-1");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Decision Tree");
    }

    @Test
    void shouldFindDecisionTreesByStatus() {
        // Given
        DecisionTreeEntity activeTree = new DecisionTreeEntity("active-tree", "Active Tree", "ACTIVE");
        DecisionTreeEntity inactiveTree = new DecisionTreeEntity("inactive-tree", "Inactive Tree", "INACTIVE");
        
        decisionTreeRepository.save(activeTree);
        decisionTreeRepository.save(inactiveTree);
        entityManager.flush();

        // When
        List<DecisionTreeEntity> activeTrees = decisionTreeRepository.findByStatus("ACTIVE");
        List<DecisionTreeEntity> inactiveTrees = decisionTreeRepository.findByStatus("INACTIVE");

        // Then
        assertThat(activeTrees).hasSize(1);
        assertThat(activeTrees.get(0).getName()).isEqualTo("Active Tree");
        
        assertThat(inactiveTrees).hasSize(1);
        assertThat(inactiveTrees.get(0).getName()).isEqualTo("Inactive Tree");
    }

    @Test
    void shouldFindActiveDecisionTrees() {
        // Given
        DecisionTreeEntity activeTree1 = new DecisionTreeEntity("active-tree-1", "Active Tree 1", "ACTIVE");
        DecisionTreeEntity activeTree2 = new DecisionTreeEntity("active-tree-2", "Active Tree 2", "ACTIVE");
        DecisionTreeEntity inactiveTree = new DecisionTreeEntity("inactive-tree", "Inactive Tree", "INACTIVE");
        
        decisionTreeRepository.save(activeTree1);
        decisionTreeRepository.save(activeTree2);
        decisionTreeRepository.save(inactiveTree);
        entityManager.flush();

        // When
        List<DecisionTreeEntity> activeTrees = decisionTreeRepository.findActiveDecisionTrees();

        // Then
        assertThat(activeTrees).hasSize(2);
        assertThat(activeTrees).extracting(DecisionTreeEntity::getStatus)
                               .containsOnly("ACTIVE");
    }

    @Test
    void shouldFindDecisionTreeByName() {
        // Given
        decisionTreeRepository.save(testDecisionTree);
        entityManager.flush();

        // When
        Optional<DecisionTreeEntity> found = decisionTreeRepository.findByName("Test Decision Tree");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo("test-tree-1");
    }

    @Test
    void shouldCheckIfDecisionTreeExistsByName() {
        // Given
        decisionTreeRepository.save(testDecisionTree);
        entityManager.flush();

        // When & Then
        assertThat(decisionTreeRepository.existsByName("Test Decision Tree")).isTrue();
        assertThat(decisionTreeRepository.existsByName("Non-existent Tree")).isFalse();
    }

    @Test
    void shouldCountDecisionTreesByStatus() {
        // Given
        DecisionTreeEntity activeTree1 = new DecisionTreeEntity("active-tree-1", "Active Tree 1", "ACTIVE");
        DecisionTreeEntity activeTree2 = new DecisionTreeEntity("active-tree-2", "Active Tree 2", "ACTIVE");
        DecisionTreeEntity inactiveTree = new DecisionTreeEntity("inactive-tree", "Inactive Tree", "INACTIVE");
        
        decisionTreeRepository.save(activeTree1);
        decisionTreeRepository.save(activeTree2);
        decisionTreeRepository.save(inactiveTree);
        entityManager.flush();

        // When & Then
        assertThat(decisionTreeRepository.countByStatus("ACTIVE")).isEqualTo(2);
        assertThat(decisionTreeRepository.countByStatus("INACTIVE")).isEqualTo(1);
        assertThat(decisionTreeRepository.countByStatus("DRAFT")).isEqualTo(0);
    }

    @Test
    void shouldDeleteDecisionTree() {
        // Given
        decisionTreeRepository.save(testDecisionTree);
        entityManager.flush();
        
        assertThat(decisionTreeRepository.findById("test-tree-1")).isPresent();

        // When
        decisionTreeRepository.deleteById("test-tree-1");
        entityManager.flush();

        // Then
        assertThat(decisionTreeRepository.findById("test-tree-1")).isEmpty();
    }

    @Test
    void shouldUpdateDecisionTreeStatus() {
        // Given
        decisionTreeRepository.save(testDecisionTree);
        entityManager.flush();

        // When
        testDecisionTree.setStatus("INACTIVE");
        DecisionTreeEntity updated = decisionTreeRepository.save(testDecisionTree);
        entityManager.flush();

        // Then
        assertThat(updated.getStatus()).isEqualTo("INACTIVE");
        assertThat(updated.getUpdatedAt()).isNotNull();
        
        Optional<DecisionTreeEntity> found = decisionTreeRepository.findById("test-tree-1");
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo("INACTIVE");
    }
}