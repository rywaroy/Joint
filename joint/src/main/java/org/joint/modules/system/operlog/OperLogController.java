package org.joint.modules.system.operlog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.Log;
import org.joint.common.annotation.RequirePermission;
import org.joint.common.enums.BusinessType;
import org.joint.modules.system.operlog.dto.QueryOperLogDto;
import org.joint.modules.system.operlog.vo.OperLogVo;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/system/log")
@RequiredArgsConstructor
@Tag(name = "操作日志")
public class OperLogController {

    private final OperLogService operLogService;

    @GetMapping("/list")
    @RequirePermission("system:log:list")
    @Operation(summary = "分页查询操作日志")
    public Map<String, Object> list(@ParameterObject QueryOperLogDto query) {
        return operLogService.findPage(query);
    }

    @GetMapping("/{id}")
    @RequirePermission("system:log:query")
    @Operation(summary = "查询操作日志详情")
    public OperLogVo getById(@PathVariable String id) {
        return operLogService.findById(id);
    }

    @DeleteMapping("/clean")
    @RequirePermission("system:log:delete")
    @Operation(summary = "清空操作日志")
    public Map<String, Integer> clean() {
        return operLogService.clean();
    }

    @DeleteMapping("/{ids}")
    @RequirePermission("system:log:delete")
    @Log(module = "操作日志管理", type = BusinessType.DELETE, description = "删除操作日志")
    @Operation(summary = "删除操作日志")
    public Map<String, Integer> delete(@PathVariable String ids) {
        return operLogService.remove(Arrays.stream(ids.split(",")).toList());
    }
}
