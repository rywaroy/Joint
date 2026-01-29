# 2.4 分页与查询

## 学习目标

- 配置分页插件
- 掌握 QueryWrapper 条件构造器
- 实现复杂查询

## 与 Nexus 对照

Nexus 分页查询：
```typescript
async findAll(query: QueryUserDto) {
    const { page, size, username, status } = query;
    const where: Prisma.UserWhereInput = {};

    if (username) {
        where.username = { contains: username };
    }
    if (status !== undefined) {
        where.status = status;
    }

    const [data, total] = await Promise.all([
        this.prisma.user.findMany({
            where,
            skip: (page - 1) * size,
            take: size,
        }),
        this.prisma.user.count({ where }),
    ]);

    return { data, total, page, size };
}
```

Joint 使用 MyBatis-Plus：
```java
public IPage<User> findAll(QueryUserDto query) {
    Page<User> page = new Page<>(query.getPage(), query.getSize());
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

    if (StringUtils.hasText(query.getUsername())) {
        wrapper.like(User::getUsername, query.getUsername());
    }
    if (query.getStatus() != null) {
        wrapper.eq(User::getStatus, query.getStatus());
    }

    return userMapper.selectPage(page, wrapper);
}
```

## 实践步骤

### 步骤 1：配置分页插件

```java
package com.joint.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

### 步骤 2：基本分页查询

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public IPage<User> findPage(int page, int size) {
        // 创建分页对象
        Page<User> pageParam = new Page<>(page, size);
        // 执行分页查询
        return userMapper.selectPage(pageParam, null);
    }
}
```

返回结果：
```json
{
    "records": [...],    // 数据列表
    "total": 100,        // 总记录数
    "size": 10,          // 每页大小
    "current": 1,        // 当前页码
    "pages": 10          // 总页数
}
```

### 步骤 3：QueryWrapper 条件构造

```java
// 方式一：QueryWrapper（字符串字段名）
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.eq("status", 0)                    // status = 0
       .like("username", "admin")          // username LIKE '%admin%'
       .isNotNull("email")                 // email IS NOT NULL
       .orderByDesc("created_at");         // ORDER BY created_at DESC

// 方式二：LambdaQueryWrapper（方法引用，推荐）
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getStatus, 0)
       .like(User::getUsername, "admin")
       .isNotNull(User::getEmail)
       .orderByDesc(User::getCreatedAt);

// 执行查询
List<User> users = userMapper.selectList(wrapper);
```

### 步骤 4：常用条件方法

| 方法 | SQL | 示例 |
|------|-----|------|
| `eq` | `=` | `eq("status", 0)` |
| `ne` | `<>` | `ne("status", 1)` |
| `gt` | `>` | `gt("age", 18)` |
| `ge` | `>=` | `ge("age", 18)` |
| `lt` | `<` | `lt("age", 60)` |
| `le` | `<=` | `le("age", 60)` |
| `like` | `LIKE '%val%'` | `like("name", "张")` |
| `likeLeft` | `LIKE '%val'` | `likeLeft("name", "三")` |
| `likeRight` | `LIKE 'val%'` | `likeRight("name", "张")` |
| `isNull` | `IS NULL` | `isNull("email")` |
| `isNotNull` | `IS NOT NULL` | `isNotNull("email")` |
| `in` | `IN (...)` | `in("id", ids)` |
| `notIn` | `NOT IN (...)` | `notIn("id", ids)` |
| `between` | `BETWEEN ... AND ...` | `between("age", 18, 60)` |
| `orderByAsc` | `ORDER BY ... ASC` | `orderByAsc("sort")` |
| `orderByDesc` | `ORDER BY ... DESC` | `orderByDesc("created_at")` |

### 步骤 5：条件组合

