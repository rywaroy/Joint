package org.joint.modules.system.menu.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MenuVo {

    private String id;

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

    private LocalDateTime createdAt;

    private List<MenuVo> children;
}
