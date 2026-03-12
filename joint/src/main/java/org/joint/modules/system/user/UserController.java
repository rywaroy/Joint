package org.joint.modules.system.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.CurrentUser;
import org.joint.common.annotation.RequirePermission;
import org.joint.common.response.PageResult;
import org.joint.modules.system.user.dto.CreateUserDto;
import org.joint.modules.system.user.dto.QueryUserDto;
import org.joint.modules.system.user.entity.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    @RequirePermission("system:user:list")
    public PageResult<User> list(QueryUserDto query) {
        return PageResult.of(userService.findAll(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:user:query")
    public User getById(@PathVariable String id) {
        return userService.findById(id);
    }

    @GetMapping("/profile")
    public User profile(@CurrentUser String userId) {
        return userService.findById(userId);
    }

    @PostMapping
    @RequirePermission("system:user:add")
    public User create(@Valid @RequestBody CreateUserDto dto) {
        return userService.create(dto);
    }

    @PutMapping
    @RequirePermission("system:user:update")
    public User update(@RequestBody User user) {
        return userService.update(user);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:user:delete")
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }
}
