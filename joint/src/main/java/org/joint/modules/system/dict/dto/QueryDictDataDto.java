package org.joint.modules.system.dict.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "字典数据查询条件")
public class QueryDictDataDto {

    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    private Integer pageSize = 10;

    private String typeId;

    private String dictType;

    private String dictLabel;

    private Integer status;
}
