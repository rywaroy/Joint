# Phase 5: 高级特性

## 学习目标

- 实现 Redis 缓存
- 实现文件上传
- 实现 AOP 操作日志
- 实现 Swagger API 文档

## 任务列表

| 任务 | 文档 | 核心知识点 |
|------|------|-----------|
| 5.1 | [Redis 缓存](01-redis-cache.md) | RedisTemplate、缓存注解 |
| 5.2 | [文件上传](02-file-upload.md) | MultipartFile、本地/OSS |
| 5.3 | [操作日志](03-oper-log.md) | AOP、自定义注解 |
| 5.4 | [Swagger 文档](04-swagger.md) | SpringDoc、接口文档 |

## Nexus 对照

| Nexus 模块 | Joint 实现 |
|-----------|-----------|
| RedisModule | Spring Data Redis |
| FileModule | MultipartFile + OSS SDK |
| @Log 装饰器 | @Log 注解 + AOP |
| Swagger | SpringDoc OpenAPI |

## 完成标准

- [ ] 实现 Redis 缓存
- [ ] 实现文件上传（本地/OSS）
- [ ] 实现操作日志记录
- [ ] 生成 Swagger API 文档
