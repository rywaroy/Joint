package org.joint.modules.system.menu;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.Log;
import org.joint.common.annotation.RequirePermission;
import org.joint.common.enums.BusinessType;
import org.joint.modules.system.menu.dto.CreateMenuDto;
import org.joint.modules.system.menu.dto.UpdateMenuDto;
import org.joint.modules.system.menu.vo.MenuVo;
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
@RequestMapping("/system/menu")
@RequiredArgsConstructor
@Tag(name = "菜单管理")
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/tree")
    @RequirePermission("system:menu:list")
    @Operation(summary = "获取菜单树")
    public List<MenuVo> tree() {
        return menuService.getMenuTree();
    }

    @GetMapping("/{id}")
    @RequirePermission("system:menu:query")
    @Operation(summary = "查询菜单详情")
    public MenuVo getById(@PathVariable String id) {
        return menuService.findById(id);
    }

    @PostMapping
    @RequirePermission("system:menu:add")
    @Log(module = "菜单管理", type = BusinessType.INSERT, description = "创建菜单")
    @Operation(summary = "创建菜单")
    public MenuVo create(@Valid @RequestBody CreateMenuDto dto) {
        return menuService.create(dto);
    }

    @PutMapping("/{id}")
    @RequirePermission("system:menu:edit")
    @Log(module = "菜单管理", type = BusinessType.UPDATE, description = "更新菜单")
    @Operation(summary = "更新菜单")
    public MenuVo update(@PathVariable String id, @Valid @RequestBody UpdateMenuDto dto) {
        return menuService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:menu:delete")
    @Log(module = "菜单管理", type = BusinessType.DELETE, description = "删除菜单")
    @Operation(summary = "删除菜单")
    public void delete(@PathVariable String id) {
        menuService.delete(id);
    }
}
