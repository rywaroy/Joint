# Phase 5 Advanced Features Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Complete phase 5 for Joint by adding Redis-backed cache infrastructure, file upload, operation logs, and Swagger documentation.

**Architecture:** Keep the existing Spring Boot layering and add cross-cutting features as focused modules. Redis will back cache annotations and JWT blacklist checks, file upload will use a storage strategy abstraction with local storage as the default, operation logs will be recorded through an annotation-driven aspect into `sys_oper_log`, and SpringDoc will expose authenticated API documentation without changing existing response wrapping.

**Tech Stack:** Spring Boot 4, Spring Security 7, Spring Cache, Spring Data Redis, SpringDoc OpenAPI, MyBatis-Plus, Lombok, JUnit 5, Mockito, MockMvc

### Task 1: Add test coverage for phase-5 building blocks

**Files:**
- Create: `joint/src/test/java/org/joint/common/security/TokenBlacklistServiceTest.java`
- Create: `joint/src/test/java/org/joint/modules/file/FileServiceTest.java`
- Create: `joint/src/test/java/org/joint/common/aspect/LogAspectTest.java`
- Create: `joint/src/test/java/org/joint/config/SwaggerConfigTest.java`
- Modify: `joint/src/test/java/org/joint/auth/JwtAuthenticationFilterTest.java`

**Step 1: Write the failing test**

Add tests that verify:
- blacklisting a token stores a prefixed Redis key with the remaining TTL and filter authentication is skipped for blacklisted tokens
- file upload rejects empty files and delegates storage through the selected strategy for single and multiple uploads
- the log aspect records success and failure metadata for methods annotated with `@Log`
- the OpenAPI bean exposes the expected title and Bearer security scheme

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=TokenBlacklistServiceTest,FileServiceTest,LogAspectTest,SwaggerConfigTest,JwtAuthenticationFilterTest test`
Expected: FAIL because the phase-5 services, aspect, and Swagger config do not exist yet.

**Step 3: Write minimal implementation**

Introduce only the minimum production code needed to make these tests meaningful, keeping dependencies mockable and avoiding runtime-only branches in unit tests.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=TokenBlacklistServiceTest,FileServiceTest,LogAspectTest,SwaggerConfigTest,JwtAuthenticationFilterTest test`
Expected: PASS.

### Task 2: Wire Redis cache and JWT blacklist support

**Files:**
- Modify: `joint/pom.xml`
- Modify: `joint/src/main/resources/application.properties`
- Modify: `joint/src/main/java/org/joint/JointApplication.java`
- Modify: `joint/src/main/java/org/joint/modules/auth/AuthController.java`
- Modify: `joint/src/main/java/org/joint/modules/auth/AuthService.java`
- Modify: `joint/src/main/java/org/joint/common/security/JwtAuthenticationFilter.java`
- Modify: `joint/src/main/java/org/joint/modules/system/user/UserService.java`
- Create: `joint/src/main/java/org/joint/config/RedisConfig.java`
- Create: `joint/src/main/java/org/joint/config/CacheConfig.java`
- Create: `joint/src/main/java/org/joint/common/utils/RedisUtils.java`
- Create: `joint/src/main/java/org/joint/common/security/TokenBlacklistService.java`

**Step 1: Write the failing test**

Use the Task 1 Redis and filter tests as the red phase.

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=TokenBlacklistServiceTest,JwtAuthenticationFilterTest test`
Expected: FAIL because logout is a no-op and the filter always accepts valid tokens.

**Step 3: Write minimal implementation**

Add Redis and cache dependencies, enable async and caching on the application, provide Redis serializers and a cache manager, implement `RedisUtils`, add `TokenBlacklistService`, make `/auth/logout` blacklist the presented token, have the JWT filter reject blacklisted tokens, and add cache annotations to user read/write paths.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=TokenBlacklistServiceTest,JwtAuthenticationFilterTest,AuthServiceTest,UserServiceTest test`
Expected: PASS.

### Task 3: Implement the file upload module

**Files:**
- Modify: `joint/src/main/resources/application.properties`
- Modify: `joint/src/main/java/org/joint/config/WebMvcConfig.java`
- Create: `joint/src/main/java/org/joint/config/FileStorageProperties.java`
- Create: `joint/src/main/java/org/joint/config/OssProperties.java`
- Create: `joint/src/main/java/org/joint/config/StorageConfig.java`
- Create: `joint/src/main/java/org/joint/modules/file/FileController.java`
- Create: `joint/src/main/java/org/joint/modules/file/FileService.java`
- Create: `joint/src/main/java/org/joint/modules/file/storage/StorageStrategy.java`
- Create: `joint/src/main/java/org/joint/modules/file/storage/FileInfo.java`
- Create: `joint/src/main/java/org/joint/modules/file/storage/LocalStorage.java`
- Create: `joint/src/main/java/org/joint/modules/file/storage/OssStorage.java`

**Step 1: Write the failing test**

