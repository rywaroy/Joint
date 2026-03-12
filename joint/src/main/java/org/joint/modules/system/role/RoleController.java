package org.joint.modules.system.role;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.RequirePermission;
import org.joint.common.response.PageResult;
import org.joint.modules.system.role.dto.CreateRoleDto;
import org.joint.modules.system.role.dto.QueryRoleDto;
import org.joint.modules.system.role.dto.UpdateRoleDto;
import org.joint.modules.system.role.vo.RoleVo;
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
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/list")
    @RequirePermission("system:role:list")
    public PageResult<RoleVo> list(QueryRoleDto query) {
        return roleService.findPage(query);
    }

    @GetMapping("/options")
    public List<RoleVo> options() {
        return roleService.findAllEnabled();
    }

    @GetMapping("/{id}")
    @RequirePermission("system:role:query")
    public RoleVo getById(@PathVariable String id) {
        return roleService.findById(id);
    }

    @PostMapping
    @RequirePermission("system:role:create")
    public RoleVo create(@Valid @RequestBody CreateRoleDto dto) {
        return roleService.create(dto);
    }

    @PutMapping("/{id}")
    @RequirePermission("system:role:update")
    public RoleVo update(@PathVariable String id, @Valid @RequestBody UpdateRoleDto dto) {
        return roleService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:role:delete")
    public void delete(@PathVariable String id) {
        roleService.delete(id);
    }
}
