# 4.3 角色模块

## 学习目标

- 实现角色 CRUD 完整功能
- 处理角色-菜单多对多关联（权限分配）
- 对照 Nexus role 模块实现

## 数据结构

### 角色实体

```java
@Data
@TableName("sys_role")
public class Role {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String name;          // 角色名称（唯一）
    private String remark;        // 备注
    private Integer status;       // 状态：0-正常 1-禁用
    private Boolean isBuiltin;    // 是否内置角色
    private Boolean isSuper;      // 是否超级管理员（拥有全部权限）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 角色-菜单关联表

```java
@Data
@TableName("sys_role_menu")
public class RoleMenu {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String roleId;
    private String menuId;
    private LocalDateTime grantedAt;
}
```

## API 接口

与 Nexus 保持一致：

| 方法 | 路径 | 描述 | 权限码 |
|------|------|------|--------|
| GET | `/system/role/list` | 角色列表（分页） | `system:role:list` |
| GET | `/system/role/options` | 角色选项（下拉框） | 仅需登录 |
| GET | `/system/role/{id}` | 角色详情 | `system:role:query` |
| POST | `/system/role` | 创建角色 | `system:role:create` |
| PUT | `/system/role/{id}` | 更新角色 | `system:role:update` |
| DELETE | `/system/role/{id}` | 删除角色 | `system:role:delete` |

## 实现代码

### DTO 定义

```java
// CreateRoleDto.java
@Data
public class CreateRoleDto {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称长度不超过30个字符")
    private String name;

    private String remark;

    private Integer status;

    /**
     * 权限菜单 ID 列表
     * 包含 "*" 表示超级管理员
     */
    private List<String> permissions;
}

// UpdateRoleDto.java
@Data
public class UpdateRoleDto {
    private String name;
    private String remark;
    private Integer status;
    private List<String> permissions;
}

