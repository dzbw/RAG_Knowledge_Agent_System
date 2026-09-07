package com.java1234.controller;

import com.java1234.common.PageResult;
import com.java1234.common.R;
import com.java1234.entity.KbDocument;
import com.java1234.service.KbDocumentService;
import com.java1234.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识文档上传与管理。
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final KbDocumentService kbDocumentService;

    /**
     * 上传并向量化（管理员）。
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<KbDocument> upload(@RequestPart("file") MultipartFile file,
                               @RequestParam Long categoryId,
                               @RequestParam(required = false) String title) throws Exception {
        var u = SecurityUtils.requireUser();
        return R.ok(kbDocumentService.upload(file, categoryId, title, u.getUserId()));
    }

    /**
     * 文档分页（管理员）。
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page")
    public R<PageResult<KbDocument>> page(@RequestParam(defaultValue = "") String keyword,
                                          @RequestParam(required = false) Long categoryId,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return R.ok(kbDocumentService.page(keyword, categoryId, page, size));
    }

    /**
     * 删除文档及向量（管理员）。
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) throws Exception {
        kbDocumentService.delete(id);
        return R.ok();
    }
}
