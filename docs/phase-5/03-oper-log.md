# 5.3 操作日志

## 学习目标

- 使用 AOP 实现操作日志
- 创建自定义 @Log 注解
- 对照 Nexus 的 @Log 装饰器

## 与 Nexus 对照

Nexus 日志装饰器：

```typescript
// log.decorator.ts
export function Log(type: BusinessTypeEnum) {
    return applyDecorators(SetMetadata(LOG_KEY, type));
}

// 使用
@Log(BusinessTypeEnum.INSERT)
@Post()
create(@Body() dto: CreateUserDto) { ... }
```

## 实践步骤

### 步骤 1：创建日志注解

```java
package com.joint.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 模块名称
     */
    String module() default "";

    /**
     * 操作类型
     */
    BusinessType type() default BusinessType.OTHER;

    /**
     * 操作描述
     */
    String description() default "";
}
```

```java
package com.joint.common.annotation;

public enum BusinessType {
    OTHER,      // 其他
    INSERT,     // 新增
    UPDATE,     // 修改
    DELETE,     // 删除
    QUERY,      // 查询
    EXPORT,     // 导出
    IMPORT,     // 导入
    LOGIN,      // 登录
    LOGOUT      // 登出
}
```

### 步骤 2：创建日志实体

```java
package com.joint.modules.system.operlog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_oper_log")
public class OperLog {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String module;           // 模块名称

    private String businessType;     // 业务类型

    private String description;      // 操作描述

    private String method;           // 请求方法

    private String requestMethod;    // HTTP 方法

    private String requestUrl;       // 请求 URL

    private String requestParams;    // 请求参数

    private String responseResult;   // 返回结果

    private Integer status;          // 状态 0-成功 1-失败

    private String errorMsg;         // 错误信息

    private String operatorId;       // 操作人 ID

    private String operatorName;     // 操作人姓名

    private String operatorIp;       // 操作 IP

    private Long costTime;           // 耗时（毫秒）

    private LocalDateTime operateTime; // 操作时间
}
```

### 步骤 3：创建日志切面

```java
package com.joint.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joint.common.annotation.Log;
import com.joint.common.security.LoginUser;
import com.joint.common.utils.IpUtils;
import com.joint.modules.system.operlog.entity.OperLog;
import com.joint.modules.system.operlog.mapper.OperLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final OperLogMapper operLogMapper;
    private final ObjectMapper objectMapper;

    // 线程变量，保存开始时间
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    /**
     * 切入点：标注 @Log 注解的方法
     */
    @Pointcut("@annotation(com.joint.common.annotation.Log)")
    public void logPointcut() {}

    /**
     * 前置通知：记录开始时间
     */
    @Before("logPointcut()")
    public void doBefore() {
        START_TIME.set(System.currentTimeMillis());
    }

    /**
     * 返回通知：记录成功日志
     */
    @AfterReturning(pointcut = "logPointcut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        handleLog(joinPoint, null, result);
    }

    /**
     * 异常通知：记录失败日志
     */
    @AfterThrowing(pointcut = "logPointcut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }

    /**
     * 处理日志
     */
    private void handleLog(JoinPoint joinPoint, Exception e, Object result) {
        try {
            // 获取注解
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Log logAnnotation = method.getAnnotation(Log.class);

            if (logAnnotation == null) {
                return;
            }

            // 获取请求信息
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

            // 获取当前用户
            LoginUser loginUser = getLoginUser();

            // 构建日志对象
            OperLog operLog = new OperLog();
            operLog.setModule(logAnnotation.module());
            operLog.setBusinessType(logAnnotation.type().name());
            operLog.setDescription(logAnnotation.description());
            operLog.setMethod(joinPoint.getSignature().getDeclaringTypeName()
                    + "." + joinPoint.getSignature().getName());

            if (request != null) {
                operLog.setRequestMethod(request.getMethod());
                operLog.setRequestUrl(request.getRequestURI());
                operLog.setOperatorIp(IpUtils.getClientIp(request));
            }

            // 请求参数（限制长度）
            try {
                String params = objectMapper.writeValueAsString(joinPoint.getArgs());
                operLog.setRequestParams(truncate(params, 2000));
            } catch (Exception ex) {
                operLog.setRequestParams("参数序列化失败");
            }

            // 返回结果（限制长度）
            if (result != null) {
                try {
                    String resultStr = objectMapper.writeValueAsString(result);
                    operLog.setResponseResult(truncate(resultStr, 2000));
                } catch (Exception ex) {
                    operLog.setResponseResult("结果序列化失败");
                }
            }

            // 状态和错误信息
            if (e != null) {
                operLog.setStatus(1);
                operLog.setErrorMsg(truncate(e.getMessage(), 2000));
            } else {
                operLog.setStatus(0);
            }

            // 操作人信息
            if (loginUser != null) {
                operLog.setOperatorId(loginUser.getUserId());
                operLog.setOperatorName(loginUser.getUsername());
            }

            // 耗时
            Long startTime = START_TIME.get();
            if (startTime != null) {
                operLog.setCostTime(System.currentTimeMillis() - startTime);
            }

            operLog.setOperateTime(LocalDateTime.now());

            // 异步保存日志
            saveLog(operLog);

        } catch (Exception ex) {
            log.error("记录操作日志失败", ex);
        } finally {
            START_TIME.remove();
        }
    }

    @Async
    public void saveLog(OperLog operLog) {
        operLogMapper.insert(operLog);
    }

    private LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
}
```

### 步骤 4：IP 工具类

```java
package com.joint.common.utils;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtils {

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
```

### 步骤 5：在 Controller 中使用

```java
@RestController
@RequestMapping("/system/user")
public class UserController {

    @PostMapping
    @Log(module = "用户管理", type = BusinessType.INSERT, description = "创建用户")
    public UserVo create(@Valid @RequestBody CreateUserDto dto) {
        return userService.create(dto);
    }

    @PutMapping("/{id}")
    @Log(module = "用户管理", type = BusinessType.UPDATE, description = "更新用户")
    public UserVo update(@PathVariable String id, @RequestBody UpdateUserDto dto) {
        return userService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Log(module = "用户管理", type = BusinessType.DELETE, description = "删除用户")
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }
}
```

### 步骤 6：启用异步

```java
@SpringBootApplication
@EnableAsync  // 启用异步
public class JointApplication { }
```

## 知识点总结

| 概念 | 说明 |
|------|------|
| @Aspect | 切面类注解 |
| @Pointcut | 切入点定义 |
| @Before | 前置通知 |
| @AfterReturning | 返回通知 |
| @AfterThrowing | 异常通知 |
| @Async | 异步执行 |

## 练习任务

1. 创建 @Log 注解和 BusinessType 枚举
2. 实现 LogAspect 切面
3. 为 CRUD 接口添加日志注解
4. 查询操作日志列表
