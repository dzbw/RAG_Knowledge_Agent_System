package com.java1234.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link ChatClient}：业务侧自行做一次向量检索并拼上下文（见 {@link com.java1234.service.impl.ChatServiceImpl}），
 * 避免 QuestionAnswerAdvisor 与手写 similaritySearch 重复检索导致耗时翻倍。
 * <p>
 * 挂载 Spring AI 自带的 {@link SimpleLoggerAdvisor}，便于观察请求/响应（日志级别见 application.properties）。
 */
@Configuration
public class ChatClientConfig {

    /**
     * 对话客户端：仅挂日志 Advisor；RAG 检索由 ChatServiceImpl 单次 similaritySearch 完成。
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        SimpleLoggerAdvisor logAdvisor = new SimpleLoggerAdvisor();
        return ChatClient.builder(chatModel).defaultAdvisors(logAdvisor).build();
    }
}
