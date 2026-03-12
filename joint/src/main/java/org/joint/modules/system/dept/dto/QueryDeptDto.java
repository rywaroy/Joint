package org.joint.modules.system.dept.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "部门查询条件")
public class QueryDeptDto {

    private String name;

    private Integer status;
}
