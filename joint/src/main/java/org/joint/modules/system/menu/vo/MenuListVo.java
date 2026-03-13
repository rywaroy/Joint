package org.joint.modules.system.menu.vo;

import lombok.Data;

import java.util.List;

@Data
public class MenuListVo {

    private String id;

    private String pid;

    private String name;

    private String path;

    private String component;

    private String authCode;

    private Integer status;

    private String type;

    private Meta meta;

    private List<MenuListVo> children;

    @Data
    public static class Meta {

        private String title;

        private String icon;

        private Integer order;

        private Boolean hideInMenu;
    }
}