```java
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

// AND 条件（默认）
wrapper.eq(User::getStatus, 0)
       .like(User::getUsername, "admin");
// WHERE status = 0 AND username LIKE '%admin%'

// OR 条件
wrapper.eq(User::getStatus, 0)
       .or()
       .eq(User::getStatus, 1);
// WHERE status = 0 OR status = 1

// 嵌套条件
wrapper.eq(User::getDeptId, deptId)
       .and(w -> w.like(User::getUsername, keyword)
                  .or()
                  .like(User::getNickName, keyword));
// WHERE dept_id = ? AND (username LIKE ? OR nick_name LIKE ?)
```

### 步骤 6：动态条件（条件不为空时才添加）

```java
public IPage<User> findAll(QueryUserDto query) {
    Page<User> page = new Page<>(query.getPage(), query.getSize());
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

    // 条件不为空时才添加
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
```

### 步骤 7：完整的查询 DTO

```java
package com.joint.modules.system.user.dto;

import lombok.Data;

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

### 步骤 8：Controller 层实现

```java
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    public IPage<User> list(QueryUserDto query) {
        return userService.findAll(query);
    }
}
```

### 步骤 9：自定义返回分页格式

如果需要与 Nexus 返回格式一致：

```java
package com.joint.common.response;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {

    private List<T> data;
    private Long total;
    private Long page;
    private Long size;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setData(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPage(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }
}
```

使用：
```java
@GetMapping("/list")
public PageResult<User> list(QueryUserDto query) {
    IPage<User> page = userService.findAll(query);
    return PageResult.of(page);
}
```

### 步骤 10：指定查询字段

```java
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.select(User::getId, User::getUsername, User::getNickName)  // 只查询指定字段
       .eq(User::getStatus, 0);
```

### 步骤 11：统计查询

```java
// 统计数量
Long count = userMapper.selectCount(wrapper);

// 聚合查询需要自定义 SQL
@Select("SELECT dept_id, COUNT(*) as count FROM sys_user GROUP BY dept_id")
List<Map<String, Object>> countByDept();
```

## 高级用法：自定义 SQL

当 Wrapper 无法满足需求时，可以写自定义 SQL：

```java
// UserMapper.java
@Mapper
public interface UserMapper extends BaseMapper<User> {

    // 使用注解
    @Select("SELECT u.*, d.name as dept_name FROM sys_user u " +
            "LEFT JOIN sys_dept d ON u.dept_id = d.id " +
            "WHERE u.deleted = 0")
    List<UserVO> selectUserWithDept();

    // 或使用 XML（更适合复杂 SQL）
    List<UserVO> selectUserWithRoles(@Param("userId") String userId);
}
```

```xml
<!-- resources/mapper/UserMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.joint.modules.system.user.mapper.UserMapper">

    <select id="selectUserWithRoles" resultType="com.joint.modules.system.user.vo.UserVO">
        SELECT u.*, r.name as role_name
        FROM sys_user u
        LEFT JOIN sys_user_role ur ON u.id = ur.user_id
        LEFT JOIN sys_role r ON ur.role_id = r.id
        WHERE u.id = #{userId} AND u.deleted = 0
    </select>

</mapper>
```

## 知识点总结

| 概念 | 说明 |
|------|------|
| Page | 分页参数对象 |
| IPage | 分页结果接口 |
| QueryWrapper | 条件构造器 |
| LambdaQueryWrapper | Lambda 条件构造器（推荐） |
| 动态条件 | 第一个参数为 boolean，控制是否添加条件 |

## 与 Nexus 对照总结

| Nexus (Prisma) | Joint (MyBatis-Plus) |
|----------------|---------------------|
| `skip: (page-1)*size` | `new Page<>(page, size)` |
| `take: size` | Page 自动处理 |
| `where: { contains }` | `like()` |
| `where: { equals }` | `eq()` |
| `orderBy: { desc }` | `orderByDesc()` |
| `Promise.all([...])` | `selectPage()` 自动查总数 |

## 练习任务

1. 配置分页插件
2. 实现用户列表分页查询
3. 添加模糊搜索和状态筛选
4. 使用自定义 SQL 实现关联查询
