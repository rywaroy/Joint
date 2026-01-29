# 2.2 MyBatis-Plus 入门

## 学习目标

- 配置 MyBatis-Plus
- 使用 BaseMapper 实现 CRUD
- 理解 Service 层封装

## 实践步骤

### 步骤 1：添加依赖

```xml
<!-- pom.xml -->
<dependencies>
    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        <version>3.5.5</version>
    </dependency>

    <!-- MySQL 驱动 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- 或 PostgreSQL 驱动 -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### 步骤 2：配置数据源

```yaml
# application.yml
spring:
  datasource:
    # MySQL
    url: jdbc:mysql://localhost:3306/joint?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

    # 或 PostgreSQL
    # url: jdbc:postgresql://localhost:5432/joint
    # username: postgres
    # password: your_password
    # driver-class-name: org.postgresql.Driver

# MyBatis-Plus 配置
mybatis-plus:
  configuration:
    # 开启驼峰命名转换（user_name -> userName）
    map-underscore-to-camel-case: true
    # 开发时打印 SQL
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      # 主键策略：雪花算法
      id-type: ASSIGN_ID
      # 逻辑删除字段
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 步骤 3：创建数据库表

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS joint DEFAULT CHARACTER SET utf8mb4;

USE joint;

-- 用户表
CREATE TABLE sys_user (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nick_name VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 0 COMMENT '状态 0-正常 1-禁用',
    dept_id VARCHAR(32) COMMENT '部门ID',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_status (status)
) COMMENT '用户表';
```

### 步骤 4：创建实体类

```java
package com.joint.modules.system.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")  // 指定表名
public class User {

    @TableId(type = IdType.ASSIGN_ID)  // 主键，雪花算法生成
    private String id;

    private String username;

    private String password;

    private String nickName;  // 自动映射 nick_name

    private String email;

    private String phone;

    private String avatar;

    private Integer status;

    private String deptId;

    private String remark;

    @TableLogic  // 逻辑删除字段
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)  // 插入和更新时自动填充
    private LocalDateTime updatedAt;
}
```

### 步骤 5：创建 Mapper 接口

```java
package com.joint.modules.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joint.modules.system.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper  // 标记为 MyBatis Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承 BaseMapper 后，已经拥有了完整的 CRUD 方法
    // 如需自定义 SQL，可以在这里添加方法
}
```

### 步骤 6：配置 Mapper 扫描

```java
package com.joint;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.joint.modules.**.mapper")  // 扫描 Mapper 接口
public class JointApplication {
    public static void main(String[] args) {
        SpringApplication.run(JointApplication.class, args);
    }
}
```

### 步骤 7：使用 Mapper 进行 CRUD

```java
package com.joint.modules.system.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.joint.modules.system.user.entity.User;
import com.joint.modules.system.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    // 根据 ID 查询
    public User findById(String id) {
        return userMapper.selectById(id);
    }

    // 查询所有
    public List<User> findAll() {
        return userMapper.selectList(null);  // null 表示无条件
    }

    // 根据用户名查询
    public User findByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return userMapper.selectOne(wrapper);
    }

    // 创建用户
    public User create(User user) {
        userMapper.insert(user);
        return user;  // 插入后 id 会自动回填
    }

    // 更新用户
    public User update(User user) {
        userMapper.updateById(user);
        return user;
    }

    // 删除用户（逻辑删除）
    public void delete(String id) {
        userMapper.deleteById(id);  // 实际执行 UPDATE ... SET deleted = 1
    }

    // 批量删除
    public void deleteBatch(List<String> ids) {
        userMapper.deleteBatchIds(ids);
    }
}
```

### 步骤 8：配置自动填充

```java
package com.joint.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 插入时自动填充
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时自动填充
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
```

### 步骤 9：使用 IService（可选）

MyBatis-Plus 提供了 IService 接口，封装了更多方法：

```java
package com.joint.modules.system.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.joint.modules.system.user.entity.User;

public interface IUserService extends IService<User> {
    // 自定义方法
    User findByUsername(String username);
}
```

```java
package com.joint.modules.system.user;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.joint.modules.system.user.entity.User;
import com.joint.modules.system.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public User findByUsername(String username) {
        return lambdaQuery()
                .eq(User::getUsername, username)
                .one();
    }
}
```

IService 提供的常用方法：

```java
// 保存
boolean save(T entity);
boolean saveBatch(Collection<T> entityList);

// 删除
boolean removeById(Serializable id);
boolean removeByIds(Collection<?> idList);

// 更新
boolean updateById(T entity);

// 查询
T getById(Serializable id);
List<T> list();
List<T> listByIds(Collection<?> idList);
long count();

// Lambda 查询（推荐）
lambdaQuery().eq(User::getStatus, 0).list();
lambdaUpdate().set(User::getStatus, 1).eq(User::getId, id).update();
```

## 与 Nexus (Prisma) 对照

| Prisma | MyBatis-Plus |
|--------|--------------|
| `prisma.user.findUnique({ where: { id } })` | `userMapper.selectById(id)` |
| `prisma.user.findMany()` | `userMapper.selectList(null)` |
| `prisma.user.findFirst({ where: { username } })` | `userMapper.selectOne(wrapper)` |
| `prisma.user.create({ data })` | `userMapper.insert(entity)` |
| `prisma.user.update({ where, data })` | `userMapper.updateById(entity)` |
| `prisma.user.delete({ where: { id } })` | `userMapper.deleteById(id)` |

## 目录结构

```
src/main/java/com/joint/
├── config/
│   └── MyMetaObjectHandler.java
└── modules/
    └── system/
        └── user/
            ├── entity/
            │   └── User.java
            ├── mapper/
            │   └── UserMapper.java
            ├── UserService.java
            └── UserController.java
```

## 知识点总结

| 概念 | 说明 |
|------|------|
| @TableName | 指定实体对应的表名 |
| @TableId | 指定主键字段 |
| @TableField | 字段配置（填充、存在等） |
| @TableLogic | 逻辑删除字段 |
| BaseMapper | 通用 Mapper 接口 |
| IService | 通用 Service 接口 |

## 练习任务

1. 配置 MyBatis-Plus 和数据源
2. 创建 sys_user 表和 User 实体类
3. 实现 UserMapper 和 UserService
4. 测试 CRUD 操作
