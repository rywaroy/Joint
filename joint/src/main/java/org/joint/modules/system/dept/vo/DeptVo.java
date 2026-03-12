package org.joint.modules.system.dept.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DeptVo {

    private String id;

    private String parentId;

    private String name;

    private Integer sort;

    private String leader;

    private String phone;

    private String email;

    private Integer status;

    private LocalDateTime createdAt;

    private List<DeptVo> children;
}
