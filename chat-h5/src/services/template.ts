import request from '@/utils/request'

export type QuestionTemplate = {
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
}

