package org.joint.modules.system.dict;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.Log;
import org.joint.common.annotation.RequirePermission;
import org.joint.common.enums.BusinessType;
import org.joint.modules.system.dict.dto.CreateDictDataDto;
import org.joint.modules.system.dict.dto.CreateDictTypeDto;
import org.joint.modules.system.dict.dto.QueryDictDataDto;
import org.joint.modules.system.dict.dto.QueryDictTypeDto;
import org.joint.modules.system.dict.dto.UpdateDictDataDto;
import org.joint.modules.system.dict.dto.UpdateDictTypeDto;
import org.joint.modules.system.dict.vo.DictDataVo;
import org.joint.modules.system.dict.vo.DictTypeVo;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
@Tag(name = "字典管理")
public class DictController {

    private final DictService dictService;

    @GetMapping("/type/list")
    @RequirePermission("system:dict:list")
    @Operation(summary = "分页查询字典类型")
    public Map<String, Object> findTypeList(@Valid @ParameterObject QueryDictTypeDto query) {
        return dictService.findTypeList(query);
    }

    @GetMapping("/type/options")
    @RequirePermission("system:dict:query")
    @Operation(summary = "获取字典类型选项")
    public List<DictTypeVo> findTypeOptions() {
        return dictService.findTypeOptions();
    }

    @GetMapping("/type/{id}")
    @RequirePermission("system:dict:query")
    @Operation(summary = "获取字典类型详情")
    public DictTypeVo findTypeById(@PathVariable String id) {
        return dictService.findTypeById(id);
    }

    @PostMapping("/type")
    @RequirePermission("system:dict:create")
    @Log(module = "字典管理", type = BusinessType.INSERT, description = "创建字典类型")
    @Operation(summary = "创建字典类型")
    public DictTypeVo createType(@Valid @RequestBody CreateDictTypeDto dto) {
        return dictService.createType(dto);
    }

    @PutMapping("/type/{id}")
    @RequirePermission("system:dict:update")
    @Log(module = "字典管理", type = BusinessType.UPDATE, description = "更新字典类型")
    @Operation(summary = "更新字典类型")
    public DictTypeVo updateType(@PathVariable String id, @Valid @RequestBody UpdateDictTypeDto dto) {
        return dictService.updateType(id, dto);
    }

    @DeleteMapping("/type/{id}")
    @RequirePermission("system:dict:delete")
    @Log(module = "字典管理", type = BusinessType.DELETE, description = "删除字典类型")
    @Operation(summary = "删除字典类型")
    public Map<String, String> deleteType(@PathVariable String id) {
        return dictService.deleteType(id);
    }

    @DeleteMapping("/cache")
    @RequirePermission("system:dict:update")
    @Log(module = "字典管理", type = BusinessType.CLEAN, description = "刷新字典缓存")
    @Operation(summary = "刷新字典缓存")
    public Map<String, Integer> refreshCache() {
        return dictService.refreshCache();
    }

    @GetMapping("/data/list")
    @RequirePermission("system:dict:query")
    @Operation(summary = "分页查询字典数据")
    public Map<String, Object> findDataList(@Valid @ParameterObject QueryDictDataDto query) {
        return dictService.findDataList(query);
    }

    @GetMapping("/data/type/{dictType}")
    @RequirePermission("system:dict:query")
    @Operation(summary = "根据字典类型获取字典数据")
    public List<DictDataVo> findDictDataByType(@PathVariable String dictType) {
        return dictService.findDictDataByType(dictType);
    }

    @GetMapping("/data/{id}")
    @RequirePermission("system:dict:query")
    @Operation(summary = "获取字典数据详情")
    public DictDataVo findDataById(@PathVariable String id) {
        return dictService.findDataById(id);
    }

    @PostMapping("/data")
    @RequirePermission("system:dict:create")
    @Log(module = "字典管理", type = BusinessType.INSERT, description = "创建字典数据")
    @Operation(summary = "创建字典数据")
    public DictDataVo createData(@Valid @RequestBody CreateDictDataDto dto) {
        return dictService.createData(dto);
    }

    @PutMapping("/data/{id}")
    @RequirePermission("system:dict:update")
    @Log(module = "字典管理", type = BusinessType.UPDATE, description = "更新字典数据")
    @Operation(summary = "更新字典数据")
    public DictDataVo updateData(@PathVariable String id, @Valid @RequestBody UpdateDictDataDto dto) {
        return dictService.updateData(id, dto);
    }

    @DeleteMapping("/data/{id}")
    @RequirePermission("system:dict:delete")
    @Log(module = "字典管理", type = BusinessType.DELETE, description = "删除字典数据")
    @Operation(summary = "删除字典数据")
    public Map<String, String> deleteData(@PathVariable String id) {
        return dictService.deleteData(id);
    }
}
