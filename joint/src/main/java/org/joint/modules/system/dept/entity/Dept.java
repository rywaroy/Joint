package org.joint.modules.system.dept.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("depts")
public class Dept {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("pid")
    private String parentId;

    private String name;

    private Integer status;

    private String remark;

    @TableField("treePath")
    private String treePath;

    @TableField(value = "createdAt", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updatedAt", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<Dept> children;
}
