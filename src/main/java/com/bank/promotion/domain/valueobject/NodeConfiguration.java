package com.bank.promotion.domain.valueobject;

import java.util.Map;
import java.util.Objects;

/**
 * 節點配置值物件
 * 包含決策節點的配置資訊
 */
public final class NodeConfiguration {
    
    private final String nodeId;
    private final String nodeType;
    private final String expression;
    private final String commandType;
    private final Map<String, Object> parameters;
    private final String description;
    
    public NodeConfiguration(String nodeId, String nodeType, String expression,
                           String commandType, Map<String, Object> parameters, String description) {
        this.nodeId = validateNodeId(nodeId);
        this.nodeType = validateNodeType(nodeType);
        this.expression = expression;
        this.commandType = validateCommandType(commandType);
        this.parameters = parameters != null ? Map.copyOf(parameters) : Map.of();
        this.description = description;
    }
    
    private String validateNodeId(String nodeId) {
        if (nodeId == null || nodeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Node ID cannot be null or empty");
        }
        return nodeId.trim();
    }
    
    private String validateNodeType(String nodeType) {
        if (nodeType == null || nodeType.trim().isEmpty()) {
            throw new IllegalArgumentException("Node type cannot be null or empty");
        }
        if (!isValidNodeType(nodeType.trim())) {
            throw new IllegalArgumentException("Invalid node type: " + nodeType);
        }
        return nodeType.trim();
    }
    
    private boolean isValidNodeType(String nodeType) {
        return "CONDITION".equals(nodeType) || "CALCULATION".equals(nodeType);
    }
    
    private String validateCommandType(String commandType) {
        if (commandType == null || commandType.trim().isEmpty()) {
            throw new IllegalArgumentException("Command type cannot be null or empty");
        }
        if (!isValidCommandType(commandType.trim())) {
            throw new IllegalArgumentException("Invalid command type: " + commandType);
        }
        return commandType.trim();
    }
    
    private boolean isValidCommandType(String commandType) {
        return "SPEL".equals(commandType) || 
               "DROOLS".equals(commandType) || 
               "EXTERNAL_SYSTEM".equals(commandType) || 
               "DATABASE_QUERY".equals(commandType);
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
    public String getNodeType() {
        return nodeType;
    }
    
    public String getExpression() {
        return expression;
    }
    
    public String getCommandType() {
        return commandType;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeConfiguration that = (NodeConfiguration) o;
        return Objects.equals(nodeId, that.nodeId) &&
               Objects.equals(nodeType, that.nodeType) &&
               Objects.equals(expression, that.expression) &&
               Objects.equals(commandType, that.commandType) &&
               Objects.equals(parameters, that.parameters) &&
               Objects.equals(description, that.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nodeId, nodeType, expression, commandType, parameters, description);
    }
    
    @Override
    public String toString() {
        return "NodeConfiguration{" +
               "nodeId='" + nodeId + '\'' +
               ", nodeType='" + nodeType + '\'' +
               ", expression='" + expression + '\'' +
               ", commandType='" + commandType + '\'' +
               ", parameters=" + parameters +
               ", description='" + description + '\'' +
               '}';
    }
}