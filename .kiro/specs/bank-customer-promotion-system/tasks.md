# 實作計劃

- [x] 1. 建立專案結構和核心配置
  - 建立 Spring Boot 3 專案結構，配置 Gradle 建置檔案
  - 設定多環境配置檔案 (dev/sit/uat/prod)
  - 配置 H2 和 PostgreSQL 資料庫連線
  - 整合 Drools 規則引擎依賴
  - _需求: 1.2, 2.1, 6.1_

- [x] 2. 實作領域模型和聚合根
  - [x] 2.1 建立核心值物件和實體
    - 實作 CustomerPayload、PromotionResult、NodeConfiguration 值物件
    - 建立 DecisionNode 抽象類別和 ConditionNode、CalculationNode 實體
    - 定義領域異常類別層次結構
    - _需求: 7.1, 7.3, 6.3_

  - [x] 2.2 實作 PromotionDecisionTree 聚合根
    - 建立 PromotionDecisionTree 聚合根類別
    - 實作決策樹評估邏輯和節點遍歷機制
    - 加入樹結構一致性驗證
    - _需求: 7.3, 5.1, 5.2_

  - [x] 2.3 實作 CustomerProfile 聚合根
    - 建立 CustomerProfile 聚合根類別
    - 實作客戶條件評估介面
    - 加入客戶資料驗證邏輯
    - _需求: 7.1, 1.1, 1.3_

- [ ] 3. 實作 Command Pattern 和節點命令
  - [ ] 3.1 建立命令介面和基礎架構
    - 定義 NodeCommand 介面和 ExecutionContext
    - 實作命令工廠和命令註冊機制
    - 建立命令執行結果 NodeResult 類別
    - _需求: 3.1, 3.2, 3.6_

  - [ ] 3.2 實作 SpEL 表達式命令
    - 建立 SpELConditionCommand 和 SpELCalculationCommand
    - 整合 Spring Expression Language 解析器
    - 實作表達式快取機制
    - _需求: 2.3, 2.5, 3.1_

  - [ ] 3.3 實作 Drools 規則命令
    - 建立 DroolsRuleCommand 類別
    - 整合 Drools 規則引擎執行邏輯
    - 實作規則熱更新機制
    - _需求: 4.1, 4.3, 4.4_

  - [ ] 3.4 實作外部系統和資料庫整合命令
    - 建立 ExternalSystemCommand 和 DatabaseQueryCommand
    - 實作外部系統適配器介面
    - 加入錯誤處理和降級策略
    - _需求: 3.4, 3.5, 2.6_

- [ ] 4. 實作策略模式和狀態模式
  - [ ] 4.1 建立計算策略模式
    - 定義 CalculationStrategy 介面
    - 實作 PercentageDiscountStrategy、TieredDiscountStrategy、FixedAmountStrategy
    - 建立 CalculationStrategyFactory 工廠類別
    - _需求: 9.2, 9.3, 5.4_

  - [ ] 4.2 實作優惠狀態模式
    - 建立 PromotionState 抽象類別和具體狀態類別
    - 實作狀態轉換邏輯和狀態管理
    - 加入狀態變更事件通知
    - _需求: 9.1, 9.5_

- [ ] 5. 實作 CQRS 模式和應用層
  - [ ] 5.1 建立命令端處理器
    - 實作 CreateDecisionTreeCommand 和對應的 CommandHandler
    - 實作 UpdatePromotionRuleCommand 和對應的 CommandHandler
    - 實作 EvaluatePromotionCommand 和對應的 CommandHandler
    - _需求: 8.1, 8.2, 1.1_

  - [ ] 5.2 建立查詢端處理器
    - 實作 GetPromotionHistoryQuery 和對應的 QueryHandler
    - 實作 GetAvailablePromotionsQuery 和對應的 QueryHandler
    - 建立讀取模型視圖類別
    - _需求: 8.1, 8.3, 8.4_

  - [ ] 5.3 實作應用服務層
    - 建立 PromotionApplicationService 協調命令和查詢
    - 實作事務管理和錯誤處理
    - 加入效能監控和日誌記錄
    - _需求: 6.2, 1.5, 8.5_

