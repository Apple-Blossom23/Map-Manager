# Map Workshop Backend

基于 Java 21 + Spring Boot 3.5.9 + PostgreSQL 的地图工坊后端服务。

## 技术栈

- **Java 21** - LTS 版本
- **Spring Boot 3.5.9** - 企业级框架
- **PostgreSQL 15+** - 关系型数据库
- **Spring Data JPA** - ORM 框架
- **Spring Security + JWT** - 认证授权
- **Maven** - 构建工具

## 快速开始

### 前置要求

- JDK 21
- PostgreSQL 15+
- Maven 3.9+

### 数据库初始化

```bash
# 创建数据库并执行 schema
psql -U postgres
\i schema_postgresql.sql
```

### 环境变量配置

复制环境变量示例文件并重命名：

```bash
# 复制示例文件
cp .env.example .env

# 编辑 .env 文件，修改数据库连接等配置
```

### 运行项目

```bash
# 安装依赖
mvn clean install

# 确保 .env 文件已配置完成
# 然后运行开发环境
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/map-workshop-backend-1.0.0.jar --spring.profiles.active=dev
```

服务默认将在 `http://localhost:8080` 启动，API路径默认为 `/api`


## 项目结构

```
├── pom.xml                                   # Maven 依赖配置
├── src/main/
│   ├── java/com/workshop/
│   │   ├── WorkshopApplication.java          # Spring Boot 启动类
│   │   ├── config/
│   │   │   └── SecurityConfig.java           # Spring Security + CORS 配置
│   │   ├── controller/
│   │   │   └── AuthController.java           # 认证控制器 (注册/登录)
│   │   ├── service/
│   │   │   ├── AuthService.java              # 认证业务逻辑
│   │   │   ├── TransactionService.java       # 经济系统服务
│   │   │   └── SystemConfigService.java      # 系统配置服务
│   │   ├── repository/
│   │   │   ├── UserRepository.java           # 用户数据访问
│   │   │   ├── MapRepository.java            # 地图数据访问
│   │   │   ├── TransactionRepository.java    # 交易记录访问
│   │   │   ├── DailyTaskLogRepository.java   # 每日任务访问
│   │   │   └── SystemConfigRepository.java   # 系统配置访问
│   │   ├── entity/
│   │   │   ├── User.java                     # 用户实体
│   │   │   ├── Map.java                      # 地图实体
│   │   │   ├── Transaction.java              # 交易记录实体
│   │   │   ├── DailyTaskLog.java             # 每日任务实体
│   │   │   └── SystemConfig.java             # 系统配置实体
│   │   ├── dto/
│   │   │   ├── ApiResponse.java              # 统一响应格式
│   │   │   └── auth/
│   │   │       ├── RegisterRequest.java      # 注册请求 DTO
│   │   │       ├── LoginRequest.java         # 登录请求 DTO
│   │   │       └── AuthResponse.java         # 认证响应 DTO
│   │   ├── security/
│   │   │   ├── JwtTokenProvider.java         # JWT Token 生成/验证
│   │   │   └── JwtAuthenticationFilter.java  # JWT 认证过滤器
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java   # 全局异常处理
│   │   │   └── BusinessException.java        # 业务异常类
│   │   └── util/
│   │       └── InviteCodeGenerator.java      # 邀请码生成工具
│   └── resources/
│       ├── application.yml                   # 主配置文件
│       ├── application-dev.yml               # 开发环境配置
│       └── application-prod.yml              # 生产环境配置
└── README.md                                 # 项目文档
```


## 功能模块详细设计

### 用户与认证模块 (User & Auth)
- **注册**:
  - 校验注册开关 (`system_configs`)
  - 校验邮箱后缀白名单 (`system_configs`)
  - 校验同IP单日注册限制
  - 生成唯一 `invite_code`
  - 密码 bcrypt 加密
- **登录**: JWT 签发 token
- **个人中心**:
  - 编辑资料 (昵称, 头像, 简介) - 用户名不可改
  - 安全中心 (修改邮箱/密码, 注销申请)
  - 邀请码逻辑: 填写邀请码，双方获利 (事务处理: 更新 `users` 表的 drops/lightning, 记录 `transactions`)

### 等级与考试模块 (Level & Exam)
- **等级系统**:
  - 经验值(闪电)阈值判定
  - Lv0 -> Lv1 考试逻辑
- **考试**:
  - 获取随机题目 (从 `exam_questions` 表)
  - 提交答案 -> 计算分数 -> 若 >= 60 -> 更新等级 -> 记录 `exam_attempts`

### 签到与任务系统 (Checked & Tasks
- **每日签到**:
  - 检查 `daily_task_logs.is_checked_in`
  - 随机算法 -> 更新 `users.drops` -> 记录 `transactions` -> 更新 `daily_task_logs`
- **每日任务**:
  - 监听相关动作 (登录, 浏览, 点赞, 打赏)
  - 检查 `daily_task_logs` 对应计数
  - 若未达上限 -> 发放奖励 -> 更新计数与资产 -> 记录日志

### 地图创作与管理 (Map & Creator)
- **创作者申请**:
  - 校验条件 (Lv1+, 注册>3天, 邮箱验证) -> 插入 `creator_applications`
- **地图上传**:
  - 校验角色 (creator/admin)
  - 原创/转载区分校验
  - 文件上传至存储 -> 记录 `maps` 表 (status='pending')
