package org.joint.modules.system.user.vo;

import lombok.Data;
import org.joint.modules.system.dept.vo.DeptVo;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDetailVo {

    private String id;

    private String username;

    private String nickName;

    private String email;

    private String phone;

    private String avatar;

    private Integer status;

    private String deptId;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<String> roleIds;

    private List<String> postIds;

    private DeptVo dept;
}
