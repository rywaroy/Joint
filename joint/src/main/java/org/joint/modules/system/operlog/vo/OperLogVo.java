package org.joint.modules.system.operlog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "操作日志")
public class OperLogVo {

    @Schema(description = "日志ID")
    private String id;
    @Schema(description = "模块名称")
    private String module;
    @Schema(description = "业务类型")
    private String businessType;
    @Schema(description = "操作描述")
    private String description;
    @Schema(description = "方法签名")
    private String method;
    @Schema(description = "请求方法")
    private String requestMethod;
    @Schema(description = "请求地址")
    private String requestUrl;
    @Schema(description = "请求参数")
    private String requestParams;
    @Schema(description = "响应结果")
    private String responseResult;
    @Schema(description = "执行状态")
    private Integer status;
    @Schema(description = "错误信息")
    private String errorMsg;
    @Schema(description = "操作人ID")
    private String operatorId;
    @Schema(description = "操作人名称")
    private String operatorName;
    @Schema(description = "操作IP")
    private String operatorIp;
    @Schema(description = "耗时(毫秒)")
    private Long costTime;
    @Schema(description = "操作时间")
    private LocalDateTime operateTime;
}
