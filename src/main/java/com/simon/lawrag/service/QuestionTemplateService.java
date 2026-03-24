package com.simon.lawrag.service;

import com.simon.lawrag.entity.SysQuestionTemplate;
import com.simon.lawrag.entity.vo.PageVO;

import java.util.List;

/**
 * 问题模板 Service
 */
public interface QuestionTemplateService {

    /** 分页查询（支持按分类过滤，公开接口） */
    PageVO<SysQuestionTemplate> listTemplates(int current, int size, String category);

    /** 获取所有启用状态的模板（用于 Chat 页面展示） */
    List<SysQuestionTemplate> listEnabledTemplates();

    /** 创建模板（管理员） */
    void createTemplate(SysQuestionTemplate template);

    /** 更新模板（管理员） */
    void updateTemplate(SysQuestionTemplate template);

    /** 删除模板（管理员，逻辑删除） */
    void deleteTemplate(Long id);

    /** 切换启用 / 禁用状态 */
    void toggleStatus(Long id);
}
