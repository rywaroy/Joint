package org.joint.modules.system.role;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.joint.common.exception.BusinessException;
import org.joint.common.response.PageResult;
import org.joint.modules.system.role.dto.CreateRoleDto;
import org.joint.modules.system.role.dto.QueryRoleDto;
import org.joint.modules.system.role.dto.UpdateRoleDto;
import org.joint.modules.system.role.entity.Role;
import org.joint.modules.system.role.entity.RoleMenu;
import org.joint.modules.system.role.mapper.RoleMapper;
import org.joint.modules.system.role.mapper.RoleMenuMapper;
import org.joint.modules.system.role.vo.RoleVo;
import org.joint.modules.system.user.entity.UserRole;
import org.joint.modules.system.user.mapper.UserRoleMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService {

    private static final Set<String> BUILTIN_ROLE_CODES = Set.of("admin", "user");

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;

    public PageResult<RoleVo> findPage(QueryRoleDto query) {
        Page<Role> page = new Page<>(query.getPage(), query.getSize());
        IPage<Role> result = roleMapper.selectPage(page, buildQueryWrapper(query));
        return PageResult.of(result, this::toVo);
    }

    public RoleVo findById(String id) {
        return toVo(getExistingRole(id));
    }

    public RoleVo create(CreateRoleDto dto) {
        ensureUnique(dto.getName(), dto.getCode(), null);

        Role role = new Role();
        BeanUtils.copyProperties(dto, role);
        if (role.getSort() == null) {
            role.setSort(0);
        }
        if (role.getStatus() == null) {
            role.setStatus(0);
        }
        role.setIsSuper(dto.getPermissions() != null && dto.getPermissions().contains("*"));

        roleMapper.insert(role);
        saveRoleMenus(role.getId(), dto.getPermissions());
        return toVo(role);
    }

    public RoleVo update(String id, UpdateRoleDto dto) {
        Role role = getExistingRole(id);
        if (isBuiltinRole(role) && changesBuiltinIdentity(role, dto)) {
            throw new BusinessException("内置角色不能修改");
        }

        String targetName = dto.getName() != null ? dto.getName() : role.getName();
        String targetCode = dto.getCode() != null ? dto.getCode() : role.getCode();
        if (!targetName.equals(role.getName()) || !targetCode.equals(role.getCode())) {
            ensureUnique(targetName, targetCode, id);
        }

        if (dto.getName() != null) {
            role.setName(dto.getName());
        }
        if (dto.getCode() != null) {
            role.setCode(dto.getCode());
        }
        if (dto.getSort() != null) {
            role.setSort(dto.getSort());
        }
        if (dto.getStatus() != null) {
            role.setStatus(dto.getStatus());
        }
        if (dto.getRemark() != null) {
            role.setRemark(dto.getRemark());
        }
        if (dto.getPermissions() != null) {
            role.setIsSuper(dto.getPermissions().contains("*"));
            deleteRoleMenus(id);
            saveRoleMenus(id, dto.getPermissions());
        }

        roleMapper.updateById(role);
        return toVo(role);
    }

    public void delete(String id) {
        Role role = getExistingRole(id);
        if (isBuiltinRole(role)) {
            throw new BusinessException("内置角色不能删除");
        }

        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
        if (count > 0) {
            throw new BusinessException("该角色已分配用户，不能删除");
        }

        deleteRoleMenus(id);
        roleMapper.deleteById(id);
    }

    public List<RoleVo> findAllEnabled() {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>()
                        .eq(Role::getStatus, 0)
                        .orderByAsc(Role::getSort)
                        .orderByDesc(Role::getCreatedAt))
                .stream()
                .map(this::toSimpleVo)
                .toList();
    }

    private Role getExistingRole(String id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }

    private LambdaQueryWrapper<Role> buildQueryWrapper(QueryRoleDto query) {
        return new LambdaQueryWrapper<Role>()
                .like(StringUtils.hasText(query.getName()), Role::getName, query.getName())
                .like(StringUtils.hasText(query.getCode()), Role::getCode, query.getCode())
                .eq(query.getStatus() != null, Role::getStatus, query.getStatus())
                .orderByAsc(Role::getSort)
                .orderByDesc(Role::getCreatedAt);
    }

    private void ensureUnique(String name, String code, String excludeId) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<Role>()
                .nested(w -> w.eq(Role::getName, name).or().eq(Role::getCode, code));
        if (excludeId != null) {
            wrapper.ne(Role::getId, excludeId);
        }
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("角色名称或编码已存在");
        }
    }

    private boolean changesBuiltinIdentity(Role role, UpdateRoleDto dto) {
        return (dto.getName() != null && !dto.getName().equals(role.getName()))
                || (dto.getCode() != null && !dto.getCode().equals(role.getCode()));
    }

    private boolean isBuiltinRole(Role role) {
        return BUILTIN_ROLE_CODES.contains(role.getCode());
    }

    private void saveRoleMenus(String roleId, List<String> permissions) {
        if (permissions == null) {
            return;
        }
        for (String permission : permissions) {
            if ("*".equals(permission)) {
                continue;
            }
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(permission);
            roleMenuMapper.insert(roleMenu);
        }
    }

    private void deleteRoleMenus(String roleId) {
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
    }

    private List<String> getRoleMenuIds(String roleId) {
        return roleMenuMapper.selectList(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId)).stream()
                .map(RoleMenu::getMenuId)
                .toList();
    }

    private RoleVo toVo(Role role) {
        RoleVo vo = new RoleVo();
        BeanUtils.copyProperties(role, vo);
        vo.setIsBuiltin(isBuiltinRole(role));
        if (Boolean.TRUE.equals(role.getIsSuper())) {
            vo.setPermissions(List.of("*"));
        } else {
            vo.setPermissions(getRoleMenuIds(role.getId()));
        }
        return vo;
    }

    private RoleVo toSimpleVo(Role role) {
        RoleVo vo = new RoleVo();
        BeanUtils.copyProperties(role, vo);
        vo.setIsBuiltin(isBuiltinRole(role));
        return vo;
    }
}
