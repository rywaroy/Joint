# 1.6 参数校验

## 学习目标

- 使用 JSR-380 (Bean Validation) 进行参数校验
- 掌握常用校验注解
- 实现自定义校验器
- 对照 Nexus 的 ValidationPipe

## 与 Nexus 对比

Nexus 使用 class-validator：

```typescript
// dto/create-user.dto.ts
import { IsNotEmpty, IsString, Length, IsEmail, IsOptional } from 'class-validator';

export class CreateUserDto {
    @IsNotEmpty({ message: '用户名不能为空' })
    @IsString()
    @Length(3, 20, { message: '用户名长度为3-20个字符' })
    username: string;

    @IsNotEmpty({ message: '密码不能为空' })
    @Length(6, 20)
    password: string;

    @IsOptional()
    @IsEmail({}, { message: '邮箱格式不正确' })
    email?: string;
}
```

Spring Boot 使用 Hibernate Validator（JSR-380 实现）：

```java
public class CreateUserDto {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度为3-20个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20)
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;
}
```

## 实践步骤

### 步骤 1：添加依赖

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 步骤 2：常用校验注解

| 注解 | 说明 | 示例 |
|------|------|------|
| `@NotNull` | 不能为 null | `@NotNull` |
| `@NotEmpty` | 不能为 null 且长度 > 0 | 用于集合、字符串 |
| `@NotBlank` | 不能为 null 且去空格后长度 > 0 | 用于字符串 |
| `@Size` | 长度/大小范围 | `@Size(min=3, max=20)` |
| `@Min` / `@Max` | 数值范围 | `@Min(0) @Max(100)` |
| `@Email` | 邮箱格式 | `@Email` |
| `@Pattern` | 正则匹配 | `@Pattern(regexp="^1[3-9]\\d{9}$")` |
| `@Positive` | 正数 | `@Positive` |
| `@Future` / `@Past` | 日期校验 | `@Future` |

### 步骤 3：给 DTO 添加校验注解

```java
package com.joint.modules.system.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateUserDto {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度为3-20个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickName;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;
}
```

### 步骤 4：在 Controller 中启用校验

使用 `@Valid` 或 `@Validated` 注解：

```java
@RestController
@RequestMapping("/system/user")
public class UserController {

    @PostMapping
    public User create(@Valid @RequestBody CreateUserDto dto) {
        // 如果校验失败，会抛出 MethodArgumentNotValidException
        // 已在 GlobalExceptionHandler 中处理
        return userService.create(dto);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable String id,
                       @Valid @RequestBody UpdateUserDto dto) {
        return userService.update(id, dto);
    }
}
```

### 步骤 5：查询参数校验

对于 GET 请求的查询参数，需要在类上添加 `@Validated`：

```java
@RestController
@RequestMapping("/system/user")
@Validated  // 启用方法级别的参数校验
public class UserController {

    @GetMapping("/list")
    public List<User> list(
        @RequestParam @Min(1) Integer page,
        @RequestParam @Min(1) @Max(100) Integer size
    ) {
        return userService.findAll(page, size);
    }

    @GetMapping("/{id}")
    public User getById(
        @PathVariable @NotBlank String id
    ) {
        return userService.findById(id);
    }
}
```

### 步骤 6：分组校验

不同场景使用不同校验规则：

```java
// 定义校验分组
public interface CreateGroup {}
public interface UpdateGroup {}

@Data
public class UserDto {

    @Null(groups = CreateGroup.class, message = "创建时不能指定ID")
    @NotBlank(groups = UpdateGroup.class, message = "更新时必须指定ID")
    private String id;

    @NotBlank(groups = CreateGroup.class, message = "用户名不能为空")
    private String username;
}
```

使用：

```java
@PostMapping
public User create(@Validated(CreateGroup.class) @RequestBody UserDto dto) { ... }

@PutMapping("/{id}")
public User update(@Validated(UpdateGroup.class) @RequestBody UserDto dto) { ... }
```

### 步骤 7：自定义校验注解

创建一个校验手机号的注解：

```java
package com.joint.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = PhoneValidator.class)
public @interface Phone {

    String message() default "手机号格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

```java
package com.joint.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PhoneValidator implements ConstraintValidator<Phone, String> {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;  // null 值由 @NotBlank 处理
        }
        return PHONE_PATTERN.matcher(value).matches();
    }
}
```

使用：

```java
@Data
public class CreateUserDto {
    @Phone
    private String phone;
}
```

### 步骤 8：嵌套对象校验

```java
@Data
public class CreateOrderDto {

    @NotBlank
    private String orderNo;

    @Valid  // 嵌套对象也需要校验
    @NotNull(message = "收货地址不能为空")
    private AddressDto address;
}

@Data
public class AddressDto {

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "详细地址不能为空")
    private String detail;
}
```

## 校验失败响应

结合之前的 GlobalExceptionHandler，校验失败会返回：

```json
{
    "code": 400,
    "message": "用户名不能为空, 密码长度为6-20个字符",
    "data": null
}
```

## 目录结构

```
src/main/java/com/joint/
├── common/
│   ├── annotation/
│   │   ├── Phone.java
│   │   └── PhoneValidator.java
│   └── ...
└── modules/
    └── system/
        └── user/
            └── dto/
                ├── CreateUserDto.java
                └── UpdateUserDto.java
```

## 知识点总结

| 概念 | 说明 |
|------|------|
| JSR-380 | Bean Validation 2.0 规范 |
| Hibernate Validator | JSR-380 的实现 |
| @Valid | 启用 DTO 校验 |
| @Validated | 启用方法参数校验/分组校验 |
| ConstraintValidator | 自定义校验器接口 |

## 与 Nexus 对照

| Nexus (class-validator) | Joint (Hibernate Validator) |
|-------------------------|----------------------------|
| `@IsNotEmpty()` | `@NotBlank` |
| `@IsString()` | 类型由 Java 保证 |
| `@Length(3, 20)` | `@Size(min=3, max=20)` |
| `@IsEmail()` | `@Email` |
| `@IsOptional()` | 不加 @NotNull 即为可选 |
| `@Matches(regex)` | `@Pattern(regexp=...)` |
| `ValidationPipe` | `@Valid` + ExceptionHandler |

## 练习任务

1. 给 CreateUserDto 添加校验注解
2. 在 Controller 中使用 @Valid 启用校验
3. 测试校验失败的响应
4. 实现 @Phone 自定义校验注解
