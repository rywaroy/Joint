package org.joint.modules.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.joint.common.exception.BusinessException;
import org.joint.common.security.JwtTokenProvider;
import org.joint.modules.auth.dto.LoginDto;
import org.joint.modules.auth.vo.LoginVo;
import org.joint.modules.system.role.entity.Role;
import org.joint.modules.system.role.mapper.RoleMapper;
import org.joint.modules.system.user.entity.User;
import org.joint.modules.system.user.entity.UserRole;
import org.joint.modules.system.user.mapper.UserMapper;
import org.joint.modules.system.user.mapper.UserRoleMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginVo login(LoginDto dto) {
        User user = findUserForLogin(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 1) {
            throw new BusinessException("用户已被禁用");
        }

        List<String> roleCodes = getUserRoleCodes(user.getId());
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roleCodes);

        return LoginVo.builder()
                .accessToken(jwtTokenProvider.generateToken(user.getId(), user.getUsername(), claims))
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getNickName())
                .roles(roleCodes)
                .build();
    }

    private User findUserForLogin(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("id", "username", "password", "nick_name", "status");
        wrapper.eq("username", username);
        return userMapper.selectOne(wrapper);
    }

    private List<String> getUserRoleCodes(String userId) {
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserRole> userRoles = userRoleMapper.selectList(wrapper);
        if (userRoles.isEmpty()) {
            return List.of();
        }

        List<String> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .toList();

        return roleMapper.selectBatchIds(roleIds).stream()
                .map(Role::getCode)
                .toList();
    }
}
