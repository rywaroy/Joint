package org.joint.modules.auth.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginVo {

    private String accessToken;
    private String id;
    private String username;
    private String realName;
    private List<String> roles;
}
