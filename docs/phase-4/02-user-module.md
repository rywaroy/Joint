# 4.2 用户模块

## 学习目标

- 实现用户 CRUD 完整功能
- 处理用户-角色多对多关联
- 对照 Nexus user 模块实现

## API 接口

与 Nexus 保持一致：

| 方法 | 路径 | 描述 | 权限码 |
|------|------|------|--------|
| GET | `/system/user/list` | 用户列表（分页） | `system:user:list` |
| GET | `/system/user/{id}` | 用户详情 | `system:user:query` |
| POST | `/system/user` | 创建用户 | `system:user:add` |
| PUT | `/system/user/{id}` | 更新用户 | `system:user:edit` |
| DELETE | `/system/user/{id}` | 删除用户 | `system:user:delete` |
| PUT | `/system/user/{id}/status` | 修改状态 | `system:user:edit` |
| PUT | `/system/user/{id}/reset-password` | 重置密码 | `system:user:edit` |

## 实现代码

### DTO 定义

```java
// CreateUserDto.java
@Data
public class CreateUserDto {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度为3-20个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickName;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    private String deptId;

    private String remark;

    private List<String> roleIds;

    private List<String> postIds;
}

// UpdateUserDto.java
@Data
public class UpdateUserDto {
    private String nickName;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    private String deptId;

    private Integer status;

    private String remark;

    private List<String> roleIds;

    private List<String> postIds;
}

// QueryUserDto.java
@Data
public class QueryUserDto {
    private Integer page = 1;
    private Integer size = 10;
    private String username;
    private String nickName;
    private Integer status;
    private String deptId;
}
```

### VO 定义

```java
// UserVo.java
@Data
public class UserVo {
    private String id;
    private String username;
    private String nickName;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private String deptId;
    private String deptName;
    private LocalDateTime createdAt;
}

// UserDetailVo.java
@Data
public class UserDetailVo {
    private String id;
    private String username;
    private String nickName;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private String deptId;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<String> roleIds;
    private List<String> postIds;
    private DeptVo dept;
}
```

