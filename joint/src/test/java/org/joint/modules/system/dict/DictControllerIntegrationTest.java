package org.joint.modules.system.dict;

import org.joint.common.utils.RedisUtils;
import org.joint.support.integration.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DictControllerIntegrationTest extends BaseIntegrationTest {

    @MockitoBean
    private RedisUtils redisUtils;

    @BeforeEach
    void setUpRedisDefaults() {
        when(redisUtils.get(anyString())).thenReturn(null);
        when(redisUtils.delete(anyString())).thenReturn(true);
    }

    @AfterEach
    void cleanDictTables() {
        jdbcTemplate.update("DELETE FROM dict_data");
        jdbcTemplate.update("DELETE FROM dict_types");
    }

    @Test
    void typeListReturnsNexusDictPageShape() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO dict_types (id, dictName, dictType, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "type-list-1",
                "状态",
                "sys_normal_disable",
                0,
                "系统状态"
        );

        mockMvc.perform(get("/api/system/dict/type/list").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("dictName", "状态"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].id").value("type-list-1"))
                .andExpect(jsonPath("$.data.list[0].dictName").value("状态"))
                .andExpect(jsonPath("$.data.list[0].dictType").value("sys_normal_disable"))
                .andExpect(jsonPath("$.data.list[0].status").value(0))
                .andExpect(jsonPath("$.data.list[0].remark").value("系统状态"))
                .andExpect(jsonPath("$.data.list[0].createTime").isNotEmpty())
                .andExpect(jsonPath("$.data.list[0].createdAt").doesNotExist())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.page").doesNotExist())
                .andExpect(jsonPath("$.data.size").doesNotExist());
    }

    @Test
    void typeOptionsReturnNexusOptionShape() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO dict_types (id, dictName, dictType, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "type-option-1",
                "用户性别",
                "sys_user_sex",
                0,
                "性别字典"
        );

        mockMvc.perform(get("/api/system/dict/type/options").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value("type-option-1"))
                .andExpect(jsonPath("$.data[0].dictName").value("用户性别"))
                .andExpect(jsonPath("$.data[0].dictType").value("sys_user_sex"));
    }

    @Test
    void createTypeReturnsNexusDictTypeShape() throws Exception {
        mockMvc.perform(post("/api/system/dict/type").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dictName": "通知类型",
                                  "dictType": "sys_notice_type"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.dictName").value("通知类型"))
                .andExpect(jsonPath("$.data.dictType").value("sys_notice_type"))
                .andExpect(jsonPath("$.data.status").value(0))
                .andExpect(jsonPath("$.data.remark").value(""))
                .andExpect(jsonPath("$.data.createTime").isNotEmpty());
    }

    @Test
    void deleteTypeReturnsDeletedIdPayload() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO dict_types (id, dictName, dictType, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "type-delete-1",
                "通知状态",
                "sys_notice_status",
                0,
                "待删除"
        );

        mockMvc.perform(delete("/api/system/dict/type/type-delete-1").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("type-delete-1"));
    }

    @Test
    void dataListReturnsNexusDictDataPageShape() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO dict_types (id, dictName, dictType, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "type-data-list-1",
                "状态",
                "sys_normal_disable",
                0,
                "系统状态"
        );
        jdbcTemplate.update(
                """
                INSERT INTO dict_data (id, typeId, dictLabel, dictValue, dictSort, cssClass, listClass, isDefault, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "data-list-1",
                "type-data-list-1",
                "启用",
                "0",
                1,
                "",
                "success",
                true,
                0,
                "正常状态"
        );

        mockMvc.perform(get("/api/system/dict/data/list").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("dictType", "sys_normal_disable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].id").value("data-list-1"))
                .andExpect(jsonPath("$.data.list[0].typeId").value("type-data-list-1"))
                .andExpect(jsonPath("$.data.list[0].dictName").value("状态"))
                .andExpect(jsonPath("$.data.list[0].dictType").value("sys_normal_disable"))
                .andExpect(jsonPath("$.data.list[0].dictLabel").value("启用"))
                .andExpect(jsonPath("$.data.list[0].dictValue").value("0"))
                .andExpect(jsonPath("$.data.list[0].dictSort").value(1))
                .andExpect(jsonPath("$.data.list[0].listClass").value("success"))
                .andExpect(jsonPath("$.data.list[0].isDefault").value(true))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void dataByTypeReturnsNexusDictDataShapeAndBackfillsCache() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO dict_types (id, dictName, dictType, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "type-data-type-1",
                "状态",
                "sys_normal_disable",
                0,
                "系统状态"
        );
        jdbcTemplate.update(
                """
                INSERT INTO dict_data (id, typeId, dictLabel, dictValue, dictSort, cssClass, listClass, isDefault, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "data-type-1",
                "type-data-type-1",
                "启用",
                "0",
                1,
                "",
                "success",
                true,
                0,
                ""
        );

        mockMvc.perform(get("/api/system/dict/data/type/sys_normal_disable").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value("data-type-1"))
                .andExpect(jsonPath("$.data[0].dictName").value("状态"))
                .andExpect(jsonPath("$.data[0].dictType").value("sys_normal_disable"))
                .andExpect(jsonPath("$.data[0].dictLabel").value("启用"))
                .andExpect(jsonPath("$.data[0].dictValue").value("0"));

        verify(redisUtils).set(eq("system:dict:sys_normal_disable"), any(), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    void createDataRejectsMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/system/dict/data").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dictSort": 0,
                                  "status": 0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("字典类型不能为空")))
                .andExpect(jsonPath("$.message").value(containsString("字典标签不能为空")))
                .andExpect(jsonPath("$.message").value(containsString("字典键值不能为空")));
    }

    @Test
    void updateDataAllowsPartialStatusOnlyPayload() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO dict_types (id, dictName, dictType, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "type-update-1",
                "状态",
                "sys_normal_disable",
                0,
                "系统状态"
        );
        jdbcTemplate.update(
                """
                INSERT INTO dict_data (id, typeId, dictLabel, dictValue, dictSort, cssClass, listClass, isDefault, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "data-update-1",
                "type-update-1",
                "启用",
                "0",
                1,
                "",
                "success",
                true,
                0,
                "初始备注"
        );

        mockMvc.perform(put("/api/system/dict/data/data-update-1").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("data-update-1"))
                .andExpect(jsonPath("$.data.dictLabel").value("启用"))
                .andExpect(jsonPath("$.data.dictValue").value("0"))
                .andExpect(jsonPath("$.data.status").value(1));
    }

    @Test
    void deleteDataReturnsDeletedIdPayload() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO dict_types (id, dictName, dictType, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "type-delete-data-1",
                "状态",
                "sys_normal_disable",
                0,
                "系统状态"
        );
        jdbcTemplate.update(
                """
                INSERT INTO dict_data (id, typeId, dictLabel, dictValue, dictSort, cssClass, listClass, isDefault, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "data-delete-1",
                "type-delete-data-1",
                "启用",
                "0",
                1,
                "",
                "success",
                true,
                0,
                ""
        );

        mockMvc.perform(delete("/api/system/dict/data/data-delete-1").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("data-delete-1"));
    }

    @Test
    void refreshCacheReturnsClearedCount() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO dict_types (id, dictName, dictType, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "type-cache-1",
                "状态",
                "sys_normal_disable",
                0,
                "系统状态"
        );
        jdbcTemplate.update(
                """
                INSERT INTO dict_types (id, dictName, dictType, status, remark, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "type-cache-2",
                "性别",
                "sys_user_sex",
                0,
                "用户性别"
        );

        mockMvc.perform(delete("/api/system/dict/cache").contextPath("/api")
                        .header("Authorization", bearerToken("u-admin", "admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.cleared").value(2));
    }
}
