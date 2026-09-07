package com.java1234.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统日志，对应 t_system_log。
 */
@Data
public class SystemLog {
    private Long id;
    private Long userId;
    private String action;
    private String ip;
    private LocalDateTime createTime;
}
