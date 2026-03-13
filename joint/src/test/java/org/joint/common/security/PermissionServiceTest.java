package org.joint.common.security;

import org.joint.modules.system.menu.entity.Menu;
import org.joint.modules.system.menu.mapper.MenuMapper;
import org.joint.modules.system.role.entity.Role;
import org.joint.modules.system.role.entity.RoleMenu;
import org.joint.modules.system.role.mapper.RoleMapper;
import org.joint.modules.system.role.mapper.RoleMenuMapper;
import org.joint.modules.system.user.entity.UserRole;
import org.joint.modules.system.user.mapper.UserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermissionServiceTest {

    private UserRoleMapper userRoleMapper;
    private RoleMapper roleMapper;
    private RoleMenuMapper roleMenuMapper;
    private MenuMapper menuMapper;
    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        userRoleMapper = mock(UserRoleMapper.class);
        roleMapper = mock(RoleMapper.class);
        roleMenuMapper = mock(RoleMenuMapper.class);
        menuMapper = mock(MenuMapper.class);
        permissionService = new PermissionService(userRoleMapper, roleMapper, roleMenuMapper, menuMapper);
    }

    @Test
    void getUserPermissionsIgnoresDisabledRolesAndMenus() {
        UserRole userRole = new UserRole();
        userRole.setRoleId("r-1");

        Role disabledRole = new Role();
        disabledRole.setId("r-1");
        disabledRole.setCode("ops");
        disabledRole.setStatus(1);

        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRoleId("r-1");
        roleMenu.setMenuId("m-1");

        Menu disabledMenu = new Menu();
        disabledMenu.setId("m-1");
        disabledMenu.setAuthCode("system:role:list");
        disabledMenu.setStatus(1);

        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(roleMapper.selectList(any())).thenReturn(List.of(disabledRole));
        when(roleMenuMapper.selectList(any())).thenReturn(List.of(roleMenu));
        when(menuMapper.selectList(any())).thenReturn(List.of(disabledMenu));

        Set<String> result = permissionService.getUserPermissions("u-1");

        assertThat(result).isEmpty();
    }
}
