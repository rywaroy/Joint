package org.joint.modules.system.menu.dto;

import lombok.Data;

@Data
public class MenuApiQueryDto {

    private String name;

    private String title;

    private Integer status;

    private String type;

    private String parentId;
}
