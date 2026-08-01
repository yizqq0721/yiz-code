package com.yizqq.yizcode.common;


import lombok.Data;

@Data
public class PageRequest {

    /**
     * 当前页
     */
    private long current = 1;

    /**
     * 页面大小
     */
    private long pageSize = 10;

    /**
     * 搜索字段
     */
    private String searchText;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "descend";
}
