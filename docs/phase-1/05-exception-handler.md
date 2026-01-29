# 1.5 全局异常处理

## 学习目标

- 实现全局异常处理
- 自定义业务异常
- 对照 Nexus 的 HttpExceptionFilter

## 目标效果

任何异常都返回统一格式：

```json
{
    "code": 500,
    "message": "服务器内部错误",
    "data": null
}
```

业务异常：

```json
{
    "code": 400,
    "message": "用户名已存在",
    "data": null
}
```

## 与 Nexus 对比

Nexus 使用 ExceptionFilter：

```typescript
// http-exception.filter.ts
@Catch()
export class HttpExceptionFilter implements ExceptionFilter {
    catch(exception: unknown, host: ArgumentsHost) {
        const ctx = host.switchToHttp();
        const response = ctx.getResponse<Response>();

        let code = 500;
        let message = '服务器内部错误';

        if (exception instanceof HttpException) {
            code = exception.getStatus();
            message = exception.message;
        }

        response.status(200).json({ code, message, data: null });
    }
}
```

Spring Boot 使用 @ControllerAdvice + @ExceptionHandler：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error(e.getMessage());
    }
}
```

## 实践步骤

### 步骤 1：创建业务异常类

```java
package com.joint.common.exception;

import lombok.Getter;

/**
 * 业务异常
 * 用于在业务逻辑中抛出的可预期异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
```

### 步骤 2：创建错误码枚举（可选）

```java
package com.joint.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用错误
    SUCCESS(0, "请求成功"),
    FAILED(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),

    // 认证相关
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限访问"),

    // 业务错误
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    USERNAME_EXISTS(1003, "用户名已存在"),
    PASSWORD_ERROR(1004, "密码错误");

    private final Integer code;
    private final String message;
}
```

扩展 BusinessException：

```java
public BusinessException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.code = errorCode.getCode();
}
```

### 步骤 3：创建全局异常处理器

```java
package com.joint.common.exception;

import com.joint.common.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid 校验失败）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理缺少请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getParameterName());
        return Result.error(400, "缺少必要参数: " + e.getParameterName());
    }

    /**
     * 处理请求方法不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.error(405, "不支持的请求方法: " + e.getMethod());
    }

    /**
     * 处理 404
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNotFound(NoHandlerFoundException e) {
        return Result.error(404, "接口不存在: " + e.getRequestURL());
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(500, "服务器内部错误");
    }
}
```

### 步骤 4：启用 404 异常抛出

默认情况下，Spring Boot 的 404 不会抛出异常。需要配置：

```yaml
# application.yml
spring:
  mvc:
    throw-exception-if-no-handler-found: true
  web:
    resources:
      add-mappings: false  # 禁用静态资源映射
```

### 步骤 5：在 Service 中抛出异常

```java
@Service
public class UserService {

    public User findById(String id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public User create(CreateUserDto dto) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        // ...
    }
}
```

### 步骤 6：测试异常处理

```bash
# 访问不存在的用户
curl http://localhost:8080/api/system/user/not-exist-id
# 响应: {"code": 1001, "message": "用户不存在", "data": null}

# 创建重复用户名
curl -X POST http://localhost:8080/api/system/user \
  -H "Content-Type: application/json" \
  -d '{"username":"admin"}'
# 响应: {"code": 1003, "message": "用户名已存在", "data": null}

# 访问不存在的接口
curl http://localhost:8080/api/not-exist
# 响应: {"code": 404, "message": "接口不存在: /api/not-exist", "data": null}
```

## 目录结构

```
src/main/java/com/joint/
├── common/
│   ├── exception/
│   │   ├── BusinessException.java
│   │   ├── ErrorCode.java
│   │   └── GlobalExceptionHandler.java
│   └── response/
│       └── ...
└── modules/
    └── ...
```

## 知识点总结

| 概念 | 说明 |
|------|------|
| @RestControllerAdvice | 全局异常处理增强 |
| @ExceptionHandler | 指定处理的异常类型 |
| @ResponseStatus | 指定 HTTP 状态码 |
| BusinessException | 自定义业务异常 |

## 与 Nexus 对照

| Nexus | Joint |
|-------|-------|
| `HttpExceptionFilter` | `GlobalExceptionHandler` |
| `@Catch()` | `@ExceptionHandler` |
| `throw new BadRequestException()` | `throw new BusinessException()` |
| `exception.getStatus()` | `exception.getCode()` |

## 练习任务

1. 实现 BusinessException 和 GlobalExceptionHandler
2. 创建 ErrorCode 枚举定义常见错误
3. 在 Service 中使用 BusinessException
4. 测试各种异常情况的响应
