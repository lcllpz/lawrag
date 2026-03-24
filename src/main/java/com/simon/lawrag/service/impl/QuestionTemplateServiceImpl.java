package com.simon.lawrag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simon.lawrag.common.exception.BusinessException;
import com.simon.lawrag.entity.SysQuestionTemplate;
import com.simon.lawrag.entity.vo.PageVO;
import com.simon.lawrag.mapper.SysQuestionTemplateMapper;
import com.simon.lawrag.service.QuestionTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 问题模板服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionTemplateServiceImpl implements QuestionTemplateService {

    private final SysQuestionTemplateMapper templateMapper;

    @Override
    public PageVO<SysQuestionTemplate> listTemplates(int current, int size, String category) {
        Page<SysQuestionTemplate> page = new Page<>(current, size);
        LambdaQueryWrapper<SysQuestionTemplate> wrapper = new LambdaQueryWrapper<SysQuestionTemplate>()
                .eq(SysQuestionTemplate::getDeleted, 0)
                .orderByAsc(SysQuestionTemplate::getSortOrder)
                .orderByDesc(SysQuestionTemplate::getCreateTime);

        if (StringUtils.isNotBlank(category)) {
            wrapper.eq(SysQuestionTemplate::getCategory, category);
        }

        templateMapper.selectPage(page, wrapper);
        return PageVO.of(page);
    }

    @Override
    public List<SysQuestionTemplate> listEnabledTemplates() {
        return templateMapper.selectList(
                new LambdaQueryWrapper<SysQuestionTemplate>()
                        .eq(SysQuestionTemplate::getDeleted, 0)
                        .eq(SysQuestionTemplate::getStatus, 1)
                        .orderByAsc(SysQuestionTemplate::getSortOrder));
    }

    @Override
    @Transactional
    public void createTemplate(SysQuestionTemplate template) {
        if (StringUtils.isBlank(template.getTitle())) {
            throw new BusinessException("模板标题不能为空");
        }
        if (StringUtils.isBlank(template.getContent())) {
            throw new BusinessException("模板内容不能为空");
        }
        if (template.getStatus() == null) {
            template.setStatus(1);
        }
        if (template.getSortOrder() == null) {
            template.setSortOrder(0);
        }
        templateMapper.insert(template);
        log.info("创建问题模板: {}", template.getTitle());
    }

    @Override
    @Transactional
    public void updateTemplate(SysQuestionTemplate template) {
        if (template.getId() == null) {
            throw new BusinessException("模板ID不能为空");
        }
        SysQuestionTemplate existing = templateMapper.selectById(template.getId());
        if (existing == null) {
            throw new BusinessException("模板不存在");
        }
        templateMapper.updateById(template);
        log.info("更新问题模板: id={}", template.getId());
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        SysQuestionTemplate existing = templateMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("模板不存在");
        }
        templateMapper.deleteById(id);
        log.info("删除问题模板: id={}", id);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        SysQuestionTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        template.setStatus(template.getStatus() == 1 ? 0 : 1);
        templateMapper.updateById(template);
        log.info("切换问题模板状态: id={}, newStatus={}", id, template.getStatus());
    }
}
