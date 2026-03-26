import request from '@/utils/request'

export const favoriteApi = {
  add: (data: { messageId: number; conversationId: number; note?: string }) =>
    request.post('/favorite/', data),

  remove: (id: number) => request.delete(`/favorite/${id}`),

  getList: (params: { current?: number; size?: number }) =>
    request.get('/favorite/list', { params }),

  check: (messageId: number) =>
    request.get(`/favorite/check/${messageId}`),
}

