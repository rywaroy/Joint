package org.joint.modules.system.post.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建岗位请求")
public class CreatePostDto {

    @NotBlank(message = "岗位编码不能为空")
    private String postCode;

    @NotBlank(message = "岗位名称不能为空")
    private String postName;

    @Min(value = 0, message = "岗位排序不能小于0")
    private Integer postSort;

    private Integer status;

    private String remark;
}
