package org.joint.modules.system.menu;

import org.joint.common.exception.BusinessException;
import org.joint.modules.system.menu.dto.CreateMenuDto;
import org.joint.modules.system.menu.dto.UpdateMenuDto;
import org.joint.modules.system.menu.entity.Menu;
import org.joint.modules.system.menu.mapper.MenuMapper;
import org.joint.modules.system.menu.vo.MenuVo;
import org.joint.modules.system.role.entity.RoleMenu;
import org.joint.modules.system.role.mapper.RoleMenuMapper;
import org.joint.modules.system.user.entity.UserRole;
import org.joint.modules.system.user.mapper.UserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuServiceTest {

    private MenuMapper menuMapper;
    private RoleMenuMapper roleMenuMapper;
    private UserRoleMapper userRoleMapper;
    private MenuService menuService;

    @BeforeEach
    void setUp() {
        menuMapper = mock(MenuMapper.class);
        roleMenuMapper = mock(RoleMenuMapper.class);
        userRoleMapper = mock(UserRoleMapper.class);
        menuService = new MenuService(menuMapper, roleMenuMapper, userRoleMapper);
    }

    @Test
    void getMenuTreeBuildsSortedHierarchy() {
        Menu root = new Menu();
        root.setId("m-root");
        root.setName("系统");
        root.setSort(1);

        Menu child = new Menu();
        child.setId("m-child");
        child.setParentId("m-root");
        child.setName("用户");
        child.setSort(2);

        when(menuMapper.selectList(any())).thenReturn(List.of(root, child));

        List<MenuVo> result = menuService.getMenuTree();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("m-root");
        assertThat(result.get(0).getChildren()).hasSize(1);
        assertThat(result.get(0).getChildren().get(0).getId()).isEqualTo("m-child");
    }

    @Test
    void createRejectsMissingParent() {
        CreateMenuDto dto = new CreateMenuDto();
        dto.setParentId("missing");
        dto.setName("用户");

        when(menuMapper.selectById("missing")).thenReturn(null);

        assertThatThrownBy(() -> menuService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("父级菜单不存在");
    }

    @Test
    void updateRejectsDescendantAsParent() {
        Menu current = new Menu();
        current.setId("m-root");

        Menu child = new Menu();
        child.setId("m-child");
        child.setParentId("m-root");

        UpdateMenuDto dto = new UpdateMenuDto();
        dto.setParentId("m-child");

        when(menuMapper.selectById("m-root")).thenReturn(current);
        when(menuMapper.selectById("m-child")).thenReturn(child);
        when(menuMapper.selectList(any())).thenReturn(List.of(child), List.of());

        assertThatThrownBy(() -> menuService.update("m-root", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("不能将子菜单设为父级");
    }

    @Test
    void deleteRejectsMenuWithChildren() {
        when(menuMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> menuService.delete("m-root"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("请先删除子菜单");

        verify(menuMapper, never()).deleteById("m-root");
    }

    @Test
    void getUserMenuTreeReturnsAssignedNonButtonMenus() {
        UserRole userRole = new UserRole();
        userRole.setRoleId("r-1");

        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRoleId("r-1");
        roleMenu.setMenuId("m-root");

        Menu root = new Menu();
        root.setId("m-root");
        root.setName("系统");
        root.setType(1);

        Menu button = new Menu();
        button.setId("m-btn");
        button.setParentId("m-root");
        button.setName("删除");
        button.setType(2);

        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(roleMenuMapper.selectList(any())).thenReturn(List.of(roleMenu));
        when(menuMapper.selectList(any())).thenReturn(List.of(root, button));

        List<MenuVo> result = menuService.getUserMenuTree("u-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("m-root");
        assertThat(result.get(0).getChildren()).isNull();
    }
}
