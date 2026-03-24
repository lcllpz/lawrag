package com.simon.lawrag.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问题模板实体
 */
@Data
@TableName("sys_question_template")
public class SysQuestionTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模板标题 */
    private String title;

    /** 模板内容（完整问题文本） */
    private String content;

    /** 分类 */
    private String category;

    /** 排序序号 */
    private Integer sortOrder;

    /** 状态: 1启用 0禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
