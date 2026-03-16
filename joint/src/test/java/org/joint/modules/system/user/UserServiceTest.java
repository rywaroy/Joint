package org.joint.modules.system.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.joint.common.exception.BusinessException;
import org.joint.modules.system.post.entity.UserPost;
import org.joint.modules.system.post.mapper.UserPostMapper;
import org.joint.modules.system.role.entity.Role;
import org.joint.modules.system.role.mapper.RoleMapper;
import org.joint.modules.system.user.dto.CreateUserDto;
import org.joint.modules.system.user.dto.QueryUserDto;
import org.joint.modules.system.user.dto.UpdateUserDto;
import org.joint.modules.system.user.entity.User;
import org.joint.modules.system.user.entity.UserRole;
import org.joint.modules.system.user.mapper.UserMapper;
import org.joint.modules.system.user.mapper.UserRoleMapper;
import org.joint.modules.system.user.vo.UserVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private UserMapper userMapper;
    private UserRoleMapper userRoleMapper;
    private UserPostMapper userPostMapper;
    private RoleMapper roleMapper;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        userRoleMapper = mock(UserRoleMapper.class);
        userPostMapper = mock(UserPostMapper.class);
        roleMapper = mock(RoleMapper.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userMapper, userRoleMapper, userPostMapper, roleMapper, passwordEncoder);
    }

    @Test
    void findPageReturnsNodeShapeWithRolesAndPostIds() {
        User user = new User();
        user.setId("u-1");
        user.setUsername("alice");
        user.setNickName("Alice");
        user.setStatus(0);

        Page<User> page = new Page<>(1, 10);
        page.setRecords(List.of(user));
        page.setTotal(1);

        UserRole userRole = new UserRole();
        userRole.setUserId("u-1");
        userRole.setRoleId("r-1");

        Role role = new Role();
        role.setId("r-1");
        role.setName("admin");

        UserPost userPost = new UserPost();
        userPost.setUserId("u-1");
        userPost.setPostId("p-1");

        when(userMapper.selectPage(any(), any())).thenReturn(page);
        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(roleMapper.selectBatchIds(any())).thenReturn(List.of(role));
        when(userPostMapper.selectList(any())).thenReturn(List.of(userPost));

        Map<String, Object> result = userService.findPage(new QueryUserDto());

        assertThat(result.get("total")).isEqualTo(1L);
        List<?> list = (List<?>) result.get("list");
        assertThat(list).hasSize(1);
        UserVo userVo = (UserVo) list.get(0);
        assertThat(userVo.getUsername()).isEqualTo("alice");
        assertThat(userVo.getRoles()).containsExactly("admin");
        assertThat(userVo.getPostIds()).containsExactly("p-1");
    }

    @Test
    void findByIdReturnsNodeStyleUserVo() {
        User user = new User();
        user.setId("u-1");
        user.setUsername("alice");
        user.setNickName("Alice");

        UserRole userRole = new UserRole();
        userRole.setUserId("u-1");
        userRole.setRoleId("r-1");

        Role role = new Role();
        role.setId("r-1");
        role.setName("user");

        UserPost userPost = new UserPost();
        userPost.setUserId("u-1");
        userPost.setPostId("p-1");

        when(userMapper.selectById("u-1")).thenReturn(user);
        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(roleMapper.selectBatchIds(any())).thenReturn(List.of(role));
        when(userPostMapper.selectList(any())).thenReturn(List.of(userPost));

        UserVo result = userService.findById("u-1");

        assertThat(result.getId()).isEqualTo("u-1");
        assertThat(result.getRoles()).containsExactly("user");
        assertThat(result.getPostIds()).containsExactly("p-1");
    }

    @Test
    void createEncodesPasswordAndSavesRelationsUsingRoleNames() {
        CreateUserDto dto = new CreateUserDto();
        dto.setUsername("alice");
        dto.setPassword("plain");
        dto.setNickName("Alice");
        dto.setRoles(List.of("admin", "user"));
        dto.setPostIds(List.of("p-1"));

        Role admin = new Role();
        admin.setId("r-1");
        admin.setName("admin");

        Role user = new Role();
        user.setId("r-2");
        user.setName("user");

        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(roleMapper.selectList(any())).thenReturn(List.of(admin, user));
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User created = invocation.getArgument(0);
            created.setId("u-1");
            return 1;
        });
        when(userMapper.selectById("u-1")).thenAnswer(invocation -> {
            User created = new User();
            created.setId("u-1");
            created.setUsername("alice");
            created.setNickName("Alice");
            created.setStatus(0);
            return created;
        });
        when(userRoleMapper.selectList(any())).thenReturn(List.of());
        when(userPostMapper.selectList(any())).thenReturn(List.of());

        UserVo result = userService.create(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded");
        assertThat(result.getId()).isEqualTo("u-1");
        verify(userRoleMapper, times(2)).insert(any(UserRole.class));
        verify(userPostMapper).insert(any(UserPost.class));
    }

    @Test
    void updateReplacesRelationsAndSupportsClearingOptionalFields() {
        User existing = new User();
        existing.setId("u-1");
        existing.setUsername("alice");
        existing.setNickName("Alice");
        existing.setEmail("old@example.com");
        existing.setPhone("13800138000");
        existing.setDeptId("d-1");
        existing.setStatus(0);

        UpdateUserDto dto = new UpdateUserDto();
        dto.setNickName("Alice 2");
        dto.setEmail("");
        dto.setPhone("");
        dto.setDeptId("");
        dto.setStatus(1);
        dto.setRoles(List.of("admin"));
        dto.setPostIds(List.of("p-9"));

        Role role = new Role();
        role.setId("r-9");
        role.setName("admin");

        when(userMapper.selectById("u-1")).thenReturn(existing);
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(roleMapper.selectList(any())).thenReturn(List.of(role));
        when(userRoleMapper.selectList(any())).thenReturn(List.of());
        when(userPostMapper.selectList(any())).thenReturn(List.of());

        UserVo result = userService.update("u-1", dto);

        assertThat(result.getNickName()).isEqualTo("Alice 2");
        assertThat(existing.getEmail()).isNull();
        assertThat(existing.getPhone()).isNull();
        assertThat(existing.getDeptId()).isNull();
        verify(userRoleMapper).delete(any());
        verify(userPostMapper).delete(any());
        verify(userRoleMapper).insert(any(UserRole.class));
        verify(userPostMapper).insert(any(UserPost.class));
    }

    @Test
    void deleteRemovesRelationsAndReturnsDeletedId() {
        User existing = new User();
        existing.setId("u-1");
        existing.setUsername("alice");

        when(userMapper.selectById("u-1")).thenReturn(existing);

        Map<String, String> result = userService.delete("u-1");

        assertThat(result).containsEntry("id", "u-1");
        verify(userRoleMapper).delete(any());
        verify(userPostMapper).delete(any());
        verify(userMapper).deleteById("u-1");
    }

    @Test
    void changePasswordRejectsWrongOldPassword() {
        User user = new User();
        user.setId("u-1");
        user.setPassword("encoded-old");

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("wrong-old", "encoded-old")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("u-1", "wrong-old", "new-pass"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("旧密码错误");
    }
}
