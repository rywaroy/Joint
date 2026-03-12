package org.joint.modules.system.post;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.RequirePermission;
import org.joint.common.response.PageResult;
import org.joint.modules.system.post.dto.CreatePostDto;
import org.joint.modules.system.post.dto.QueryPostDto;
import org.joint.modules.system.post.dto.UpdatePostDto;
import org.joint.modules.system.post.vo.PostVo;
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
@RequestMapping("/system/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/list")
    @RequirePermission("system:post:list")
    public PageResult<PostVo> list(QueryPostDto query) {
        return postService.findPage(query);
    }

    @GetMapping("/options")
    public List<PostVo> options() {
        return postService.findAllEnabled();
    }

    @GetMapping("/{id}")
    @RequirePermission("system:post:query")
    public PostVo getById(@PathVariable String id) {
        return postService.findById(id);
    }

    @PostMapping
    @RequirePermission("system:post:create")
    public PostVo create(@Valid @RequestBody CreatePostDto dto) {
        return postService.create(dto);
    }

    @PutMapping("/{id}")
    @RequirePermission("system:post:update")
    public PostVo update(@PathVariable String id, @Valid @RequestBody UpdatePostDto dto) {
        return postService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:post:delete")
    public void delete(@PathVariable String id) {
        postService.delete(id);
    }
}
