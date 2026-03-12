package org.joint.modules.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joint.modules.system.user.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
