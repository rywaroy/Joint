# 5.1 Redis 缓存

## 学习目标

- 配置 Spring Data Redis
- 使用 RedisTemplate 操作 Redis
- 使用 @Cacheable 注解缓存

## 与 Nexus 对照

Nexus Redis 使用：

```typescript
@Injectable()
export class RedisService {
    constructor(@InjectRedis() private readonly redis: Redis) {}

    async get(key: string): Promise<string | null> {
        return this.redis.get(key);
    }

    async set(key: string, value: string, ttl?: number): Promise<void> {
        if (ttl) {
            await this.redis.setex(key, ttl, value);
        } else {
            await this.redis.set(key, value);
        }
    }
}
```

## 实践步骤

### 步骤 1：添加依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 步骤 2：配置 Redis

```yaml
# application.yml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:        # 如果有密码
      database: 0
      timeout: 10000   # 连接超时时间
```

### 步骤 3：配置 RedisTemplate

```java
package com.joint.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Key 使用 String 序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value 使用 JSON 序列化
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        Jackson2JsonRedisSerializer<Object> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
```

### 步骤 4：创建 Redis 工具类

```java
package com.joint.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存（带过期时间）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断 key 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取过期时间
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }
}
```

### 步骤 5：使用缓存注解

启用缓存：

```java
@SpringBootApplication
@EnableCaching  // 启用缓存
public class JointApplication { }
```

配置缓存管理器：

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))  // 默认过期时间
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }
}
```

在 Service 中使用：

```java
@Service
public class UserServiceImpl implements UserService {

    @Cacheable(value = "user", key = "#id")
    public User findById(String id) {
        // 首次调用查询数据库，之后从缓存获取
        return userMapper.selectById(id);
    }

    @CachePut(value = "user", key = "#result.id")
    public User create(CreateUserDto dto) {
        // 创建后更新缓存
        User user = new User();
        // ...
        userMapper.insert(user);
        return user;
    }

    @CacheEvict(value = "user", key = "#id")
    public void delete(String id) {
        // 删除时清除缓存
        userMapper.deleteById(id);
    }

    @CacheEvict(value = "user", allEntries = true)
    public void clearCache() {
        // 清除所有 user 缓存
    }
}
```

### 缓存注解说明

| 注解 | 说明 |
|------|------|
| `@Cacheable` | 查询时缓存，存在则返回缓存 |
| `@CachePut` | 更新缓存 |
| `@CacheEvict` | 删除缓存 |
| `@Caching` | 组合多个缓存操作 |

### 步骤 6：实现 Token 黑名单（登出使 Token 失效）

```java
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisUtils redisUtils;
    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * 将 Token 加入黑名单
     */
    public void addToBlacklist(String token, long expireSeconds) {
        String key = BLACKLIST_PREFIX + token;
        redisUtils.set(key, "1", expireSeconds, TimeUnit.SECONDS);
    }

    /**
     * 检查 Token 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisUtils.hasKey(key));
    }
}
```

在 JwtAuthenticationFilter 中检查：

```java
if (tokenBlacklistService.isBlacklisted(token)) {
    throw new BusinessException(401, "Token 已失效");
}
```

## 知识点总结

| 概念 | 说明 |
|------|------|
| RedisTemplate | Redis 操作模板 |
| @Cacheable | 缓存查询结果 |
| @CacheEvict | 清除缓存 |
| CacheManager | 缓存管理器 |

## 练习任务

1. 配置 Redis 连接
2. 实现 RedisUtils 工具类
3. 为用户查询添加缓存
4. 实现 Token 黑名单功能
