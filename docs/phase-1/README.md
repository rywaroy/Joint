# Phase 1: 项目搭建与 Spring Boot 基础

## 学习目标

- 理解 Spring Boot 项目结构
- 掌握 Maven 依赖管理
- 学习 Spring IoC 和 DI 概念
- 实现统一响应格式和异常处理
- 掌握参数校验机制

## 任务列表

| 任务 | 文档 | 核心知识点 |
|------|------|-----------|
| 1.1 | [项目初始化](01-project-init.md) | Spring Initializr、Maven、项目结构 |
| 1.2 | [Spring Boot 核心概念](02-spring-core.md) | IoC、DI、Bean、注解 |
| 1.3 | [第一个 Controller](03-first-controller.md) | @RestController、@RequestMapping |
| 1.4 | [统一响应格式](04-response-format.md) | ResponseBodyAdvice、泛型响应类 |
| 1.5 | [全局异常处理](05-exception-handler.md) | @ControllerAdvice、自定义异常 |
| 1.6 | [参数校验](06-validation.md) | @Valid、Hibernate Validator |

## Nexus 对照

| Nexus (NestJS) | Joint (Spring Boot) |
|----------------|---------------------|
| `main.ts` | `JointApplication.java` |
| `app.module.ts` | `@SpringBootApplication` |
| `@Controller()` | `@RestController` |
| `@Get()/@Post()` | `@GetMapping/@PostMapping` |
| `TransformReturnInterceptor` | `ResponseBodyAdvice` |
| `HttpExceptionFilter` | `@ControllerAdvice` |
| `ValidationPipe` | `@Valid` + `MethodArgumentNotValidException` |

## 完成标准

- [ ] 能启动 Spring Boot 项目
- [ ] 理解 Bean 生命周期和依赖注入
- [ ] 所有接口返回统一格式 `{ code, message, data }`
- [ ] 异常能被统一捕获并返回友好信息
- [ ] 参数校验失败能返回具体错误信息