### Service 实现

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserPostMapper userPostMapper;
    private final DeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserVo> findPage(QueryUserDto query) {
        Page<User> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getUsername()),
                     User::getUsername, query.getUsername())
               .like(StringUtils.hasText(query.getNickName()),
                     User::getNickName, query.getNickName())
               .eq(query.getStatus() != null, User::getStatus, query.getStatus())
               .eq(StringUtils.hasText(query.getDeptId()),
                   User::getDeptId, query.getDeptId())
               .orderByDesc(User::getCreatedAt);

        IPage<User> result = userMapper.selectPage(page, wrapper);

        // 批量加载部门名称
        List<UserVo> voList = result.getRecords().stream()
                .map(this::toVo)
                .toList();
        loadDeptNames(voList);

        return PageResult.of(result.getTotal(), voList);
    }

    @Override
    public UserDetailVo findById(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toDetailVo(user);
    }

    @Override
    @Transactional
    public UserVo create(CreateUserDto dto) {
        // 检查用户名唯一性
        if (existsByUsername(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userMapper.insert(user);

        // 保存角色关联
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            saveUserRoles(user.getId(), dto.getRoleIds());
        }

        // 保存岗位关联
        if (dto.getPostIds() != null && !dto.getPostIds().isEmpty()) {
            saveUserPosts(user.getId(), dto.getPostIds());
        }

        return toVo(user);
    }

    @Override
    @Transactional
    public UserVo update(String id, UpdateUserDto dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新基本信息
        if (dto.getNickName() != null) user.setNickName(dto.getNickName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getDeptId() != null) user.setDeptId(dto.getDeptId());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        if (dto.getRemark() != null) user.setRemark(dto.getRemark());

        userMapper.updateById(user);

        // 更新角色关联
        if (dto.getRoleIds() != null) {
            deleteUserRoles(id);
            if (!dto.getRoleIds().isEmpty()) {
                saveUserRoles(id, dto.getRoleIds());
            }
        }

        // 更新岗位关联
        if (dto.getPostIds() != null) {
            deleteUserPosts(id);
            if (!dto.getPostIds().isEmpty()) {
                saveUserPosts(id, dto.getPostIds());
            }
        }

        return toVo(user);
    }

    @Override
    @Transactional
    public void delete(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 不能删除 admin 用户
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除管理员用户");
        }

        // 删除关联
        deleteUserRoles(id);
        deleteUserPosts(id);

        // 删除用户
        userMapper.deleteById(id);
    }

    @Override
    public void updateStatus(String id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(String id, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    // ========== 私有方法 ==========

    private boolean existsByUsername(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)) > 0;
    }

    private void saveUserRoles(String userId, List<String> roleIds) {
        List<UserRole> userRoles = roleIds.stream()
                .map(roleId -> {
                    UserRole ur = new UserRole();
                    ur.setUserId(userId);
                    ur.setRoleId(roleId);
                    return ur;
                })
                .toList();
        // 批量插入
        userRoles.forEach(userRoleMapper::insert);
    }

    private void deleteUserRoles(String userId) {
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId));
    }

    private void saveUserPosts(String userId, List<String> postIds) {
        List<UserPost> userPosts = postIds.stream()
                .map(postId -> {
                    UserPost up = new UserPost();
                    up.setUserId(userId);
                    up.setPostId(postId);
                    return up;
                })
                .toList();
        userPosts.forEach(userPostMapper::insert);
    }

    private void deleteUserPosts(String userId) {
        userPostMapper.delete(new LambdaQueryWrapper<UserPost>()
                .eq(UserPost::getUserId, userId));
    }

    private UserVo toVo(User user) {
        UserVo vo = new UserVo();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    private UserDetailVo toDetailVo(User user) {
        UserDetailVo vo = new UserDetailVo();
        BeanUtils.copyProperties(user, vo);

        // 加载角色 ID 列表
        vo.setRoleIds(getUserRoleIds(user.getId()));

        // 加载岗位 ID 列表
        vo.setPostIds(getUserPostIds(user.getId()));

        // 加载部门信息
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
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        return userRoles.stream().map(UserRole::getRoleId).toList();
    }

    private List<String> getUserPostIds(String userId) {
        List<UserPost> userPosts = userPostMapper.selectList(
                new LambdaQueryWrapper<UserPost>().eq(UserPost::getUserId, userId));
        return userPosts.stream().map(UserPost::getPostId).toList();
    }

    private void loadDeptNames(List<UserVo> voList) {
        Set<String> deptIds = voList.stream()
                .map(UserVo::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (deptIds.isEmpty()) return;

        Map<String, String> deptNameMap = deptMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(Dept::getId, Dept::getName));

        voList.forEach(vo -> {
            if (vo.getDeptId() != null) {
                vo.setDeptName(deptNameMap.get(vo.getDeptId()));
            }
        });
    }
}
```

### Controller 实现

```java
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@Tag(name = "用户管理")
public class SystemUserController {

    private final UserService userService;

    @GetMapping("/list")
    @Operation(summary = "获取用户列表")
    @RequirePermission("system:user:list")
    public PageResult<UserVo> list(QueryUserDto query) {
        return userService.findPage(query);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    @RequirePermission("system:user:query")
    public UserDetailVo getById(@PathVariable String id) {
        return userService.findById(id);
    }

    @PostMapping
    @Operation(summary = "创建用户")
    @RequirePermission("system:user:add")
    public UserVo create(@Valid @RequestBody CreateUserDto dto) {
        return userService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    @RequirePermission("system:user:edit")
    public UserVo update(@PathVariable String id,
                         @Valid @RequestBody UpdateUserDto dto) {
        return userService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @RequirePermission("system:user:delete")
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "修改用户状态")
    @RequirePermission("system:user:edit")
    public void updateStatus(@PathVariable String id,
                             @RequestParam Integer status) {
        userService.updateStatus(id, status);
    }

    @PutMapping("/{id}/reset-password")
    @Operation(summary = "重置密码")
    @RequirePermission("system:user:edit")
    public void resetPassword(@PathVariable String id,
                              @RequestParam String password) {
        userService.resetPassword(id, password);
    }
}
```

## 与 Nexus 对照

| Nexus | Joint |
|-------|-------|
| `this.prisma.user.findMany()` | `userMapper.selectPage()` |
| `include: { roles: true }` | 手动查询关联表 |
| `BeanUtils.copyProperties()` | `Object.assign()` |

## 练习任务

1. 完成用户模块所有接口
2. 测试用户 CRUD 功能
3. 测试用户角色关联功能
