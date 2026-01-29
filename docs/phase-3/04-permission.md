# 3.4 权限控制

## 学习目标

- 实现 RBAC 权限模型
- 创建自定义权限注解
- 对照 Nexus 的 PermissionGuard

## 与 Nexus 对照

Nexus PermissionGuard：

```typescript
@Injectable()
export class PermissionGuard implements CanActivate {
    async canActivate(context: ExecutionContext): Promise<boolean> {
        const requiredPermissions = this.reflector.get<string[]>(
            PERMISSION_KEY,
            context.getHandler()
        );

        if (!requiredPermissions) return true;

        const user = request.user;
        if (user.roles.includes('admin')) return true;

        // 查询用户权限码
        const ownedAuthCodes = await this.getUserPermissions(user);
        return requiredPermissions.some(code => ownedAuthCodes.has(code));
    }
}

// 使用
@RequirePermission('system:user:list')
@Get('list')
findAll() { ... }
```

## 实践步骤

### 步骤 1：创建权限注解

```java
package com.joint.common.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 标注在 Controller 方法上，表示访问该接口需要指定权限
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 权限码
     * 如: "system:user:list", "system:user:add"
     */
    String[] value();

    /**
     * 权限关系：AND 或 OR
     * AND: 需要同时拥有所有权限
     * OR: 拥有任一权限即可
     */
    Logical logical() default Logical.OR;
}
```

```java
package com.joint.common.annotation;

public enum Logical {
    AND, OR
}
```

### 步骤 2：创建权限切面

```java
package com.joint.common.security;

import com.joint.common.annotation.Logical;
import com.joint.common.annotation.RequirePermission;
import com.joint.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionService permissionService;

    @Before("@annotation(com.joint.common.annotation.RequirePermission)")
    public void checkPermission(JoinPoint joinPoint) {
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录");
        }

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String userId = loginUser.getUserId();

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);

        String[] requiredPermissions = annotation.value();
        Logical logical = annotation.logical();

        // admin 角色直接放行
        if (isAdmin(loginUser)) {
            return;
        }

        // 获取用户权限码
        Set<String> userPermissions = permissionService.getUserPermissions(userId);

        // 检查权限
        boolean hasPermission;
        if (logical == Logical.AND) {
            // 需要同时拥有所有权限
            hasPermission = true;
            for (String permission : requiredPermissions) {
                if (!userPermissions.contains(permission)) {
                    hasPermission = false;
                    break;
                }
            }
        } else {
            // 拥有任一权限即可
            hasPermission = false;
            for (String permission : requiredPermissions) {
                if (userPermissions.contains(permission)) {
                    hasPermission = true;
                    break;
                }
            }
        }

        if (!hasPermission) {
            throw new BusinessException(403, "没有权限访问此资源");
        }
    }

    private boolean isAdmin(LoginUser loginUser) {
        return loginUser.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_admin".equals(auth.getAuthority()));
    }
}
```

### 步骤 3：创建权限服务

```java
package com.joint.common.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.joint.modules.system.menu.entity.Menu;
import com.joint.modules.system.menu.mapper.MenuMapper;
import com.joint.modules.system.role.entity.Role;
import com.joint.modules.system.role.entity.RoleMenu;
import com.joint.modules.system.role.mapper.RoleMapper;
import com.joint.modules.system.role.mapper.RoleMenuMapper;
import com.joint.modules.system.user.entity.UserRole;
import com.joint.modules.system.user.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;

    /**
     * 获取用户的所有权限码
     */
    public Set<String> getUserPermissions(String userId) {
        // 1. 查询用户的角色
        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleWrapper);

        if (userRoles.isEmpty()) {
            return Set.of();
        }

        List<String> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .toList();

        // 2. 检查是否有超级管理员角色
        LambdaQueryWrapper<Role> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.in(Role::getId, roleIds)
                   .eq(Role::getStatus, 0);
        List<Role> roles = roleMapper.selectList(roleWrapper);

        boolean isSuper = roles.stream()
                .anyMatch(role -> role.getIsSuper() || "admin".equals(role.getName()));

        if (isSuper) {
            // 超级管理员拥有所有权限
            return getAllPermissions();
        }

        // 3. 查询角色的菜单
        LambdaQueryWrapper<RoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
        roleMenuWrapper.in(RoleMenu::getRoleId, roleIds);
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(roleMenuWrapper);

        if (roleMenus.isEmpty()) {
            return Set.of();
        }

        List<String> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .distinct()
                .toList();

        // 4. 查询菜单的权限码
        LambdaQueryWrapper<Menu> menuWrapper = new LambdaQueryWrapper<>();
        menuWrapper.in(Menu::getId, menuIds)
                   .eq(Menu::getStatus, 0)
                   .isNotNull(Menu::getAuthCode);
        List<Menu> menus = menuMapper.selectList(menuWrapper);

        return menus.stream()
                .map(Menu::getAuthCode)
                .filter(code -> code != null && !code.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * 获取所有权限码（超级管理员用）
     */
    private Set<String> getAllPermissions() {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getStatus, 0)
               .isNotNull(Menu::getAuthCode);
        List<Menu> menus = menuMapper.selectList(wrapper);

        return menus.stream()
                .map(Menu::getAuthCode)
                .filter(code -> code != null && !code.isEmpty())
                .collect(Collectors.toSet());
    }
}
```

