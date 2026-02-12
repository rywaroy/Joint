# 5.4 Swagger 文档

## 学习目标

- 使用 SpringDoc 生成 API 文档
- 掌握常用文档注解
- 对照 Nexus 的 Swagger 配置

## 与 Nexus 对照

Nexus 使用 `@nestjs/swagger`：

```typescript
const options = new DocumentBuilder()
    .setTitle('标题')
    .setDescription('描述')
    .setVersion('1.0')
    .build();
const document = SwaggerModule.createDocument(app, options);
SwaggerModule.setup('api/v1/swagger', app, document);
```

## 实践步骤

### 步骤 1：添加依赖

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

### 步骤 2：配置 SpringDoc

```java
package com.joint.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Joint API")
                        .description("Joint 后台管理系统接口文档")
                        .version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new Components()
                        .addSecuritySchemes("Bearer",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("输入 JWT Token")));
    }
}
```

配置文件：

```yaml
# application.yml
springdoc:
  api-docs:
    path: /api/v1/api-docs     # OpenAPI JSON 路径
  swagger-ui:
    path: /api/v1/swagger       # Swagger UI 路径
    tags-sorter: alpha
    operations-sorter: method
```

### 步骤 3：在 Security 中放行 Swagger 路径

```java
// SecurityConfig.java 中放行以下路径
.requestMatchers(
    "/api/v1/swagger/**",
    "/api/v1/api-docs/**",
    "/swagger-ui/**",
    "/v3/api-docs/**"
).permitAll()
```

### 步骤 4：Controller 注解

```java
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@Tag(name = "用户管理")                     // 接口分组
public class UserController {

    @GetMapping("/list")
    @Operation(summary = "获取用户列表",      // 接口描述
               description = "支持分页和条件筛选")
    public PageResult<UserVo> list(QueryUserDto query) {
        return userService.findPage(query);
    }

    @PostMapping
    @Operation(summary = "创建用户")
    public UserVo create(@Valid @RequestBody CreateUserDto dto) {
        return userService.create(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @Parameter(name = "id", description = "用户 ID", required = true)
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }
}
```

### 步骤 5：DTO 注解

```java
@Data
@Schema(description = "创建用户请求")
public class CreateUserDto {

    @Schema(description = "用户名", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "昵称", example = "张三")
    private String nickName;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "角色 ID 列表")
    private List<String> roleIds;
}
```

### 步骤 6：统一响应文档

```java
@Data
@Schema(description = "统一响应")
public class R<T> {

    @Schema(description = "状态码", example = "200")
    private Integer code;

    @Schema(description = "提示信息", example = "操作成功")
    private String message;

    @Schema(description = "响应数据")
    private T data;
}

@Data
@Schema(description = "分页结果")
public class PageResult<T> {

    @Schema(description = "数据列表")
    private List<T> list;

    @Schema(description = "总数", example = "100")
    private Long total;
}
```

### 步骤 7：文件上传文档

```java
@PostMapping("/upload")
@Operation(summary = "单文件上传")
@io.swagger.v3.oas.annotations.parameters.RequestBody(
    content = @Content(mediaType = "multipart/form-data",
        schema = @Schema(type = "object",
            requiredProperties = {"file"},
            properties = @StringToClassMapItem(
                key = "file",
                value = String.class
            )))
)
public FileInfo upload(@RequestParam("file") MultipartFile file) {
    // ...
}
```

## 常用注解速查

### Controller 层

| 注解 | 位置 | 说明 |
|------|------|------|
| `@Tag(name = "...")` | 类 | 接口分组 |
| `@Operation(summary = "...")` | 方法 | 接口描述 |
| `@Parameter(name, description)` | 参数/方法 | 路径/查询参数说明 |
| `@Hidden` | 类/方法 | 隐藏接口 |

### DTO 层

| 注解 | 位置 | 说明 |
|------|------|------|
| `@Schema(description = "...")` | 类/字段 | 模型/字段描述 |
| `@Schema(example = "...")` | 字段 | 示例值 |
| `@Schema(requiredMode = REQUIRED)` | 字段 | 标记必填 |
| `@Schema(hidden = true)` | 字段 | 隐藏字段 |

## 与 Nexus 注解对照

| Nexus (`@nestjs/swagger`) | Joint (SpringDoc) |
|---------------------------|-------------------|
| `@ApiTags('...')` | `@Tag(name = "...")` |
| `@ApiOperation({ summary })` | `@Operation(summary = "...")` |
| `@ApiProperty({ description })` | `@Schema(description = "...")` |
| `@ApiPropertyOptional()` | `@Schema(requiredMode = NOT_REQUIRED)` |
| `@ApiBearerAuth()` | `SecurityScheme` 全局配置 |

## 访问地址

| 资源 | 路径 |
|------|------|
| Swagger UI | `http://localhost:8080/api/v1/swagger` |
| OpenAPI JSON | `http://localhost:8080/api/v1/api-docs` |

## 知识点总结

| 概念 | 说明 |
|------|------|
| SpringDoc | Spring Boot 3 的 OpenAPI 文档库 |
| @Tag | 接口分组 |
| @Operation | 接口描述 |
| @Schema | 模型/字段描述 |
| SecurityScheme | JWT 认证配置 |

## 练习任务

1. 配置 SpringDoc 并访问 Swagger UI
2. 为所有 Controller 添加 @Tag 和 @Operation
3. 为 DTO 添加 @Schema 描述
4. 在 Swagger UI 中测试 JWT 认证接口
