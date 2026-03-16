package org.joint.modules.system.menu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "菜单信息")
public class MenuVo {

    private String id;

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

    private LocalDateTime createdAt;

    private List<MenuVo> children;
}
