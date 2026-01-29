# 1.2 Spring Boot 核心概念

## 学习目标

- 理解 IoC（控制反转）和 DI（依赖注入）
- 掌握 Bean 的创建和生命周期
- 学会使用常用注解

## 概念讲解

### IoC（Inversion of Control）控制反转

传统方式：对象自己创建依赖
```java
public class UserService {
    private UserRepository userRepository = new UserRepository(); // 自己 new
}
```

IoC 方式：由容器创建并注入依赖
```java
public class UserService {
    private UserRepository userRepository; // 容器注入
}
```

**好处：** 解耦、易于测试、易于替换实现

### DI（Dependency Injection）依赖注入

DI 是 IoC 的实现方式，Spring 支持三种注入方式：

```java
// 1. 构造器注入（推荐）
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// 2. Setter 注入
@Service
public class UserService {
    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// 3. 字段注入（不推荐，但简洁）
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}
```

### Bean 是什么？

Bean 是由 Spring 容器管理的对象。Spring 容器（ApplicationContext）负责：
- 创建 Bean 实例
- 管理 Bean 生命周期
- 注入依赖关系

### 与 NestJS 对比

| NestJS | Spring Boot |
|--------|-------------|
| `@Injectable()` | `@Service` / `@Component` |
| `@Module({ providers: [...] })` | `@ComponentScan` 自动扫描 |
| 构造器注入 | 构造器注入 |
| Provider | Bean |

NestJS 的 `user.module.ts`:
```typescript
@Module({
    providers: [UserService],  // 需要显式声明
    exports: [UserService],
})
export class UserModule {}
```

Spring Boot:
```java
@Service  // 自动被扫描，无需显式注册
public class UserService {}
```

## 常用注解

### 组件注解（用于类）

| 注解 | 说明 | 语义 |
|------|------|------|
| `@Component` | 通用组件 | 任意 Bean |
| `@Service` | 业务逻辑层 | Service 类 |
| `@Repository` | 数据访问层 | DAO 类 |
| `@Controller` | Web 控制器 | 返回视图 |
| `@RestController` | REST 控制器 | 返回 JSON |
| `@Configuration` | 配置类 | 定义 Bean |

### 注入注解

| 注解 | 说明 |
|------|------|
| `@Autowired` | 按类型注入 |
| `@Qualifier("name")` | 指定 Bean 名称注入 |
| `@Value("${key}")` | 注入配置值 |

### 配置注解

| 注解 | 说明 |
|------|------|
| `@Configuration` | 标记配置类 |
| `@Bean` | 在配置类中定义 Bean |
| `@ConfigurationProperties` | 绑定配置文件 |

## 实践步骤

### 步骤 1：创建 Service

```java
package com.joint.modules.system.user;

import org.springframework.stereotype.Service;

@Service  // 标记为 Service Bean，会被 Spring 容器管理
public class UserService {

    public String hello() {
        return "Hello from UserService";
    }
}
```

### 步骤 2：在 Controller 中注入 Service

```java
package com.joint.modules.system.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor  // Lombok: 生成包含 final 字段的构造器
public class UserController {

    private final UserService userService;  // 构造器注入

    @GetMapping("/hello")
    public String hello() {
        return userService.hello();
    }
}
```

### 步骤 3：使用 @Value 注入配置

application.yml:
```yaml
app:
  name: Joint
  version: 1.0.0
```

```java
@Service
public class AppService {

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    public String getAppInfo() {
        return appName + " v" + appVersion;
    }
}
```

### 步骤 4：使用 @ConfigurationProperties（推荐）

```java
package com.joint.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String name;
    private String version;
}
```

使用：
```java
@Service
@RequiredArgsConstructor
public class AppService {

    private final AppProperties appProperties;

    public String getAppInfo() {
        return appProperties.getName() + " v" + appProperties.getVersion();
    }
}
```

### 步骤 5：使用 @Configuration 定义 Bean

```java
package com.joint.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    // 定义一个 Bean，名称默认为方法名
    @Bean
    public SomeThirdPartyService someService() {
        SomeThirdPartyService service = new SomeThirdPartyService();
        service.setConfig("custom config");
        return service;
    }
}
```

## Bean 生命周期

```
1. 实例化 Bean
   ↓
2. 属性赋值（依赖注入）
   ↓
3. 调用 @PostConstruct 方法
   ↓
4. Bean 可用
   ↓
5. 调用 @PreDestroy 方法（容器关闭时）
   ↓
6. 销毁 Bean
```

```java
@Service
public class UserService {

    @PostConstruct
    public void init() {
        System.out.println("UserService 初始化完成");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("UserService 即将销毁");
    }
}
```

## 知识点总结

| 概念 | 说明 |
|------|------|
| IoC | 控制反转，由容器管理对象创建 |
| DI | 依赖注入，容器自动注入依赖 |
| Bean | Spring 容器管理的对象 |
| @Service | 标记业务逻辑层组件 |
| @Autowired | 自动注入依赖 |
| @Value | 注入配置值 |

## 练习任务

1. 创建 UserService 和 UserController
2. 在配置文件中定义 app.name，并在 Service 中读取
3. 使用 @PostConstruct 打印初始化日志
