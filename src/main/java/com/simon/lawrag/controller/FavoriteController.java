package com.simon.lawrag.controller;

import com.simon.lawrag.common.result.Result;
import com.simon.lawrag.entity.vo.PageVO;
import com.simon.lawrag.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息收藏接口
 */
@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 收藏消息（当前登录用户）
     *
     * @param body 包含 messageId、conversationId、note 字段的请求体
     */
    @PostMapping("/")
    public Result<Void> addFavorite(@RequestBody Map<String, Object> body) {
        Long messageId = Long.valueOf(body.get("messageId").toString());
        Long conversationId = Long.valueOf(body.get("conversationId").toString());
        String note = body.containsKey("note") && body.get("note") != null
                ? body.get("note").toString() : null;
        favoriteService.addFavorite(messageId, conversationId, note);
        return Result.success();
    }

    /**
     * 取消收藏
     *
     * @param id 收藏记录 ID
     */
    @DeleteMapping("/{id}")
    public Result<Void> removeFavorite(@PathVariable Long id) {
        favoriteService.removeFavorite(id);
        return Result.success();
    }

    /**
     * 分页查询当前用户收藏列表
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     */
    @GetMapping("/list")
    public Result<PageVO<Map<String, Object>>> listFavorites(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(favoriteService.listFavorites(current, size));
    }

    /**
     * 检查当前用户是否已收藏指定消息
     *
     * @param messageId 消息 ID
     */
    @GetMapping("/check/{messageId}")
    public Result<Boolean> isFavorited(@PathVariable Long messageId) {
        return Result.success(favoriteService.isFavorited(messageId));
    }
}
