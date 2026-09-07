package com.java1234.service.impl;

import com.java1234.dto.CategorySaveRequest;
import com.java1234.entity.KbCategory;
import com.java1234.exception.BusinessException;
import com.java1234.mapper.KbCategoryMapper;
import com.java1234.mapper.KbDocumentMapper;
import com.java1234.service.KbCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link KbCategoryService} 实现。
 */
@Service
@RequiredArgsConstructor
public class KbCategoryServiceImpl implements KbCategoryService {

    private final KbCategoryMapper kbCategoryMapper;
    private final KbDocumentMapper kbDocumentMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<KbCategory> listAll() {
        return kbCategoryMapper.listAllOrderBySort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(CategorySaveRequest req) {
        if (req.getId() == null) {
            KbCategory c = new KbCategory();
            c.setName(req.getName());
            c.setDescription(req.getDescription());
            c.setIcon(req.getIcon());
            c.setSortOrder(req.getSortOrder());
            kbCategoryMapper.insert(c);
        } else {
            KbCategory c = new KbCategory();
            c.setId(req.getId());
            c.setName(req.getName());
            c.setDescription(req.getDescription());
            c.setIcon(req.getIcon());
            c.setSortOrder(req.getSortOrder());
            kbCategoryMapper.update(c);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        int n = kbDocumentMapper.countByCategoryId(id);
        if (n > 0) {
            throw new BusinessException("该分类下仍有文档，无法删除");
        }
        kbCategoryMapper.deleteById(id);
    }
}
