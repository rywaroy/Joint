# 2.1 ORM 概念与选型

## 学习目标

- 理解什么是 ORM
- 了解 Java 主流 ORM 框架
- 理解 MyBatis-Plus 的优势

## 概念讲解

### 什么是 ORM？

ORM（Object-Relational Mapping，对象关系映射）是一种将数据库表映射为程序对象的技术。

**没有 ORM 时（JDBC）：**

```java
String sql = "SELECT * FROM user WHERE id = ?";
PreparedStatement stmt = connection.prepareStatement(sql);
stmt.setString(1, id);
ResultSet rs = stmt.executeQuery();

User user = new User();
if (rs.next()) {
    user.setId(rs.getString("id"));
    user.setUsername(rs.getString("username"));
    user.setEmail(rs.getString("email"));
    // ... 手动映射每个字段
}
```

**使用 ORM 后：**

```java
User user = userMapper.selectById(id);  // 一行搞定
```

### ORM 的核心价值

1. **减少样板代码**：自动处理 SQL 和对象映射
2. **类型安全**：编译时检查，减少运行时错误
3. **数据库无关性**：切换数据库只需改配置
4. **防止 SQL 注入**：参数化查询

### Java 主流 ORM 框架

| 框架 | 类型 | 特点 |
|------|------|------|
| **JPA/Hibernate** | 全自动 ORM | 标准规范，自动生成 SQL，学习曲线陡 |
| **MyBatis** | 半自动 ORM | SQL 写在 XML/注解中，灵活可控 |
| **MyBatis-Plus** | MyBatis 增强 | 简单 CRUD 零 SQL，保留 MyBatis 灵活性 |
| **JOOQ** | SQL DSL | 类型安全的 SQL 构建器 |
| **Spring Data JDBC** | 轻量 ORM | 简单直接，无复杂特性 |

### 与 Nexus (Prisma) 对比

| 特性 | Prisma | MyBatis-Plus |
|------|--------|--------------|
| Schema 定义 | `.prisma` 文件 | Java 实体类 + 注解 |
| 查询方式 | 链式 API | Mapper 接口 / Wrapper |
| 类型安全 | TypeScript 类型生成 | Java 泛型 |
| 关联查询 | `include` 选项 | 手动 Join 或分步查询 |
| 迁移 | `prisma migrate` | Flyway / 手动 |

Prisma:
```typescript
const user = await prisma.user.findUnique({
    where: { id },
    include: { roles: true }
});
```

MyBatis-Plus:
```java
User user = userMapper.selectById(id);
// 关联需要额外查询或使用 Join
```

## MyBatis-Plus 简介

MyBatis-Plus（简称 MP）是 MyBatis 的增强工具，在 MyBatis 基础上只做增强不做改变。

### 核心特性

1. **无侵入**：只做增强，不影响现有 MyBatis 代码
2. **内置 CRUD**：通用 Mapper、Service，零 SQL 实现 CRUD
3. **条件构造器**：QueryWrapper 链式构建查询条件
4. **分页插件**：内置分页，无需额外配置
5. **代码生成器**：一键生成 Entity、Mapper、Service、Controller

### 架构图

```
┌─────────────────────────────────────────┐
│           Your Application              │
├─────────────────────────────────────────┤
│  Service (IService<T>)                  │  <- MP 提供通用 Service
├─────────────────────────────────────────┤
│  Mapper (BaseMapper<T>)                 │  <- MP 提供通用 Mapper
├─────────────────────────────────────────┤
│  MyBatis-Plus                           │  <- 增强层
├─────────────────────────────────────────┤
│  MyBatis                                │  <- 原生框架
├─────────────────────────────────────────┤
│  JDBC                                   │  <- 数据库驱动
├─────────────────────────────────────────┤
│  Database (MySQL/PostgreSQL/...)        │
└─────────────────────────────────────────┘
```

### BaseMapper 提供的方法

```java
public interface BaseMapper<T> {
    // 插入
    int insert(T entity);

    // 删除
    int deleteById(Serializable id);
    int deleteByMap(Map<String, Object> columnMap);
    int deleteBatchIds(Collection<?> idList);

    // 更新
    int updateById(T entity);
    int update(T entity, Wrapper<T> updateWrapper);

    // 查询
    T selectById(Serializable id);
    List<T> selectBatchIds(Collection<?> idList);
    List<T> selectByMap(Map<String, Object> columnMap);
    List<T> selectList(Wrapper<T> queryWrapper);
    Long selectCount(Wrapper<T> queryWrapper);
    // ... 更多方法
}
```

## 为什么选择 MyBatis-Plus？

| 优势 | 说明 |
|------|------|
| **简单易学** | 30 分钟上手，文档完善 |
| **国内主流** | 大量企业使用，招聘市场认可 |
| **灵活性** | 简单用 MP，复杂写原生 SQL |
| **功能丰富** | 分页、逻辑删除、乐观锁、自动填充 |
| **活跃社区** | 持续更新，问题响应快 |

## 知识点总结

| 概念 | 说明 |
|------|------|
| ORM | 对象关系映射，简化数据库操作 |
| MyBatis | 半自动 ORM，SQL 可控 |
| MyBatis-Plus | MyBatis 增强，零 SQL CRUD |
| BaseMapper | 通用 Mapper 接口 |
| Wrapper | 条件构造器 |

## 下一步

接下来我们将实际配置 MyBatis-Plus 并实现 CRUD 操作。
