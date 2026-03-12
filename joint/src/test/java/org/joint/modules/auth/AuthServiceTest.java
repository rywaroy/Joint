package org.joint.modules.auth;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserMapper userMapper;
    private UserRoleMapper userRoleMapper;
    private RoleMapper roleMapper;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        userRoleMapper = mock(UserRoleMapper.class);
        roleMapper = mock(RoleMapper.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        authService = new AuthService(userMapper, userRoleMapper, roleMapper, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void loginReturnsTokenAndRoleCodesForActiveUser() {
        User user = new User();
        user.setId("u-1");
        user.setUsername("admin");
        user.setPassword("$2a$10$encoded");
        user.setNickName("管理员");
        user.setStatus(0);

        UserRole userRole = new UserRole();
        userRole.setRoleId("r-1");

        Role role = new Role();
        role.setId("r-1");
        role.setCode("admin");

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("secret", "$2a$10$encoded")).thenReturn(true);
        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(roleMapper.selectBatchIds(List.of("r-1"))).thenReturn(List.of(role));
        when(jwtTokenProvider.generateToken(eq("u-1"), eq("admin"), any())).thenReturn("jwt-token");

        LoginDto dto = new LoginDto();
        dto.setUsername("admin");
        dto.setPassword("secret");

        LoginVo result = authService.login(dto);

        assertThat(result.getAccessToken()).isEqualTo("jwt-token");
        assertThat(result.getId()).isEqualTo("u-1");
        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getRealName()).isEqualTo("管理员");
        assertThat(result.getRoles()).containsExactly("admin");

        ArgumentCaptor<java.util.Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(java.util.Map.class);
        verify(jwtTokenProvider).generateToken(eq("u-1"), eq("admin"), claimsCaptor.capture());
        assertThat(claimsCaptor.getValue()).containsEntry("roles", List.of("admin"));
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("$2a$10$encoded");
        user.setStatus(0);

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("wrong", "$2a$10$encoded")).thenReturn(false);

        LoginDto dto = new LoginDto();
        dto.setUsername("admin");
        dto.setPassword("wrong");

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名或密码错误");
    }

    @Test
    void loginRejectsDisabledUser() {
        User user = new User();
        user.setUsername("locked");
        user.setPassword("$2a$10$encoded");
        user.setStatus(1);

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("secret", "$2a$10$encoded")).thenReturn(true);

        LoginDto dto = new LoginDto();
        dto.setUsername("locked");
        dto.setPassword("secret");

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户已被禁用");
    }
}
