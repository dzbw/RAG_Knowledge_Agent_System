package com.java1234.service.impl;

import com.java1234.common.FileTypeUtil;
import com.java1234.common.PageResult;
import com.java1234.entity.KbDocument;
import com.java1234.exception.BusinessException;
import com.java1234.mapper.KbDocumentMapper;
import com.java1234.service.FileStorageService;
import com.java1234.service.KbDocumentService;
import com.java1234.service.RagIngestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * {@link KbDocumentService} 实现。
 */
@Service
@RequiredArgsConstructor
public class KbDocumentServiceImpl implements KbDocumentService {

    @Value("${file.upload.path}")
    private String uploadRoot;

    private final KbDocumentMapper kbDocumentMapper;
    private final FileStorageService fileStorageService;
    private final RagIngestService ragIngestService;

    /**
     * {@inheritDoc}
     */
    @Override
    public KbDocument upload(MultipartFile file, Long categoryId, String title, Long uploadUserId) throws Exception {
        if (!FileTypeUtil.allowed(file)) {
            throw new BusinessException("仅支持 txt、pdf、doc、docx、markdown(md) 文件");
        }
        String ext = FileTypeUtil.ext(file.getOriginalFilename());
        FileStorageService.StoredFile stored = fileStorageService.save(file);
        KbDocument doc = new KbDocument();
        doc.setCategoryId(categoryId);
        doc.setTitle(title != null && !title.isBlank() ? title : file.getOriginalFilename());
        doc.setFileName(file.getOriginalFilename());
        doc.setFilePath(stored.relativePath());
        doc.setFileType(ext);
        doc.setFileSize(file.getSize());
        doc.setStatus("PROCESSING");
        doc.setVectorCount(0);
        doc.setUploadUserId(uploadUserId);
        kbDocumentMapper.insert(doc);
        try {
            int n = ragIngestService.ingest(stored.absolutePath(), ext, doc.getId(), categoryId, doc.getTitle());
            doc.setVectorCount(n);
            doc.setStatus("SUCCESS");
            kbDocumentMapper.update(doc);
        } catch (Exception ex) {
            doc.setStatus("FAIL");
            kbDocumentMapper.update(doc);
            throw ex;
        }
        return kbDocumentMapper.selectById(doc.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResult<KbDocument> page(String keyword, Long categoryId, int page, int size) {
        int off = Math.max(0, (page - 1) * size);
        long total = kbDocumentMapper.countByKeyword(keyword, categoryId);
        List<KbDocument> list = kbDocumentMapper.selectPage(keyword, categoryId, off, size);
        return PageResult.of(total, list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) throws Exception {
        KbDocument d = kbDocumentMapper.selectById(id);
        if (d == null) {
            return;
        }
        ragIngestService.deleteVectorsByDocumentId(id);
        Path abs = Paths.get(uploadRoot, d.getFilePath());
        Files.deleteIfExists(abs);
        kbDocumentMapper.deleteById(id);
    }
}
