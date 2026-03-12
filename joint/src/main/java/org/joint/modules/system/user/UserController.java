package org.joint.modules.system.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.joint.modules.system.user.dto.CreateUserDto;
import org.joint.modules.system.user.dto.QueryUserDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    public List<Map<String, Object>> list(QueryUserDto query) {
        return userService.findAll(query.getPage(), query.getSize());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable String id) {
        return userService.findById(id);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateUserDto dto) {
        return userService.create(dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }
}
