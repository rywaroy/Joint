package org.joint.modules.system.menu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_menu")
public class Menu {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String parentId;

    private String name;

    private String path;

    private String component;

    private String icon;

    private Integer type;

    private String authCode;

    private Integer sort;

    private Integer status;

    private Boolean hidden;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<Menu> children;
}
