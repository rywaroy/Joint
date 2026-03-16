package org.joint.common.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.joint.modules.system.menu.entity.Menu;
import org.joint.modules.system.menu.mapper.MenuMapper;
import org.joint.modules.system.role.entity.Role;
import org.joint.modules.system.role.entity.RoleMenu;
import org.joint.modules.system.role.mapper.RoleMapper;
import org.joint.modules.system.role.mapper.RoleMenuMapper;
import org.joint.modules.system.user.entity.UserRole;
import org.joint.modules.system.user.mapper.UserRoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;

    public Set<String> getUserPermissions(String userId) {
        QueryWrapper<UserRole> userRoleWrapper = new QueryWrapper<>();
        userRoleWrapper.eq("userId", userId);
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleWrapper);
        if (userRoles.isEmpty()) {
            return Set.of();
        }

        List<String> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .distinct()
                .toList();

        QueryWrapper<Role> roleWrapper = new QueryWrapper<>();
        roleWrapper.in("id", roleIds);
        roleWrapper.eq("status", 0);
        List<Role> roles = roleMapper.selectList(roleWrapper).stream()
                .filter(role -> Integer.valueOf(0).equals(role.getStatus()))
                .toList();
        if (roles.isEmpty()) {
            return Set.of();
        }
        boolean isAdmin = roles.stream()
                .anyMatch(role -> Boolean.TRUE.equals(role.getIsSuper()) || "admin".equals(role.getName()));
        if (isAdmin) {
            QueryWrapper<Menu> allMenuWrapper = new QueryWrapper<>();
            allMenuWrapper.isNotNull("authCode");
            allMenuWrapper.ne("authCode", "");
            allMenuWrapper.eq("status", 0);
            return menuMapper.selectList(allMenuWrapper).stream()
                    .filter(menu -> Integer.valueOf(0).equals(menu.getStatus()))
                    .filter(menu -> menu.getAuthCode() != null && !menu.getAuthCode().isBlank())
                    .map(Menu::getAuthCode)
                    .collect(Collectors.toSet());
        }

        QueryWrapper<RoleMenu> roleMenuWrapper = new QueryWrapper<>();
        roleMenuWrapper.in("roleId", roleIds);
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(roleMenuWrapper);
        if (roleMenus.isEmpty()) {
            return Set.of();
        }

        List<String> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .distinct()
                .toList();

        QueryWrapper<Menu> menuWrapper = new QueryWrapper<>();
        menuWrapper.in("id", menuIds);
        menuWrapper.isNotNull("authCode");
        menuWrapper.ne("authCode", "");
        menuWrapper.eq("status", 0);
        return menuMapper.selectList(menuWrapper).stream()
                .filter(menu -> Integer.valueOf(0).equals(menu.getStatus()))
                .filter(menu -> menu.getAuthCode() != null && !menu.getAuthCode().isBlank())
                .map(Menu::getAuthCode)
                .collect(Collectors.toSet());
    }
}
