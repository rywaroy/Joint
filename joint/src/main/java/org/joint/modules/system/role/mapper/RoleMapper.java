package org.joint.modules.system.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joint.modules.system.role.entity.Role;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
