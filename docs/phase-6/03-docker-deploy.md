# 6.3 Docker 部署

## 学习目标

- 创建 Dockerfile
- 使用 docker-compose 编排服务
- 配置多环境

## Dockerfile

```dockerfile
# 多阶段构建

# 阶段一：构建
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制 pom.xml 并下载依赖（利用缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并构建
COPY src ./src
RUN mvn package -DskipTests -B

# 阶段二：运行
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 从构建阶段复制 jar
COPY --from=builder /app/target/*.jar app.jar

# 设置权限
RUN chown -R appuser:appgroup /app
USER appuser

# 暴露端口
EXPOSE 8080

# JVM 参数
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

## docker-compose.yml

```yaml
version: '3.8'

services:
  # 应用服务
  joint:
    build: .
    container_name: joint-app
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/joint?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=${MYSQL_ROOT_PASSWORD}
      - SPRING_DATA_REDIS_HOST=redis
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_started
    networks:
      - joint-network
    restart: unless-stopped

  # MySQL 数据库
  mysql:
    image: mysql:8.0
    container_name: joint-mysql
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
      - MYSQL_DATABASE=joint
    volumes:
      - mysql-data:/var/lib/mysql
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"
    networks:
      - joint-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  # Redis 缓存
  redis:
    image: redis:7-alpine
    container_name: joint-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - joint-network
    restart: unless-stopped

networks:
  joint-network:
    driver: bridge

volumes:
  mysql-data:
  redis-data:
```

## 环境变量文件 (.env)

```bash
# .env
MYSQL_ROOT_PASSWORD=your_secure_password
JWT_SECRET=your_256_bit_secret_key_at_least_32_characters
```

## 多环境配置

### application.yml

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
```

### application-dev.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/joint
    username: root
    password: root
  data:
    redis:
      host: localhost

logging:
  level:
    com.joint: DEBUG
    org.springframework.security: DEBUG
```

### application-prod.yml

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  data:
    redis:
      host: ${SPRING_DATA_REDIS_HOST}

logging:
  level:
    com.joint: INFO
```

## 部署命令

```bash
# 构建镜像
docker-compose build

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f joint

# 停止服务
docker-compose down

# 停止并删除数据
docker-compose down -v
```

## 生产优化

### Dockerfile 优化

```dockerfile
FROM eclipse-temurin:17-jre-alpine

# 安全优化
RUN apk add --no-cache dumb-init

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# 使用 dumb-init 处理信号
ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["java", "-jar", "app.jar"]
```

### JVM 参数优化

```yaml
# docker-compose.yml
environment:
  - JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

## 知识点总结

| 概念 | 说明 |
|------|------|
| 多阶段构建 | 减小镜像体积 |
| docker-compose | 服务编排 |
| healthcheck | 健康检查 |
| volumes | 数据持久化 |

## 练习任务

1. 创建 Dockerfile
2. 编写 docker-compose.yml
3. 配置多环境
4. 本地测试容器化部署
