package com.java1234.controller;

import com.java1234.common.R;
import com.java1234.dto.CategorySaveRequest;
import com.java1234.entity.KbCategory;
import com.java1234.service.KbCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库分类。
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final KbCategoryService kbCategoryService;

    /**
     * 全部分类（树前扁平列表），登录用户可查。
     */
    @GetMapping
    public R<List<KbCategory>> list() {
        return R.ok(kbCategoryService.listAll());
    }

    /**
     * 新增或更新分类（管理员）。
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public R<Void> save(@Valid @RequestBody CategorySaveRequest req) {
        kbCategoryService.save(req);
        return R.ok();
    }

    /**
     * 删除分类（管理员）。
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        kbCategoryService.delete(id);
        return R.ok();
    }
}
