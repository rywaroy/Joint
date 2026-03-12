package org.joint.modules.system.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.joint.common.annotation.Phone;

import java.util.List;

@Data
@Schema(description = "创建用户请求")
public class CreateUserDto {

    @Schema(description = "用户名", example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度为3-20个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String password;

    @Schema(description = "昵称", example = "张三")
    @NotBlank(message = "昵称不能为空")
    private String nickName;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    @Phone
    private String phone;

    @Schema(description = "状态 0-正常 1-禁用", example = "0")
    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;

    @Schema(description = "所属部门ID")
    private String deptId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "角色ID列表")
    private List<String> roleIds;

    @Schema(description = "岗位ID列表")
    private List<String> postIds;
}
