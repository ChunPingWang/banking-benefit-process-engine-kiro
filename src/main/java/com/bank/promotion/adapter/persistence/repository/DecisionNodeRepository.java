package com.bank.promotion.adapter.persistence.repository;

import com.bank.promotion.adapter.persistence.entity.DecisionNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DecisionNode entities
 * Provides CRUD operations and custom queries
 */
@Repository
public interface DecisionNodeRepository extends JpaRepository<DecisionNodeEntity, String>, 
                                              JpaSpecificationExecutor<DecisionNodeEntity> {

    /**
     * Find nodes by tree ID
     */
    List<DecisionNodeEntity> findByTreeId(String treeId);

    /**
     * Find nodes by tree ID and node type
     */
    List<DecisionNodeEntity> findByTreeIdAndNodeType(String treeId, String nodeType);

    /**
     * Find child nodes by parent ID
     */
    List<DecisionNodeEntity> findByParentId(String parentId);

    /**
     * Find root nodes (nodes without parent)
     */
    @Query("SELECT dn FROM DecisionNodeEntity dn WHERE dn.parentId IS NULL AND dn.treeId = :treeId")
    List<DecisionNodeEntity> findRootNodesByTreeId(@Param("treeId") String treeId);

    /**
     * Find leaf nodes (nodes without children)
     */
    @Query("SELECT dn FROM DecisionNodeEntity dn WHERE dn.id NOT IN " +
           "(SELECT DISTINCT dn2.parentId FROM DecisionNodeEntity dn2 WHERE dn2.parentId IS NOT NULL) " +
           "AND dn.treeId = :treeId")
    List<DecisionNodeEntity> findLeafNodesByTreeId(@Param("treeId") String treeId);

    /**
     * Find nodes by tree ID ordered by creation time
     */
    List<DecisionNodeEntity> findByTreeIdOrderByCreatedAt(String treeId);

    /**
     * Count nodes by tree ID
     */
    long countByTreeId(String treeId);

    /**
     * Count nodes by tree ID and node type
     */
    long countByTreeIdAndNodeType(String treeId, String nodeType);

    /**
     * Delete nodes by tree ID
     */
    void deleteByTreeId(String treeId);
}