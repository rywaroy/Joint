package org.joint.modules.system.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.joint.modules.system.dept.vo.DeptVo;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "用户详情")
public class UserDetailVo {

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

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "角色ID列表")
    private List<String> roleIds;

    @Schema(description = "岗位ID列表")
    private List<String> postIds;

    @Schema(description = "部门信息")
    private DeptVo dept;
}
