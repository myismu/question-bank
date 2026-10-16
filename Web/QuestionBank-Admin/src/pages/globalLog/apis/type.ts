// 全局日志 DTO 类型定义
export interface GlobalLogDTO {
  id: string | number
  requestUrl: string
  requestMethod: string
  ipAddress: string
  requestHeaders: string
  requestParams: string
  responseStatusCode: number
  responseTime: number
  responseHeaders: string
  responseBody: string
  exceptionMessage: string
  exceptionStackTrace: string
  userId: string | number
  userName: string
  userAgent: string
  createTime: string
  updateTime: string
}

// 分页查询参数类型
type Nullable<T> = T | null | undefined
export interface GlobalLogPageParams {
  current?: number
  size?: number
  requestUrl?: string
  userName?: string
  responseStatusCode?: Nullable<number>
}
