# 4.4 菜单模块

## 学习目标

- 实现树形结构的 CRUD
- 递归构建菜单树
- 对照 Nexus menu 模块实现

## 数据结构

```java
@Data
@TableName("sys_menu")
public class Menu {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String parentId;      // 父级 ID，顶级为 null 或 "0"
    private String name;          // 菜单名称
    private String path;          // 路由路径
    private String component;     // 组件路径
    private String icon;          // 图标
    private Integer type;         // 类型：0-目录 1-菜单 2-按钮
    private String authCode;      // 权限标识
    private Integer sort;         // 排序
    private Integer status;       // 状态：0-正常 1-禁用
    private Boolean hidden;       // 是否隐藏
    // ...
}
```

## 树形结构处理

### 构建树形结构

```java
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;

    @Override
    public List<MenuVo> getMenuTree() {
        // 查询所有菜单
        List<Menu> menus = menuMapper.selectList(
                new LambdaQueryWrapper<Menu>()
                        .eq(Menu::getStatus, 0)
                        .orderByAsc(Menu::getSort)
        );

        // 转换为 VO
        List<MenuVo> menuVos = menus.stream()
                .map(this::toVo)
                .toList();

        // 构建树形结构
        return buildTree(menuVos, null);
    }

    /**
     * 递归构建树形结构
     */
    private List<MenuVo> buildTree(List<MenuVo> menus, String parentId) {
        return menus.stream()
                .filter(menu -> {
                    if (parentId == null) {
                        return menu.getParentId() == null
                               || "0".equals(menu.getParentId())
                               || menu.getParentId().isEmpty();
                    }
                    return parentId.equals(menu.getParentId());
                })
                .peek(menu -> {
                    List<MenuVo> children = buildTree(menus, menu.getId());
                    menu.setChildren(children.isEmpty() ? null : children);
                })
                .toList();
    }
}
```

### VO 定义

```java
@Data
public class MenuVo {
    private String id;
    private String parentId;
    private String name;
    private String path;
    private String component;
    private String icon;
    private Integer type;
    private String authCode;
    private Integer sort;
    private Integer status;
    private Boolean hidden;
    private List<MenuVo> children;  // 子菜单
}
```

### 前端路由格式

如果前端需要特定格式的路由数据：

```java
@Data
public class RouteVo {
    private String path;
    private String name;
    private String component;
    private RouteMeta meta;
    private List<RouteVo> children;

    @Data
    public static class RouteMeta {
        private String title;
        private String icon;
        private Boolean hidden;
        private Integer order;
    }
}
```

```java
private RouteVo toRouteVo(Menu menu) {
    RouteVo route = new RouteVo();
    route.setPath(menu.getPath());
    route.setName(menu.getName());
    route.setComponent(menu.getComponent());

    RouteMeta meta = new RouteMeta();
    meta.setTitle(menu.getName());
    meta.setIcon(menu.getIcon());
    meta.setHidden(menu.getHidden());
    meta.setOrder(menu.getSort());
    route.setMeta(meta);

    return route;
}
```

## 完整 CRUD 实现

```java
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;

    @Override
    public List<MenuVo> getMenuTree() {
        List<Menu> menus = menuMapper.selectList(
                new LambdaQueryWrapper<Menu>().orderByAsc(Menu::getSort));
        return buildTree(menus.stream().map(this::toVo).toList(), null);
    }

    @Override
    public MenuVo findById(String id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        return toVo(menu);
    }

    @Override
    @Transactional
    public MenuVo create(CreateMenuDto dto) {
        // 验证父级菜单是否存在
        if (StringUtils.hasText(dto.getParentId()) && !"0".equals(dto.getParentId())) {
            Menu parent = menuMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("父级菜单不存在");
            }
        }

        Menu menu = new Menu();
        BeanUtils.copyProperties(dto, menu);
        menuMapper.insert(menu);
        return toVo(menu);
    }

    @Override
    @Transactional
    public MenuVo update(String id, UpdateMenuDto dto) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        // 不能将自己设为自己的父级
        if (id.equals(dto.getParentId())) {
            throw new BusinessException("不能将自己设为父级菜单");
        }

        // 不能将自己设为子级的父级
        if (StringUtils.hasText(dto.getParentId())) {
            List<String> childIds = getChildIds(id);
            if (childIds.contains(dto.getParentId())) {
                throw new BusinessException("不能将子菜单设为父级");
            }
        }

        BeanUtils.copyProperties(dto, menu);
        menuMapper.updateById(menu);
        return toVo(menu);
    }

    @Override
    @Transactional
    public void delete(String id) {
        // 检查是否有子菜单
        Long childCount = menuMapper.selectCount(
                new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("请先删除子菜单");
        }

        // 删除角色-菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getMenuId, id));

        // 删除菜单
        menuMapper.deleteById(id);
    }

    /**
     * 获取所有子菜单 ID（递归）
     */
    private List<String> getChildIds(String parentId) {
        List<String> result = new ArrayList<>();
        List<Menu> children = menuMapper.selectList(
                new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, parentId));
        for (Menu child : children) {
            result.add(child.getId());
            result.addAll(getChildIds(child.getId()));
        }
        return result;
    }

    /**
     * 获取用户菜单树（根据权限过滤）
     */
    @Override
    public List<MenuVo> getUserMenuTree(String userId) {
        // 1. 获取用户的菜单 ID 列表
        Set<String> menuIds = getUserMenuIds(userId);

        // 2. 查询菜单
        List<Menu> menus;
        if (menuIds.isEmpty()) {
            return List.of();
        }

        menus = menuMapper.selectList(
                new LambdaQueryWrapper<Menu>()
                        .in(Menu::getId, menuIds)
                        .eq(Menu::getStatus, 0)
                        .ne(Menu::getType, 2)  // 排除按钮
                        .orderByAsc(Menu::getSort)
        );

        // 3. 构建树
        return buildTree(menus.stream().map(this::toVo).toList(), null);
    }

    private Set<String> getUserMenuIds(String userId) {
        // 查询用户角色
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));

        if (userRoles.isEmpty()) {
            return Set.of();
        }

        // 查询角色菜单
        List<String> roleIds = userRoles.stream()
                .map(UserRole::getRoleId).toList();

        List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenu>().in(RoleMenu::getRoleId, roleIds));

        return roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toSet());
    }
}
```

## Controller

```java
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/tree")
    @RequirePermission("system:menu:list")
    public List<MenuVo> getTree() {
        return menuService.getMenuTree();
    }

    @GetMapping("/{id}")
    @RequirePermission("system:menu:query")
    public MenuVo getById(@PathVariable String id) {
        return menuService.findById(id);
    }

    @PostMapping
    @RequirePermission("system:menu:add")
    public MenuVo create(@Valid @RequestBody CreateMenuDto dto) {
        return menuService.create(dto);
    }

    @PutMapping("/{id}")
    @RequirePermission("system:menu:edit")
    public MenuVo update(@PathVariable String id,
                         @Valid @RequestBody UpdateMenuDto dto) {
        return menuService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:menu:delete")
    public void delete(@PathVariable String id) {
        menuService.delete(id);
    }
}
```

## 知识点总结

| 概念 | 说明 |
|------|------|
| 树形结构 | parentId 实现父子关系 |
| 递归构建 | buildTree 递归组装 |
| 级联删除 | 检查子节点后删除 |
| 循环引用检查 | 防止自己成为自己的父级 |

## 练习任务

1. 实现菜单树 CRUD
2. 实现用户菜单树（根据权限过滤）
3. 实现路由格式转换
