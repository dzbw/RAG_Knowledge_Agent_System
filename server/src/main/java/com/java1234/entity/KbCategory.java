package com.java1234.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库分类，对应 t_kb_category。
 */
@Data
public class KbCategory {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
