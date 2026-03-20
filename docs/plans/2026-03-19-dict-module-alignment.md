# Dict Module Alignment Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Align Joint's backend with Nexus commit `51b8ee017e3a5791943fc2c25aa3315770ed0cce` by adding dictionary type/data management, cache refresh, and menu/schema seeding.

**Architecture:** Follow Joint's existing Spring Boot + MyBatis-Plus module pattern (`Controller -> Service -> Mapper -> DTO/VO`). Keep dictionary cache in Redis via `RedisUtils`, expose Nexus-compatible response shapes, and seed system menus through `schema.sql` just like existing system modules.

**Tech Stack:** Spring Boot 4, Spring Security 7, MyBatis-Plus, RedisTemplate, JUnit 5, Mockito, MockMvc, H2

### Task 1: Lock dict service behavior with unit tests

**Files:**
- Create: `joint/src/test/java/org/joint/modules/system/dict/DictServiceTest.java`

**Step 1: Write the failing test**

Cover:
- dict type list returns `{list, total}` with Nexus field names
- `findDictDataByType` reads from cache first and backfills cache on miss
- duplicate dict type creation is rejected
- duplicate dict label under same type is rejected
- refresh cache clears per-type keys and returns cleared count

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=DictServiceTest test`
Expected: FAIL because no dict service/module exists yet.

**Step 3: Write minimal implementation**

Create dict entities, mappers, DTOs, VOs, and service logic with cache invalidation on type/data mutation.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=DictServiceTest test`
Expected: PASS.

### Task 2: Lock controller contract with integration tests

**Files:**
- Create: `joint/src/test/java/org/joint/modules/system/dict/DictControllerIntegrationTest.java`
- Modify: `joint/src/test/java/org/joint/support/integration/BaseIntegrationTest.java` if shared mocking is needed

**Step 1: Write the failing test**

Cover:
- `/system/dict/type/list` returns paged type payload
- `/system/dict/type/options` returns type options
- `/system/dict/data/type/{dictType}` returns cached-or-db data payload
- `/system/dict/data` create validates required fields
- `/system/dict/cache` returns `{cleared}` payload

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=DictControllerIntegrationTest test`
Expected: FAIL because endpoints and schema are missing.

**Step 3: Write minimal implementation**

Add controller mappings, permission annotations, log annotations, and integration-test Redis mocking.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=DictControllerIntegrationTest test`
Expected: PASS.

### Task 3: Add schema and menu seed alignment

**Files:**
- Modify: `joint/src/main/resources/sql/schema.sql`
- Modify: `joint/src/test/resources/schema-h2.sql`

**Step 1: Write the failing test**

Extend `DictControllerIntegrationTest` assertions so admin permission resolution can see:
- `dict_types` / `dict_data` tables
- `SystemDict` menu and its `system:dict:*` button permissions

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=DictControllerIntegrationTest test`
Expected: FAIL because H2/MySQL schema lacks dict tables and menu seeds.

**Step 3: Write minimal implementation**

Create both tables and insert `SystemDict`, `SystemDictList`, `SystemDictQuery`, `SystemDictCreate`, `SystemDictUpdate`, `SystemDictDelete`, shifting later menu orders to match Nexus.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=DictControllerIntegrationTest test`
Expected: PASS.

### Task 4: Final verification

**Files:**
- Test: `joint/src/test/java/org/joint/modules/system/dict/*.java`

**Step 1: Run focused dict tests**

Run: `./mvnw -q -Dtest=DictServiceTest,DictControllerIntegrationTest test`
Expected: PASS.

**Step 2: Run broader regression tests**

Run: `./mvnw -q -Dtest=RoleControllerIntegrationTest,PostControllerIntegrationTest,PermissionAuthorizationTest test`
Expected: PASS.

**Step 3: Review scope**

Confirm:
- no defensive fallback branches were introduced
- existing dirty files outside dict scope were untouched
- dict feature is aligned for backend only
