# 實作計劃

## 開發方式說明
本專案採用**測試先行 (Test-First Development)** 的開發方式：
1. **第一階段**：完成 Task 3 - 撰寫完整的 BDD 測試場景和 Gherkin 規格
2. **Review 階段**：所有 Gherkin 規格必須經過 review 和確認
3. **實作階段**：基於已確認的測試規格進行系統開發 (Task 4 onwards)
4. **測試要求**：每個主要功能模組 (Task) 完成後，必須撰寫對應的 BDD 測試場景和單元測試
5. **Mock 策略**：對於尚未開發完成的外部介接，先撰寫 Mock 實作，使用 Dependency Injection 進行單元測試
6. **測試覆蓋**：所有錯誤處理和降級策略都必須有對應的測試案例
7. **版本控制**：每個主要任務 (Task) 完成後，必須使用中文提交訊息進行 Git commit
8. **測試驗證**：每次 commit 前必須確保所有測試通過，包括單元測試和 BDD 測試

## 稽核需求
系統必須記錄每次外部請求的完整處理軌跡：
- 記錄每個決策節點的執行結果
- 追蹤所有外部系統呼叫
- 保存完整的請求和回應資料
- 提供稽核查詢和報告功能

- [x] 1. 建立專案結構和核心配置
  - 建立 Spring Boot 3 專案結構，配置 Gradle 建置檔案
  - 設定多環境配置檔案 (dev/sit/uat/prod)
  - 配置 H2 和 PostgreSQL 資料庫連線
  - 整合 Drools 規則引擎依賴
  - _需求: 1.2, 2.1, 6.1_
  - _已提交: "建立專案結構和核心配置"_

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

  - [x] 2.4 Task 2 完成提交
    - _已提交: "完成領域模型和聚合根實作"_

- [x] 3. 撰寫 BDD 測試場景和 Gherkin 規格 (測試先行開發)
  - [x] 3.1 建立 Cucumber 測試框架和稽核機制
    - 配置 Cucumber 測試環境和 Spring Boot 整合
    - 建立測試資料初始化和清理機制
    - 實作測試用的 Mock 外部系統
    - 設計稽核追蹤機制和資料結構
    - _需求: 10.1, 10.2, 6.5, 稽核需求_

  - [x] 3.2 撰寫優惠評估 BDD 場景 (含稽核追蹤)
    - 撰寫高價值客戶 VIP 優惠場景 (包含每步驟稽核記錄)
    - 撰寫一般客戶基礎優惠場景 (包含決策路徑追蹤)
    - 撰寫不符合條件客戶的反向場景 (包含拒絕原因記錄)
    - 撰寫稽核軌跡查詢和報告場景
    - _需求: 10.2, 5.1, 5.3, 稽核需求_

  - [x] 3.3 撰寫決策樹配置和管理 BDD 場景
    - 撰寫決策樹建立和更新場景 (包含變更稽核)
    - 撰寫規則配置和熱更新場景 (包含版本控制)
    - 撰寫錯誤處理和異常場景 (包含錯誤追蹤)
    - 撰寫系統管理和監控場景
    - _需求: 10.2, 2.2, 4.4, 稽核需求_

  - [x] 3.4 撰寫稽核和合規性 BDD 場景
    - 撰寫完整請求生命週期稽核場景
    - 撰寫稽核資料查詢和報告場景
    - 撰寫合規性檢查和驗證場景
    - 撰寫資料保留和清理場景
    - _需求: 稽核需求, 合規性需求_

  - [x] 3.5 Task 3 完成提交
    - _已提交: "完成 BDD 測試場景和 Gherkin 規格撰寫"_

- [x] 4. 實作 Command Pattern 和節點命令
  - [x] 4.1 建立命令介面和基礎架構
    - 定義 NodeCommand 介面和 ExecutionContext
    - 實作命令工廠和命令註冊機制
    - 建立命令執行結果 NodeResult 類別
    - _需求: 3.1, 3.2, 3.6_

  - [x] 4.2 實作 SpEL 表達式命令
    - 建立 SpELConditionCommand 和 SpELCalculationCommand
    - 整合 Spring Expression Language 解析器
    - 實作表達式快取機制
    - _需求: 2.3, 2.5, 3.1_

  - [x] 4.3 實作 Drools 規則命令
    - 建立 DroolsRuleCommand 類別
    - 整合 Drools 規則引擎執行邏輯
    - 實作規則熱更新機制
    - _需求: 4.1, 4.3, 4.4_

  - [x] 4.4 實作外部系統和資料庫整合命令
    - 建立 ExternalSystemCommand 和 DatabaseQueryCommand
    - 實作外部系統適配器介面
    - 加入錯誤處理和降級策略
    - _需求: 3.4, 3.5, 2.6_

  - [x] 4.5 Command Pattern 測試實作
    - 建立 Mock 外部系統適配器用於測試
    - 實作 Command Pattern 單元測試
    - 撰寫 BDD 測試場景驗證命令執行
    - 測試錯誤處理和降級策略
    - _需求: 6.1, 6.2, 6.3_

  - [x] 4.6 Task 4 完成提交
    - _已提交: "完成 Command Pattern 和節點命令實作及測試"_

