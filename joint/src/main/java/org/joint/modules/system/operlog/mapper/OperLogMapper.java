package org.joint.modules.system.operlog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joint.modules.system.operlog.entity.OperLog;

@Mapper
public interface OperLogMapper extends BaseMapper<OperLog> {
}
