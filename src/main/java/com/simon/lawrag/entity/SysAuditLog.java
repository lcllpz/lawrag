package com.simon.lawrag.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作审计日志实体
 */
@Data
@TableName("sys_audit_log")
public class SysAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作用户ID */
    private Long userId;

    /** 操作用户名 */
    private String username;

    /** 操作描述，如: 创建知识库、删除用户 */
    private String operation;

    /** HTTP 方法: GET/POST/PUT/DELETE */
    private String method;

    /** 请求 URL */
    private String requestUrl;

    /** 客户端 IP */
    private String ip;

    /** 操作状态: 1成功 0失败 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
