package com.simon.lawrag.common.annotation;

import java.lang.annotation.*;

/**
 * 操作审计日志注解
 * 标注在 Controller 方法上，自动记录操作人、操作类型、请求信息
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {
    /** 操作描述，如"上传知识库文档" */
    String value();
}