### 步骤 4：启用 AOP

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### 步骤 5：在 Controller 中使用

```java
@RestController
@RequestMapping("/system/user")
public class SystemUserController {

    @GetMapping("/list")
    @RequirePermission("system:user:list")  // 需要此权限
    public IPage<User> list(QueryUserDto query) {
        return userService.findAll(query);
    }

    @PostMapping
    @RequirePermission("system:user:add")
    public User create(@Valid @RequestBody CreateUserDto dto) {
        return userService.create(dto);
    }

    @PutMapping("/{id}")
    @RequirePermission("system:user:edit")
    public User update(@PathVariable String id, @Valid @RequestBody UpdateUserDto dto) {
        return userService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:user:delete")
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }

    // 需要多个权限
    @PostMapping("/batch-delete")
    @RequirePermission(value = {"system:user:delete", "system:user:list"}, logical = Logical.AND)
    public void batchDelete(@RequestBody List<String> ids) {
        userService.deleteBatch(ids);
    }
}
```

### 步骤 6：完善 UserDetailsService（加载角色）

```java
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 加载用户角色
        List<SimpleGrantedAuthority> authorities = loadUserAuthorities(userId);

        return new LoginUser(user, authorities);
    }

    private List<SimpleGrantedAuthority> loadUserAuthorities(String userId) {
        // 查询用户角色
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = userRoleMapper.selectList(wrapper);

        if (userRoles.isEmpty()) {
            return List.of();
        }

        List<String> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .toList();

        LambdaQueryWrapper<Role> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.in(Role::getId, roleIds)
                   .eq(Role::getStatus, 0);
        List<Role> roles = roleMapper.selectList(roleWrapper);

        // 转换为 GrantedAuthority
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList();
    }
}
```

### 步骤 7：方案二 - 使用 Spring Security 内置注解

如果不想自定义注解，可以使用 Spring Security 的 `@PreAuthorize`：

```java
@Configuration
@EnableMethodSecurity  // 启用方法级安全
public class SecurityConfig { ... }
```

```java
@RestController
@RequestMapping("/system/user")
public class SystemUserController {

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:user:list')")
    public IPage<User> list(QueryUserDto query) { ... }

    @PostMapping
    @PreAuthorize("hasRole('admin') or hasAuthority('system:user:add')")
    public User create(@RequestBody CreateUserDto dto) { ... }
}
```

需要在 UserDetailsService 中设置 authorities：

```java
// 将权限码作为 authority
List<SimpleGrantedAuthority> authorities = permissionService.getUserPermissions(userId)
        .stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());

// 添加角色（以 ROLE_ 前缀）
roles.forEach(role ->
    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()))
);
```

## 权限模型说明

```
用户 (User)
  ↓ 多对多
角色 (Role)
  ↓ 多对多
菜单 (Menu) → authCode (权限码)

例如：
- 用户 admin → 角色 admin (isSuper=true) → 所有权限
- 用户 user1 → 角色 operator → 菜单 [用户管理, 角色管理]
                              → authCode: system:user:list, system:role:list
```

## 与 Nexus 对照

| Nexus | Joint |
|-------|-------|
| `@RequirePermission('...')` | `@RequirePermission("...")` |
| `PermissionGuard` | `PermissionAspect` |
| `Reflector.get()` | AOP 获取注解 |
| `user.roles.includes('admin')` | `isAdmin(loginUser)` |

## 知识点总结

| 概念 | 说明 |
|------|------|
| RBAC | 基于角色的访问控制 |
| AOP | 面向切面编程 |
| @Aspect | 切面类注解 |
| @Before | 前置通知 |
| @PreAuthorize | Spring Security 权限注解 |

## 练习任务

1. 创建 @RequirePermission 注解
2. 实现 PermissionAspect 切面
3. 实现 PermissionService
4. 在 Controller 中应用权限注解
5. 测试不同用户的权限访问
