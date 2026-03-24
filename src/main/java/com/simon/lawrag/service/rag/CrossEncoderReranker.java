package com.simon.lawrag.service.rag;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * RAG 链路第4步：Cross-Encoder 重排序
 * 使用阿里云 DashScope gte-rerank 模型对候选集做精细化语义排序
 * 直接 HTTP 调用，无需 Python 微服务
 */
@Slf4j
@Component
public class CrossEncoderReranker {

    @Value("${llm.api-key}")
    private String apiKey;

    @Value("${reranker.api-url:https://dashscope.aliyuncs.com/api/v1/services/rerank/text-rerank/text-rerank}")
    private String rerankApiUrl;

    @Value("${reranker.model:gte-rerank}")
    private String rerankModel;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 对候选集做 Cross-Encoder 重排序
     */
    public List<RetrievedChunk> rerank(String query, List<RetrievedChunk> candidates, int topK) {
        if (candidates.isEmpty()) return candidates;
        if (candidates.size() <= topK) return setDefaultScores(candidates);

        try {
            return callDashScopeRerank(query, candidates, topK);
        } catch (Exception e) {
            log.warn("DashScope Rerank 调用失败，降级使用关键词排序: {}", e.getMessage());
            return fallbackRerank(query, candidates, topK);
        }
    }

    /**
     * 调用阿里云 DashScope gte-rerank 接口
     * 文档: https://help.aliyun.com/document_detail/2712193.html
     */
    private List<RetrievedChunk> callDashScopeRerank(String query, List<RetrievedChunk> candidates, int topK) {
        List<String> documents = candidates.stream()
                .map(RetrievedChunk::getContent)
                .toList();

        // 构造请求体
        JSONObject input = new JSONObject();
        input.put("query", query);
        input.put("documents", documents);

        JSONObject parameters = new JSONObject();
        parameters.put("top_n", Math.min(topK, candidates.size()));
        parameters.put("return_documents", false);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", rerankModel);
        requestBody.put("input", input);
        requestBody.put("parameters", parameters);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(rerankApiUrl, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("DashScope Rerank 返回错误: " + response.getStatusCode());
        }

        // 解析响应: output.results[].{index, relevance_score}
        JSONObject result = JSON.parseObject(response.getBody());
        JSONArray results = result.getJSONObject("output").getJSONArray("results");

        List<RetrievedChunk> reranked = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            JSONObject item = results.getJSONObject(i);
            int originalIndex = item.getIntValue("index");
            float score = item.getFloatValue("relevance_score");

            RetrievedChunk chunk = candidates.get(originalIndex);
            chunk.setRerankScore(score);
            reranked.add(chunk);
        }

        log.info("DashScope Rerank 完成: {} 候选 → top-{}", candidates.size(), reranked.size());
        return reranked;
    }

    /**
     * 降级排序：基于关键词匹配频度（当 API 不可用时）
     */
    private List<RetrievedChunk> fallbackRerank(String query, List<RetrievedChunk> candidates, int topK) {
        String queryLower = query.toLowerCase();
        String[] tokens = queryLower.split("[\\s，。！？；：、]+");

        for (RetrievedChunk chunk : candidates) {
            String contentLower = chunk.getContent().toLowerCase();
            float matchScore = 0f;
            for (String token : tokens) {
                if (token.length() >= 2 && contentLower.contains(token)) {
                    matchScore += (float) token.length() / queryLower.length();
                }
            }
            chunk.setRerankScore(chunk.getScore() * 0.6f + matchScore * 0.4f);
        }

        candidates.sort(Comparator.comparingDouble(RetrievedChunk::getRerankScore).reversed());
        return candidates.subList(0, Math.min(topK, candidates.size()));
    }

    private List<RetrievedChunk> setDefaultScores(List<RetrievedChunk> chunks) {
        for (RetrievedChunk chunk : chunks) {
            if (chunk.getRerankScore() == 0f) chunk.setRerankScore(chunk.getScore());
        }
        return chunks;
    }
}
