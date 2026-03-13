package org.joint.modules.system.menu.dto;

import lombok.Data;

@Data
public class MenuApiSaveDto {

    private String parentId;

    private String pid;

    private String name;

    private String title;

    private String path;

    private String component;

    private String type;

    private String authCode;

    private Integer order;

    private Integer status;

    private String icon;

    private String activeIcon;

    private String activePath;

    private String badge;

    private String badgeType;

    private String badgeVariants;

    private Boolean keepAlive;

    private Boolean affixTab;

    private Boolean hideInMenu;

    private Boolean hideChildrenInMenu;

    private Boolean hideInBreadcrumb;

    private Boolean hideInTab;

    private String iframeSrc;

    private String link;
}
