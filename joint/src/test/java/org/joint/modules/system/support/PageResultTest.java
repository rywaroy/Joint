package org.joint.modules.system.support;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.joint.common.response.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResultTest {

    @Test
    void ofCopiesPagedMetadata() {
        Page<String> page = new Page<>(2, 5);
        page.setRecords(List.of("a", "b"));
        page.setTotal(11);

        PageResult<String> result = PageResult.of(page);

        assertThat(result.getData()).containsExactly("a", "b");
        assertThat(result.getTotal()).isEqualTo(11);
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(5);
    }

    @Test
    void ofMapsRecordsIntoAnotherType() {
        Page<Integer> page = new Page<>(1, 10);
        page.setRecords(List.of(1, 2, 3));
        page.setTotal(3);

        PageResult<String> result = PageResult.of(page, value -> "n-" + value);

        assertThat(result.getData()).containsExactly("n-1", "n-2", "n-3");
        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    void ofUsesProvidedTotalAndData() {
        PageResult<String> result = PageResult.of(7L, List.of("left", "right"));

        assertThat(result.getData()).containsExactly("left", "right");
        assertThat(result.getTotal()).isEqualTo(7);
        assertThat(result.getPage()).isNull();
        assertThat(result.getSize()).isNull();
    }
}
