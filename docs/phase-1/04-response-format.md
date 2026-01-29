# 1.4 统一响应格式

## 学习目标

- 实现统一的 API 响应格式
- 学习 ResponseBodyAdvice 的使用
- 对照 Nexus 的 TransformReturnInterceptor

## 目标响应格式

与 Nexus 保持一致：

```json
{
    "code": 0,
    "message": "请求成功",
    "data": { ... }
}
```

错误响应：
```json
{
    "code": 500,
    "message": "服务器内部错误",
    "data": null
}
```

## 与 Nexus 对比

Nexus 使用 Interceptor 实现：

```typescript
// transform-return.interceptor.ts
@Injectable()
export class TransformReturnInterceptor implements NestInterceptor {
    intercept(context: ExecutionContext, next: CallHandler) {
        return next.handle().pipe(
            map((data) => ({
                code: 0,
                message: '请求成功',
                data,
            }))
        );
    }
}
```

Spring Boot 使用 ResponseBodyAdvice：

```java
@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public Object beforeBodyWrite(Object body, ...) {
        return Result.success(body);
    }
}
```

## 实践步骤

### 步骤 1：创建响应类

```java
package com.joint.common.response;

import lombok.Data;

@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    private Result() {}

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("请求成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
```

### 步骤 2：创建 ResponseBodyAdvice

```java
package com.joint.common.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.joint")  // 只处理指定包
@RequiredArgsConstructor
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    /**
     * 是否支持处理
     * 返回 true 才会执行 beforeBodyWrite
     */
    @Override
    public boolean supports(MethodParameter returnType,
                           Class<? extends HttpMessageConverter<?>> converterType) {
        // 如果已经是 Result 类型，不再包装
        return !returnType.getParameterType().equals(Result.class);
    }

    /**
     * 响应体写入前的处理
     */
    @Override
    @SneakyThrows
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        // 已经是 Result 类型，直接返回
        if (body instanceof Result) {
            return body;
        }

        // String 类型需要特殊处理（Spring 默认用 StringHttpMessageConverter）
        if (body instanceof String) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return objectMapper.writeValueAsString(Result.success(body));
        }

        // 包装为统一格式
        return Result.success(body);
    }
}
```

### 步骤 3：创建跳过包装的注解（可选）

某些接口可能不需要包装（如文件下载、健康检查）：

```java
package com.joint.common.annotation;

import java.lang.annotation.*;

/**
 * 标记此注解的方法不会被 ResponseAdvice 包装
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RawResponse {
}
```

修改 ResponseAdvice：

```java
@Override
public boolean supports(MethodParameter returnType,
                       Class<? extends HttpMessageConverter<?>> converterType) {
    // 检查方法或类上是否有 @RawResponse 注解
    if (returnType.hasMethodAnnotation(RawResponse.class)) {
        return false;
    }
    if (returnType.getDeclaringClass().isAnnotationPresent(RawResponse.class)) {
        return false;
    }
    return !returnType.getParameterType().equals(Result.class);
}
```

使用：

```java
@GetMapping("/health")
@RawResponse  // 不包装
public String health() {
    return "OK";
}
```

### 步骤 4：测试效果

Controller 代码无需修改：

```java
@GetMapping("/{id}")
public Map<String, Object> getById(@PathVariable String id) {
    return userService.findById(id);  // 直接返回数据
}
```

实际响应：

```json
{
    "code": 0,
    "message": "请求成功",
    "data": {
        "id": "xxx",
        "username": "admin",
        "nickName": "管理员"
    }
}
```

## 目录结构

```
src/main/java/com/joint/
├── common/
│   ├── annotation/
│   │   └── RawResponse.java
│   └── response/
│       ├── Result.java
│       └── ResponseAdvice.java
└── modules/
    └── ...
```

## 知识点总结

| 概念 | 说明 |
|------|------|
| ResponseBodyAdvice | 在响应体写入前进行处理 |
| @RestControllerAdvice | 全局 Controller 增强 |
| supports() | 判断是否需要处理 |
| beforeBodyWrite() | 实际的处理逻辑 |

## 与 Nexus 对照

| Nexus | Joint |
|-------|-------|
| `TransformReturnInterceptor` | `ResponseAdvice` |
| `@UseInterceptors()` | `@RestControllerAdvice` |
| `map(data => ({...}))` | `beforeBodyWrite()` |
| RxJS pipe | 方法返回值包装 |

## 练习任务

1. 实现 Result 类和 ResponseAdvice
2. 测试接口响应是否被自动包装
3. 实现 @RawResponse 注解跳过包装
