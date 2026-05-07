# "吃点啥" 校园餐饮平台 — UML 图表设计

---

## 一、用例图（Use Case Diagram）

### 1.1 学生用户用例图

```mermaid
graph LR
    Student((("学生用户")))

    subgraph 认证模块
        UC1[微信登录]
        UC2[修改密码]
        UC3[刷新Token]
        UC4[登出]
    end

    subgraph 用户模块
        UC5[查看个人信息]
        UC6[更新个人信息]
        UC7[设置偏好]
    end

    subgraph 浏览模块
        UC8[浏览商业区]
        UC9[浏览店铺]
        UC10[浏览菜品]
        UC11[搜索菜品]
        UC12[查看菜品详情]
        UC13[查看排行榜]
    end

    subgraph 互动模块
        UC14[发表评价]
        UC15[点赞评价]
        UC16[收藏菜品/帖子]
        UC17[取消收藏]
        UC18[添加想吃清单]
        UC19[标记已吃]
        UC20[提交反馈]
    end

    subgraph 帖子模块
        UC21[发布帖子]
        UC22[浏览帖子]
        UC23[点赞帖子]
        UC24[删除我的帖子]
    end

    subgraph 抽奖模块
        UC25[随机抽奖]
        UC26[条件抽奖]
        UC27[从收藏中抽奖]
        UC28[查看抽奖历史]
    end

    subgraph 商家入驻
        UC29[提交入驻申请]
    end

    subgraph 小法庭
        UC30[发起庭审]
        UC31[参与投票]
        UC32[查看庭审大厅]
        UC33[查看我的庭审]
    end

    Student --> UC1
    Student --> UC2
    Student --> UC3
    Student --> UC4
    Student --> UC5
    Student --> UC6
    Student --> UC7
    Student --> UC8
    Student --> UC9
    Student --> UC10
    Student --> UC11
    Student --> UC12
    Student --> UC13
    Student --> UC14
    Student --> UC15
    Student --> UC16
    Student --> UC17
    Student --> UC18
    Student --> UC19
    Student --> UC20
    Student --> UC21
    Student --> UC22
    Student --> UC23
    Student --> UC24
    Student --> UC25
    Student --> UC26
    Student --> UC27
    Student --> UC28
    Student --> UC29
    Student --> UC30
    Student --> UC31
    Student --> UC32
    Student --> UC33
```

---

### 1.2 商家用户用例图

```mermaid
graph LR
    Merchant((("商家用户")))

    subgraph 认证模块
        M1[微信登录]
        M2[查看入驻状态]
    end

    subgraph 菜品管理
        M3[新增菜品]
        M4[更新菜品]
        M5[上架/下架菜品]
        M6[删除菜品]
    end

    subgraph 推荐管理
        M7[发布推荐]
        M8[更新推荐]
        M9[删除推荐]
    end

    subgraph 反馈管理
        M10[查看反馈列表]
        M11[回复反馈]
        M12[更新反馈状态]
    end

    subgraph 改进管理
        M13[创建改进记录]
        M14[管理改进记录]
    end

    subgraph 数据看板
        M15[查看数据概览]
        M16[查看最受欢迎菜品]
        M17[查看反馈较多菜品]
    end

    subgraph 商家信息
        M18[更新商家信息]
    end

    subgraph 小法庭
        M19[发起庭审]
        M20[补充证据]
    end

    Merchant --> M1
    Merchant --> M2
    Merchant --> M3
    Merchant --> M4
    Merchant --> M5
    Merchant --> M6
    Merchant --> M7
    Merchant --> M8
    Merchant --> M9
    Merchant --> M10
    Merchant --> M11
    Merchant --> M12
    Merchant --> M13
    Merchant --> M14
    Merchant --> M15
    Merchant --> M16
    Merchant --> M17
    Merchant --> M18
    Merchant --> M19
    Merchant --> M20
```

---

### 1.3 管理员用例图

