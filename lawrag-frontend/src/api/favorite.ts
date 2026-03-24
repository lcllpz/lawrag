import request from '@/utils/request'

export interface Favorite {
  id: number
  userId: number
  messageId: number
  conversationId: number
  note: string
  createTime: string
  messageContent?: string
}

export const favoriteApi = {
  add: (data: { messageId: number; conversationId: number; note?: string }): Promise<void> =>
    request.post('/favorite/', data),

  remove: (id: number): Promise<void> =>
    request.delete(`/favorite/${id}`),

  getList: (params: { current?: number; size?: number }): Promise<any> =>
    request.get('/favorite/list', { params }),

  check: (messageId: number): Promise<{ favorited: boolean; favoriteId?: number }> =>
    request.get(`/favorite/check/${messageId}`)
}
