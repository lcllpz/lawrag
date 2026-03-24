import request from '@/utils/request'

export interface AuditLog {
  id: number
  userId: number
  username: string
  operation: string
  method: string
  requestUrl: string
  ip: string
  status: number
  createTime: string
}

export const auditLogApi = {
  getList: (params: {
    current?: number
    size?: number
    username?: string
    operation?: string
    startDate?: string
    endDate?: string
  }): Promise<any> =>
    request.get('/audit-log/list', { params })
}
