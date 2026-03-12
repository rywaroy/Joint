# Phase 4 System Modules Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Complete phase 4 for Joint by upgrading the system module to layered CRUD modules for user, role, menu, dept, and post management.

**Architecture:** Keep the existing Spring Boot + MyBatis-Plus structure and standardize every phase-4 module around `Controller -> Service -> Mapper` with DTO/VO boundaries. Reuse current auth and permission infrastructure, keep tree assembly inside services, and manage many-to-many relations explicitly through join-table entities and mappers.

**Tech Stack:** Spring Boot 4, Spring Security 7, MyBatis-Plus, Lombok, JUnit 5, Mockito, MockMvc

### Task 1: Prepare shared phase-4 module foundations

**Files:**
- Modify: `joint/src/main/java/org/joint/common/response/PageResult.java`
- Modify: `joint/src/main/resources/sql/schema.sql`
- Create: `joint/src/main/java/org/joint/modules/system/post/entity/Post.java`
- Create: `joint/src/main/java/org/joint/modules/system/post/entity/UserPost.java`
- Create: `joint/src/main/java/org/joint/modules/system/post/mapper/PostMapper.java`
- Create: `joint/src/main/java/org/joint/modules/system/post/mapper/UserPostMapper.java`

**Step 1: Write the failing test**

Create `joint/src/test/java/org/joint/modules/system/support/PageResultTest.java` with tests that verify:
- page conversion keeps `data`, `total`, `page`, and `size`
- mapped page conversion can transform records from entities to VOs

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=PageResultTest test`
Expected: FAIL because `PageResult` only supports direct `IPage<T>` conversion.

**Step 3: Write minimal implementation**

Add overloaded `PageResult.of(...)` helpers for `(total, data)` and `(IPage<S>, mapper)` so phase-4 services can return VO pages without duplicating paging code. Extend the SQL schema with `sys_post` and `sys_user_post`, and add the missing post-side entities and mappers.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=PageResultTest test`
Expected: PASS.

### Task 2: Upgrade the user module to full phase-4 CRUD

**Files:**
- Modify: `joint/src/main/java/org/joint/modules/system/user/UserController.java`
- Modify: `joint/src/main/java/org/joint/modules/system/user/UserService.java`
- Modify: `joint/src/main/java/org/joint/modules/system/user/dto/CreateUserDto.java`
- Modify: `joint/src/main/java/org/joint/modules/system/user/dto/QueryUserDto.java`
- Create: `joint/src/main/java/org/joint/modules/system/user/dto/UpdateUserDto.java`
- Create: `joint/src/main/java/org/joint/modules/system/user/dto/UpdateUserStatusDto.java`
- Create: `joint/src/main/java/org/joint/modules/system/user/dto/ResetPasswordDto.java`
- Create: `joint/src/main/java/org/joint/modules/system/user/vo/UserVo.java`
- Create: `joint/src/main/java/org/joint/modules/system/user/vo/UserDetailVo.java`

**Step 1: Write the failing test**

Create `joint/src/test/java/org/joint/modules/system/user/UserServiceTest.java` with tests that verify:
- listing users returns paged `UserVo` data with department names
- user detail loads role IDs, post IDs, and department info
- creating a user encodes the password and stores role/post relations
- deleting `admin` is rejected
- updating status and resetting password persist the expected fields

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=UserServiceTest test`
Expected: FAIL because the current service returns entities only and has no relation-loading or phase-4 commands.

**Step 3: Write minimal implementation**

Refactor the user service to expose phase-4 CRUD methods returning VO objects, add update/status/reset DTOs, load `sys_user_role` and `sys_user_post` relations, and keep `/system/user/profile` working with the new detail model.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=UserServiceTest test`
Expected: PASS.

### Task 3: Add the role module with menu-permission assignment

**Files:**
- Modify: `joint/src/main/java/org/joint/modules/system/role/entity/Role.java`
- Modify: `joint/src/main/java/org/joint/modules/system/role/entity/RoleMenu.java`
- Create: `joint/src/main/java/org/joint/modules/system/role/RoleController.java`
- Create: `joint/src/main/java/org/joint/modules/system/role/RoleService.java`
- Create: `joint/src/main/java/org/joint/modules/system/role/dto/CreateRoleDto.java`
- Create: `joint/src/main/java/org/joint/modules/system/role/dto/UpdateRoleDto.java`
- Create: `joint/src/main/java/org/joint/modules/system/role/dto/QueryRoleDto.java`
- Create: `joint/src/main/java/org/joint/modules/system/role/vo/RoleVo.java`

**Step 1: Write the failing test**

Create `joint/src/test/java/org/joint/modules/system/role/RoleServiceTest.java` with tests that verify:
- creating a role stores menu assignments and marks `"*"` permissions as super role
- builtin roles cannot change code/name semantics or be deleted
- roles assigned to users cannot be deleted
- enabled roles can be returned as option data

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=RoleServiceTest test`
Expected: FAIL because no role service or controller exists yet.

**Step 3: Write minimal implementation**

Create role DTO/VO/service/controller classes, align the entity to the actual schema fields (`code`, `sort`, `isSuper`), and manage `sys_role_menu` with delete-then-insert semantics.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=RoleServiceTest test`
Expected: PASS.

