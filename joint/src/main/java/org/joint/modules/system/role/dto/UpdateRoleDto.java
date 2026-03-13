package org.joint.modules.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "更新角色请求")
public class UpdateRoleDto {

    private String name;

    @Min(value = 0, message = "状态值必须为 0 或 1")
    @Max(value = 1, message = "状态值必须为 0 或 1")
    private Integer status;

    private String remark;

    private List<String> permissions;
}
