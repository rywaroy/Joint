# Phase 6 Testing And Deploy Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Complete phase 6 for Joint by adding real controller integration tests, Docker deployment assets, and environment-specific application configuration.

**Architecture:** Reuse the current Spring Boot module boundaries and add a dedicated `test` profile that swaps MySQL for an in-memory database while keeping the real MVC, security, MyBatis-Plus, and service stack active. Deployment will stay minimal: a multi-stage Docker image, a compose stack for MySQL + Redis + app, and profile-based property files so local and container startup paths are explicit.

**Tech Stack:** Spring Boot 4, Spring Security 7, MyBatis-Plus, H2, MockMvc, JUnit 5, Mockito, Docker, Docker Compose

### Task 1: Add phase-6 test infrastructure

**Files:**
- Create: `joint/src/test/resources/application-test.properties`
- Create: `joint/src/test/resources/schema-h2.sql`
- Create: `joint/src/test/java/org/joint/support/integration/IntegrationTestConfig.java`
- Create: `joint/src/test/java/org/joint/support/integration/BaseIntegrationTest.java`

**Step 1: Write the failing test**

Add a Spring Boot integration base that activates a `test` profile and expects the application context, MockMvc, H2 datasource, and a non-Redis cache manager to boot together.

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=JointApplicationTests test`
Expected: FAIL because there is no test profile or embedded database configuration.

**Step 3: Write minimal implementation**

Add H2 test dependencies and the smallest test-only configuration needed to replace Redis beans and load schema into an in-memory database.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=JointApplicationTests test`
Expected: PASS.

### Task 2: Add real controller integration tests

**Files:**
- Create: `joint/src/test/java/org/joint/modules/system/user/UserControllerIntegrationTest.java`
- Modify: `joint/src/test/java/org/joint/JointApplicationTests.java`

**Step 1: Write the failing test**

Add integration tests that boot the full application and verify:
- unauthenticated `/api/system/user/list` returns the auth error payload
- authenticated create user succeeds against the real service + mapper stack
- missing required fields on create returns validation errors
- list returns persisted users with paging metadata

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=UserControllerIntegrationTest test`
Expected: FAIL because the test profile and integration wiring do not exist yet.

**Step 3: Write minimal implementation**

Seed only the rows required for security and permissions, provide a JWT helper in the test base, and clean mutable tables after each test.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=UserControllerIntegrationTest test`
Expected: PASS.

### Task 3: Split application configuration by environment

**Files:**
- Modify: `joint/src/main/resources/application.properties`
- Create: `joint/src/main/resources/application-dev.properties`
- Create: `joint/src/main/resources/application-prod.properties`

**Step 1: Write the failing test**

Use the integration tests as the red phase for profile-aware configuration loading.

**Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=JointApplicationTests,UserControllerIntegrationTest test`
Expected: FAIL if profile defaults and environment overrides are not aligned.

**Step 3: Write minimal implementation**

Move the current local defaults into `application-dev.properties`, keep shared settings in `application.properties`, and bind production datasource / Redis / file base URL from environment variables in `application-prod.properties`.

**Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=JointApplicationTests,UserControllerIntegrationTest test`
Expected: PASS.

### Task 4: Add container deployment assets

**Files:**
- Create: `joint/Dockerfile`
- Create: `joint/docker-compose.yml`
- Create: `joint/.env.example`

**Step 1: Write the failing test**

Treat missing deployment artifacts as the red phase: the phase requirement cannot be satisfied until these files exist and reference the new production profile.

**Step 2: Run test to verify it fails**

Run: `test -f Dockerfile && test -f docker-compose.yml && test -f .env.example`
Expected: FAIL because the files do not exist.

**Step 3: Write minimal implementation**

Add a multi-stage Maven build Dockerfile, a compose stack with app + MySQL + Redis, and an example env file that documents required secrets.

**Step 4: Run test to verify it passes**

Run: `test -f Dockerfile && test -f docker-compose.yml && test -f .env.example`
Expected: PASS.

### Task 5: Final verification

**Files:**
- Test: `joint/src/test/java/org/joint/**/*.java`

**Step 1: Run focused phase-6 verification**

Run: `./mvnw -q -Dtest=JointApplicationTests,UserControllerIntegrationTest,UserServiceTest,AuthServiceTest test`
Expected: PASS.

**Step 2: Run full test suite**

Run: `./mvnw -q test`
Expected: PASS.

**Step 3: Review scope**

Confirm the implementation stays within phase-6 scope:
- no unrelated module redesign
- no defensive fallback branches
- deployment assets point at the existing app entrypoint and schema
