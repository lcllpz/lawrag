package com.simon.lawrag.service;

import com.simon.lawrag.entity.vo.PageVO;
import com.simon.lawrag.entity.SysNotification;

/**
 * 系统通知 Service
 */
public interface NotificationService {

    /** 获取指定用户的未读通知数量 */
    long getUnreadCount(Long userId);

    /** 分页查询用户通知列表（type 为 null 时查全部） */
    PageVO<SysNotification> listNotifications(Long userId, int current, int size, String type);

    /** 标记单条通知为已读 */
    void markRead(Long notificationId, Long userId);

    /** 将指定用户所有通知标记为已读 */
    void markAllRead(Long userId);

    /** 发送通知（供其他服务内部调用） */
    void sendNotification(Long userId, String title, String content, String type);

    /** 删除通知 */
    void deleteNotification(Long notificationId, Long userId);

    /** 从 Security 上下文获取当前登录用户 ID */
    Long getCurrentUserId();
}
