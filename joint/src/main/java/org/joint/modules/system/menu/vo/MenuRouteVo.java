package org.joint.modules.system.menu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "动态路由")
public class MenuRouteVo {

    @Schema(description = "路由名称")
    private String name;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "重定向路径")
    private String redirect;

    @Schema(description = "路由元数据")
    private Map<String, Object> meta;

    @Schema(description = "子路由")
    private List<MenuRouteVo> children;
}
