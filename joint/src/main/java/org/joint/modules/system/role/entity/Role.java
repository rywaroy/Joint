package org.joint.modules.system.role.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("roles")
public class Role {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String name;

    @TableField(exist = false)
    private String code;

    private Integer status;

    @TableField("isBuiltin")
    private Boolean isBuiltin;

    @TableField("isSuper")
    private Boolean isSuper;

    private String remark;

    @TableField(value = "createdAt", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updatedAt", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<org.joint.modules.system.menu.entity.Menu> menus;
}
