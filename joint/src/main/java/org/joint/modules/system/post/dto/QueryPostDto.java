package org.joint.modules.system.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "岗位查询条件")
public class QueryPostDto {

    private Integer page = 1;

    private Integer size = 10;

    private String postCode;

    private String postName;

    private Integer status;
}
