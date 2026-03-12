package org.joint.common.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.joint.common.utils.RedisUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    private final RedisUtils redisUtils;
    private final JwtTokenProvider jwtTokenProvider;

    public void blacklist(String token) {
        Claims claims = jwtTokenProvider.parseToken(token);
        Date expiration = claims.getExpiration();
        long remainingMillis = expiration.getTime() - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            return;
        }
        long ttlSeconds = Math.max(1, (remainingMillis + 999) / 1000);
        redisUtils.set(buildKey(token), "1", ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisUtils.hasKey(buildKey(token)));
    }

    private String buildKey(String token) {
        return BLACKLIST_PREFIX + token;
    }
}
