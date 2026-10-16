export namespace QuestionChapters {
  export interface ChapterInfo {
    id: number | string
    bankId: number | string
    title: string
    description?: string
    createdAt: string
    updatedAt: string
  }

  export interface ChapterQuery {
    current: number
    size: number
    title?: string
    bankId?: number | string
  }

  export interface ChapterForm {
    id?: number | string
    bankId: number | string
    title: string
    description?: string
  }


  export interface PageResult<T> {
    records: T[]
    total: number
    size: number
    current: number
  }
}
