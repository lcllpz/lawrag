package com.simon.lawrag.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simon.lawrag.common.annotation.AuditLog;
import com.simon.lawrag.common.result.Result;
import com.simon.lawrag.entity.SysUser;
import com.simon.lawrag.entity.dto.LoginDTO;
import com.simon.lawrag.entity.dto.RegisterDTO;
import com.simon.lawrag.entity.dto.UserUpdateDTO;
import com.simon.lawrag.entity.vo.LoginVO;
import com.simon.lawrag.entity.vo.PageVO;
import com.simon.lawrag.service.UserService;
import com.simon.lawrag.entity.dto.ResetPasswordDTO;
import com.simon.lawrag.entity.dto.SendCodeDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户管理接口
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ==================== 找回密码（无需登录）====================

    /**
     * 发送找回密码验证码
     */
    @PostMapping("/forgot-password/send-code")
    public Result<String> sendResetCode(@Valid @RequestBody SendCodeDTO dto) {
        userService.sendResetCode(dto.getPhone());
        return Result.success("验证码已发送");
    }

    /**
     * 验证码校验并重置密码
     */
    @PostMapping("/forgot-password/reset")
    public Result<String> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto);
        return Result.success("密码重置成功");
    }

    // ==================== 登录注册 ====================

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<SysUser> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<Void> updateUser(@RequestBody UserUpdateDTO dto) {
        userService.updateUser(dto);
        return Result.success();
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(file);
        return Result.success("头像更新成功", avatarUrl);
    }

    /**
     * 更新用户档案（沿用历史字段名）
     */
    @PutMapping("/legal-profile")
    public Result<Void> updateLegalProfile(@RequestBody String legalProfile) {
        userService.updateLegalProfile(legalProfile);
        return Result.success();
    }

    // ==================== 管理员接口 ====================

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageVO<SysUser>> listUsers(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Page<SysUser> page = userService.listUsers(current, size, keyword);
        return Result.success(PageVO.of(page));
    }

    /**
     * 修改用户状态
     */
    @AuditLog("修改用户状态")
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @RequestParam("status") Integer status) {
        userService.updateUserStatus(userId, status);
        return Result.success();
    }

    /**
     * 修改用户角色
     */
    @AuditLog("修改用户角色")
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserRole(@PathVariable Long userId, @RequestParam("role") String role) {
        userService.updateUserRole(userId, role);
        return Result.success();
    }
}
