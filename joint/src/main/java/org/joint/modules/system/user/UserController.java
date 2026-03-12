package org.joint.modules.system.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.CurrentUser;
import org.joint.common.annotation.RequirePermission;
import org.joint.common.response.PageResult;
import org.joint.modules.system.user.dto.CreateUserDto;
import org.joint.modules.system.user.dto.QueryUserDto;
import org.joint.modules.system.user.dto.ResetPasswordDto;
import org.joint.modules.system.user.dto.UpdateUserDto;
import org.joint.modules.system.user.dto.UpdateUserStatusDto;
import org.joint.modules.system.user.entity.User;
import org.joint.modules.system.user.vo.UserDetailVo;
import org.joint.modules.system.user.vo.UserVo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    @RequirePermission("system:user:list")
    public PageResult<UserVo> list(QueryUserDto query) {
        return userService.findPage(query);
    }

    @GetMapping("/{id}")
    @RequirePermission("system:user:query")
    public UserDetailVo getById(@PathVariable String id) {
        return userService.findDetailById(id);
    }

    @GetMapping("/profile")
    public User profile(@CurrentUser String userId) {
        return userService.findById(userId);
    }

    @PostMapping
    @RequirePermission("system:user:add")
    public UserVo create(@Valid @RequestBody CreateUserDto dto) {
        return userService.create(dto);
    }

    @PutMapping("/{id}")
    @RequirePermission("system:user:edit")
    public UserVo update(@PathVariable String id, @Valid @RequestBody UpdateUserDto dto) {
        return userService.update(id, dto);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("system:user:edit")
    public void updateStatus(@PathVariable String id, @Valid @RequestBody UpdateUserStatusDto dto) {
        userService.updateStatus(id, dto.getStatus());
    }

    @PutMapping("/{id}/reset-password")
    @RequirePermission("system:user:edit")
    public void resetPassword(@PathVariable String id, @Valid @RequestBody ResetPasswordDto dto) {
        userService.resetPassword(id, dto.getPassword());
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:user:delete")
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }
}
