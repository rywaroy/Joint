package org.joint.modules.system.menu;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.CurrentUser;
import org.joint.common.security.PermissionService;
import org.joint.modules.system.menu.vo.MenuRouteVo;
import org.springframework.web.bind.annotation.GetMapping;
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
}
