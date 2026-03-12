package org.joint.auth;

import org.joint.common.security.CurrentUserArgumentResolver;
import org.joint.common.security.JwtAuthenticationFilter;
import org.joint.common.security.JwtTokenProvider;
import org.joint.common.security.SecurityExceptionHandlers;
import org.joint.config.JwtProperties;
import org.joint.config.SecurityConfig;
import org.joint.config.WebMvcConfig;
import org.joint.modules.system.dept.mapper.DeptMapper;
import org.joint.modules.system.menu.mapper.MenuMapper;
import org.joint.modules.system.post.mapper.PostMapper;
import org.joint.modules.system.post.mapper.UserPostMapper;
import org.joint.modules.system.role.mapper.RoleMapper;
import org.joint.modules.system.role.mapper.RoleMenuMapper;
import org.joint.modules.system.user.UserController;
import org.joint.modules.system.user.UserService;
import org.joint.modules.system.user.entity.User;
import org.joint.modules.system.user.mapper.UserMapper;
import org.joint.modules.system.user.mapper.UserRoleMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenProvider.class,
        JwtProperties.class,
        SecurityExceptionHandlers.class,
        CurrentUserArgumentResolver.class,
        WebMvcConfig.class
})
@TestPropertySource(properties = {
        "jwt.secret=joint-phase-3-secret-key-must-be-at-least-32",
        "jwt.expiration=86400000"
})
class CurrentUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserService userService;

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
    void profileReturnsCurrentUser() throws Exception {
        User user = new User();
        user.setId("u-1");
        user.setUsername("admin");
        user.setNickName("管理员");
        user.setStatus(0);
        when(userService.findById("u-1")).thenReturn(user);

        String token = jwtTokenProvider.generateToken("u-1", "admin", Map.of("roles", List.of("admin")));

        mockMvc.perform(get("/system/user/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("u-1"))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    void profileRejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/system/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}
