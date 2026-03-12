package org.joint.modules.system.operlog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.RequirePermission;
import org.joint.common.response.PageResult;
import org.joint.modules.system.operlog.dto.QueryOperLogDto;
import org.joint.modules.system.operlog.vo.OperLogVo;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/oper-log")
@RequiredArgsConstructor
@Tag(name = "操作日志")
public class OperLogController {

    private final OperLogService operLogService;

    @GetMapping("/list")
    @RequirePermission("system:operlog:list")
    @Operation(summary = "分页查询操作日志")
    public PageResult<OperLogVo> list(@ParameterObject QueryOperLogDto query) {
        return operLogService.findPage(query);
    }
}
