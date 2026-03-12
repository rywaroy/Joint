package org.joint.modules.system.dept;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.RequirePermission;
import org.joint.modules.system.dept.dto.CreateDeptDto;
import org.joint.modules.system.dept.dto.QueryDeptDto;
import org.joint.modules.system.dept.dto.UpdateDeptDto;
import org.joint.modules.system.dept.vo.DeptVo;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    @GetMapping("/list")
    @RequirePermission("system:dept:list")
    public List<DeptVo> list(QueryDeptDto query) {
        return deptService.findTree(query);
    }

    @PostMapping
    @RequirePermission("system:dept:create")
    public DeptVo create(@Valid @RequestBody CreateDeptDto dto) {
        return deptService.create(dto);
    }

    @PutMapping("/{id}")
    @RequirePermission("system:dept:update")
    public DeptVo update(@PathVariable String id, @Valid @RequestBody UpdateDeptDto dto) {
        return deptService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:dept:delete")
    public void delete(@PathVariable String id) {
        deptService.delete(id);
    }
}
