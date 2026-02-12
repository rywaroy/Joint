# 6.2 集成测试

## 学习目标

- 使用 @SpringBootTest 编写集成测试
- 使用 MockMvc 测试 Controller 层
- 使用 TestContainers 管理测试数据库

## 与 6.1 单元测试的区别

| 维度 | 单元测试 | 集成测试 |
|------|---------|---------|
| 范围 | 单个类/方法 | 多层协作 |
| 依赖 | Mock 所有依赖 | 真实 Spring 上下文 |
| 数据库 | 不涉及 | 真实数据库 |
| 速度 | 快 | 较慢 |
| 注解 | `@ExtendWith(MockitoExtension.class)` | `@SpringBootTest` |

## 与 Nexus 对照

Nexus 使用 `supertest` 进行 E2E 测试：

```typescript
const moduleFixture = await Test.createTestingModule({
    imports: [AppModule],
}).compile();

app = moduleFixture.createNestApplication();

request(app.getHttpServer())
    .get('/')
    .expect(200);
```

## 实践步骤

### 步骤 1：添加依赖

```xml
<!-- spring-boot-starter-test 已包含基础依赖 -->

<!-- TestContainers（可选，用于数据库容器） -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <version>1.19.8</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.19.8</version>
    <scope>test</scope>
</dependency>
```

### 步骤 2：测试配置文件

```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    # 使用 H2 内存数据库（简单方案）
    url: jdbc:h2:mem:testdb;MODE=MySQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  data:
    redis:
      host: localhost
      port: 6379

# 或使用 TestContainers（见步骤 6）
```

### 步骤 3：MockMvc 测试 Controller

```java
package com.joint.modules.system.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joint.modules.system.user.dto.CreateUserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("用户接口集成测试")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("未登录访问接口 - 返回 401")
    void accessWithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/system/user/list"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("创建用户 - 参数校验失败")
    @WithMockJwt(username = "admin", permissions = {"system:user:add"})
    void createUser_ValidationFail() throws Exception {
        CreateUserDto dto = new CreateUserDto();
        // 不设置必填字段

        mockMvc.perform(post("/system/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("创建用户 - 成功")
    @WithMockJwt(username = "admin", permissions = {"system:user:add"})
    void createUser_Success() throws Exception {
        CreateUserDto dto = new CreateUserDto();
        dto.setUsername("testuser");
        dto.setPassword("123456");
        dto.setNickName("测试用户");

        mockMvc.perform(post("/system/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("获取用户列表 - 分页")
    @WithMockJwt(username = "admin", permissions = {"system:user:list"})
    void listUsers_Paginated() throws Exception {
        mockMvc.perform(get("/system/user/list")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").isNumber());
    }
}
```

### 步骤 4：模拟 JWT 认证

创建测试用的认证注解和处理器：

```java
// 自定义测试注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithMockJwt {
    String username() default "admin";
    String userId() default "1";
    String[] permissions() default {};
}

// 测试安全上下文处理器
public class MockJwtSecurityContextFactory
        implements WithSecurityContextFactory<WithMockJwt> {

    @Override
    public SecurityContext createSecurityContext(WithMockJwt annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(annotation.userId());
        loginUser.setUsername(annotation.username());
        loginUser.setPermissions(Set.of(annotation.permissions()));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        loginUser, null, loginUser.getAuthorities());

        context.setAuthentication(auth);
        return context;
    }
}
```

更新注解引用：

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = MockJwtSecurityContextFactory.class)
public @interface WithMockJwt {
    String username() default "admin";
    String userId() default "1";
    String[] permissions() default {};
}
```

### 步骤 5：数据清理

```java
/**
 * 测试基类，每个测试后清理数据
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @AfterEach
    void cleanUp() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // 按外键依赖顺序清理
            stmt.execute("DELETE FROM sys_user_role");
            stmt.execute("DELETE FROM sys_role_menu");
            stmt.execute("DELETE FROM sys_user_post");
            stmt.execute("DELETE FROM sys_oper_log");
            stmt.execute("DELETE FROM sys_user WHERE username != 'admin'");
            stmt.execute("DELETE FROM sys_role WHERE is_builtin != 1");
            stmt.execute("DELETE FROM sys_menu");
            stmt.execute("DELETE FROM sys_dept");
            stmt.execute("DELETE FROM sys_post");
        }
    }
}
```

### 步骤 6：使用 TestContainers（进阶）

```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class UserControllerTestContainersTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("joint_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testWithRealDatabase() throws Exception {
        // 使用真实 MySQL 容器测试
        mockMvc.perform(get("/system/user/list"))
                .andExpect(status().isUnauthorized());
    }
}
```

### 步骤 7：运行测试

```bash
# 运行所有测试
./mvnw test

# 仅运行集成测试
./mvnw test -Dtest="*IntegrationTest"

# 使用指定 profile
./mvnw test -Dspring.profiles.active=test

# 生成覆盖率报告
./mvnw test jacoco:report
```

## 常用 MockMvc 断言

```java
// 状态码
.andExpect(status().isOk())
.andExpect(status().isUnauthorized())
.andExpect(status().isBadRequest())

// JSON 路径
.andExpect(jsonPath("$.code").value(200))
.andExpect(jsonPath("$.data.username").value("admin"))
.andExpect(jsonPath("$.data.list").isArray())
.andExpect(jsonPath("$.data.list", hasSize(10)))
.andExpect(jsonPath("$.data.total").isNumber())

// 响应头
.andExpect(header().string("Content-Type", "application/json"))

// 打印请求/响应（调试用）
.andDo(print())
```

## 知识点总结

| 注解/工具 | 说明 |
|-----------|------|
| `@SpringBootTest` | 启动完整 Spring 上下文 |
| `@AutoConfigureMockMvc` | 注入 MockMvc |
| `@ActiveProfiles("test")` | 使用测试环境配置 |
| `MockMvc` | 模拟 HTTP 请求 |
| `@WithSecurityContext` | 自定义安全上下文 |
| `@Testcontainers` | 管理 Docker 容器 |
| `@DynamicPropertySource` | 动态注入配置 |

## 练习任务

1. 为用户 CRUD 接口编写集成测试
2. 测试权限拦截（无权限返回 403）
3. 测试参数校验（非法参数返回 400）
4. （可选）使用 TestContainers 替代 H2
