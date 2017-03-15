package com.feedss.manage.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : by AegisLee on 16/12/9.
 */
@Data
public class PageVo<T> {
    /** 行实体类 */
    private List<T> rows = new ArrayList<T>();
    /** 总条数 */
    private int total;
}
