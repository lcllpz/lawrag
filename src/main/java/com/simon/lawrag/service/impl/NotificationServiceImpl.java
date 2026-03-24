package com.simon.lawrag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simon.lawrag.common.exception.BusinessException;
import com.simon.lawrag.common.result.ResultCode;
import com.simon.lawrag.entity.SysNotification;
import com.simon.lawrag.entity.SysUser;
import com.simon.lawrag.entity.vo.PageVO;
import com.simon.lawrag.mapper.SysNotificationMapper;
import com.simon.lawrag.mapper.SysUserMapper;
import com.simon.lawrag.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统通知服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final SysNotificationMapper notificationMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public long getUnreadCount(Long userId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<SysNotification>()
                        .eq(SysNotification::getUserId, userId)
                        .eq(SysNotification::getIsRead, 0));
    }

    @Override
    public PageVO<SysNotification> listNotifications(Long userId, int current, int size, String type) {
        Page<SysNotification> page = new Page<>(current, size);
        notificationMapper.selectPage(page,
                new LambdaQueryWrapper<SysNotification>()
                        .eq(SysNotification::getUserId, userId)
                        .eq(StringUtils.isNotBlank(type), SysNotification::getType, type)
                        .orderByDesc(SysNotification::getCreateTime));
        return PageVO.of(page);
    }

    @Override
    @Transactional
    public void markRead(Long notificationId, Long userId) {
        SysNotification notification = notificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            throw new BusinessException("通知不存在或无权操作");
        }
        notification.setIsRead(1);
        notificationMapper.updateById(notification);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        notificationMapper.update(null,
                new LambdaUpdateWrapper<SysNotification>()
                        .eq(SysNotification::getUserId, userId)
                        .eq(SysNotification::getIsRead, 0)
                        .set(SysNotification::getIsRead, 1));
    }

    @Override
    @Transactional
    public void sendNotification(Long userId, String title, String content, String type) {
        SysNotification notification = new SysNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
        log.info("向用户 {} 发送通知: {}", userId, title);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        SysNotification notification = notificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            throw new BusinessException("通知不存在或无权操作");
        }
        notificationMapper.deleteById(notificationId);
    }

    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user.getId();
    }
}
