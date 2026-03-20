package org.joint.modules.system.dict.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dict_data")
public class DictData {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("typeId")
    private String typeId;

    @TableField("dictLabel")
    private String dictLabel;

    @TableField("dictValue")
    private String dictValue;

    @TableField("dictSort")
    private Integer dictSort;

    @TableField("cssClass")
    private String cssClass;

    @TableField("listClass")
    private String listClass;

    @TableField("isDefault")
    private Boolean isDefault;

    private Integer status;

    private String remark;

    @TableField(value = "createdAt", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updatedAt", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
