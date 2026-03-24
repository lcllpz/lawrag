package com.simon.lawrag.config;

import com.simon.lawrag.service.knowledge.MilvusService;
import com.simon.lawrag.service.knowledge.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动初始化
 * 自动创建 MinIO Bucket 和 Milvus Collection
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppInitConfig implements ApplicationRunner {

    private final MinioService minioService;
    private final MilvusService milvusService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("========== MediRAG 系统初始化 ==========");
        // 初始化 MinIO Bucket
        minioService.initBucket();
        // 初始化 Milvus Collection
        milvusService.initCollection();
        log.info("========== 初始化完成 ==========");
    }
}
