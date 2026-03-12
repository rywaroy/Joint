package org.joint.modules.system.role.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoleVo {

    private String id;

    private String name;

    private String code;

    private Integer sort;

    private Integer status;

    private Boolean isSuper;

    private Boolean isBuiltin;

    private String remark;

    private List<String> permissions;

    private LocalDateTime createdAt;
}
