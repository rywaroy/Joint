package org.joint.modules.system.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user_post")
public class UserPost {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String userId;

    private String postId;
}
