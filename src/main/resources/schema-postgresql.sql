-- PostgreSQL Schema for Bank Customer Promotion System
-- This schema uses PostgreSQL-specific features like JSON data type

-- Core Business Tables

-- Decision Trees Table
CREATE TABLE IF NOT EXISTS decision_trees (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    root_node_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Decision Nodes Table (PostgreSQL JSON support)
CREATE TABLE IF NOT EXISTS decision_nodes (
    id VARCHAR(36) PRIMARY KEY,
    tree_id VARCHAR(36) NOT NULL,
    node_type VARCHAR(20) NOT NULL,
    parent_id VARCHAR(36),
    configuration JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tree_id) REFERENCES decision_trees(id)
);

-- Promotion Rules Table
CREATE TABLE IF NOT EXISTS promotion_rules (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    rule_type VARCHAR(20) NOT NULL,
    rule_content TEXT NOT NULL,
    parameters JSON,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Promotion History Table
CREATE TABLE IF NOT EXISTS promotion_history (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    promotion_id VARCHAR(36) NOT NULL,
    promotion_result JSON NOT NULL,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit Tables

-- Request Logs Table
CREATE TABLE IF NOT EXISTS request_logs (
    id VARCHAR(36) PRIMARY KEY,
    request_id VARCHAR(36) UNIQUE NOT NULL,
    api_endpoint VARCHAR(200) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    request_payload JSON NOT NULL,
    response_payload JSON,
    response_status INTEGER,
    client_ip VARCHAR(45),
    user_agent TEXT,
    processing_time_ms INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- Audit Trails Table
CREATE TABLE IF NOT EXISTS audit_trails (
    id VARCHAR(36) PRIMARY KEY,
    request_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    operation_details JSON NOT NULL,
    execution_time_ms INTEGER,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Decision Steps Table
CREATE TABLE IF NOT EXISTS decision_steps (
    id VARCHAR(36) PRIMARY KEY,
    request_id VARCHAR(36) NOT NULL,
    tree_id VARCHAR(36) NOT NULL,
    node_id VARCHAR(36) NOT NULL,
    step_order INTEGER NOT NULL,
    node_type VARCHAR(20) NOT NULL,
    input_data JSON NOT NULL,
    output_data JSON,
    execution_time_ms INTEGER,
    status VARCHAR(20) NOT NULL,
    error_details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES request_logs(request_id),
    FOREIGN KEY (tree_id) REFERENCES decision_trees(id)
);

-- System Events Table
CREATE TABLE IF NOT EXISTS system_events (
    id VARCHAR(36) PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    event_category VARCHAR(30) NOT NULL,
    event_details JSON NOT NULL,
    severity_level VARCHAR(20) NOT NULL,
    source_component VARCHAR(100) NOT NULL,
    correlation_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_customer_date ON promotion_history(customer_id, executed_at);
CREATE INDEX IF NOT EXISTS idx_tree_status ON decision_trees(status);
CREATE INDEX IF NOT EXISTS idx_rule_status ON promotion_rules(status);

-- Audit table indexes
CREATE INDEX IF NOT EXISTS idx_request_id ON request_logs(request_id);
CREATE INDEX IF NOT EXISTS idx_endpoint_date ON request_logs(api_endpoint, created_at);
CREATE INDEX IF NOT EXISTS idx_audit_request_id ON audit_trails(request_id);
CREATE INDEX IF NOT EXISTS idx_audit_customer_operation ON audit_trails(customer_id, operation_type);
CREATE INDEX IF NOT EXISTS idx_audit_created_at ON audit_trails(created_at);
CREATE INDEX IF NOT EXISTS idx_decision_request_step ON decision_steps(request_id, step_order);
CREATE INDEX IF NOT EXISTS idx_decision_tree_node ON decision_steps(tree_id, node_id);
CREATE INDEX IF NOT EXISTS idx_event_type_date ON system_events(event_type, created_at);
CREATE INDEX IF NOT EXISTS idx_event_correlation_id ON system_events(correlation_id);
CREATE INDEX IF NOT EXISTS idx_event_severity_date ON system_events(severity_level, created_at);

-- PostgreSQL-specific optimizations
CREATE INDEX IF NOT EXISTS idx_decision_nodes_config_gin ON decision_nodes USING GIN (configuration);
CREATE INDEX IF NOT EXISTS idx_promotion_rules_params_gin ON promotion_rules USING GIN (parameters);
CREATE INDEX IF NOT EXISTS idx_audit_details_gin ON audit_trails USING GIN (operation_details);