package org.joint.modules.system.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.joint.common.annotation.Phone;

import java.util.List;

@Data
@Schema(description = "更新用户请求")
public class UpdateUserDto {

    @Schema(description = "昵称", example = "张三")
    private String nickName;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    @Phone
    private String phone;

    @Schema(description = "所属部门ID")
    private String deptId;

    @Schema(description = "状态 0-正常 1-禁用", example = "0")
    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "角色ID列表")
    private List<String> roleIds;

    @Schema(description = "岗位ID列表")
    private List<String> postIds;
}
