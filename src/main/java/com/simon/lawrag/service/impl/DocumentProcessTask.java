package com.simon.lawrag.service.impl;

import com.simon.lawrag.entity.MedKnowledgeBase;
import com.simon.lawrag.mapper.MedKnowledgeBaseMapper;
import com.simon.lawrag.service.knowledge.DocumentExtractor;
import com.simon.lawrag.service.knowledge.MilvusService;
import com.simon.lawrag.service.knowledge.TextChunker;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档异步处理任务（独立组件，避免 @Async 自调用 AOP 代理失效问题）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentProcessTask {

    private final MedKnowledgeBaseMapper knowledgeBaseMapper;
    private final MilvusService milvusService;
    private final DocumentExtractor documentExtractor;
    private final TextChunker textChunker;
    private final OpenAiEmbeddingModel embeddingModel;

    @Value("${upload.path:uploads}")
    private String uploadPath;

    // 阿里云 text-embedding-v3 单批上限为 10
    private static final int EMBED_BATCH_SIZE = 10;

    /**
     * 异步文档处理流水线：uploading → processing → ready (or failed)
     */
    @Async
    public void process(Long knowledgeBaseId) {
        MedKnowledgeBase kb = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (kb == null) {
            log.error("文档处理启动失败：找不到记录 id={}，可能事务未提交或已被删除", knowledgeBaseId);
            return;
        }

        try {
            // 更新状态为 processing
            kb.setStatus("processing");
            kb.setErrorMsg(null);
            knowledgeBaseMapper.updateById(kb);

            // 1. 从本地磁盘读取文件（fileUrl 为相对路径，如 knowledge/uuid.pdf）
            Path filePath = Paths.get(uploadPath, kb.getFileUrl());
            if (!Files.exists(filePath)) {
                throw new RuntimeException("本地文件不存在: " + filePath.toAbsolutePath());
            }
            InputStream inputStream = Files.newInputStream(filePath);

            // 2. 提取文本
            String text = documentExtractor.extract(inputStream, kb.getFileType());
            if (StringUtils.isBlank(text)) {
                throw new RuntimeException("文档内容为空，请检查文件");
            }

            // 3. 智能切分
            List<TextChunker.TextChunk> chunks = textChunker.chunk(text, knowledgeBaseId, kb.getCategory());
            if (chunks.isEmpty()) {
                throw new RuntimeException("文档切片结果为空");
            }

            // 4. 批量向量化（调用阿里云 text-embedding-v3）
            List<List<Float>> embeddings = batchEmbed(chunks);

            // 5. 写入 Milvus
            milvusService.insertVectors(chunks, embeddings);

            // 6. 更新状态为 ready
            kb.setChunkCount(chunks.size());
            kb.setStatus("ready");
            kb.setErrorMsg(null);
            knowledgeBaseMapper.updateById(kb);

            log.info("文档处理完成: id={}, chunks={}", knowledgeBaseId, chunks.size());

        } catch (Exception e) {
            log.error("文档处理失败: id={}", knowledgeBaseId, e);
            kb.setStatus("failed");
            kb.setErrorMsg(e.getMessage());
            knowledgeBaseMapper.updateById(kb);
        }
    }

    /**
     * 分批向量化，避免单次请求超限
     */
    private List<List<Float>> batchEmbed(List<TextChunker.TextChunk> chunks) {
        List<List<Float>> allEmbeddings = new ArrayList<>(chunks.size());

        for (int i = 0; i < chunks.size(); i += EMBED_BATCH_SIZE) {
            int end = Math.min(i + EMBED_BATCH_SIZE, chunks.size());
            List<TextChunker.TextChunk> batch = chunks.subList(i, end);

            List<dev.langchain4j.data.segment.TextSegment> segments = batch.stream()
                    .map(c -> dev.langchain4j.data.segment.TextSegment.from(c.getContent()))
                    .toList();

            var response = embeddingModel.embedAll(segments);
            response.content().forEach(e -> allEmbeddings.add(e.vectorAsList()));

            log.debug("向量化进度: {}/{}", end, chunks.size());
        }

        return allEmbeddings;
    }
}
