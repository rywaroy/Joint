package org.joint.modules.system.operlog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "操作日志")
public class OperLogVo {

    @Schema(description = "日志ID")
    private String id;

    @Schema(description = "模块标题")
    private String title;

    @Schema(description = "业务类型")
    private Integer businessType;

    @Schema(description = "方法签名")
    private String method;

    @Schema(description = "请求方法")
    private String requestMethod;

    @Schema(description = "操作人员")
    private String operName;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "请求地址")
    private String operUrl;

    @Schema(description = "操作IP")
    private String operIp;

    @Schema(description = "操作地点")
    private String operLocation;

    @Schema(description = "请求参数")
    private String operParam;

    @Schema(description = "响应结果")
    private String jsonResult;

    @Schema(description = "执行状态")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "耗时(毫秒)")
    private Long costTime;

    @Schema(description = "操作时间")
    private String operTime;
}
