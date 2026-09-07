package com.java1234.service.impl;



import com.fasterxml.jackson.databind.ObjectMapper;

import com.java1234.dto.ChatAskRequest;

import com.java1234.dto.ChatAskResult;

import com.java1234.entity.ChatMessage;

import com.java1234.entity.ChatSession;

import com.java1234.exception.BusinessException;

import com.java1234.mapper.ChatMessageMapper;

import com.java1234.mapper.ChatSessionMapper;

import com.java1234.service.ChatService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.document.Document;

import org.springframework.ai.vectorstore.SearchRequest;

import org.springframework.ai.vectorstore.VectorStore;

import org.springframework.ai.vectorstore.filter.Filter;

import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.util.ArrayList;

import java.util.Collections;

import java.util.LinkedHashMap;

import java.util.LinkedHashSet;

import java.util.List;

import java.util.Map;

import java.util.Objects;

import java.util.Set;

import java.util.stream.Collectors;

/**

 * {@link com.java1234.service.ChatService} 实现。

 */

@Service

@RequiredArgsConstructor

@Slf4j

public class ChatServiceImpl implements ChatService {



    private static final int RAG_TOP_K = 10;



    /**

     * 系统提示：要求仅依据上下文、Markdown 输出。

     */

    public static final String SYSTEM_PROMPT = """

            你是「Java1234 RAG 企业知识库」的智能助手。请严格根据检索到的上下文回答问题。

            若上下文不足以回答，请明确说明「知识库中未找到相关信息」，不要编造。

            回答请使用清晰的 Markdown（可适当使用标题、列表）。结尾可简要列出依据的文档标题。

            """;



    private final ChatClient chatClient;

    private final VectorStore vectorStore;

    private final ChatSessionMapper chatSessionMapper;

    private final ChatMessageMapper chatMessageMapper;

    private final ObjectMapper objectMapper;



    /**

     * {@inheritDoc}

     */

    @Override

    @Transactional(rollbackFor = Exception.class)

    public ChatAskResult ask(Long userId, ChatAskRequest req) throws Exception {

        Long sessionId = req.getSessionId();

        if (sessionId == null) {

            ChatSession s = new ChatSession();

            s.setUserId(userId);

            String t = req.getQuestion().trim();

            s.setTitle(t.length() > 30 ? t.substring(0, 30) + "…" : t);

            chatSessionMapper.insert(s);

            sessionId = s.getId();

        }

        else {

            ChatSession exist = chatSessionMapper.selectById(sessionId);

            if (exist == null || !exist.getUserId().equals(userId)) {

                throw new BusinessException("会话不存在或无权限");

            }

        }



        long t0 = System.nanoTime();

        List<Document> cited = retrieveForCategories(req.getQuestion(), req.getCategoryIds());

        long retrievalMs = (System.nanoTime() - t0) / 1_000_000L;

        log.info("RAG 向量检索完成 sessionId={} 命中块数={} 耗时={}ms", sessionId, cited.size(), retrievalMs);



        String userTurn = buildRagUserMessage(req.getQuestion(), cited);



        t0 = System.nanoTime();

        String answer = chatClient.prompt().system(SYSTEM_PROMPT).user(userTurn).call().content();

        long llmMs = (System.nanoTime() - t0) / 1_000_000L;

        log.info("LLM 生成完成 sessionId={} 耗时={}ms（SimpleLoggerAdvisor 将打出请求/响应摘要）", sessionId, llmMs);



        List<Map<String, Object>> refs = toRefs(cited);

        String refsJson = objectMapper.writeValueAsString(refs);



        ChatMessage um = new ChatMessage();

        um.setSessionId(sessionId);

        um.setRole("USER");

        um.setContent(req.getQuestion());

        um.setRefs(null);

        chatMessageMapper.insert(um);



        ChatMessage am = new ChatMessage();

        am.setSessionId(sessionId);

        am.setRole("ASSISTANT");

        am.setContent(answer);

        am.setRefs(refsJson);

        chatMessageMapper.insert(am);



        chatSessionMapper.touchUpdateTime(sessionId);



        ChatAskResult res = new ChatAskResult();

        res.setSessionId(sessionId);

        res.setAnswer(answer);

        res.setReferences(refs);

        return res;

    }



    /**

     * 有分类时先带 {@code categoryId} 过滤检索；无命中或异常时与无分类相同，做全库无条件检索兜底。

     */

