package org.joint.modules.system.user;

import org.joint.modules.system.user.dto.CreateUserDto;
import org.joint.support.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void listRejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/system/user/list").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void infoReturnsCurrentAuthenticatedUser() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO sys_user (id, username, password, nick_name, avatar, status, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "u-info-1",
                "info_user",
                "encoded-password",
                "信息用户",
                "https://example.com/avatar.png",
                0,
                0
        );

        mockMvc.perform(get("/api/user/info").contextPath("/api")
                        .header("Authorization", bearerToken("u-info-1", "info_user", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("u-info-1"))
                .andExpect(jsonPath("$.data.username").value("info_user"))
                .andExpect(jsonPath("$.data.realName").value("信息用户"))
                .andExpect(jsonPath("$.data.avatar").value("https://example.com/avatar.png"))
                .andExpect(jsonPath("$.data.roles[0]").value("admin"));
    }

    @Test
    void menuRoutesReturnsDynamicRouteTree() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO sys_role (id, name, code, status, is_super, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "r-menu-admin",
                "管理员",
                "admin",
                0,
                0,
                0
        );
        jdbcTemplate.update(
                """
                INSERT INTO sys_user (id, username, password, nick_name, status, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "u-menu-1",
                "menu_user",
                "encoded-password",
                "菜单用户",
                0,
                0
        );
        jdbcTemplate.update(
                "INSERT INTO sys_user_role (id, user_id, role_id) VALUES (?, ?, ?)",
                "ur-menu-1",
                "u-menu-1",
                "r-menu-admin"
        );
        jdbcTemplate.update(
                """
                INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "m-dashboard",
                null,
                "Dashboard",
                "/dashboard",
                null,
                "lucide:layout-dashboard",
                0,
                null,
                -1,
                0,
                false,
                0
        );
        jdbcTemplate.update(
                """
                INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "m-analytics",
                "m-dashboard",
                "Analytics",
                "/analytics",
                "dashboard/analytics/index",
                "lucide:chart",
                1,
                null,
                1,
                0,
                false,
                0
        );
        jdbcTemplate.update(
                "INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES (?, ?, ?)",
                "rm-menu-1",
                "r-menu-admin",
                "m-dashboard"
        );
        jdbcTemplate.update(
                "INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES (?, ?, ?)",
                "rm-menu-2",
                "r-menu-admin",
                "m-analytics"
        );

        mockMvc.perform(get("/api/menu/routes").contextPath("/api")
                        .header("Authorization", bearerToken("u-menu-1", "menu_user", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].name").value("Dashboard"))
                .andExpect(jsonPath("$.data[0].component").value("BasicLayout"))
                .andExpect(jsonPath("$.data[0].redirect").value("/analytics"))
                .andExpect(jsonPath("$.data[0].children[0].name").value("Analytics"))
                .andExpect(jsonPath("$.data[0].children[0].component").value("dashboard/analytics/index"));
    }

    @Test
    void menuCodesReturnsCurrentUserAccessCodes() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO sys_role (id, name, code, status, is_super, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "r-code-1",
                "编辑员",
                "editor",
                0,
                0,
                0
        );
        jdbcTemplate.update(
                """
                INSERT INTO sys_user (id, username, password, nick_name, status, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "u-code-1",
                "code_user",
                "encoded-password",
                "权限用户",
                0,
                0
        );
        jdbcTemplate.update(
                "INSERT INTO sys_user_role (id, user_id, role_id) VALUES (?, ?, ?)",
                "ur-code-1",
                "u-code-1",
                "r-code-1"
        );
        jdbcTemplate.update(
                """
                INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "m-code-1",
                null,
                "SystemUserList",
                "#",
                null,
                null,
                2,
                "system:user:list",
                1,
                0,
                false,
                0
        );
        jdbcTemplate.update(
                "INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES (?, ?, ?)",
                "rm-code-1",
                "r-code-1",
                "m-code-1"
        );

        mockMvc.perform(get("/api/menu/codes").contextPath("/api")
                        .header("Authorization", bearerToken("u-code-1", "code_user", "editor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0]").value("system:user:list"));
    }

    @Test
    void createReturnsValidationErrorsForMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/system/user").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUserDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("用户名不能为空")));
    }

    @Test
    void createPersistsUserThroughRealServiceAndMapper() throws Exception {
        CreateUserDto dto = new CreateUserDto();
        dto.setUsername("integration_user");
        dto.setPassword("secret12");
        dto.setNickName("集成测试用户");

        mockMvc.perform(post("/api/system/user").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("integration_user"));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE username = ?",
                Integer.class,
                "integration_user"
        );
        org.assertj.core.api.Assertions.assertThat(count).isEqualTo(1);
    }

    @Test
    void listReturnsPagedUsersFromDatabase() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO sys_user (id, username, password, nick_name, status, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "u-list-1",
                "paged_user",
                "encoded-password",
                "分页用户",
                0,
                0
        );

        mockMvc.perform(get("/api/system/user/list").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.data[0].username").value("paged_user"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
    }
}
