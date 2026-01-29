# Joint 学习路线

> 本项目是 Nexus (NestJS) 的 Java 版本，使用 Spring Boot 3.x 构建，对接 vue-vben-admin 前端。

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | Spring Boot 3.x |
| 语言 | Java 17+ |
| 构建工具 | Maven |
| ORM | MyBatis-Plus / JPA |
| 数据库 | MySQL / PostgreSQL |
| 缓存 | Redis |
| 认证 | Spring Security + JWT |
| 文档 | Swagger (SpringDoc) |

## 学习阶段

| 阶段 | 主题 | 预计任务数 | 状态 |
|------|------|-----------|------|
| [Phase 1](phase-1/README.md) | 项目搭建与 Spring Boot 基础 | 5 | 待开始 |
| [Phase 2](phase-2/README.md) | 数据库集成 | 4 | 待开始 |
| [Phase 3](phase-3/README.md) | 认证授权 | 5 | 待开始 |
| [Phase 4](phase-4/README.md) | 业务模块开发 | 6 | 待开始 |
| [Phase 5](phase-5/README.md) | 高级特性 | 4 | 待开始 |
| [Phase 6](phase-6/README.md) | 测试与部署 | 3 | 待开始 |

## 与 Nexus 功能对照

| Nexus 模块 | Joint 对应 | 所属阶段 |
|-----------|-----------|---------|
| AuthGuard | Spring Security Filter | Phase 3 |
| PermissionGuard | 自定义权限拦截器 | Phase 3 |
| HttpExceptionFilter | @ControllerAdvice | Phase 1 |
| TransformReturnInterceptor | ResponseBodyAdvice | Phase 1 |
| ValidationPipe | @Valid + Validator | Phase 1 |
| PrismaService | MyBatis-Plus / JPA | Phase 2 |
| auth 模块 | AuthController/Service | Phase 3 |
| user 模块 | UserController/Service | Phase 4 |
| role 模块 | RoleController/Service | Phase 4 |
| menu 模块 | MenuController/Service | Phase 4 |
| dept 模块 | DeptController/Service | Phase 4 |
| post 模块 | PostController/Service | Phase 4 |
| oper-log 模块 | OperLogController/Service | Phase 5 |
| file 模块 | FileController/Service | Phase 5 |
| redis 模块 | RedisTemplate | Phase 5 |

## 学习方法

1. **先读文档**：每个任务开始前先读对应的学习文档
2. **动手实践**：跟着文档步骤编写代码
3. **对照 Nexus**：理解 NestJS 与 Spring Boot 的概念映射
4. **验证测试**：每个功能完成后进行测试验证

## 目录结构预览

```
Joint/
├── src/main/java/com/joint/
│   ├── JointApplication.java        # 启动类
│   ├── config/                      # 配置类
│   ├── common/                      # 公共组件
│   │   ├── exception/               # 异常处理
│   │   ├── response/                # 统一响应
│   │   ├── annotation/              # 自定义注解
│   │   └── utils/                   # 工具类
│   └── modules/                     # 业务模块
│       ├── auth/                    # 认证模块
│       └── system/                  # 系统管理
│           ├── user/
│           ├── role/
│           ├── menu/
│           ├── dept/
│           └── post/
├── src/main/resources/
│   ├── application.yml              # 主配置
│   ├── application-dev.yml          # 开发环境
│   └── mapper/                      # MyBatis XML
└── pom.xml                          # Maven 配置
```
