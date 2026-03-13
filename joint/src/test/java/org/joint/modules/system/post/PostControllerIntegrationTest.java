package org.joint.modules.system.post;

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

class PostControllerIntegrationTest extends BaseIntegrationTest {

    @AfterEach
    void cleanPostTables() {
        jdbcTemplate.update("DELETE FROM user_posts");
        jdbcTemplate.update("DELETE FROM posts");
    }

    @Test
    void listReturnsNexusPostPageShape() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO posts (id, postCode, postName, postSort, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "p-list-1",
                "dev",
                "开发工程师",
                1,
                0,
                "研发岗位"
        );

        mockMvc.perform(get("/api/system/post/list").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].id").value("p-list-1"))
                .andExpect(jsonPath("$.data.list[0].postCode").value("dev"))
                .andExpect(jsonPath("$.data.list[0].postName").value("开发工程师"))
                .andExpect(jsonPath("$.data.list[0].postSort").value(1))
                .andExpect(jsonPath("$.data.list[0].status").value(0))
                .andExpect(jsonPath("$.data.list[0].remark").value("研发岗位"))
                .andExpect(jsonPath("$.data.list[0].createTime").isNotEmpty())
                .andExpect(jsonPath("$.data.list[0].createdAt").doesNotExist())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.page").doesNotExist())
                .andExpect(jsonPath("$.data.size").doesNotExist())
                .andExpect(jsonPath("$.data.data").doesNotExist());
    }

    @Test
    void createRejectsMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/system/post").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postSort": 0,
                                  "status": 0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("岗位编码不能为空")))
                .andExpect(jsonPath("$.message").value(containsString("岗位名称不能为空")));
    }

    @Test
    void createReturnsNexusPostShape() throws Exception {
        mockMvc.perform(post("/api/system/post").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postCode": "qa",
                                  "postName": "测试工程师"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.postCode").value("qa"))
                .andExpect(jsonPath("$.data.postName").value("测试工程师"))
                .andExpect(jsonPath("$.data.postSort").value(0))
                .andExpect(jsonPath("$.data.status").value(0))
                .andExpect(jsonPath("$.data.remark").value(""))
                .andExpect(jsonPath("$.data.createTime").isNotEmpty())
                .andExpect(jsonPath("$.data.createdAt").doesNotExist());
    }

    @Test
    void deleteReturnsDeletedIdPayload() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO posts (id, postCode, postName, postSort, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "p-delete-1",
                "ops",
                "运维工程师",
                1,
                0,
                "待删除岗位"
        );

        mockMvc.perform(delete("/api/system/post/p-delete-1").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("p-delete-1"));
    }

    @Test
    void updateAllowsPartialStatusOnlyPayload() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO posts (id, postCode, postName, postSort, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "p-update-1",
                "mgr",
                "产品经理",
                1,
                0,
                "初始备注"
        );

        mockMvc.perform(put("/api/system/post/p-update-1").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("p-update-1"))
                .andExpect(jsonPath("$.data.postCode").value("mgr"))
                .andExpect(jsonPath("$.data.postName").value("产品经理"))
                .andExpect(jsonPath("$.data.status").value(1));
    }
}
