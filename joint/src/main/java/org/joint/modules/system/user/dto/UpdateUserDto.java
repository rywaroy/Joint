package org.joint.modules.system.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.joint.common.annotation.Phone;

import java.util.List;

@Data
public class UpdateUserDto {

    private String nickName;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Phone
    private String phone;

    private String deptId;

    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;

    private String remark;

    private List<String> roleIds;

    private List<String> postIds;
}
