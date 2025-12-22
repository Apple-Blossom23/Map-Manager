# 后端启动指南

## 🎯 快速启动

### 1. 环境要求

- ✅ JDK 21（已配置）
- ✅ Maven 3.9.9（已配置）
- ✅ PostgreSQL 15+（远程数据库：154.21.90.113:5432）

### 2. 初始化数据库配置

在启动应用前，需要初始化系统配置表。连接到 PostgreSQL 数据库并执行：

```bash
psql -h 154.21.90.113 -p 5432 -U workshop -d workshop_db -f init_system_config.sql
```

或者使用 DBeaver/pgAdmin 等工具执行 `init_system_config.sql` 文件。

### 3. 启动应用

```bash
# 使用 Maven 启动（推荐）
mvn spring-boot:run -DskipTests -s .mvn\settings.xml

# 或者先打包再运行
mvn clean package -DskipTests -s .mvn\settings.xml
java -jar target\map-workshop-backend-1.0.0.jar
```

### 4. 验证启动

应用启动成功后，会在控制台看到：

```
Started WorkshopApplication in X.XXX seconds
```

访问：http://localhost:8080/api

## 📡 API 测试

### 使用 PowerShell 脚本测试

```powershell
.\test_api.ps1
```

### 手动测试注册接口

```powershell
$body = @{
    username = "testuser"
    nickname = "测试用户"
    email = "test@example.com"
    password = "123456"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" `
    -Method POST `
    -ContentType "application/json; charset=utf-8" `
    -Body ([System.Text.Encoding]::UTF8.GetBytes($body))
```

### 手动测试登录接口

```powershell
$body = @{
    usernameOrEmail = "testuser"
    password = "123456"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json; charset=utf-8" `
    -Body ([System.Text.Encoding]::UTF8.GetBytes($body))
```

## 🔧 配置说明

### 数据库配置

配置文件：`src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://154.21.90.113:5432/workshop_db
    username: workshop
    password: RFWXjbH325ifBsAG
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update  # 自动创建/更新表结构
```

### 端口配置

默认端口：8080  
API 前缀：/api

如需修改，编辑 `application.yml`：

```yaml
server:
  port: 8080
  servlet:
    context-path: /api
```

### JWT 配置

```yaml
jwt:
  secret: your-secret-key-change-this-in-production-min-256-bits
  expiration: 86400000  # 24小时（毫秒）
```

**⚠️ 生产环境请务必修改 JWT secret！**

## 🐛 常见问题

### 1. 端口被占用

**错误**：`Port 8080 was already in use`

**解决**：
```powershell
# 查找占用端口的进程
netstat -ano | findstr :8080

# 终止进程（替换 PID）
taskkill /F /PID <PID>
```

### 2. 数据库连接失败

**错误**：`Connection refused` 或 `timeout`

**检查**：
- 数据库服务器是否可访问
- 防火墙是否允许 5432 端口
- 用户名密码是否正确

### 3. 邮箱后缀不允许注册

**错误**：`该邮箱后缀不允许注册`

**解决**：执行 `init_system_config.sql` 初始化系统配置，或在数据库中手动添加允许的邮箱域名：

```sql
UPDATE system_configs 
SET value = 'gmail.com,qq.com,163.com,126.com,outlook.com,example.com,yourdomain.com'
WHERE key = 'allowed_email_domains';
```

### 4. Lombok 相关编译错误

**错误**：`找不到符号 getXxx()` 或 `setXxx()`

**原因**：Lombok 版本与 Java 版本不兼容

**解决**：
- 确保使用 JDK 21（不是 JDK 24）
- 或使用 Lombok edge-SNAPSHOT 版本（见 `pom.xml` 注释）

### 5. Maven 依赖下载慢

**解决**：已配置阿里云镜像（`.mvn/settings.xml`），如仍然很慢，检查网络连接。

## 📊 数据库表结构

应用启动后，Hibernate 会自动创建以下表：

- `users` - 用户表
- `maps` - 地图表
- `transactions` - 交易记录表
- `daily_task_logs` - 每日任务日志表
- `system_configs` - 系统配置表

查看表结构：

```sql
\dt  -- 列出所有表
\d users  -- 查看 users 表结构
```

## 🚀 生产部署

### 1. 打包

```bash
mvn clean package -DskipTests -s .mvn\settings.xml
```

生成文件：`target/map-workshop-backend-1.0.0.jar`

### 2. 运行

```bash
java -jar target/map-workshop-backend-1.0.0.jar
```

### 3. 使用环境变量（推荐）

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/workshop_db
export SPRING_DATASOURCE_USERNAME=your-username
export SPRING_DATASOURCE_PASSWORD=your-password
export JWT_SECRET=your-production-secret-key-min-256-bits

java -jar target/map-workshop-backend-1.0.0.jar
```

### 4. Docker 部署

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/map-workshop-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

构建并运行：

```bash
docker build -t map-workshop-backend .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/workshop_db \
  -e SPRING_DATASOURCE_USERNAME=workshop \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e JWT_SECRET=your-secret \
  map-workshop-backend
```

## 📝 开发建议

### 热重载

项目已配置 Spring Boot DevTools，修改代码后会自动重新加载，无需重启应用。

### 日志级别

开发环境日志级别（`application-dev.yml`）：

```yaml
logging:
  level:
    com.workshop: DEBUG
    org.springframework.security: DEBUG
```

生产环境建议改为 INFO 或 WARN。

### API 文档

建议集成 Swagger/OpenAPI：

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

访问：http://localhost:8080/api/swagger-ui.html

## 🔗 相关文档

- [前端对接指导](../../frontend_integration_guide.md)
- [后端设计文档](../../backend_design.md)
- [Java 版本配置指南](JAVA_VERSION_GUIDE.md)
- [数据库 Schema](../../schema_postgresql.sql)

## 📞 技术支持

如遇到问题，请检查：
1. 日志输出（控制台或日志文件）
2. 数据库连接状态
3. 系统配置是否正确初始化
4. JDK 版本是否为 21
