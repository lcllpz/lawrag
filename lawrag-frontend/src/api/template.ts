import request from '@/utils/request'

export interface QuestionTemplate {
  id?: number
  title: string
  content: string
  category: string
  sortOrder?: number
  status?: number
  createTime?: string
}

export const templateApi = {
  getEnabled: (): Promise<QuestionTemplate[]> =>
    request.get('/template/enabled'),

  getList: (params: { current?: number; size?: number; category?: string }): Promise<any> =>
    request.get('/template/list', { params }),

  create: (data: QuestionTemplate): Promise<void> =>
    request.post('/template/', data),

  update: (id: number, data: QuestionTemplate): Promise<void> =>
    request.put(`/template/${id}`, data),

  delete: (id: number): Promise<void> =>
    request.delete(`/template/${id}`),

  toggle: (id: number): Promise<void> =>
    request.put(`/template/${id}/toggle`)
}
