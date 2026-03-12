package org.joint.modules.system.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String username;

    @TableField(select = false)
    private String password;

    private String nickName;

    private String email;

    private String phone;

    private String avatar;

    private Integer status;

    private String deptId;

    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<org.joint.modules.system.role.entity.Role> roles;

    @TableField(exist = false)
    private org.joint.modules.system.dept.entity.Dept dept;
}
