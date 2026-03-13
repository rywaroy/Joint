package org.joint.modules.system.operlog.dto;

import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "操作日志查询条件")
public class QueryOperLogDto {

    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "模块标题")
    private String title;

    @Schema(description = "操作人员")
    private String operName;

    @Schema(description = "业务类型")
    private Integer businessType;

    @Schema(description = "执行状态")
    private Integer status;

    @Schema(description = "开始时间")
    private String beginTime;

    @Schema(description = "结束时间")
    private String endTime;
}
