package org.joint.modules.system.post.dto;

import lombok.Data;

@Data
public class CreatePostDto {

    private String postCode;

    private String postName;

    private Integer postSort;

    private Integer status;

    private String remark;
}
