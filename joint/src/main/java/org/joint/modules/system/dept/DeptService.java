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
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
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
                        .orderByAsc(Dept::getSort)
                        .orderByDesc(Dept::getCreatedAt))
                .stream()
                .map(this::toVo)
                .sorted(deptComparator())
                .toList();
        return buildTree(depts);
    }

    public DeptVo create(CreateDeptDto dto) {
        validateParent(dto.getParentId());
        ensureNameUnique(dto.getName(), dto.getParentId(), null);

        Dept dept = new Dept();
        BeanUtils.copyProperties(dto, dept);
        if (dept.getSort() == null) {
            dept.setSort(0);
        }
        if (dept.getStatus() == null) {
            dept.setStatus(0);
        }
        deptMapper.insert(dept);
        return toVo(dept);
    }

    public DeptVo update(String id, UpdateDeptDto dto) {
        Dept dept = getExistingDept(id);
        if (id.equals(dto.getParentId())) {
            throw new BusinessException("不能将自己设为父级");
        }
        if (StringUtils.hasText(dto.getParentId())) {
            validateParent(dto.getParentId());
            if (isDescendant(id, dto.getParentId())) {
                throw new BusinessException("不能将子部门设为父级");
            }
        }

        String targetName = dto.getName() != null ? dto.getName() : dept.getName();
        String targetParentId = dto.getParentId() != null ? dto.getParentId() : dept.getParentId();
        if (dto.getName() != null || dto.getParentId() != null) {
            ensureNameUnique(targetName, targetParentId, id);
        }

        if (dto.getName() != null) {
            dept.setName(dto.getName());
        }
        if (dto.getParentId() != null) {
            dept.setParentId(dto.getParentId());
        }
        if (dto.getSort() != null) {
            dept.setSort(dto.getSort());
        }
        if (dto.getLeader() != null) {
            dept.setLeader(dto.getLeader());
        }
        if (dto.getPhone() != null) {
            dept.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            dept.setEmail(dto.getEmail());
        }
        if (dto.getStatus() != null) {
            dept.setStatus(dto.getStatus());
        }

        deptMapper.updateById(dept);
        return toVo(dept);
    }

    public void delete(String id) {
        getExistingDept(id);
        Long childCount = deptMapper.selectCount(new LambdaQueryWrapper<Dept>().eq(Dept::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("请先删除子部门");
        }
        deptMapper.deleteById(id);
    }

    private Dept getExistingDept(String id) {
        Dept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        return dept;
    }

    private void validateParent(String parentId) {
        if (!StringUtils.hasText(parentId) || "0".equals(parentId)) {
            return;
        }
        if (deptMapper.selectById(parentId) == null) {
            throw new BusinessException("父级部门不存在");
        }
    }

    private boolean isDescendant(String parentId, String targetParentId) {
        String currentId = targetParentId;
        while (StringUtils.hasText(currentId) && !"0".equals(currentId)) {
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
        if (StringUtils.hasText(parentId) && !"0".equals(parentId)) {
            wrapper.eq(Dept::getParentId, parentId);
        } else {
            wrapper.and(w -> w.isNull(Dept::getParentId).or().eq(Dept::getParentId, ""));
        }
        if (excludeId != null) {
            wrapper.ne(Dept::getId, excludeId);
        }
        if (deptMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("同级下已存在相同名称的部门");
        }
    }

    private List<DeptVo> buildTree(List<DeptVo> depts) {
        Map<String, DeptVo> deptMap = new LinkedHashMap<>();
        for (DeptVo dept : depts) {
            deptMap.put(dept.getId(), dept);
        }

        List<DeptVo> roots = new ArrayList<>();
        for (DeptVo dept : depts) {
            if (!StringUtils.hasText(dept.getParentId()) || "0".equals(dept.getParentId()) || !deptMap.containsKey(dept.getParentId())) {
                roots.add(dept);
                continue;
            }

            DeptVo parent = deptMap.get(dept.getParentId());
            if (parent.getChildren() == null) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(dept);
            parent.getChildren().sort(deptComparator());
        }

        roots.sort(deptComparator());
        return roots;
    }

    private Comparator<DeptVo> deptComparator() {
        return Comparator.comparing(DeptVo::getSort, Comparator.nullsFirst(Integer::compareTo))
                .thenComparing(DeptVo::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder()));
    }

    private DeptVo toVo(Dept dept) {
        DeptVo vo = new DeptVo();
        BeanUtils.copyProperties(dept, vo);
        return vo;
    }
}
