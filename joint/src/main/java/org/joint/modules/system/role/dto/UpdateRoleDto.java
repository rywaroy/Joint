package org.joint.modules.system.role.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateRoleDto {

    private String name;

    private String code;

    private Integer sort;

    private Integer status;

    private String remark;

    private List<String> permissions;
}
