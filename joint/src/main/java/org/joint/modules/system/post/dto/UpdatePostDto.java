package org.joint.modules.system.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新岗位请求")
public class UpdatePostDto {

    private String postCode;

    private String postName;

    private Integer postSort;

    private Integer status;

    private String remark;
}
