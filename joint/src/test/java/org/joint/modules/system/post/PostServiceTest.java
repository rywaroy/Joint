package org.joint.modules.system.post;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.joint.common.exception.BusinessException;
import org.joint.modules.system.post.dto.CreatePostDto;
import org.joint.modules.system.post.dto.QueryPostDto;
import org.joint.modules.system.post.dto.UpdatePostDto;
import org.joint.modules.system.post.entity.Post;
import org.joint.modules.system.post.mapper.PostMapper;
import org.joint.modules.system.post.vo.PostVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostServiceTest {

    private PostMapper postMapper;
    private PostService postService;

    @BeforeEach
    void setUp() {
        postMapper = mock(PostMapper.class);
        postService = new PostService(postMapper);
    }

    @Test
    void findPageReturnsPostVos() {
        Post post = new Post();
        post.setId("p-1");
        post.setPostCode("dev");
        post.setPostName("开发工程师");
        post.setPostSort(1);
        post.setStatus(0);

        Page<Post> page = new Page<>(1, 10);
        page.setRecords(List.of(post));
        page.setTotal(1);

        when(postMapper.selectPage(any(), any())).thenReturn(page);

        Map<String, Object> result = postService.findPage(new QueryPostDto());
        @SuppressWarnings("unchecked")
        List<PostVo> list = (List<PostVo>) result.get("list");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getPostCode()).isEqualTo("dev");
        assertThat(result.get("total")).isEqualTo(1L);
    }

    @Test
    void createRejectsDuplicateCode() {
        CreatePostDto dto = new CreatePostDto();
        dto.setPostCode("dev");
        dto.setPostName("开发工程师");

        when(postMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> postService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("岗位编码已存在");
    }

    @Test
    void updateRejectsDuplicateCodeWhenChanged() {
        Post post = new Post();
        post.setId("p-1");
        post.setPostCode("dev");

        UpdatePostDto dto = new UpdatePostDto();
        dto.setPostCode("qa");

        when(postMapper.selectById("p-1")).thenReturn(post);
        when(postMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> postService.update("p-1", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("岗位编码已存在");
    }

    @Test
    void deleteRemovesExistingPost() {
        Post post = new Post();
        post.setId("p-1");

        when(postMapper.selectById("p-1")).thenReturn(post);

        Map<String, String> result = postService.delete("p-1");

        verify(postMapper).deleteById("p-1");
        assertThat(result).containsEntry("id", "p-1");
    }

    @Test
    void findAllEnabledReturnsOptions() {
        Post post = new Post();
        post.setId("p-1");
        post.setPostCode("dev");
        post.setPostName("开发工程师");
        post.setStatus(0);

        when(postMapper.selectList(any())).thenReturn(List.of(post));

        List<PostVo> result = postService.findAllEnabled();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPostName()).isEqualTo("开发工程师");
        assertThat(result.get(0).getRemark()).isEqualTo("");
    }
}
