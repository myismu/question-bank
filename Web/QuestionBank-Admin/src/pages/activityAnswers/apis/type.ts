/**
 * 答题记录信息
 */
export interface ActivityAnswersInfo {
  id?: number
  recordId?: string
  questionId?: number
  answer?: string
  isCorrect?: boolean
  score?: number
  createTime?: string
  updateTime?: string
}

/**
 * 答题记录查询参数
 */
export interface ActivityAnswersQuery {
  current: number
  size: number
  recordId?: string
  questionId?: number
}

/**
 * 答题记录表单
 */
export interface ActivityAnswersForm {
  id?: number
  recordId?: string
  questionId?: number
  answer?: string
  isCorrect?: boolean
  score?: number
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