```mermaid
graph LR
    Admin((("管理员")))

    subgraph 认证模块
        A1[账号密码登录]
    end

    subgraph 用户管理
        A2[查看用户列表]
        A3[查看用户详情]
        A4[禁用用户]
        A5[恢复用户]
    end

    subgraph 商家审核
        A6[查看入驻申请]
        A7[通过入驻申请]
        A8[驳回入驻申请]
    end

    subgraph 内容管理
        A9[管理商业区]
        A10[管理店铺]
        A11[管理菜品状态]
        A12[管理评价]
    end

    subgraph 平台统计
        A13[查看平台统计]
        A14[查看热门排行]
        A15[查看趋势分析]
    end

    Admin --> A1
    Admin --> A2
    Admin --> A3
    Admin --> A4
    Admin --> A5
    Admin --> A6
    Admin --> A7
    Admin --> A8
    Admin --> A9
    Admin --> A10
    Admin --> A11
    Admin --> A12
    Admin --> A13
    Admin --> A14
    Admin --> A15
```

---

### 1.4 完整系统用例图（总览）

```mermaid
graph TB
    Student((("学生")))
    Merchant((("商家")))
    Admin((("管理员")))

    subgraph 认证与用户
        L1[微信登录]
        L2[账号密码登录]
        L3[修改密码]
        L4[个人信息管理]
        L5[偏好设置]
    end

    subgraph 商业区与店铺
        B1[浏览商业区]
        B2[浏览店铺]
        B3[管理商业区]
        B4[管理店铺]
    end

    subgraph 商家管理
        MGT1[商家入驻申请]
        MGT2[入驻审核]
        MGT3[商家信息管理]
        MGT4[数据看板]
    end

    subgraph 菜品管理
        D1[浏览/搜索菜品]
        D2[查看菜品详情]
        D3[管理菜品]
        D4[菜品状态管理]
        D5[发布推荐]
    end

    subgraph 互动与评价
        I1[发表评价]
        I2[点赞评价]
        I3[收藏]
        I4[想吃清单]
        I5[提交反馈]
        I6[回复反馈]
    end

    subgraph 学生帖子
        P1[发布帖子]
        P2[浏览帖子]
        P3[点赞帖子]
    end

    subgraph 抽奖系统
        LK1[抽奖]
        LK2[查看抽奖历史]
    end

    subgraph 排行榜
        R1[查看排行榜]
    end

    subgraph 小法庭
        CT1[发起庭审]
        CT2[参与投票]
        CT3[庭审大厅]
    end

    subgraph 管理员功能
        AD1[用户管理]
        AD2[内容管理]
        AD3[平台统计]
    end

    Student --> L1
    Student --> L3
    Student --> L4
    Student --> L5
    Student --> B1
    Student --> B2
    Student --> MGT1
    Student --> D1
    Student --> D2
    Student --> I1
    Student --> I2
    Student --> I3
    Student --> I4
    Student --> I5
    Student --> P1
    Student --> P2
    Student --> P3
    Student --> LK1
    Student --> LK2
    Student --> R1
    Student --> CT1
    Student --> CT2
    Student --> CT3

    Merchant --> L1
    Merchant --> MGT3
    Merchant --> MGT4
    Merchant --> D3
    Merchant --> D5
    Merchant --> I6
    Merchant --> CT1

    Admin --> L2
    Admin --> MGT2
    Admin --> B3
    Admin --> B4
    Admin --> D4
    Admin --> AD1
    Admin --> AD2
    Admin --> AD3
```

---

## 二、活动图（Activity Diagram）

### 2.1 学生微信登录活动图

```mermaid
flowchart TD
    Start([开始]) --> A[学生打开小程序]
    A --> B[点击微信授权登录]
    B --> C[微信获取 code]
    C --> D[发送 code 到后端]
    D --> E{code 有效?}
    E -- 否 --> F[返回错误提示]
    F --> G([结束])
    E -- 是 --> H[后端调用微信接口获取 openid]
    H --> I{用户是否存在?}
    I -- 否 --> J[创建新用户]
    J --> K[绑定 STUDENT 角色]
    K --> L[生成 JWT Token + Refresh Token]
    I -- 是 --> M{用户状态正常?}
    M -- 否 --> N[返回账号已禁用]
    N --> G
    M -- 是 --> L
    L --> O[返回 Token 和用户信息]
    O --> P{是否新用户?}
    P -- 是 --> Q[引导完善个人信息]
    Q --> G
    P -- 否 --> G
```

