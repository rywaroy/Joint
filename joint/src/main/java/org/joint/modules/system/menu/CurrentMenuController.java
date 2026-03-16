package org.joint.modules.system.menu;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.CurrentUser;
import org.joint.common.annotation.Log;
import org.joint.common.annotation.RequirePermission;
import org.joint.common.enums.BusinessType;
import org.joint.common.exception.BusinessException;
import org.joint.modules.system.menu.dto.CreateMenuDto;
import org.joint.modules.system.menu.dto.MenuApiQueryDto;
import org.joint.modules.system.menu.dto.MenuApiSaveDto;
import org.joint.modules.system.menu.dto.UpdateMenuDto;
import org.joint.common.security.PermissionService;
import org.joint.modules.system.menu.vo.MenuListVo;
import org.joint.modules.system.menu.vo.MenuRouteVo;
import org.joint.modules.system.menu.vo.MenuVo;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@Tag(name = "当前用户菜单")
public class CurrentMenuController {

    private final MenuService menuService;
    private final PermissionService permissionService;

    @GetMapping("/list")
    @RequirePermission("system:menu:list")
    @Operation(summary = "获取菜单列表")
    public List<MenuListVo> list(@ModelAttribute MenuApiQueryDto query) {
        List<MenuVo> menus = isEmptyQuery(query)
                ? menuService.getMenuTree()
                : menuService.getMenuTree(resolveNameFilter(query), query.getParentId(), query.getStatus(), toBackendType(query.getType()));
        return menus.stream()
                .map(this::toListVo)
                .toList();
    }

    @GetMapping("/tree")
    @RequirePermission("system:menu:list")
    @Operation(summary = "获取菜单树")
    public List<MenuListVo> tree() {
        return menuService.getMenuTree().stream()
                .map(this::toListVo)
                .toList();
    }

    @GetMapping("/name-exists")
    @Operation(summary = "检查菜单名称是否存在")
    public boolean nameExists(@RequestParam String name, @RequestParam(required = false) String id) {
        return menuService.checkNameExists(name, id);
    }

    @GetMapping("/path-exists")
    @Operation(summary = "检查菜单路径是否存在")
    public boolean pathExists(@RequestParam String path, @RequestParam(required = false) String id) {
        return menuService.checkPathExists(path, id);
    }

    @GetMapping("/{id}")
    @RequirePermission("system:menu:query")
    @Operation(summary = "获取菜单详情")
    public MenuListVo getById(@PathVariable String id) {
        return toListVo(menuService.findById(id));
    }

    @PostMapping
    @RequirePermission("system:menu:create")
    @Log(module = "菜单管理", type = BusinessType.INSERT, description = "创建菜单")
    @Operation(summary = "创建菜单")
    public MenuListVo create(@RequestBody MenuApiSaveDto dto) {
        return toListVo(menuService.create(toCreateDto(dto)));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:menu:update")
    @Log(module = "菜单管理", type = BusinessType.UPDATE, description = "更新菜单")
    @Operation(summary = "更新菜单")
    public MenuListVo update(@PathVariable String id, @RequestBody MenuApiSaveDto dto) {
        return toListVo(menuService.update(id, toUpdateDto(dto)));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:menu:delete")
    @Log(module = "菜单管理", type = BusinessType.DELETE, description = "删除菜单")
    @Operation(summary = "删除菜单")
    public void delete(@PathVariable String id) {
        menuService.delete(id);
    }

    @GetMapping("/routes")
    @Operation(summary = "获取当前用户动态路由")
    public List<MenuRouteVo> routes(@CurrentUser String userId) {
        return menuService.getCurrentUserRoutes(userId);
    }

    @GetMapping("/codes")
    @Operation(summary = "获取当前用户权限码")
    public List<String> codes(@CurrentUser String userId) {
        return permissionService.getUserPermissions(userId).stream()
                .sorted()
                .toList();
    }

    private MenuListVo toListVo(MenuVo menu) {
        MenuListVo vo = new MenuListVo();
        vo.setId(menu.getId());
        vo.setPid(menu.getParentId());
        vo.setName(menu.getName());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setAuthCode(menu.getAuthCode());
        vo.setStatus(menu.getStatus());
        vo.setType(toFrontendType(menu.getType()));
        vo.setMeta(toMeta(menu));
        if (menu.getChildren() != null) {
            vo.setChildren(menu.getChildren().stream().map(this::toListVo).toList());
        }
        return vo;
    }

    private MenuListVo.Meta toMeta(MenuVo menu) {
        MenuListVo.Meta meta = new MenuListVo.Meta();
        meta.setTitle(StringUtils.hasText(menu.getTitle()) ? menu.getTitle() : menu.getName());
        meta.setIcon(menu.getIcon());
        meta.setOrder(menu.getSort());
        meta.setHideInMenu(menu.getHidden());
        return meta;
    }

    private String toFrontendType(String type) {
        if (!StringUtils.hasText(type)) {
            return null;
        }
        return type.toLowerCase();
    }

    private CreateMenuDto toCreateDto(MenuApiSaveDto dto) {
        CreateMenuDto target = new CreateMenuDto();
        target.setParentId(resolveParentId(dto));
        target.setName(dto.getName());
        target.setTitle(dto.getTitle());
        target.setPath(dto.getPath());
        target.setComponent(dto.getComponent());
        target.setIcon(dto.getIcon());
        target.setType(toBackendType(dto.getType()));
        target.setAuthCode(dto.getAuthCode());
        target.setSort(dto.getOrder());
        target.setStatus(dto.getStatus());
        target.setHidden(dto.getHideInMenu());
        return target;
    }

    private UpdateMenuDto toUpdateDto(MenuApiSaveDto dto) {
        UpdateMenuDto target = new UpdateMenuDto();
        target.setParentId(resolveParentId(dto));
        target.setName(dto.getName());
        target.setTitle(dto.getTitle());
        target.setPath(dto.getPath());
        target.setComponent(dto.getComponent());
        target.setIcon(dto.getIcon());
        target.setType(toBackendType(dto.getType()));
        target.setAuthCode(dto.getAuthCode());
        target.setSort(dto.getOrder());
        target.setStatus(dto.getStatus());
        target.setHidden(dto.getHideInMenu());
        return target;
    }

    private String resolveParentId(MenuApiSaveDto dto) {
        if (StringUtils.hasText(dto.getParentId())) {
            return dto.getParentId();
        }
        return dto.getPid();
    }

    private String resolveNameFilter(MenuApiQueryDto query) {
        if (StringUtils.hasText(query.getName())) {
            return query.getName();
        }
        return query.getTitle();
    }

    private boolean isEmptyQuery(MenuApiQueryDto query) {
        return !StringUtils.hasText(query.getName())
                && !StringUtils.hasText(query.getTitle())
                && !StringUtils.hasText(query.getParentId())
                && query.getStatus() == null
                && !StringUtils.hasText(query.getType());
    }

    private String toBackendType(String type) {
        if (!StringUtils.hasText(type)) {
            return null;
        }
        return switch (type) {
            case "catalog" -> "CATALOG";
            case "menu" -> "MENU";
            case "button" -> "BUTTON";
            case "embedded" -> "EMBEDDED";
            case "link" -> "LINK";
            default -> throw new BusinessException("当前后端仅支持 catalog、menu、button、embedded、link 类型菜单");
        };
    }
}
