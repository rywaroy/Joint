package org.joint.modules.system.dept.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新部门请求")
public class UpdateDeptDto {

    @Schema(description = "部门名称", example = "研发部")
    @Size(min = 2, max = 50, message = "部门名称长度为2-50个字符")
    private String name;

    @Schema(description = "父级部门ID")
    private String pid;

    @Schema(description = "状态 0-启用 1-停用", example = "0")
    @Min(value = 0, message = "状态值只能是0或1")
    @Max(value = 1, message = "状态值只能是0或1")
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 200, message = "备注最多200个字符")
    private String remark;

    private boolean nameSet;

    private boolean pidSet;

    private boolean statusSet;

    private boolean remarkSet;

    @JsonSetter("name")
    public void setName(String name) {
        this.name = name;
        this.nameSet = true;
    }

    @JsonSetter("pid")
    public void setPid(String pid) {
        this.pid = pid;
        this.pidSet = true;
    }

    @JsonSetter("status")
    public void setStatus(Integer status) {
        this.status = status;
        this.statusSet = true;
    }

    @JsonSetter("remark")
    public void setRemark(String remark) {
        this.remark = remark;
        this.remarkSet = true;
    }
}
