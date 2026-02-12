# 4.5 部门模块

## 学习目标

- 实现树形结构的部门 CRUD
- 处理层级关系和循环引用检测
- 对照 Nexus dept 模块实现

## 数据结构

```java
@Data
@TableName("sys_dept")
public class Dept {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String name;          // 部门名称
    private String pid;           // 父级 ID，顶级为 null
    private Integer status;       // 状态：0-正常 1-禁用
    private String remark;        // 备注
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## API 接口

| 方法 | 路径 | 描述 | 权限码 |
|------|------|------|--------|
| GET | `/system/dept/list` | 部门树 | `system:dept:list` |
| POST | `/system/dept` | 创建部门 | `system:dept:create` |
| PUT | `/system/dept/{id}` | 更新部门 | `system:dept:update` |
| DELETE | `/system/dept/{id}` | 删除部门 | `system:dept:delete` |

## 实现代码

### DTO 定义

```java
// CreateDeptDto.java
@Data
public class CreateDeptDto {

    @NotBlank(message = "部门名称不能为空")
    @Size(min = 2, max = 50, message = "部门名称长度为2-50个字符")
    private String name;

    private String pid;        // 父级 ID

    private Integer status;

    @Size(max = 200, message = "备注不超过200个字符")
    private String remark;
}

// UpdateDeptDto.java
@Data
public class UpdateDeptDto {
    @Size(min = 2, max = 50, message = "部门名称长度为2-50个字符")
    private String name;
    private String pid;
    private Integer status;
    @Size(max = 200, message = "备注不超过200个字符")
    private String remark;
}

// QueryDeptDto.java
@Data
public class QueryDeptDto {
    private String name;
    private Integer status;
}
```

### VO 定义

```java
@Data
public class DeptVo {
    private String id;
    private String pid;
    private String name;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
    private List<DeptVo> children;  // 子部门
}
```

### Service 实现

```java
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptMapper deptMapper;

    @Override
    public List<DeptVo> findTree(QueryDeptDto query) {
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getName()), Dept::getName, query.getName())
               .eq(query.getStatus() != null, Dept::getStatus, query.getStatus())
               .orderByAsc(Dept::getCreatedAt);

        List<Dept> depts = deptMapper.selectList(wrapper);
        List<DeptVo> voList = depts.stream().map(this::toVo).toList();

        return buildTree(voList);
    }

    @Override
    @Transactional
    public DeptVo create(CreateDeptDto dto) {
        // 验证父级存在
        if (StringUtils.hasText(dto.getPid())) {
            Dept parent = deptMapper.selectById(dto.getPid());
            if (parent == null) {
                throw new BusinessException("父级部门不存在");
            }
        }

        // 同级名称唯一性检查
        ensureNameUnique(dto.getName(), dto.getPid(), null);

        Dept dept = new Dept();
        BeanUtils.copyProperties(dto, dept);
        deptMapper.insert(dept);
        return toVo(dept);
    }

    @Override
    @Transactional
    public DeptVo update(String id, UpdateDeptDto dto) {
        Dept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }

        // 不能将自己设为父级
        if (id.equals(dto.getPid())) {
            throw new BusinessException("不能将自己设为父级部门");
        }

        // 不能将子部门设为父级（循环引用检测）
        if (StringUtils.hasText(dto.getPid()) && isDescendant(id, dto.getPid())) {
            throw new BusinessException("不能将子部门设为父级");
        }

        // 名称唯一性检查
        String targetPid = dto.getPid() != null ? dto.getPid() : dept.getPid();
        if (dto.getName() != null) {
            ensureNameUnique(dto.getName(), targetPid, id);
        }

        if (dto.getName() != null) dept.setName(dto.getName());
        if (dto.getPid() != null) dept.setPid(dto.getPid());
        if (dto.getStatus() != null) dept.setStatus(dto.getStatus());
        if (dto.getRemark() != null) dept.setRemark(dto.getRemark());

        deptMapper.updateById(dept);
        return toVo(dept);
    }

    @Override
    @Transactional
    public void delete(String id) {
        Dept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }

        // 检查是否有子部门
        Long childCount = deptMapper.selectCount(
                new LambdaQueryWrapper<Dept>().eq(Dept::getPid, id));
        if (childCount > 0) {
            throw new BusinessException("请先删除子部门");
        }

        deptMapper.deleteById(id);
    }

    // ========== 私有方法 ==========

    /**
     * 检查 targetId 是否为 parentId 的后代节点
     * 通过从 targetId 向上遍历祖先链实现
     */
    private boolean isDescendant(String parentId, String targetId) {
        String currentId = targetId;
        while (currentId != null) {
            if (currentId.equals(parentId)) {
                return true;
            }
            Dept current = deptMapper.selectById(currentId);
            if (current == null) break;
            currentId = current.getPid();
        }
        return false;
    }

    /**
     * 同级部门名称唯一性检查
     */
    private void ensureNameUnique(String name, String pid, String excludeId) {
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dept::getName, name);

        if (StringUtils.hasText(pid)) {
            wrapper.eq(Dept::getPid, pid);
        } else {
            wrapper.isNull(Dept::getPid);
        }

        if (excludeId != null) {
            wrapper.ne(Dept::getId, excludeId);
        }

        if (deptMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("同级下已存在相同名称的部门");
        }
    }

    /**
     * 构建树形结构
     */
    private List<DeptVo> buildTree(List<DeptVo> depts) {
        Map<String, DeptVo> map = depts.stream()
                .collect(Collectors.toMap(DeptVo::getId, v -> v));

        List<DeptVo> roots = new ArrayList<>();

        for (DeptVo dept : depts) {
            if (!StringUtils.hasText(dept.getPid()) || !map.containsKey(dept.getPid())) {
                roots.add(dept);
            } else {
                DeptVo parent = map.get(dept.getPid());
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(dept);
            }
        }

        return roots;
    }

    private DeptVo toVo(Dept dept) {
        DeptVo vo = new DeptVo();
        BeanUtils.copyProperties(dept, vo);
        return vo;
    }
}
```

### Controller 实现

```java
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
@Tag(name = "部门管理")
public class DeptController {

