package com.simon.lawrag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simon.lawrag.common.exception.BusinessException;
import com.simon.lawrag.common.result.ResultCode;
import com.simon.lawrag.entity.MedKnowledgeBase;
import com.simon.lawrag.mapper.MedKnowledgeBaseMapper;
import com.simon.lawrag.service.KnowledgeBaseService;
import com.simon.lawrag.service.knowledge.DocumentExtractor;
import com.simon.lawrag.service.knowledge.MilvusService;
import com.simon.lawrag.service.knowledge.TextChunker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 知识库服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final MedKnowledgeBaseMapper knowledgeBaseMapper;
    private final MilvusService milvusService;
    private final DocumentExtractor documentExtractor;
    private final TextChunker textChunker;
    private final DocumentProcessTask documentProcessTask;

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Override
    @Transactional
    public Long uploadDocument(MultipartFile file, String category, String description) {
        // 1. 校验文件类型
        String originalName = file.getOriginalFilename();
        String fileType = getFileExtension(originalName);
        if (!List.of("pdf", "docx", "doc", "txt").contains(fileType.toLowerCase())) {
            throw new BusinessException("不支持的文件格式，请上传 PDF/Word/TXT 文件");
        }

        // 2. 保存文件到本地磁盘
        String fileUrl = saveToLocal(file, originalName);

        // 3. 创建知识库记录
        MedKnowledgeBase kb = new MedKnowledgeBase();
        kb.setName(originalName);
        kb.setDescription(description);
        kb.setCategory(category);
        kb.setFileUrl(fileUrl);
        kb.setFileType(fileType);
        kb.setFileSize(file.getSize());
        kb.setStatus("uploading");
        kb.setChunkCount(0);
        knowledgeBaseMapper.insert(kb);

        // 4. 事务提交后再触发异步任务，避免异步线程读到未提交数据导致状态卡在 uploading
        final Long kbId = kb.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                documentProcessTask.process(kbId);
            }
        });

        log.info("文档上传成功: id={}, name={}", kb.getId(), originalName);
        return kb.getId();
    }

    @Override
    public Page<MedKnowledgeBase> listKnowledge(Integer current, Integer size,
                                                   String category, String status) {
        Page<MedKnowledgeBase> page = new Page<>(current, size);
        LambdaQueryWrapper<MedKnowledgeBase> wrapper = new LambdaQueryWrapper<MedKnowledgeBase>()
                .eq(MedKnowledgeBase::getDeleted, 0)
                .eq(StringUtils.isNotBlank(category), MedKnowledgeBase::getCategory, category)
                .eq(StringUtils.isNotBlank(status), MedKnowledgeBase::getStatus, status)
                .orderByDesc(MedKnowledgeBase::getCreateTime);
        return knowledgeBaseMapper.selectPage(page, wrapper);
    }

    @Override
    public MedKnowledgeBase getById(Long id) {
        MedKnowledgeBase kb = knowledgeBaseMapper.selectById(id);
        if (kb == null || kb.getDeleted() == 1) {
            throw new BusinessException(ResultCode.KNOWLEDGE_NOT_FOUND);
        }
        return kb;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        MedKnowledgeBase kb = getById(id);
        // 删除 Milvus 向量（失败不影响主流程，向量丢失可接受）
        milvusService.deleteByKnowledgeBaseId(id);
        // 删除本地文件
        deleteLocalFile(kb.getFileUrl());
        // 逻辑删除：必须用 deleteById 而非 updateById
        // @TableLogic 字段会被 updateById 的 SET 子句忽略，直接 setDeleted(1)+updateById 无效
        knowledgeBaseMapper.deleteById(id);
        log.info("知识库删除完成: id={}", id);
    }

    @Override
    public List<String> listCategories() {
        return knowledgeBaseMapper.selectList(
                new LambdaQueryWrapper<MedKnowledgeBase>()
                        .eq(MedKnowledgeBase::getDeleted, 0)
                        .select(MedKnowledgeBase::getCategory)
                        .groupBy(MedKnowledgeBase::getCategory)
        ).stream().map(MedKnowledgeBase::getCategory).distinct().collect(Collectors.toList());
    }

    @Override
    public void reprocess(Long id) {
        MedKnowledgeBase kb = getById(id);
        if (!"failed".equals(kb.getStatus())) {
            throw new BusinessException("只有处理失败的文档才能重新处理");
        }
        // 先删除旧向量
        milvusService.deleteByKnowledgeBaseId(id);
        updateStatus(kb, "uploading", null);
        documentProcessTask.process(id);
    }

    // ==================== 私有方法 ====================

    private void updateStatus(MedKnowledgeBase kb, String status, String errorMsg) {
        kb.setStatus(status);
        kb.setErrorMsg(errorMsg);
        knowledgeBaseMapper.updateById(kb);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "txt";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 保存文件到本地磁盘，返回相对路径（作为 fileUrl 存入数据库）
     */
    private String saveToLocal(MultipartFile file, String originalName) {
        try {
            String ext = getFileExtension(originalName);
            String filename = UUID.randomUUID() + "." + ext;
            Path dir = Paths.get(uploadPath, "knowledge");
            Files.createDirectories(dir);
            Path dest = dir.resolve(filename);
            file.transferTo(dest.toAbsolutePath().toFile());
            // 返回相对路径，供 DocumentProcessTask 读取
            String relativePath = "knowledge/" + filename;
            log.info("文档已保存到本地: {}", dest.toAbsolutePath());
            return relativePath;
        } catch (IOException e) {
            throw new BusinessException("文件保存失败: " + e.getMessage());
        }
    }

    /**
     * 删除本地文件
     */
    private void deleteLocalFile(String fileUrl) {
        try {
            Path path = Paths.get(uploadPath, fileUrl);
            Files.deleteIfExists(path);
            log.info("本地文件已删除: {}", path.toAbsolutePath());
        } catch (IOException e) {
            log.warn("本地文件删除失败: {}", e.getMessage());
        }
    }
}
