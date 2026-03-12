package org.joint.modules.system.role.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateRoleDto {

    @NotBlank(message = "角色名称不能为空")
    private String name;

    @NotBlank(message = "角色编码不能为空")
    private String code;

    private Integer sort;

    private Integer status;

    private String remark;

    private List<String> permissions;
}
