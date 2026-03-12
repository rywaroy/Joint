package org.joint.modules.system.post.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostVo {

    private String id;

    private String postCode;

    private String postName;

    private Integer postSort;

    private Integer status;

    private String remark;

    private LocalDateTime createdAt;
}
