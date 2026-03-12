package org.joint.common.response;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

@Data
@Schema(description = "分页响应")
public class PageResult<T> {

    @Schema(description = "当前页数据")
    private List<T> data;

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Long page;

    @Schema(description = "每页大小", example = "10")
    private Long size;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setData(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPage(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    public static <S, T> PageResult<T> of(IPage<S> page, Function<S, T> mapper) {
        PageResult<T> result = new PageResult<>();
        result.setData(page.getRecords().stream().map(mapper).toList());
        result.setTotal(page.getTotal());
        result.setPage(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    public static <T> PageResult<T> of(Long total, List<T> data) {
        PageResult<T> result = new PageResult<>();
        result.setData(data);
        result.setTotal(total);
        return result;
    }
}
