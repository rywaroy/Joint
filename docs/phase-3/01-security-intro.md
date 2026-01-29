# 3.1 Spring Security 入门

## 学习目标

- 理解 Spring Security 架构
- 了解过滤器链机制
- 配置基本的安全规则

## 概念讲解

### Spring Security 是什么？

Spring Security 是 Spring 生态中的安全框架，提供：
- **认证（Authentication）**：验证用户身份（你是谁？）
- **授权（Authorization）**：检查用户权限（你能做什么？）

### 与 NestJS Guards 对比

| NestJS | Spring Security |
|--------|-----------------|
| Guard | Filter（过滤器） |
| 每个 Guard 独立 | 过滤器链依次执行 |
| `@UseGuards()` 装饰器 | 配置类统一管理 |
| `canActivate()` | `doFilter()` |

NestJS 是在请求进入 Controller 前拦截：
```typescript
@UseGuards(AuthGuard, PermissionGuard)
@Controller('user')
export class UserController {}
```

Spring Security 是在请求进入 Servlet 前就通过过滤器链处理：
```
请求 → Filter1 → Filter2 → ... → DispatcherServlet → Controller
```

### 过滤器链（Filter Chain）

```
┌──────────────────────────────────────────────────────────────┐
│                    SecurityFilterChain                        │
├──────────────────────────────────────────────────────────────┤
│  DisableEncodeUrlFilter                                      │
│  WebAsyncManagerIntegrationFilter                            │
│  SecurityContextHolderFilter          ← 管理安全上下文        │
│  HeaderWriterFilter                                          │
│  CorsFilter                           ← CORS 处理            │
│  LogoutFilter                         ← 登出处理             │
│  JwtAuthenticationFilter              ← 自定义：JWT 验证      │
│  UsernamePasswordAuthenticationFilter ← 表单登录（可选）      │
│  RequestCacheAwareFilter                                     │
│  SecurityContextHolderAwareRequestFilter                     │
│  AnonymousAuthenticationFilter        ← 匿名用户处理         │
│  SessionManagementFilter                                     │
│  ExceptionTranslationFilter           ← 异常转换             │
│  AuthorizationFilter                  ← 权限验证             │
└──────────────────────────────────────────────────────────────┘
                              ↓
                      DispatcherServlet
                              ↓
                         Controller
```

### 核心组件

| 组件 | 说明 |
|------|------|
| `SecurityFilterChain` | 安全过滤器链配置 |
| `AuthenticationManager` | 认证管理器 |
| `UserDetailsService` | 加载用户信息 |
| `PasswordEncoder` | 密码编码器 |
| `SecurityContextHolder` | 安全上下文持有者 |

## 实践步骤

### 步骤 1：添加依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

添加后，Spring Security 会自动启用，所有接口都需要认证。

### 步骤 2：创建安全配置类

```java
package com.joint.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（前后端分离项目不需要）
            .csrf(csrf -> csrf.disable())
            // 禁用 Session（使用 JWT 无状态认证）
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 允许匿名访问的路径
                .requestMatchers("/auth/login", "/auth/register").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
```

### 步骤 3：理解配置项

```java
// 禁用 CSRF
// CSRF 保护是针对表单提交的，前后端分离使用 Token 认证，不需要
.csrf(csrf -> csrf.disable())

// 无状态 Session
// JWT 自包含用户信息，服务端不需要存储 Session
.sessionManagement(session ->
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

// 请求授权配置
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/public/**").permitAll()     // 完全公开
    .requestMatchers("/admin/**").hasRole("ADMIN") // 需要 ADMIN 角色
    .anyRequest().authenticated()                   // 其他需要登录
)
```

### 步骤 4：白名单配置

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 不需要认证的路径
    private static final String[] WHITE_LIST = {
        "/auth/login",
        "/auth/register",
        "/auth/captcha",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/webjars/**",
        "/static/**",
        "/favicon.ico"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(WHITE_LIST).permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
```

### 步骤 5：CORS 配置

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // ... 其他配置
        // CORS 配置
        .cors(cors -> cors.configurationSource(corsConfigurationSource()));

    return http.build();
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // 前端地址
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### 步骤 6：异常处理

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // ... 其他配置
        // 异常处理
        .exceptionHandling(exception -> exception
            // 未认证时的处理（401）
            .authenticationEntryPoint((request, response, authException) -> {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(
                    "{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}"
                );
            })
            // 无权限时的处理（403）
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(
                    "{\"code\":403,\"message\":\"没有权限访问\",\"data\":null}"
                );
            })
        );

    return http.build();
}
```

## SecurityContext 概念

Spring Security 使用 `SecurityContextHolder` 存储当前用户信息：

```java
// 获取当前认证信息
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

// 获取用户名
String username = authentication.getName();

// 获取用户详情
Object principal = authentication.getPrincipal();
if (principal instanceof UserDetails) {
    UserDetails userDetails = (UserDetails) principal;
    String username = userDetails.getUsername();
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
}
```

## 与 Nexus 对照

| Nexus | Spring Security |
|-------|-----------------|
| `main.ts` 中 `app.useGlobalGuards()` | `SecurityFilterChain` Bean |
| `AuthGuard.canActivate()` | Filter.doFilter() |
| `request.user` | `SecurityContextHolder.getContext().getAuthentication()` |
| `throw new UnauthorizedException()` | `authenticationEntryPoint` |
| `throw new ForbiddenException()` | `accessDeniedHandler` |

## 知识点总结

| 概念 | 说明 |
|------|------|
| SecurityFilterChain | 安全过滤器链 |
| 过滤器链 | 请求依次通过的安全检查点 |
| CSRF | 跨站请求伪造保护 |
| SessionCreationPolicy | Session 创建策略 |
| PasswordEncoder | 密码编码器 |
| SecurityContextHolder | 安全上下文持有者 |

## 练习任务

1. 添加 Spring Security 依赖
2. 创建 SecurityConfig 配置类
3. 配置白名单路径
4. 测试未认证时的响应
