package org.joint.modules.system.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.joint.common.exception.BusinessException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

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
    private DeptMapper deptMapper;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        userRoleMapper = mock(UserRoleMapper.class);
        userPostMapper = mock(UserPostMapper.class);
        deptMapper = mock(DeptMapper.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userMapper, userRoleMapper, userPostMapper, deptMapper, passwordEncoder);
    }

    @Test
    void findPageReturnsUserVosWithDeptName() {
        User user = new User();
        user.setId("u-1");
        user.setUsername("alice");
        user.setNickName("Alice");
        user.setDeptId("d-1");
        user.setStatus(0);

        Page<User> page = new Page<>(1, 10);
        page.setRecords(List.of(user));
        page.setTotal(1);

        Dept dept = new Dept();
        dept.setId("d-1");
        dept.setName("研发部");

        when(userMapper.selectPage(any(), any())).thenReturn(page);
        when(deptMapper.selectBatchIds(any())).thenReturn(List.of(dept));

        PageResult<UserVo> result = userService.findPage(new QueryUserDto());

        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getUsername()).isEqualTo("alice");
        assertThat(result.getData().get(0).getDeptName()).isEqualTo("研发部");
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    void findDetailByIdLoadsRoleIdsPostIdsAndDept() {
        User user = new User();
        user.setId("u-1");
        user.setUsername("alice");
        user.setNickName("Alice");
        user.setDeptId("d-1");

        UserRole userRole = new UserRole();
        userRole.setUserId("u-1");
        userRole.setRoleId("r-1");

        UserPost userPost = new UserPost();
        userPost.setUserId("u-1");
        userPost.setPostId("p-1");

        Dept dept = new Dept();
        dept.setId("d-1");
        dept.setName("研发部");

        when(userMapper.selectById("u-1")).thenReturn(user);
        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(userPostMapper.selectList(any())).thenReturn(List.of(userPost));
        when(deptMapper.selectById("d-1")).thenReturn(dept);

        UserDetailVo result = userService.findDetailById("u-1");

        assertThat(result.getId()).isEqualTo("u-1");
        assertThat(result.getRoleIds()).containsExactly("r-1");
        assertThat(result.getPostIds()).containsExactly("p-1");
        assertThat(result.getDept()).extracting(DeptVo::getName).isEqualTo("研发部");
    }

    @Test
    void createEncodesPasswordAndSavesRelations() {
        CreateUserDto dto = new CreateUserDto();
        dto.setUsername("alice");
        dto.setPassword("plain");
        dto.setNickName("Alice");
        dto.setDeptId("d-1");
        dto.setRoleIds(List.of("r-1", "r-2"));
        dto.setPostIds(List.of("p-1"));

        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("u-1");
            return 1;
        });

        UserVo result = userService.create(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded");
        assertThat(result.getId()).isEqualTo("u-1");
        verify(userRoleMapper, times(2)).insert(any(UserRole.class));
        verify(userPostMapper).insert(any(UserPost.class));
    }

    @Test
    void updateReplacesRelationsAndUpdatesMutableFields() {
        User existing = new User();
        existing.setId("u-1");
        existing.setUsername("alice");
        existing.setNickName("Alice");
        existing.setDeptId("d-1");
        existing.setStatus(0);

        UpdateUserDto dto = new UpdateUserDto();
        dto.setNickName("Alice 2");
        dto.setDeptId("d-2");
        dto.setStatus(1);
        dto.setRoleIds(List.of("r-9"));
        dto.setPostIds(List.of("p-9"));

        when(userMapper.selectById("u-1")).thenReturn(existing);

        UserVo result = userService.update("u-1", dto);

        assertThat(result.getNickName()).isEqualTo("Alice 2");
        verify(userRoleMapper).delete(any());
        verify(userPostMapper).delete(any());
        verify(userRoleMapper).insert(any(UserRole.class));
        verify(userPostMapper).insert(any(UserPost.class));
    }

    @Test
    void deleteRejectsAdminUser() {
        User user = new User();
        user.setId("u-1");
        user.setUsername("admin");

        when(userMapper.selectById("u-1")).thenReturn(user);

        assertThatThrownBy(() -> userService.delete("u-1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("不能删除管理员用户");
    }

    @Test
    void updateStatusAndResetPasswordPersistExpectedFields() {
        User user = new User();
        user.setId("u-1");
        user.setUsername("alice");
        user.setStatus(0);

        when(userMapper.selectById("u-1")).thenReturn(user);
        when(passwordEncoder.encode("new-pass")).thenReturn("encoded-pass");

        userService.updateStatus("u-1", 1);
        userService.resetPassword("u-1", "new-pass");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper, times(2)).updateById(userCaptor.capture());
        assertThat(userCaptor.getAllValues().get(0).getStatus()).isEqualTo(1);
        assertThat(userCaptor.getAllValues().get(1).getPassword()).isEqualTo("encoded-pass");
    }
}
