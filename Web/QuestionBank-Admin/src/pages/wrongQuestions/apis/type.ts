// 错题 DTO 类型定义
export interface WrongQuestionsDTO {
  id: string | number
  userId: string | number
  questionId: string | number
  errorCount: number
  lastErrorTime: string
  createdAt: string
  updatedAt: string
}

// 分页查询参数
export interface WrongQuestionsPageParams {
  current?: number
  size?: number
  userId?: string | number
  questionId?: string | number
}

// 错题表单类型
export interface WrongQuestionsForm {
  id?: string | number
  userId: string | number
  questionId: string | number
  errorCount: number
  lastErrorTime: string
}
