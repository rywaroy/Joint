package org.joint.modules.system.dict.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字典数据信息")
public class DictDataVo {

    private String id;

    private String typeId;

    private String dictName;

    private String dictType;

    private String dictLabel;

    private String dictValue;

    private Integer dictSort;

    private String cssClass;

    private String listClass;

    private Boolean isDefault;

    private Integer status;

    private String remark;

    private String createTime;
}
