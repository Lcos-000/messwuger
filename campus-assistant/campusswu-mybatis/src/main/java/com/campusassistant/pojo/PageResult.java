package com.campusassistant.pojo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> records; //查询数据结果
    private long total; //总记录数
    private long current; //当前页条数
    private long size; //每页大小
    private long pages; //总页数

    public static <T> PageResult<T> of(IPage<T> page){
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }
}
