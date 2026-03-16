package org.joint.modules.system.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_roles")
public class UserRole {

    @TableField("userId")
    private String userId;

    @TableField("roleId")
    private String roleId;

    @TableField("assignedAt")
    private LocalDateTime assignedAt;
}
