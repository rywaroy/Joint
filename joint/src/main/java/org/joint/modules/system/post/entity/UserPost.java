package org.joint.modules.system.post.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_posts")
public class UserPost {

    @TableField("userId")
    private String userId;

    @TableField("postId")
    private String postId;

    @TableField("assignedAt")
    private LocalDateTime assignedAt;
}
