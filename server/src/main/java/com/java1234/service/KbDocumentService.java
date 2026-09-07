package com.java1234.service;

import com.java1234.common.PageResult;
import com.java1234.entity.KbDocument;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识文档：上传、分页、删除。
 */
public interface KbDocumentService {

    KbDocument upload(MultipartFile file, Long categoryId, String title, Long uploadUserId) throws Exception;

    PageResult<KbDocument> page(String keyword, Long categoryId, int page, int size);

    void delete(Long id) throws Exception;
}
