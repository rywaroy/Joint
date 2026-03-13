package org.joint.modules.system.dept.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "部门查询条件")
public class QueryDeptDto {

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "状态 0-启用 1-停用")
    @Min(value = 0, message = "状态值只能是0或1")
    @Max(value = 1, message = "状态值只能是0或1")
    private Integer status;
}
