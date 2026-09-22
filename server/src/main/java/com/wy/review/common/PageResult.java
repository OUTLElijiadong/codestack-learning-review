package com.wy.review.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 统一分页返回体：MyBatis-Plus IPage 转 JSON 时字段名不直观，
 * 统一转为 {total, pages, current, size, list} 与前端分页组件约定一致
 */
@Data
public class PageResult<T> implements Serializable {

    private Long total;
    private Long pages;
    private Long current;
    private Long size;
    private List<T> list;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> r = new PageResult<>();
        r.setTotal(page.getTotal());
        r.setPages(page.getPages());
        r.setCurrent(page.getCurrent());
        r.setSize(page.getSize());
        r.setList(page.getRecords());
        return r;
    }

    /** 实体转 VO 场景：对 records 逐个转换后再包装 */
    public static <E, T> PageResult<T> of(IPage<E> page, Function<E, T> mapper) {
        PageResult<T> r = new PageResult<>();
        r.setTotal(page.getTotal());
        r.setPages(page.getPages());
        r.setCurrent(page.getCurrent());
        r.setSize(page.getSize());
        r.setList(page.getRecords().stream().map(mapper).collect(Collectors.toList()));
        return r;
    }
}
