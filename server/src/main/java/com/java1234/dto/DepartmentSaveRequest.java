package com.java1234.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增/编辑部门请求。
 */
@Data
public class DepartmentSaveRequest {
    private Long id;
    @NotBlank(message = "部门名称不能为空")
    private String name;
    private String remark;
}
