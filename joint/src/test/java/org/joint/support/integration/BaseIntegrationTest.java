package org.joint.support.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.joint.common.security.JwtTokenProvider;
import org.joint.common.security.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUpAuthenticationDefaults() {
        when(tokenBlacklistService.isBlacklisted(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM sys_oper_log");
        jdbcTemplate.update("DELETE FROM sys_user");
    }

    protected String bearerToken(String userId, String username, String... roles) {
        return "Bearer " + jwtTokenProvider.generateToken(
                userId,
                username,
                Map.of("roles", List.of(roles))
        );
    }
}
