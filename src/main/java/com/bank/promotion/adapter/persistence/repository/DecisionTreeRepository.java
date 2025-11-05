package com.bank.promotion.adapter.persistence.repository;

import com.bank.promotion.adapter.persistence.entity.DecisionTreeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DecisionTree entities
 * Provides CRUD operations and custom queries
 */
@Repository
public interface DecisionTreeRepository extends JpaRepository<DecisionTreeEntity, String>, 
                                              JpaSpecificationExecutor<DecisionTreeEntity> {

    /**
     * Find decision trees by status
     */
    List<DecisionTreeEntity> findByStatus(String status);

    /**
     * Find decision tree by name
     */
    Optional<DecisionTreeEntity> findByName(String name);

    /**
     * Find active decision trees
     */
    @Query("SELECT dt FROM DecisionTreeEntity dt WHERE dt.status = 'ACTIVE'")
    List<DecisionTreeEntity> findActiveDecisionTrees();

    /**
     * Find decision trees with their nodes
     */
    @Query("SELECT DISTINCT dt FROM DecisionTreeEntity dt LEFT JOIN FETCH dt.nodes WHERE dt.id = :id")
    Optional<DecisionTreeEntity> findByIdWithNodes(@Param("id") String id);

    /**
     * Find decision trees by status with nodes
     */
    @Query("SELECT DISTINCT dt FROM DecisionTreeEntity dt LEFT JOIN FETCH dt.nodes WHERE dt.status = :status")
    List<DecisionTreeEntity> findByStatusWithNodes(@Param("status") String status);

    /**
     * Check if decision tree exists by name
     */
    boolean existsByName(String name);

    /**
     * Count decision trees by status
     */
    long countByStatus(String status);
}