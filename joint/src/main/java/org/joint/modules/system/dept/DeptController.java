package org.joint.modules.system.dept;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.Log;
import org.joint.common.annotation.RequirePermission;
import org.joint.common.enums.BusinessType;
import org.joint.modules.system.dept.dto.CreateDeptDto;
import org.joint.modules.system.dept.dto.QueryDeptDto;
import org.joint.modules.system.dept.dto.UpdateDeptDto;
import org.joint.modules.system.dept.vo.DeptVo;
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

@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
@Tag(name = "部门管理")
public class DeptController {

    private final DeptService deptService;

    @GetMapping("/list")
    @RequirePermission("system:dept:list")
    @Operation(summary = "获取部门树")
    public List<DeptVo> list(@ParameterObject QueryDeptDto query) {
        return deptService.findTree(query);
    }

    @PostMapping
    @RequirePermission("system:dept:create")
    @Log(module = "部门管理", type = BusinessType.INSERT, description = "创建部门")
    @Operation(summary = "创建部门")
    public DeptVo create(@Valid @RequestBody CreateDeptDto dto) {
        return deptService.create(dto);
    }

    @PutMapping("/{id}")
    @RequirePermission("system:dept:update")
    @Log(module = "部门管理", type = BusinessType.UPDATE, description = "更新部门")
    @Operation(summary = "更新部门")
    public DeptVo update(@PathVariable String id, @Valid @RequestBody UpdateDeptDto dto) {
        return deptService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:dept:delete")
    @Log(module = "部门管理", type = BusinessType.DELETE, description = "删除部门")
    @Operation(summary = "删除部门")
    public void delete(@PathVariable String id) {
        deptService.delete(id);
    }
}
