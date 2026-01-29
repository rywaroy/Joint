# 4.1 模块结构规范

## 学习目标

- 理解 Controller-Service-Mapper 三层架构
- 掌握 DTO/VO/Entity 的区别和使用场景
- 建立统一的模块开发规范

## 三层架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Controller 层                           │
│  职责：接收请求、参数校验、调用 Service、返回响应              │
│  注解：@RestController, @RequestMapping                      │
└─────────────────────────────┬───────────────────────────────┘
                              │ 调用
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       Service 层                             │
│  职责：业务逻辑处理、事务管理、数据组装                        │
│  注解：@Service, @Transactional                              │
└─────────────────────────────┬───────────────────────────────┘
                              │ 调用
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       Mapper 层                              │
│  职责：数据库操作、SQL 执行                                   │
│  注解：@Mapper, 继承 BaseMapper                              │
└─────────────────────────────────────────────────────────────┘
```

## 数据对象类型

| 类型 | 全称 | 用途 | 示例 |
|------|------|------|------|
| **Entity** | Entity | 数据库表映射 | User.java |
| **DTO** | Data Transfer Object | 接收前端数据 | CreateUserDto.java |
| **VO** | View Object | 返回给前端的数据 | UserVo.java |
| **Query** | Query Object | 查询条件封装 | QueryUserDto.java |

### 为什么需要这些区分？

1. **Entity**：与数据库表一一对应，包含所有字段
2. **DTO**：只包含接口需要的字段，避免暴露不需要的字段
3. **VO**：只返回前端需要的数据，可能聚合多个 Entity
4. **Query**：封装分页、筛选条件

```java
// Entity - 完整的数据库字段
public class User {
    private String id;
    private String username;
    private String password;      // 敏感字段
    private String nickName;
    private Integer status;
    private Integer deleted;      // 内部字段
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// CreateDto - 创建时需要的字段
public class CreateUserDto {
    private String username;      // 必填
    private String password;      // 必填
    private String nickName;
    private List<String> roleIds; // 关联数据
}

// UpdateDto - 更新时需要的字段
public class UpdateUserDto {
    private String nickName;      // 可选
    private String email;
    private Integer status;       // 不能修改 username
}

// VO - 返回给前端的字段
public class UserVo {
    private String id;
    private String username;
    private String nickName;
    private List<RoleVo> roles;   // 关联数据
    private String deptName;      // 聚合数据
    // 没有 password、deleted
}

// Query - 查询条件
public class QueryUserDto {
    private Integer page = 1;
    private Integer size = 10;
    private String username;      // 模糊搜索
    private Integer status;       // 精确筛选
    private String deptId;        // 关联筛选
}
```

## 模块目录结构

```
src/main/java/com/joint/modules/
└── system/
    └── user/
        ├── entity/
        │   ├── User.java
        │   └── UserRole.java          # 关联表实体
        ├── mapper/
        │   ├── UserMapper.java
        │   └── UserRoleMapper.java
        ├── dto/
        │   ├── CreateUserDto.java
        │   ├── UpdateUserDto.java
        │   ├── QueryUserDto.java
        │   └── AssignRoleDto.java     # 分配角色 DTO
        ├── vo/
        │   ├── UserVo.java
        │   └── UserDetailVo.java      # 详情 VO
        ├── UserService.java           # 接口
        ├── UserServiceImpl.java       # 实现
        └── UserController.java
```

## Controller 规范

```java
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@Tag(name = "用户管理")  // Swagger 分组
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户列表")
    @RequirePermission("system:user:list")
    public PageResult<UserVo> list(QueryUserDto query) {
        return userService.findPage(query);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    @RequirePermission("system:user:query")
    public UserDetailVo getById(@PathVariable String id) {
        return userService.findById(id);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @Operation(summary = "创建用户")
    @RequirePermission("system:user:add")
    public UserVo create(@Valid @RequestBody CreateUserDto dto) {
        return userService.create(dto);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    @RequirePermission("system:user:edit")
    public UserVo update(@PathVariable String id,
                         @Valid @RequestBody UpdateUserDto dto) {
        return userService.update(id, dto);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @RequirePermission("system:user:delete")
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }
}
```

## Service 规范

```java
// 接口
public interface UserService {
    PageResult<UserVo> findPage(QueryUserDto query);
    UserDetailVo findById(String id);
    UserVo create(CreateUserDto dto);
    UserVo update(String id, UpdateUserDto dto);
    void delete(String id);
}

// 实现
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserVo> findPage(QueryUserDto query) {
        Page<User> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<User> wrapper = buildQueryWrapper(query);
        IPage<User> result = userMapper.selectPage(page, wrapper);
        return PageResult.of(result, this::toVo);
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
    @Transactional  // 涉及多表操作，需要事务
    public UserVo create(CreateUserDto dto) {
        // 1. 检查用户名唯一
        checkUsernameUnique(dto.getUsername(), null);

        // 2. 创建用户
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userMapper.insert(user);

        // 3. 保存用户角色关联
        saveUserRoles(user.getId(), dto.getRoleIds());

        return toVo(user);
    }

    @Override
    @Transactional
    public UserVo update(String id, UpdateUserDto dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新字段
        if (dto.getNickName() != null) {
            user.setNickName(dto.getNickName());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }

        userMapper.updateById(user);
        return toVo(user);
    }

    @Override
    @Transactional
    public void delete(String id) {
        // 1. 删除用户角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, id));
        // 2. 删除用户（逻辑删除）
        userMapper.deleteById(id);
    }

    // 私有方法
    private LambdaQueryWrapper<User> buildQueryWrapper(QueryUserDto query) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getUsername()),
                     User::getUsername, query.getUsername())
               .eq(query.getStatus() != null, User::getStatus, query.getStatus())
               .orderByDesc(User::getCreatedAt);
        return wrapper;
    }

    private UserVo toVo(User user) {
        UserVo vo = new UserVo();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    private UserDetailVo toDetailVo(User user) {
        UserDetailVo vo = new UserDetailVo();
        BeanUtils.copyProperties(user, vo);
        // 加载关联数据
        vo.setRoles(getUserRoles(user.getId()));
        return vo;
    }
}
```

## 命名规范

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| Controller | `XxxController` | `UserController` |
| Service 接口 | `XxxService` | `UserService` |
| Service 实现 | `XxxServiceImpl` | `UserServiceImpl` |
| Mapper | `XxxMapper` | `UserMapper` |
| Entity | 单数名词 | `User` |
| DTO | `XxxDto` | `CreateUserDto` |
| VO | `XxxVo` | `UserVo` |
| Query | `QueryXxxDto` | `QueryUserDto` |

## 接口路径规范

| 操作 | HTTP 方法 | 路径 | 示例 |
|------|----------|------|------|
| 列表 | GET | `/list` | `GET /system/user/list` |
| 详情 | GET | `/{id}` | `GET /system/user/123` |
| 创建 | POST | `/` | `POST /system/user` |
| 更新 | PUT | `/{id}` | `PUT /system/user/123` |
| 删除 | DELETE | `/{id}` | `DELETE /system/user/123` |
| 批量删除 | DELETE | `/batch` | `DELETE /system/user/batch` |

## 知识点总结

| 概念 | 说明 |
|------|------|
| 三层架构 | Controller-Service-Mapper |
| Entity | 数据库实体 |
| DTO | 数据传输对象 |
| VO | 视图对象 |
| BeanUtils.copyProperties | 对象属性拷贝 |
| @Transactional | 事务注解 |

## 练习任务

1. 按规范创建用户模块目录结构
2. 定义 DTO 和 VO 类
3. 实现完整的 CRUD 接口
