package org.joint.modules.system.role;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.joint.common.exception.BusinessException;
import org.joint.modules.system.role.dto.CreateRoleDto;
import org.joint.modules.system.role.dto.QueryRoleDto;
import org.joint.modules.system.role.dto.UpdateRoleDto;
import org.joint.modules.system.role.entity.Role;
import org.joint.modules.system.role.entity.RoleMenu;
import org.joint.modules.system.role.mapper.RoleMapper;
import org.joint.modules.system.role.mapper.RoleMenuMapper;
import org.joint.modules.system.role.vo.RoleVo;
import org.joint.modules.system.user.mapper.UserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoleServiceTest {

    private RoleMapper roleMapper;
    private RoleMenuMapper roleMenuMapper;
    private UserRoleMapper userRoleMapper;
    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleMapper = mock(RoleMapper.class);
        roleMenuMapper = mock(RoleMenuMapper.class);
        userRoleMapper = mock(UserRoleMapper.class);
        roleService = new RoleService(roleMapper, roleMenuMapper, userRoleMapper);
    }

    @Test
    void findPageReturnsRoleVos() {
        Role role = new Role();
        role.setId("r-1");
        role.setName("运营");
        role.setStatus(0);

        Page<Role> page = new Page<>(1, 10);
        page.setRecords(List.of(role));
        page.setTotal(1);

        when(roleMapper.selectPage(any(), any())).thenReturn(page);
        when(roleMenuMapper.selectList(any())).thenReturn(List.of());

        Map<String, Object> result = roleService.findPage(new QueryRoleDto());
        @SuppressWarnings("unchecked")
        List<RoleVo> list = (List<RoleVo>) result.get("list");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("运营");
        assertThat(result.get("total")).isEqualTo(1L);
    }

    @Test
    void createStoresPermissionsAndMarksSuperRole() {
        CreateRoleDto dto = new CreateRoleDto();
        dto.setName("管理员");
        dto.setPermissions(List.of("*", "m-1"));

        when(roleMapper.selectCount(any())).thenReturn(0L);
        when(roleMapper.insert(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            role.setId("r-1");
            return 1;
        });

        RoleVo result = roleService.create(dto);

        assertThat(result.getId()).isEqualTo("r-1");
        assertThat(result.getPermissions()).containsExactly("*");
        verify(roleMenuMapper).insert(any(RoleMenu.class));
    }

    @Test
    void updateRejectsBuiltinRoleMutation() {
        Role role = new Role();
        role.setId("r-1");
        role.setName("admin");

        UpdateRoleDto dto = new UpdateRoleDto();
        dto.setName("超级管理员");

        when(roleMapper.selectById("r-1")).thenReturn(role);

        assertThatThrownBy(() -> roleService.update("r-1", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("内置角色不能修改");
    }

    @Test
    void updateRejectsDisablingAdminRole() {
        Role role = new Role();
        role.setId("r-1");
        role.setName("admin");
        role.setStatus(0);

        UpdateRoleDto dto = new UpdateRoleDto();
        dto.setStatus(1);

        when(roleMapper.selectById("r-1")).thenReturn(role);

        assertThatThrownBy(() -> roleService.update("r-1", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("admin 角色不允许停用");
    }

    @Test
    void deleteRejectsRoleAssignedToUsers() {
        Role role = new Role();
        role.setId("r-1");
        role.setName("ops");

        when(roleMapper.selectById("r-1")).thenReturn(role);
        when(userRoleMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> roleService.delete("r-1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("该角色已分配用户，不能删除");

        verify(roleMapper, never()).deleteById("r-1");
    }

    @Test
    void deleteReturnsDeletedIdPayload() {
        Role role = new Role();
        role.setId("r-1");
        role.setName("ops");

        when(roleMapper.selectById("r-1")).thenReturn(role);
        when(userRoleMapper.selectCount(any())).thenReturn(0L);

        Map<String, String> result = roleService.delete("r-1");

        verify(roleMapper).deleteById("r-1");
        assertThat(result).containsEntry("id", "r-1");
    }

    @Test
    void findByIdReturnsWildcardForSuperRole() {
        Role role = new Role();
        role.setId("r-1");
        role.setName("ops");
        role.setIsSuper(true);

        when(roleMapper.selectById("r-1")).thenReturn(role);

        RoleVo result = roleService.findById("r-1");

        assertThat(result.getPermissions()).containsExactly("*");
    }

    @Test
    void findAllEnabledReturnsOptionRolesWithPermissionsField() {
        Role role = new Role();
        role.setId("r-1");
        role.setName("运营");
        role.setStatus(0);

        when(roleMapper.selectList(any())).thenReturn(List.of(role));
        when(roleMenuMapper.selectList(any())).thenReturn(List.of());

        List<RoleVo> result = roleService.findAllEnabled();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPermissions()).isEmpty();
        verify(roleMapper, times(1)).selectList(any());
    }
}
