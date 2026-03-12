package org.joint.modules.system.menu.dto;

import lombok.Data;

@Data
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
