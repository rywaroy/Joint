package org.joint.auth;

import org.joint.common.response.PageResult;
import org.joint.common.security.JwtAuthenticationFilter;
import org.joint.common.security.JwtTokenProvider;
import org.joint.common.security.PermissionAspect;
import org.joint.common.security.PermissionService;
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
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({
        AopAutoConfiguration.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenProvider.class,
        JwtProperties.class,
        SecurityExceptionHandlers.class,
        PermissionAspect.class
})
@TestPropertySource(properties = {
        "jwt.secret=joint-phase-3-secret-key-must-be-at-least-32",
        "jwt.expiration=86400000"
})
class PermissionAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PermissionService permissionService;

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
    void userWithRequiredPermissionCanAccessList() throws Exception {
        PageResult<UserVo> page = new PageResult<>();
        page.setData(List.of());
        page.setTotal(0L);
        page.setPage(1L);
        page.setSize(10L);
        when(userService.findPage(any())).thenReturn(page);
        when(permissionService.getUserPermissions("u-1")).thenReturn(Set.of("system:user:list"));

        String token = jwtTokenProvider.generateToken("u-1", "editor", Map.of("roles", List.of("editor")));

        mockMvc.perform(get("/system/user/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void userWithoutRequiredPermissionGetsForbidden() throws Exception {
        when(permissionService.getUserPermissions("u-1")).thenReturn(Set.of());

        String token = jwtTokenProvider.generateToken("u-1", "guest", Map.of("roles", List.of("guest")));

        mockMvc.perform(get("/system/user/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void adminRoleBypassesPermissionCheck() throws Exception {
        PageResult<UserVo> page = new PageResult<>();
        page.setData(List.of());
        page.setTotal(0L);
        page.setPage(1L);
        page.setSize(10L);
        when(userService.findPage(any())).thenReturn(page);

        String token = jwtTokenProvider.generateToken("u-1", "admin", Map.of("roles", List.of("admin")));

        mockMvc.perform(get("/system/user/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
