# 3.2 JWT 认证实现

## 学习目标

- 理解 JWT 结构和原理
- 实现 JWT 生成和验证
- 创建 JWT 认证过滤器

## JWT 概念

### 什么是 JWT？

JWT（JSON Web Token）是一种紧凑的、自包含的令牌格式，用于在各方之间安全传输信息。

结构：`Header.Payload.Signature`

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.     <- Header（算法和类型）
eyJ1c2VySWQiOiIxMjM0NTYiLCJ1c2VybmFtZSI6ImFkbWluIn0.  <- Payload（数据）
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c  <- Signature（签名）
```

### 与 Session 对比

| Session | JWT |
|---------|-----|
| 服务端存储 | 客户端存储 |
| 有状态 | 无状态 |
| 需要 Redis 集群同步 | 天然支持分布式 |
| 服务端可主动失效 | 只能等待过期 |

### Nexus 的 JWT 实现

```typescript
// auth.service.ts
async login(dto: LoginDto) {
    const user = await this.validateUser(dto.username, dto.password);
    const payload = { id: user.id, username: user.username, roles: user.roles };
    return {
        accessToken: this.jwtService.sign(payload),
        ...user
    };
}
```

## 实践步骤

### 步骤 1：添加 JWT 依赖

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

### 步骤 2：配置 JWT 参数

```yaml
# application.yml
jwt:
  secret: your-256-bit-secret-key-here-at-least-32-characters
  expiration: 86400000  # 24小时，单位毫秒
```

```java
package com.joint.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private Long expiration;
}
```

### 步骤 3：创建 JWT 工具类

```java
package com.joint.common.security;

import com.joint.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    /**
     * 生成 Token
     */
    public String generateToken(String userId, String username, Map<String, Object> claims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析 Token
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 Token
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token 格式错误: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("Token 签名错误: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Token 无效: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public String getUserIdFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).get("username", String.class);
    }

    /**
     * 判断 Token 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = parseToken(token).getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

### 步骤 4：创建认证过滤器

```java
package com.joint.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 从请求头获取 Token
            String token = getTokenFromRequest(request);

            // 验证 Token
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                // 从 Token 中获取用户 ID
                String userId = jwtTokenProvider.getUserIdFromToken(token);

                // 加载用户信息
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置到安全上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("无法设置用户认证: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头获取 Token
     * Authorization: Bearer <token>
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

### 步骤 5：创建 UserDetailsService 实现

```java
package com.joint.common.security;

import com.joint.modules.system.user.entity.User;
import com.joint.modules.system.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // 这里 username 参数实际传入的是 userId
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }

        if (user.getStatus() == 1) {
            throw new UsernameNotFoundException("用户已被禁用: " + userId);
        }

        // 获取用户权限（后续实现）
        List<SimpleGrantedAuthority> authorities = Collections.emptyList();

        return new LoginUser(user, authorities);
    }
}
```

### 步骤 6：创建 LoginUser 类

```java
package com.joint.common.security;

import com.joint.modules.system.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class LoginUser implements UserDetails {

    private final User user;
    private final List<? extends GrantedAuthority> authorities;

    public LoginUser(User user, List<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == 0;
    }

    // 便捷方法
    public String getUserId() {
        return user.getId();
    }
}
```

### 步骤 7：注册过滤器到 Security 配置

```java
package com.joint.config;

import com.joint.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] WHITE_LIST = {
        "/auth/login",
        "/auth/register",
        "/swagger-ui/**",
        "/v3/api-docs/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(WHITE_LIST).permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}"
                    );
                })
            )
            // 添加 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

## 与 Nexus 对照

| Nexus | Joint |
|-------|-------|
| `JwtService.sign(payload)` | `jwtTokenProvider.generateToken()` |
| `JwtService.verifyAsync(token)` | `jwtTokenProvider.validateToken()` |
| `AuthGuard.extractTokenFromHeader()` | `getTokenFromRequest()` |
| `request.user = user` | `SecurityContextHolder.setAuthentication()` |

## 目录结构

```
src/main/java/com/joint/
├── config/
│   ├── JwtProperties.java
│   └── SecurityConfig.java
└── common/
    └── security/
        ├── JwtTokenProvider.java
        ├── JwtAuthenticationFilter.java
        ├── UserDetailsServiceImpl.java
        └── LoginUser.java
```

## 知识点总结

| 概念 | 说明 |
|------|------|
| JWT | JSON Web Token，自包含令牌 |
| jjwt | Java JWT 库 |
| OncePerRequestFilter | 每个请求只执行一次的过滤器 |
| UserDetailsService | 加载用户信息的服务 |
| UserDetails | 用户详情接口 |

## 练习任务

1. 添加 jjwt 依赖
2. 实现 JwtTokenProvider
3. 实现 JwtAuthenticationFilter
4. 测试 Token 生成和验证
