package org.joint.auth;

import org.joint.common.security.CurrentUserArgumentResolver;
import org.joint.common.security.JwtAuthenticationFilter;
import org.joint.common.security.JwtTokenProvider;
import org.joint.common.security.PermissionService;
import org.joint.common.security.SecurityExceptionHandlers;
import org.joint.common.security.TokenBlacklistService;
import org.joint.config.JwtProperties;
import org.joint.config.SecurityConfig;
import org.joint.config.WebMvcConfig;
import org.joint.modules.system.dept.mapper.DeptMapper;
import org.joint.modules.system.menu.CurrentMenuController;
import org.joint.modules.system.menu.MenuService;
import org.joint.modules.system.menu.mapper.MenuMapper;
import org.joint.modules.system.operlog.mapper.OperLogMapper;
import org.joint.modules.system.post.mapper.PostMapper;
import org.joint.modules.system.post.mapper.UserPostMapper;
import org.joint.modules.system.role.mapper.RoleMapper;
import org.joint.modules.system.role.mapper.RoleMenuMapper;
import org.joint.modules.system.user.UserService;
import org.joint.modules.system.user.mapper.UserMapper;
import org.joint.modules.system.user.mapper.UserRoleMapper;
import org.joint.modules.system.menu.dto.CreateMenuDto;
import org.joint.modules.system.menu.dto.UpdateMenuDto;
import org.joint.modules.system.menu.vo.MenuVo;
import org.joint.modules.system.menu.vo.MenuRouteVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrentMenuController.class)
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
class CurrentMenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private MenuService menuService;

    @MockitoBean
    private PermissionService permissionService;

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
    void listReturnsTreeMenusForManagementPage() throws Exception {
        MenuVo child = new MenuVo();
        child.setId("m-user");
        child.setParentId("m-system");
        child.setName("SystemUser");
        child.setPath("/system/user");
        child.setComponent("/system/user/list");
        child.setType(1);
        child.setStatus(0);
        child.setAuthCode("system:user:list");

        MenuVo root = new MenuVo();
        root.setId("m-system");
        root.setName("System");
        root.setPath("/system");
        root.setType(0);
        root.setStatus(0);
        root.setChildren(List.of(child));

        when(menuService.getMenuTree()).thenReturn(List.of(root));

        String token = jwtTokenProvider.generateToken("u-1", "admin", Map.of("roles", List.of("admin")));

        mockMvc.perform(get("/menu/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value("m-system"))
                .andExpect(jsonPath("$.data[0].type").value("catalog"))
                .andExpect(jsonPath("$.data[0].meta.title").value("System"))
                .andExpect(jsonPath("$.data[0].children[0].pid").value("m-system"))
                .andExpect(jsonPath("$.data[0].children[0].type").value("menu"))
                .andExpect(jsonPath("$.data[0].children[0].meta.title").value("SystemUser"));
    }

    @Test
    void treeReturnsTreeMenusForSelector() throws Exception {
        MenuVo root = new MenuVo();
        root.setId("m-system");
        root.setName("System");
        root.setType(0);
        root.setStatus(0);

        when(menuService.getMenuTree()).thenReturn(List.of(root));

        String token = jwtTokenProvider.generateToken("u-1", "admin", Map.of("roles", List.of("admin")));

        mockMvc.perform(get("/menu/tree")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value("m-system"))
                .andExpect(jsonPath("$.data[0].meta.title").value("System"));
    }

    @Test
    void nameExistsReturnsServiceResult() throws Exception {
        when(menuService.checkNameExists("SystemMenu", "m-1")).thenReturn(true);

        String token = jwtTokenProvider.generateToken("u-1", "admin", Map.of("roles", List.of("admin")));

        mockMvc.perform(get("/menu/name-exists")
                        .header("Authorization", "Bearer " + token)
                        .param("name", "SystemMenu")
                        .param("id", "m-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void pathExistsReturnsServiceResult() throws Exception {
        when(menuService.checkPathExists("/system/menu", "m-1")).thenReturn(true);

        String token = jwtTokenProvider.generateToken("u-1", "admin", Map.of("roles", List.of("admin")));

        mockMvc.perform(get("/menu/path-exists")
                        .header("Authorization", "Bearer " + token)
                        .param("path", "/system/menu")
                        .param("id", "m-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void getByIdReturnsFrontendMenuShape() throws Exception {
        MenuVo menu = new MenuVo();
        menu.setId("m-1");
        menu.setParentId("m-root");
        menu.setName("SystemMenu");
        menu.setPath("/system/menu");
        menu.setComponent("/system/menu/list");
        menu.setType(1);
        menu.setStatus(0);
        menu.setAuthCode("system:menu:list");

        when(menuService.findById("m-1")).thenReturn(menu);

        String token = jwtTokenProvider.generateToken("u-1", "admin", Map.of("roles", List.of("admin")));

        mockMvc.perform(get("/menu/m-1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("m-1"))
                .andExpect(jsonPath("$.data.pid").value("m-root"))
                .andExpect(jsonPath("$.data.type").value("menu"))
                .andExpect(jsonPath("$.data.meta.title").value("SystemMenu"));
    }

    @Test
    void createAcceptsFrontendMenuPayload() throws Exception {
        MenuVo created = new MenuVo();
        created.setId("m-new");
        created.setParentId("m-root");
        created.setName("SystemMenu");
        created.setPath("/system/menu");
        created.setComponent("/system/menu/list");
        created.setType(1);
        created.setStatus(0);
        created.setAuthCode("system:menu:list");

        when(menuService.create(argThat(dto ->
                "m-root".equals(dto.getParentId())
                        && "SystemMenu".equals(dto.getName())
                        && "/system/menu".equals(dto.getPath())
                        && "/system/menu/list".equals(dto.getComponent())
                        && Integer.valueOf(1).equals(dto.getType())
                        && "system:menu:list".equals(dto.getAuthCode())
                        && Integer.valueOf(3).equals(dto.getSort())
                        && Integer.valueOf(0).equals(dto.getStatus())
                        && Boolean.TRUE.equals(dto.getHidden())
                        && "carbon:menu".equals(dto.getIcon())
        ))).thenReturn(created);

        String token = jwtTokenProvider.generateToken("u-1", "admin", Map.of("roles", List.of("admin")));

        mockMvc.perform(post("/menu")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "parentId": "m-root",
                                  "name": "SystemMenu",
                                  "title": "system.menu.title",
                                  "path": "/system/menu",
                                  "component": "/system/menu/list",
                                  "type": "menu",
                                  "authCode": "system:menu:list",
                                  "order": 3,
                                  "status": 0,
                                  "icon": "carbon:menu",
                                  "hideInMenu": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("m-new"))
                .andExpect(jsonPath("$.data.pid").value("m-root"))
                .andExpect(jsonPath("$.data.type").value("menu"))
                .andExpect(jsonPath("$.data.meta.title").value("SystemMenu"));
    }

    @Test
    void updateAcceptsFrontendMenuPayload() throws Exception {
        MenuVo updated = new MenuVo();
        updated.setId("m-1");
        updated.setParentId("m-root");
        updated.setName("SystemMenu");
        updated.setPath("/system/menu");
        updated.setComponent("/system/menu/list");
        updated.setType(1);
        updated.setStatus(0);

        when(menuService.update(eq("m-1"), argThat(dto ->
                "m-root".equals(dto.getParentId())
                        && "SystemMenu".equals(dto.getName())
                        && "/system/menu".equals(dto.getPath())
                        && Integer.valueOf(4).equals(dto.getSort())
                        && Boolean.FALSE.equals(dto.getHidden())
                        && Integer.valueOf(1).equals(dto.getType())
        ))).thenReturn(updated);

        String token = jwtTokenProvider.generateToken("u-1", "admin", Map.of("roles", List.of("admin")));

        mockMvc.perform(put("/menu/m-1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pid": "m-root",
                                  "name": "SystemMenu",
                                  "title": "system.menu.title",
                                  "path": "/system/menu",
                                  "component": "/system/menu/list",
                                  "type": "menu",
                                  "order": 4,
                                  "status": 0,
                                  "hideInMenu": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("m-1"))
                .andExpect(jsonPath("$.data.pid").value("m-root"));
    }

    @Test
    void deleteDelegatesToService() throws Exception {
        String token = jwtTokenProvider.generateToken("u-1", "admin", Map.of("roles", List.of("admin")));

        mockMvc.perform(delete("/menu/m-1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(menuService).delete("m-1");
    }

    @Test
    void routesReturnsCurrentUserRoutes() throws Exception {
        MenuRouteVo child = new MenuRouteVo();
        child.setName("Analytics");
        child.setPath("/analytics");
        child.setComponent("dashboard/analytics/index");
        child.setMeta(Map.of("title", "Analytics", "order", 1));

        MenuRouteVo root = new MenuRouteVo();
        root.setName("Dashboard");
        root.setPath("/dashboard");
        root.setComponent("BasicLayout");
        root.setRedirect("/analytics");
        root.setMeta(Map.of("title", "Dashboard", "order", -1));
        root.setChildren(List.of(child));

        when(menuService.getCurrentUserRoutes("u-1")).thenReturn(List.of(root));

        String token = jwtTokenProvider.generateToken("u-1", "admin", Map.of("roles", List.of("admin")));

        mockMvc.perform(get("/menu/routes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].name").value("Dashboard"))
                .andExpect(jsonPath("$.data[0].component").value("BasicLayout"))
                .andExpect(jsonPath("$.data[0].redirect").value("/analytics"))
                .andExpect(jsonPath("$.data[0].children[0].component").value("dashboard/analytics/index"));
    }

    @Test
    void routesRejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/menu/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void codesReturnsCurrentUserAccessCodes() throws Exception {
        when(permissionService.getUserPermissions("u-1")).thenReturn(java.util.Set.of("system:menu:list", "system:user:list"));

        String token = jwtTokenProvider.generateToken("u-1", "admin", Map.of("roles", List.of("admin")));

        mockMvc.perform(get("/menu/codes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0]").value("system:menu:list"))
                .andExpect(jsonPath("$.data[1]").value("system:user:list"));
    }
}
