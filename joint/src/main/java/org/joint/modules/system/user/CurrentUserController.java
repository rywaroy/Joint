package org.joint.modules.system.user;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.CurrentUser;
import org.joint.common.annotation.Log;
import org.joint.common.enums.BusinessType;
import org.joint.common.security.LoginUser;
import org.joint.modules.system.user.dto.ChangePasswordDto;
import org.joint.modules.system.user.dto.RegisterUserDto;
import org.joint.modules.system.user.vo.CurrentUserInfoVo;
import org.joint.modules.system.user.vo.UserVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "当前用户")
public class CurrentUserController {

    private final UserService userService;

    @GetMapping("/info")
    @Operation(summary = "获取当前登录用户信息")
    public CurrentUserInfoVo info(@CurrentUser LoginUser loginUser) {
        return userService.findCurrentUserInfo(loginUser);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public UserVo register(@Valid @RequestBody RegisterUserDto dto) {
        return userService.register(dto);
    }

    @PutMapping("/change-password")
    @Log(module = "用户管理", type = BusinessType.UPDATE, description = "修改当前用户密码", saveRequestData = false)
    @Operation(summary = "修改当前用户密码")
    public Map<String, String> changePassword(@CurrentUser LoginUser loginUser,
                                              @Valid @RequestBody ChangePasswordDto dto) {
        return userService.changePassword(loginUser.getUserId(), dto.getOldPassword(), dto.getNewPassword());
    }
}
