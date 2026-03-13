package org.joint.modules.system.operlog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oper_logs")
public class OperLog {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("title")
    private String title;

    @TableField("businessType")
    private Integer businessType;

    private String method;

    @TableField("requestMethod")
    private String requestMethod;

    @TableField("operName")
    private String operName;

    @TableField("deptName")
    private String deptName;

    @TableField("operUrl")
    private String operUrl;

    @TableField("operIp")
    private String operIp;

    @TableField("operLocation")
    private String operLocation;

    @TableField("operParam")
    private String operParam;

    @TableField("jsonResult")
    private String jsonResult;

    private Integer status;

    @TableField("errorMsg")
    private String errorMsg;

    @TableField("costTime")
    private Long costTime;

    @TableField("operTime")
    private LocalDateTime operTime;
}
