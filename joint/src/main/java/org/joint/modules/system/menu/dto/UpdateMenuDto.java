package org.joint.modules.system.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新菜单请求")
public class UpdateMenuDto {

    private String parentId;

    private String name;

    private String title;

    private String path;

    private String component;

    private String icon;

    private String type;

    private String authCode;

    private Integer sort;

    private Integer status;

    private Boolean hidden;
}
