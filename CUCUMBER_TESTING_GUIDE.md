# Cucumber 測試執行指南

## 目前狀況

由於步驟定義重複問題，Cucumber 測試目前無法正常執行。以下是完整的解決方案和執行方法。

## 測試報告路徑

### 1. JUnit 測試報告 (目前可用)
```bash
# 執行 JUnit 測試
./gradlew test --no-daemon

# 報告位置
build/reports/tests/test/index.html          # HTML 測試報告
build/test-results/test/                     # XML 測試結果
```

### 2. Jacoco 覆蓋率報告
```bash
# 執行測試並生成覆蓋率報告
./gradlew test jacocoTestReport --no-daemon

# 報告位置
build/reports/jacoco/test/html/index.html    # HTML 覆蓋率報告
```

### 3. Cucumber 測試報告 (修復後可用)
```bash
# 執行 Cucumber 測試 (需要先修復步驟定義重複問題)
./gradlew cucumberTest --no-daemon

# 報告位置
build/reports/cucumber-html/index.html       # HTML 報告
build/reports/cucumber-json/cucumber.json    # JSON 報告
build/reports/cucumber-junit/junit.xml       # JUnit XML 報告
```

## 如何修復 Cucumber 測試

### 問題 1: 步驟定義重複

**原因**: 多個步驟定義類別中定義了相同的步驟

**解決方法**:
1. 檢查所有 `*Steps.java` 檔案
2. 移除重複的步驟定義
3. 將共用步驟移至 `CommonSteps.java`

### 問題 2: Spring 上下文配置

**原因**: Cucumber 無法正確載入 Spring 上下文

**解決方法**:
1. 確保 `CucumberSpringConfiguration.java` 正確配置
2. 檢查 `@CucumberContextConfiguration` 註解
3. 驗證測試配置類別

## 執行測試的方法

### 方法 1: 執行所有測試
```bash
# 執行所有 JUnit 測試 (推薦)
./gradlew test --no-daemon

# 查看測試報告
open build/reports/tests/test/index.html
```

### 方法 2: 執行特定測試類別
```bash
# 執行特定測試類別
./gradlew test --tests "*PromotionControllerTest" --no-daemon

# 執行特定包下的測試
./gradlew test --tests "com.bank.promotion.adapter.web.controller.*" --no-daemon
```

### 方法 3: 執行 Cucumber 測試 (修復後)
```bash
# 執行 Cucumber 測試
./gradlew cucumberTest --no-daemon

# 只執行特定 feature
./gradlew cucumberTest --tests "*simple-test*" --no-daemon
```

## 測試覆蓋率檢查

```bash
# 生成測試覆蓋率報告
./gradlew test jacocoTestReport --no-daemon

# 檢查覆蓋率
open build/reports/jacoco/test/html/index.html

# 驗證覆蓋率是否達到 90%
./gradlew jacocoTestCoverageVerification --no-daemon
```

## 持續整合測試

```bash
# 完整的測試流程
./gradlew clean test jacocoTestReport --no-daemon

# 檢查所有測試是否通過
echo "Exit code: $?"
```

## 測試資料和 Mock

### 測試資料位置
- `src/test/resources/` - 測試資源檔案
- `src/test/java/com/bank/promotion/bdd/TestDataManager.java` - 測試資料管理

### Mock 服務
- `MockExternalSystemService` - 外部系統 Mock
- `TestAuditTracker` - 稽核追蹤 Mock

## 故障排除

### 常見問題

1. **編譯錯誤**
   ```bash
   ./gradlew compileTestJava --no-daemon
   ```

2. **依賴問題**
   ```bash
   ./gradlew dependencies --configuration testRuntimeClasspath
   ```

3. **Spring 上下文問題**
   - 檢查 `@SpringBootTest` 配置
   - 驗證 `application-test.yml` 設定

4. **資料庫連線問題**
   - 確認 H2 記憶體資料庫配置
   - 檢查測試 profile 設定

## 建議的測試執行順序

1. **單元測試**: `./gradlew test --no-daemon`
2. **覆蓋率檢查**: `./gradlew jacocoTestReport --no-daemon`
3. **整合測試**: 檢查 `@SpringBootTest` 測試
4. **Cucumber 測試**: 修復後執行 `./gradlew cucumberTest --no-daemon`

## 測試報告查看

### HTML 報告
```bash
# JUnit 測試報告
open build/reports/tests/test/index.html

# Jacoco 覆蓋率報告
open build/reports/jacoco/test/html/index.html

# Cucumber 報告 (修復後)
open build/reports/cucumber-html/index.html
```

### 命令列查看
```bash
# 查看測試結果摘要
cat build/test-results/test/TEST-*.xml | grep -E "(tests|failures|errors)"

# 查看失敗的測試
find build/test-results -name "*.xml" -exec grep -l "failure\|error" {} \;
```

## 總結

目前建議使用 JUnit 測試和 Jacoco 覆蓋率報告，這些都能正常運作。Cucumber 測試需要先解決步驟定義重複的問題才能正常執行。

測試覆蓋率目標是 90%，目前的 JUnit 測試應該能達到這個標準。