- [x] 5. 實作策略模式和狀態模式
  - [x] 5.1 建立計算策略模式
    - 定義 CalculationStrategy 介面
    - 實作 PercentageDiscountStrategy、TieredDiscountStrategy、FixedAmountStrategy
    - 建立 CalculationStrategyFactory 工廠類別
    - _需求: 9.2, 9.3, 5.4_

  - [x] 5.2 實作優惠狀態模式
    - 建立 PromotionState 抽象類別和具體狀態類別
    - 實作狀態轉換邏輯和狀態管理
    - 加入狀態變更事件通知
    - _需求: 9.1, 9.5_

  - [x] 5.3 策略模式和狀態模式測試實作
    - 撰寫策略模式單元測試 (CalculationStrategy 各實作類別)
    - 撰寫狀態模式單元測試 (PromotionState 狀態轉換)
    - 撰寫 BDD 測試場景驗證策略選擇和狀態變更
    - 建立 Mock 策略和狀態用於測試
    - _需求: 10.3, 10.4, 9.1_

  - [x] 5.4 Task 5 完成提交
    - 執行所有相關測試確保通過
    - Git commit with message: "完成策略模式和狀態模式實作及測試"
    - 確認程式碼品質和測試覆蓋率
    - _需求: 版本控制, 測試驗證_

- [x] 6. 實作 CQRS 模式和應用層
  - [x] 6.1 建立命令端處理器
    - 實作 CreateDecisionTreeCommand 和對應的 CommandHandler
    - 實作 UpdatePromotionRuleCommand 和對應的 CommandHandler
    - 實作 EvaluatePromotionCommand 和對應的 CommandHandler
    - _需求: 8.1, 8.2, 1.1_

  - [x] 6.2 建立查詢端處理器
    - 實作 GetPromotionHistoryQuery 和對應的 QueryHandler
    - 實作 GetAvailablePromotionsQuery 和對應的 QueryHandler
    - 建立讀取模型視圖類別
    - _需求: 8.1, 8.3, 8.4_

  - [x] 6.3 實作應用服務層 (含稽核服務)
    - 建立 PromotionApplicationService 協調命令和查詢
    - 實作 AuditService 記錄每個處理步驟
    - 實作事務管理和錯誤處理 (含稽核事務)
    - 加入效能監控和日誌記錄
    - 實作稽核資料查詢和報告服務
    - _需求: 6.2, 1.5, 8.5, 稽核需求_

  - [x] 6.4 CQRS 模式和應用層測試實作
    - 撰寫 CommandHandler 和 QueryHandler 單元測試
    - 撰寫 ApplicationService 整合測試
    - 撰寫 CQRS 模式的端到端測試
    - 驗證事務處理和錯誤處理邏輯
    - 測試稽核服務的記錄功能
    - _需求: 10.3, 10.4, 8.1_

  - [x] 6.5 Task 6 完成提交
    - 執行所有相關測試確保通過
    - Git commit with message: "完成 CQRS 模式和應用層實作及測試"
    - 確認程式碼品質和測試覆蓋率
    - _需求: 版本控制, 測試驗證_

- [x] 7. 實作資料存取層和適配器層
  - [x] 7.1 建立資料庫 Schema 和 JPA 實體 (含稽核資料表)
    - 建立核心業務資料表結構 (decision_trees, decision_nodes, promotion_rules, promotion_history)
    - 建立稽核追蹤資料表 (audit_trails, request_logs, decision_steps, system_events)
    - 實作 JPA 實體類別和對應關係 (包含稽核實體)
    - 配置多環境資料庫連線 (H2/PostgreSQL)
    - 實作稽核資料自動記錄機制 (AOP/Event Listener)
    - _需求: 2.1, 2.2, 6.1, 稽核需求_

  - [x] 7.2 實作 Repository 模式
    - 建立 Repository 介面和 JPA 實作
    - 實作自定義查詢方法和規格模式
    - 加入資料存取異常處理
    - _需求: 6.1, 6.2, 7.5_

  - [x] 7.3 實作快取和效能優化
    - 整合 Spring Cache 快取決策樹和規則配置
    - 實作非同步處理優惠歷史記錄
    - 加入資料庫連線池配置
    - _需求: 1.4, 8.5_

  - [x] 7.4 資料存取層和適配器層測試實作
    - 撰寫 Repository 介面單元測試
    - 撰寫 JPA 實體關係測試
    - 撰寫資料庫整合測試 (H2/PostgreSQL)
    - 測試快取機制和效能優化
    - 驗證稽核資料自動記錄功能
    - _需求: 10.3, 10.4, 7.5_

  - [x] 7.5 Task 7 完成提交
    - 執行所有相關測試確保通過
    - Git commit with message: "完成資料存取層和適配器層實作及測試"
    - 確認程式碼品質和測試覆蓋率
    - _需求: 版本控制, 測試驗證_

