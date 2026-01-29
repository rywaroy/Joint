# 1.1 项目初始化

## 学习目标

- 使用 Spring Initializr 创建项目
- 理解 Maven pom.xml 配置
- 熟悉 Spring Boot 项目结构

## 概念讲解

### Spring Boot 是什么？

Spring Boot 是 Spring 框架的扩展，它简化了 Spring 应用的初始搭建和开发过程：
- **自动配置**：根据依赖自动配置 Spring 应用
- **起步依赖**：简化依赖管理，一个 starter 引入一组相关依赖
- **内嵌服务器**：内置 Tomcat，无需部署 WAR 包

### Maven 是什么？

Maven 是 Java 项目的构建和依赖管理工具：
- **pom.xml**：项目对象模型，定义项目信息和依赖
- **依赖管理**：自动下载和管理第三方库
- **生命周期**：compile → test → package → install → deploy

## 实践步骤

### 步骤 1：创建项目

访问 [Spring Initializr](https://start.spring.io/) 或使用 IDE：

```
Project: Maven
Language: Java
Spring Boot: 4.0.x (最新稳定版)
Group: org.joint
Artifact: joint
Name: joint
Package name: org.joint
Packaging: Jar
Java: 17
```

选择依赖：
- Spring Web
- Lombok
- MyBatis Framework
- MySQL Driver

### 步骤 2：理解项目结构

```
joint/
├── src/
│   ├── main/
│   │   ├── java/org/joint/
│   │   │   └── JointApplication.java   # 启动类
│   │   └── resources/
│   │       ├── application.properties  # 配置文件
│   │       ├── static/                  # 静态资源
│   │       └── templates/               # 模板文件
│   └── test/                            # 测试代码
├── pom.xml                              # Maven 配置
└── mvnw                                 # Maven Wrapper
```

### 步骤 3：理解 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 继承 Spring Boot 父 POM，统一管理依赖版本 -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.2</version>
    </parent>

    <!-- 项目坐标 -->
    <groupId>org</groupId>
    <artifactId>joint</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>joint</name>

    <!-- Java 版本 -->
    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Web 开发 starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>

        <!-- MyBatis：ORM 框架 -->
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>4.0.1</version>
        </dependency>

        <!-- MySQL 驱动 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok: 简化代码 -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

**关键概念：**
- `parent`：继承 Spring Boot 父 POM，统一管理所有 starter 的版本
- `starter`：起步依赖，如 `spring-boot-starter-webmvc` 包含了 Web MVC 开发所需的所有依赖
- `scope`：依赖范围，`runtime` 表示仅运行时需要（如 MySQL 驱动）

### 步骤 4：理解启动类

```java
package org.joint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // 组合注解，包含 @Configuration + @EnableAutoConfiguration + @ComponentScan
public class JointApplication {
    public static void main(String[] args) {
        SpringApplication.run(JointApplication.class, args);
    }
}
```

**@SpringBootApplication 包含：**
- `@Configuration`：标记为配置类
- `@EnableAutoConfiguration`：启用自动配置
- `@ComponentScan`：扫描当前包及子包的组件

### 步骤 5：配置 application.properties

```properties
# 应用名称
spring.application.name=joint

# 服务端口
server.port=8080

# 全局路径前缀，对应 Nexus 的 setGlobalPrefix('api')
server.servlet.context-path=/api

# MySQL 数据库连接
spring.datasource.url=jdbc:mysql://localhost:3306/nexus?serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=

# MyBatis 下划线转驼峰
mybatis.configuration.map-underscore-to-camel-case=true
```

### 步骤 6：启动项目

```bash
# 方式一：Maven 命令
./mvnw spring-boot:run

# 方式二：IDE 运行 JointApplication 的 main 方法

# 方式三：打包后运行
./mvnw package
java -jar target/joint-0.0.1-SNAPSHOT.jar
```

访问 http://localhost:8080/api 验证启动成功。

## 知识点总结

| 概念 | 说明 |
|------|------|
| Spring Boot | 简化 Spring 应用开发的框架 |
| Maven | Java 依赖管理和构建工具 |
| pom.xml | Maven 项目配置文件 |
| starter | 起步依赖，简化依赖引入 |
| @SpringBootApplication | 标记主启动类的组合注解 |

## 与 Nexus 对比

| Nexus | Joint |
|-------|-------|
| `package.json` | `pom.xml` |
| `pnpm install` | `./mvnw install` |
| `pnpm start:dev` | `./mvnw spring-boot:run` |
| `main.ts` | `JointApplication.java` |
| `setGlobalPrefix('api')` | `server.servlet.context-path=/api` |

## 练习任务

1. 使用 Spring Initializr 创建项目
2. 修改端口为 3000（与 Nexus 一致）
3. 启动项目并访问验证