---

### 2.2 商家入驻申请活动图

```mermaid
flowchart TD
    Start([开始]) --> A[学生登录后进入入驻页面]
    A --> B[填写商家信息]
    B --> C[选择绑定店铺]
    C --> D[填写名称/描述/营业时间/电话]
    D --> E[提交入驻申请]
    E --> F[系统创建申请记录]
    F --> G[申请状态设为 PENDING]
    G --> H[通知管理员审核]
    H --> I([等待审核])

    subgraph 管理员审核流程
        J[管理员查看待审核列表] --> K{审核结果}
        K -- 通过 --> L[更新申请状态为 APPROVED]
        L --> M[授予用户 MERCHANT 角色]
        M --> N[商家状态设为 OPEN]
        N --> O[通知商家审核通过]
        K -- 驳回 --> P[填写驳回原因]
        P --> Q[更新申请状态为 REJECTED]
        Q --> R[通知商家审核驳回]
    end

    I --> J
    O --> End1([结束])
    R --> End2([结束])
```

---

### 2.3 菜品浏览与评价活动图

```mermaid
flowchart TD
    Start([开始]) --> A[学生进入菜品列表页]
    A --> B{选择筛选条件}
    B --> C[按商业区筛选]
    B --> D[按分类筛选]
    B --> E[按标签筛选]
    B --> F[按价格范围筛选]
    B --> G[关键词搜索]
    C --> H[查询菜品列表]
    D --> H
    E --> H
    F --> H
    G --> H
    H --> I[展示菜品列表]
    I --> J[学生点击菜品]
    J --> K[查看菜品详情]
    K --> L[浏览量 +1]
    L --> M{学生操作}

    M -- 收藏 --> N[添加收藏记录]
    N --> O[菜品收藏数 +1]
    O --> M

    M -- 加入想吃 --> P[添加到想吃清单]
    P --> M

    M -- 发表评价 --> Q[填写评分]
    Q --> R[填写口味/分量/性价比评分]
    R --> S[填写评价文字]
    S --> T{上传图片?}
    T -- 是 --> U[上传评价图片]
    U --> V[提交评价]
    T -- 否 --> V
    V --> W[创建评价记录]
    W --> X[更新菜品平均评分]
    X --> Y[更新商家平均评分]
    Y --> M

    M -- 提交反馈 --> Z[选择反馈类型]
    Z --> AA[填写反馈内容]
    AA --> AB[创建反馈记录]
    AB --> M

    M -- 返回 --> AC([结束])
```

---

### 2.4 抽奖活动图

```mermaid
flowchart TD
    Start([开始]) --> A[学生点击抽奖]
    A --> B{选择抽奖模式}

    B -- 随机抽奖 --> C[从抽奖池随机抽取]
    B -- 条件抽奖 --> D[输入筛选条件]
    D --> E[按条件过滤抽奖池]
    E --> C
    B -- 从收藏抽 --> F[获取用户收藏列表]
    F --> C

    C --> G[获取抽奖规则]
    G --> H[按规则权重抽取]
    H --> I[生成抽奖记录]
    I --> J[记录候选列表]
    J --> K[展示抽奖结果]
    K --> L{学生操作}

    L -- 接受 --> M[记录行为: ACCEPT]
    M --> N[加入想吃清单]
    N --> O([结束])

    L -- 跳过 --> P[记录行为: SKIP]
    P --> O

    L -- 收藏 --> Q[记录行为: FAVORITE]
    Q --> R[添加收藏]
    R --> O
```

---

### 2.5 反馈处理活动图

