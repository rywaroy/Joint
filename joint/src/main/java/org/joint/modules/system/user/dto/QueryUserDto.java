package org.joint.modules.system.user.dto;

import lombok.Data;

@Data
public class QueryUserDto {
    private Integer page = 1;
    private Integer size = 10;
    private String username;
    private Integer status;
}
