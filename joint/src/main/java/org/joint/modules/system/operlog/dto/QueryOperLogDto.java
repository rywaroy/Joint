package org.joint.modules.system.operlog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "操作日志查询条件")
public class QueryOperLogDto {

    @Schema(description = "页码", example = "1")
    private Integer page = 1;
    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;
    @Schema(description = "模块名称")
    private String module;
    @Schema(description = "业务类型")
    private String businessType;
    @Schema(description = "执行状态")
    private Integer status;
    @Schema(description = "操作人名称")
    private String operatorName;
}
