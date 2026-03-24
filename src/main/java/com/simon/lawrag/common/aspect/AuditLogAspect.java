package com.simon.lawrag.common.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.simon.lawrag.common.annotation.AuditLog;
import com.simon.lawrag.entity.SysUser;
import com.simon.lawrag.mapper.SysUserMapper;
import com.simon.lawrag.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 审计日志 AOP 切面
 * 自动拦截标注了 @AuditLog 的方法，记录操作人、操作类型、IP 等信息
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final SysUserMapper sysUserMapper;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint point, AuditLog auditLog) throws Throwable {
        int status = 1;
        try {
            Object result = point.proceed();
            return result;
        } catch (Throwable e) {
            status = 0;
            throw e;
        } finally {
            try {
                saveLog(auditLog.value(), status);
            } catch (Exception ex) {
                log.warn("审计日志记录失败: {}", ex.getMessage());
            }
        }
    }

    private void saveLog(String operation, int status) {
        // 获取当前登录用户
        String username = "anonymous";
        Long userId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
            SysUser user = sysUserMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
            if (user != null) userId = user.getId();
        }

        // 获取请求信息
        String method = "UNKNOWN";
        String requestUrl = "";
        String ip = "127.0.0.1";
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            method = request.getMethod();
            requestUrl = request.getRequestURI();
            ip = getClientIp(request);
        }

        auditLogService.log(userId, username, operation, method, requestUrl, ip, status);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
