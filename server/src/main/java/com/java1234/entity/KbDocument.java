package com.java1234.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识文档，对应 t_kb_document。
 */
@Data
public class KbDocument {
    private Long id;
    private Long categoryId;
    private String title;
    private String fileName;
    /** 相对 uploads 根的路径 */
    private String filePath;
    private String fileType;
    private Long fileSize;
    /** PROCESSING / SUCCESS / FAIL */
    private String status;
    private Integer vectorCount;
    private Long uploadUserId;
    private LocalDateTime createTime;
}
