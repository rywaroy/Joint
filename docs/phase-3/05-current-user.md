# 3.5 获取当前用户

## 学习目标

- 从 SecurityContext 获取当前用户
- 创建 @CurrentUser 注解简化获取
- 实现获取当前用户信息接口

## 与 Nexus 对照

Nexus 获取当前用户：

```typescript
@UseGuards(AuthGuard)
@Get('info')
async getInfo(@Request() req) {
    return this.userService.findById(req.user.id);
}
```

## 实践步骤

### 步骤 1：直接从 SecurityContext 获取

```java
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/info")
    public User getInfo() {
        // 从 SecurityContext 获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return loginUser.getUser();
    }
}
```

### 步骤 2：创建 @CurrentUser 注解

```java
package com.joint.common.annotation;

import java.lang.annotation.*;

/**
 * 获取当前登录用户
 * 用于 Controller 方法参数
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
```

### 步骤 3：创建参数解析器

```java
package com.joint.common.security;

import com.joint.common.annotation.CurrentUser;
import com.joint.modules.system.user.entity.User;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 检查参数是否有 @CurrentUser 注解
        return parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof LoginUser loginUser) {
            // 根据参数类型返回不同对象
            Class<?> parameterType = parameter.getParameterType();

            if (parameterType.equals(User.class)) {
                return loginUser.getUser();
            } else if (parameterType.equals(LoginUser.class)) {
                return loginUser;
            } else if (parameterType.equals(String.class)) {
                // 如果参数类型是 String，返回用户 ID
                return loginUser.getUserId();
            }
        }

        return null;
    }
}
```

### 步骤 4：注册参数解析器

```java
package com.joint.config;

import com.joint.common.security.CurrentUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
}
```

### 步骤 5：在 Controller 中使用

```java
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     * GET /api/user/info
     */
    @GetMapping("/info")
    public User getInfo(@CurrentUser User user) {
        // 直接使用注入的当前用户
        return userService.findById(user.getId());
    }

    /**
     * 获取当前用户 ID
     */
    @GetMapping("/id")
    public String getCurrentUserId(@CurrentUser String userId) {
        return userId;
    }

    /**
     * 获取完整的登录用户信息（包含权限）
     */
    @GetMapping("/detail")
    public LoginUser getLoginUser(@CurrentUser LoginUser loginUser) {
        return loginUser;
    }

    /**
     * 修改当前用户信息
     */
    @PutMapping("/profile")
    public User updateProfile(@CurrentUser User currentUser,
                              @RequestBody UpdateProfileDto dto) {
        return userService.updateProfile(currentUser.getId(), dto);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public void changePassword(@CurrentUser String userId,
                               @Valid @RequestBody ChangePasswordDto dto) {
        userService.changePassword(userId, dto);
    }
}
```

### 步骤 6：创建工具类（可选）

```java
package com.joint.common.utils;

import com.joint.common.security.LoginUser;
import com.joint.modules.system.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 * 用于在 Service 层获取当前用户
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户
     */
    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前用户 ID
     */
    public static String getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }

    /**
     * 获取当前用户实体
     */
    public static User getUser() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUser() : null;
    }

    /**
     * 判断是否已登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof LoginUser;
    }

    /**
     * 判断是否是管理员
     */
    public static boolean isAdmin() {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return loginUser.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_admin".equals(auth.getAuthority()));
    }
}
```

在 Service 中使用：

```java
@Service
public class OperLogService {

    public void saveLog(String operation) {
        OperLog log = new OperLog();
        log.setOperation(operation);
        log.setOperatorId(SecurityUtils.getUserId());
        log.setOperatorName(SecurityUtils.getUsername());
        log.setOperateTime(LocalDateTime.now());
        operLogMapper.insert(log);
    }
}
```

### 步骤 7：获取用户信息接口完整实现

```java
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PermissionService permissionService;

    /**
     * 获取当前用户信息（包含角色和权限）
     */
    @GetMapping("/info")
    public UserInfoVo getInfo(@CurrentUser User user) {
        // 获取用户角色
        List<String> roles = userService.getUserRoles(user.getId());

        // 获取用户权限码
        Set<String> permissions = permissionService.getUserPermissions(user.getId());

        // 获取用户菜单树
        List<MenuVo> menus = menuService.getUserMenuTree(user.getId());

        return UserInfoVo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getNickName())
                .avatar(user.getAvatar())
                .roles(roles)
                .permissions(permissions)
                .menus(menus)
                .build();
    }
}
```

```java
@Data
@Builder
public class UserInfoVo {
    private String id;
    private String username;
    private String realName;
    private String avatar;
    private List<String> roles;
    private Set<String> permissions;
    private List<MenuVo> menus;
}
```

## 与 Nexus 对照

| Nexus | Joint |
|-------|-------|
| `@Request() req` | `@CurrentUser User user` |
| `req.user` | `user` |
| `req.user.id` | `user.getId()` |
| 在 AuthGuard 中设置 | 在 JwtAuthenticationFilter 中设置 |

## 知识点总结

| 概念 | 说明 |
|------|------|
| SecurityContextHolder | 安全上下文持有者，存储当前用户 |
| HandlerMethodArgumentResolver | 方法参数解析器 |
| @CurrentUser | 自定义注解获取当前用户 |
| SecurityUtils | 安全工具类 |

## 练习任务

1. 创建 @CurrentUser 注解
2. 实现 CurrentUserArgumentResolver
3. 注册参数解析器
4. 实现获取当前用户信息接口
5. 创建 SecurityUtils 工具类
