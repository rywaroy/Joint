# 1.3 第一个 Controller

## 学习目标

- 掌握 @RestController 的使用
- 学习请求映射注解
- 理解请求参数的接收方式

## 概念讲解

### @Controller vs @RestController

| 注解 | 返回值处理 | 使用场景 |
|------|-----------|---------|
| @Controller | 返回视图名称 | 传统 MVC，返回 HTML |
| @RestController | 自动序列化为 JSON | RESTful API |

`@RestController` = `@Controller` + `@ResponseBody`

### 与 NestJS 对比

| NestJS | Spring Boot |
|--------|-------------|
| `@Controller('user')` | `@RequestMapping("/user")` |
| `@Get()` | `@GetMapping` |
| `@Post()` | `@PostMapping` |
| `@Put()` | `@PutMapping` |
| `@Delete()` | `@DeleteMapping` |
| `@Param('id')` | `@PathVariable("id")` |
| `@Query()` | `@RequestParam` |
| `@Body()` | `@RequestBody` |
| `@Request()` | `HttpServletRequest` |

## 请求映射注解

### 基础映射

```java
@RestController
@RequestMapping("/api/user")  // 类级别前缀
public class UserController {

    @GetMapping           // GET /api/user
    @GetMapping("/list")  // GET /api/user/list
    @PostMapping          // POST /api/user
    @PutMapping("/{id}")  // PUT /api/user/{id}
    @DeleteMapping("/{id}") // DELETE /api/user/{id}
}
```

### 参数绑定

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    // 1. 路径参数 - GET /api/user/123
    @GetMapping("/{id}")
    public User getById(@PathVariable String id) {
        return userService.findById(id);
    }

    // 2. 查询参数 - GET /api/user/list?page=1&size=10
    @GetMapping("/list")
    public List<User> list(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer size
    ) {
        return userService.findAll(page, size);
    }

    // 3. 请求体 - POST /api/user (JSON body)
    @PostMapping
    public User create(@RequestBody CreateUserDto dto) {
        return userService.create(dto);
    }

    // 4. 请求头
    @GetMapping("/info")
    public User getInfo(@RequestHeader("Authorization") String token) {
        return userService.getByToken(token);
    }
}
```

## 实践步骤

### 步骤 1：创建目录结构

```
src/main/java/com/joint/
├── JointApplication.java
└── modules/
    └── system/
        └── user/
            ├── UserController.java
            ├── UserService.java
            └── dto/
                ├── CreateUserDto.java
                └── QueryUserDto.java
```

### 步骤 2：创建 DTO

```java
package com.joint.modules.system.user.dto;

import lombok.Data;

@Data  // Lombok: 自动生成 getter/setter/toString/equals/hashCode
public class CreateUserDto {
    private String username;
    private String password;
    private String nickName;
    private String email;
}
```

```java
package com.joint.modules.system.user.dto;

import lombok.Data;

@Data
public class QueryUserDto {
    private Integer page = 1;
    private Integer size = 10;
    private String username;  // 可选的筛选条件
    private Integer status;
}
```

### 步骤 3：创建 Service

```java
package com.joint.modules.system.user;

import com.joint.modules.system.user.dto.CreateUserDto;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserService {

    // 临时用 Map 模拟数据库
    private final Map<String, Map<String, Object>> users = new HashMap<>();

    public Map<String, Object> findById(String id) {
        return users.get(id);
    }

    public List<Map<String, Object>> findAll(Integer page, Integer size) {
        return new ArrayList<>(users.values());
    }

    public Map<String, Object> create(CreateUserDto dto) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("username", dto.getUsername());
        user.put("nickName", dto.getNickName());
        user.put("email", dto.getEmail());
        user.put("createdAt", new Date());
        users.put(id, user);
        return user;
    }

    public void delete(String id) {
        users.remove(id);
    }
}
```

### 步骤 4：创建 Controller

```java
package com.joint.modules.system.user;

import com.joint.modules.system.user.dto.CreateUserDto;
import com.joint.modules.system.user.dto.QueryUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户列表
     * GET /api/system/user/list?page=1&size=10
     */
    @GetMapping("/list")
    public List<Map<String, Object>> list(QueryUserDto query) {
        // Spring 会自动将查询参数绑定到 DTO
        return userService.findAll(query.getPage(), query.getSize());
    }

    /**
     * 获取用户详情
     * GET /api/system/user/{id}
     */
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable String id) {
        return userService.findById(id);
    }

    /**
     * 创建用户
     * POST /api/system/user
     */
    @PostMapping
    public Map<String, Object> create(@RequestBody CreateUserDto dto) {
        return userService.create(dto);
    }

    /**
     * 删除用户
     * DELETE /api/system/user/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }
}
```

### 步骤 5：测试接口

```bash
# 创建用户
curl -X POST http://localhost:8080/api/system/user \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456","nickName":"管理员"}'

# 查询列表
curl http://localhost:8080/api/system/user/list

# 查询详情
curl http://localhost:8080/api/system/user/{id}

# 删除用户
curl -X DELETE http://localhost:8080/api/system/user/{id}
```

## 高级用法

### 多路径映射

```java
@GetMapping({"/list", ""})  // 同时匹配 /list 和 /
public List<User> list() { ... }
```

### 限定请求头/参数

```java
@GetMapping(value = "/info", headers = "X-API-Version=1")
@GetMapping(value = "/search", params = "keyword")
```

### 获取完整请求对象

```java
@GetMapping("/info")
public User getInfo(HttpServletRequest request) {
    String token = request.getHeader("Authorization");
    // ...
}
```

## 与 Nexus 对照

Nexus `user.controller.ts`:
```typescript
@ApiTags('系统管理 - 用户')
@Controller('system/user')
@UseGuards(AuthGuard, PermissionGuard)
export class SystemUserController {

    @Get('list')
    @RequirePermission('system:user:list')
    findAll(@Query() query: QueryUserDto) {
        return this.userService.findAll(query);
    }

    @Get(':id')
    findById(@Param('id') id: string) {
        return this.userService.findById(id);
    }
}
```

Joint `UserController.java`:
```java
@RestController
@RequestMapping("/system/user")
public class UserController {

    @GetMapping("/list")
    public List<User> findAll(QueryUserDto query) {
        return userService.findAll(query);
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable String id) {
        return userService.findById(id);
    }
}
```

## 知识点总结

| 概念 | 说明 |
|------|------|
| @RestController | REST 控制器，自动返回 JSON |
| @RequestMapping | 类级别路由前缀 |
| @GetMapping | GET 请求映射 |
| @PathVariable | 获取路径参数 |
| @RequestParam | 获取查询参数 |
| @RequestBody | 获取请求体 JSON |

## 练习任务

1. 完成用户 CRUD 的四个接口
2. 添加更新用户的 PUT 接口
3. 使用 curl 或 Postman 测试所有接口
