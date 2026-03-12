package org.joint.modules.system.dept.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_dept")
public class Dept {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String parentId;

    private String name;

    private Integer sort;

    private String leader;

    private String phone;

    private String email;

    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<Dept> children;
}