### Task 4: Add menu tree CRUD and user-scoped menu loading

**Files:**
- Create: `joint/src/main/java/org/joint/modules/system/menu/MenuController.java`
- Create: `joint/src/main/java/org/joint/modules/system/menu/MenuService.java`
- Create: `joint/src/main/java/org/joint/modules/system/menu/dto/CreateMenuDto.java`
- Create: `joint/src/main/java/org/joint/modules/system/menu/dto/UpdateMenuDto.java`
- Create: `joint/src/main/java/org/joint/modules/system/menu/vo/MenuVo.java`

**Step 1: Write the failing test**

Create `joint/src/test/java/org/joint/modules/system/menu/MenuServiceTest.java` with tests that verify:
- menu list builds a parent-child tree ordered by sort
- creating or updating rejects missing parents and descendant cycles
- deleting a menu with children is rejected
- user menu tree excludes button-type menus and only includes assigned menu IDs

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=MenuServiceTest test`
Expected: FAIL because no menu service exists.

**Step 3: Write minimal implementation**

Implement menu CRUD, recursive tree assembly, descendant lookup, and user-menu filtering based on `sys_user_role` and `sys_role_menu`.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=MenuServiceTest test`
Expected: PASS.

### Task 5: Add dept tree CRUD and post CRUD

**Files:**
- Create: `joint/src/main/java/org/joint/modules/system/dept/DeptController.java`
- Create: `joint/src/main/java/org/joint/modules/system/dept/DeptService.java`
- Create: `joint/src/main/java/org/joint/modules/system/dept/dto/CreateDeptDto.java`
- Create: `joint/src/main/java/org/joint/modules/system/dept/dto/UpdateDeptDto.java`
- Create: `joint/src/main/java/org/joint/modules/system/dept/dto/QueryDeptDto.java`
- Create: `joint/src/main/java/org/joint/modules/system/dept/vo/DeptVo.java`
- Create: `joint/src/main/java/org/joint/modules/system/post/PostController.java`
- Create: `joint/src/main/java/org/joint/modules/system/post/PostService.java`
- Create: `joint/src/main/java/org/joint/modules/system/post/dto/CreatePostDto.java`
- Create: `joint/src/main/java/org/joint/modules/system/post/dto/UpdatePostDto.java`
- Create: `joint/src/main/java/org/joint/modules/system/post/dto/QueryPostDto.java`
- Create: `joint/src/main/java/org/joint/modules/system/post/vo/PostVo.java`

**Step 1: Write the failing test**

Create `joint/src/test/java/org/joint/modules/system/dept/DeptServiceTest.java` with tests that verify:
- dept list builds a tree from flat rows
- creating/updating enforces same-level name uniqueness
- updating rejects parent cycles
- deleting a dept with children is rejected

Create `joint/src/test/java/org/joint/modules/system/post/PostServiceTest.java` with tests that verify:
- post list returns sorted VO pages
- post code uniqueness is enforced on create and update
- enabled posts are returned for options

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=DeptServiceTest,PostServiceTest test`
Expected: FAIL because dept/post service layers do not exist.

**Step 3: Write minimal implementation**

Implement department tree CRUD with map-based tree assembly and ancestor-chain cycle checks. Implement post CRUD plus enabled options query and unique code validation.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=DeptServiceTest,PostServiceTest test`
Expected: PASS.

### Task 6: Align controller-level security and finish verification

**Files:**
- Modify: `joint/src/test/java/org/joint/auth/PermissionAuthorizationTest.java`
- Modify: `joint/src/test/java/org/joint/auth/CurrentUserControllerTest.java`
- Test: `joint/src/test/java/org/joint/modules/system/**/*.java`

**Step 1: Write the failing test**

Update controller tests to verify:
- user update endpoint permission code matches phase-4 path and method shape
- user profile still returns the authenticated user after the user controller refactor

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=PermissionAuthorizationTest,CurrentUserControllerTest test`
Expected: FAIL if the refactored controller signatures break existing auth expectations.

**Step 3: Write minimal implementation**

Adjust permission annotations, endpoint mappings, and profile behavior so phase-4 CRUD additions remain compatible with the auth module.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=PermissionAuthorizationTest,CurrentUserControllerTest test`
Expected: PASS.

### Task 7: Final verification

**Files:**
- Test: `joint/src/test/java/org/joint/modules/system/**/*.java`
- Test: `joint/src/test/java/org/joint/auth/*.java`
- Test: `joint/src/test/java/org/joint/modules/auth/AuthServiceTest.java`

**Step 1: Run focused module tests**

Run: `./mvnw -q -Dtest=PageResultTest,UserServiceTest,RoleServiceTest,MenuServiceTest,DeptServiceTest,PostServiceTest,PermissionAuthorizationTest,CurrentUserControllerTest,AuthServiceTest test`
Expected: PASS.

**Step 2: Run full verification**

Run: `./mvnw -q test`
Expected: PASS.

**Step 3: Review scope**

Confirm phase-4 work stays within documented scope:
- no defensive fallback branches
- no undocumented schema rewrites beyond post tables and user-post relation
- no unrelated auth redesign
