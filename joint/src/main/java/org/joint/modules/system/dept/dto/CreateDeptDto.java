package org.joint.modules.system.dept.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建部门请求")
public class CreateDeptDto {

    @Schema(description = "部门名称", example = "研发部")
    @NotBlank(message = "部门名称不能为空")
    @Size(min = 2, max = 50, message = "部门名称长度为2-50个字符")
    private String name;

    @Schema(description = "父级部门ID")
    private String pid;

    @Schema(description = "状态 0-启用 1-停用", example = "0")
    @Min(value = 0, message = "状态值只能是0或1")
    @Max(value = 1, message = "状态值只能是0或1")
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 200, message = "备注最多200个字符")
    private String remark;
}
