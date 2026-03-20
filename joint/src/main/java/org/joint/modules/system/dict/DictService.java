package org.joint.modules.system.dict;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.joint.common.exception.BusinessException;
import org.joint.common.utils.RedisUtils;
import org.joint.modules.system.dict.dto.CreateDictDataDto;
import org.joint.modules.system.dict.dto.CreateDictTypeDto;
import org.joint.modules.system.dict.dto.QueryDictDataDto;
import org.joint.modules.system.dict.dto.QueryDictTypeDto;
import org.joint.modules.system.dict.dto.UpdateDictDataDto;
import org.joint.modules.system.dict.dto.UpdateDictTypeDto;
import org.joint.modules.system.dict.entity.DictData;
import org.joint.modules.system.dict.entity.DictType;
import org.joint.modules.system.dict.mapper.DictDataMapper;
import org.joint.modules.system.dict.mapper.DictTypeMapper;
import org.joint.modules.system.dict.vo.DictDataVo;
import org.joint.modules.system.dict.vo.DictTypeVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictService {

    private static final String DICT_CACHE_PREFIX = "system:dict:";
    private static final long DICT_CACHE_TTL_SECONDS = 3600L;

    private final DictTypeMapper dictTypeMapper;
    private final DictDataMapper dictDataMapper;
    private final RedisUtils redisUtils;

    public Map<String, Object> findTypeList(QueryDictTypeDto query) {
        Page<DictType> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<DictType> result = dictTypeMapper.selectPage(page, new LambdaQueryWrapper<DictType>()
                .like(StringUtils.hasText(query.getDictName()), DictType::getDictName, query.getDictName())
                .like(StringUtils.hasText(query.getDictType()), DictType::getDictType, query.getDictType())
                .eq(query.getStatus() != null, DictType::getStatus, query.getStatus())
                .orderByDesc(DictType::getCreatedAt));
        return Map.of(
                "list", result.getRecords().stream().map(this::toTypeVo).toList(),
                "total", result.getTotal()
        );
    }

    public List<DictTypeVo> findTypeOptions() {
        return dictTypeMapper.selectList(new LambdaQueryWrapper<DictType>()
                        .orderByAsc(DictType::getDictName)
                        .orderByAsc(DictType::getCreatedAt))
                .stream()
                .map(this::toTypeVo)
                .toList();
    }

    public DictTypeVo findTypeById(String id) {
        return toTypeVo(getExistingType(id));
    }

    public DictTypeVo createType(CreateDictTypeDto dto) {
        ensureTypeUnique(dto.getDictType(), null);

        DictType dictType = new DictType();
        dictType.setDictName(dto.getDictName());
        dictType.setDictType(dto.getDictType());
        dictType.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        dictType.setRemark(dto.getRemark() == null ? "" : dto.getRemark());
        dictTypeMapper.insert(dictType);
        return toTypeVo(dictType);
    }

    public DictTypeVo updateType(String id, UpdateDictTypeDto dto) {
        DictType dictType = getExistingType(id);
        String currentDictType = dictType.getDictType();
        if (dto.getDictType() != null && !dto.getDictType().equals(currentDictType)) {
            ensureTypeUnique(dto.getDictType(), id);
            dictType.setDictType(dto.getDictType());
        }
        if (dto.getDictName() != null) {
            dictType.setDictName(dto.getDictName());
        }
        if (dto.getStatus() != null) {
            dictType.setStatus(dto.getStatus());
        }
        if (dto.getRemark() != null) {
            dictType.setRemark(dto.getRemark());
        }

        dictTypeMapper.updateById(dictType);
        clearCacheByType(currentDictType);
        if (!currentDictType.equals(dictType.getDictType())) {
            clearCacheByType(dictType.getDictType());
        }
        return toTypeVo(dictType);
    }

    public Map<String, String> deleteType(String id) {
        DictType dictType = getExistingType(id);
        dictDataMapper.delete(new LambdaQueryWrapper<DictData>().eq(DictData::getTypeId, id));
        dictTypeMapper.deleteById(id);
        clearCacheByType(dictType.getDictType());
        return Map.of("id", id);
    }

    public Map<String, Integer> refreshCache() {
        List<DictType> dictTypes = dictTypeMapper.selectList(new LambdaQueryWrapper<>());
        for (DictType dictType : dictTypes) {
            clearCacheByType(dictType.getDictType());
        }
        return Map.of("cleared", dictTypes.size());
    }

    public Map<String, Object> findDataList(QueryDictDataDto query) {
        String effectiveTypeId = resolveTypeId(query.getTypeId(), query.getDictType());
        if (effectiveTypeId == null && StringUtils.hasText(query.getDictType())) {
            return Map.of("list", List.of(), "total", 0L);
        }

        Page<DictData> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<DictData> result = dictDataMapper.selectPage(page, new LambdaQueryWrapper<DictData>()
                .eq(StringUtils.hasText(effectiveTypeId), DictData::getTypeId, effectiveTypeId)
                .like(StringUtils.hasText(query.getDictLabel()), DictData::getDictLabel, query.getDictLabel())
                .eq(query.getStatus() != null, DictData::getStatus, query.getStatus())
                .orderByAsc(DictData::getDictSort)
                .orderByAsc(DictData::getCreatedAt));
        Map<String, DictType> typeMap = buildTypeMap(result.getRecords().stream()
                .map(DictData::getTypeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        return Map.of(
                "list", result.getRecords().stream()
                        .map(data -> toDataVo(data, typeMap.get(data.getTypeId())))
                        .toList(),
                "total", result.getTotal()
        );
    }

    public DictDataVo findDataById(String id) {
        DictData dictData = getExistingData(id);
        return toDataVo(dictData, getExistingType(dictData.getTypeId()));
    }

    public List<DictDataVo> findDictDataByType(String dictType) {
        List<DictDataVo> cached = redisUtils.get(getCacheKey(dictType));
        if (cached != null) {
            return cached;
        }

        DictType type = getTypeByDictType(dictType);
        if (type == null) {
            return List.of();
        }

        List<DictDataVo> result = dictDataMapper.selectList(new LambdaQueryWrapper<DictData>()
                        .eq(DictData::getTypeId, type.getId())
                        .orderByAsc(DictData::getDictSort)
                        .orderByAsc(DictData::getCreatedAt))
                .stream()
                .map(data -> toDataVo(data, type))
                .toList();
        redisUtils.set(getCacheKey(dictType), result, DICT_CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        return result;
    }

    public DictDataVo createData(CreateDictDataDto dto) {
        DictType dictType = getExistingType(dto.getTypeId());
        ensureDataLabelUnique(dto.getTypeId(), dto.getDictLabel(), null);

        DictData dictData = new DictData();
        dictData.setTypeId(dto.getTypeId());
        dictData.setDictLabel(dto.getDictLabel());
        dictData.setDictValue(dto.getDictValue());
        dictData.setDictSort(dto.getDictSort() == null ? 0 : dto.getDictSort());
        dictData.setCssClass(dto.getCssClass() == null ? "" : dto.getCssClass());
        dictData.setListClass(dto.getListClass() == null ? "" : dto.getListClass());
        dictData.setIsDefault(dto.getIsDefault() != null && dto.getIsDefault());
        dictData.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        dictData.setRemark(dto.getRemark() == null ? "" : dto.getRemark());
        dictDataMapper.insert(dictData);
        clearCacheByType(dictType.getDictType());
        return toDataVo(dictData, dictType);
    }

    public DictDataVo updateData(String id, UpdateDictDataDto dto) {
        DictData dictData = getExistingData(id);
        DictType currentType = getExistingType(dictData.getTypeId());
        String nextTypeId = dto.getTypeId() == null ? dictData.getTypeId() : dto.getTypeId();
        DictType nextType = getExistingType(nextTypeId);
        String nextLabel = dto.getDictLabel() == null ? dictData.getDictLabel() : dto.getDictLabel();

        if (!nextTypeId.equals(dictData.getTypeId()) || !nextLabel.equals(dictData.getDictLabel())) {
            ensureDataLabelUnique(nextTypeId, nextLabel, id);
        }

        if (dto.getTypeId() != null) {
            dictData.setTypeId(dto.getTypeId());
        }
        if (dto.getDictLabel() != null) {
            dictData.setDictLabel(dto.getDictLabel());
        }
        if (dto.getDictValue() != null) {
            dictData.setDictValue(dto.getDictValue());
        }
        if (dto.getDictSort() != null) {
            dictData.setDictSort(dto.getDictSort());
        }
        if (dto.getCssClass() != null) {
            dictData.setCssClass(dto.getCssClass());
        }
        if (dto.getListClass() != null) {
            dictData.setListClass(dto.getListClass());
        }
        if (dto.getIsDefault() != null) {
            dictData.setIsDefault(dto.getIsDefault());
        }
        if (dto.getStatus() != null) {
            dictData.setStatus(dto.getStatus());
        }
        if (dto.getRemark() != null) {
            dictData.setRemark(dto.getRemark());
        }

        dictDataMapper.updateById(dictData);
        clearCacheByType(currentType.getDictType());
        if (!currentType.getDictType().equals(nextType.getDictType())) {
            clearCacheByType(nextType.getDictType());
        }
        return toDataVo(dictData, nextType);
    }

    public Map<String, String> deleteData(String id) {
        DictData dictData = getExistingData(id);
        DictType dictType = getExistingType(dictData.getTypeId());
        dictDataMapper.deleteById(id);
        clearCacheByType(dictType.getDictType());
        return Map.of("id", id);
    }

    private DictType getExistingType(String id) {
        DictType dictType = dictTypeMapper.selectById(id);
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }
        return dictType;
    }

    private DictData getExistingData(String id) {
        DictData dictData = dictDataMapper.selectById(id);
        if (dictData == null) {
            throw new BusinessException("字典数据不存在");
        }
        return dictData;
    }

    private DictType getTypeByDictType(String dictType) {
        return dictTypeMapper.selectOne(new LambdaQueryWrapper<DictType>()
                .eq(DictType::getDictType, dictType));
    }

    private void ensureTypeUnique(String dictType, String excludeId) {
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<DictType>()
                .eq(DictType::getDictType, dictType);
        if (excludeId != null) {
            wrapper.ne(DictType::getId, excludeId);
        }
        if (dictTypeMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("字典类型已存在");
        }
    }

    private void ensureDataLabelUnique(String typeId, String dictLabel, String excludeId) {
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<DictData>()
                .eq(DictData::getTypeId, typeId)
                .eq(DictData::getDictLabel, dictLabel);
        if (excludeId != null) {
            wrapper.ne(DictData::getId, excludeId);
        }
        if (dictDataMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("字典标签已存在");
        }
    }

    private String resolveTypeId(String typeId, String dictType) {
        if (!StringUtils.hasText(dictType)) {
            return typeId;
        }
        DictType current = getTypeByDictType(dictType);
        if (current == null) {
            return null;
        }
        if (StringUtils.hasText(typeId) && !typeId.equals(current.getId())) {
            return null;
        }
        return current.getId();
    }

    private Map<String, DictType> buildTypeMap(List<String> typeIds) {
        if (typeIds.isEmpty()) {
            return Map.of();
        }
        return dictTypeMapper.selectList(new LambdaQueryWrapper<DictType>()
                        .in(DictType::getId, typeIds))
                .stream()
                .collect(Collectors.toMap(DictType::getId, Function.identity()));
    }

    private void clearCacheByType(String dictType) {
        redisUtils.delete(getCacheKey(dictType));
    }

    private String getCacheKey(String dictType) {
        return DICT_CACHE_PREFIX + dictType;
    }

    private DictTypeVo toTypeVo(DictType dictType) {
        DictTypeVo vo = new DictTypeVo();
        vo.setId(dictType.getId());
        vo.setDictName(dictType.getDictName());
        vo.setDictType(dictType.getDictType());
        vo.setStatus(dictType.getStatus());
        vo.setRemark(dictType.getRemark() == null ? "" : dictType.getRemark());
        vo.setCreateTime(formatDateTime(dictType.getCreatedAt()));
        return vo;
    }

    private DictDataVo toDataVo(DictData dictData, DictType dictType) {
        DictDataVo vo = new DictDataVo();
        vo.setId(dictData.getId());
        vo.setTypeId(dictData.getTypeId());
        vo.setDictName(dictType.getDictName());
        vo.setDictType(dictType.getDictType());
        vo.setDictLabel(dictData.getDictLabel());
        vo.setDictValue(dictData.getDictValue());
        vo.setDictSort(dictData.getDictSort());
        vo.setCssClass(dictData.getCssClass() == null ? "" : dictData.getCssClass());
        vo.setListClass(dictData.getListClass() == null ? "" : dictData.getListClass());
        vo.setIsDefault(Boolean.TRUE.equals(dictData.getIsDefault()));
        vo.setStatus(dictData.getStatus());
        vo.setRemark(dictData.getRemark() == null ? "" : dictData.getRemark());
        vo.setCreateTime(formatDateTime(dictData.getCreatedAt()));
        return vo;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toString();
    }
}
