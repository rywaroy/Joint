package org.joint.modules.system.role.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "角色信息")
public class RoleVo {

    private String id;

    private String name;

    private Integer status;

    private Boolean isSuper;

    private Boolean isBuiltin;

    private String remark;

    private List<String> permissions;

    private String createTime;
}
