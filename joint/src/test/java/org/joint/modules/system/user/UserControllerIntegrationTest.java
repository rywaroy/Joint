package org.joint.modules.system.user;

import org.joint.modules.system.user.dto.CreateUserDto;
import org.junit.jupiter.api.AfterEach;
import org.joint.support.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    @AfterEach
    void cleanUserModuleTables() {
        jdbcTemplate.update("DELETE FROM user_posts");
        jdbcTemplate.update("DELETE FROM posts");
        jdbcTemplate.update("DELETE FROM user_roles");
        jdbcTemplate.update("DELETE FROM role_menus");
        jdbcTemplate.update("DELETE FROM menus");
        jdbcTemplate.update("DELETE FROM roles");
    }

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
                INSERT INTO users (id, username, password, nickName, avatar, status, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "u-info-1",
                "info_user",
                "encoded-password",
                "信息用户",
                "https://example.com/avatar.png",
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
                INSERT INTO roles (id, name, status, isBuiltin, isSuper, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "r-menu-admin",
                "admin",
                0,
                0,
                1
        );
        jdbcTemplate.update(
                """
                INSERT INTO users (id, username, password, nickName, status, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "u-menu-1",
                "menu_user",
                "encoded-password",
                "菜单用户",
                0
        );
        jdbcTemplate.update(
                "INSERT INTO user_roles (userId, roleId) VALUES (?, ?)",
                "u-menu-1",
                "r-menu-admin"
        );
        jdbcTemplate.update(
                """
                INSERT INTO menus (id, parentId, name, title, path, component, icon, type, authCode, "order", status, hideInMenu, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "m-dashboard",
                null,
                "Dashboard",
                "page.dashboard.title",
                "/dashboard",
                null,
                "lucide:layout-dashboard",
                "CATALOG",
                null,
                -1,
                0,
                false
        );
        jdbcTemplate.update(
                """
                INSERT INTO menus (id, parentId, name, title, path, component, icon, type, authCode, "order", status, hideInMenu, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "m-analytics",
                "m-dashboard",
                "Analytics",
                "page.dashboard.analytics",
                "/analytics",
                "dashboard/analytics/index",
                "lucide:chart",
                "MENU",
                null,
                1,
                0,
                false
        );
        jdbcTemplate.update(
                "INSERT INTO role_menus (roleId, menuId) VALUES (?, ?)",
                "r-menu-admin",
                "m-dashboard"
        );
        jdbcTemplate.update(
                "INSERT INTO role_menus (roleId, menuId) VALUES (?, ?)",
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
                INSERT INTO roles (id, name, status, isBuiltin, isSuper, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "r-code-1",
                "editor",
                0,
                0,
                0
        );
        jdbcTemplate.update(
                """
                INSERT INTO users (id, username, password, nickName, status, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "u-code-1",
                "code_user",
                "encoded-password",
                "权限用户",
                0
        );
        jdbcTemplate.update(
                "INSERT INTO user_roles (userId, roleId) VALUES (?, ?)",
                "u-code-1",
                "r-code-1"
        );
        jdbcTemplate.update(
                """
                INSERT INTO menus (id, parentId, name, title, path, component, icon, type, authCode, "order", status, hideInMenu, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "m-code-1",
                null,
                "SystemUserList",
                "system.user.list",
                "#",
                null,
                null,
                "BUTTON",
                "system:user:list",
                1,
                0,
                false
        );
        jdbcTemplate.update(
                "INSERT INTO role_menus (roleId, menuId) VALUES (?, ?)",
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
        jdbcTemplate.update(
                """
                INSERT INTO roles (id, name, status, isBuiltin, isSuper, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "r-create-user",
                "user",
                0,
                0,
                0
        );

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
                .andExpect(jsonPath("$.data.username").value("integration_user"))
                .andExpect(jsonPath("$.data.roles[0]").value("user"))
                .andExpect(jsonPath("$.data.postIds").isArray());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?",
                Integer.class,
                "integration_user"
        );
        org.assertj.core.api.Assertions.assertThat(count).isEqualTo(1);
    }

    @Test
    void listReturnsPagedUsersFromDatabase() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO users (id, username, password, nickName, status, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "u-list-1",
                "paged_user",
                "encoded-password",
                "分页用户",
                0
        );

        mockMvc.perform(get("/api/system/user/list").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].username").value("paged_user"))
                .andExpect(jsonPath("$.data.list[0].roles").isArray())
                .andExpect(jsonPath("$.data.list[0].postIds").isArray());
    }

    @Test
    void listSupportsPhoneAndPostFilters() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO users (id, username, password, nickName, phone, status, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "u-filter-1",
                "filter_user",
                "encoded-password",
                "过滤用户",
                "13800138000",
                0
        );
        jdbcTemplate.update(
                """
                INSERT INTO posts (id, postCode, postName, postSort, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "p-filter-1",
                "dev",
                "开发岗位",
                1,
                0,
                "开发"
        );
        jdbcTemplate.update(
                "INSERT INTO user_posts (userId, postId) VALUES (?, ?)",
                "u-filter-1",
                "p-filter-1"
        );

        mockMvc.perform(get("/api/system/user/list").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .param("phone", "13800138000")
                        .param("postId", "p-filter-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].username").value("filter_user"));
    }

    @Test
    void listSupportsRoleFilter() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO roles (id, name, status, isBuiltin, isSuper, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "r-role-filter-admin",
                "admin",
                0,
                0,
                1
        );
        jdbcTemplate.update(
                """
                INSERT INTO roles (id, name, status, isBuiltin, isSuper, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "r-role-filter-editor",
                "editor",
                0,
                0,
                0
        );
        jdbcTemplate.update(
                """
                INSERT INTO users (id, username, password, nickName, status, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "u-role-filter-1",
                "admin_user",
                "encoded-password",
                "管理员用户",
                0
        );
        jdbcTemplate.update(
                """
                INSERT INTO users (id, username, password, nickName, status, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "u-role-filter-2",
                "editor_user",
                "encoded-password",
                "编辑用户",
                0
        );
        jdbcTemplate.update(
                "INSERT INTO user_roles (userId, roleId) VALUES (?, ?)",
                "u-role-filter-1",
                "r-role-filter-admin"
        );
        jdbcTemplate.update(
                "INSERT INTO user_roles (userId, roleId) VALUES (?, ?)",
                "u-role-filter-2",
                "r-role-filter-editor"
        );

        mockMvc.perform(get("/api/system/user/list").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .param("roleId", "r-role-filter-editor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].username").value("editor_user"))
                .andExpect(jsonPath("$.data.list[0].roles[0]").value("editor"));
    }

    @Test
    void registerCreatesDefaultUserRole() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO roles (id, name, status, isBuiltin, isSuper, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "r-register-user",
                "user",
                0,
                0,
                0
        );

        mockMvc.perform(post("/api/user/register").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "register_user",
                                  "password": "secret12",
                                  "nickName": "注册用户"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("register_user"))
                .andExpect(jsonPath("$.data.roles[0]").value("user"))
                .andExpect(jsonPath("$.data.status").value(0));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?",
                Integer.class,
                "register_user"
        );
        org.assertj.core.api.Assertions.assertThat(count).isEqualTo(1);
    }

    @Test
    void changePasswordUsesCurrentUserAndValidatesOldPassword() throws Exception {
        String encodedPassword = passwordEncoder.encode("old-pass");
        jdbcTemplate.update(
                """
                INSERT INTO users (id, username, password, nickName, status, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "u-change-password-1",
                "change_user",
                encodedPassword,
                "改密用户",
                0
        );

        mockMvc.perform(put("/api/user/change-password").contextPath("/api")
                        .header("Authorization", bearerToken("u-change-password-1", "change_user", "user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword": "old-pass",
                                  "newPassword": "new-pass"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.message").value("密码修改成功"));
    }
}
