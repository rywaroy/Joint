package org.joint.modules.system.dept;

import org.joint.support.integration.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeptControllerIntegrationTest extends BaseIntegrationTest {

    @AfterEach
    void cleanDeptTable() {
        jdbcTemplate.update("DELETE FROM depts");
    }

    @Test
    void listReturnsNexusDeptTreeShape() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO depts (id, pid, name, status, remark, treePath, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "d-root",
                null,
                "总部",
                0,
                "根节点备注",
                "d-root"
        );
        jdbcTemplate.update(
                """
                INSERT INTO depts (id, pid, name, status, remark, treePath, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "d-child",
                "d-root",
                "研发部",
                1,
                "子节点备注",
                "d-root,d-child"
        );

        mockMvc.perform(get("/api/system/dept/list").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value("d-root"))
                .andExpect(jsonPath("$.data[0].pid").isEmpty())
                .andExpect(jsonPath("$.data[0].remark").value("根节点备注"))
                .andExpect(jsonPath("$.data[0].createTime").isNotEmpty())
                .andExpect(jsonPath("$.data[0].children[0].id").value("d-child"))
                .andExpect(jsonPath("$.data[0].children[0].pid").value("d-root"))
                .andExpect(jsonPath("$.data[0].children[0].status").value(1))
                .andExpect(jsonPath("$.data[0].children[0].remark").value("子节点备注"));
    }

    @Test
    void createAcceptsPidAndRemarkPayload() throws Exception {
        mockMvc.perform(post("/api/system/dept").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "研发部",
                                  "pid": null,
                                  "status": 0,
                                  "remark": "研发备注"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("研发部"))
                .andExpect(jsonPath("$.data.pid").isEmpty())
                .andExpect(jsonPath("$.data.status").value(0))
                .andExpect(jsonPath("$.data.remark").value("研发备注"))
                .andExpect(jsonPath("$.data.createTime").isNotEmpty());
    }

    @Test
    void updateReturnsNexusDeptNodeShape() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO depts (id, pid, name, status, remark, treePath, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "d-root",
                null,
                "总部",
                0,
                "根节点备注",
                "d-root"
        );
        jdbcTemplate.update(
                """
                INSERT INTO depts (id, pid, name, status, remark, treePath, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "d-old",
                null,
                "旧部门",
                0,
                "旧备注",
                "d-old"
        );

        mockMvc.perform(put("/api/system/dept/d-old").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "产品部",
                                  "pid": "d-root",
                                  "status": 1,
                                  "remark": "更新备注"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("d-old"))
                .andExpect(jsonPath("$.data.pid").value("d-root"))
                .andExpect(jsonPath("$.data.name").value("产品部"))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.remark").value("更新备注"))
                .andExpect(jsonPath("$.data.createTime").isNotEmpty());
    }

    @Test
    void updateWithNullPidMovesDeptToRoot() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO depts (id, pid, name, status, remark, treePath, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "d-root",
                null,
                "总部",
                0,
                "根节点备注",
                "d-root"
        );
        jdbcTemplate.update(
                """
                INSERT INTO depts (id, pid, name, status, remark, treePath, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "d-child",
                "d-root",
                "研发部",
                0,
                "子节点备注",
                "d-root,d-child"
        );

        mockMvc.perform(put("/api/system/dept/d-child").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pid": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("d-child"))
                .andExpect(jsonPath("$.data.pid").isEmpty());
    }

    @Test
    void deleteReturnsDeletedIdPayload() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO depts (id, pid, name, status, remark, treePath, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "d-delete",
                null,
                "待删除部门",
                0,
                "备注",
                "d-delete"
        );

        mockMvc.perform(delete("/api/system/dept/d-delete").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("d-delete"));
    }
}
