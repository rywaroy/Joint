package org.joint.modules.system.dept;

import org.joint.common.exception.BusinessException;
import org.joint.modules.system.dept.dto.CreateDeptDto;
import org.joint.modules.system.dept.dto.QueryDeptDto;
import org.joint.modules.system.dept.dto.UpdateDeptDto;
import org.joint.modules.system.dept.entity.Dept;
import org.joint.modules.system.dept.mapper.DeptMapper;
import org.joint.modules.system.dept.vo.DeptVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeptServiceTest {

    private DeptMapper deptMapper;
    private DeptService deptService;

    @BeforeEach
    void setUp() {
        deptMapper = mock(DeptMapper.class);
        deptService = new DeptService(deptMapper);
    }

    @Test
    void findTreeBuildsHierarchyFromFlatRows() {
        Dept root = new Dept();
        root.setId("d-root");
        root.setName("总部");

        Dept child = new Dept();
        child.setId("d-child");
        child.setParentId("d-root");
        child.setName("研发部");

        when(deptMapper.selectList(any())).thenReturn(List.of(root, child));

        List<DeptVo> result = deptService.findTree(new QueryDeptDto());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("d-root");
        assertThat(result.get(0).getChildren()).hasSize(1);
        assertThat(result.get(0).getChildren().get(0).getId()).isEqualTo("d-child");
    }

    @Test
    void createRejectsDuplicateSiblingName() {
        CreateDeptDto dto = new CreateDeptDto();
        dto.setName("研发部");

        when(deptMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> deptService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("同级下已存在相同名称的部门");
    }

    @Test
    void updateRejectsParentCycle() {
        Dept root = new Dept();
        root.setId("d-root");
        root.setParentId(null);

        Dept child = new Dept();
        child.setId("d-child");
        child.setParentId("d-root");

        UpdateDeptDto dto = new UpdateDeptDto();
        dto.setParentId("d-child");

        when(deptMapper.selectById("d-root")).thenReturn(root);
        when(deptMapper.selectById("d-child")).thenReturn(child);

        assertThatThrownBy(() -> deptService.update("d-root", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("不能将子部门设为父级");
    }

    @Test
    void deleteRejectsDeptWithChildren() {
        Dept dept = new Dept();
        dept.setId("d-root");

        when(deptMapper.selectById("d-root")).thenReturn(dept);
        when(deptMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> deptService.delete("d-root"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("请先删除子部门");

        verify(deptMapper, never()).deleteById("d-root");
    }
}
