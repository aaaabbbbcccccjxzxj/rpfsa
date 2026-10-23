package com.erdongj.pojo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description: 存储分页查询
 * @author: Erdong J
 * @create: 2023-03-17 20:53
 **/
@Data
@NoArgsConstructor
public class Pagination<T> {

    /** 当前页码 */
    private long current;
    /** 当前页数据 */
    private List<T> records;
    /** 总页数 */
    private long pages;
    /** 记录总数 */
    private long total;
    /** 每页条数 —— 必须与真实分页大小一致，前端据此渲染 */
    private long size;

    public Pagination(long current, List<T> records, long pages, long total, long size) {
        this.current = current;
        this.records = records;
        this.pages = pages;
        this.total = total;
        this.size = size;
    }

    /**
     * 直接由 MyBatis-Plus 的分页结果构造，size 从 {@link IPage} 取真实值。
     * <p>
     * 改造前各 Service 手写 {@code new Pagination(..., 10)}，与实际页大小（10 或 12）对不上，
     * 出现"实际每页 12 条却上报 size=10"这类元数据错误。
     */
    public static <T> Pagination<T> of(IPage<?> page, List<T> records) {
        return new Pagination<>(page.getCurrent(), records, page.getPages(), page.getTotal(), page.getSize());
    }
}
