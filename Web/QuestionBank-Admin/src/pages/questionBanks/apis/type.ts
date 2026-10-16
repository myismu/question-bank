// 题库 DTO 类型定义
export interface QuestionBanksDTO {
  id: string | number
  userId: string | number
  name: string
  description: string
  coverImage: string
  type: "public" | "private"
  difficulty: number
  questionCount: number
  createdAt: string
  updatedAt: string
}

// 分页查询参数类型
type Nullable<T> = T | null | undefined
export interface QuestionBanksPageParams {
  current?: number
  size?: number
  name?: string
  userId?: Nullable<string | number>
  type?: Nullable<"public" | "private">
  difficulty?: Nullable<number>
}

// 题库表单类型
export interface QuestionBanksForm {
  id?: string | number
  userId?: string | number
  name: string
  description: string
  coverImage: string
  type: "public" | "private"
  difficulty: number
}