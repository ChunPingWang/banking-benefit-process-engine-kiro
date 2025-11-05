# language: zh-TW
功能: Command Pattern 執行測試

  背景:
    假設 系統已初始化完成
    而且 測試資料已準備就緒

  場景: SpEL 條件命令成功執行
    假設 系統配置了 SpEL 條件命令 "#{creditScore > 700}"
    而且 客戶信用評分為 750
    當 執行 SpEL 條件命令
    那麼 命令應該成功執行
    而且 返回結果應該為 true
    而且 命令類型應該為 "SPEL_CONDITION"

  場景: SpEL 條件命令條件不符合
    假設 系統配置了 SpEL 條件命令 "#{creditScore > 700}"
    而且 客戶信用評分為 650
    當 執行 SpEL 條件命令
    那麼 命令應該成功執行
    而且 返回結果應該為 false

  場景: SpEL 計算命令執行優惠計算
    假設 系統配置了 SpEL 計算命令 "#{accountBalance * 0.05}"
    而且 客戶帳戶餘額為 500000 元
    當 執行 SpEL 計算命令
    那麼 命令應該成功執行
    而且 返回優惠結果應該包含折扣金額 25000 元
    而且 優惠名稱應該為 "SpEL計算優惠"

  場景: SpEL 計算命令使用配置參數
    假設 系統配置了 SpEL 計算命令 "#{accountBalance * param_discountRate}"
    而且 配置參數 "discountRate" 為 0.03
    而且 配置參數 "promotionName" 為 "自定義優惠"
    而且 客戶帳戶餘額為 500000 元
    當 執行 SpEL 計算命令
    那麼 命令應該成功執行
    而且 返回優惠結果應該包含折扣金額 15000 元
    而且 優惠名稱應該為 "自定義優惠"

  場景: SpEL 命令處理無效表達式
    假設 系統配置了 SpEL 條件命令 "#{invalidProperty}"
    而且 客戶信用評分為 750
    當 執行 SpEL 條件命令
    那麼 命令應該執行失敗
    而且 錯誤訊息應該包含 "SpEL condition evaluation failed"

  場景: Drools 規則命令執行高價值客戶判斷
    假設 系統配置了 Drools 規則命令 "高價值客戶判斷規則"
    而且 Drools 規則內容為:
      """
      rule "HighValueCustomer"
      when
          $customer : CustomerPayload(annualIncome > 1500000, creditScore > 700)
      then
          results.put("conditionResult", true);
          results.put("customerLevel", "HIGH_VALUE");
      end
      """
    而且 客戶年收入為 2000000 元
    而且 客戶信用評分為 750
    當 執行 Drools 規則命令
    那麼 命令應該成功執行
    而且 返回結果應該為 true

  場景: Drools 規則命令計算優惠金額
    假設 系統配置了 Drools 計算命令 "優惠金額計算規則"
    而且 Drools 規則內容為:
      """
      rule "CalculateDiscount"
      when
          $customer : CustomerPayload(accountBalance > 100000)
      then
          BigDecimal discount = $customer.getAccountBalance().multiply(new BigDecimal("0.02"));
          results.put("calculationResult", discount);
      end
      """
    而且 客戶帳戶餘額為 500000 元
    當 執行 Drools 計算命令
    那麼 命令應該成功執行
    而且 返回優惠結果應該包含折扣金額 10000 元

  場景: 外部系統命令成功呼叫
    假設 系統配置了外部系統條件命令呼叫信用評估服務
    而且 外部系統端點為 "http://credit-service/evaluate"
    而且 外部信用評估服務返回結果:
      | conditionResult | true |
      | creditLevel     | HIGH |
    當 執行外部系統命令
    那麼 命令應該成功執行
    而且 返回結果應該為 true

  場景: 外部系統命令計算優惠
    假設 系統配置了外部系統計算命令呼叫優惠計算服務
    而且 外部系統端點為 "http://promotion-service/calculate"
    而且 外部優惠計算服務返回結果:
      | discountAmount | 5000.0           |
      | promotionName  | 外部系統優惠     |
      | promotionType  | EXTERNAL_CALCULATED |
    當 執行外部系統命令
    那麼 命令應該成功執行
    而且 返回優惠結果應該包含折扣金額 5000 元
    而且 優惠名稱應該為 "外部系統優惠"

  場景: 外部系統命令降級處理
    假設 系統配置了外部系統條件命令呼叫信用評估服務
    而且 外部系統端點為 "http://credit-service/evaluate"
    而且 外部信用評估服務不可用
    而且 系統啟用降級策略
    而且 降級條件值設定為 false
    當 執行外部系統命令
    那麼 命令應該執行降級邏輯
    而且 返回結果應該為 false
    而且 系統應該記錄降級事件

  場景: 外部系統命令計算降級處理
    假設 系統配置了外部系統計算命令呼叫優惠計算服務
    而且 外部系統端點為 "http://promotion-service/calculate"
    而且 外部優惠計算服務不可用
    而且 系統啟用降級策略
    而且 降級折扣金額設定為 1000 元
    而且 降級優惠名稱設定為 "降級優惠"
    當 執行外部系統命令
    那麼 命令應該執行降級邏輯
    而且 返回優惠結果應該包含折扣金額 1000 元
    而且 優惠名稱應該為 "降級優惠"
    而且 優惠類型應該為 "FALLBACK"

  場景: 外部系統命令超時處理
    假設 系統配置了外部系統條件命令呼叫信用評估服務
    而且 外部系統端點為 "http://credit-service/evaluate"
    而且 命令超時設定為 5 秒
    而且 外部信用評估服務回應時間為 10 秒
    而且 系統啟用降級策略
    當 執行外部系統命令
    那麼 命令應該執行降級邏輯
    而且 系統應該記錄超時事件

  場景: 資料庫查詢命令成功執行
    假設 系統配置了資料庫查詢條件命令
    而且 資料庫連線字串為 "jdbc:h2:mem:testdb"
    而且 查詢模板為 "SELECT COUNT(*) as resultCount FROM customer_history WHERE customer_id = #{customerId}"
    而且 資料庫查詢返回結果:
      | resultCount | 5 |
    當 執行資料庫查詢命令
    那麼 命令應該成功執行
    而且 返回結果應該為 true

  場景: 資料庫查詢命令計算優惠
    假設 系統配置了資料庫查詢計算命令
    而且 資料庫連線字串為 "jdbc:h2:mem:testdb"
    而且 查詢模板為 "SELECT #{accountBalance} * 0.03 as discountAmount"
    而且 資料庫查詢返回結果:
      | discountAmount | 15000.0        |
      | promotionName  | 資料庫查詢優惠 |
    當 執行資料庫查詢命令
    那麼 命令應該成功執行
    而且 返回優惠結果應該包含折扣金額 15000 元
    而且 優惠名稱應該為 "資料庫查詢優惠"

  場景: 資料庫查詢命令錯誤處理
    假設 系統配置了資料庫查詢條件命令
    而且 資料庫連線字串為 "jdbc:h2:mem:testdb"
    而且 查詢模板為 "SELECT * FROM non_existent_table"
    當 執行資料庫查詢命令
    那麼 命令應該執行失敗
    而且 錯誤訊息應該包含 "Database query execution failed"

  場景: 命令工廠創建不同類型的命令
    假設 命令工廠已初始化
    當 使用工廠創建 "SPEL_CONDITION" 類型的命令
    那麼 應該成功創建 SpEL 條件命令
    當 使用工廠創建 "SPEL_CALCULATION" 類型的命令
    那麼 應該成功創建 SpEL 計算命令
    當 使用工廠創建 "DROOLS_CONDITION" 類型的命令
    那麼 應該成功創建 Drools 規則命令
    當 使用工廠創建 "EXTERNAL_SYSTEM_CONDITION" 類型的命令
    那麼 應該成功創建外部系統命令
    當 使用工廠創建 "DATABASE_QUERY_CONDITION" 類型的命令
    那麼 應該成功創建資料庫查詢命令

  場景: 命令工廠處理不支援的命令類型
    假設 命令工廠已初始化
    當 使用工廠創建 "UNSUPPORTED_COMMAND" 類型的命令
    那麼 應該拋出 "Unsupported command type" 異常

  場景: 命令註冊器動態註冊新命令
    假設 命令註冊器已初始化
    而且 自定義命令類型為 "CUSTOM_COMMAND"
    當 註冊自定義命令到註冊器
    那麼 註冊器應該包含該自定義命令
    而且 可以使用註冊器創建自定義命令實例

  場景: 命令執行上下文資料傳遞
    假設 系統配置了 SpEL 條件命令 "#{contextValue > threshold}"
    而且 執行上下文包含資料:
      | contextValue | 150 |
      | threshold    | 100 |
    當 執行 SpEL 條件命令
    那麼 命令應該成功執行
    而且 返回結果應該為 true

  場景: 命令執行效能監控
    假設 系統配置了外部系統條件命令
    而且 外部系統模擬延遲 100 毫秒
    當 執行外部系統命令
    那麼 命令應該成功執行
    而且 執行時間應該大於等於 100 毫秒
    而且 系統應該記錄執行時間