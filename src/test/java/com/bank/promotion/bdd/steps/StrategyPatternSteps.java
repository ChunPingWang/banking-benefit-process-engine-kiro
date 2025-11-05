package com.bank.promotion.bdd.steps;

import com.bank.promotion.bdd.BaseStepDefinitions;
import com.bank.promotion.domain.aggregate.CustomerProfile;
import com.bank.promotion.domain.strategy.CalculationStrategy;
import com.bank.promotion.domain.strategy.CalculationStrategyFactory;
import com.bank.promotion.domain.valueobject.CustomerPayload;
import com.bank.promotion.domain.valueobject.PromotionResult;
import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.而且;
import io.cucumber.java.zh_tw.那麼;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 策略模式 BDD 步驟定義
 */
public class StrategyPatternSteps extends BaseStepDefinitions {
    
    @Autowired
    private CalculationStrategyFactory strategyFactory;
    
    private CustomerProfile testCustomer;
    private CalculationStrategy currentStrategy;
    private Map<String, Object> strategyParameters;
    private PromotionResult calculationResult;
    private BigDecimal calculatedAmount;
    private Exception lastException;
    
    @假設("系統配置了 {string} 計算策略")
    public void 系統配置了計算策略(String strategyType) {
        currentStrategy = strategyFactory.getStrategy(strategyType);
        assertNotNull(currentStrategy, "策略應該存在: " + strategyType);
        recordSystemEvent("STRATEGY_CONFIG", "SETUP", 
            Map.of("strategyType", strategyType), "INFO", "CalculationStrategyFactory");
    }
    
    @而且("客戶年收入為 {int} 元")
    public void 客戶年收入為元(int annualIncome) {
        CustomerPayload customerPayload = new CustomerPayload(
            "CUST001", "PREMIUM", BigDecimal.valueOf(annualIncome), 750, "台北", 50
        );
        testCustomer = new CustomerProfile("CUST001", customerPayload);
    }
    
    @而且("客戶帳戶類型為 {string}")
    public void 客戶帳戶類型為(String accountType) {
        BigDecimal annualIncome = (testCustomer != null) ? 
            testCustomer.getBasicInfo().getAnnualIncome() : BigDecimal.valueOf(1000000);
        CustomerPayload customerPayload = new CustomerPayload(
            "CUST001", accountType, annualIncome, 
            750, "台北", 50
        );
        testCustomer = new CustomerProfile("CUST001", customerPayload);
    }
    
    @而且("策略參數設定折扣百分比為 {double}%")
    public void 策略參數設定折扣百分比為(double discountPercentage) {
        if (strategyParameters == null) {
            strategyParameters = new HashMap<>();
        }
        strategyParameters.put("discountPercentage", BigDecimal.valueOf(discountPercentage));
    }
    
    @而且("策略參數設定基準金額為 {int} 元")
    public void 策略參數設定基準金額為元(int baseAmount) {
        if (strategyParameters == null) {
            strategyParameters = new HashMap<>();
        }
        strategyParameters.put("baseAmount", BigDecimal.valueOf(baseAmount));
    }
    
    @而且("策略參數設定固定優惠金額為 {int} 元")
    public void 策略參數設定固定優惠金額為元(int fixedAmount) {
        if (strategyParameters == null) {
            strategyParameters = new HashMap<>();
        }
        strategyParameters.put("fixedAmount", BigDecimal.valueOf(fixedAmount));
    }
    
    @而且("策略參數設定最低購買金額為 {int} 元")
    public void 策略參數設定最低購買金額為元(int minPurchaseAmount) {
        if (strategyParameters == null) {
            strategyParameters = new HashMap<>();
        }
        strategyParameters.put("minPurchaseAmount", BigDecimal.valueOf(minPurchaseAmount));
    }
    
    @而且("策略參數設定階層式折扣規則")
    public void 策略參數設定階層式折扣規則() {
        if (strategyParameters == null) {
            strategyParameters = new HashMap<>();
        }
        
        List<Map<String, Object>> tiers = List.of(
            Map.of(
                "minAmount", BigDecimal.valueOf(2000000),
                "discountPercentage", BigDecimal.valueOf(15),
                "description", "VIP頂級客戶"
            ),
            Map.of(
                "minAmount", BigDecimal.valueOf(1000000),
                "discountPercentage", BigDecimal.valueOf(10),
                "description", "VIP客戶"
            ),
            Map.of(
                "minAmount", BigDecimal.valueOf(500000),
                "discountPercentage", BigDecimal.valueOf(5),
                "description", "優質客戶"
            )
        );
        
        strategyParameters.put("tiers", tiers);
    }
    
    @而且("策略參數設定優惠名稱為 {string}")
    public void 策略參數設定優惠名稱為(String promotionName) {
        if (strategyParameters == null) {
            strategyParameters = new HashMap<>();
        }
        strategyParameters.put("promotionName", promotionName);
    }
    
    @當("執行策略計算")
    public void 執行策略計算() {
        try {
            calculatedAmount = currentStrategy.calculate(testCustomer, strategyParameters);
            recordSystemEvent("STRATEGY_CALCULATION", "PROCESSING",
                Map.of("strategyType", currentStrategy.getStrategyType(), 
                       "parameters", strategyParameters,
                       "result", calculatedAmount), 
                "INFO", "CalculationStrategy");
        } catch (Exception e) {
            lastException = e;
            recordSystemEvent("STRATEGY_ERROR", "ERROR",
                Map.of("strategyType", currentStrategy.getStrategyType(),
                       "error", e.getMessage()),
                "ERROR", "CalculationStrategy");
        }
    }
    
