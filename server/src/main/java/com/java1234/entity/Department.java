package com.java1234.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门实体，对应表 t_department。
 */
@Data
public class Department {
    private Long id;
    private String name;
    private String remark;
    private LocalDateTime createTime;
}
