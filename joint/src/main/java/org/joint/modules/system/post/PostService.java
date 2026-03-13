package org.joint.modules.system.post;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.joint.common.exception.BusinessException;
import org.joint.modules.system.post.dto.CreatePostDto;
import org.joint.modules.system.post.dto.QueryPostDto;
import org.joint.modules.system.post.dto.UpdatePostDto;
import org.joint.modules.system.post.entity.Post;
import org.joint.modules.system.post.mapper.PostMapper;
import org.joint.modules.system.post.vo.PostVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;

    public Map<String, Object> findPage(QueryPostDto query) {
        Page<Post> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<Post> result = postMapper.selectPage(page, new LambdaQueryWrapper<Post>()
                .like(StringUtils.hasText(query.getPostCode()), Post::getPostCode, query.getPostCode())
                .like(StringUtils.hasText(query.getPostName()), Post::getPostName, query.getPostName())
                .eq(query.getStatus() != null, Post::getStatus, query.getStatus())
                .orderByAsc(Post::getPostSort));
        return Map.of(
                "list", result.getRecords().stream().map(this::toVo).toList(),
                "total", result.getTotal()
        );
    }

    public PostVo findById(String id) {
        return toVo(getExistingPost(id));
    }

    public PostVo create(CreatePostDto dto) {
        ensureCodeUnique(dto.getPostCode(), null);

        Post post = new Post();
        BeanUtils.copyProperties(dto, post);
        if (post.getPostSort() == null) {
            post.setPostSort(0);
        }
        if (post.getStatus() == null) {
            post.setStatus(0);
        }
        if (post.getRemark() == null) {
            post.setRemark("");
        }
        postMapper.insert(post);
        return toVo(post);
    }

    public PostVo update(String id, UpdatePostDto dto) {
        Post post = getExistingPost(id);
        if (dto.getPostCode() != null && !dto.getPostCode().equals(post.getPostCode())) {
            ensureCodeUnique(dto.getPostCode(), id);
            post.setPostCode(dto.getPostCode());
        }
        if (dto.getPostName() != null) {
            post.setPostName(dto.getPostName());
        }
        if (dto.getPostSort() != null) {
            post.setPostSort(dto.getPostSort());
        }
        if (dto.getStatus() != null) {
            post.setStatus(dto.getStatus());
        }
        if (dto.getRemark() != null) {
            post.setRemark(dto.getRemark());
        }

        postMapper.updateById(post);
        return toVo(post);
    }

    public Map<String, String> delete(String id) {
        getExistingPost(id);
        postMapper.deleteById(id);
        return Map.of("id", id);
    }

    public List<PostVo> findAllEnabled() {
        return postMapper.selectList(new LambdaQueryWrapper<Post>()
                        .eq(Post::getStatus, 0)
                        .orderByAsc(Post::getPostSort))
                .stream()
                .map(this::toVo)
                .toList();
    }

    private Post getExistingPost(String id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("岗位不存在");
        }
        return post;
    }

    private void ensureCodeUnique(String postCode, String excludeId) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>().eq(Post::getPostCode, postCode);
        if (excludeId != null) {
            wrapper.ne(Post::getId, excludeId);
        }
        if (postMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("岗位编码已存在");
        }
    }

    private PostVo toVo(Post post) {
        PostVo vo = new PostVo();
        vo.setId(post.getId());
        vo.setPostCode(post.getPostCode());
        vo.setPostName(post.getPostName());
        vo.setPostSort(post.getPostSort());
        vo.setStatus(post.getStatus());
        vo.setRemark(post.getRemark() == null ? "" : post.getRemark());
        vo.setCreateTime(formatDateTime(post.getCreatedAt()));
        return vo;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toString();
    }
}
