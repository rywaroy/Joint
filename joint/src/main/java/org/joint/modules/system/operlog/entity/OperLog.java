package org.joint.modules.system.operlog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_oper_log")
public class OperLog {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String module;
    private String businessType;
    private String description;
    private String method;
    private String requestMethod;
    private String requestUrl;
    private String requestParams;
    private String responseResult;
    private Integer status;
    private String errorMsg;
    private String operatorId;
    private String operatorName;
    private String operatorIp;
    private Long costTime;
    private LocalDateTime operateTime;
}
