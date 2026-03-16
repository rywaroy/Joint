package org.joint.modules.system.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "用户列表项")
public class UserVo {

    @Schema(description = "用户ID")
    private String id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "部门ID")
    private String deptId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "角色名称列表")
    private List<String> roles;

    @Schema(description = "岗位ID列表")
    private List<String> postIds;

    @Schema(description = "创建时间")
    private String createTime;
}
