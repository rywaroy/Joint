package org.joint.modules.system.menu;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.joint.common.exception.BusinessException;
import org.joint.modules.system.menu.dto.CreateMenuDto;
import org.joint.modules.system.menu.dto.UpdateMenuDto;
import org.joint.modules.system.menu.entity.Menu;
import org.joint.modules.system.menu.mapper.MenuMapper;
import org.joint.modules.system.menu.vo.MenuRouteVo;
import org.joint.modules.system.menu.vo.MenuVo;
import org.joint.modules.system.role.entity.RoleMenu;
import org.joint.modules.system.role.mapper.RoleMenuMapper;
import org.joint.modules.system.user.entity.UserRole;
import org.joint.modules.system.user.mapper.UserRoleMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;

    public List<MenuVo> getMenuTree() {
        List<MenuVo> menus = menuMapper.selectList(new LambdaQueryWrapper<Menu>()
                        .orderByAsc(Menu::getSort)
                        .orderByDesc(Menu::getCreatedAt))
                .stream()
                .map(this::toVo)
                .sorted(menuComparator())
                .toList();
        return buildTree(menus, null);
    }

    public MenuVo findById(String id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        return toVo(menu);
    }

    public MenuVo create(CreateMenuDto dto) {
        validateParent(dto.getParentId());

        Menu menu = new Menu();
        BeanUtils.copyProperties(dto, menu);
        if (menu.getSort() == null) {
            menu.setSort(0);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(0);
        }
        if (menu.getHidden() == null) {
            menu.setHidden(false);
        }
        menuMapper.insert(menu);
        return toVo(menu);
    }

    public MenuVo update(String id, UpdateMenuDto dto) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        if (id.equals(dto.getParentId())) {
            throw new BusinessException("不能将自己设为父级菜单");
        }
        if (StringUtils.hasText(dto.getParentId())) {
            validateParent(dto.getParentId());
            if (getChildIds(id).contains(dto.getParentId())) {
                throw new BusinessException("不能将子菜单设为父级");
            }
        }

        if (dto.getParentId() != null) {
            menu.setParentId(dto.getParentId());
        }
        if (dto.getName() != null) {
            menu.setName(dto.getName());
        }
        if (dto.getPath() != null) {
            menu.setPath(dto.getPath());
        }
        if (dto.getComponent() != null) {
            menu.setComponent(dto.getComponent());
        }
        if (dto.getIcon() != null) {
            menu.setIcon(dto.getIcon());
        }
        if (dto.getType() != null) {
            menu.setType(dto.getType());
        }
        if (dto.getAuthCode() != null) {
            menu.setAuthCode(dto.getAuthCode());
        }
        if (dto.getSort() != null) {
            menu.setSort(dto.getSort());
        }
        if (dto.getStatus() != null) {
            menu.setStatus(dto.getStatus());
        }
        if (dto.getHidden() != null) {
            menu.setHidden(dto.getHidden());
        }

        menuMapper.updateById(menu);
        return toVo(menu);
    }

    public void delete(String id) {
        Long childCount = menuMapper.selectCount(new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("请先删除子菜单");
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getMenuId, id));
        menuMapper.deleteById(id);
    }

    public List<MenuVo> getUserMenuTree(String userId) {
        List<UserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return List.of();
        }

        List<String> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(new LambdaQueryWrapper<RoleMenu>().in(RoleMenu::getRoleId, roleIds));
        if (roleMenus.isEmpty()) {
            return List.of();
        }

        Set<String> menuIds = roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toSet());
        List<MenuVo> menus = menuMapper.selectList(new LambdaQueryWrapper<Menu>()
                        .in(Menu::getId, menuIds)
                        .eq(Menu::getStatus, 0)
                        .orderByAsc(Menu::getSort)
                        .orderByDesc(Menu::getCreatedAt))
                .stream()
                .filter(menu -> !Integer.valueOf(2).equals(menu.getType()))
                .map(this::toVo)
                .sorted(menuComparator())
                .toList();
        return buildTree(menus, null);
    }

    public List<MenuRouteVo> getCurrentUserRoutes(String userId) {
        return getUserMenuTree(userId).stream()
                .map(this::toRouteVo)
                .toList();
    }

    private void validateParent(String parentId) {
        if (!StringUtils.hasText(parentId) || "0".equals(parentId)) {
            return;
        }
        if (menuMapper.selectById(parentId) == null) {
            throw new BusinessException("父级菜单不存在");
        }
    }

    private List<String> getChildIds(String parentId) {
        List<String> childIds = new ArrayList<>();
        List<Menu> children = menuMapper.selectList(new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, parentId));
        for (Menu child : children) {
            childIds.add(child.getId());
            childIds.addAll(getChildIds(child.getId()));
        }
        return childIds;
    }

    private List<MenuVo> buildTree(List<MenuVo> menus, String parentId) {
        return menus.stream()
                .filter(menu -> isSameParent(parentId, menu.getParentId()))
                .peek(menu -> {
                    List<MenuVo> children = buildTree(menus, menu.getId());
                    if (!children.isEmpty()) {
                        menu.setChildren(children);
                    }
                })
                .sorted(menuComparator())
                .toList();
    }

    private boolean isSameParent(String expectedParentId, String actualParentId) {
        if (!StringUtils.hasText(expectedParentId)) {
            return !StringUtils.hasText(actualParentId) || "0".equals(actualParentId);
        }
        return expectedParentId.equals(actualParentId);
    }

    private Comparator<MenuVo> menuComparator() {
        return Comparator.comparing(MenuVo::getSort, Comparator.nullsFirst(Integer::compareTo))
                .thenComparing(MenuVo::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder()));
    }

    private MenuRouteVo toRouteVo(MenuVo menu) {
        MenuRouteVo route = new MenuRouteVo();
        route.setName(menu.getName());
        route.setPath(menu.getPath());
        route.setMeta(buildRouteMeta(menu));
        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            List<MenuRouteVo> children = menu.getChildren().stream()
                    .map(this::toRouteVo)
                    .toList();
            route.setComponent("BasicLayout");
            route.setChildren(children);
            route.setRedirect(children.get(0).getPath());
            return route;
        }

        route.setComponent(menu.getComponent());
        return route;
    }

    private Map<String, Object> buildRouteMeta(MenuVo menu) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("title", menu.getName());
        if (StringUtils.hasText(menu.getIcon())) {
            meta.put("icon", menu.getIcon());
        }
        if (menu.getSort() != null) {
            meta.put("order", menu.getSort());
        }
        if (Boolean.TRUE.equals(menu.getHidden())) {
            meta.put("hideInMenu", true);
        }
        return meta;
    }

    private MenuVo toVo(Menu menu) {
        MenuVo vo = new MenuVo();
        BeanUtils.copyProperties(menu, vo);
        return vo;
    }
}
