# 4.6 岗位模块

## 学习目标

- 实现标准 CRUD 模块（巩固练习）
- 掌握编码唯一性校验
- 对照 Nexus post 模块实现

## 数据结构

```java
@Data
@TableName("sys_post")
public class Post {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String postCode;      // 岗位编码（唯一）
    private String postName;      // 岗位名称
    private Integer postSort;     // 排序
    private Integer status;       // 状态：0-正常 1-禁用
    private String remark;        // 备注
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## API 接口

| 方法 | 路径 | 描述 | 权限码 |
|------|------|------|--------|
| GET | `/system/post/list` | 岗位列表（分页） | `system:post:list` |
| GET | `/system/post/options` | 岗位选项（下拉框） | 仅需登录 |
| GET | `/system/post/{id}` | 岗位详情 | `system:post:query` |
| POST | `/system/post` | 创建岗位 | `system:post:create` |
| PUT | `/system/post/{id}` | 更新岗位 | `system:post:update` |
| DELETE | `/system/post/{id}` | 删除岗位 | `system:post:delete` |

## 实现代码

### DTO 定义

```java
// CreatePostDto.java
@Data
public class CreatePostDto {

    @NotBlank(message = "岗位编码不能为空")
    private String postCode;

    @NotBlank(message = "岗位名称不能为空")
    private String postName;

    private Integer postSort;

    private Integer status;

    private String remark;
}

// UpdatePostDto.java
@Data
public class UpdatePostDto {
    private String postCode;
    private String postName;
    private Integer postSort;
    private Integer status;
    private String remark;
}

// QueryPostDto.java
@Data
public class QueryPostDto {
    private Integer page = 1;
    private Integer size = 10;
    private String postCode;
    private String postName;
    private Integer status;
}
```

### VO 定义

```java
@Data
public class PostVo {
    private String id;
    private String postCode;
    private String postName;
    private Integer postSort;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
}
```

### Service 实现

```java
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;

    @Override
    public PageResult<PostVo> findPage(QueryPostDto query) {
        Page<Post> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getPostCode()),
                     Post::getPostCode, query.getPostCode())
               .like(StringUtils.hasText(query.getPostName()),
                     Post::getPostName, query.getPostName())
               .eq(query.getStatus() != null, Post::getStatus, query.getStatus())
               .orderByAsc(Post::getPostSort);

        IPage<Post> result = postMapper.selectPage(page, wrapper);

        List<PostVo> voList = result.getRecords().stream()
                .map(this::toVo)
                .toList();

        return PageResult.of(result.getTotal(), voList);
    }

    @Override
    public PostVo findById(String id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("岗位不存在");
        }
        return toVo(post);
    }

    @Override
    public PostVo create(CreatePostDto dto) {
        // 检查编码唯一性
        if (existsByCode(dto.getPostCode())) {
            throw new BusinessException("岗位编码已存在");
        }

        Post post = new Post();
        BeanUtils.copyProperties(dto, post);
        if (post.getPostSort() == null) post.setPostSort(0);
        if (post.getStatus() == null) post.setStatus(0);
        postMapper.insert(post);
        return toVo(post);
    }

    @Override
    public PostVo update(String id, UpdatePostDto dto) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("岗位不存在");
        }

        // 编码变更时检查唯一性
        if (dto.getPostCode() != null && !dto.getPostCode().equals(post.getPostCode())) {
            if (existsByCode(dto.getPostCode())) {
                throw new BusinessException("岗位编码已存在");
            }
            post.setPostCode(dto.getPostCode());
        }

        if (dto.getPostName() != null) post.setPostName(dto.getPostName());
        if (dto.getPostSort() != null) post.setPostSort(dto.getPostSort());
        if (dto.getStatus() != null) post.setStatus(dto.getStatus());
        if (dto.getRemark() != null) post.setRemark(dto.getRemark());

        postMapper.updateById(post);
        return toVo(post);
    }

    @Override
    public void delete(String id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("岗位不存在");
        }
        postMapper.deleteById(id);
    }

    @Override
    public List<PostVo> findAllEnabled() {
        List<Post> posts = postMapper.selectList(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getStatus, 0)
                        .orderByAsc(Post::getPostSort));
        return posts.stream().map(this::toVo).toList();
    }

    private boolean existsByCode(String postCode) {
        return postMapper.selectCount(
                new LambdaQueryWrapper<Post>().eq(Post::getPostCode, postCode)) > 0;
    }

    private PostVo toVo(Post post) {
        PostVo vo = new PostVo();
        BeanUtils.copyProperties(post, vo);
        return vo;
    }
}
```

### Controller 实现

```java
@RestController
@RequestMapping("/system/post")
@RequiredArgsConstructor
@Tag(name = "岗位管理")
public class PostController {

    private final PostService postService;

    @GetMapping("/list")
    @Operation(summary = "获取岗位列表")
    @RequirePermission("system:post:list")
    public PageResult<PostVo> list(QueryPostDto query) {
        return postService.findPage(query);
    }

    @GetMapping("/options")
    @Operation(summary = "获取岗位选项")
    public List<PostVo> options() {
        return postService.findAllEnabled();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取岗位详情")
    @RequirePermission("system:post:query")
    public PostVo getById(@PathVariable String id) {
        return postService.findById(id);
    }

    @PostMapping
    @Operation(summary = "创建岗位")
    @RequirePermission("system:post:create")
    public PostVo create(@Valid @RequestBody CreatePostDto dto) {
        return postService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新岗位")
    @RequirePermission("system:post:update")
    public PostVo update(@PathVariable String id,
                         @Valid @RequestBody UpdatePostDto dto) {
        return postService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除岗位")
    @RequirePermission("system:post:delete")
    public void delete(@PathVariable String id) {
        postService.delete(id);
    }
}
```

## 与 Nexus 对照

| Nexus | Joint |
|-------|-------|
| `prisma.post.findUnique({ where: { postCode } })` | `selectCount()` 检查唯一性 |
| `prisma.post.findMany({ orderBy: { postSort: 'asc' } })` | `orderByAsc(Post::getPostSort)` |
| `NOT: { id }` 排除自身 | `ne(Post::getId, id)` |

## 知识点总结

| 概念 | 说明 |
|------|------|
| 编码唯一性 | postCode 字段全局唯一 |
| 排序字段 | postSort 控制展示顺序 |
| 选项接口 | options 返回启用的岗位列表供下拉框使用 |

## 练习任务

1. 独立完成岗位模块全部接口（不参考其他模块代码）
2. 测试编码唯一性校验
3. 回顾 4.2-4.5 模块，总结 CRUD 开发模式
