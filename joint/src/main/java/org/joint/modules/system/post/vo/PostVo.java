package org.joint.modules.system.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "岗位信息")
public class PostVo {

    private String id;

    private String postCode;

    private String postName;

    private Integer postSort;

    private Integer status;

    private String remark;

    private String createTime;
}
