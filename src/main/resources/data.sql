-- Sample Decision Tree
INSERT INTO decision_trees (id, name, status, root_node_id) VALUES 
('dt-001', 'Customer Promotion Decision Tree', 'ACTIVE', 'node-001');

-- Sample Decision Nodes
INSERT INTO decision_nodes (id, tree_id, node_type, parent_id, configuration) VALUES 
('node-001', 'dt-001', 'CONDITION', NULL, '{"type":"SpEL","expression":"customer.annualIncome >= 2000000","trueNodeId":"node-002","falseNodeId":"node-003"}'),
('node-002', 'dt-001', 'CALCULATION', 'node-001', '{"type":"VIP","promotionName":"VIP專屬理財優惠","discountRate":0.15}'),
('node-003', 'dt-001', 'CALCULATION', 'node-001', '{"type":"Regular","promotionName":"新戶開戶優惠","discountRate":0.05}');

-- Sample Promotion Rules
INSERT INTO promotion_rules (id, name, rule_type, rule_content, parameters, status) VALUES 
('rule-001', 'VIP Customer Rule', 'DROOLS', 'VIP Customer Promotion', '{"minIncome":2000000,"accountType":"VIP"}', 'ACTIVE'),
('rule-002', 'Regular Customer Rule', 'DROOLS', 'Regular Customer Promotion', '{"minIncome":500000,"accountType":"一般"}', 'ACTIVE');