    private List<Document> retrieveForCategories(String question, List<Long> categoryIds) {

        if (categoryIds == null || categoryIds.isEmpty()) {

            return vectorSimilaritySearch(question, null);

        }

        Set<String> keys = categoryIds.stream()

                .filter(Objects::nonNull)

                .map(String::valueOf)

                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (keys.isEmpty()) {

            return vectorSimilaritySearch(question, null);

        }

        try {
            Filter.Expression expr = buildCategoryIdFilter(keys);
            List<Document> filtered = vectorSimilaritySearch(question, expr);
            if (filtered != null && !filtered.isEmpty()) {
                return filtered;
            }
            log.info("限定 categoryId {} 向量检索无命中，降级为全库无条件检索", keys);
            return vectorSimilaritySearch(question, null);
        }
        catch (Exception ex) {
            log.warn("categoryId 过滤向量检索失败，降级为全库无条件检索：{}", ex.toString());
            return vectorSimilaritySearch(question, null);
        }

    }



    /**

     * Redis 向量检索：无多余参数；{@code filter} 为 null 表示全库。

     */

    private List<Document> vectorSimilaritySearch(String question, Filter.Expression filter) {

        SearchRequest.Builder b = SearchRequest.builder()

                .query(question)

                .topK(RAG_TOP_K)

                .similarityThreshold(0.0);

        if (filter != null) {

            b.filterExpression(filter);

        }

        List<Document> docs = vectorStore.similaritySearch(b.build());

        return docs != null ? docs : Collections.emptyList();

    }



    private static Filter.Expression buildCategoryIdFilter(Set<String> categoryIdsAsString) {

        FilterExpressionBuilder fb = new FilterExpressionBuilder();

        if (categoryIdsAsString.size() == 1) {

            return fb.eq("categoryId", categoryIdsAsString.iterator().next()).build();

        }

        List<Object> values = new ArrayList<>(categoryIdsAsString);

        return fb.in("categoryId", values).build();

    }



    /**

     * 将检索结果拼成单条 user 消息，等价于一次 RAG 上下文注入（避免 Advisor 内二次检索）。

     */

    private static String buildRagUserMessage(String question, List<Document> cited) {

        if (cited == null || cited.isEmpty()) {

            return """

                    （知识库检索未命中足够相关的片段，请直接依据系统说明作答。）



                    用户问题：

                    """ + question;

        }

        StringBuilder sb = new StringBuilder();

        sb.append("以下是检索到的知识片段，请严格据此回答；片段相互冲突时优先采纳与问题最直接相关的表述。\n\n");

        int i = 1;

        for (Document d : cited) {

            Map<String, Object> meta = d.getMetadata();

            String title = meta != null && meta.get("title") != null ? String.valueOf(meta.get("title")) : "(无标题)";

            sb.append("### 片段 ").append(i++).append(" · ").append(title).append("\n");

            String text = d.getText();

            if (text != null) {

                sb.append(text.strip()).append("\n\n");

            }

        }

        sb.append("---\n用户问题：\n").append(question.strip());

        return sb.toString();

    }



    /**

     * {@inheritDoc}

     */

    @Override

    public List<ChatSession> listSessions(Long userId) {

        return chatSessionMapper.listByUserId(userId);

    }



    /**

     * {@inheritDoc}

     */

    @Override

    public List<ChatMessage> listMessages(Long userId, Long sessionId) {

        ChatSession s = chatSessionMapper.selectById(sessionId);

        if (s == null || !s.getUserId().equals(userId)) {

            throw new BusinessException("会话不存在或无权限");

        }

        return chatMessageMapper.listBySessionId(sessionId);

    }



    /**

     * {@inheritDoc}

     */

    @Override

    @Transactional(rollbackFor = Exception.class)

    public void deleteSession(Long userId, Long sessionId) {

        ChatSession s = chatSessionMapper.selectById(sessionId);

        if (s == null || !s.getUserId().equals(userId)) {

            throw new BusinessException("会话不存在或无权限");

        }

        chatMessageMapper.deleteBySessionId(sessionId);

        chatSessionMapper.deleteById(sessionId);

    }



    private static List<Map<String, Object>> toRefs(List<Document> docs) {

        List<Map<String, Object>> refs = new ArrayList<>();

        for (Document d : docs) {

            Map<String, Object> m = new LinkedHashMap<>();

            Map<String, Object> meta = d.getMetadata();

            m.put("title", meta != null ? meta.get("title") : null);

            m.put("docId", meta != null ? meta.get("docId") : null);

            m.put("categoryId", meta != null ? meta.get("categoryId") : null);

            String tx = d.getText();

            if (tx != null && tx.length() > 240) {

                tx = tx.substring(0, 240) + "…";

            }

            m.put("snippet", tx);

            refs.add(m);

        }

        return refs;

    }

}

