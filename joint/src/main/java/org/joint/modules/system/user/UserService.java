package org.joint.modules.system.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.joint.common.exception.BusinessException;
import org.joint.common.exception.ErrorCode;
import org.joint.common.security.LoginUser;
import org.joint.modules.system.post.entity.UserPost;
import org.joint.modules.system.post.mapper.UserPostMapper;
import org.joint.modules.system.role.entity.Role;
import org.joint.modules.system.role.mapper.RoleMapper;
import org.joint.modules.system.user.dto.CreateUserDto;
import org.joint.modules.system.user.dto.QueryUserDto;
import org.joint.modules.system.user.dto.RegisterUserDto;
import org.joint.modules.system.user.dto.UpdateUserDto;
import org.joint.modules.system.user.entity.User;
import org.joint.modules.system.user.entity.UserRole;
import org.joint.modules.system.user.mapper.UserMapper;
import org.joint.modules.system.user.mapper.UserRoleMapper;
import org.joint.modules.system.user.vo.CurrentUserInfoVo;
import org.joint.modules.system.user.vo.UserVo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserPostMapper userPostMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public User findEntityById(String id) {
        return getExistingUser(id);
    }

    public Map<String, Object> findPage(QueryUserDto query) {
        Page<User> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<User> result = userMapper.selectPage(page, buildQueryWrapper(query));
        return Map.of(
                "list", toVos(result.getRecords()),
                "total", result.getTotal()
        );
    }

    public UserVo findById(String id) {
        return toVo(getExistingUser(id));
    }

    public CurrentUserInfoVo findCurrentUserInfo(LoginUser loginUser) {
        User user = getExistingUser(loginUser.getUserId());
        CurrentUserInfoVo vo = new CurrentUserInfoVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getNickName());
        vo.setAvatar(user.getAvatar());
        vo.setRoles(loginUser.getRoles());
        return vo;
    }

    public User findByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    public UserVo create(CreateUserDto dto) {
        ensureUnique(dto.getUsername(), dto.getEmail(), dto.getPhone(), null);

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickName(dto.getNickName());
        user.setEmail(normalizeNullableString(dto.getEmail()));
        user.setPhone(normalizeNullableString(dto.getPhone()));
        user.setAvatar(normalizeNullableString(dto.getAvatar()));
        user.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        user.setDeptId(normalizeNullableString(dto.getDeptId()));
        user.setRemark(normalizeNullableString(dto.getRemark()));
        userMapper.insert(user);

        replaceUserRoles(user.getId(), dto.getRoles());
        replaceUserPosts(user.getId(), dto.getPostIds());
        return findById(user.getId());
    }

    public UserVo register(RegisterUserDto dto) {
        ensureUnique(dto.getUsername(), null, null, null);

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickName(dto.getNickName());
        user.setStatus(0);
        userMapper.insert(user);

        replaceUserRoles(user.getId(), List.of("user"));
        return findById(user.getId());
    }

    public UserVo update(String id, UpdateUserDto dto) {
        User user = getExistingUser(id);
        ensureUnique(null, dto.getEmail(), dto.getPhone(), id);

        if (dto.getNickName() != null) {
            user.setNickName(dto.getNickName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(normalizeNullableString(dto.getEmail()));
        }
        if (dto.getPhone() != null) {
            user.setPhone(normalizeNullableString(dto.getPhone()));
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(normalizeNullableString(dto.getAvatar()));
        }
        if (dto.getDeptId() != null) {
            user.setDeptId(normalizeNullableString(dto.getDeptId()));
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        if (dto.getRemark() != null) {
            user.setRemark(normalizeNullableString(dto.getRemark()));
        }

        userMapper.updateById(user);

        if (dto.getRoles() != null) {
            replaceUserRoles(id, dto.getRoles());
        }
        if (dto.getPostIds() != null) {
            replaceUserPosts(id, dto.getPostIds());
        }

        return findById(id);
    }

    public Map<String, String> delete(String id) {
        getExistingUser(id);
        deleteUserRoles(id);
        deleteUserPosts(id);
        userMapper.deleteById(id);
        return Map.of("id", id);
    }

    public UserVo updateStatus(String id, Integer status) {
        User user = getExistingUser(id);
        user.setStatus(status);
        userMapper.updateById(user);
        return findById(id);
    }

    public UserVo resetPassword(String id, String newPassword) {
        getExistingUser(id);
        User passwordUser = getUserWithPassword(id);
        passwordUser.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(passwordUser);
        return findById(id);
    }

    public Map<String, String> changePassword(String id, String oldPassword, String newPassword) {
        User user = getUserWithPassword(id);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return Map.of("message", "密码修改成功");
    }

    private User getExistingUser(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private User getUserWithPassword(String id) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("id", "username", "password", "nickName", "status");
        wrapper.eq("id", id);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private QueryWrapper<User> buildQueryWrapper(QueryUserDto query) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getUsername()), "username", query.getUsername())
                .like(StringUtils.hasText(query.getNickName()), "nickName", query.getNickName())
                .eq(StringUtils.hasText(query.getPhone()), "phone", query.getPhone())
                .eq(query.getStatus() != null, "status", query.getStatus())
                .eq(StringUtils.hasText(query.getDeptId()), "deptId", query.getDeptId())
                .orderByDesc("createdAt");

        if (StringUtils.hasText(query.getPostId())) {
            List<String> userIds = userPostMapper.selectList(
                            new LambdaQueryWrapper<UserPost>().eq(UserPost::getPostId, query.getPostId()))
                    .stream()
                    .map(UserPost::getUserId)
                    .distinct()
                    .toList();
            if (userIds.isEmpty()) {
                wrapper.eq("id", "__no_match__");
            } else {
                wrapper.in("id", userIds);
            }
        }
        return wrapper;
    }

    private void ensureUnique(String username, String email, String phone, String excludeId) {
        if (StringUtils.hasText(username) && existsByField("username", username, excludeId)) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        if (StringUtils.hasText(email) && existsByField("email", email, excludeId)) {
            throw new BusinessException("邮箱已存在");
        }
        if (StringUtils.hasText(phone) && existsByField("phone", phone, excludeId)) {
            throw new BusinessException("手机号已存在");
        }
    }

    private boolean existsByField(String field, String value, String excludeId) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq(field, value);
        if (StringUtils.hasText(excludeId)) {
            wrapper.ne("id", excludeId);
        }
        return userMapper.selectCount(wrapper) > 0;
    }

    private void replaceUserRoles(String userId, List<String> roleNames) {
        deleteUserRoles(userId);
        List<String> normalizedRoles = normalizeRoles(roleNames);
        if (normalizedRoles.isEmpty()) {
            return;
        }

        List<Role> roles = roleMapper.selectList(new LambdaQueryWrapper<Role>().in(Role::getName, normalizedRoles));
        for (Role role : roles) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            userRoleMapper.insert(userRole);
        }
    }

    private void replaceUserPosts(String userId, List<String> postIds) {
        deleteUserPosts(userId);
        if (postIds == null || postIds.isEmpty()) {
            return;
        }
        for (String postId : postIds) {
            UserPost userPost = new UserPost();
            userPost.setUserId(userId);
            userPost.setPostId(postId);
            userPostMapper.insert(userPost);
        }
    }

    private void deleteUserRoles(String userId) {
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
    }

    private void deleteUserPosts(String userId) {
        userPostMapper.delete(new LambdaQueryWrapper<UserPost>().eq(UserPost::getUserId, userId));
    }

    private List<String> normalizeRoles(List<String> roles) {
        if (roles == null) {
            return List.of("user");
        }

        List<String> normalized = roles.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (normalized.isEmpty()) {
            return List.of();
        }
        return normalized;
    }

    private List<UserVo> toVos(List<User> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }

        List<String> userIds = users.stream().map(User::getId).toList();
        Map<String, List<String>> roleNamesByUserId = loadRoleNamesByUserId(userIds);
        Map<String, List<String>> postIdsByUserId = loadPostIdsByUserId(userIds);

        return users.stream()
                .map(user -> toVo(
                        user,
                        roleNamesByUserId.getOrDefault(user.getId(), List.of()),
                        postIdsByUserId.getOrDefault(user.getId(), List.of())
                ))
                .toList();
    }

    private UserVo toVo(User user) {
        return toVos(List.of(user)).get(0);
    }

    private UserVo toVo(User user, List<String> roles, List<String> postIds) {
        UserVo vo = new UserVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickName(user.getNickName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setDeptId(user.getDeptId());
        vo.setRemark(user.getRemark());
        vo.setRoles(roles);
        vo.setPostIds(postIds);
        vo.setCreateTime(formatDateTime(user.getCreatedAt()));
        return vo;
    }

    private Map<String, List<String>> loadRoleNamesByUserId(List<String> userIds) {
        List<UserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, userIds));
        if (userRoles.isEmpty()) {
            return Map.of();
        }

        Set<String> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        Map<String, String> roleNamesById = roleMapper.selectBatchIds(roleIds).stream()
                .collect(LinkedHashMap::new, (map, role) -> map.put(role.getId(), role.getName()), Map::putAll);

        Map<String, List<String>> roleNamesByUserId = new LinkedHashMap<>();
        for (UserRole userRole : userRoles) {
            String roleName = roleNamesById.get(userRole.getRoleId());
            if (!StringUtils.hasText(roleName)) {
                continue;
            }
            roleNamesByUserId.computeIfAbsent(userRole.getUserId(), key -> new ArrayList<>()).add(roleName);
        }
        return roleNamesByUserId;
    }

    private Map<String, List<String>> loadPostIdsByUserId(List<String> userIds) {
        List<UserPost> userPosts = userPostMapper.selectList(new LambdaQueryWrapper<UserPost>().in(UserPost::getUserId, userIds));
        if (userPosts.isEmpty()) {
            return Map.of();
        }

        Map<String, List<String>> postIdsByUserId = new LinkedHashMap<>();
        for (UserPost userPost : userPosts) {
            postIdsByUserId.computeIfAbsent(userPost.getUserId(), key -> new ArrayList<>()).add(userPost.getPostId());
        }
        return postIdsByUserId;
    }

    private String normalizeNullableString(String value) {
        return StringUtils.hasText(value) ? value : null;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toString();
    }
}
