package org.joint.modules.system.operlog;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.joint.modules.system.operlog.dto.QueryOperLogDto;
import org.joint.modules.system.operlog.entity.OperLog;
import org.joint.modules.system.operlog.mapper.OperLogMapper;
import org.joint.modules.system.operlog.vo.OperLogVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OperLogService {

    private final OperLogMapper operLogMapper;

    public Map<String, Object> findPage(QueryOperLogDto query) {
        Page<OperLog> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<OperLog> wrapper = new LambdaQueryWrapper<OperLog>()
                .like(StringUtils.hasText(query.getTitle()), OperLog::getTitle, query.getTitle())
                .like(StringUtils.hasText(query.getOperName()), OperLog::getOperName, query.getOperName())
                .eq(query.getBusinessType() != null, OperLog::getBusinessType, query.getBusinessType())
                .eq(query.getStatus() != null, OperLog::getStatus, query.getStatus());
        if (StringUtils.hasText(query.getBeginTime())) {
            wrapper.ge(OperLog::getOperTime, parseDateTime(query.getBeginTime()));
        }
        if (StringUtils.hasText(query.getEndTime())) {
            wrapper.le(OperLog::getOperTime, parseDateTime(query.getEndTime()));
        }
        wrapper.orderByDesc(OperLog::getOperTime);
        Page<OperLog> result = operLogMapper.selectPage(page, wrapper);
        return Map.of(
                "list", result.getRecords().stream().map(this::toVo).toList(),
                "total", result.getTotal()
        );
    }

    public OperLogVo findById(String id) {
        OperLog operLog = operLogMapper.selectById(id);
        if (operLog == null) {
            return null;
        }
        return toVo(operLog);
    }

    public Map<String, Integer> remove(List<String> ids) {
        int deletedCount = operLogMapper.delete(new LambdaQueryWrapper<OperLog>().in(OperLog::getId, ids));
        return Map.of("deletedCount", deletedCount);
    }

    public Map<String, Integer> clean() {
        int deletedCount = operLogMapper.delete(null);
        return Map.of("deletedCount", deletedCount);
    }

    private OperLogVo toVo(OperLog operLog) {
        OperLogVo vo = new OperLogVo();
        vo.setId(operLog.getId());
        vo.setTitle(operLog.getTitle());
        vo.setBusinessType(operLog.getBusinessType());
        vo.setMethod(operLog.getMethod());
        vo.setRequestMethod(operLog.getRequestMethod());
        vo.setOperName(operLog.getOperName());
        vo.setDeptName(operLog.getDeptName());
        vo.setOperUrl(operLog.getOperUrl());
        vo.setOperIp(operLog.getOperIp());
        vo.setOperLocation(operLog.getOperLocation());
        vo.setOperParam(operLog.getOperParam());
        vo.setJsonResult(operLog.getJsonResult());
        vo.setStatus(operLog.getStatus());
        vo.setErrorMsg(operLog.getErrorMsg());
        vo.setCostTime(operLog.getCostTime());
        vo.setOperTime(formatDateTime(operLog.getOperTime()));
        return vo;
    }

    private LocalDateTime parseDateTime(String value) {
        return LocalDateTime.ofInstant(Instant.parse(value), ZoneId.systemDefault());
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
