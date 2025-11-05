package com.bank.promotion.bdd;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Cucumber 測試鉤子
 * 處理測試前後的設定和清理
 */
public class TestHooks {
    
    @Autowired
    private TestDataManager testDataManager;
    
    @Before
    public void setUp() {
        testDataManager.initializeTestData();
    }
    
    @After
    public void tearDown() {
        testDataManager.cleanupTestData();
    }
}