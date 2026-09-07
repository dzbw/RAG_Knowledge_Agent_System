package com.java1234.service;

import com.java1234.dto.DepartmentSaveRequest;
import com.java1234.entity.Department;

import java.util.List;

/**
 * 部门业务。
 */
public interface DepartmentService {

    /**
     * 全部部门（下拉选择用）。
     */
    List<Department> listAll();

    void save(DepartmentSaveRequest req);

    void delete(Long id);
}
