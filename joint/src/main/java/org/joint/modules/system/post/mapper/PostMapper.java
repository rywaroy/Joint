package org.joint.modules.system.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joint.modules.system.post.entity.Post;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}
