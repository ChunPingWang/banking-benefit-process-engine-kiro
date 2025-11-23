# 銀行客戶優惠推薦系統

基於決策樹的智能優惠推薦系統，採用領域驅動設計(DDD)和六角形架構，整合 Drools 規則引擎和 SpEL 表達式語言，提供靈活的優惠推薦服務。

## 目錄

- [系統概述](#系統概述)
- [技術架構](#技術架構)
- [系統架構圖](#系統架構圖)
- [優惠評估流程](#優惠評估流程)
- [條件因子與計算因子](#條件因子與計算因子)
- [資料庫設計](#資料庫設計)
- [API 文檔](#api-文檔)
- [完整 API 清單](#完整-api-清單)
- [快速開始](#快速開始)
- [API 使用範例](#api-使用範例)
- [開發指南](#開發指南)

## 系統概述

銀行客戶優惠推薦系統是一個企業級的優惠評估平台，具備以下核心功能：

- **智能優惠評估**: 基於決策樹和規則引擎的客戶優惠推薦
- **靈活規則配置**: 支援 SpEL 表達式和 Drools 規則的動態配置
- **完整稽核追蹤**: 記錄每次請求的完整處理軌跡
- **多層次安全**: 基於角色的 API 存取控制
- **高效能設計**: 快取機制和非同步處理優化

### 核心特性

- ✅ **六角形架構**: 清晰的領域邊界和依賴反轉
- ✅ **CQRS 模式**: 讀寫分離優化效能
- ✅ **Command Pattern**: 可擴展的節點操作封裝
- ✅ **Strategy Pattern**: 靈活的優惠計算策略
- ✅ **State Pattern**: 優惠狀態管理
- ✅ **完整測試**: BDD 測試和 90%+ 覆蓋率
- ✅ **API 文檔**: Swagger/OpenAPI 3.0 完整文檔

## 技術架構

### 技術棧

| 技術領域 | 技術選型 |
|---------|---------|
| **框架** | Spring Boot 3.2.0, Spring Security |
| **資料庫** | PostgreSQL (生產), H2 (開發) |
| **規則引擎** | Drools 8.44.0 |
| **快取** | Spring Cache + Caffeine |
| **測試** | JUnit 5, Mockito, Cucumber |
| **文檔** | SpringDoc OpenAPI 3.0 |
| **建置** | Gradle 8.5 |

### 系統需求

- **JDK**: 17+
- **記憶體**: 最小 2GB RAM
- **資料庫**: PostgreSQL 12+ (生產環境)
- **作業系統**: Linux/macOS/Windows

## 系統架構圖

### 整體架構圖

```mermaid
graph TB
    subgraph "外部系統"
        CLIENT[客戶端應用]
        EXT_SYS[外部系統]
        DB[(PostgreSQL)]
        CACHE[(Redis Cache)]
    end
    
    subgraph "API Gateway Layer"
        SWAGGER[Swagger UI]
        REST[REST Controllers]
    end
    
    subgraph "Application Layer"
        CMD[Command Handlers]
        QRY[Query Handlers]
        APP[Application Services]
        AUDIT[Audit Service]
    end
    
    subgraph "Domain Layer"
        AGG[Aggregates]
        ENT[Entities]
        VO[Value Objects]
        DS[Domain Services]
        REPO_INT[Repository Interfaces]
    end
    
    subgraph "Infrastructure Layer"
        REPO_IMPL[Repository Implementations]
        EXT_ADAPTER[External System Adapters]
        DROOLS[Drools Engine]
        SPEL[SpEL Engine]
    end
    
    CLIENT --> SWAGGER
    CLIENT --> REST
    REST --> CMD
    REST --> QRY
    CMD --> APP
    QRY --> APP
    APP --> DS
    APP --> AUDIT
    DS --> AGG
    DS --> REPO_INT
    REPO_INT --> REPO_IMPL
    REPO_IMPL --> DB
    DS --> EXT_ADAPTER
    EXT_ADAPTER --> EXT_SYS
    DS --> DROOLS
    DS --> SPEL
    APP --> CACHE
```

### 六角形架構圖

```mermaid
graph LR
    subgraph "外部世界"
        REST_CLIENT[REST Client]
        DATABASE[(Database)]
        EXTERNAL[External Systems]
        RULES_ENGINE[Drools Engine]
        CACHE_SYS[(Cache)]
    end
    
    subgraph "應用核心"
        subgraph "Application Layer"
            CONTROLLERS[Controllers]
            CMD_HANDLERS[Command Handlers]
            QRY_HANDLERS[Query Handlers]
            APP_SERVICES[Application Services]
        end
        
        subgraph "Domain Layer"
            AGGREGATES[Aggregates]
            ENTITIES[Entities]
            VALUE_OBJECTS[Value Objects]
            DOMAIN_SERVICES[Domain Services]
            PORTS[Repository Ports]
        end
    end
    
    subgraph "Adapter Layer"
        REPO_ADAPTERS[Repository Adapters]
        EXT_ADAPTERS[External System Adapters]
        CACHE_ADAPTERS[Cache Adapters]
        AUDIT_ADAPTERS[Audit Adapters]
    end
    
    REST_CLIENT --> CONTROLLERS
    CONTROLLERS --> CMD_HANDLERS
    CONTROLLERS --> QRY_HANDLERS
    CMD_HANDLERS --> APP_SERVICES
    QRY_HANDLERS --> APP_SERVICES
    APP_SERVICES --> DOMAIN_SERVICES
    DOMAIN_SERVICES --> AGGREGATES
    DOMAIN_SERVICES --> PORTS
    PORTS --> REPO_ADAPTERS
    PORTS --> EXT_ADAPTERS
    REPO_ADAPTERS --> DATABASE
    EXT_ADAPTERS --> EXTERNAL
    EXT_ADAPTERS --> RULES_ENGINE
    QRY_HANDLERS --> CACHE_ADAPTERS
    CACHE_ADAPTERS --> CACHE_SYS
    APP_SERVICES --> AUDIT_ADAPTERS
```

## 循序圖

### 優惠評估流程

```mermaid
sequenceDiagram
    participant Client as 客戶端
    participant Controller as PromotionController
    participant AppService as PromotionApplicationService
    participant DomainService as Domain Service
    participant DecisionTree as PromotionDecisionTree
    participant CommandFactory as Command Factory
    participant ExternalSystem as 外部系統
    participant AuditService as Audit Service
    participant Database as 資料庫

    Client->>Controller: POST /api/v1/promotions/evaluate
    Controller->>AppService: evaluatePromotion(command)
    AppService->>AuditService: recordRequest(requestId, payload)
    AuditService->>Database: 記錄請求日誌
    
    AppService->>DomainService: evaluateCustomerPromotion(customer)
    DomainService->>DecisionTree: traverse(customerPayload)
    
    loop 決策樹遍歷
        DecisionTree->>CommandFactory: createCommand(nodeConfig)
        CommandFactory->>DecisionTree: return command
        DecisionTree->>DecisionTree: executeCommand(command)
        
        alt 需要外部系統呼叫
            DecisionTree->>ExternalSystem: callExternalService()
            ExternalSystem->>DecisionTree: return result
            DecisionTree->>AuditService: recordExternalCall()
        end
        
        DecisionTree->>AuditService: recordDecisionStep()
    end
    
    DecisionTree->>DomainService: return promotionResult
    DomainService->>AppService: return result
    AppService->>AuditService: recordResponse(result)
    AppService->>Controller: return promotionResult
    Controller->>Client: HTTP 200 + promotionResult
```

### 稽核查詢流程

```mermaid
sequenceDiagram
    participant Client as 稽核人員
    participant Controller as AuditController
    participant AuditService as AuditService
    participant Repository as AuditRepository
    participant Database as 資料庫

    Client->>Controller: GET /api/v1/audit/trails?requestId=xxx
    Controller->>Controller: 驗證權限 (ADMIN/AUDITOR)
    Controller->>AuditService: getAuditTrails(requestId)
    AuditService->>Repository: findByRequestId(requestId)
    Repository->>Database: SELECT * FROM audit_trails WHERE request_id = ?
    Database->>Repository: return audit records
    Repository->>AuditService: return auditTrails
    AuditService->>Controller: return auditTrails
    Controller->>Client: HTTP 200 + auditTrails
```

## 類別圖

### 領域模型類別圖

```mermaid
classDiagram
    class PromotionDecisionTree {
        -String id
        -String name
        -DecisionNode rootNode
        -TreeStatus status
        +traverse(CustomerPayload) PromotionResult
        +addNode(DecisionNode) void
        +validateStructure() boolean
    }
    
    class DecisionNode {
        <<abstract>>
        -String id
        -String nodeType
        -NodeConfiguration config
        -List~DecisionNode~ children
        +execute(ExecutionContext) NodeResult
        +addChild(DecisionNode) void
    }
    
    class ConditionNode {
        +execute(ExecutionContext) NodeResult
        +evaluateCondition(CustomerPayload) boolean
    }
    
    class CalculationNode {
        +execute(ExecutionContext) NodeResult
        +calculatePromotion(CustomerPayload) PromotionResult
    }
    
    class CustomerPayload {
        -String customerId
        -String accountType
        -BigDecimal annualIncome
        -Integer creditScore
        -String region
        -Integer transactionCount
        -BigDecimal accountBalance
        -List~Object~ transactionHistory
    }
    
    class PromotionResult {
        -String promotionId
        -String promotionName
        -String promotionType
        -BigDecimal discountAmount
        -Double discountPercentage
        -String description
        -LocalDateTime validUntil
        -Map~String,Object~ additionalDetails
        -boolean eligible
    }
    
    class NodeCommand {
        <<interface>>
        +execute(CustomerProfile, ExecutionContext) NodeResult
    }
    
    class SpELConditionCommand {
        -String expression
        +execute(CustomerProfile, ExecutionContext) NodeResult
    }
    
    class DroolsRuleCommand {
        -String ruleName
        +execute(CustomerProfile, ExecutionContext) NodeResult
    }
    
    class CalculationStrategy {
        <<interface>>
        +calculate(CustomerProfile, PromotionParameters) BigDecimal
    }
    
    class PercentageDiscountStrategy {
        +calculate(CustomerProfile, PromotionParameters) BigDecimal
    }
    
    class FixedAmountStrategy {
        +calculate(CustomerProfile, PromotionParameters) BigDecimal
    }
    
    PromotionDecisionTree --> DecisionNode
    DecisionNode <|-- ConditionNode
    DecisionNode <|-- CalculationNode
    DecisionNode --> NodeCommand
    NodeCommand <|.. SpELConditionCommand
    NodeCommand <|.. DroolsRuleCommand
    CalculationNode --> CalculationStrategy
    CalculationStrategy <|.. PercentageDiscountStrategy
    CalculationStrategy <|.. FixedAmountStrategy
    PromotionDecisionTree ..> CustomerPayload
    PromotionDecisionTree ..> PromotionResult
```

### Command Pattern 類別圖

```mermaid
classDiagram
    class NodeCommand {
        <<interface>>
        +execute(ExecutionContext) NodeResult
    }
    
    class ExecutionContext {
        -CustomerPayload customer
        -Map~String,Object~ variables
        -String requestId
        +getCustomer() CustomerPayload
        +setVariable(String, Object) void
        +getVariable(String) Object
    }
    
    class NodeResult {
        -boolean success
        -Object result
        -String errorMessage
        -long executionTimeMs
        +isSuccess() boolean
        +getResult() Object
    }
    
    class SpELConditionCommand {
        -String expression
        -SpelExpressionParser parser
        +execute(ExecutionContext) NodeResult
    }
    
    class DroolsRuleCommand {
        -String ruleName
        -KieSession kieSession
        +execute(ExecutionContext) NodeResult
    }
    
    class ExternalSystemCommand {
        -String endpoint
        -ExternalSystemAdapter adapter
        +execute(ExecutionContext) NodeResult
    }
    
    class DatabaseQueryCommand {
        -String query
        -DatabaseAdapter adapter
        +execute(ExecutionContext) NodeResult
    }
    
    class CommandFactory {
        +createCommand(NodeConfiguration) NodeCommand
        +registerCommand(String, Class) void
    }
    
    NodeCommand <|.. SpELConditionCommand
    NodeCommand <|.. DroolsRuleCommand
    NodeCommand <|.. ExternalSystemCommand
    NodeCommand <|.. DatabaseQueryCommand
    NodeCommand ..> ExecutionContext
    NodeCommand ..> NodeResult
    CommandFactory ..> NodeCommand
```

## 優惠評估流程

### 整體流程圖

```mermaid
flowchart TD
    A[客戶提交評估請求] --> B[驗證請求資料]
    B --> C[記錄請求日誌]
    C --> D[載入決策樹配置]
    D --> E[開始決策樹遍歷]
    
    E --> F{是否為條件節點?}
    F -->|是| G[執行條件因子]
    F -->|否| H[執行計算因子]
    
    G --> I{條件是否滿足?}
    I -->|是| J[進入子節點]
    I -->|否| K[嘗試兄弟節點]
    
    J --> L{還有子節點?}
    L -->|是| F
    L -->|否| M[到達葉節點]
    
    K --> N{還有兄弟節點?}
    N -->|是| F
    N -->|否| O[返回預設結果]
    
    H --> P[計算優惠結果]
    M --> P
    O --> Q[記錄決策軌跡]
    P --> Q
    Q --> R[返回優惠結果]
    
    style A fill:#e1f5fe
    style R fill:#c8e6c9
    style G fill:#fff3e0
    style H fill:#fce4ec
```

### 決策樹執行邏輯

1. **請求接收階段**
   - 接收客戶資料 (CustomerPayload)
   - 驗證必要欄位完整性
   - 生成唯一請求 ID

2. **決策樹載入階段**
   - 根據 treeId 載入決策樹配置
   - 初始化執行上下文 (ExecutionContext)
   - 準備稽核記錄機制

3. **節點遍歷階段**
   - 從根節點開始深度優先遍歷
   - 每個節點執行對應的 Command
   - 記錄每步驟的執行結果

4. **結果生成階段**
   - 彙整所有決策步驟結果
   - 生成最終優惠推薦
   - 記錄完整稽核軌跡

### 優惠評估決策邏輯

```mermaid
graph TD
    START([開始評估]) --> INPUT[客戶資料輸入]
    INPUT --> VALIDATE{資料驗證}
    VALIDATE -->|失敗| ERROR[返回錯誤]
    VALIDATE -->|成功| LOAD_TREE[載入決策樹]
    
    LOAD_TREE --> ROOT[根節點評估]
    ROOT --> ACCOUNT_TYPE{帳戶類型判斷}
    
    ACCOUNT_TYPE -->|VIP| VIP_BRANCH[VIP 分支]
    ACCOUNT_TYPE -->|白金| PLATINUM_BRANCH[白金分支]
    ACCOUNT_TYPE -->|一般| REGULAR_BRANCH[一般分支]
    
    VIP_BRANCH --> VIP_INCOME{年收入 >= 200萬?}
    VIP_INCOME -->|是| VIP_HIGH[VIP高價值客戶]
    VIP_INCOME -->|否| VIP_REGULAR[VIP一般客戶]
    
    PLATINUM_BRANCH --> PLAT_CREDIT{信用評分 >= 700?}
    PLAT_CREDIT -->|是| PLAT_HIGH[白金優質客戶]
    PLAT_CREDIT -->|否| PLAT_REGULAR[白金一般客戶]
    
    REGULAR_BRANCH --> REG_NEW{新戶判斷}
    REG_NEW -->|是| NEW_CUSTOMER[新戶優惠]
    REG_NEW -->|否| EXISTING[既有客戶優惠]
    
    VIP_HIGH --> CALC_VIP_HIGH[計算VIP專屬優惠]
    VIP_REGULAR --> CALC_VIP_REG[計算VIP標準優惠]
    PLAT_HIGH --> CALC_PLAT_HIGH[計算白金優質優惠]
    PLAT_REGULAR --> CALC_PLAT_REG[計算白金標準優惠]
    NEW_CUSTOMER --> CALC_NEW[計算新戶優惠]
    EXISTING --> CALC_EXISTING[計算既有客戶優惠]
    
    CALC_VIP_HIGH --> RESULT[生成優惠結果]
    CALC_VIP_REG --> RESULT
    CALC_PLAT_HIGH --> RESULT
    CALC_PLAT_REG --> RESULT
    CALC_NEW --> RESULT
    CALC_EXISTING --> RESULT
    
    RESULT --> AUDIT[記錄稽核軌跡]
    AUDIT --> END([結束])
    ERROR --> END
    
    style START fill:#e8f5e8
    style END fill:#ffe8e8
    style CALC_VIP_HIGH fill:#e3f2fd
    style CALC_PLAT_HIGH fill:#f3e5f5
    style CALC_NEW fill:#fff3e0
```

## 條件因子與計算因子

### 條件因子清單 (Condition Factors)

條件因子負責評估客戶是否符合特定條件，決定決策樹的遍歷路徑。

#### 1. 客戶基本資料條件

| 條件因子 | 描述 | SpEL 表達式範例 | 用途 |
|---------|------|----------------|------|
| **帳戶類型判斷** | 判斷客戶帳戶等級 | `#{accountType == 'VIP'}` | 區分客戶等級 |
| **年收入門檻** | 評估客戶收入水準 | `#{annualIncome >= 2000000}` | 高價值客戶識別 |
| **信用評分等級** | 評估客戶信用狀況 | `#{creditScore >= 750}` | 風險評估 |
| **地區限制** | 特定地區優惠限制 | `#{region in {'台北市','新北市'}}` | 地區性優惠 |
| **帳戶餘額門檻** | 評估客戶資產狀況 | `#{accountBalance >= 500000}` | 資產評估 |

#### 2. 交易行為條件

| 條件因子 | 描述 | SpEL 表達式範例 | 用途 |
|---------|------|----------------|------|
| **交易頻率** | 評估客戶活躍度 | `#{transactionCount >= 50}` | 活躍度評估 |
| **交易金額** | 評估交易規模 | `#{transactionHistory.?[amount > 10000].size() >= 5}` | 大額交易客戶 |
| **新戶判斷** | 識別新開戶客戶 | `#{transactionHistory.size() <= 3}` | 新戶優惠 |
| **VIP 交易模式** | VIP 客戶特殊交易模式 | `#{accountType == 'VIP' and transactionCount >= 30}` | VIP 活躍客戶 |

#### 3. 複合條件

| 條件因子 | 描述 | SpEL 表達式範例 | 用途 |
|---------|------|----------------|------|
| **高價值客戶** | 綜合評估高價值客戶 | `#{annualIncome >= 2000000 and creditScore >= 700}` | 頂級客戶識別 |
| **優質新戶** | 有潛力的新客戶 | `#{creditScore >= 650 and annualIncome >= 800000 and transactionHistory.size() <= 5}` | 新戶潛力評估 |
| **風險客戶** | 需要特別關注的客戶 | `#{creditScore < 600 or accountBalance < 10000}` | 風險控制 |

#### 4. 外部系統整合條件

| 條件因子 | 描述 | 實作方式 | 用途 |
|---------|------|----------|------|
| **信用局查詢** | 查詢外部信用資料 | ExternalSystemCommand | 信用驗證 |
| **反洗錢檢查** | AML 合規檢查 | ExternalSystemCommand | 合規控制 |
| **黑名單檢查** | 檢查客戶黑名單狀態 | DatabaseQueryCommand | 風險控制 |
| **產品持有狀況** | 查詢客戶現有產品 | DatabaseQueryCommand | 交叉銷售 |

### 計算因子清單 (Calculation Factors)

計算因子負責計算具體的優惠內容和金額，是決策樹的葉節點。

#### 1. 折扣計算策略

| 計算因子 | 策略類型 | 計算邏輯 | 適用場景 |
|---------|---------|----------|----------|
| **百分比折扣** | PercentageDiscountStrategy | 基於交易金額的百分比折扣 | 手續費減免 |
| **固定金額優惠** | FixedAmountStrategy | 固定金額的現金回饋 | 開戶禮金 |
| **階層式折扣** | TieredDiscountStrategy | 根據客戶等級的階層折扣 | VIP 專屬優惠 |
| **累進式優惠** | ProgressiveDiscountStrategy | 隨交易量增加的累進優惠 | 交易量獎勵 |

#### 2. VIP 客戶專屬計算

| 計算因子 | 描述 | 計算公式 | 優惠內容 |
|---------|------|----------|----------|
| **VIP 專屬理財優惠** | 高價值客戶專屬服務 | `基礎優惠 × 1.5 + VIP加成` | 專屬理財顧問 + 手續費全免 |
| **VIP 信用卡優惠** | VIP 信用卡專案 | `年費減免 + 現金回饋加碼` | 白金卡年費減免 |
| **VIP 貸款優惠** | 優惠貸款利率 | `基準利率 - 0.5%` | 房貸利率優惠 |

#### 3. 一般客戶優惠計算

| 計算因子 | 描述 | 計算公式 | 優惠內容 |
|---------|------|----------|----------|
| **新戶開戶優惠** | 新客戶開戶禮 | `固定金額 1000 元` | 開戶現金禮 |
| **數位帳戶優惠** | 數位服務推廣 | `轉帳手續費減免 6 個月` | 數位金融優惠 |
| **定存利率加碼** | 定期存款優惠 | `基準利率 + 0.2%` | 定存利率優惠 |

#### 4. 地區性優惠計算

| 計算因子 | 描述 | 適用地區 | 優惠內容 |
|---------|------|----------|----------|
| **北部地區優惠** | 台北、新北專屬 | 台北市、新北市 | 分行服務費減免 |
| **中部地區優惠** | 台中、彰化專屬 | 台中市、彰化縣 | 在地商家合作優惠 |
| **南部地區優惠** | 高雄、台南專屬 | 高雄市、台南市 | 區域性現金回饋 |

#### 5. 產品交叉銷售計算

| 計算因子 | 描述 | 觸發條件 | 優惠內容 |
|---------|------|----------|----------|
| **信用卡推薦** | 信用卡產品推薦 | 無信用卡且信用良好 | 首年年費減免 |
| **投資理財推薦** | 理財產品推薦 | 高資產且無投資產品 | 理財手續費優惠 |
| **保險產品推薦** | 保險產品推薦 | 家庭客戶且無保險 | 保費折扣優惠 |

### 決策樹配置範例

#### 範例 1: VIP 客戶決策樹

```json
{
  "treeId": "vip-customer-tree",
  "name": "VIP客戶優惠決策樹",
  "rootNode": {
    "nodeId": "root",
    "nodeType": "CONDITION",
    "configuration": {
      "commandType": "SPEL_CONDITION",
      "expression": "#{accountType == 'VIP'}",
      "description": "VIP客戶判斷"
    },
    "children": [
      {
        "nodeId": "vip-income-check",
        "nodeType": "CONDITION",
        "configuration": {
          "commandType": "SPEL_CONDITION",
          "expression": "#{annualIncome >= 2000000}",
          "description": "高收入VIP客戶判斷"
        },
        "children": [
          {
            "nodeId": "vip-premium-offer",
            "nodeType": "CALCULATION",
            "configuration": {
              "commandType": "CALCULATION",
              "strategyType": "TIERED_DISCOUNT",
              "parameters": {
                "discountAmount": 5000,
                "discountPercentage": 10.0,
                "promotionName": "VIP專屬理財優惠"
              }
            }
          }
        ]
      }
    ]
  }
}
```

#### 範例 2: 新戶優惠決策樹

```json
{
  "treeId": "new-customer-tree",
  "name": "新戶優惠決策樹",
  "rootNode": {
    "nodeId": "root",
    "nodeType": "CONDITION",
    "configuration": {
      "commandType": "SPEL_CONDITION",
      "expression": "#{transactionHistory.size() <= 3}",
      "description": "新戶判斷"
    },
    "children": [
      {
        "nodeId": "credit-check",
        "nodeType": "CONDITION",
        "configuration": {
          "commandType": "SPEL_CONDITION",
          "expression": "#{creditScore >= 650}",
          "description": "信用評分檢查"
        },
        "children": [
          {
            "nodeId": "new-customer-offer",
            "nodeType": "CALCULATION",
            "configuration": {
              "commandType": "CALCULATION",
              "strategyType": "FIXED_AMOUNT",
              "parameters": {
                "discountAmount": 1000,
                "promotionName": "新戶開戶優惠",
                "validDays": 30
              }
            }
          }
        ]
      }
    ]
  }
}
```

## 資料庫設計

### 資料表結構清單 (Database Schema)

系統共包含 8 個核心資料表，分為業務資料表和稽核資料表兩大類別。

#### 業務資料表 (Business Tables)

##### 1. 決策樹資料表 (decision_trees)

| 中文名稱 | 英文欄位名 | 資料型態 | 長度 | 約束 | 說明 |
|---------|-----------|---------|------|------|------|
| 決策樹ID | id | VARCHAR | 36 | PRIMARY KEY | 決策樹唯一識別碼 (UUID) |
| 決策樹名稱 | name | VARCHAR | 100 | NOT NULL | 決策樹顯示名稱 |
| 狀態 | status | VARCHAR | 20 | NOT NULL | 決策樹狀態 (ACTIVE/INACTIVE/DRAFT) |
| 根節點ID | root_node_id | VARCHAR | 36 | FOREIGN KEY | 指向根節點的外鍵 |
| 建立時間 | created_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 記錄建立時間 |
| 更新時間 | updated_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 記錄最後更新時間 |

**索引**:
- `idx_tree_status` ON (status)

**用途**: 儲存決策樹的基本配置資訊，每個決策樹代表一套完整的優惠評估邏輯。

---

##### 2. 決策節點資料表 (decision_nodes)

| 中文名稱 | 英文欄位名 | 資料型態 | 長度 | 約束 | 說明 |
|---------|-----------|---------|------|------|------|
| 節點ID | id | VARCHAR | 36 | PRIMARY KEY | 決策節點唯一識別碼 (UUID) |
| 決策樹ID | tree_id | VARCHAR | 36 | NOT NULL, FOREIGN KEY | 所屬決策樹ID |
| 節點類型 | node_type | VARCHAR | 20 | NOT NULL | 節點類型 (CONDITION/CALCULATION) |
| 父節點ID | parent_id | VARCHAR | 36 | FOREIGN KEY | 父節點ID，根節點為 NULL |
| 節點配置 | configuration | JSON/TEXT | - | NOT NULL | 節點執行配置 (JSON格式) |
| 建立時間 | created_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 記錄建立時間 |

**外鍵關係**:
- `tree_id` → `decision_trees(id)`
- `parent_id` → `decision_nodes(id)` (自參照)

**用途**: 儲存決策樹中每個節點的詳細配置，包括條件判斷和計算邏輯。

---

##### 3. 優惠規則資料表 (promotion_rules)

| 中文名稱 | 英文欄位名 | 資料型態 | 長度 | 約束 | 說明 |
|---------|-----------|---------|------|------|------|
| 規則ID | id | VARCHAR | 36 | PRIMARY KEY | 優惠規則唯一識別碼 (UUID) |
| 規則名稱 | name | VARCHAR | 100 | NOT NULL | 規則顯示名稱 |
| 規則類型 | rule_type | VARCHAR | 20 | NOT NULL | 規則類型 (SPEL/DROOLS/HARDCODED) |
| 規則內容 | rule_content | TEXT | - | NOT NULL | 規則表達式或邏輯內容 |
| 參數設定 | parameters | JSON/TEXT | - | - | 規則執行參數 (JSON格式) |
| 狀態 | status | VARCHAR | 20 | NOT NULL | 規則狀態 (ACTIVE/INACTIVE) |
| 建立時間 | created_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 記錄建立時間 |
| 更新時間 | updated_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 記錄最後更新時間 |

**索引**:
- `idx_rule_status` ON (status)

**用途**: 儲存可重複使用的優惠規則，支援 SpEL 表達式和 Drools 規則引擎。

---

##### 4. 優惠歷史資料表 (promotion_history)

| 中文名稱 | 英文欄位名 | 資料型態 | 長度 | 約束 | 說明 |
|---------|-----------|---------|------|------|------|
| 歷史記錄ID | id | VARCHAR | 36 | PRIMARY KEY | 優惠歷史唯一識別碼 (UUID) |
| 客戶ID | customer_id | VARCHAR | 50 | NOT NULL | 客戶識別碼 |
| 優惠ID | promotion_id | VARCHAR | 36 | NOT NULL | 優惠方案識別碼 |
| 優惠結果 | promotion_result | JSON/TEXT | - | NOT NULL | 優惠評估結果 (JSON格式) |
| 執行時間 | executed_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 優惠執行時間 |

**索引**:
- `idx_customer_date` ON (customer_id, executed_at)

**用途**: 記錄客戶的優惠申請和執行歷史，用於分析和報表。

---

#### 稽核資料表 (Audit Tables)

##### 5. 請求日誌資料表 (request_logs)

| 中文名稱 | 英文欄位名 | 資料型態 | 長度 | 約束 | 說明 |
|---------|-----------|---------|------|------|------|
| 日誌ID | id | VARCHAR | 36 | PRIMARY KEY | 請求日誌唯一識別碼 (UUID) |
| 請求ID | request_id | VARCHAR | 36 | UNIQUE, NOT NULL | 請求唯一識別碼 |
| API端點 | api_endpoint | VARCHAR | 200 | NOT NULL | 被呼叫的API端點路徑 |
| HTTP方法 | http_method | VARCHAR | 10 | NOT NULL | HTTP請求方法 (GET/POST/PUT/DELETE) |
| 請求內容 | request_payload | JSON/TEXT | - | NOT NULL | 請求參數和內容 (JSON格式) |
| 回應內容 | response_payload | JSON/TEXT | - | - | 回應資料內容 (JSON格式) |
| 回應狀態碼 | response_status | INTEGER | - | - | HTTP回應狀態碼 |
| 客戶端IP | client_ip | VARCHAR | 45 | - | 客戶端IP位址 (支援IPv6) |
| 使用者代理 | user_agent | TEXT | - | - | 客戶端瀏覽器資訊 |
| 處理時間 | processing_time_ms | INTEGER | - | - | 請求處理時間 (毫秒) |
| 建立時間 | created_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 請求開始時間 |
| 完成時間 | completed_at | TIMESTAMP | - | - | 請求完成時間 |

**索引**:
- `idx_request_id` ON (request_id)
- `idx_endpoint_date` ON (api_endpoint, created_at)

**用途**: 記錄所有API請求的完整資訊，用於效能監控和問題追蹤。

---

##### 6. 稽核軌跡資料表 (audit_trails)

| 中文名稱 | 英文欄位名 | 資料型態 | 長度 | 約束 | 說明 |
|---------|-----------|---------|------|------|------|
| 稽核ID | id | VARCHAR | 36 | PRIMARY KEY | 稽核記錄唯一識別碼 (UUID) |
| 請求ID | request_id | VARCHAR | 36 | NOT NULL | 關聯的請求ID |
| 客戶ID | customer_id | VARCHAR | 50 | NOT NULL | 相關客戶ID |
| 操作類型 | operation_type | VARCHAR | 50 | NOT NULL | 操作類型 (如 PROMOTION_EVALUATION) |
| 操作詳情 | operation_details | JSON/TEXT | - | NOT NULL | 操作詳細資訊 (JSON格式) |
| 執行時間 | execution_time_ms | INTEGER | - | - | 操作執行時間 (毫秒) |
| 狀態 | status | VARCHAR | 20 | NOT NULL | 執行狀態 (SUCCESS/FAILED/PENDING) |
| 錯誤訊息 | error_message | TEXT | - | - | 錯誤詳細訊息 |
| 建立時間 | created_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 記錄建立時間 |

**索引**:
- `idx_audit_request_id` ON (request_id)
- `idx_audit_customer_operation` ON (customer_id, operation_type)
- `idx_audit_created_at` ON (created_at)

**用途**: 記錄業務操作的詳細軌跡，確保所有操作可追蹤和稽核。

---

##### 7. 決策步驟資料表 (decision_steps)

| 中文名稱 | 英文欄位名 | 資料型態 | 長度 | 約束 | 說明 |
|---------|-----------|---------|------|------|------|
| 步驟ID | id | VARCHAR | 36 | PRIMARY KEY | 決策步驟唯一識別碼 (UUID) |
| 請求ID | request_id | VARCHAR | 36 | NOT NULL, FOREIGN KEY | 關聯的請求ID |
| 決策樹ID | tree_id | VARCHAR | 36 | NOT NULL, FOREIGN KEY | 執行的決策樹ID |
| 節點ID | node_id | VARCHAR | 36 | NOT NULL, FOREIGN KEY | 執行的節點ID |
| 步驟順序 | step_order | INTEGER | - | NOT NULL | 執行順序編號 |
| 節點類型 | node_type | VARCHAR | 20 | NOT NULL | 節點類型 (CONDITION/CALCULATION) |
| 輸入資料 | input_data | JSON/TEXT | - | NOT NULL | 節點輸入資料 (JSON格式) |
| 輸出資料 | output_data | JSON/TEXT | - | - | 節點輸出結果 (JSON格式) |
| 執行時間 | execution_time_ms | INTEGER | - | - | 節點執行時間 (毫秒) |
| 狀態 | status | VARCHAR | 20 | NOT NULL | 執行狀態 (SUCCESS/FAILED) |
| 錯誤詳情 | error_details | TEXT | - | - | 錯誤詳細資訊 |
| 建立時間 | created_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 記錄建立時間 |

**外鍵關係**:
- `request_id` → `request_logs(request_id)`
- `tree_id` → `decision_trees(id)`

**索引**:
- `idx_decision_request_step` ON (request_id, step_order)
- `idx_decision_tree_node` ON (tree_id, node_id)

**用途**: 詳細記錄決策樹執行過程中每個節點的執行情況，用於除錯和效能分析。

---

##### 8. 系統事件資料表 (system_events)

| 中文名稱 | 英文欄位名 | 資料型態 | 長度 | 約束 | 說明 |
|---------|-----------|---------|------|------|------|
| 事件ID | id | VARCHAR | 36 | PRIMARY KEY | 系統事件唯一識別碼 (UUID) |
| 事件類型 | event_type | VARCHAR | 50 | NOT NULL | 事件類型 (如 SYSTEM_START, ERROR) |
| 事件分類 | event_category | VARCHAR | 30 | NOT NULL | 事件分類 (SYSTEM/BUSINESS/SECURITY) |
| 事件詳情 | event_details | JSON/TEXT | - | NOT NULL | 事件詳細資訊 (JSON格式) |
| 嚴重程度 | severity_level | VARCHAR | 20 | NOT NULL | 嚴重程度 (INFO/WARN/ERROR/CRITICAL) |
| 來源元件 | source_component | VARCHAR | 100 | NOT NULL | 產生事件的系統元件 |
| 關聯ID | correlation_id | VARCHAR | 36 | - | 關聯的請求或操作ID |
| 建立時間 | created_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 事件發生時間 |

**索引**:
- `idx_event_type_date` ON (event_type, created_at)
- `idx_event_correlation_id` ON (correlation_id)
- `idx_event_severity_date` ON (severity_level, created_at)

**用途**: 記錄系統運行過程中的各種事件，用於系統監控和問題診斷。

---

### 資料表關係圖 (ER-Diagram)

```mermaid
erDiagram
    DECISION_TREES {
        varchar id PK
        varchar name
        varchar status
        varchar root_node_id FK
        timestamp created_at
        timestamp updated_at
    }
    
    DECISION_NODES {
        varchar id PK
        varchar tree_id FK
        varchar node_type
        varchar parent_id FK
        json configuration
        timestamp created_at
    }
    
    PROMOTION_RULES {
        varchar id PK
        varchar name
        varchar rule_type
        text rule_content
        json parameters
        varchar status
        timestamp created_at
        timestamp updated_at
    }
    
    PROMOTION_HISTORY {
        varchar id PK
        varchar customer_id
        varchar promotion_id FK
        json promotion_result
        timestamp executed_at
    }
    
    AUDIT_TRAILS {
        varchar id PK
        varchar request_id
        varchar customer_id
        varchar operation_type
        json operation_details
        integer execution_time_ms
        varchar status
        text error_message
        timestamp created_at
    }
    
    REQUEST_LOGS {
        varchar id PK
        varchar request_id UK
        varchar api_endpoint
        varchar http_method
        json request_payload
        json response_payload
        integer response_status
        varchar client_ip
        text user_agent
        integer processing_time_ms
        timestamp created_at
        timestamp completed_at
    }
    
    DECISION_STEPS {
        varchar id PK
        varchar request_id FK
        varchar tree_id FK
        varchar node_id FK
        integer step_order
        varchar node_type
        json input_data
        json output_data
        integer execution_time_ms
        varchar status
        text error_details
        timestamp created_at
    }
    
    SYSTEM_EVENTS {
        varchar id PK
        varchar event_type
        varchar event_category
        json event_details
        varchar severity_level
        varchar source_component
        varchar correlation_id
        timestamp created_at
    }
    
    DECISION_TREES ||--o{ DECISION_NODES : contains
    DECISION_NODES ||--o{ DECISION_NODES : parent_child
    PROMOTION_RULES ||--o{ PROMOTION_HISTORY : applies_to
    REQUEST_LOGS ||--o{ DECISION_STEPS : tracks
    DECISION_TREES ||--o{ DECISION_STEPS : executes
```

### 資料型態說明

#### PostgreSQL vs H2 資料型態對應

| 邏輯型態 | PostgreSQL | H2 | 說明 |
|---------|-----------|-----|------|
| **UUID** | VARCHAR(36) | VARCHAR(36) | 36字元的UUID字串 |
| **短文字** | VARCHAR(n) | VARCHAR(n) | 可變長度字串，最大n字元 |
| **長文字** | TEXT | TEXT | 不限長度的文字內容 |
| **JSON資料** | JSON | TEXT | PostgreSQL原生JSON，H2使用TEXT儲存 |
| **整數** | INTEGER | INTEGER | 32位元整數 |
| **時間戳記** | TIMESTAMP | TIMESTAMP | 日期時間，精確到毫秒 |

#### 約束條件說明

| 約束類型 | 說明 | 範例 |
|---------|------|------|
| **PRIMARY KEY** | 主鍵，唯一且非空 | `id VARCHAR(36) PRIMARY KEY` |
| **FOREIGN KEY** | 外鍵，參照其他資料表 | `FOREIGN KEY (tree_id) REFERENCES decision_trees(id)` |
| **UNIQUE** | 唯一約束，不允許重複值 | `request_id VARCHAR(36) UNIQUE` |
| **NOT NULL** | 非空約束，必須有值 | `name VARCHAR(100) NOT NULL` |
| **DEFAULT** | 預設值 | `created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP` |

#### 狀態欄位值域

| 資料表 | 欄位 | 可能值 | 說明 |
|--------|------|--------|------|
| **decision_trees** | status | ACTIVE, INACTIVE, DRAFT | 啟用、停用、草稿 |
| **promotion_rules** | status | ACTIVE, INACTIVE | 啟用、停用 |
| **promotion_rules** | rule_type | SPEL, DROOLS, HARDCODED | SpEL表達式、Drools規則、硬編碼 |
| **decision_nodes** | node_type | CONDITION, CALCULATION | 條件節點、計算節點 |
| **audit_trails** | status | SUCCESS, FAILED, PENDING | 成功、失敗、處理中 |
| **decision_steps** | status | SUCCESS, FAILED | 成功、失敗 |
| **system_events** | severity_level | INFO, WARN, ERROR, CRITICAL | 資訊、警告、錯誤、嚴重 |
| **system_events** | event_category | SYSTEM, BUSINESS, SECURITY | 系統、業務、安全 |

#### JSON 欄位結構範例

##### decision_nodes.configuration
```json
{
  "commandType": "SPEL_CONDITION",
  "expression": "#{accountType == 'VIP'}",
  "description": "VIP客戶判斷",
  "parameters": {
    "timeout": 5000,
    "retryCount": 3
  }
}
```

##### promotion_rules.parameters
```json
{
  "discountAmount": 5000,
  "discountPercentage": 10.0,
  "maxDiscount": 50000,
  "validDays": 30,
  "applicableRegions": ["台北市", "新北市"]
}
```

##### promotion_history.promotion_result
```json
{
  "promotionId": "promo-vip-001",
  "promotionName": "VIP專屬優惠",
  "discountAmount": 5000,
  "eligible": true,
  "validUntil": "2024-12-31T23:59:59",
  "additionalDetails": {
    "tier": "VIP",
    "category": "premium"
  }
}
```

### 資料表統計資訊

| 資料表 | 預估記錄數 | 成長率 | 保留期間 | 備註 |
|--------|-----------|--------|----------|------|
| **decision_trees** | < 100 | 低 | 永久 | 配置資料，變動少 |
| **decision_nodes** | < 1,000 | 低 | 永久 | 配置資料，變動少 |
| **promotion_rules** | < 500 | 中 | 永久 | 業務規則，定期更新 |
| **promotion_history** | 100萬+ | 高 | 7年 | 交易記錄，快速成長 |
| **request_logs** | 1000萬+ | 極高 | 3個月 | 請求日誌，需定期清理 |
| **audit_trails** | 1000萬+ | 極高 | 7年 | 稽核記錄，法規要求 |
| **decision_steps** | 5000萬+ | 極高 | 1年 | 詳細追蹤，可壓縮歸檔 |
| **system_events** | 100萬+ | 高 | 6個月 | 系統日誌，定期清理 |

### 效能優化建議

#### 索引策略
- **複合索引**: 針對常用查詢條件建立複合索引
- **部分索引**: 對於有狀態篩選的查詢使用部分索引
- **GIN索引**: PostgreSQL 的 JSON 欄位使用 GIN 索引

#### 分割策略
```sql
-- 按時間分割大型資料表 (PostgreSQL)
CREATE TABLE audit_trails_2024_01 PARTITION OF audit_trails
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE audit_trails_2024_02 PARTITION OF audit_trails  
FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');
```

#### 資料清理策略
```sql
-- 定期清理舊的請求日誌 (保留3個月)
DELETE FROM request_logs 
WHERE created_at < CURRENT_DATE - INTERVAL '3 months';

-- 歸檔舊的決策步驟 (保留1年)
INSERT INTO decision_steps_archive 
SELECT * FROM decision_steps 
WHERE created_at < CURRENT_DATE - INTERVAL '1 year';

DELETE FROM decision_steps 
WHERE created_at < CURRENT_DATE - INTERVAL '1 year';
```

### 資料表摘要

| 分類 | 資料表數量 | 主要用途 | 特性 |
|------|-----------|----------|------|
| **業務資料表** | 4個 | 核心業務邏輯和配置 | 資料量小，變動少，需要高可用性 |
| **稽核資料表** | 4個 | 稽核追蹤和合規 | 資料量大，只增不改，需要長期保存 |
| **總計** | 8個 | 完整的優惠推薦系統 | 支援高併發，確保資料一致性 |

## API 文檔

### Swagger UI 存取

系統整合了完整的 Swagger/OpenAPI 3.0 文檔，提供互動式 API 測試介面。

**存取方式:**
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **重定向**: http://localhost:8080/swagger-ui.html → http://localhost:8080/swagger-ui/index.html

### API 端點清單

#### 1. 優惠評估 API

| 端點 | 方法 | 描述 | 權限 |
|------|------|------|------|
| `/api/v1/promotions/evaluate` | POST | 評估客戶優惠資格 | 公開 |
| `/api/v1/promotions/history` | GET | 查詢優惠歷史 | 認證 |
| `/api/v1/promotions/available` | GET | 查詢可用優惠 | 認證 |

#### 2. 管理 API

| 端點 | 方法 | 描述 | 權限 |
|------|------|------|------|
| `/api/v1/management/decision-trees` | GET, POST | 決策樹管理 | ADMIN, MANAGER |
| `/api/v1/management/decision-trees/{treeId}` | GET, PUT, DELETE | 特定決策樹操作 | ADMIN, MANAGER |
| `/api/v1/management/promotion-rules` | GET, POST | 優惠規則管理 | ADMIN, MANAGER |
| `/api/v1/management/promotion-rules/{ruleId}` | GET, PUT, DELETE | 特定規則操作 | ADMIN, MANAGER |
| `/api/v1/management/promotion-rules/{ruleId}/status` | PATCH | 規則狀態更新 | ADMIN, MANAGER |

#### 3. 稽核追蹤 API

| 端點 | 方法 | 描述 | 權限 |
|------|------|------|------|
| `/api/v1/audit/trails` | GET | 查詢稽核軌跡 | ADMIN, AUDITOR |
| `/api/v1/audit/decisions` | GET | 查詢決策步驟 | ADMIN, AUDITOR |
| `/api/v1/audit/external-calls` | GET | 查詢外部系統呼叫 | ADMIN, AUDITOR |
| `/api/v1/audit/database-queries` | GET | 查詢資料庫操作 | ADMIN, AUDITOR |
| `/api/v1/audit/compliance-report` | GET | 生成合規性報告 | ADMIN, AUDITOR |

#### 4. 系統監控 API

| 端點 | 方法 | 描述 | 權限 |
|------|------|------|------|
| `/actuator/health` | GET | 系統健康檢查 | 公開 |

## 完整 API 清單

### 優惠評估相關 API

#### POST /api/v1/promotions/evaluate
**功能**: 評估客戶優惠資格  
**權限**: 公開存取  
**請求格式**:
```json
{
  "customerId": "string",
  "accountType": "string",
  "annualIncome": "number",
  "creditScore": "integer",
  "region": "string",
  "transactionCount": "integer",
  "accountBalance": "number",
  "transactionHistory": "array",
  "treeId": "string (optional)"
}
```
**回應格式**:
```json
{
  "success": true,
  "data": {
    "promotionId": "string",
    "promotionName": "string",
    "promotionType": "string",
    "discountAmount": "number",
    "discountPercentage": "number",
    "description": "string",
    "validUntil": "datetime",
    "additionalDetails": "object",
    "eligible": "boolean"
  },
  "message": "string",
  "timestamp": "datetime"
}
```

#### GET /api/v1/promotions/history
**功能**: 查詢客戶優惠歷史記錄  
**權限**: 需要認證  
**查詢參數**:
- `customerId`: 客戶ID
- `startDate`: 開始日期 (ISO 8601)
- `endDate`: 結束日期 (ISO 8601)
- `page`: 頁碼 (預設: 0)
- `size`: 每頁筆數 (預設: 20)

#### GET /api/v1/promotions/available
**功能**: 查詢目前可用的優惠方案  
**權限**: 需要認證  
**查詢參數**:
- `accountType`: 帳戶類型篩選
- `region`: 地區篩選
- `active`: 是否只顯示啟用的優惠

### 管理相關 API

#### GET /api/v1/management/decision-trees
**功能**: 取得所有決策樹列表  
**權限**: ADMIN, MANAGER  
**查詢參數**:
- `status`: 狀態篩選 (ACTIVE, INACTIVE, DRAFT)
- `page`: 頁碼
- `size`: 每頁筆數

#### POST /api/v1/management/decision-trees
**功能**: 建立新的決策樹  
**權限**: ADMIN, MANAGER  
**請求格式**:
```json
{
  "name": "string",
  "description": "string",
  "rootNode": {
    "nodeType": "CONDITION|CALCULATION",
    "configuration": "object",
    "children": "array"
  },
  "status": "ACTIVE|INACTIVE|DRAFT"
}
```

#### GET /api/v1/management/decision-trees/{treeId}
**功能**: 取得特定決策樹詳細資訊  
**權限**: ADMIN, MANAGER  
**路徑參數**:
- `treeId`: 決策樹ID

#### PUT /api/v1/management/decision-trees/{treeId}
**功能**: 更新決策樹配置  
**權限**: ADMIN, MANAGER  
**請求格式**: 同 POST 格式

#### DELETE /api/v1/management/decision-trees/{treeId}
**功能**: 刪除決策樹  
**權限**: ADMIN  
**注意**: 軟刪除，不會實際刪除資料

#### GET /api/v1/management/promotion-rules
**功能**: 取得所有優惠規則列表  
**權限**: ADMIN, MANAGER  
**查詢參數**:
- `ruleType`: 規則類型 (SPEL, DROOLS, HARDCODED)
- `status`: 狀態篩選
- `page`: 頁碼
- `size`: 每頁筆數

#### POST /api/v1/management/promotion-rules
**功能**: 建立新的優惠規則  
**權限**: ADMIN, MANAGER  
**請求格式**:
```json
{
  "name": "string",
  "ruleType": "SPEL|DROOLS|HARDCODED",
  "ruleContent": "string",
  "parameters": "object",
  "description": "string",
  "status": "ACTIVE|INACTIVE"
}
```

#### GET /api/v1/management/promotion-rules/{ruleId}
**功能**: 取得特定優惠規則詳細資訊  
**權限**: ADMIN, MANAGER

#### PUT /api/v1/management/promotion-rules/{ruleId}
**功能**: 更新優惠規則  
**權限**: ADMIN, MANAGER

#### DELETE /api/v1/management/promotion-rules/{ruleId}
**功能**: 刪除優惠規則  
**權限**: ADMIN

#### PATCH /api/v1/management/promotion-rules/{ruleId}/status
**功能**: 更新規則狀態 (啟用/停用)  
**權限**: ADMIN, MANAGER  
**請求格式**:
```json
{
  "status": "ACTIVE|INACTIVE"
}
```

### 稽核追蹤相關 API

#### GET /api/v1/audit/trails
**功能**: 查詢稽核軌跡記錄  
**權限**: ADMIN, AUDITOR  
**查詢參數**:
- `requestId`: 請求ID
- `customerId`: 客戶ID
- `startDate`: 開始時間
- `endDate`: 結束時間
- `operationType`: 操作類型
- `status`: 執行狀態
- `page`: 頁碼
- `size`: 每頁筆數

#### GET /api/v1/audit/decisions
**功能**: 查詢決策步驟追蹤  
**權限**: ADMIN, AUDITOR  
**查詢參數**:
- `requestId`: 請求ID (必填)
- `treeId`: 決策樹ID
- `nodeType`: 節點類型

#### GET /api/v1/audit/external-calls
**功能**: 查詢外部系統呼叫記錄  
**權限**: ADMIN, AUDITOR  
**查詢參數**:
- `requestId`: 請求ID
- `customerId`: 客戶ID
- `startDate`: 開始時間
- `endDate`: 結束時間
- `endpoint`: 外部系統端點
- `status`: 呼叫狀態

#### GET /api/v1/audit/database-queries
**功能**: 查詢資料庫操作記錄  
**權限**: ADMIN, AUDITOR  
**查詢參數**:
- `requestId`: 請求ID
- `customerId`: 客戶ID
- `startDate`: 開始時間
- `endDate`: 結束時間
- `queryType`: 查詢類型

#### GET /api/v1/audit/compliance-report
**功能**: 生成合規性報告  
**權限**: ADMIN, AUDITOR  
**查詢參數**:
- `startDate`: 報告開始時間 (必填)
- `endDate`: 報告結束時間 (必填)
- `customerId`: 特定客戶ID (可選)
- `reportType`: 報告類型 (SUMMARY, DETAILED)
- `format`: 輸出格式 (JSON, CSV)

### 系統監控相關 API

#### GET /actuator/health
**功能**: 系統健康檢查  
**權限**: 公開存取  
**回應格式**:
```json
{
  "status": "UP|DOWN",
  "components": {
    "db": {"status": "UP|DOWN"},
    "diskSpace": {"status": "UP|DOWN"},
    "ping": {"status": "UP|DOWN"}
  }
}
```

### API 回應狀態碼

| 狀態碼 | 說明 | 使用場景 |
|--------|------|----------|
| **200** | 成功 | 正常處理完成 |
| **201** | 已建立 | 資源建立成功 |
| **400** | 請求錯誤 | 請求參數驗證失敗 |
| **401** | 未授權 | 需要認證 |
| **403** | 禁止存取 | 權限不足 |
| **404** | 找不到資源 | 資源不存在 |
| **409** | 衝突 | 資源已存在或狀態衝突 |
| **422** | 無法處理的實體 | 業務邏輯驗證失敗 |
| **500** | 伺服器內部錯誤 | 系統異常 |

### 錯誤回應格式

所有 API 的錯誤回應都遵循統一格式：

```json
{
  "success": false,
  "data": null,
  "message": "錯誤描述訊息",
  "errorCode": "ERROR_CODE",
  "timestamp": "2024-01-15T10:30:00"
}
```

#### 常見錯誤代碼

| 錯誤代碼 | 描述 | HTTP 狀態碼 |
|---------|------|-------------|
| `INVALID_REQUEST` | 請求資料格式錯誤 | 400 |
| `VALIDATION_FAILED` | 資料驗證失敗 | 400 |
| `UNAUTHORIZED` | 未授權存取 | 401 |
| `FORBIDDEN` | 權限不足 | 403 |
| `RESOURCE_NOT_FOUND` | 資源不存在 | 404 |
| `DUPLICATE_RESOURCE` | 資源重複 | 409 |
| `BUSINESS_RULE_VIOLATION` | 業務規則違反 | 422 |
| `SYSTEM_ERROR` | 系統內部錯誤 | 500 |
| `EXTERNAL_SYSTEM_ERROR` | 外部系統錯誤 | 500 |
| `DATABASE_ERROR` | 資料庫操作錯誤 | 500 |

### 認證說明

系統使用 HTTP Basic Authentication，支援以下預設使用者：

| 使用者名稱 | 密碼 | 角色 | 權限 |
|-----------|------|------|------|
| `admin` | `admin123` | ADMIN, MANAGER, AUDITOR | 所有 API |
| `manager` | `manager123` | MANAGER | 管理 API |
| `auditor` | `auditor123` | AUDITOR | 稽核 API |

**認證方式:**
```bash
# 使用 curl 進行認證
curl -u admin:admin123 http://localhost:8080/api/v1/audit/trails

# 或在 Header 中加入 Authorization
curl -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" http://localhost:8080/api/v1/audit/trails
```

## 快速開始

### 1. 環境準備

```bash
# 檢查 Java 版本 (需要 JDK 17+)
java -version

# 檢查 Gradle 版本
./gradlew --version
```

### 2. 啟動應用程式

```bash
# 開發環境啟動 (使用 H2 記憶體資料庫)
./gradlew bootRun

# 或指定 profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 3. 驗證啟動

```bash
# 檢查應用程式健康狀態
curl http://localhost:8080/actuator/health

# 存取 Swagger UI
open http://localhost:8080/swagger-ui.html

# 存取 H2 資料庫 Console (開發環境)
open http://localhost:8080/h2-console
```

### 4. 執行測試

```bash
# 執行所有測試
./gradlew test

# 執行特定測試
./gradlew test --tests "*PromotionControllerTest"

# 生成測試報告
./gradlew test jacocoTestReport
```

### 5. 查看 H2 資料庫

開發環境使用 H2 記憶體資料庫，提供 Web Console 介面方便查看和操作資料。

#### 啟動應用程式
```bash
# 啟動開發環境
./gradlew bootRun

# 或指定開發 profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

#### 存取 H2 Console
1. **開啟瀏覽器**: http://localhost:8080/h2-console
2. **連線設定**:
   - **JDBC URL**: `jdbc:h2:mem:testdb`
   - **User Name**: `sa`
   - **Password**: (留空)
   - **Driver Class**: `org.h2.Driver`
3. **點擊 Connect** 連線到資料庫

#### H2 Console 功能

**查看資料表結構**:
```sql
-- 查看所有資料表
SHOW TABLES;

-- 查看特定資料表結構
DESCRIBE DECISION_TREES;
DESCRIBE PROMOTION_RULES;
DESCRIBE AUDIT_TRAILS;
```

**查詢資料**:
```sql
-- 查看決策樹配置
SELECT * FROM DECISION_TREES;

-- 查看優惠規則
SELECT * FROM PROMOTION_RULES;

-- 查看稽核軌跡
SELECT * FROM AUDIT_TRAILS ORDER BY CREATED_AT DESC LIMIT 10;

-- 查看請求日誌
SELECT * FROM REQUEST_LOGS ORDER BY CREATED_AT DESC LIMIT 10;

-- 查看決策步驟
SELECT * FROM DECISION_STEPS WHERE REQUEST_ID = 'your-request-id';
```

**測試資料操作**:
```sql
-- 插入測試決策樹
INSERT INTO DECISION_TREES (ID, NAME, STATUS, ROOT_NODE_ID, CREATED_AT, UPDATED_AT) 
VALUES ('test-tree-001', '測試決策樹', 'ACTIVE', 'root-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入測試優惠規則
INSERT INTO PROMOTION_RULES (ID, NAME, RULE_TYPE, RULE_CONTENT, STATUS, CREATED_AT, UPDATED_AT)
VALUES ('rule-001', '測試規則', 'SPEL', '#{annualIncome > 1000000}', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 查看資料表統計
SELECT 
    TABLE_NAME,
    ROW_COUNT_ESTIMATE as ROWS
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'PUBLIC'
ORDER BY TABLE_NAME;
```

#### 常用查詢範例

**稽核追蹤查詢**:
```sql
-- 查看特定客戶的稽核記錄
SELECT * FROM AUDIT_TRAILS 
WHERE CUSTOMER_ID = 'CUST001' 
ORDER BY CREATED_AT DESC;

-- 查看系統事件統計
SELECT EVENT_TYPE, COUNT(*) as COUNT 
FROM SYSTEM_EVENTS 
GROUP BY EVENT_TYPE 
ORDER BY COUNT DESC;

-- 查看決策樹執行統計
SELECT 
    dt.NAME as TREE_NAME,
    COUNT(ds.ID) as EXECUTION_COUNT,
    AVG(ds.EXECUTION_TIME_MS) as AVG_TIME_MS
FROM DECISION_TREES dt
LEFT JOIN DECISION_STEPS ds ON dt.ID = ds.TREE_ID
GROUP BY dt.ID, dt.NAME;
```

**效能分析查詢**:
```sql
-- 查看最慢的決策步驟
SELECT 
    TREE_ID,
    NODE_ID,
    NODE_TYPE,
    EXECUTION_TIME_MS,
    CREATED_AT
FROM DECISION_STEPS 
WHERE EXECUTION_TIME_MS > 100
ORDER BY EXECUTION_TIME_MS DESC
LIMIT 10;

-- 查看 API 回應時間統計
SELECT 
    API_ENDPOINT,
    COUNT(*) as REQUEST_COUNT,
    AVG(PROCESSING_TIME_MS) as AVG_TIME_MS,
    MAX(PROCESSING_TIME_MS) as MAX_TIME_MS
FROM REQUEST_LOGS 
GROUP BY API_ENDPOINT
ORDER BY AVG_TIME_MS DESC;
```

#### H2 資料庫特性

**優點**:
- 輕量級記憶體資料庫，啟動快速
- 內建 Web Console，方便開發和除錯
- 完全相容 SQL 標準
- 支援 PostgreSQL 相容模式

**注意事項**:
- 記憶體資料庫，應用程式重啟後資料會消失
- 適合開發和測試環境使用
- 生產環境建議使用 PostgreSQL

#### 資料持久化 (可選)

如需在開發時保留資料，可修改配置使用檔案資料庫：

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:h2:file:./data/testdb;AUTO_SERVER=TRUE
```

這樣資料會保存在 `./data/testdb.mv.db` 檔案中。

### H2 Console 使用指南

#### 基本操作步驟

1. **啟動應用程式**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

2. **開啟 H2 Console**
   - 瀏覽器存取: http://localhost:8080/h2-console
   - 會看到 H2 Console 登入頁面

3. **連線設定**
   ```
   Saved Settings: Generic H2 (Embedded)
   Setting Name: Generic H2 (Embedded)
   Driver Class: org.h2.Driver
   JDBC URL: jdbc:h2:mem:testdb
   User Name: sa
   Password: (留空)
   ```

4. **點擊 "Connect" 連線**

#### 資料表查看和操作

**查看所有資料表**:
```sql
-- 顯示所有資料表
SHOW TABLES;

-- 查看資料表詳細資訊
SELECT TABLE_NAME, TABLE_TYPE 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'PUBLIC'
ORDER BY TABLE_NAME;
```

**查看資料表結構**:
```sql
-- 查看決策樹資料表結構
DESCRIBE DECISION_TREES;

-- 查看優惠規則資料表結構  
DESCRIBE PROMOTION_RULES;

-- 查看稽核軌跡資料表結構
DESCRIBE AUDIT_TRAILS;

-- 查看所有欄位資訊
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'DECISION_TREES'
ORDER BY ORDINAL_POSITION;
```

**查詢業務資料**:
```sql
-- 查看決策樹配置
SELECT ID, NAME, STATUS, ROOT_NODE_ID, CREATED_AT 
FROM DECISION_TREES;

-- 查看決策節點配置
SELECT ID, TREE_ID, NODE_TYPE, PARENT_ID, CONFIGURATION 
FROM DECISION_NODES 
ORDER BY TREE_ID, PARENT_ID;

-- 查看優惠規則
SELECT ID, NAME, RULE_TYPE, STATUS, RULE_CONTENT, PARAMETERS 
FROM PROMOTION_RULES 
WHERE STATUS = 'ACTIVE';

-- 查看客戶優惠歷史
SELECT CUSTOMER_ID, PROMOTION_ID, PROMOTION_RESULT, EXECUTED_AT 
FROM PROMOTION_HISTORY 
ORDER BY EXECUTED_AT DESC 
LIMIT 10;
```

**稽核資料查詢**:
```sql
-- 查看最近的稽核軌跡
SELECT REQUEST_ID, CUSTOMER_ID, OPERATION_TYPE, STATUS, EXECUTION_TIME_MS, CREATED_AT
FROM AUDIT_TRAILS 
ORDER BY CREATED_AT DESC 
LIMIT 20;

-- 查看特定客戶的稽核記錄
SELECT * FROM AUDIT_TRAILS 
WHERE CUSTOMER_ID = 'CUST001' 
ORDER BY CREATED_AT DESC;

-- 查看 API 請求日誌
SELECT REQUEST_ID, API_ENDPOINT, HTTP_METHOD, RESPONSE_STATUS, PROCESSING_TIME_MS, CREATED_AT
FROM REQUEST_LOGS 
ORDER BY CREATED_AT DESC 
LIMIT 10;

-- 查看決策步驟詳情
SELECT ds.REQUEST_ID, ds.TREE_ID, ds.NODE_ID, ds.NODE_TYPE, 
       ds.STEP_ORDER, ds.EXECUTION_TIME_MS, ds.STATUS, ds.CREATED_AT
FROM DECISION_STEPS ds
ORDER BY ds.CREATED_AT DESC, ds.STEP_ORDER ASC
LIMIT 20;

-- 查看系統事件
SELECT EVENT_TYPE, EVENT_CATEGORY, SEVERITY_LEVEL, SOURCE_COMPONENT, CREATED_AT
FROM SYSTEM_EVENTS 
ORDER BY CREATED_AT DESC 
LIMIT 15;
```

**效能分析查詢**:
```sql
-- 查看最慢的 API 請求
SELECT API_ENDPOINT, REQUEST_ID, PROCESSING_TIME_MS, CREATED_AT
FROM REQUEST_LOGS 
WHERE PROCESSING_TIME_MS > 1000
ORDER BY PROCESSING_TIME_MS DESC;

-- 查看決策樹執行效能統計
SELECT 
    dt.NAME as TREE_NAME,
    COUNT(ds.ID) as EXECUTION_COUNT,
    AVG(ds.EXECUTION_TIME_MS) as AVG_TIME_MS,
    MAX(ds.EXECUTION_TIME_MS) as MAX_TIME_MS,
    MIN(ds.EXECUTION_TIME_MS) as MIN_TIME_MS
FROM DECISION_TREES dt
LEFT JOIN DECISION_STEPS ds ON dt.ID = ds.TREE_ID
GROUP BY dt.ID, dt.NAME
HAVING COUNT(ds.ID) > 0
ORDER BY AVG_TIME_MS DESC;

-- 查看節點類型執行統計
SELECT 
    NODE_TYPE,
    COUNT(*) as EXECUTION_COUNT,
    AVG(EXECUTION_TIME_MS) as AVG_TIME_MS
FROM DECISION_STEPS 
GROUP BY NODE_TYPE
ORDER BY AVG_TIME_MS DESC;
```

**資料維護操作**:
```sql
-- 清理舊的稽核資料 (測試用)
DELETE FROM AUDIT_TRAILS 
WHERE CREATED_AT < DATEADD('DAY', -7, CURRENT_TIMESTAMP);

-- 清理舊的請求日誌
DELETE FROM REQUEST_LOGS 
WHERE CREATED_AT < DATEADD('DAY', -30, CURRENT_TIMESTAMP);

-- 查看資料表大小統計
SELECT 
    TABLE_NAME,
    ROW_COUNT_ESTIMATE as ESTIMATED_ROWS
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'PUBLIC' 
    AND TABLE_TYPE = 'TABLE'
ORDER BY ROW_COUNT_ESTIMATE DESC;
```

#### 測試資料插入範例

**插入測試決策樹**:
```sql
-- 插入決策樹
INSERT INTO DECISION_TREES (ID, NAME, STATUS, ROOT_NODE_ID, CREATED_AT, UPDATED_AT) 
VALUES ('test-tree-001', 'VIP客戶決策樹', 'ACTIVE', 'root-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入根節點
INSERT INTO DECISION_NODES (ID, TREE_ID, NODE_TYPE, PARENT_ID, CONFIGURATION, CREATED_AT)
VALUES ('root-001', 'test-tree-001', 'CONDITION', NULL, 
        '{"commandType":"SPEL_CONDITION","expression":"#{accountType == ''VIP''}","description":"VIP客戶判斷"}', 
        CURRENT_TIMESTAMP);

-- 插入子節點
INSERT INTO DECISION_NODES (ID, TREE_ID, NODE_TYPE, PARENT_ID, CONFIGURATION, CREATED_AT)
VALUES ('calc-001', 'test-tree-001', 'CALCULATION', 'root-001',
        '{"commandType":"CALCULATION","strategyType":"TIERED_DISCOUNT","parameters":{"discountAmount":5000,"promotionName":"VIP專屬優惠"}}',
        CURRENT_TIMESTAMP);
```

**插入測試優惠規則**:
```sql
INSERT INTO PROMOTION_RULES (ID, NAME, RULE_TYPE, RULE_CONTENT, PARAMETERS, STATUS, CREATED_AT, UPDATED_AT)
VALUES 
('rule-001', 'VIP高收入客戶規則', 'SPEL', '#{annualIncome >= 2000000 and accountType == "VIP"}', 
 '{"discountPercentage": 10.0, "maxDiscount": 10000}', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('rule-002', '新戶開戶規則', 'SPEL', '#{transactionHistory.size() <= 3 and creditScore >= 650}',
 '{"discountAmount": 1000, "validDays": 30}', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

**插入測試稽核資料**:
```sql
INSERT INTO REQUEST_LOGS (ID, REQUEST_ID, API_ENDPOINT, HTTP_METHOD, REQUEST_PAYLOAD, 
                         RESPONSE_PAYLOAD, RESPONSE_STATUS, PROCESSING_TIME_MS, CREATED_AT, COMPLETED_AT)
VALUES ('log-001', 'req-001', '/api/v1/promotions/evaluate', 'POST',
        '{"customerId":"CUST001","accountType":"VIP","annualIncome":2000000}',
        '{"success":true,"data":{"promotionId":"promo-vip-001"}}',
        200, 150, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO AUDIT_TRAILS (ID, REQUEST_ID, CUSTOMER_ID, OPERATION_TYPE, OPERATION_DETAILS,
                         EXECUTION_TIME_MS, STATUS, CREATED_AT)
VALUES ('audit-001', 'req-001', 'CUST001', 'PROMOTION_EVALUATION',
        '{"treeId":"test-tree-001","result":"VIP專屬優惠","discountAmount":5000}',
        150, 'SUCCESS', CURRENT_TIMESTAMP);
```

#### H2 Console 進階功能

**匯出資料**:
```sql
-- 匯出資料表為 CSV
SELECT * FROM DECISION_TREES;
-- 然後點擊結果區域的 "CSV" 按鈕下載

-- 匯出為 SQL 腳本
SCRIPT TO 'backup.sql';
```

**執行計劃分析**:
```sql
-- 查看查詢執行計劃
EXPLAIN SELECT * FROM AUDIT_TRAILS WHERE CUSTOMER_ID = 'CUST001';

-- 分析複雜查詢效能
EXPLAIN ANALYZE 
SELECT dt.NAME, COUNT(ds.ID) as STEPS
FROM DECISION_TREES dt
LEFT JOIN DECISION_STEPS ds ON dt.ID = ds.TREE_ID
GROUP BY dt.ID, dt.NAME;
```

#### 故障排除

**常見問題**:

1. **無法連線到 H2 Console**
   - 確認應用程式已啟動且使用 `dev` profile
   - 檢查 URL: http://localhost:8080/h2-console
   - 確認 JDBC URL: `jdbc:h2:mem:testdb`

2. **資料表不存在**
   - 確認 Hibernate 已經執行 DDL
   - 檢查應用程式啟動日誌中的 CREATE TABLE 語句

3. **權限問題**
   - 確認安全配置已允許 `/h2-console/**` 端點
   - 檢查 X-Frame-Options 設定為 SAMEORIGIN

**除錯查詢**:
```sql
-- 檢查資料庫連線資訊
SELECT * FROM INFORMATION_SCHEMA.SESSIONS;

-- 檢查當前資料庫設定
SELECT * FROM INFORMATION_SCHEMA.SETTINGS;

-- 檢查記憶體使用情況
SELECT * FROM INFORMATION_SCHEMA.MEMORY_USAGE;
```

## API 使用範例

### 1. 優惠評估 API

#### 高價值客戶評估

```bash
curl -X POST http://localhost:8080/api/v1/promotions/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "accountType": "VIP",
    "annualIncome": 2000000,
    "creditScore": 750,
    "region": "台北市",
    "transactionCount": 50,
    "accountBalance": 500000,
    "transactionHistory": [
      {"amount": 10000, "type": "DEPOSIT", "date": "2024-01-15"},
      {"amount": 5000, "type": "TRANSFER", "date": "2024-01-20"}
    ]
  }'
```

**回應範例:**
```json
{
  "success": true,
  "data": {
    "promotionId": "promo-vip-001",
    "promotionName": "VIP專屬優惠",
    "promotionType": "VIP",
    "discountAmount": 5000,
    "discountPercentage": 10.0,
    "description": "VIP客戶專屬高額優惠",
    "validUntil": "2026-02-06T19:50:55.898193",
    "additionalDetails": {
      "tier": "VIP",
      "category": "premium"
    },
    "eligible": true
  },
  "message": "優惠評估完成",
  "errorCode": null,
  "timestamp": "2025-11-06T19:50:55.91632"
}
```

#### 一般客戶評估

```bash
curl -X POST http://localhost:8080/api/v1/promotions/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST002",
    "accountType": "一般",
    "annualIncome": 500000,
    "creditScore": 650,
    "region": "台中市",
    "transactionCount": 20,
    "accountBalance": 100000,
    "transactionHistory": []
  }'
```

### 2. 稽核查詢 API

#### 查詢特定請求的稽核軌跡

```bash
curl -u admin:admin123 \
  "http://localhost:8080/api/v1/audit/trails?requestId=req-123"
```

#### 查詢客戶的稽核記錄

```bash
curl -u auditor:auditor123 \
  "http://localhost:8080/api/v1/audit/trails?customerId=CUST001&startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59"
```

#### 查詢決策步驟

```bash
curl -u admin:admin123 \
  "http://localhost:8080/api/v1/audit/decisions?requestId=req-123"
```

### 3. 管理 API

#### 查詢決策樹列表

```bash
curl -u manager:manager123 \
  http://localhost:8080/api/v1/management/decision-trees
```

#### 建立新的優惠規則

```bash
curl -X POST http://localhost:8080/api/v1/management/promotion-rules \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "新戶開戶優惠",
    "ruleType": "SPEL",
    "ruleContent": "#{creditScore > 600 and accountType == \"一般\"}",
    "parameters": {
      "discountAmount": 1000,
      "validDays": 30
    },
    "status": "ACTIVE"
  }'
```

### 4. 錯誤處理範例

#### 無效請求

```bash
curl -X POST http://localhost:8080/api/v1/promotions/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "",
    "accountType": "VIP"
  }'
```

**錯誤回應:**
```json
{
  "success": false,
  "data": null,
  "message": "請求資料驗證失敗: 客戶ID不能為空",
  "errorCode": "INVALID_REQUEST",
  "timestamp": "2024-01-15T10:30:00"
}
```

#### 權限不足

```bash
curl http://localhost:8080/api/v1/audit/trails
```

**錯誤回應:**
```
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Basic realm="Realm"
```

### 5. 使用 Swagger UI 測試

1. **開啟 Swagger UI**: http://localhost:8080/swagger-ui.html
2. **選擇 API 分組**: 
   - 優惠評估
   - 稽核追蹤
3. **展開 API 端點**: 點擊想要測試的 API
4. **填入參數**: 使用提供的範例資料或自定義參數
5. **執行測試**: 點擊 "Try it out" 按鈕
6. **查看結果**: 檢查回應狀態碼和資料

#### Swagger UI 認證設定

1. 點擊右上角的 "Authorize" 按鈕
2. 選擇 "Bearer Authentication"
3. 輸入認證資訊:
   - Username: `admin`
   - Password: `admin123`
4. 點擊 "Authorize" 完成認證

## 開發指南

### 專案結構

```
src/
├── main/
│   ├── java/com/bank/promotion/
│   │   ├── adapter/           # 適配器層
│   │   │   ├── config/        # 配置類別
│   │   │   ├── persistence/   # 資料存取
│   │   │   └── web/           # Web 控制器
│   │   ├── application/       # 應用層
│   │   │   ├── command/       # 命令物件
│   │   │   ├── query/         # 查詢物件
│   │   │   └── service/       # 應用服務
│   │   └── domain/            # 領域層
│   │       ├── aggregate/     # 聚合根
│   │       ├── entity/        # 實體
│   │       ├── valueobject/   # 值物件
│   │       ├── state/         # 狀態模式
│   │       └── strategy/      # 策略模式
│   └── resources/
│       ├── application.yml    # 應用配置
│       ├── schema.sql         # 資料庫結構
│       └── features/          # BDD 測試場景
└── test/                      # 測試程式碼
```

### 開發流程

1. **需求分析**: 更新 requirements.md
2. **設計文檔**: 更新 design.md  
3. **任務規劃**: 更新 tasks.md
4. **TDD 開發**: 先寫測試，再寫實作
5. **BDD 驗證**: 撰寫 Cucumber 場景
6. **程式碼審查**: 確保符合架構原則
7. **整合測試**: 驗證端到端流程

### 程式碼規範

- **命名規範**: 使用有意義的英文命名
- **註解規範**: 重要類別和方法必須有 Javadoc
- **測試覆蓋**: 維持 90% 以上的測試覆蓋率
- **架構原則**: 遵循六角形架構和 DDD 原則
- **提交規範**: 使用中文提交訊息

### 環境配置

#### 開發環境 (dev)
- 資料庫: H2 記憶體資料庫
- H2 Console: http://localhost:8080/h2-console
- 日誌等級: DEBUG
- Swagger: 啟用
- SQL 日誌: 啟用

#### 測試環境 (sit/uat)
- 資料庫: PostgreSQL
- 日誌等級: INFO
- Swagger: 啟用

#### 生產環境 (prod)
- 資料庫: PostgreSQL
- 日誌等級: WARN
- Swagger: 停用

---

## 聯絡資訊

- **開發團隊**: dev-team@bank.com
- **專案網站**: https://bank.com
- **技術文檔**: 參考 `/docs` 目錄
- **問題回報**: 使用 GitHub Issues

## 授權條款

本專案採用 MIT License 授權條款。詳細內容請參考 [LICENSE](LICENSE) 檔案。