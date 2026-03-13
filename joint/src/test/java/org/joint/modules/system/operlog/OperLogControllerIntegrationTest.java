package org.joint.modules.system.operlog;

import org.joint.support.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OperLogControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void listReturnsNodeCompatiblePagedResult() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO oper_logs (
                    id, title, businessType, method, requestMethod, operName, deptName, operUrl, operIp,
                    operLocation, operParam, jsonResult, status, errorMsg, costTime, operTime
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                "log-keep-1",
                "用户管理",
                1,
                "SystemUserController.create()",
                "POST",
                "alice",
                "研发部",
                "/api/system/user",
                "127.0.0.1",
                "",
                "{\"body\":{\"username\":\"alice\"}}",
                "{\"id\":\"u-1\"}",
                0,
                "",
                18L,
                Timestamp.from(Instant.parse("2026-03-12T08:00:00Z"))
        );
        jdbcTemplate.update(
                """
                INSERT INTO oper_logs (
                    id, title, businessType, method, requestMethod, operName, deptName, operUrl, operIp,
                    operLocation, operParam, jsonResult, status, errorMsg, costTime, operTime
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                "log-filtered-out",
                "角色管理",
                2,
                "RoleController.update()",
                "PUT",
                "bob",
                "产品部",
                "/api/system/role/r-1",
                "127.0.0.2",
                "",
                "{}",
                "{}",
                1,
                "boom",
                35L,
                Timestamp.from(Instant.parse("2026-03-10T08:00:00Z"))
        );

        mockMvc.perform(get("/api/system/log/list").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .param("title", "用户")
                        .param("operName", "ali")
                        .param("businessType", "1")
                        .param("status", "0")
                        .param("beginTime", "2026-03-12T00:00:00Z")
                        .param("endTime", "2026-03-12T23:59:59Z")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value("log-keep-1"))
                .andExpect(jsonPath("$.data.list[0].title").value("用户管理"))
                .andExpect(jsonPath("$.data.list[0].businessType").value(1))
                .andExpect(jsonPath("$.data.list[0].operName").value("alice"))
                .andExpect(jsonPath("$.data.list[0].deptName").value("研发部"))
                .andExpect(jsonPath("$.data.list[0].operUrl").value("/api/system/user"))
                .andExpect(jsonPath("$.data.list[0].operIp").value("127.0.0.1"))
                .andExpect(jsonPath("$.data.list[0].operParam").value("{\"body\":{\"username\":\"alice\"}}"))
                .andExpect(jsonPath("$.data.list[0].jsonResult").value("{\"id\":\"u-1\"}"))
                .andExpect(jsonPath("$.data.list[0].operTime").exists());
    }

    @Test
    void detailReturnsNodeCompatibleLogObject() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO oper_logs (
                    id, title, businessType, method, requestMethod, operName, deptName, operUrl, operIp,
                    operLocation, operParam, jsonResult, status, errorMsg, costTime, operTime
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                "log-detail-1",
                "岗位管理",
                3,
                "SystemPostController.remove()",
                "DELETE",
                "admin",
                "平台部",
                "/api/system/post/p-1",
                "127.0.0.3",
                "",
                "{\"params\":{\"id\":\"p-1\"}}",
                "{\"deleted\":true}",
                1,
                "删除失败",
                27L,
                Timestamp.from(Instant.parse("2026-03-13T02:00:00Z"))
        );

        mockMvc.perform(get("/api/system/log/log-detail-1").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("log-detail-1"))
                .andExpect(jsonPath("$.data.title").value("岗位管理"))
                .andExpect(jsonPath("$.data.businessType").value(3))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.errorMsg").value("删除失败"));
    }

    @Test
    void deleteRemovesBatchLogsAndReturnsDeletedCount() throws Exception {
        jdbcTemplate.update("INSERT INTO oper_logs (id, title, businessType, status, costTime, operTime) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)", "log-del-1", "用户管理", 3, 0, 1L);
        jdbcTemplate.update("INSERT INTO oper_logs (id, title, businessType, status, costTime, operTime) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)", "log-del-2", "角色管理", 3, 0, 1L);

        mockMvc.perform(delete("/api/system/log/log-del-1,log-del-2").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.deletedCount").value(2));

        Integer remaining = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM oper_logs", Integer.class);
        assertThat(remaining).isEqualTo(1);
        String title = jdbcTemplate.queryForObject("SELECT title FROM oper_logs", String.class);
        assertThat(title).isEqualTo("操作日志管理");
    }

    @Test
    void cleanRemovesAllLogsAndReturnsDeletedCount() throws Exception {
        jdbcTemplate.update("INSERT INTO oper_logs (id, title, businessType, status, costTime, operTime) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)", "log-clean-1", "用户管理", 1, 0, 1L);
        jdbcTemplate.update("INSERT INTO oper_logs (id, title, businessType, status, costTime, operTime) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)", "log-clean-2", "角色管理", 2, 0, 1L);

        mockMvc.perform(delete("/api/system/log/clean").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.deletedCount").value(2));

        Integer remaining = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM oper_logs", Integer.class);
        assertThat(remaining).isZero();
    }
}
