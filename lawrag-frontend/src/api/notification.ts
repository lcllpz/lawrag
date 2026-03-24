import request from '@/utils/request'

export interface Notification {
  id: number
  userId: number
  title: string
  content: string
  type: string // system | security | activity
  isRead: number // 0未读 1已读
  createTime: string
}

export const notificationApi = {
  getUnreadCount: (): Promise<number> =>
    request.get('/notification/unread-count'),

  getList: (params: { current?: number; size?: number }): Promise<any> =>
    request.get('/notification/list', { params }),

  markRead: (id: number): Promise<void> =>
    request.put(`/notification/${id}/read`),

  markAllRead: (): Promise<void> =>
    request.put('/notification/read-all'),

  deleteNotification: (id: number): Promise<void> =>
    request.delete(`/notification/${id}`),

  send: (data: { title: string; content: string; type: string; targetUserId?: number | null }): Promise<string> =>
    request.post('/notification/send', data)
}
