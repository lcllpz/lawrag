package com.simon.lawrag.controller;

import com.simon.lawrag.common.result.Result;
import com.simon.lawrag.entity.SysAuditLog;
import com.simon.lawrag.entity.vo.PageVO;
import com.simon.lawrag.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 审计日志接口
 */
@RestController
@RequestMapping("/api/audit-log")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * 分页查询审计日志（管理员）
     *
     * @param current   当前页码，默认1
     * @param size      每页条数，默认20
     * @param username  按用户名模糊过滤（可选）
     * @param operation 按操作描述模糊过滤（可选）
     * @param startDate 开始日期，格式 yyyy-MM-dd（可选）
     * @param endDate   结束日期，格式 yyyy-MM-dd（可选）
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageVO<SysAuditLog>> listLogs(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "operation", required = false) String operation,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        return Result.success(auditLogService.listLogs(current, size, username, operation, startDate, endDate));
    }
}
