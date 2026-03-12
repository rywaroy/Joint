package org.joint.modules.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.joint.modules.auth.dto.LoginDto;
import org.joint.modules.auth.vo.LoginVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginVo login(@Valid @RequestBody LoginDto dto) {
        return authService.login(dto);
    }

    @PostMapping("/logout")
    public void logout() {
    }
}
