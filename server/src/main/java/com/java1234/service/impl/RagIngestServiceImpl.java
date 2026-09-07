package com.java1234.service.impl;

import com.java1234.service.RagIngestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.json.Path2;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
/**
 * {@link RagIngestService} 实现：Tika / Markdown 读取 + Token 切块 + Redis 存储。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagIngestServiceImpl implements RagIngestService {

    private static final int REDIS_DELETE_BATCH_LIMIT = 10000;

    private final RedisVectorStore redisVectorStore;

    @Value("${spring.ai.vectorstore.redis.index-name:spring-ai-index}")
    private String redisVectorIndexName;

    @Value("${spring.ai.vectorstore.redis.prefix:embedding:}")
    private String redisKeyPrefix;

    /**
     * 删除向量时 SCAN 的 key 前缀列表（逗号分隔）。默认空则自动包含当前 prefix + 历史常用 embedding:
     */
    @Value("${spring.ai.vectorstore.redis.delete-scan-prefixes:}")
    private String deleteScanPrefixesCsv;

    /**
     * {@inheritDoc}
     */
    @Override
    public int ingest(Path absolutePath, String ext, Long documentId, Long categoryId, String title) {
        List<Document> loaded = loadDocuments(absolutePath, ext);
        TokenTextSplitter splitter = TokenTextSplitter.builder().build();
        List<Document> chunks = splitter.apply(loaded);
        List<Document> toAdd = new ArrayList<>();
        for (Document ch : chunks) {
            Map<String, Object> meta = new HashMap<>(ch.getMetadata());
            meta.put("docId", String.valueOf(documentId));
            meta.put("categoryId", String.valueOf(categoryId));
            meta.put("title", title != null ? title : "");
            String text = ch.getText();
            if (text == null || text.isBlank()) {
                continue;
            }
            toAdd.add(new Document(text, meta));
        }
        if (!toAdd.isEmpty()) {
            redisVectorStore.add(toAdd);
        }
        log.info("文档 {} 已向量化入库，块数 {}", documentId, toAdd.size());
        return toAdd.size();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 依次：Spring AI 官方 {@code delete(Filter)}、RediSearch 批量删除、多前缀 SCAN + JSON/DEL 物理删除，
     * 解决索引/key 前缀不一致、历史 embedding: 前缀、仅用 JSON.DEL 删不掉等情况。
     */
    @Override
    public void deleteVectorsByDocumentId(Long documentId) {
        String docIdStr = String.valueOf(documentId);
        String tag = escapeRedisTag(docIdStr);
        JedisPooled jedis = redisVectorStore.getJedis();
        List<String> scanPrefixes = resolveScanPrefixes();

        deleteByFrameworkFilter(docIdStr);
        int bySearch = deleteByRediSearchAllVariants(jedis, scanPrefixes, docIdStr, tag);
        int byScan = deleteByScanJsonDocId(jedis, scanPrefixes, docIdStr);

        int total = bySearch + byScan;
        boolean stillIndexed = anyChunkRemainingInIndex(jedis, tag);
        if (stillIndexed) {
            log.error(
                    "文档 {} 删除后 RediSearch 仍能查到 docId 向量，物理删除 FT={}, SCAN={}；请核对索引 [{}]、前缀 {}",
                    documentId,
                    bySearch,
                    byScan,
                    redisVectorIndexName,
                    scanPrefixes);
        }
        else if (total == 0) {
            log.info(
                    "文档 {} 向量已从 Redis 清理（未在本轮 FT/SCAN 计数到 key，可能由框架 delete 完成或无向量）",
                    documentId);
        }
        else {
            log.info("已从 Redis 清理文档 {} 向量：FT={}, SCAN={}", documentId, bySearch, byScan);
        }
    }

    /** 与 Spring AI RedisVectorStore 一致的过滤删除。 */
    private void deleteByFrameworkFilter(String docIdStr) {
        try {
            Filter.Expression expr = new FilterExpressionBuilder().eq("docId", docIdStr).build();
            redisVectorStore.delete(expr);
        }
        catch (Exception ex) {
            log.debug("RedisVectorStore.delete(Filter) docId={} : {}", docIdStr, ex.toString());
        }
    }

    /** 删除后校验索引中是否仍存在该 docId（LIMIT 1）。 */
    private boolean anyChunkRemainingInIndex(JedisPooled jedis, String escapedTag) {
        try {
            Query q = new Query("* @docId:{" + escapedTag + "}").limit(0, 1).dialect(2);
            SearchResult r = jedis.ftSearch(redisVectorIndexName, q);
            List<redis.clients.jedis.search.Document> docs = r.getDocuments();
            return docs != null && !docs.isEmpty();
        }
        catch (Exception ex) {
            log.debug("删除后校验 FT 失败: {}", ex.toString());
            return false;
        }
    }

    private List<String> resolveScanPrefixes() {
        Set<String> set = new LinkedHashSet<>();
        if (StringUtils.hasText(deleteScanPrefixesCsv)) {
            Arrays.stream(deleteScanPrefixesCsv.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .forEach(p -> set.add(ensureColonSuffix(p)));
        }
        set.add(ensureColonSuffix(redisKeyPrefix));
        set.add("embedding:");
        return new ArrayList<>(set);
    }

    /** SCAN/MATCH 需要明确前缀形态，如 java1234_rag: */
    private static String ensureColonSuffix(String p) {
        if (p == null || p.isEmpty()) {
            return p;
        }
        return p.endsWith(":") ? p : p + ":";
    }

    private int deleteByRediSearchAllVariants(JedisPooled jedis, List<String> scanPrefixes, String docIdStr, String escapedTag) {
        String[] queryStrings = {
                "* @docId:{" + escapedTag + "}",
                "@docId:{" + escapedTag + "}",
        };
        int total = 0;
        for (String qs : queryStrings) {
            total += deleteByRediSearchQuery(jedis, scanPrefixes, qs);
            if (total > 0) {
                log.debug("RediSearch 使用查询 [{}] 删除 docId={}", qs, docIdStr);
                break;
            }
        }
        return total;
    }

    private int deleteByRediSearchQuery(JedisPooled jedis, List<String> scanPrefixes, String queryString) {
        Query query = new Query(queryString).limit(0, REDIS_DELETE_BATCH_LIMIT).dialect(2);
        int totalRemoved = 0;
        while (true) {
            SearchResult result;
            try {
                result = jedis.ftSearch(redisVectorIndexName, query);
            }
            catch (Exception ex) {
                log.debug("FT.SEARCH 失败 query=[{}]: {}", queryString, ex.toString());
                return totalRemoved;
            }
            List<redis.clients.jedis.search.Document> docs = result.getDocuments();
            if (docs == null || docs.isEmpty()) {
                break;
            }
            int batchOk = 0;
            for (redis.clients.jedis.search.Document doc : docs) {
                batchOk += unlinkVectorKeyCandidates(jedis, scanPrefixes, doc.getId());
            }
            if (batchOk < docs.size()) {
                log.warn("本批 FT 命中 {} 条，物理删除成功 {} 条", docs.size(), batchOk);
            }
            if (batchOk == 0) {
                log.warn("FT 命中 {} 条但无任何 key 删除成功，结束以免死循环", docs.size());
                return totalRemoved;
            }
            totalRemoved += batchOk;
            if (docs.size() < REDIS_DELETE_BATCH_LIMIT) {
                break;
            }
        }
        return totalRemoved;
    }

    /**
     * 尝试多种可能 Redis key：完整 key、仅 suffix、补当前/历史前缀（避免双前缀或漏前缀）。
     */
    private static int unlinkVectorKeyCandidates(JedisPooled jedis, List<String> scanPrefixes, String idFromSearch) {
        if (idFromSearch == null || idFromSearch.isEmpty()) {
            return 0;
        }
        LinkedHashSet<String> tried = new LinkedHashSet<>();
        tried.add(idFromSearch);
        String suffix = stripAnyKnownPrefix(idFromSearch, scanPrefixes);
        for (String prefix : scanPrefixes) {
            tried.add(prefix + suffix);
        }
        int removed = 0;
        for (String key : tried) {
            if (unlinkJsonOrDel(jedis, key)) {
                removed = 1;
                break;
            }
        }
        return removed;
    }

    private static String stripAnyKnownPrefix(String fullKey, List<String> prefixes) {
        for (String p : prefixes) {
            if (fullKey.startsWith(p)) {
                return fullKey.substring(p.length());
            }
        }
        return fullKey;
    }

    /** RedisJSON 文档优先 JSON.DEL；失败或非 JSON 再 DEL，兼容不同模块/版本。 */
    private static boolean unlinkJsonOrDel(JedisPooled jedis, String key) {
        try {
            Long n = jedis.jsonDel(key);
            if (n != null && n >= 1L) {
                return true;
            }
        }
        catch (Exception ignored) {
            // 非 JSON key 或路径错误
        }
        try {
            Long n = jedis.del(key);
            return n != null && n >= 1L;
        }
        catch (Exception ignored) {
            return false;
        }
    }

    private int deleteByScanJsonDocId(JedisPooled jedis, List<String> scanPrefixes, String docIdStr) {
        int removed = 0;
        for (String prefix : scanPrefixes) {
            ScanParams params = new ScanParams().match(prefix + "*").count(500);
            String cursor = "0";
            do {
                ScanResult<String> scan = jedis.scan(cursor, params);
                for (String key : scan.getResult()) {
                    if (!key.startsWith(prefix)) {
                        continue;
                    }
                    String stored = readJsonDocId(jedis, key);
                    if (!docIdMatches(docIdStr, stored)) {
                        continue;
                    }
                    if (unlinkJsonOrDel(jedis, key)) {
                        removed++;
                    }
                }
                cursor = scan.getCursor();
            } while (!"0".equals(cursor));
        }
        return removed;
    }

    private static String readJsonDocId(JedisPooled jedis, String key) {
        try {
            Object raw = jedis.jsonGet(key, new Path2("$.docId"));
            String s = extractDocIdString(raw);
            if (s != null) {
                return s;
            }
            raw = jedis.jsonGet(key, new Path2("$.metadata.docId"));
            return extractDocIdString(raw);
        }
        catch (Exception ex) {
            return null;
        }
    }

    private static String extractDocIdString(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number num) {
            return String.valueOf(num.longValue());
        }
        if (raw instanceof String s) {
            return normalizeJsonScalarOrArrayString(s);
        }
        if (raw instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Number num) {
                return String.valueOf(num.longValue());
            }
            return normalizeJsonScalarOrArrayString(String.valueOf(first));
        }
        return normalizeJsonScalarOrArrayString(String.valueOf(raw));
    }

    /** Jedis 可能返回带引号的标量或 JSON 数组字符串，如 \"5\" 或 [\"5\"]。 */
    private static String normalizeJsonScalarOrArrayString(String s) {
        String t = s.trim();
        if (t.startsWith("[") && t.endsWith("]")) {
            String inner = t.substring(1, t.length() - 1).trim();
            if (inner.startsWith("\"") && inner.endsWith("\"") && inner.length() >= 2) {
                inner = inner.substring(1, inner.length() - 1);
            }
            return inner.trim();
        }
        return stripJsonQuotes(t).trim();
    }

    private static String stripJsonQuotes(String s) {
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static boolean docIdMatches(String expected, String stored) {
        if (stored == null) {
            return false;
        }
        if (Objects.equals(expected, stored)) {
            return true;
        }
        try {
            return Long.parseLong(expected) == Long.parseLong(stored.trim());
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    private List<Document> loadDocuments(Path absolutePath, String ext) {
        String e = ext == null ? "" : ext.toLowerCase(Locale.ROOT);
        FileSystemResource resource = new FileSystemResource(absolutePath.toFile());
        if ("md".equals(e)) {
            return new MarkdownDocumentReader(resource, MarkdownDocumentReaderConfig.defaultConfig()).get();
        }
        return new TikaDocumentReader(resource).get();
    }

    private static String escapeRedisTag(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\', '$', '|', '{', '}', '(', ')', '[', ']', '-', '\'' -> sb.append('\\').append(c);
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
