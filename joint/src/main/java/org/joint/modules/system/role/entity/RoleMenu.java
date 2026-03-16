package org.joint.modules.system.role.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("role_menus")
public class RoleMenu {

    @TableField("roleId")
    private String roleId;

    @TableField("menuId")
    private String menuId;

    @TableField("grantedAt")
    private LocalDateTime grantedAt;
}
