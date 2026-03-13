package org.joint.modules.system.role;

import org.joint.support.integration.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoleControllerIntegrationTest extends BaseIntegrationTest {

    @AfterEach
    void cleanRoleTables() {
        jdbcTemplate.update("DELETE FROM sys_role_menu");
        jdbcTemplate.update("DELETE FROM sys_user_role");
        jdbcTemplate.update("DELETE FROM sys_role");
        jdbcTemplate.update("DELETE FROM sys_menu");
    }

    @Test
    void listReturnsNexusRolePageShape() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO sys_role (id, name, code, sort, status, is_super, remark, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "r-list-1",
                "运营角色",
                "ops",
                1,
                0,
                0,
                "角色备注",
                0
        );
        jdbcTemplate.update(
                """
                INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "m-role-list",
                null,
                "角色列表",
                "/system/role",
                "/system/role/list",
                null,
                2,
                "system:role:list",
                1,
                0,
                0,
                0
        );
        jdbcTemplate.update(
                """
                INSERT INTO sys_role_menu (id, role_id, menu_id)
                VALUES (?, ?, ?)
                """,
                "rm-role-list",
                "r-list-1",
                "m-role-list"
        );

        mockMvc.perform(get("/api/system/role/list").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("name", "运营"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].id").value("r-list-1"))
                .andExpect(jsonPath("$.data.list[0].name").value("运营角色"))
                .andExpect(jsonPath("$.data.list[0].remark").value("角色备注"))
                .andExpect(jsonPath("$.data.list[0].status").value(0))
                .andExpect(jsonPath("$.data.list[0].permissions[0]").value("m-role-list"))
                .andExpect(jsonPath("$.data.list[0].createTime").isNotEmpty())
                .andExpect(jsonPath("$.data.list[0].code").doesNotExist())
                .andExpect(jsonPath("$.data.list[0].sort").doesNotExist())
                .andExpect(jsonPath("$.data.list[0].createdAt").doesNotExist())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.page").doesNotExist())
                .andExpect(jsonPath("$.data.size").doesNotExist())
                .andExpect(jsonPath("$.data.data").doesNotExist());
    }

    @Test
    void createAcceptsNodePayloadWithoutCodeOrSort() throws Exception {
        mockMvc.perform(post("/api/system/role").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "审计角色",
                                  "status": 0,
                                  "remark": "审计备注",
                                  "permissions": ["*"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("审计角色"))
                .andExpect(jsonPath("$.data.status").value(0))
                .andExpect(jsonPath("$.data.remark").value("审计备注"))
                .andExpect(jsonPath("$.data.permissions[0]").value("*"))
                .andExpect(jsonPath("$.data.code").doesNotExist())
                .andExpect(jsonPath("$.data.sort").doesNotExist())
                .andExpect(jsonPath("$.data.createTime").isNotEmpty());
    }

    @Test
    void optionsReturnsPermissionsForEnabledRoles() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO sys_role (id, name, code, sort, status, is_super, remark, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "r-option-1",
                "普通角色",
                "normal",
                1,
                0,
                0,
                "启用角色",
                0
        );
        jdbcTemplate.update(
                """
                INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "m-option-1",
                null,
                "角色查询",
                "/system/role/query",
                "/system/role/detail",
                null,
                2,
                "system:role:query",
                1,
                0,
                0,
                0
        );
        jdbcTemplate.update(
                """
                INSERT INTO sys_role_menu (id, role_id, menu_id)
                VALUES (?, ?, ?)
                """,
                "rm-option-1",
                "r-option-1",
                "m-option-1"
        );

        mockMvc.perform(get("/api/system/role/options").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value("r-option-1"))
                .andExpect(jsonPath("$.data[0].permissions[0]").value("m-option-1"))
                .andExpect(jsonPath("$.data[0].code").doesNotExist())
                .andExpect(jsonPath("$.data[0].sort").doesNotExist());
    }

    @Test
    void deleteReturnsDeletedIdPayload() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO sys_role (id, name, code, sort, status, is_super, remark, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "r-delete-1",
                "待删除角色",
                "delete-me",
                1,
                0,
                0,
                "备注",
                0
        );

        mockMvc.perform(delete("/api/system/role/r-delete-1").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("r-delete-1"));
    }

    @Test
    void updateRejectsDisablingAdminRole() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO sys_role (id, name, code, sort, status, is_super, remark, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "r-admin-1",
                "admin",
                "admin",
                1,
                0,
                1,
                "内置管理员",
                0
        );

        mockMvc.perform(put("/api/system/role/r-admin-1").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("admin 角色不允许停用")));
    }
}
