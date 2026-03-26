import request from '@/utils/request'
import type { Conversation, Message } from '@/types/chat'

export const chatApi = {
  listConversations: (params: { size?: number } = {}): Promise<{
    records?: Conversation[]
  }> => request.get('/chat/conversations', { params }),

  getHistory: (
    conversationId: number,
    params: { size?: number } = {}
  ): Promise<{
    records?: Message[]
  }> => request.get(`/chat/history/${conversationId}`, { params }),

  deleteConversation: (conversationId: number): Promise<void> =>
    request.delete(`/chat/conversations/${conversationId}`),

  submitFeedback: (messageId: number, rating: number): Promise<void> =>
    request.post('/chat/feedback', { messageId, rating }),
}

