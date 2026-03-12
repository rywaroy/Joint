package org.joint.modules.system.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVo {

    private String id;

    private String username;

    private String nickName;

    private String email;

    private String phone;

    private String avatar;

    private Integer status;

    private String deptId;

    private String deptName;

    private String remark;

    private LocalDateTime createdAt;
}
