package org.joint.modules.system.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.joint.common.annotation.Phone;

@Data
public class CreateUserDto {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度为3-20个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickName;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Phone
    private String phone;

    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;
}
