package org.joint.modules.system.post.dto;

import lombok.Data;

@Data
public class QueryPostDto {

    private Integer page = 1;

    private Integer size = 10;

    private String postCode;

    private String postName;

    private Integer status;
}
