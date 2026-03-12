package org.joint.common.response;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

@Data
public class PageResult<T> {

    private List<T> data;
    private Long total;
    private Long page;
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
