package com.simon.lawrag.controller;

import com.simon.lawrag.common.annotation.AuditLog;
import com.simon.lawrag.common.result.Result;
import com.simon.lawrag.entity.SysNotification;
import com.simon.lawrag.entity.SysUser;
import com.simon.lawrag.entity.vo.PageVO;
import com.simon.lawrag.mapper.SysUserMapper;
import com.simon.lawrag.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统通知接口
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SysUserMapper sysUserMapper;

    /**
     * 获取当前用户未读通知数量
     */
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        Long userId = notificationService.getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 分页查询当前用户通知列表
     */
    @GetMapping("/list")
    public Result<PageVO<SysNotification>> listNotifications(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "type", required = false) String type) {
        Long userId = notificationService.getCurrentUserId();
        return Result.success(notificationService.listNotifications(userId, current, size, type));
    }

    /**
     * 标记单条通知为已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        Long userId = notificationService.getCurrentUserId();
        notificationService.markRead(id, userId);
        return Result.success();
    }

    /**
     * 将当前用户所有通知标记为已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        Long userId = notificationService.getCurrentUserId();
        notificationService.markAllRead(userId);
        return Result.success();
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        Long userId = notificationService.getCurrentUserId();
        notificationService.deleteNotification(id, userId);
        return Result.success();
    }

    /**
     * 管理员发送通知（可发给全体用户或指定用户）
     * body: { title, content, type, targetUserId(可选,不传则发给所有用户) }
     */
    @AuditLog("发送系统通知")
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> sendNotification(@RequestBody Map<String, Object> body) {
        String title = body.get("title").toString();
        String content = body.get("content").toString();
        String type = body.getOrDefault("type", "system").toString();
        Object targetUserIdObj = body.get("targetUserId");

        if (targetUserIdObj != null && !targetUserIdObj.toString().isEmpty()) {
            // 发给指定用户
            Long targetUserId = Long.valueOf(targetUserIdObj.toString());
            notificationService.sendNotification(targetUserId, title, content, type);
            return Result.success("通知已发送给指定用户");
        } else {
            // 广播给所有正常用户
            List<SysUser> users = sysUserMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getDeleted, 0)
                            .eq(SysUser::getStatus, 1)
                            .select(SysUser::getId));
            for (SysUser user : users) {
                notificationService.sendNotification(user.getId(), title, content, type);
            }
            return Result.success("通知已广播给全体 " + users.size() + " 位用户");
        }
    }
}
