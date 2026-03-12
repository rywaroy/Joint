package org.joint.common.security;

import io.jsonwebtoken.Claims;
import org.joint.common.utils.RedisUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TokenBlacklistServiceTest {

    private RedisUtils redisUtils;
    private JwtTokenProvider jwtTokenProvider;
    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        redisUtils = mock(RedisUtils.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        tokenBlacklistService = new TokenBlacklistService(redisUtils, jwtTokenProvider);
    }

    @Test
    void blacklistStoresTokenWithRemainingExpiration() {
        Claims claims = mock(Claims.class);
        Date expiration = new Date(System.currentTimeMillis() + 60_000);
        when(jwtTokenProvider.parseToken("jwt-token")).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(expiration);

        tokenBlacklistService.blacklist("jwt-token");

        verify(redisUtils).set(eq("token:blacklist:jwt-token"), eq("1"), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    void isBlacklistedChecksPrefixedRedisKey() {
        when(redisUtils.hasKey("token:blacklist:jwt-token")).thenReturn(true);

        tokenBlacklistService.isBlacklisted("jwt-token");

        verify(redisUtils).hasKey("token:blacklist:jwt-token");
    }
}
