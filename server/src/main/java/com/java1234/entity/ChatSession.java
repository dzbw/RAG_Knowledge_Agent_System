package com.java1234.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天会话，对应 t_chat_session。
 */
@Data
public class ChatSession {
    private Long id;
    private Long userId;
    private String title;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