```mermaid
flowchart TD
    Start([开始]) --> A[学生提交反馈]
    A --> B[选择反馈类型]
    B --> C[填写反馈内容]
    C --> D[创建反馈记录]
    D --> E[状态设为 PENDING]
    E --> F[通知商家]

    F --> G[商家查看反馈列表]
    G --> H[商家查看反馈详情]
    H --> I[状态更新为 VIEWED]
    I --> J{商家处理决策}

    J -- 采纳 --> K[状态更新为 ACCEPTED]
    K --> L[商家回复反馈]
    L --> M[改进后状态更新为 IMPROVED]
    M --> N[发布改进记录]
    N --> O([结束])

    J -- 暂不处理 --> P[状态更新为 REJECTED]
    P --> Q[商家填写回复说明]
    Q --> O
```

---

### 2.6 小法庭庭审活动图

```mermaid
flowchart TD
    Start([开始]) --> A[用户发起庭审]
    A --> B[填写纠纷类型]
    B --> C[填写纠纷描述]
    C --> D[上传举证图片]
    D --> E[创建庭审案件]
    E --> F[状态设为 EVIDENCE]
    F --> G[进入举证阶段]

    G --> H{举证阶段}
    H -- 双方补充证据 --> I[提交补充证据]
    I --> H
    H -- 举证时间到 --> J[状态更新为 VOTING]
    J --> K[进入投票阶段]

    K --> L[庭审大厅展示案件]
    L --> M[用户浏览案件]
    M --> N[用户阅读双方陈述]
    N --> O{用户投票}
    O --> P[选择支持方]
    P --> Q{是否匿名?}
    Q -- 是 --> R[匿名投票]
    Q -- 否 --> S[公开投票]
    R --> T[记录投票]
    S --> T
    T --> U[更新得票统计]
    U --> V{投票时间到?}
    V -- 否 --> M
    V -- 是 --> W[状态更新为 CLOSED]
    W --> X[系统统计投票结果]
    X --> Y{结果判定}
    Y -- 买家票多 --> Z[结果: BUYER_WIN]
    Y -- 卖家票多 --> AA[结果: SELLER_WIN]
    Y -- 票数相同 --> AB[结果: DRAW]
    Z --> AC[公布庭审结果]
    AA --> AC
    AB --> AC
    AC --> AD([结束])
```

---

### 2.7 商家菜品管理活动图

```mermaid
flowchart TD
    Start([开始]) --> A[商家登录后台]
    A --> B{选择操作}

    B -- 新增菜品 --> C[填写菜品信息]
    C --> D[设置名称/描述/价格]
    D --> E[选择分类和标签]
    E --> F[上传菜品图片]
    F --> G[提交新增]
    G --> H[创建菜品记录]
    H --> I[状态设为 PENDING]
    I --> J[等待管理员审核]
    J --> K([结束])

    B -- 更新菜品 --> L[选择要修改的菜品]
    L --> M[修改菜品信息]
    M --> N[保存更新]
    N --> K

    B -- 上架/下架 --> O[选择菜品]
    O --> P{当前状态}
    P -- ON_SALE --> Q[设为 OFF_SHELF]
    P -- OFF_SHELF --> R[设为 ON_SALE]
    P -- SOLD_OUT --> R
    Q --> K
    R --> K

    B -- 发布推荐 --> S[选择推荐菜品]
    S --> T[填写推荐标题和理由]
    T --> U[选择推荐类型]
    U --> V[设置生效时间段]
    V --> W[是否置顶]
    W --> X[创建推荐记录]
    X --> K
```

---

### 2.8 管理员审核与管理活动图

