package org.joint.modules.system.post.dto;

import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "岗位查询条件")
public class QueryPostDto {

    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    private Integer pageSize = 10;

    private String postCode;

    private String postName;

    private Integer status;
}
