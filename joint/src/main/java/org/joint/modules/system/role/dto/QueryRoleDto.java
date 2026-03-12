package org.joint.modules.system.role.dto;

import lombok.Data;

@Data
public class QueryRoleDto {

    private Integer page = 1;

    private Integer size = 10;

    private String name;

    private String code;

    private Integer status;
}