// QueryRoleDto.java
@Data
public class QueryRoleDto {
    private Integer page = 1;
    private Integer size = 10;
    private String name;
    private Integer status;
}
```

### VO 定义

```java
@Data
public class RoleVo {
    private String id;
    private String name;
    private String remark;
    private Integer status;
    private Boolean isBuiltin;
    private Boolean isSuper;
    private List<String> permissions;  // 菜单 ID 列表，超级管理员返回 ["*"]
    private LocalDateTime createdAt;
}
```

### Service 实现

```java
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public PageResult<RoleVo> findPage(QueryRoleDto query) {
        Page<Role> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getName()), Role::getName, query.getName())
               .eq(query.getStatus() != null, Role::getStatus, query.getStatus())
               .orderByDesc(Role::getCreatedAt);

        IPage<Role> result = roleMapper.selectPage(page, wrapper);

        List<RoleVo> voList = result.getRecords().stream()
                .map(this::toVo)
                .toList();

        return PageResult.of(result.getTotal(), voList);
    }

    @Override
    public RoleVo findById(String id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return toVo(role);
    }

    @Override
    @Transactional
    public RoleVo create(CreateRoleDto dto) {
        // 检查名称唯一性
        if (existsByName(dto.getName())) {
            throw new BusinessException("角色名称已存在");
        }

        Role role = new Role();
        role.setName(dto.getName());
        role.setRemark(dto.getRemark());
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        role.setIsBuiltin(false);

        // 判断是否超级管理员
        boolean isSuper = dto.getPermissions() != null
                          && dto.getPermissions().contains("*");
        role.setIsSuper(isSuper);

        roleMapper.insert(role);

        // 保存角色-菜单关联
        if (dto.getPermissions() != null) {
            List<String> menuIds = dto.getPermissions().stream()
                    .filter(p -> !"*".equals(p))
                    .toList();
            saveRoleMenus(role.getId(), menuIds);
        }

        return toVo(role);
    }

    @Override
    @Transactional
    public RoleVo update(String id, UpdateRoleDto dto) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 内置角色不能修改名称
        if (Boolean.TRUE.equals(role.getIsBuiltin())
                && dto.getName() != null
                && !role.getName().equals(dto.getName())) {
            throw new BusinessException("内置角色不能修改名称");
        }

        // 检查名称唯一性（排除自身）
        if (dto.getName() != null && !dto.getName().equals(role.getName())) {
            if (existsByName(dto.getName())) {
                throw new BusinessException("角色名称已存在");
            }
            role.setName(dto.getName());
        }

        if (dto.getRemark() != null) role.setRemark(dto.getRemark());
        if (dto.getStatus() != null) role.setStatus(dto.getStatus());

        // 更新权限
        if (dto.getPermissions() != null) {
            boolean isSuper = dto.getPermissions().contains("*");
            role.setIsSuper(isSuper);

            // 先删后增
            deleteRoleMenus(id);
            List<String> menuIds = dto.getPermissions().stream()
                    .filter(p -> !"*".equals(p))
                    .toList();
            saveRoleMenus(id, menuIds);
        }

        roleMapper.updateById(role);
        return toVo(role);
    }

    @Override
    @Transactional
    public void delete(String id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 内置角色不能删除
        if (Boolean.TRUE.equals(role.getIsBuiltin())) {
            throw new BusinessException("内置角色不能删除");
        }

        // 检查是否有用户关联
        Long userCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
        if (userCount > 0) {
            throw new BusinessException("该角色已分配用户，不能删除");
        }

        // 删除关联和角色
        deleteRoleMenus(id);
        roleMapper.deleteById(id);
    }

    @Override
    public List<RoleVo> findAllEnabled() {
        List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getStatus, 0)
                        .orderByDesc(Role::getCreatedAt));
        return roles.stream().map(this::toSimpleVo).toList();
    }

    // ========== 私有方法 ==========

    private boolean existsByName(String name) {
        return roleMapper.selectCount(
                new LambdaQueryWrapper<Role>().eq(Role::getName, name)) > 0;
    }

    private void saveRoleMenus(String roleId, List<String> menuIds) {
        if (menuIds.isEmpty()) return;
        menuIds.forEach(menuId -> {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            rm.setGrantedAt(LocalDateTime.now());
            roleMenuMapper.insert(rm);
        });
    }

    private void deleteRoleMenus(String roleId) {
        roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
    }

    private List<String> getRoleMenuIds(String roleId) {
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
        return roleMenus.stream().map(RoleMenu::getMenuId).toList();
    }

    private RoleVo toVo(Role role) {
        RoleVo vo = new RoleVo();
        BeanUtils.copyProperties(role, vo);
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
        return vo;
    }
}
```

### Controller 实现

```java
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
@Tag(name = "角色管理")
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/list")
    @Operation(summary = "获取角色列表")
    @RequirePermission("system:role:list")
    public PageResult<RoleVo> list(QueryRoleDto query) {
        return roleService.findPage(query);
    }

    @GetMapping("/options")
    @Operation(summary = "获取角色选项")
    public List<RoleVo> options() {
        return roleService.findAllEnabled();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取角色详情")
    @RequirePermission("system:role:query")
    public RoleVo getById(@PathVariable String id) {
        return roleService.findById(id);
    }

    @PostMapping
    @Operation(summary = "创建角色")
    @RequirePermission("system:role:create")
    public RoleVo create(@Valid @RequestBody CreateRoleDto dto) {
        return roleService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    @RequirePermission("system:role:update")
    public RoleVo update(@PathVariable String id,
                         @Valid @RequestBody UpdateRoleDto dto) {
        return roleService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    @RequirePermission("system:role:delete")
    public void delete(@PathVariable String id) {
        roleService.delete(id);
    }
}
```

## 内置角色初始化

在应用启动时初始化内置角色（可通过 `CommandLineRunner` 实现）：

```java
@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleMapper roleMapper;

    @Override
    public void run(String... args) {
        initRole("admin", true);
        initRole("user", false);
    }

    private void initRole(String name, boolean isSuper) {
        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<Role>().eq(Role::getName, name));
        if (count == 0) {
            Role role = new Role();
            role.setName(name);
            role.setStatus(0);
            role.setIsBuiltin(true);
            role.setIsSuper(isSuper);
            roleMapper.insert(role);
        }
    }
}
```

## 与 Nexus 对照

| Nexus | Joint |
|-------|-------|
| `this.prisma.role.create({ data, include: { menus: true } })` | `roleMapper.insert()` + `saveRoleMenus()` |
| `RoleMenu` junction table (Prisma relation) | `sys_role_menu` 手动管理关联表 |
| `PartialType(CreateRoleDto)` | 独立定义 `UpdateRoleDto` |
| `@Module({ onModuleInit })` 初始化 | `CommandLineRunner` 初始化 |

## 知识点总结

| 概念 | 说明 |
|------|------|
| 多对多关联 | 通过中间表 `sys_role_menu` 实现 |
| 先删后增 | 更新权限时先清空再重建关联 |
| 内置保护 | isBuiltin 标记防止删除/修改 |
| 超级管理员 | isSuper 标记拥有全部权限 |

## 练习任务

1. 完成角色模块所有接口
2. 实现角色-菜单权限分配
3. 实现内置角色初始化
4. 测试角色删除保护逻辑
