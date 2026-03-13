package org.joint.modules.system.role;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.joint.common.exception.BusinessException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService {

    private static final Set<String> BUILTIN_ROLE_CODES = Set.of("admin", "user");

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;

    public Map<String, Object> findPage(QueryRoleDto query) {
        Page<Role> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<Role> result = roleMapper.selectPage(page, buildQueryWrapper(query));
        return Map.of(
                "list", result.getRecords().stream().map(this::toVo).toList(),
                "total", result.getTotal()
        );
    }

    public RoleVo findById(String id) {
        return toVo(getExistingRole(id));
    }

    public RoleVo create(CreateRoleDto dto) {
        ensureNameUnique(dto.getName(), null);

        Role role = new Role();
        role.setName(dto.getName());
        if (role.getStatus() == null) {
            role.setStatus(0);
        }
        role.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        role.setRemark(dto.getRemark() == null ? "" : dto.getRemark());
        role.setSort(0);
        role.setCode(generateRoleCode());
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
        if (isBuiltinRole(role) && "admin".equals(role.getCode()) && Integer.valueOf(1).equals(dto.getStatus())) {
            throw new BusinessException("admin 角色不允许停用");
        }

        String targetName = dto.getName() != null ? dto.getName() : role.getName();
        if (!targetName.equals(role.getName())) {
            ensureNameUnique(targetName, id);
        }

        if (dto.getName() != null) {
            role.setName(dto.getName());
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

    public Map<String, String> delete(String id) {
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
        return Map.of("id", id);
    }

    public List<RoleVo> findAllEnabled() {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>()
                        .eq(Role::getStatus, 0)
                        .orderByDesc(Role::getCreatedAt))
                .stream()
                .map(this::toVo)
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
                .eq(query.getStatus() != null, Role::getStatus, query.getStatus())
                .orderByDesc(Role::getCreatedAt);
    }

    private void ensureNameUnique(String name, String excludeId) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<Role>()
                .eq(Role::getName, name);
        if (excludeId != null) {
            wrapper.ne(Role::getId, excludeId);
        }
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("角色名称已存在");
        }
    }

    private boolean changesBuiltinIdentity(Role role, UpdateRoleDto dto) {
        return dto.getName() != null && !dto.getName().equals(role.getName());
    }

    private boolean isBuiltinRole(Role role) {
        return role.getCode() != null && BUILTIN_ROLE_CODES.contains(role.getCode());
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
        vo.setId(role.getId());
        vo.setName(role.getName());
        vo.setStatus(role.getStatus());
        vo.setIsSuper(role.getIsSuper());
        vo.setRemark(role.getRemark() == null ? "" : role.getRemark());
        vo.setCreateTime(formatDateTime(role.getCreatedAt()));
        vo.setIsBuiltin(isBuiltinRole(role));
        if (Boolean.TRUE.equals(role.getIsSuper())) {
            vo.setPermissions(List.of("*"));
        } else {
            vo.setPermissions(getRoleMenuIds(role.getId()));
        }
        return vo;
    }

    private String generateRoleCode() {
        return "role_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toString();
    }
}