    private final DeptService deptService;

    @GetMapping("/list")
    @Operation(summary = "获取部门树")
    @RequirePermission("system:dept:list")
    public List<DeptVo> list(QueryDeptDto query) {
        return deptService.findTree(query);
    }

    @PostMapping
    @Operation(summary = "创建部门")
    @RequirePermission("system:dept:create")
    public DeptVo create(@Valid @RequestBody CreateDeptDto dto) {
        return deptService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新部门")
    @RequirePermission("system:dept:update")
    public DeptVo update(@PathVariable String id,
                         @Valid @RequestBody UpdateDeptDto dto) {
        return deptService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门")
    @RequirePermission("system:dept:delete")
    public void delete(@PathVariable String id) {
        deptService.delete(id);
    }
}
```

## 树构建算法对比

4.4 菜单模块用**递归**构建树，本模块用 **Map + 单次遍历**构建树，两种方式对比：

| 方式 | 优点 | 缺点 |
|------|------|------|
| 递归 (菜单模块) | 代码直观 | 数据量大时效率低 |
| Map 遍历 (本模块) | O(n) 时间复杂度 | 代码稍长 |

## 与 Nexus 对照

| Nexus | Joint |
|-------|-------|
| `prisma.dept.findMany()` + `buildTree()` | `deptMapper.selectList()` + `buildTree()` |
| `@@unique([pid, name])` Prisma 约束 | `ensureNameUnique()` 手动校验 |
| `isDescendant()` 祖先链遍历 | 同样的祖先链遍历 |

## 知识点总结

| 概念 | 说明 |
|------|------|
| 树形结构 | pid 字段实现父子关系 |
| 循环引用检测 | 祖先链向上遍历 |
| 同级唯一性 | 同一父级下名称不重复 |
| Map 建树 | 用 HashMap 一次遍历构建树 |

## 练习任务

1. 实现部门树 CRUD
2. 验证循环引用检测逻辑
3. 测试同级名称唯一性约束
4. 对比菜单模块的递归建树方式
