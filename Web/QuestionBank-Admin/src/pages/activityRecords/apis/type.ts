export namespace ActivityRecords {
  // 活动记录信息
  export interface ActivityRecordsInfo {
    id: string
    userId: string
    bankId: string
    type: string
    startTime: string
    endTime?: string
    totalScore?: number
    userScore?: number
    status: 'in_progress' | 'completed' | 'timeout' | 'abandoned'
    chapterIds?: string
  }

  // 查询参数
  export interface ActivityRecordsQuery {
    userId?: string
    bankId?: string
    type?: string
    status?: string
    startTime?: string
    endTime?: string
    current: number
    size: number
  }

  // 表单参数
  export interface ActivityRecordsForm {
    id?: string
    userId: string
    bankId: string
    type: string
    startTime: string
    endTime?: string
    totalScore?: number
    userScore?: number
    status: ''
    chapterIds?: string
  }
  export interface PageResult<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

}



