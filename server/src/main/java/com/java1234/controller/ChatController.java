package com.java1234.controller;

import com.java1234.common.R;
import com.java1234.dto.ChatAskRequest;
import com.java1234.dto.ChatAskResult;
import com.java1234.entity.ChatMessage;
import com.java1234.entity.ChatSession;
import com.java1234.service.ChatService;
import com.java1234.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库对话与历史。
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 提交问题，返回助手回答与引用。
     */
    @PostMapping("/ask")
    public R<ChatAskResult> ask(@Valid @RequestBody ChatAskRequest req) throws Exception {
        var u = SecurityUtils.requireUser();
        return R.ok(chatService.ask(u.getUserId(), req));
    }

    /**
     * 当前用户的会话列表。
     */
    @GetMapping("/sessions")
    public R<List<ChatSession>> sessions() {
        var u = SecurityUtils.requireUser();
        return R.ok(chatService.listSessions(u.getUserId()));
    }

    /**
     * 会话消息列表。
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public R<List<ChatMessage>> messages(@PathVariable Long sessionId) {
        var u = SecurityUtils.requireUser();
        return R.ok(chatService.listMessages(u.getUserId(), sessionId));
    }

    /**
     * 删除会话及消息。
     */
    @DeleteMapping("/sessions/{sessionId}")
    public R<Void> deleteSession(@PathVariable Long sessionId) {
        var u = SecurityUtils.requireUser();
        chatService.deleteSession(u.getUserId(), sessionId);
        return R.ok();
    }
}
