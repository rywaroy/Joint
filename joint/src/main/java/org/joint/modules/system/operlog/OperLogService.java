package org.joint.modules.system.operlog;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.joint.common.response.PageResult;
import org.joint.modules.system.operlog.dto.QueryOperLogDto;
import org.joint.modules.system.operlog.entity.OperLog;
import org.joint.modules.system.operlog.mapper.OperLogMapper;
import org.joint.modules.system.operlog.vo.OperLogVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class OperLogService {

    private final OperLogMapper operLogMapper;

    public PageResult<OperLogVo> findPage(QueryOperLogDto query) {
        Page<OperLog> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<OperLog> wrapper = new LambdaQueryWrapper<OperLog>()
                .like(StringUtils.hasText(query.getModule()), OperLog::getModule, query.getModule())
                .eq(StringUtils.hasText(query.getBusinessType()), OperLog::getBusinessType, query.getBusinessType())
                .eq(query.getStatus() != null, OperLog::getStatus, query.getStatus())
                .like(StringUtils.hasText(query.getOperatorName()), OperLog::getOperatorName, query.getOperatorName())
                .orderByDesc(OperLog::getOperateTime);
        IPage<OperLog> result = operLogMapper.selectPage(page, wrapper);
        return PageResult.of(result, this::toVo);
    }

    private OperLogVo toVo(OperLog operLog) {
        OperLogVo operLogVo = new OperLogVo();
        BeanUtils.copyProperties(operLog, operLogVo);
        return operLogVo;
    }
}
