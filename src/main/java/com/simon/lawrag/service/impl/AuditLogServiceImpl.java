package com.simon.lawrag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simon.lawrag.entity.SysAuditLog;
import com.simon.lawrag.entity.vo.PageVO;
import com.simon.lawrag.mapper.SysAuditLogMapper;
import com.simon.lawrag.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 审计日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final SysAuditLogMapper auditLogMapper;

    @Override
    public PageVO<SysAuditLog> listLogs(int current, int size, String username, String operation,
                                         String startDate, String endDate) {
        Page<SysAuditLog> page = new Page<>(current, size);
        LambdaQueryWrapper<SysAuditLog> wrapper = new LambdaQueryWrapper<SysAuditLog>()
                .orderByDesc(SysAuditLog::getCreateTime);

        if (StringUtils.isNotBlank(username)) {
            wrapper.like(SysAuditLog::getUsername, username);
        }
        if (StringUtils.isNotBlank(operation)) {
            wrapper.like(SysAuditLog::getOperation, operation);
        }
        if (StringUtils.isNotBlank(startDate)) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime start = LocalDate.parse(startDate, fmt).atStartOfDay();
            wrapper.ge(SysAuditLog::getCreateTime, start);
        }
        if (StringUtils.isNotBlank(endDate)) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime end = LocalDate.parse(endDate, fmt).atTime(23, 59, 59);
            wrapper.le(SysAuditLog::getCreateTime, end);
        }

        auditLogMapper.selectPage(page, wrapper);
        return PageVO.of(page);
    }

    @Override
    public void log(Long userId, String username, String operation, String method,
                    String requestUrl, String ip, int status) {
        SysAuditLog auditLog = new SysAuditLog();
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setOperation(operation);
        auditLog.setMethod(method);
        auditLog.setRequestUrl(requestUrl);
        auditLog.setIp(ip);
        auditLog.setStatus(status);
        auditLogMapper.insert(auditLog);
        log.debug("记录审计日志: user={}, operation={}, status={}", username, operation, status);
    }
}
