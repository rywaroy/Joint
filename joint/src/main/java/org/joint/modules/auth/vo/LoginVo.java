package org.joint.modules.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "登录响应")
public class LoginVo {

    @Schema(description = "访问令牌")
    private String accessToken;
    @Schema(description = "用户ID")
    private String id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "显示名称")
    private String realName;
    @Schema(description = "角色编码列表")
    private List<String> roles;
}
