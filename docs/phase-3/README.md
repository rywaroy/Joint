# Phase 3: 认证授权

## 学习目标

- 理解 Spring Security 核心概念
- 实现 JWT 认证
- 实现 RBAC 权限控制
- 对照 Nexus 的 AuthGuard 和 PermissionGuard

## 任务列表

| 任务 | 文档 | 核心知识点 |
|------|------|-----------|
| 3.1 | [Spring Security 入门](01-security-intro.md) | 过滤器链、认证流程 |
| 3.2 | [JWT 认证实现](02-jwt-auth.md) | JWT 生成、验证、刷新 |
| 3.3 | [登录接口实现](03-login.md) | AuthController、密码加密 |
| 3.4 | [权限控制](04-permission.md) | RBAC、自定义权限注解 |
| 3.5 | [获取当前用户](05-current-user.md) | SecurityContext、@CurrentUser |

## Nexus 对照

| Nexus | Joint |
|-------|-------|
| `AuthGuard` | `JwtAuthenticationFilter` |
| `PermissionGuard` | `@PreAuthorize` / 自定义注解 |
| `JwtStrategy` | `JwtTokenProvider` |
| `@UseGuards(AuthGuard)` | `SecurityFilterChain` 配置 |
| `@RequirePermission()` | `@RequirePermission` 自定义注解 |
| `request.user` | `SecurityContextHolder` |
| `bcrypt.compare()` | `PasswordEncoder.matches()` |

## 认证流程对比

### Nexus 流程

```
请求 → AuthGuard → JwtService.verify() → UserService.findOne() → request.user
                         ↓ 失败
                    UnauthorizedException
```

### Joint 流程

```
请求 → JwtAuthenticationFilter → JwtTokenProvider.validate()
                                        ↓ 成功
                               SecurityContextHolder.setAuthentication()
                                        ↓ 失败
                               AuthenticationEntryPoint (401)
```

## 技术选型

| 组件 | 技术 |
|------|------|
| 安全框架 | Spring Security 6.x |
| Token | JWT (jjwt 库) |
| 密码加密 | BCrypt |
| 权限模型 | RBAC (基于角色的访问控制) |

## 完成标准

- [ ] 理解 Spring Security 过滤器链
- [ ] 实现 JWT 生成和验证
- [ ] 实现登录/登出接口
- [ ] 实现基于权限码的访问控制
- [ ] 能获取当前登录用户信息
