package org.joint.modules.system.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建菜单请求")
public class CreateMenuDto {

    private String parentId;

    private String name;

    private String path;

    private String component;

    private String icon;

    private Integer type;

    private String authCode;

    private Integer sort;

    private Integer status;

    private Boolean hidden;
}
