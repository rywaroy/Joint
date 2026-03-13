package org.joint.modules.system.dept.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "部门信息")
public class DeptVo {

    private String id;

    private String pid;

    private String name;

    private Integer status;

    private String remark;

    private String createTime;

    private List<DeptVo> children;
}
