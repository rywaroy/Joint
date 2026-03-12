package org.joint.modules.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色查询条件")
public class QueryRoleDto {

    private Integer page = 1;

    private Integer size = 10;

    private String name;

    private String code;

    private Integer status;
}
