package org.joint.modules.system.menu;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.RequirePermission;
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
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/tree")
    @RequirePermission("system:menu:list")
    public List<MenuVo> tree() {
        return menuService.getMenuTree();
    }

    @GetMapping("/{id}")
    @RequirePermission("system:menu:query")
    public MenuVo getById(@PathVariable String id) {
        return menuService.findById(id);
    }

    @PostMapping
    @RequirePermission("system:menu:add")
    public MenuVo create(@Valid @RequestBody CreateMenuDto dto) {
        return menuService.create(dto);
    }

    @PutMapping("/{id}")
    @RequirePermission("system:menu:edit")
    public MenuVo update(@PathVariable String id, @Valid @RequestBody UpdateMenuDto dto) {
        return menuService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:menu:delete")
    public void delete(@PathVariable String id) {
        menuService.delete(id);
    }
}
