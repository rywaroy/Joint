package org.joint.modules.system.role;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.Log;
import org.joint.common.annotation.RequirePermission;
import org.joint.common.enums.BusinessType;
import org.joint.common.response.PageResult;
import org.joint.modules.system.role.dto.CreateRoleDto;
import org.joint.modules.system.role.dto.QueryRoleDto;
import org.joint.modules.system.role.dto.UpdateRoleDto;
import org.joint.modules.system.role.vo.RoleVo;
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
@RequestMapping("/system/role")
@RequiredArgsConstructor
@Tag(name = "角色管理")
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/list")
    @RequirePermission("system:role:list")
    @Operation(summary = "分页查询角色")
    public PageResult<RoleVo> list(@ParameterObject QueryRoleDto query) {
        return roleService.findPage(query);
    }

    @GetMapping("/options")
    @Operation(summary = "获取角色选项")
    public List<RoleVo> options() {
        return roleService.findAllEnabled();
    }

    @GetMapping("/{id}")
    @RequirePermission("system:role:query")
    @Operation(summary = "查询角色详情")
    public RoleVo getById(@PathVariable String id) {
        return roleService.findById(id);
    }

    @PostMapping
    @RequirePermission("system:role:create")
    @Log(module = "角色管理", type = BusinessType.INSERT, description = "创建角色")
    @Operation(summary = "创建角色")
    public RoleVo create(@Valid @RequestBody CreateRoleDto dto) {
        return roleService.create(dto);
    }

    @PutMapping("/{id}")
    @RequirePermission("system:role:update")
    @Log(module = "角色管理", type = BusinessType.UPDATE, description = "更新角色")
    @Operation(summary = "更新角色")
    public RoleVo update(@PathVariable String id, @Valid @RequestBody UpdateRoleDto dto) {
        return roleService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:role:delete")
    @Log(module = "角色管理", type = BusinessType.DELETE, description = "删除角色")
    @Operation(summary = "删除角色")
    public void delete(@PathVariable String id) {
        roleService.delete(id);
    }
}
