# Map Workshop Backend

基于 Java 21 + Spring Boot 3.2 + PostgreSQL 的地图工坊后端服务。

## 技术栈

- **Java 21** - LTS 版本
- **Spring Boot 3.2.1** - 企业级框架
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

服务默认将在 `http://localhost:8080` 启动，API路径默认为 `/api`。

## API 文档

### 认证接口

#### 注册
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "nickname": "测试用户",
  "email": "test@example.com",
  "password": "password123",
  "inviteCode": "ABC12345"
}
```

#### 登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "testuser",
  "password": "password123"
}
```

响应:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "userId": 1,
    "username": "testuser",
    "role": "USER"
  }
}
```

### 认证请求

后续请求需要在 Header 中携带 Token:

```http
Authorization: Bearer <your_token>
```

## 项目结构

```
src/main/java/com/workshop/
├── config/           # 配置类 (Security, CORS)
├── controller/       # REST 控制器
├── service/          # 业务逻辑层
├── repository/       # 数据访问层
├── entity/           # JPA 实体类
├── dto/              # 数据传输对象
├── security/         # 安全相关 (JWT)
├── exception/        # 异常处理
└── util/             # 工具类
```

## 开发进度

- [x] 项目初始化
- [x] 数据库设计 (PostgreSQL)
- [x] 用户认证 (注册/登录)
- [x] JWT 认证
- [x] 基础实体和仓库
- [x] 用户中心功能 (个人资料、修改密码、修改邮箱)
- [ ] 每日签到
- [ ] 等级系统
- [ ] 地图上传与管理
- [ ] 后台管理

## 许可证

MIT
