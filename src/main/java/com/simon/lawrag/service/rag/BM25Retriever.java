package com.simon.lawrag.service.rag;

import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.QueryResults;
import io.milvus.param.R;
import io.milvus.param.dml.QueryParam;
import io.milvus.response.QueryResultsWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RAG 链路第2步（B路）：BM25 关键词检索
 * 提取查询关键词，通过 Milvus 标量过滤实现关键词匹配
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BM25Retriever {

    private final MilvusServiceClient milvusClient;

    @Value("${milvus.collection-name}")
    private String collectionName;

    private static final int DEFAULT_TOP_K = 20;

    // 停用词（高频无意义词）
    private static final Set<String> STOP_WORDS = Set.of(
            "的", "了", "是", "在", "我", "有", "和", "就", "不", "人", "都", "一", "一个",
            "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好",
            "自己", "这", "那", "什么", "怎么", "如何", "可以", "吗", "吧", "呢", "啊"
    );

    /**
     * BM25 关键词检索
     * @param query 查询文本（已改写）
     * @param categoryFilter 科室过滤
     * @param topK 返回数量
     */
    public List<RetrievedChunk> retrieve(String query, String categoryFilter, int topK) {
        try {
            // 1. 提取关键词
            List<String> keywords = extractKeywords(query);
            if (keywords.isEmpty()) {
                log.warn("BM25：未提取到有效关键词");
                return new ArrayList<>();
            }

            // 2. 构建 Milvus 过滤表达式（多关键词 OR，前缀匹配）
            // Milvus 标量过滤仅支持前缀模式 like "ab%"，不支持 "%ab%" (contains)
            StringBuilder exprBuilder = new StringBuilder();
            for (int i = 0; i < keywords.size(); i++) {
                if (i > 0) exprBuilder.append(" or ");
                String kw = keywords.get(i).replace("\"", "\\\"");
                exprBuilder.append("content like \"").append(kw).append("%\"");
            }

            if (categoryFilter != null && !categoryFilter.isEmpty()) {
                exprBuilder.insert(0, "(").append(") and category == \"").append(categoryFilter).append("\"");
            }

            QueryParam queryParam = QueryParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withExpr(exprBuilder.toString())
                    .withOutFields(Arrays.asList("id", "content", "category",
                            "content_type", "chapter", "page_number", "knowledge_base_id"))
                    .withLimit((long) topK)
                    .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
                    .build();

            R<QueryResults> response = milvusClient.query(queryParam);

            if (response.getStatus() != R.Status.Success.getCode()) {
                log.warn("BM25 检索失败: {}", response.getMessage());
                return new ArrayList<>();
            }

            // 3. 解析结果并计算 BM25 得分
            List<RetrievedChunk> results = parseQueryResults(response.getData(), keywords);

            log.info("BM25检索完成: keywords={}, 命中={}条", keywords, results.size());
            return results;

        } catch (Exception e) {
            log.error("BM25检索异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 从查询中提取关键词
     */
    public List<String> extractKeywords(String query) {
        List<String> keywords = new ArrayList<>();

        // 1. 先尝试提取引号内的精确短语
        Pattern phrasePattern = Pattern.compile("[「「\"'](.*?)[」」\"']");
        Matcher matcher = phrasePattern.matcher(query);
        while (matcher.find()) {
            keywords.add(matcher.group(1));
        }

        // 2. 提取2字以上的词（简单分词）
        // 移除标点，按空格/标点切分
        String cleaned = query.replaceAll("[，。！？；：、\u201c\u201d\u2018\u2019\\(\\)（）\\[\\]【】]", " ");
        String[] tokens = cleaned.split("\\s+");

        for (String token : tokens) {
            token = token.trim();
            if (token.length() >= 2 && !STOP_WORDS.contains(token) && !keywords.contains(token)) {
                keywords.add(token);
            }
        }

        // 3. 提取医学专有词（简单规则）
        extractMedicalTerms(query, keywords);

        return keywords.subList(0, Math.min(keywords.size(), 8)); // 最多8个关键词
    }

    /**
     * 提取医学专有词（简单规则匹配）
     */
    private void extractMedicalTerms(String query, List<String> keywords) {
        // 常见医学关键词模式
        String[] medicalPatterns = {
                "\\d+[岁年]", // 年龄
                "[A-Z]{2,}", // 英文缩写（如 CT、MRI）
                "\\d+mg|\\d+ml", // 剂量
        };
        for (String pattern : medicalPatterns) {
            Matcher m = Pattern.compile(pattern).matcher(query);
            while (m.find()) {
                String term = m.group();
                if (!keywords.contains(term)) keywords.add(term);
            }
        }
    }

    /**
     * 解析查询结果，计算简化 BM25 关键词频次得分
     */
    private List<RetrievedChunk> parseQueryResults(QueryResults results, List<String> keywords) {
        List<RetrievedChunk> chunks = new ArrayList<>();
        try {
            QueryResultsWrapper wrapper = new QueryResultsWrapper(results);
            List<QueryResultsWrapper.RowRecord> rows = wrapper.getRowRecords();

            for (QueryResultsWrapper.RowRecord row : rows) {
                RetrievedChunk chunk = new RetrievedChunk();

                Object idObj = row.get("id");
                chunk.setId(idObj != null ? idObj.toString() : "");

                Object contentObj = row.get("content");
                String content = contentObj != null ? contentObj.toString() : "";
                chunk.setContent(content);

                Object catObj = row.get("category");
                chunk.setCategory(catObj != null ? catObj.toString() : "");

                Object ctObj = row.get("content_type");
                chunk.setContentType(ctObj != null ? ctObj.toString() : "");

                Object chapObj = row.get("chapter");
                chunk.setChapter(chapObj != null ? chapObj.toString() : "");

                Object pageObj = row.get("page_number");
                if (pageObj != null) {
                    try { chunk.setPageNumber(Integer.parseInt(pageObj.toString())); } catch (NumberFormatException ignored) {}
                }

                Object kbIdObj = row.get("knowledge_base_id");
                if (kbIdObj != null) {
                    try { chunk.setKnowledgeBaseId(Long.parseLong(kbIdObj.toString())); } catch (NumberFormatException ignored) {}
                }

                chunk.setScore(calculateBm25Score(content, keywords));
                chunk.setSource(RetrievedChunk.Source.BM25);
                chunks.add(chunk);
            }

            // 按得分降序排列
            chunks.sort((a, b) -> Float.compare(b.getScore(), a.getScore()));
        } catch (Exception e) {
            log.error("BM25 结果解析失败", e);
        }
        return chunks;
    }

    /**
     * 简化 BM25 得分：统计关键词在文档中出现次数，除以文档长度归一化
     */
    private float calculateBm25Score(String content, List<String> keywords) {
        if (content == null || content.isEmpty()) return 0f;
        String lower = content.toLowerCase();
        int hits = 0;
        for (String kw : keywords) {
            String k = kw.toLowerCase();
            int idx = 0;
            while ((idx = lower.indexOf(k, idx)) != -1) {
                hits++;
                idx += k.length();
            }
        }
        return hits / (float) Math.max(1, content.length() / 50);
    }
}