- **地图互动**:
  - **浏览**: 计数 + 触发每日任务
  - **点赞**: 唯一性校验 (`map_likes`) + 触发作者奖励 + 触发每日任务
  - **收藏**: `map_favorites`
  - **下载**:
    - 检查 `map_downloads` (当日是否已买)
    - 若未买 -> 扣除用户水滴 -> 记录 `map_downloads` -> 记录 `transactions`
    - 返回下载链接
  - **打赏**: 扣除水滴 -> 增加作者水滴 (扣税/分成逻辑) -> 记录 `map_donations`

### 后台管理 (Admin)
- **基础管理**:
  - 用户管理 (封禁/解封, 修改角色, 打标签)
  - 系统配置 (`system_configs`: 注册开关, 邮箱后缀, IP限制等)
  - SMTP配置 (`smtp_configs`: 轮询逻辑)
- **审核**:
  - 地图审核 (通过/驳回)
  - 举报处理 (核实/无违规) -> 若核实则奖励举报者

## 数据库交互说明
使用 Spring Data JPA + Hibernate 进行 ORM 操作。
- 将现有 MySQL schema.sql 迁移为 PostgreSQL 语法
- 使用 JPA 注解定义实体类映射
- 复杂事务 (如打赏、下载扣费) 使用 `@Transactional` 注解保证数据一致性
- 使用 PostgreSQL 特性: JSONB类型、序列、索引优化

## API设计规范

### RESTful风格
- **用户认证**: `/api/auth/*` (register, login, logout)
- **用户中心**: `/api/user/*` (profile, security, tasks, favorites)
- **地图管理**: `/api/maps/*` (upload, list, detail, download)
- **创作中心**: `/api/creator/*` (apply, manage)
- **后台管理**: `/api/admin/*` (users, maps, reports, system)

### 安全机制
- JWT Token 认证 (Header: `Authorization: Bearer <token>`)
- 角色权限控制 (USER, CREATOR, ADMIN)
- 接口限流 (防止恶意请求)
- CORS 配置 (允许前端跨域访问)

## 开发进度
以下功能已设计但尚未实现，需要继续开发：

### 用户中心模块
- [x] 个人资料编辑 (昵称、头像、简介)
- [x] 查看个人信息
- [x] 修改邮箱
- [x] 修改密码
- [ ] 申请注销账号
- [ ] 查看我的收藏
- [x] 查看我的等级和经验 (LV0-LV20，具体如下)
- `新注册(经验0)` - LV0
- `通过入站考试(经验0-199)` - LV1
- `经验200-499` - LV2
- `经验500-799` - LV3
- `经验800-1299` - LV4
- `经验1300-1799` - LV5
- `经验1800-2799` - LV6
- `经验2800-3999` - LV7
- `经验4000-4999` - LV8
- `经验5000-6999` - LV9
- `经验7000-8999` - LV10
- `经验9000-11999` - LV11
- `经验12000-14999` - LV12
- `经验15000-17999` - LV13
- `经验18000-22999` - LV14
- `经验23000-27999` - LV15
- `经验28000-34999` - LV16
- `经验35000-41999` - LV17
- `经验42000-49999` - LV18
- `经验50000-59999` - LV19
- `经验60000-` - LV20
- [ ] 查看邀请记录
- [ ] 上传支付宝、微信收款码 or 爱发电链接 (仅创作者可用，已上传的情况下会展示在个人主页)

### 每日签到与任务
- [x] 每日签到 (随机水滴，1-15，正态分布)
- [ ] 每日登录奖励 (10闪电)
- [ ] 每日浏览地图奖励 (1闪电/张，上限5张)
- [ ] 每日点赞奖励 (2闪电/张，上限5张)
- [ ] 每日打赏奖励 (5闪电/滴，上限5滴)
- [ ] 查看任务进度

### 等级与考试
- [ ] 获取考试题目 (随机50题，每题2分，60分及格，每账号每天有3次机会)
- [ ] 提交考试答案
- [ ] 查看考试历史
- [ ] 题库管理 (仅管理员可用)

### 地图管理
- [ ] 地图列表 (分页、筛选、排序)
- [ ] 地图详情
- [ ] 地图上传 (原创/转载)
- [ ] 地图编辑
- [ ] 地图删除
- [ ] 地图浏览计数
- [ ] 地图点赞/取消点赞
- [ ] 地图收藏/取消收藏
- [ ] 地图下载 (扣费逻辑)
- [ ] 地图打赏
- [ ] 我的地图管理

### 创作者功能
- [ ] 申请成为创作者
- [ ] 查看申请状态
- [ ] 创作者地图管理
- [ ] 收益统计

### 后台管理
- [ ] 用户管理 (列表、封禁、解封、修改角色、打标签)
- [ ] 地图审核 (通过/驳回)
- [ ] 举报处理
- [ ] 系统配置管理
- [ ] SMTP 配置管理 (多SMTP轮询)
- [ ] 公告管理 (全站公告功能)
- [ ] 通知管理 (可向某个用户、某个用户组、全站用户发送通知)
- [ ] 数据统计
- [ ] 云存储可用区管理 (支持阿里云OSS、腾讯云COS、华为云OBS、MinIO，模式按私有存储桶开发，添加至后台方便增减)

### 其他功能
- [ ] 文件上传服务 (地图文件格式MAP、图片格式JPG/PNG)
- [ ] 邮件发送服务 (验证码、通知)
- [ ] 定时任务 (每日重置、SMTP 计数重置)
- [ ] 举报功能
- [ ] 通知系统
