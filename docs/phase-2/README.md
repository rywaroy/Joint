# Phase 2: 数据库集成

## 学习目标

- 理解 ORM 概念和 JPA/MyBatis 的区别
- 掌握 MyBatis-Plus 的使用
- 学习数据库迁移管理
- 实现实体类设计

## 任务列表

| 任务 | 文档 | 核心知识点 |
|------|------|-----------|
| 2.1 | [ORM 概念与选型](01-orm-intro.md) | JPA vs MyBatis、MyBatis-Plus |
| 2.2 | [MyBatis-Plus 入门](02-mybatis-plus.md) | BaseMapper、Service、CRUD |
| 2.3 | [实体类设计](03-entity-design.md) | 注解、关联关系、审计字段 |
| 2.4 | [分页与查询](04-pagination.md) | Page、QueryWrapper、条件构造 |

## Nexus 对照

| Nexus (Prisma) | Joint (MyBatis-Plus) |
|----------------|---------------------|
| `schema.prisma` | `@TableName` 实体类 |
| `prisma.user.findMany()` | `userMapper.selectList()` |
| `prisma.user.create()` | `userMapper.insert()` |
| `where: { username }` | `QueryWrapper` |
| `include: { roles: true }` | `@TableField` + 关联查询 |
| `prisma db push` | Flyway / 手动建表 |

## 技术选型说明

### JPA vs MyBatis

| 特性 | JPA (Hibernate) | MyBatis |
|------|-----------------|---------|
| 学习曲线 | 较陡 | 平缓 |
| SQL 控制 | 自动生成 | 手写/半自动 |
| 复杂查询 | 需要学 JPQL/Criteria | 直接写 SQL |
| 国内使用 | 较少 | 主流 |
| 适合场景 | 简单 CRUD | 复杂业务 |

**本项目选择 MyBatis-Plus**：
- 国内企业主流选择
- 学习资源丰富
- 简单 CRUD 零 SQL，复杂查询可写原生 SQL
- 内置分页、逻辑删除、自动填充等功能

## 完成标准

- [ ] 理解 ORM 和 MyBatis-Plus 核心概念
- [ ] 能使用 BaseMapper 完成 CRUD
- [ ] 掌握实体类注解的使用
- [ ] 能使用 QueryWrapper 构建复杂查询
- [ ] 实现分页查询功能
