package org.joint.modules.system.dept.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新部门请求")
public class UpdateDeptDto {

    private String name;

    private String parentId;

    private Integer sort;

    private String leader;

    private String phone;

    private String email;

    private Integer status;
}
