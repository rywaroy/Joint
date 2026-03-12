package org.joint.modules.system.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public PageResult<User> list(QueryUserDto query) {
        return PageResult.of(userService.findAll(query));
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable String id) {
        return userService.findById(id);
    }

    @PostMapping
    public User create(@Valid @RequestBody CreateUserDto dto) {
        return userService.create(dto);
    }

    @PutMapping
    public User update(@RequestBody User user) {
        return userService.update(user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }
}
