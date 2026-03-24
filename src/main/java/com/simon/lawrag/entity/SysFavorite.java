package com.simon.lawrag.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息收藏实体
 */
@Data
@TableName("sys_favorite")
public class SysFavorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 收藏用户ID */
    private Long userId;

    /** 被收藏的消息ID */
    private Long messageId;

    /** 所属会话ID */
    private Long conversationId;

    /** 收藏备注 */
    private String note;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
