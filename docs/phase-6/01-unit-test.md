# 6.1 单元测试

## 学习目标

- 使用 JUnit 5 编写测试
- 使用 Mockito 模拟依赖
- 测试 Service 层业务逻辑

## 测试依赖

Spring Boot 已包含测试依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

包含：
- JUnit 5
- Mockito
- AssertJ
- Spring Test

## Service 层单元测试

```java
package com.joint.modules.system.user;

import com.joint.common.exception.BusinessException;
import com.joint.modules.system.user.dto.CreateUserDto;
import com.joint.modules.system.user.entity.User;
import com.joint.modules.system.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("1");
        testUser.setUsername("testuser");
        testUser.setPassword("encoded_password");
        testUser.setNickName("测试用户");
        testUser.setStatus(0);
    }

    @Test
    @DisplayName("根据ID查询用户 - 成功")
    void findById_Success() {
        // Given
        when(userMapper.selectById("1")).thenReturn(testUser);

        // When
        User result = userService.findById("1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userMapper).selectById("1");
    }

    @Test
    @DisplayName("根据ID查询用户 - 用户不存在")
    void findById_NotFound() {
        // Given
        when(userMapper.selectById("999")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> userService.findById("999"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");
    }

    @Test
    @DisplayName("创建用户 - 成功")
    void create_Success() {
        // Given
        CreateUserDto dto = new CreateUserDto();
        dto.setUsername("newuser");
        dto.setPassword("123456");
        dto.setNickName("新用户");

        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("123456")).thenReturn("encoded_password");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // When
        User result = userService.create(dto);

        // Then
        assertThat(result).isNotNull();
        verify(passwordEncoder).encode("123456");
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("创建用户 - 用户名已存在")
    void create_UsernameExists() {
        // Given
        CreateUserDto dto = new CreateUserDto();
        dto.setUsername("existuser");

        when(userMapper.selectCount(any())).thenReturn(1L);

        // When & Then
        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名已存在");

        verify(userMapper, never()).insert(any());
    }

    @Test
    @DisplayName("删除用户 - 不能删除管理员")
    void delete_CannotDeleteAdmin() {
        // Given
        User admin = new User();
        admin.setId("1");
        admin.setUsername("admin");
        when(userMapper.selectById("1")).thenReturn(admin);

        // When & Then
        assertThatThrownBy(() -> userService.delete("1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("不能删除管理员用户");
    }
}
```

## 常用断言（AssertJ）

```java
// 基本断言
assertThat(result).isNotNull();
assertThat(result).isNull();
assertThat(result).isEqualTo(expected);

// 字符串
assertThat(str).isNotEmpty();
assertThat(str).contains("sub");
assertThat(str).startsWith("prefix");

// 集合
assertThat(list).hasSize(3);
assertThat(list).contains("item");
assertThat(list).isEmpty();

// 异常
assertThatThrownBy(() -> method())
    .isInstanceOf(BusinessException.class)
    .hasMessage("error message");

// 对象属性
assertThat(user)
    .extracting("username", "status")
    .containsExactly("admin", 0);
```

## 常用 Mockito 用法

```java
// 模拟返回值
when(mapper.selectById("1")).thenReturn(user);
when(mapper.selectList(any())).thenReturn(List.of(user));

// 模拟抛出异常
when(mapper.selectById("1")).thenThrow(new RuntimeException());

// 验证调用
verify(mapper).selectById("1");
verify(mapper, times(2)).insert(any());
verify(mapper, never()).delete(any());

// 参数捕获
ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
verify(mapper).insert(captor.capture());
User captured = captor.getValue();
assertThat(captured.getUsername()).isEqualTo("test");
```

## 运行测试

```bash
# 运行所有测试
./mvnw test

# 运行指定类
./mvnw test -Dtest=UserServiceTest

# 运行指定方法
./mvnw test -Dtest=UserServiceTest#findById_Success

# 生成测试报告
./mvnw test jacoco:report
```

## 知识点总结

| 注解 | 说明 |
|------|------|
| @ExtendWith | 启用扩展（Mockito） |
| @Mock | 创建模拟对象 |
| @InjectMocks | 注入模拟对象 |
| @BeforeEach | 每个测试前执行 |
| @Test | 测试方法 |
| @DisplayName | 测试显示名称 |

## 练习任务

1. 为 UserService 编写完整的单元测试
2. 测试正常流程和异常流程
3. 达到 80% 以上的代码覆盖率
