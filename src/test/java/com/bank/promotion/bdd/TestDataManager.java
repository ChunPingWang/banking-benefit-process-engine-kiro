package com.bank.promotion.bdd;

import com.bank.promotion.bdd.audit.TestAuditTracker;
import com.bank.promotion.bdd.mock.MockExternalSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 測試資料管理器
 * 負責測試資料的初始化和清理
 */
@Component
public class TestDataManager {
    
    @Autowired
    private TestAuditTracker auditTracker;
    
    @Autowired
    private MockExternalSystemService mockExternalSystemService;
    
    private final Map<String, TestCustomerData> testCustomers = new HashMap<>();
    private final Map<String, TestDecisionTreeData> testDecisionTrees = new HashMap<>();
    
    /**
     * 初始化測試資料
     */
    public void initializeTestData() {
        initializeTestCustomers();
        initializeTestDecisionTrees();
        mockExternalSystemService.resetMockData();
    }
    
    /**
     * 清理測試資料
     */
    public void cleanupTestData() {
        testCustomers.clear();
        testDecisionTrees.clear();
        auditTracker.clearAll();
        mockExternalSystemService.resetMockData();
    }
    
    /**
     * 獲取測試客戶資料
     */
    public TestCustomerData getTestCustomer(String customerType) {
        return testCustomers.get(customerType);
    }
    
    /**
     * 獲取測試決策樹資料
     */
    public TestDecisionTreeData getTestDecisionTree(String treeType) {
        return testDecisionTrees.get(treeType);
    }
    
    /**
     * 初始化測試客戶資料
     */
    private void initializeTestCustomers() {
        // VIP 客戶
        testCustomers.put("VIP", new TestCustomerData(
            "CUST001",
            "VIP",
            BigDecimal.valueOf(2000000),
            30,
            "台北市",
            "金融業"
        ));
        
        // 一般客戶
        testCustomers.put("REGULAR", new TestCustomerData(
            "CUST002", 
            "一般",
            BigDecimal.valueOf(800000),
            25,
            "新北市",
            "科技業"
        ));
        
        // 新客戶
        testCustomers.put("NEW", new TestCustomerData(
            "CUST003",
            "新客戶",
            BigDecimal.valueOf(500000),
            28,
            "桃園市",
            "製造業"
        ));
        
        // 低信用客戶
        testCustomers.put("LOW_CREDIT", new TestCustomerData(
            "CUST004",
            "一般",
            BigDecimal.valueOf(300000),
            35,
            "台中市",
            "服務業"
        ));
    }
    
    /**
     * 初始化測試決策樹資料
     */
    private void initializeTestDecisionTrees() {
        // VIP 優惠決策樹
        testDecisionTrees.put("VIP_PROMOTION", new TestDecisionTreeData(
            "TREE001",
            "VIP客戶優惠決策樹",
            "ROOT001",
            "ACTIVE"
        ));
        
        // 一般優惠決策樹
        testDecisionTrees.put("REGULAR_PROMOTION", new TestDecisionTreeData(
            "TREE002",
            "一般客戶優惠決策樹", 
            "ROOT002",
            "ACTIVE"
        ));
        
        // 新戶優惠決策樹
        testDecisionTrees.put("NEW_CUSTOMER_PROMOTION", new TestDecisionTreeData(
            "TREE003",
            "新戶優惠決策樹",
            "ROOT003", 
            "ACTIVE"
        ));
    }
    
    /**
     * 測試客戶資料類別
     */
    public static class TestCustomerData {
        private final String customerId;
        private final String accountType;
        private final BigDecimal annualIncome;
        private final int age;
        private final String location;
        private final String industry;
        
        public TestCustomerData(String customerId, String accountType, BigDecimal annualIncome,
                              int age, String location, String industry) {
            this.customerId = customerId;
            this.accountType = accountType;
            this.annualIncome = annualIncome;
            this.age = age;
            this.location = location;
            this.industry = industry;
        }
        
        public String getCustomerId() { return customerId; }
        public String getAccountType() { return accountType; }
        public BigDecimal getAnnualIncome() { return annualIncome; }
        public int getAge() { return age; }
        public String getLocation() { return location; }
        public String getIndustry() { return industry; }
    }
    
    /**
     * 測試決策樹資料類別
     */
    public static class TestDecisionTreeData {
        private final String treeId;
        private final String treeName;
        private final String rootNodeId;
        private final String status;
        
        public TestDecisionTreeData(String treeId, String treeName, String rootNodeId, String status) {
            this.treeId = treeId;
            this.treeName = treeName;
            this.rootNodeId = rootNodeId;
            this.status = status;
        }
        
        public String getTreeId() { return treeId; }
        public String getTreeName() { return treeName; }
        public String getRootNodeId() { return rootNodeId; }
        public String getStatus() { return status; }
    }
}