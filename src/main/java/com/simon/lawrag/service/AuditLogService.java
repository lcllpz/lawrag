package com.simon.lawrag.service;

import com.simon.lawrag.entity.SysAuditLog;
import com.simon.lawrag.entity.vo.PageVO;

/**
 * 审计日志 Service
 */
public interface AuditLogService {

    /**
     * 分页查询审计日志（管理员使用）
     *
     * @param current   当前页码
     * @param size      每页条数
     * @param username  按用户名过滤（可为空）
     * @param operation 按操作描述过滤（可为空）
     * @param startDate 开始日期，格式 yyyy-MM-dd（可为空）
     * @param endDate   结束日期，格式 yyyy-MM-dd（可为空）
     */
    PageVO<SysAuditLog> listLogs(int current, int size, String username, String operation,
                                  String startDate, String endDate);

    /**
     * 记录一条审计日志
     *
     * @param userId     操作用户 ID
     * @param username   操作用户名
     * @param operation  操作描述
     * @param method     HTTP 方法
     * @param requestUrl 请求 URL
     * @param ip         客户端 IP
     * @param status     1成功 0失败
     */
    void log(Long userId, String username, String operation, String method,
             String requestUrl, String ip, int status);
}
