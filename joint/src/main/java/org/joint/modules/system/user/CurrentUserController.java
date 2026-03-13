package org.joint.modules.system.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.CurrentUser;
import org.joint.common.security.LoginUser;
import org.joint.modules.system.user.vo.CurrentUserInfoVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
