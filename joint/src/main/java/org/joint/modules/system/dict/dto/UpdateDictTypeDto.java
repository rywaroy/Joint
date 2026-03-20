package org.joint.modules.system.dict.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新字典类型请求")
public class UpdateDictTypeDto {

    @Size(min = 1, message = "字典名称不能为空")
    private String dictName;

    @Size(min = 1, message = "字典类型不能为空")
    private String dictType;

    private Integer status;

    private String remark;
}
