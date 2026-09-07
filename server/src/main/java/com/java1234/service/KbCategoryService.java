package com.java1234.service;

import com.java1234.dto.CategorySaveRequest;
import com.java1234.entity.KbCategory;

import java.util.List;

/**
 * 知识库分类维护。
 */
public interface KbCategoryService {

    List<KbCategory> listAll();

    void save(CategorySaveRequest req);

    void delete(Long id);
}
