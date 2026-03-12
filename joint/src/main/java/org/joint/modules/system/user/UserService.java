package org.joint.modules.system.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.joint.common.exception.BusinessException;
import org.joint.common.exception.ErrorCode;
import org.joint.common.response.PageResult;
import org.joint.modules.system.dept.entity.Dept;
import org.joint.modules.system.dept.mapper.DeptMapper;
import org.joint.modules.system.dept.vo.DeptVo;
import org.joint.modules.system.post.entity.UserPost;
import org.joint.modules.system.post.mapper.UserPostMapper;
import org.joint.modules.system.user.dto.CreateUserDto;
import org.joint.modules.system.user.dto.QueryUserDto;
import org.joint.modules.system.user.dto.UpdateUserDto;
import org.joint.modules.system.user.entity.User;
import org.joint.modules.system.user.entity.UserRole;
import org.joint.modules.system.user.mapper.UserMapper;
import org.joint.modules.system.user.mapper.UserRoleMapper;
import org.joint.modules.system.user.vo.UserDetailVo;
import org.joint.modules.system.user.vo.UserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserPostMapper userPostMapper;
    private final DeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;

    @Cacheable(value = "user", key = "#id")
    public User findById(String id) {
        return getExistingUser(id);
    }

    public IPage<User> findAll(QueryUserDto query) {
        Page<User> page = new Page<>(query.getPage(), query.getSize());
        return userMapper.selectPage(page, buildQueryWrapper(query));
    }

    public PageResult<UserVo> findPage(QueryUserDto query) {
        Page<User> page = new Page<>(query.getPage(), query.getSize());
        IPage<User> result = userMapper.selectPage(page, buildQueryWrapper(query));
        PageResult<UserVo> response = PageResult.of(result, this::toVo);
        loadDeptNames(response.getData());
        return response;
    }

    @Cacheable(value = "user-detail", key = "#id")
    public UserDetailVo findDetailById(String id) {
        return toDetailVo(getExistingUser(id));
    }

    public User findByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    public UserVo create(CreateUserDto dto) {
        if (existsByUsername(dto.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(0);
        }
        userMapper.insert(user);

        if (dto.getRoleIds() != null) {
            saveUserRoles(user.getId(), dto.getRoleIds());
        }
        if (dto.getPostIds() != null) {
            saveUserPosts(user.getId(), dto.getPostIds());
        }

        return toVo(user);
    }

    public User update(User user) {
        User existing = userMapper.selectById(user.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        userMapper.updateById(user);
        return userMapper.selectById(user.getId());
    }

    @Caching(evict = {
            @CacheEvict(value = "user", key = "#id"),
            @CacheEvict(value = "user-detail", key = "#id")
    })
    public UserVo update(String id, UpdateUserDto dto) {
        User user = getExistingUser(id);

        if (dto.getNickName() != null) {
            user.setNickName(dto.getNickName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getDeptId() != null) {
            user.setDeptId(dto.getDeptId());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        if (dto.getRemark() != null) {
            user.setRemark(dto.getRemark());
        }

        userMapper.updateById(user);

        if (dto.getRoleIds() != null) {
            deleteUserRoles(id);
            saveUserRoles(id, dto.getRoleIds());
        }
        if (dto.getPostIds() != null) {
            deleteUserPosts(id);
            saveUserPosts(id, dto.getPostIds());
        }

        return toVo(user);
    }

    @Caching(evict = {
            @CacheEvict(value = "user", key = "#id"),
            @CacheEvict(value = "user-detail", key = "#id")
    })
    public void delete(String id) {
        User user = getExistingUser(id);
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除管理员用户");
        }

        deleteUserRoles(id);
        deleteUserPosts(id);
        userMapper.deleteById(id);
    }

    @Caching(evict = {
            @CacheEvict(value = "user", key = "#id"),
            @CacheEvict(value = "user-detail", key = "#id")
    })
    public void updateStatus(String id, Integer status) {
        User user = getExistingUser(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Caching(evict = {
            @CacheEvict(value = "user", key = "#id"),
            @CacheEvict(value = "user-detail", key = "#id")
    })
    public void resetPassword(String id, String newPassword) {
        User user = getExistingUser(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private User getExistingUser(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private LambdaQueryWrapper<User> buildQueryWrapper(QueryUserDto query) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getUsername()), User::getUsername, query.getUsername())
                .like(StringUtils.hasText(query.getNickName()), User::getNickName, query.getNickName())
                .eq(query.getStatus() != null, User::getStatus, query.getStatus())
                .eq(StringUtils.hasText(query.getDeptId()), User::getDeptId, query.getDeptId())
                .orderByDesc(User::getCreatedAt);
        return wrapper;
    }

    private boolean existsByUsername(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0;
    }

    private void saveUserRoles(String userId, List<String> roleIds) {
        for (String roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }

    private void deleteUserRoles(String userId) {
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
    }

    private void saveUserPosts(String userId, List<String> postIds) {
        for (String postId : postIds) {
            UserPost userPost = new UserPost();
            userPost.setUserId(userId);
            userPost.setPostId(postId);
            userPostMapper.insert(userPost);
        }
    }

    private void deleteUserPosts(String userId) {
        userPostMapper.delete(new LambdaQueryWrapper<UserPost>().eq(UserPost::getUserId, userId));
    }

    private UserVo toVo(User user) {
        UserVo vo = new UserVo();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    private UserDetailVo toDetailVo(User user) {
        UserDetailVo vo = new UserDetailVo();
        BeanUtils.copyProperties(user, vo);
        vo.setRoleIds(getUserRoleIds(user.getId()));
        vo.setPostIds(getUserPostIds(user.getId()));
        if (user.getDeptId() != null) {
            Dept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                DeptVo deptVo = new DeptVo();
                BeanUtils.copyProperties(dept, deptVo);
                vo.setDept(deptVo);
            }
        }
        return vo;
    }

    private List<String> getUserRoleIds(String userId) {
        return userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)).stream()
                .map(UserRole::getRoleId)
                .toList();
    }

    private List<String> getUserPostIds(String userId) {
        return userPostMapper.selectList(new LambdaQueryWrapper<UserPost>().eq(UserPost::getUserId, userId)).stream()
                .map(UserPost::getPostId)
                .toList();
    }

    private void loadDeptNames(List<UserVo> userVos) {
        Set<String> deptIds = userVos.stream()
                .map(UserVo::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (deptIds.isEmpty()) {
            return;
        }

        Map<String, String> deptNames = deptMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(Dept::getId, Dept::getName));

        for (UserVo userVo : userVos) {
            if (userVo.getDeptId() != null) {
                userVo.setDeptName(deptNames.get(userVo.getDeptId()));
            }
        }
    }
}
