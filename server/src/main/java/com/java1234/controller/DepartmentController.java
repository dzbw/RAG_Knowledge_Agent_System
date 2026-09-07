package com.java1234.controller;

import com.java1234.common.R;
import com.java1234.dto.DepartmentSaveRequest;
import com.java1234.entity.Department;
import com.java1234.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理（管理员 CRUD）。
 */
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 部门列表。
     */
    @GetMapping
    public R<List<Department>> list() {
        return R.ok(departmentService.listAll());
    }

    /**
     * 新增或更新部门（管理员）。
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public R<Void> save(@Valid @RequestBody DepartmentSaveRequest req) {
        departmentService.save(req);
        return R.ok();
    }

    /**
     * 删除部门（管理员）。
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return R.ok();
    }
}
