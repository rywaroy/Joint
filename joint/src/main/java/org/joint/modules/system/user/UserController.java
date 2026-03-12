package org.joint.modules.system.user;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.CurrentUser;
import org.joint.common.annotation.Log;
import org.joint.common.annotation.RequirePermission;
import org.joint.common.enums.BusinessType;
import org.joint.common.response.PageResult;
import org.joint.modules.system.user.dto.CreateUserDto;
import org.joint.modules.system.user.dto.QueryUserDto;
import org.joint.modules.system.user.dto.ResetPasswordDto;
import org.joint.modules.system.user.dto.UpdateUserDto;
import org.joint.modules.system.user.dto.UpdateUserStatusDto;
import org.joint.modules.system.user.entity.User;
import org.joint.modules.system.user.vo.UserDetailVo;
import org.joint.modules.system.user.vo.UserVo;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@Tag(name = "用户管理")
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    @RequirePermission("system:user:list")
    @Operation(summary = "分页查询用户")
    public PageResult<UserVo> list(@ParameterObject QueryUserDto query) {
        return userService.findPage(query);
    }

    @GetMapping("/{id}")
    @RequirePermission("system:user:query")
    @Operation(summary = "查询用户详情")
    public UserDetailVo getById(@PathVariable String id) {
        return userService.findDetailById(id);
    }

    @GetMapping("/profile")
    @Operation(summary = "获取当前用户信息")
    public User profile(@CurrentUser String userId) {
        return userService.findById(userId);
    }

    @PostMapping
    @RequirePermission("system:user:add")
    @Log(module = "用户管理", type = BusinessType.INSERT, description = "创建用户")
    @Operation(summary = "创建用户")
    public UserVo create(@Valid @RequestBody CreateUserDto dto) {
        return userService.create(dto);
    }

    @PutMapping("/{id}")
    @RequirePermission("system:user:edit")
    @Log(module = "用户管理", type = BusinessType.UPDATE, description = "更新用户")
    @Operation(summary = "更新用户")
    public UserVo update(@PathVariable String id, @Valid @RequestBody UpdateUserDto dto) {
        return userService.update(id, dto);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("system:user:edit")
    @Log(module = "用户管理", type = BusinessType.UPDATE, description = "修改用户状态")
    @Operation(summary = "修改用户状态")
    public void updateStatus(@PathVariable String id, @Valid @RequestBody UpdateUserStatusDto dto) {
        userService.updateStatus(id, dto.getStatus());
    }

    @PutMapping("/{id}/reset-password")
    @RequirePermission("system:user:edit")
    @Log(module = "用户管理", type = BusinessType.UPDATE, description = "重置用户密码")
    @Operation(summary = "重置用户密码")
    public void resetPassword(@PathVariable String id, @Valid @RequestBody ResetPasswordDto dto) {
        userService.resetPassword(id, dto.getPassword());
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:user:delete")
    @Log(module = "用户管理", type = BusinessType.DELETE, description = "删除用户")
    @Operation(summary = "删除用户")
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }
}
