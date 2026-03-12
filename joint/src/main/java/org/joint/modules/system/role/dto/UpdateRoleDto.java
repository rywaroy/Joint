package org.joint.modules.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "更新角色请求")
public class UpdateRoleDto {

    private String name;

    private String code;

    private Integer sort;

    private Integer status;

    private String remark;

    private List<String> permissions;
}
