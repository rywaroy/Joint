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
        userRoleWrapper.eq("user_id", userId);
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleWrapper);
        if (userRoles.isEmpty()) {
            return Set.of();
        }

        List<String> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .distinct()
                .toList();

        List<Role> roles = roleMapper.selectBatchIds(roleIds);
        boolean isAdmin = roles.stream()
                .anyMatch(role -> Boolean.TRUE.equals(role.getIsSuper()) || "admin".equals(role.getCode()));
        if (isAdmin) {
            QueryWrapper<Menu> allMenuWrapper = new QueryWrapper<>();
            allMenuWrapper.isNotNull("auth_code");
            allMenuWrapper.ne("auth_code", "");
            return menuMapper.selectList(allMenuWrapper).stream()
                    .map(Menu::getAuthCode)
                    .collect(Collectors.toSet());
        }

        QueryWrapper<RoleMenu> roleMenuWrapper = new QueryWrapper<>();
        roleMenuWrapper.in("role_id", roleIds);
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
        menuWrapper.isNotNull("auth_code");
        menuWrapper.ne("auth_code", "");
        return menuMapper.selectList(menuWrapper).stream()
                .map(Menu::getAuthCode)
                .collect(Collectors.toSet());
    }
}
