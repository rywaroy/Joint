package org.joint.modules.system.dict;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.joint.common.exception.BusinessException;
import org.joint.common.utils.RedisUtils;
import org.joint.modules.system.dict.dto.CreateDictDataDto;
import org.joint.modules.system.dict.dto.CreateDictTypeDto;
import org.joint.modules.system.dict.dto.QueryDictTypeDto;
import org.joint.modules.system.dict.entity.DictData;
import org.joint.modules.system.dict.entity.DictType;
import org.joint.modules.system.dict.mapper.DictDataMapper;
import org.joint.modules.system.dict.mapper.DictTypeMapper;
import org.joint.modules.system.dict.vo.DictDataVo;
import org.joint.modules.system.dict.vo.DictTypeVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DictServiceTest {

    private DictTypeMapper dictTypeMapper;
    private DictDataMapper dictDataMapper;
    private RedisUtils redisUtils;
    private DictService dictService;

    @BeforeEach
    void setUp() {
        dictTypeMapper = mock(DictTypeMapper.class);
        dictDataMapper = mock(DictDataMapper.class);
        redisUtils = mock(RedisUtils.class);
        dictService = new DictService(dictTypeMapper, dictDataMapper, redisUtils);
    }

    @Test
    void findTypeListReturnsNexusDictTypePageShape() {
        DictType dictType = buildType("type-1", "状态", "sys_normal_disable");
        Page<DictType> page = new Page<>(1, 10);
        page.setRecords(List.of(dictType));
        page.setTotal(1);

        when(dictTypeMapper.selectPage(any(), any())).thenReturn(page);

        Map<String, Object> result = dictService.findTypeList(new QueryDictTypeDto());
        @SuppressWarnings("unchecked")
        List<DictTypeVo> list = (List<DictTypeVo>) result.get("list");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo("type-1");
        assertThat(list.get(0).getDictName()).isEqualTo("状态");
        assertThat(list.get(0).getDictType()).isEqualTo("sys_normal_disable");
        assertThat(list.get(0).getStatus()).isEqualTo(0);
        assertThat(list.get(0).getRemark()).isEqualTo("系统状态");
        assertThat(list.get(0).getCreateTime()).isEqualTo("2026-03-01T00:00");
        assertThat(result).containsEntry("total", 1L);
    }

    @Test
    void findDictDataByTypeReturnsCachedPayloadWithoutQueryingDatabase() {
        DictDataVo cached = new DictDataVo();
        cached.setId("data-1");
        cached.setDictType("sys_normal_disable");
        cached.setDictLabel("启用");
        cached.setDictValue("0");
        cached.setDictSort(1);

        when(redisUtils.get("system:dict:sys_normal_disable")).thenReturn(List.of(cached));

        List<DictDataVo> result = dictService.findDictDataByType("sys_normal_disable");

        assertThat(result).containsExactly(cached);
        verify(dictTypeMapper, never()).selectOne(any());
        verify(dictDataMapper, never()).selectList(any());
    }

    @Test
    void findDictDataByTypeLoadsFromDatabaseAndBackfillsCache() {
        DictType dictType = buildType("type-1", "状态", "sys_normal_disable");
        DictData dictData = buildData("data-1", "type-1", "启用", "0");

        when(redisUtils.get("system:dict:sys_normal_disable")).thenReturn(null);
        when(dictTypeMapper.selectOne(any())).thenReturn(dictType);
        when(dictDataMapper.selectList(any())).thenReturn(List.of(dictData));

        List<DictDataVo> result = dictService.findDictDataByType("sys_normal_disable");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDictName()).isEqualTo("状态");
        assertThat(result.get(0).getDictType()).isEqualTo("sys_normal_disable");
        assertThat(result.get(0).getDictLabel()).isEqualTo("启用");
        assertThat(result.get(0).getDictValue()).isEqualTo("0");
        verify(redisUtils).set(
                eq("system:dict:sys_normal_disable"),
                eq(result),
                eq(3600L),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    void createRejectsDuplicateDictType() {
        CreateDictTypeDto dto = new CreateDictTypeDto();
        dto.setDictName("状态");
        dto.setDictType("sys_normal_disable");

        when(dictTypeMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> dictService.createType(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("字典类型已存在");
    }

    @Test
    void createDataRejectsDuplicateDictLabelWithinSameType() {
        CreateDictDataDto dto = new CreateDictDataDto();
        dto.setTypeId("type-1");
        dto.setDictLabel("启用");
        dto.setDictValue("0");

        when(dictTypeMapper.selectById("type-1")).thenReturn(buildType("type-1", "状态", "sys_normal_disable"));
        when(dictDataMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> dictService.createData(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("字典标签已存在");
    }

    @Test
    void refreshCacheDeletesKnownTypeKeysAndReturnsClearedCount() {
        when(dictTypeMapper.selectList(any())).thenReturn(List.of(
                buildType("type-1", "状态", "sys_normal_disable"),
                buildType("type-2", "性别", "sys_user_sex")
        ));
        when(redisUtils.delete(anyString())).thenReturn(true);

        Map<String, Integer> result = dictService.refreshCache();

        assertThat(result).containsEntry("cleared", 2);
        verify(redisUtils).delete("system:dict:sys_normal_disable");
        verify(redisUtils).delete("system:dict:sys_user_sex");
    }

    private DictType buildType(String id, String dictName, String dictType) {
        DictType type = new DictType();
        type.setId(id);
        type.setDictName(dictName);
        type.setDictType(dictType);
        type.setStatus(0);
        type.setRemark("系统状态");
        type.setCreatedAt(LocalDateTime.parse("2026-03-01T00:00:00"));
        return type;
    }

    private DictData buildData(String id, String typeId, String label, String value) {
        DictData data = new DictData();
        data.setId(id);
        data.setTypeId(typeId);
        data.setDictLabel(label);
        data.setDictValue(value);
        data.setDictSort(1);
        data.setCssClass("");
        data.setListClass("success");
        data.setIsDefault(true);
        data.setStatus(0);
        data.setRemark("");
        data.setCreatedAt(LocalDateTime.parse("2026-03-02T00:00:00"));
        return data;
    }
}