```mermaid
flowchart TD
    Start([开始]) --> A[管理员登录]
    A --> B{选择功能}

    B -- 商家审核 --> C[查看待审核申请列表]
    C --> D[查看申请详情]
    D --> E{审核决策}
    E -- 通过 --> F[更新状态为 APPROVED]
    F --> G[授予 MERCHANT 角色]
    G --> H[通知商家]
    E -- 驳回 --> I[填写驳回原因]
    I --> J[更新状态为 REJECTED]
    J --> H
    H --> K([结束])

    B -- 用户管理 --> L[查看用户列表]
    L --> M{操作}
    M -- 查看详情 --> N[显示用户信息]
    N --> M
    M -- 禁用 --> O[更新状态为 DISABLED]
    O --> M
    M -- 恢复 --> P[更新状态为 ACTIVE]
    P --> M

    B -- 内容管理 --> Q[管理商业区/店铺/菜品]
    Q --> R{操作}
    R -- 新增 --> S[填写信息并创建]
    R -- 编辑 --> T[修改信息并保存]
    R -- 删除 --> U[确认删除]
    S --> K
    T --> K
    U --> K

    B -- 平台统计 --> V[查看统计数据]
    V --> W[查看用户/商家/菜品数量]
    W --> X[查看热门排行]
    X --> Y[查看趋势分析]
    Y --> K
```

---

### 2.9 学生发布帖子活动图

```mermaid
flowchart TD
    Start([开始]) --> A[学生点击发帖]
    A --> B[填写帖子标题]
    B --> C[填写食物名称]
    C --> D[填写店铺名称]
    D --> E[填写帖子内容]
    E --> F{可选信息}
    F --> G[选择商业区]
    F --> H[选择分类]
    F --> I[填写价格]
    F --> J[填写评分]
    F --> K[选择标签]
    G --> L[上传图片]
    H --> L
    I --> L
    J --> L
    K --> L
    L --> M{是否加入抽奖池?}
    M -- 是 --> N[标记加入抽奖池]
    M -- 否 --> O[提交帖子]
    N --> O
    O --> P[创建帖子记录]
    P --> Q[保存图片和标签关联]
    Q --> R{加入抽奖池?}
    R -- 是 --> S[同步到抽奖池]
    S --> T([结束])
    R -- 否 --> T
```

---

### 2.10 想吃清单活动图

```mermaid
flowchart TD
    Start([开始]) --> A[学生浏览菜品]
    A --> B[点击加入想吃]
    B --> C[添加备注信息]
    C --> D{来源?}
    D -- 直接添加 --> E[创建想吃记录]
    D -- 抽奖结果 --> F[关联抽奖记录]
    F --> E
    E --> G[状态设为 WANT_TO_EAT]
    G --> H([已加入清单])

    subgraph 管理想吃清单
        I[查看想吃清单] --> J{筛选状态}
        J -- 想吃 --> K[显示待吃列表]
        J -- 已吃 --> L[显示已吃列表]
        J -- 已取消 --> M[显示已取消列表]
    end

    subgraph 标记已吃
        N[选择想吃记录] --> O[更新备注]
        O --> P[记录食用时间]
        P --> Q[状态更新为 EATEN]
        Q --> R([完成])
    end

    subgraph 取消想吃
        S[选择想吃记录] --> T[状态更新为 CANCELLED]
        T --> U([完成])
    end
```

---

## 三、图表说明

### 3.1 用例图说明

| 角色 | 用例数量 | 核心功能 |
|------|----------|----------|
| 学生用户 | 33 | 登录、浏览、评价、收藏、抽奖、发帖、小法庭 |
| 商家用户 | 20 | 菜品管理、推荐管理、反馈处理、数据看板、小法庭 |
| 管理员 | 15 | 用户管理、商家审核、内容管理、平台统计 |

### 3.2 活动图说明

| 活动图 | 描述 |
|--------|------|
| 学生微信登录 | 完整的微信授权登录流程，含新用户判断 |
| 商家入驻申请 | 从提交申请到管理员审核的完整流程 |
| 菜品浏览与评价 | 学生浏览、收藏、评价、反馈的一体化流程 |
| 抽奖活动 | 三种抽奖模式及结果处理流程 |
| 反馈处理 | 学生提交到商家处理的完整闭环 |
| 小法庭庭审 | 举证、投票、判决的完整庭审流程 |
| 商家菜品管理 | 菜品增删改查和推荐发布流程 |
| 管理员审核与管理 | 管理员日常审核和管理操作流程 |
| 学生发布帖子 | 帖子创建和抽奖池同步流程 |
| 想吃清单 | 想吃清单的增删改查流程 |
