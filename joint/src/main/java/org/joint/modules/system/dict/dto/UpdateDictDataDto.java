package org.joint.modules.system.dict.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新字典数据请求")
public class UpdateDictDataDto {

    @Size(min = 1, message = "字典类型不能为空")
    private String typeId;

    @Size(min = 1, message = "字典标签不能为空")
    private String dictLabel;

    @Size(min = 1, message = "字典键值不能为空")
    private String dictValue;

    @Min(value = 0, message = "字典排序不能小于0")
    private Integer dictSort;

    private String cssClass;

    private String listClass;

    private Boolean isDefault;

    private Integer status;

    private String remark;
}
