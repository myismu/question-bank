export namespace BankTags {
  export interface BankTagInfo {
    id: number | string
    bankId: number | string
    tagName: string
    createdAt: string
  }

  export interface BankTagQuery {
    current: number
    size: number
    tagName?: string
    bankId?: number | string
  }

  export interface BankTagForm {
    id?: number | string
    bankId: number | string
    tagName: string
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
