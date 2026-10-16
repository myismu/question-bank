 export namespace LearningStatistics {
  export interface LearningStatisticsInfo {
    id: number | string
    userId: number | string
    bankId: number | string
    practiceCount: number
    examCount: number
    totalQuestions: number
    correctCount: number
    wrongCount: number
    averageScore: number
    createdAt: string
    updatedAt: string
  }

  export interface LearningStatisticsQuery {
    current: number
    size: number
    userId?: number | string
    bankId?: number | string
  }

  export interface LearningStatisticsForm {
    id?: number | string
    userId: number | string
    bankId: number | string
    practiceCount: number
    examCount: number
    totalQuestions: number
    correctCount: number
    wrongCount: number
    averageScore: number
  }

  export interface ApiResponse<T> {
    code: number
    message: string
    data: T
  }

  export interface PageResult<T> {
    records: T[]
    total: number
    size: number
    current: number
  }
}