- [x] 8. 實作 REST API 和控制器層
  - [x] 8.1 建立優惠評估 API
    - 實作 POST /api/v1/promotions/evaluate 端點
    - 加入請求資料驗證和錯誤處理
    - 實作 API 回應格式標準化
    - _需求: 1.1, 1.2, 1.3_

  - [x] 8.2 建立管理 API
    - 實作決策樹配置管理 API 端點
    - 實作優惠規則管理 API 端點
    - 加入 API 安全性和權限控制
    - _需求: 2.2, 4.4, 6.1_

  - [x] 8.3 實作查詢 API (含稽核查詢)
    - 實作優惠歷史查詢 API 端點
    - 實作可用優惠查詢 API 端點
    - 實作稽核軌跡查詢 API 端點 (GET /api/v1/audit/trails)
    - 實作決策步驟追蹤 API 端點 (GET /api/v1/audit/decisions)
    - 加入分頁和排序功能 (含稽核資料)
    - _需求: 5.4, 5.5, 8.3, 稽核需求_

  - [x] 8.4 REST API 和控制器層測試實作
    - 撰寫 REST API 端點整合測試
    - 使用 @SpringBootTest 測試完整流程
    - 驗證 API 安全性和錯誤處理
    - 測試請求驗證和回應格式
    - 驗證稽核 API 的查詢功能
    - _需求: 10.3, 10.4, 1.1_

  - [x] 8.5 Task 8 完成提交
    - 執行所有相關測試確保通過
    - Git commit with message: "完成 REST API 和控制器層實作及測試"
    - 確認程式碼品質和測試覆蓋率
    - _需求: 版本控制, 測試驗證_

- [x] 8.6 修復和完善所有測試案例
  - [x] 8.6.1 修復 BDD 測試和 Cucumber 整合問題
    - 解決 Spring 上下文載入問題和 Bean 依賴注入錯誤
    - 修復 CucumberTestRunner 中的 NoSuchBeanDefinitionException
    - 確保所有 BDD 測試場景能正常執行
    - 完善測試資料管理和 Mock 服務配置
    - _需求: 10.1, 10.2, 測試完整性_

  - [x] 8.6.2 修復控制器安全性測試
    - 修正安全性測試中的權限驗證問題
    - 確保 @PreAuthorize 註解在測試中正確運作
    - 修復預期 401/403 但實際返回 200 的測試案例
    - 完善測試安全配置和角色權限測試
    - _需求: 6.1, 6.2, API 安全性_

  - [x] 8.6.3 完善缺失的測試實作
    - 補充缺失的 Command Handler 和 Query Handler 實作
    - 實作缺失的 Repository 和 Service 層組件
    - 確保所有 Mock 依賴都有對應的實際實作
    - 完善測試覆蓋率和邊界條件測試
    - _需求: 10.3, 10.4, 程式碼完整性_

  - [x] 8.6.4 驗證所有測試通過
    - 執行完整測試套件確保 100% 通過率
    - 驗證單元測試、整合測試和 BDD 測試
    - 確認測試覆蓋率達到要求標準
    - 修復任何剩餘的測試失敗問題
    - _需求: 測試驗證, 品質保證_

  - [x] 8.6.5 Task 8.6 完成提交
    - 執行所有測試確保全部通過
    - Git commit with message: "修復和完善所有測試案例，確保測試套件完整通過"
    - 確認系統穩定性和測試可靠性
    - _需求: 版本控制, 測試驗證_

- [x] 9. 系統整合和部署準備
  - [x] 9.1 整合 Swagger API 文檔
    - 加入 SpringDoc OpenAPI 依賴到 build.gradle
    - 建立 Swagger 配置類別和自定義配置
    - 為所有 REST Controller 加入 OpenAPI 註解
    - 配置 API 分組、標籤和描述資訊
    - 實作自定義錯誤回應的 API 文檔
    - 配置多環境下的 Swagger UI 存取
    - 加入 API 安全性文檔和範例資料
    - _需求: 12.1, 12.2, 12.3, 12.4, 12.5_

  - [ ] 9.2 建立 Docker 容器化配置
    - 建立 Dockerfile 和 docker-compose.yml
    - 配置多環境容器部署腳本
    - 整合 PostgreSQL 容器配置
    - _需求: 6.1, 2.1_

  - [ ] 9.3 實作監控和日誌系統
    - 整合 Spring Boot Actuator 健康檢查
    - 配置結構化日誌輸出格式
    - 實作效能指標收集和監控
    - _需求: 1.5, 1.4_

  - [ ] 9.4 建立部署和 CI/CD 流程
    - 建立 Gradle 建置和測試腳本
    - 配置自動化測試執行流程
    - 建立環境部署和驗證腳本
    - _需求: 10.5, 6.1_

  - [ ] 9.5 系統整合測試和驗收測試
    - 執行完整的端到端整合測試
    - 驗證所有 BDD 場景通過
    - 確認系統效能和穩定性
    - 產生測試覆蓋率報告
    - _需求: 10.3, 10.4, 10.5_

  - [ ] 9.6 Task 9 完成提交和專案交付
    - 執行所有測試確保通過
    - Git commit with message: "完成系統整合和部署準備"
    - 產生專案交付文件
    - 確認專案完整性和品質
    - _需求: 版本控制, 測試驗證_