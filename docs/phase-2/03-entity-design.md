# 2.3 实体类设计

## 学习目标

- 掌握 MyBatis-Plus 实体类注解
- 理解主键策略
- 设计与 Nexus 对应的数据模型

## 与 Nexus 数据模型对照

Nexus 使用 Prisma Schema：

```prisma
model User {
  id        String     @id @default(uuid())
  username  String     @unique
  password  String
  nickName  String
  email     String?    @unique
  phone     String?    @unique
  avatar    String?
  status    Int        @default(0)
  deptId    String?
  remark    String?
  createdAt DateTime   @default(now())
  updatedAt DateTime   @updatedAt

  dept      Dept?      @relation(fields: [deptId], references: [id])
  roles     UserRole[]
}
```

Joint 使用 Java 实体类 + 注解。

## 实体类注解详解

### @TableName - 表名映射

```java
@TableName("sys_user")  // 映射到 sys_user 表
public class User { }

// 如果表名与类名一致（驼峰转下划线），可省略
// User -> user 表
```

### @TableId - 主键配置

```java
public class User {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
}
```

主键策略：

| IdType | 说明 |
|--------|------|
| `AUTO` | 数据库自增 |
| `ASSIGN_ID` | 雪花算法（默认，推荐） |
| `ASSIGN_UUID` | UUID（不带连字符） |
| `INPUT` | 手动输入 |
| `NONE` | 无策略，需手动设置 |

**雪花算法**：生成 19 位数字 ID，趋势递增，分布式唯一。

### @TableField - 字段配置

```java
public class User {
    // 指定数据库列名（通常不需要，会自动驼峰转下划线）
    @TableField("nick_name")
    private String nickName;

    // 非数据库字段
    @TableField(exist = false)
    private List<Role> roles;

    // 自动填充
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // 查询时不返回此字段
    @TableField(select = false)
    private String password;

    // 更新时忽略 null 值（配合 updateStrategy）
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String email;
}
```

填充策略：

| FieldFill | 说明 |
|-----------|------|
| `DEFAULT` | 不填充 |
| `INSERT` | 插入时填充 |
| `UPDATE` | 更新时填充 |
| `INSERT_UPDATE` | 插入和更新时填充 |

### @TableLogic - 逻辑删除

```java
public class User {
    @TableLogic
    private Integer deleted;  // 0-未删除, 1-已删除
}
```

配置逻辑删除值：

```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-value: 1
      logic-not-delete-value: 0
```

启用后：
- `deleteById(id)` 实际执行 `UPDATE ... SET deleted = 1 WHERE id = ?`
- `selectList()` 自动添加 `WHERE deleted = 0`

### @Version - 乐观锁

```java
public class User {
    @Version
    private Integer version;
}
```

更新时自动检查版本：
```sql
UPDATE user SET name = ?, version = version + 1
WHERE id = ? AND version = ?
```

需要配置插件：
```java
@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
    return interceptor;
}
```

## 实践：设计 Joint 数据模型

### 用户表 (sys_user)

```java
package com.joint.modules.system.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String username;

    @TableField(select = false)  // 查询时不返回密码
    private String password;

    private String nickName;

    private String email;

    private String phone;

    private String avatar;

    private Integer status;

    private String deptId;

    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 非数据库字段：关联的角色列表
    @TableField(exist = false)
    private List<Role> roles;

    // 非数据库字段：所属部门
    @TableField(exist = false)
    private Dept dept;
}
```

### 角色表 (sys_role)

```java
package com.joint.modules.system.role.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class Role {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String name;

    private String code;

    private Integer sort;

    private Integer status;

    private Boolean isSuper;  // 是否超级管理员

    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<Menu> menus;
}
```

### 菜单表 (sys_menu)

```java
package com.joint.modules.system.menu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class Menu {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String parentId;

    private String name;

    private String path;

    private String component;

    private String icon;

    private Integer type;  // 0-目录 1-菜单 2-按钮

    private String authCode;  // 权限标识

    private Integer sort;

    private Integer status;

    private Boolean hidden;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<Menu> children;
}
```

### 用户-角色关联表 (sys_user_role)

```java
package com.joint.modules.system.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_user_role")
public class UserRole {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String userId;

    private String roleId;
}
```

### 角色-菜单关联表 (sys_role_menu)

```java
package com.joint.modules.system.role.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_role_menu")
public class RoleMenu {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String roleId;

    private String menuId;
}
```

### 部门表 (sys_dept)

```java
package com.joint.modules.system.dept.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_dept")
public class Dept {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String parentId;

    private String name;

    private Integer sort;

    private String leader;

    private String phone;

    private String email;

    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<Dept> children;
}
```

## 建表 SQL

```sql
-- 角色表
CREATE TABLE sys_role (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 0,
    is_super TINYINT DEFAULT 0,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 菜单表
CREATE TABLE sys_menu (
    id VARCHAR(32) PRIMARY KEY,
    parent_id VARCHAR(32),
    name VARCHAR(50) NOT NULL,
    path VARCHAR(200),
    component VARCHAR(200),
    icon VARCHAR(100),
    type TINYINT DEFAULT 1 COMMENT '0-目录 1-菜单 2-按钮',
    auth_code VARCHAR(100) COMMENT '权限标识',
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 0,
    hidden TINYINT DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 用户-角色关联表
CREATE TABLE sys_user_role (
    id VARCHAR(32) PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL,
    role_id VARCHAR(32) NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
);

-- 角色-菜单关联表
CREATE TABLE sys_role_menu (
    id VARCHAR(32) PRIMARY KEY,
    role_id VARCHAR(32) NOT NULL,
    menu_id VARCHAR(32) NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id)
);

-- 部门表
CREATE TABLE sys_dept (
    id VARCHAR(32) PRIMARY KEY,
    parent_id VARCHAR(32),
    name VARCHAR(50) NOT NULL,
    sort INT DEFAULT 0,
    leader VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status TINYINT DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 知识点总结

| 注解 | 说明 |
|------|------|
| @TableName | 指定表名 |
| @TableId | 主键配置 |
| @TableField | 字段配置 |
| @TableLogic | 逻辑删除 |
| @Version | 乐观锁 |

## 练习任务

1. 创建所有实体类
2. 执行建表 SQL
3. 为每个实体创建对应的 Mapper
