package org.joint.modules.system.dict.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字典类型信息")
public class DictTypeVo {

    private String id;

    private String dictName;

    private String dictType;

    private Integer status;

    private String remark;

    private String createTime;
}
