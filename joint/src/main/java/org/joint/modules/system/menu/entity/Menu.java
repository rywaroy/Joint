package org.joint.modules.system.menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("menus")
public class Menu {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("parentId")
    private String parentId;

    private String name;

    private String title;

    private String path;

    private String component;

    private String icon;

    private String type;

    @TableField("authCode")
    private String authCode;

    @TableField("`order`")
    private Integer sort;

    private Integer status;

    @TableField("hideInMenu")
    private Boolean hidden;

    @TableField(value = "createdAt", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updatedAt", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<Menu> children;
}
