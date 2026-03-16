package org.joint.auth;

import org.joint.common.security.JwtAuthenticationFilter;
import org.joint.common.security.JwtTokenProvider;
import org.joint.common.security.LoginUser;
import org.joint.common.security.SecurityExceptionHandlers;
import org.joint.common.security.TokenBlacklistService;
import org.joint.config.JwtProperties;
import org.joint.config.SecurityConfig;
import org.joint.modules.system.dept.mapper.DeptMapper;
import org.joint.modules.system.menu.mapper.MenuMapper;
import org.joint.modules.system.operlog.mapper.OperLogMapper;
import org.joint.modules.system.post.mapper.PostMapper;
import org.joint.modules.system.post.mapper.UserPostMapper;
import org.joint.modules.system.role.mapper.RoleMapper;
import org.joint.modules.system.role.mapper.RoleMenuMapper;
import org.joint.modules.system.user.UserController;
import org.joint.modules.system.user.UserService;
import org.joint.modules.system.user.mapper.UserMapper;
import org.joint.modules.system.user.mapper.UserRoleMapper;
import org.joint.modules.system.user.vo.UserVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class, JwtProperties.class, SecurityExceptionHandlers.class})
@TestPropertySource(properties = {
        "jwt.secret=joint-phase-3-secret-key-must-be-at-least-32",
        "jwt.expiration=86400000"
})
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private OperLogMapper operLogMapper;

    @MockitoBean
    private CacheManager cacheManager;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private UserRoleMapper userRoleMapper;

    @MockitoBean
    private RoleMapper roleMapper;

    @MockitoBean
    private RoleMenuMapper roleMenuMapper;

    @MockitoBean
    private MenuMapper menuMapper;

    @MockitoBean
    private DeptMapper deptMapper;

    @MockitoBean
    private PostMapper postMapper;

    @MockitoBean
    private UserPostMapper userPostMapper;

    @Test
    void validBearerTokenAuthenticatesRequest() throws Exception {
        when(userService.findPage(any())).thenReturn(Map.of("list", List.of(), "total", 0L));

        String token = jwtTokenProvider.generateToken("u-1", "admin", Map.of("roles", List.of("admin")));
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();

        mockMvc.perform(get("/system/user/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void invalidBearerTokenStaysUnauthorized() throws Exception {
        mockMvc.perform(get("/system/user/list")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void blacklistedBearerTokenStaysUnauthorized() throws Exception {
        String token = jwtTokenProvider.generateToken("u-1", "admin", Map.of("roles", List.of("admin")));
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(true);

        mockMvc.perform(get("/system/user/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void securityChainContainsJwtAuthenticationFilter() {
        assertThat(springSecurityFilterChain.getFilterChains().stream()
                .flatMap(chain -> chain.getFilters().stream())
                .anyMatch(filter -> filter instanceof JwtAuthenticationFilter))
                .isTrue();
    }

    @Test
    void jwtAuthenticationFilterPopulatesSecurityContext() throws Exception {
        String token = jwtTokenProvider.generateToken("u-1", "admin", Map.of("roles", List.of("admin")));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        try {
            jwtAuthenticationFilter.doFilter(request, response, new MockFilterChain());

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                    .isInstanceOf(LoginUser.class);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
