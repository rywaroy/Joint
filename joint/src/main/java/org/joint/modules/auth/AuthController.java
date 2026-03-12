package org.joint.modules.auth;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.Log;
import org.joint.common.enums.BusinessType;
import org.joint.modules.auth.dto.LoginDto;
import org.joint.modules.auth.vo.LoginVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Log(module = "认证管理", type = BusinessType.LOGIN, description = "用户登录")
    @Operation(summary = "用户登录", security = {})
    public LoginVo login(@Valid @RequestBody LoginDto dto) {
        return authService.login(dto);
    }

    @PostMapping("/logout")
    @Log(module = "认证管理", type = BusinessType.LOGOUT, description = "用户登出")
    @Operation(summary = "用户登出")
    public void logout(HttpServletRequest request) {
        authService.logout(request.getHeader("Authorization").substring(7));
    }
}
