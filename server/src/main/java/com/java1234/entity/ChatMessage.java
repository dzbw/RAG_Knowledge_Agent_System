package com.java1234.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息，对应 t_chat_message。
 */
@Data
public class ChatMessage {
    private Long id;
    private Long sessionId;
    /** USER / ASSISTANT */
    private String role;
    private String content;
    /** JSON 字符串：引用列表 */
    private String refs;
    private LocalDateTime createTime;
}