- [ ] 6. 實作資料存取層和基礎設施
  - [ ] 6.1 建立資料庫 Schema 和 JPA 實體
    - 建立資料庫表結構 (decision_trees, decision_nodes, promotion_rules, promotion_history)
    - 實作 JPA 實體類別和對應關係
    - 配置多環境資料庫連線 (H2/PostgreSQL)
    - _需求: 2.1, 2.2, 6.1_

  - [ ] 6.2 實作 Repository 模式
    - 建立 Repository 介面和 JPA 實作
    - 實作自定義查詢方法和規格模式
    - 加入資料存取異常處理
    - _需求: 6.1, 6.2, 7.5_

  - [ ] 6.3 實作快取和效能優化
    - 整合 Spring Cache 快取決策樹和規則配置
    - 實作非同步處理優惠歷史記錄
    - 加入資料庫連線池配置
    - _需求: 1.4, 8.5_

- [ ] 7. 實作 REST API 和控制器層
  - [ ] 7.1 建立優惠評估 API
    - 實作 POST /api/v1/promotions/evaluate 端點
    - 加入請求資料驗證和錯誤處理
    - 實作 API 回應格式標準化
    - _需求: 1.1, 1.2, 1.3_

  - [ ] 7.2 建立管理 API
    - 實作決策樹配置管理 API 端點
    - 實作優惠規則管理 API 端點
    - 加入 API 安全性和權限控制
    - _需求: 2.2, 4.4, 6.1_

  - [ ] 7.3 實作查詢 API
    - 實作優惠歷史查詢 API 端點
    - 實作可用優惠查詢 API 端點
    - 加入分頁和排序功能
    - _需求: 5.4, 5.5, 8.3_

- [ ] 8. 撰寫 BDD 測試場景和 Gherkin 規格
  - [ ] 8.1 建立 Cucumber 測試框架
    - 配置 Cucumber 測試環境和 Spring Boot 整合
    - 建立測試資料初始化和清理機制
    - 實作測試用的 Mock 外部系統
    - _需求: 10.1, 10.2, 6.5_

  - [ ] 8.2 撰寫優惠評估 BDD 場景
    - 撰寫高價值客戶 VIP 優惠場景
    - 撰寫一般客戶基礎優惠場景
    - 撰寫不符合條件客戶的反向場景
    - _需求: 10.2, 5.1, 5.3_

  - [ ] 8.3 撰寫決策樹配置 BDD 場景
    - 撰寫決策樹建立和更新場景
    - 撰寫規則配置和熱更新場景
    - 撰寫錯誤處理和異常場景
    - _需求: 10.2, 2.2, 4.4_

- [ ]* 9. 撰寫單元測試和整合測試
  - [ ]* 9.1 領域模型單元測試
    - 撰寫聚合根和實體的單元測試
    - 撰寫值物件和領域服務的單元測試
    - 使用 Mockito 模擬外部依賴
    - _需求: 10.3, 10.4, 7.5_

  - [ ]* 9.2 命令和查詢處理器測試
    - 撰寫 CommandHandler 和 QueryHandler 單元測試
    - 撰寫 CQRS 模式的整合測試
    - 驗證事務處理和錯誤處理邏輯
    - _需求: 10.3, 10.4, 8.1_

  - [ ]* 9.3 API 控制器整合測試
    - 撰寫 REST API 端點的整合測試
    - 使用 @SpringBootTest 測試完整流程
    - 驗證 API 安全性和錯誤處理
    - _需求: 10.3, 10.4, 1.1_

- [ ] 10. 系統整合和部署準備
  - [ ] 10.1 建立 Docker 容器化配置
    - 建立 Dockerfile 和 docker-compose.yml
    - 配置多環境容器部署腳本
    - 整合 PostgreSQL 容器配置
    - _需求: 6.1, 2.1_

  - [ ] 10.2 實作監控和日誌系統
    - 整合 Spring Boot Actuator 健康檢查
    - 配置結構化日誌輸出格式
    - 實作效能指標收集和監控
    - _需求: 1.5, 1.4_

  - [ ] 10.3 建立部署和 CI/CD 流程
    - 建立 Gradle 建置和測試腳本
    - 配置自動化測試執行流程
    - 建立環境部署和驗證腳本
    - _需求: 10.5, 6.1_