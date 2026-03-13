package org.joint.modules.system.post;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.Log;
import org.joint.common.annotation.RequirePermission;
import org.joint.common.enums.BusinessType;
import org.joint.modules.system.post.dto.CreatePostDto;
import org.joint.modules.system.post.dto.QueryPostDto;
import org.joint.modules.system.post.dto.UpdatePostDto;
import org.joint.modules.system.post.vo.PostVo;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/post")
@RequiredArgsConstructor
@Tag(name = "岗位管理")
public class PostController {

    private final PostService postService;

    @GetMapping("/list")
    @RequirePermission("system:post:list")
    @Operation(summary = "分页查询岗位")
    public Map<String, Object> list(@Valid @ParameterObject QueryPostDto query) {
        return postService.findPage(query);
    }

    @GetMapping("/options")
    @Operation(summary = "获取岗位选项")
    public List<PostVo> options() {
        return postService.findAllEnabled();
    }

    @GetMapping("/{id}")
    @RequirePermission("system:post:query")
    @Operation(summary = "查询岗位详情")
    public PostVo getById(@PathVariable String id) {
        return postService.findById(id);
    }

    @PostMapping
    @RequirePermission("system:post:create")
    @Log(module = "岗位管理", type = BusinessType.INSERT, description = "创建岗位")
    @Operation(summary = "创建岗位")
    public PostVo create(@Valid @RequestBody CreatePostDto dto) {
        return postService.create(dto);
    }

    @PutMapping("/{id}")
    @RequirePermission("system:post:update")
    @Log(module = "岗位管理", type = BusinessType.UPDATE, description = "更新岗位")
    @Operation(summary = "更新岗位")
    public PostVo update(@PathVariable String id, @Valid @RequestBody UpdatePostDto dto) {
        return postService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:post:delete")
    @Log(module = "岗位管理", type = BusinessType.DELETE, description = "删除岗位")
    @Operation(summary = "删除岗位")
    public Map<String, String> delete(@PathVariable String id) {
        return postService.delete(id);
    }
}
