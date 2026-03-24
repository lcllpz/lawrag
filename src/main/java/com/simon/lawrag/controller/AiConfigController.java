package com.simon.lawrag.controller;

import com.simon.lawrag.common.annotation.AuditLog;
import com.simon.lawrag.common.result.Result;
import com.simon.lawrag.entity.SysAiConfig;
import com.simon.lawrag.service.AiConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 配置中心接口（仅管理员可访问，由 SecurityConfig /api/admin/** 规则保护）
 */
@RestController
@RequestMapping("/api/admin/ai-config")
@RequiredArgsConstructor
public class AiConfigController {

    private final AiConfigService aiConfigService;

    /** 可选模型列表 */
    private static final List<String> AVAILABLE_MODELS =
            List.of("qwen-turbo", "qwen-plus", "qwen-max", "qwen-max-longcontext");

    /**
     * 查询全部配置（按分组）
     * GET /api/admin/ai-config/list
     */
    @GetMapping("/list")
    public Result<Map<String, List<SysAiConfig>>> list() {
        return Result.success(aiConfigService.getAllGrouped());
    }

    /**
     * 批量更新配置
     * PUT /api/admin/ai-config/batch
     * body: {"rag.vector_top_k": "15", "llm.model": "qwen-max"}
     */
    @AuditLog("批量更新AI配置")
    @PutMapping("/batch")
    public Result<Void> batchUpdate(@RequestBody Map<String, String> params) {
        aiConfigService.batchUpdate(params);
        return Result.success();
    }

    /**
     * 重置指定分组为默认值
     * POST /api/admin/ai-config/reset/{groupName}
     */
    @AuditLog("重置AI配置分组")
    @PostMapping("/reset/{groupName}")
    public Result<Void> resetGroup(@PathVariable String groupName) {
        aiConfigService.resetGroup(groupName);
        return Result.success();
    }

    /**
     * 重置全部配置为默认值
     * POST /api/admin/ai-config/reset-all
     */
    @PostMapping("/reset-all")
    public Result<Void> resetAll() {
        aiConfigService.resetAll();
        return Result.success();
    }

    /**
     * 获取可选模型列表（前端下拉用）
     * GET /api/admin/ai-config/models
     */
    @GetMapping("/models")
    public Result<List<String>> models() {
        return Result.success(AVAILABLE_MODELS);
    }
}