Use the Task 1 file upload tests as the red phase.

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=FileServiceTest test`
Expected: FAIL because there is no file module or storage strategy yet.

**Step 3: Write minimal implementation**

Add storage properties, register a default local storage strategy with optional OSS switching, expose static resource mapping for local uploads, and create upload endpoints for single and multiple file submissions.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=FileServiceTest test`
Expected: PASS.

### Task 4: Implement operation log persistence and query

**Files:**
- Modify: `joint/src/main/resources/sql/schema.sql`
- Modify: `joint/src/main/java/org/joint/modules/system/user/UserController.java`
- Create: `joint/src/main/java/org/joint/common/annotation/Log.java`
- Create: `joint/src/main/java/org/joint/common/enums/BusinessType.java`
- Create: `joint/src/main/java/org/joint/common/aspect/LogAspect.java`
- Create: `joint/src/main/java/org/joint/common/utils/IpUtils.java`
- Create: `joint/src/main/java/org/joint/modules/system/operlog/OperLogController.java`
- Create: `joint/src/main/java/org/joint/modules/system/operlog/OperLogService.java`
- Create: `joint/src/main/java/org/joint/modules/system/operlog/entity/OperLog.java`
- Create: `joint/src/main/java/org/joint/modules/system/operlog/mapper/OperLogMapper.java`
- Create: `joint/src/main/java/org/joint/modules/system/operlog/dto/QueryOperLogDto.java`
- Create: `joint/src/main/java/org/joint/modules/system/operlog/vo/OperLogVo.java`

**Step 1: Write the failing test**

Use the Task 1 log-aspect test as the red phase.

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=LogAspectTest test`
Expected: FAIL because the annotation, aspect, and mapper do not exist.

**Step 3: Write minimal implementation**

Add the `sys_oper_log` table, define the annotation and business type enum, persist logs asynchronously from the aspect, annotate mutating endpoints, and expose a paged operation log list endpoint.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=LogAspectTest,UserServiceTest test`
Expected: PASS.

### Task 5: Add Swagger/OpenAPI support

**Files:**
- Modify: `joint/pom.xml`
- Modify: `joint/src/main/resources/application.properties`
- Modify: `joint/src/main/java/org/joint/config/SecurityConfig.java`
- Modify: `joint/src/main/java/org/joint/common/response/Result.java`
- Modify: `joint/src/main/java/org/joint/common/response/PageResult.java`
- Modify: `joint/src/main/java/org/joint/modules/auth/AuthController.java`
- Modify: `joint/src/main/java/org/joint/modules/auth/dto/LoginDto.java`
- Modify: `joint/src/main/java/org/joint/modules/auth/vo/LoginVo.java`
- Modify: `joint/src/main/java/org/joint/modules/system/user/UserController.java`
- Modify: `joint/src/main/java/org/joint/modules/system/user/dto/CreateUserDto.java`
- Modify: `joint/src/main/java/org/joint/modules/system/user/dto/UpdateUserDto.java`
- Modify: `joint/src/main/java/org/joint/modules/system/user/dto/QueryUserDto.java`
- Modify: `joint/src/main/java/org/joint/modules/system/user/dto/ResetPasswordDto.java`
- Modify: `joint/src/main/java/org/joint/modules/system/user/dto/UpdateUserStatusDto.java`
- Modify: `joint/src/main/java/org/joint/modules/system/user/vo/UserVo.java`
- Modify: `joint/src/main/java/org/joint/modules/system/user/vo/UserDetailVo.java`
- Modify: `joint/src/main/java/org/joint/modules/file/FileController.java`
- Modify: `joint/src/main/java/org/joint/modules/file/storage/FileInfo.java`
- Modify: `joint/src/main/java/org/joint/modules/system/operlog/OperLogController.java`
- Modify: `joint/src/main/java/org/joint/modules/system/operlog/dto/QueryOperLogDto.java`
- Modify: `joint/src/main/java/org/joint/modules/system/operlog/vo/OperLogVo.java`
- Create: `joint/src/main/java/org/joint/config/SwaggerConfig.java`

**Step 1: Write the failing test**

Use the Task 1 Swagger config test as the red phase.

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=SwaggerConfigTest test`
Expected: FAIL because SpringDoc is not configured.

**Step 3: Write minimal implementation**

Add SpringDoc dependencies, publish `/api-docs` and `/swagger` endpoints, whitelist them in security, create the OpenAPI bean, and add concise `@Tag`, `@Operation`, and `@Schema` annotations to the new or updated API surface.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=SwaggerConfigTest test`
Expected: PASS.

### Task 6: Final verification

**Files:**
- Test: `joint/src/test/java/org/joint/**/*.java`

**Step 1: Run focused phase-5 verification**

Run: `./mvnw -q -Dtest=TokenBlacklistServiceTest,FileServiceTest,LogAspectTest,SwaggerConfigTest,JwtAuthenticationFilterTest,AuthServiceTest,UserServiceTest test`
Expected: PASS.

**Step 2: Run full test suite**

Run: `./mvnw -q test`
Expected: PASS.

**Step 3: Review scope**

Confirm the implementation stays within phase-5 scope:
- no defensive fallback code paths unrelated to the documented features
- no schema changes beyond operation log support
- no unrelated auth or module redesign
