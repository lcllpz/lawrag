package com.simon.lawrag.controller;

import com.simon.lawrag.common.annotation.AuditLog;
import com.simon.lawrag.common.result.Result;
import com.simon.lawrag.entity.SysQuestionTemplate;
import com.simon.lawrag.entity.vo.PageVO;
import com.simon.lawrag.service.QuestionTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 问题模板接口
 */
@RestController
@RequestMapping("/api/template")
@RequiredArgsConstructor
public class QuestionTemplateController {

    private final QuestionTemplateService questionTemplateService;

    /**
     * 获取所有启用状态的模板（公开，用于 Chat 页面展示）
     */
    @GetMapping("/enabled")
    public Result<List<SysQuestionTemplate>> listEnabledTemplates() {
        return Result.success(questionTemplateService.listEnabledTemplates());
    }

    /**
     * 分页查询模板列表（管理员）
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageVO<SysQuestionTemplate>> listTemplates(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "category", required = false) String category) {
        return Result.success(questionTemplateService.listTemplates(current, size, category));
    }

    /**
     * 创建问题模板（管理员）
     */
    @AuditLog("创建问题模板")
    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> createTemplate(@RequestBody SysQuestionTemplate template) {
        questionTemplateService.createTemplate(template);
        return Result.success();
    }

    /**
     * 更新问题模板（管理员）
     */
    @AuditLog("更新问题模板")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateTemplate(@PathVariable Long id, @RequestBody SysQuestionTemplate template) {
        template.setId(id);
        questionTemplateService.updateTemplate(template);
        return Result.success();
    }

    /**
     * 删除问题模板（管理员，逻辑删除）
     */
    @AuditLog("删除问题模板")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        questionTemplateService.deleteTemplate(id);
        return Result.success();
    }

    /**
     * 切换模板启用 / 禁用状态（管理员）
     */
    @AuditLog("切换模板状态")
    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        questionTemplateService.toggleStatus(id);
        return Result.success();
    }
}
