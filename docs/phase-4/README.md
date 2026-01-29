# Phase 4: 业务模块开发

## 学习目标

- 实现完整的 CRUD 模块
- 掌握 Controller-Service-Mapper 分层架构
- 复刻 Nexus 的系统管理模块

## 任务列表

| 任务 | 文档 | 核心知识点 |
|------|------|-----------|
| 4.1 | [模块结构规范](01-module-structure.md) | 分层架构、命名规范 |
| 4.2 | [用户模块](02-user-module.md) | 完整 CRUD、DTO/VO 转换 |
| 4.3 | [角色模块](03-role-module.md) | 多对多关联、权限分配 |
| 4.4 | [菜单模块](04-menu-module.md) | 树形结构、递归查询 |
| 4.5 | [部门模块](05-dept-module.md) | 层级结构、数据权限 |
| 4.6 | [岗位模块](06-post-module.md) | 简单 CRUD 练习 |

## Nexus 模块对照

| Nexus 模块 | 路径 | 功能 |
|-----------|------|------|
| user | `/api/system/user` | 用户 CRUD、分配角色 |
| role | `/api/system/role` | 角色 CRUD、分配菜单权限 |
| menu | `/api/system/menu` | 菜单树 CRUD |
| dept | `/api/system/dept` | 部门树 CRUD |
| post | `/api/system/post` | 岗位 CRUD |

## 通用模块结构

```
modules/system/user/
├── entity/
│   └── User.java              # 实体类
├── mapper/
│   └── UserMapper.java        # Mapper 接口
├── dto/
│   ├── CreateUserDto.java     # 创建 DTO
│   ├── UpdateUserDto.java     # 更新 DTO
│   └── QueryUserDto.java      # 查询 DTO
├── vo/
│   └── UserVo.java            # 返回 VO
├── UserService.java           # Service 接口
├── UserServiceImpl.java       # Service 实现
└── UserController.java        # Controller
```

## 完成标准

- [ ] 理解三层架构设计
- [ ] 实现用户模块完整 CRUD
- [ ] 实现角色-菜单权限分配
- [ ] 实现菜单树形结构
- [ ] 实现部门层级查询
