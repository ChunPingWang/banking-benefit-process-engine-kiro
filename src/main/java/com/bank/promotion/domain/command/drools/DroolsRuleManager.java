package com.bank.promotion.domain.command.drools;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Drools 規則管理器
 * 提供規則的熱更新和快取管理功能
 */
@Component
public class DroolsRuleManager {
    
    private final ConcurrentMap<String, KieContainer> ruleContainers;
    private final ConcurrentMap<String, String> ruleContents;
    private final KieServices kieServices;
    
    public DroolsRuleManager() {
        this.ruleContainers = new ConcurrentHashMap<>();
        this.ruleContents = new ConcurrentHashMap<>();
        this.kieServices = KieServices.Factory.get();
    }
    
    /**
     * 註冊或更新規則
     * 
     * @param ruleId 規則ID
     * @param ruleContent 規則內容
     * @return 是否成功註冊/更新
     */
    public boolean registerRule(String ruleId, String ruleContent) {
        if (ruleId == null || ruleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Rule ID cannot be null or empty");
        }
        
        if (ruleContent == null || ruleContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Rule content cannot be null or empty");
        }
        
        try {
            // 建構新的 KieContainer
            KieContainer newContainer = buildKieContainer(ruleId, ruleContent);
            
            // 更新快取
            KieContainer oldContainer = ruleContainers.put(ruleId, newContainer);
            ruleContents.put(ruleId, ruleContent);
            
            // 清理舊的容器
            if (oldContainer != null) {
                oldContainer.dispose();
            }
            
            return true;
            
        } catch (Exception e) {
            // 如果更新失敗，保持原有的規則
            return false;
        }
    }
    
    /**
     * 獲取規則容器
     * 
     * @param ruleId 規則ID
     * @return KieContainer，如果不存在則返回 null
     */
    public KieContainer getRuleContainer(String ruleId) {
        if (ruleId == null || ruleId.trim().isEmpty()) {
            return null;
        }
        return ruleContainers.get(ruleId.trim());
    }
    
    /**
     * 檢查規則是否存在
     * 
     * @param ruleId 規則ID
     * @return 是否存在
     */
    public boolean hasRule(String ruleId) {
        if (ruleId == null || ruleId.trim().isEmpty()) {
            return false;
        }
        return ruleContainers.containsKey(ruleId.trim());
    }
    
    /**
     * 移除規則
     * 
     * @param ruleId 規則ID
     * @return 是否成功移除
     */
    public boolean removeRule(String ruleId) {
        if (ruleId == null || ruleId.trim().isEmpty()) {
            return false;
        }
        
        String normalizedId = ruleId.trim();
        KieContainer container = ruleContainers.remove(normalizedId);
        ruleContents.remove(normalizedId);
        
        if (container != null) {
            container.dispose();
            return true;
        }
        
        return false;
    }
    
    /**
     * 驗證規則語法
     * 
     * @param ruleContent 規則內容
     * @return 驗證結果
     */
    public RuleValidationResult validateRule(String ruleContent) {
        if (ruleContent == null || ruleContent.trim().isEmpty()) {
            return new RuleValidationResult(false, List.of("Rule content cannot be null or empty"));
        }
        
        try {
            String tempRuleId = "temp_validation_" + System.currentTimeMillis();
            buildKieContainer(tempRuleId, ruleContent);
            return new RuleValidationResult(true, List.of());
            
        } catch (Exception e) {
            return new RuleValidationResult(false, List.of(e.getMessage()));
        }
    }
    
    /**
     * 建構 KieContainer
     */
    private KieContainer buildKieContainer(String ruleId, String ruleContent) {
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        
        // 建立完整的規則檔案內容
        String fullRuleContent = buildFullRuleContent(ruleId, ruleContent);
        
        // 將規則加入到 KieFileSystem
        kieFileSystem.write("src/main/resources/rules/" + ruleId + ".drl", fullRuleContent);
        
        // 建構 KieBuilder
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        
        // 檢查建構錯誤
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            List<String> errors = new ArrayList<>();
            for (Message message : kieBuilder.getResults().getMessages(Message.Level.ERROR)) {
                errors.add(message.getText());
            }
            throw new IllegalArgumentException("Drools rule compilation errors: " + String.join(", ", errors));
        }
        
        return kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
    }
    
    /**
     * 建構完整的規則檔案內容
     */
    private String buildFullRuleContent(String ruleId, String ruleContent) {
        StringBuilder fullContent = new StringBuilder();
        
        // 加入 package 宣告
        fullContent.append("package com.bank.promotion.rules;\n\n");
        
        // 加入必要的 import
        fullContent.append("import com.bank.promotion.domain.valueobject.CustomerPayload;\n");
        fullContent.append("import com.bank.promotion.domain.valueobject.PromotionResult;\n");
        fullContent.append("import java.math.BigDecimal;\n");
        fullContent.append("import java.time.LocalDateTime;\n");
        fullContent.append("import java.util.Map;\n");
        fullContent.append("import java.util.HashMap;\n\n");
        
        // 加入全域變數宣告
        fullContent.append("global java.util.Map results;\n\n");
        
        // 如果規則內容不包含完整的規則定義，則包裝
        if (!ruleContent.trim().contains("rule ") || !ruleContent.trim().contains("when") || !ruleContent.trim().contains("then")) {
            fullContent.append("rule \"").append(ruleId).append("\"\n");
            fullContent.append("when\n");
            fullContent.append("    $customer : CustomerPayload()\n");
            fullContent.append("then\n");
            fullContent.append("    ").append(ruleContent).append("\n");
            fullContent.append("end\n");
        } else {
            // 如果已經是完整的規則，直接使用
            fullContent.append(ruleContent);
        }
        
        return fullContent.toString();
    }
    
    /**
     * 獲取所有已註冊的規則ID
     * 
     * @return 規則ID列表
     */
    public List<String> getAllRuleIds() {
        return new ArrayList<>(ruleContainers.keySet());
    }
    
    /**
     * 獲取規則內容
     * 
     * @param ruleId 規則ID
     * @return 規則內容，如果不存在則返回 null
     */
    public String getRuleContent(String ruleId) {
        if (ruleId == null || ruleId.trim().isEmpty()) {
            return null;
        }
        return ruleContents.get(ruleId.trim());
    }
    
    /**
     * 清除所有規則
     */
    public void clearAllRules() {
        // 清理所有容器
        for (KieContainer container : ruleContainers.values()) {
            container.dispose();
        }
        
        ruleContainers.clear();
        ruleContents.clear();
    }
    
    /**
     * 獲取規則統計資訊
     * 
     * @return 統計資訊
     */
    public RuleStatistics getStatistics() {
        return new RuleStatistics(ruleContainers.size());
    }
    
    /**
     * 規則驗證結果
     */
    public static class RuleValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public RuleValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        @Override
        public String toString() {
            return "RuleValidationResult{valid=" + valid + ", errors=" + errors + "}";
        }
    }
    
    /**
     * 規則統計資訊
     */
    public static class RuleStatistics {
        private final int totalRules;
        
        public RuleStatistics(int totalRules) {
            this.totalRules = totalRules;
        }
        
        public int getTotalRules() {
            return totalRules;
        }
        
        @Override
        public String toString() {
            return "RuleStatistics{totalRules=" + totalRules + "}";
        }
    }
}