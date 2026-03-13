package org.joint.modules.system.dept;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.joint.common.exception.BusinessException;
import org.joint.modules.system.dept.dto.CreateDeptDto;
import org.joint.modules.system.dept.dto.QueryDeptDto;
import org.joint.modules.system.dept.dto.UpdateDeptDto;
import org.joint.modules.system.dept.entity.Dept;
import org.joint.modules.system.dept.mapper.DeptMapper;
import org.joint.modules.system.dept.vo.DeptVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeptService {

    private final DeptMapper deptMapper;

    public List<DeptVo> findTree(QueryDeptDto query) {
        List<DeptVo> depts = deptMapper.selectList(new LambdaQueryWrapper<Dept>()
                        .like(StringUtils.hasText(query.getName()), Dept::getName, query.getName())
                        .eq(query.getStatus() != null, Dept::getStatus, query.getStatus())
                        .orderByAsc(Dept::getCreatedAt))
                .stream()
                .map(this::toVo)
                .toList();
        return buildTree(depts);
    }

    public DeptVo create(CreateDeptDto dto) {
        String pid = normalizePid(dto.getPid());
        validateParent(pid);
        ensureNameUnique(dto.getName(), pid, null);

        Dept dept = new Dept();
        dept.setName(dto.getName());
        dept.setParentId(pid);
        if (dept.getStatus() == null) {
            dept.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        }
        dept.setRemark(dto.getRemark());
        deptMapper.insert(dept);
        return toVo(getExistingDept(dept.getId()));
    }

    public DeptVo update(String id, UpdateDeptDto dto) {
        Dept dept = getExistingDept(id);

        String currentPid = normalizePid(dept.getParentId());
        String nextPid = dto.isPidSet() ? normalizePid(dto.getPid()) : currentPid;

        if (StringUtils.hasText(nextPid) && id.equals(nextPid)) {
            throw new BusinessException("父级部门不能是自己");
        }
        if (StringUtils.hasText(nextPid)) {
            validateParent(nextPid);
            if (isDescendant(id, nextPid)) {
                throw new BusinessException("不能将部门移动到其子级下");
            }
        }

        String targetName = dto.isNameSet() ? dto.getName() : dept.getName();
        if (dto.isNameSet() || dto.isPidSet()) {
            ensureNameUnique(targetName, nextPid, id);
        }

        if (dto.isNameSet()) {
            dept.setName(dto.getName());
        }
        if (dto.isPidSet()) {
            dept.setParentId(nextPid);
        }
        if (dto.isStatusSet()) {
            dept.setStatus(dto.getStatus());
        }
        if (dto.isRemarkSet()) {
            dept.setRemark(dto.getRemark());
        }

        deptMapper.updateById(dept);
        return toVo(dept);
    }

    public Map<String, String> delete(String id) {
        getExistingDept(id);
        Long childCount = deptMapper.selectCount(new LambdaQueryWrapper<Dept>().eq(Dept::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("存在子部门，无法删除");
        }
        deptMapper.deleteById(id);
        return Map.of("id", id);
    }

    private Dept getExistingDept(String id) {
        Dept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        return dept;
    }

    private void validateParent(String parentId) {
        if (!StringUtils.hasText(parentId)) {
            return;
        }
        if (deptMapper.selectById(parentId) == null) {
            throw new BusinessException("父级部门不存在");
        }
    }

    private boolean isDescendant(String parentId, String targetParentId) {
        String currentId = targetParentId;
        while (StringUtils.hasText(currentId)) {
            if (parentId.equals(currentId)) {
                return true;
            }
            Dept current = deptMapper.selectById(currentId);
            if (current == null) {
                break;
            }
            currentId = current.getParentId();
        }
        return false;
    }

    private void ensureNameUnique(String name, String parentId, String excludeId) {
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<Dept>().eq(Dept::getName, name);
        if (StringUtils.hasText(parentId)) {
            wrapper.eq(Dept::getParentId, parentId);
        } else {
            wrapper.isNull(Dept::getParentId);
        }
        if (excludeId != null) {
            wrapper.ne(Dept::getId, excludeId);
        }
        if (deptMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("同级部门名称已存在");
        }
    }

    private List<DeptVo> buildTree(List<DeptVo> depts) {
        Map<String, DeptVo> deptMap = new LinkedHashMap<>();
        for (DeptVo dept : depts) {
            deptMap.put(dept.getId(), dept);
        }

        List<DeptVo> roots = new ArrayList<>();
        for (DeptVo dept : depts) {
            if (!StringUtils.hasText(dept.getPid()) || !deptMap.containsKey(dept.getPid())) {
                roots.add(dept);
                continue;
            }

            DeptVo parent = deptMap.get(dept.getPid());
            if (parent.getChildren() == null) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(dept);
        }

        return roots;
    }

    private DeptVo toVo(Dept dept) {
        DeptVo vo = new DeptVo();
        vo.setId(dept.getId());
        vo.setPid(normalizePid(dept.getParentId()));
        vo.setName(dept.getName());
        vo.setStatus(dept.getStatus());
        vo.setRemark(dept.getRemark());
        vo.setCreateTime(formatDateTime(dept.getCreatedAt()));
        return vo;
    }

    private String normalizePid(String pid) {
        if (!StringUtils.hasText(pid)) {
            return null;
        }
        return pid;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toString();
    }
}
