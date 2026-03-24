package com.simon.lawrag.service;

import com.simon.lawrag.entity.vo.PageVO;

import java.util.Map;

/**
 * 消息收藏 Service
 */
public interface FavoriteService {

    /**
     * 收藏消息（当前登录用户）
     *
     * @param messageId      被收藏的消息 ID
     * @param conversationId 所属会话 ID
     * @param note           收藏备注（可为空）
     */
    void addFavorite(Long messageId, Long conversationId, String note);

    /**
     * 取消收藏
     *
     * @param favoriteId 收藏记录 ID
     */
    void removeFavorite(Long favoriteId);

    /**
     * 分页查询当前用户的收藏列表
     * 每条记录包含 favorite 信息、messageContent、conversationId
     */
    PageVO<Map<String, Object>> listFavorites(int current, int size);

    /**
     * 判断当前用户是否已收藏指定消息
     *
     * @param messageId 消息 ID
     * @return true 已收藏
     */
    boolean isFavorited(Long messageId);
}
