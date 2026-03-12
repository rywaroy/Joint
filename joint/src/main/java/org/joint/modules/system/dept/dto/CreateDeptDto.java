package org.joint.modules.system.dept.dto;

import lombok.Data;

@Data
public class CreateDeptDto {

    private String name;

    private String parentId;

    private Integer sort;

    private String leader;

    private String phone;

    private String email;

    private Integer status;
}
