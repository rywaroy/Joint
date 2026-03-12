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
