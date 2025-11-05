# 銀行客戶優惠系統 (Bank Customer Promotion System)

基於Spring Boot 3的銀行客戶優惠推薦系統，採用六角形架構和領域驅動設計。

## 專案結構

```
src/
├── main/
│   ├── java/com/bank/promotion/
│   │   ├── PromotionSystemApplication.java     # 主應用程式
│   │   ├── domain/                             # 領域層
│   │   ├── application/                        # 應用層
│   │   ├── infrastructure/                     # 基礎設施層
│   │   │   └── config/                        # 配置類別
│   │   └── presentation/                       # 展示層
│   └── resources/
│       ├── application*.yml                    # 多環境配置
│       ├── schema.sql                          # 資料庫結構
│       ├── data.sql                           # 初始資料
│       └── rules/                             # Drools規則檔案
└── test/
    ├── java/                                  # 測試程式碼
    └── resources/                             # 測試資源
```

## 技術棧

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 Database** (開發環境)
- **PostgreSQL** (生產環境)
- **Drools Rules Engine 8.44.0**
- **Gradle 8.5**
- **JUnit 5 & Mockito** (測試)
- **Cucumber** (BDD測試)

## 環境配置

### 開發環境 (dev)
- 使用 H2 記憶體資料庫
- 啟用 H2 控制台: http://localhost:8080/h2-console
- 詳細日誌輸出

### SIT/UAT/生產環境
- 使用 PostgreSQL 資料庫
- 連線池配置
- 快取配置 (Caffeine)
- 結構化日誌

## 快速開始

### 1. 建置專案
```bash
./gradlew build
```

### 2. 執行測試
```bash
./gradlew test
```

### 3. 啟動應用程式 (開發環境)
```bash
./gradlew bootRun
```

### 4. 切換環境
```bash
# SIT環境
./gradlew bootRun --args='--spring.profiles.active=sit'

# UAT環境
./gradlew bootRun --args='--spring.profiles.active=uat'

# 生產環境
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## 資料庫配置

### H2 (開發環境)
- URL: `jdbc:h2:mem:testdb`
- 控制台: http://localhost:8080/h2-console
- 使用者名稱: `sa`
- 密碼: (空白)

### PostgreSQL (其他環境)
環境變數配置:
- `DB_HOST`: 資料庫主機
- `DB_PORT`: 資料庫埠號
- `DB_NAME`: 資料庫名稱
- `DB_USERNAME`: 使用者名稱
- `DB_PASSWORD`: 密碼

## 健康檢查

應用程式啟動後可透過以下端點檢查狀態:
- 健康檢查: http://localhost:8080/actuator/health
- 應用資訊: http://localhost:8080/actuator/info
- 效能指標: http://localhost:8080/actuator/metrics

## 開發指南

本專案遵循:
- **六角形架構** (Hexagonal Architecture)
- **領域驅動設計** (Domain Driven Design)
- **CQRS模式** (Command Query Responsibility Segregation)
- **測試驅動開發** (Test Driven Development)

## 下一步

專案結構已建立完成，可以開始實作:
1. 領域模型和聚合根
2. Command Pattern 和節點命令
3. 策略模式和狀態模式
4. CQRS 模式和應用層
5. 資料存取層和基礎設施
6. REST API 和控制器層
7. BDD 測試場景

詳細實作計劃請參考 `.kiro/specs/bank-customer-promotion-system/tasks.md`