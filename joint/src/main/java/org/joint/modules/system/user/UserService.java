package org.joint.modules.system.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.joint.common.exception.BusinessException;
import org.joint.common.exception.ErrorCode;
import org.joint.modules.system.user.dto.CreateUserDto;
import org.joint.modules.system.user.dto.QueryUserDto;
import org.joint.modules.system.user.entity.User;
import org.joint.modules.system.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public User findById(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public IPage<User> findAll(QueryUserDto query) {
        Page<User> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getUsername()),
                     User::getUsername, query.getUsername())
               .like(StringUtils.hasText(query.getNickName()),
                     User::getNickName, query.getNickName())
               .eq(query.getStatus() != null,
                   User::getStatus, query.getStatus())
               .eq(StringUtils.hasText(query.getDeptId()),
                   User::getDeptId, query.getDeptId())
               .orderByDesc(User::getCreatedAt);

        return userMapper.selectPage(page, wrapper);
    }

    public User findByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    public User create(CreateUserDto dto) {
        User existing = findByUsername(dto.getUsername());
        if (existing != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setNickName(dto.getNickName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(dto.getStatus());
        userMapper.insert(user);
        return user;
    }

    public User update(User user) {
        User existing = userMapper.selectById(user.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        userMapper.updateById(user);
        return userMapper.selectById(user.getId());
    }

    public void delete(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        userMapper.deleteById(id);
    }
}
