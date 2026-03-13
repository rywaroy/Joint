package org.joint.modules.system.post.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("posts")
public class Post {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("postCode")
    private String postCode;

    @TableField("postName")
    private String postName;

    @TableField("postSort")
    private Integer postSort;

    private Integer status;

    private String remark;

    @TableField(value = "createdAt", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updatedAt", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
