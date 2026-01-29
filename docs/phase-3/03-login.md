# 3.3 登录接口实现

## 学习目标

- 实现登录接口
- 密码加密与验证
- 返回 JWT Token

## 与 Nexus 对照

Nexus 登录实现：

```typescript
// auth.controller.ts
@Post('login')
async login(@Body() dto: LoginDto) {
    return this.authService.login(dto);
}

// auth.service.ts
async login(dto: LoginDto) {
    const { username, password } = dto;
    const user = await this.prisma.user.findUnique({
        where: { username },
        include: { roles: { include: { role: true } } }
    });

    if (!user || !(await bcrypt.compare(password, user.password))) {
        throw new BadRequestException('用户名或密码错误');
    }

    const roles = user.roles.map(r => r.role.name);
    const payload = { id: user.id, username: user.username, roles };

    return {
        accessToken: this.jwtService.sign(payload),
        id: user.id,
        username: user.username,
        roles,
        realName: user.nickName
    };
}
```

## 实践步骤

### 步骤 1：创建登录 DTO

```java
package com.joint.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

### 步骤 2：创建登录响应 VO

```java
package com.joint.modules.auth.vo;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class LoginVo {

    private String accessToken;
    private String id;
    private String username;
    private String realName;
    private List<String> roles;
}
```

### 步骤 3：创建 AuthService

```java
package com.joint.modules.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.joint.common.exception.BusinessException;
import com.joint.common.security.JwtTokenProvider;
import com.joint.modules.auth.dto.LoginDto;
import com.joint.modules.auth.vo.LoginVo;
import com.joint.modules.system.user.entity.User;
import com.joint.modules.system.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 用户登录
     */
    public LoginVo login(LoginDto dto) {
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);

        // 2. 验证用户是否存在
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 4. 检查用户状态
        if (user.getStatus() == 1) {
            throw new BusinessException("用户已被禁用");
        }

        // 5. 获取用户角色（后续完善）
        List<String> roles = getUserRoles(user.getId());

        // 6. 生成 Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), claims);

        // 7. 返回登录信息
        return LoginVo.builder()
                .accessToken(token)
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getNickName())
                .roles(roles)
                .build();
    }

    /**
     * 获取用户角色列表
     */
    private List<String> getUserRoles(String userId) {
        // TODO: 查询用户角色关联表
        return List.of("user");
    }
}
```

### 步骤 4：创建 AuthController

```java
package com.joint.modules.auth;

import com.joint.modules.auth.dto.LoginDto;
import com.joint.modules.auth.vo.LoginVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public LoginVo login(@Valid @RequestBody LoginDto dto) {
        return authService.login(dto);
    }

    /**
     * 用户登出
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public void logout() {
        // JWT 是无状态的，登出只需要前端清除 Token
        // 如果需要服务端使 Token 失效，可以使用 Redis 黑名单
    }
}
```

### 步骤 5：用户注册（加密密码）

```java
// UserService.java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     */
    public User register(RegisterDto dto) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));  // 加密密码
        user.setNickName(dto.getNickName());
        user.setEmail(dto.getEmail());
        user.setStatus(0);

        userMapper.insert(user);
        return user;
    }
}
```

### 步骤 6：完善获取用户角色

```java
// AuthService.java

private final RoleMapper roleMapper;
private final UserRoleMapper userRoleMapper;

private List<String> getUserRoles(String userId) {
    // 查询用户角色关联
    LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(UserRole::getUserId, userId);
    List<UserRole> userRoles = userRoleMapper.selectList(wrapper);

    if (userRoles.isEmpty()) {
        return List.of();
    }

    // 查询角色名称
    List<String> roleIds = userRoles.stream()
            .map(UserRole::getRoleId)
            .toList();

    LambdaQueryWrapper<Role> roleWrapper = new LambdaQueryWrapper<>();
    roleWrapper.in(Role::getId, roleIds)
               .eq(Role::getStatus, 0);  // 只查询启用的角色

    return roleMapper.selectList(roleWrapper).stream()
            .map(Role::getName)
            .toList();
}
```

### 步骤 7：测试登录

```bash
# 创建测试用户（先手动在数据库插入）
# 密码需要用 BCrypt 加密，可以用在线工具生成
# 明文 123456 -> $2a$10$...

# 登录请求
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 响应
{
    "code": 0,
    "message": "请求成功",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "id": "1",
        "username": "admin",
        "realName": "管理员",
        "roles": ["admin"]
    }
}
```

### 步骤 8：初始化管理员脚本

```java
package com.joint.modules.auth;

import com.joint.modules.system.user.entity.User;
import com.joint.modules.system.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 检查是否已有管理员
        if (userMapper.selectCount(null) > 0) {
            return;
        }

        // 创建默认管理员
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setNickName("管理员");
        admin.setStatus(0);

        userMapper.insert(admin);
        log.info("已创建默认管理员账号: admin / 123456");
    }
}
```

## 登录流程图

```
┌─────────┐     POST /auth/login      ┌─────────────┐
│ Client  │ ──────────────────────────> AuthController
└─────────┘                           └──────┬──────┘
                                             │
                                             v
                                      ┌─────────────┐
                                      │ AuthService │
                                      └──────┬──────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
                    v                        v                        v
            ┌───────────────┐     ┌──────────────────┐     ┌─────────────────┐
            │ UserMapper    │     │ PasswordEncoder  │     │ JwtTokenProvider │
            │ (查询用户)     │     │ (验证密码)        │     │ (生成Token)      │
            └───────────────┘     └──────────────────┘     └─────────────────┘
                                                                    │
                                                                    v
                                                           ┌───────────────┐
                                                           │   LoginVo     │
                                                           │ (accessToken) │
                                                           └───────────────┘
```

## 与 Nexus 对照总结

| Nexus | Joint |
|-------|-------|
| `bcrypt.hash()` | `passwordEncoder.encode()` |
| `bcrypt.compare()` | `passwordEncoder.matches()` |
| `this.jwtService.sign()` | `jwtTokenProvider.generateToken()` |
| `throw new BadRequestException()` | `throw new BusinessException()` |
| `include: { roles }` | 多表查询 |

## 知识点总结

| 概念 | 说明 |
|------|------|
| BCryptPasswordEncoder | BCrypt 密码编码器 |
| CommandLineRunner | 应用启动时执行 |
| LambdaQueryWrapper | Lambda 条件构造器 |

## 练习任务

1. 实现 AuthController 和 AuthService
2. 实现用户注册接口
3. 创建 AdminInitializer 初始化管理员
4. 测试登录接口
