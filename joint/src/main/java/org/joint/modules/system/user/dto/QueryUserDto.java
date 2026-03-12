package org.joint.modules.system.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户查询条件")
public class QueryUserDto {
    @Schema(description = "页码", example = "1")
    private Integer page = 1;
    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "昵称")
    private String nickName;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "部门ID")
    private String deptId;
}