    @當("建立優惠結果")
    public void 建立優惠結果() {
        try {
            calculationResult = currentStrategy.createPromotionResult(testCustomer, calculatedAmount, strategyParameters);
            recordSystemEvent("PROMOTION_RESULT_CREATION", "PROCESSING",
                Map.of("promotionId", calculationResult.getPromotionId(),
                       "promotionType", calculationResult.getPromotionType(),
                       "discountAmount", calculationResult.getDiscountAmount()),
                "INFO", "CalculationStrategy");
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @那麼("計算結果應該為 {double} 元")
    public void 計算結果應該為元(double expectedAmount) {
        assertNotNull(calculatedAmount, "計算結果不應該為空");
        assertEquals(0, calculatedAmount.compareTo(BigDecimal.valueOf(expectedAmount)), 
            "計算結果應該為 " + expectedAmount + " 元，實際為 " + calculatedAmount + " 元");
    }
    
    @而且("優惠結果應該包含正確的策略類型")
    public void 優惠結果應該包含正確的策略類型() {
        assertNotNull(calculationResult, "優惠結果不應該為空");
        assertEquals(currentStrategy.getStrategyType(), calculationResult.getPromotionType(),
            "優惠結果的策略類型應該匹配");
    }
    
    @而且("優惠結果應該標示為符合條件")
    public void 優惠結果應該標示為符合條件() {
        assertNotNull(calculationResult, "優惠結果不應該為空");
        assertTrue(calculationResult.isEligible(), "優惠結果應該標示為符合條件");
    }
    
    @而且("優惠結果應該標示為不符合條件")
    public void 優惠結果應該標示為不符合條件() {
        assertNotNull(calculationResult, "優惠結果不應該為空");
        assertFalse(calculationResult.isEligible(), "優惠結果應該標示為不符合條件");
    }
    
    @而且("優惠結果描述應該包含 {string}")
    public void 優惠結果描述應該包含(String expectedText) {
        assertNotNull(calculationResult, "優惠結果不應該為空");
        assertNotNull(calculationResult.getDescription(), "優惠描述不應該為空");
        assertTrue(calculationResult.getDescription().contains(expectedText),
            "優惠描述應該包含 '" + expectedText + "'，實際描述: " + calculationResult.getDescription());
    }
    
    @而且("優惠結果應該包含折扣百分比 {double}%")
    public void 優惠結果應該包含折扣百分比(double expectedPercentage) {
        assertNotNull(calculationResult, "優惠結果不應該為空");
        assertNotNull(calculationResult.getDiscountPercentage(), "折扣百分比不應該為空");
        assertEquals(0, calculationResult.getDiscountPercentage().compareTo(BigDecimal.valueOf(expectedPercentage)),
            "折扣百分比應該為 " + expectedPercentage + "%");
    }
    
    @而且("優惠結果的折扣百分比應該為空")
    public void 優惠結果的折扣百分比應該為空() {
        assertNotNull(calculationResult, "優惠結果不應該為空");
        assertNull(calculationResult.getDiscountPercentage(), "固定金額策略的折扣百分比應該為空");
    }
    
    @當("使用無效的策略參數")
    public void 使用無效的策略參數() {
        strategyParameters = new HashMap<>();
        strategyParameters.put("invalidParameter", "invalidValue");
        // 不設定必要參數，使參數無效
    }
    
    @那麼("應該拋出參數驗證異常")
    public void 應該拋出參數驗證異常() {
        assertNotNull(lastException, "應該有異常被拋出");
        assertTrue(lastException instanceof IllegalArgumentException, 
            "應該拋出 IllegalArgumentException");
        assertTrue(lastException.getMessage().contains("Invalid parameters"),
            "異常訊息應該包含參數驗證錯誤");
    }
    
    @當("驗證策略參數")
    public void 驗證策略參數() {
        boolean isValid = currentStrategy.validateParameters(strategyParameters);
        recordSystemEvent("PARAMETER_VALIDATION", "VALIDATION",
            Map.of("strategyType", currentStrategy.getStrategyType(),
                   "parameters", strategyParameters,
                   "isValid", isValid),
            "INFO", "CalculationStrategy");
    }
    
    @那麼("參數驗證應該通過")
    public void 參數驗證應該通過() {
        assertTrue(currentStrategy.validateParameters(strategyParameters),
            "策略參數驗證應該通過");
    }
    
    @那麼("參數驗證應該失敗")
    public void 參數驗證應該失敗() {
        assertFalse(currentStrategy.validateParameters(strategyParameters),
            "策略參數驗證應該失敗");
    }
    
    @而且("優惠結果的附加詳情應該包含策略類型")
    public void 優惠結果的附加詳情應該包含策略類型() {
        assertNotNull(calculationResult, "優惠結果不應該為空");
        Map<String, Object> additionalDetails = calculationResult.getAdditionalDetails();
        assertNotNull(additionalDetails, "附加詳情不應該為空");
        assertEquals(currentStrategy.getStrategyType(), 
                    additionalDetails.get("strategyType"),
                    "附加詳情應該包含正確的策略類型");
    }
    
    @而且("優惠結果應該包含客戶ID")
    public void 優惠結果應該包含客戶ID() {
        assertNotNull(calculationResult, "優惠結果不應該為空");
        Map<String, Object> additionalDetails = calculationResult.getAdditionalDetails();
        assertEquals(testCustomer.getCustomerId(), 
                    additionalDetails.get("customerId"),
                    "附加詳情應該包含客戶ID");
    }
    
    @而且("優惠結果應該有有效期限")
    public void 優惠結果應該有有效期限() {
        assertNotNull(calculationResult, "優惠結果不應該為空");
        assertNotNull(calculationResult.getValidUntil(), "優惠有效期限不應該為空");
        assertTrue(calculationResult.getValidUntil().isAfter(java.time.LocalDateTime.now()),
            "優惠有效期限應該在未來");
    }
}