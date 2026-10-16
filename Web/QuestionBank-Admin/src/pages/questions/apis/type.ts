// 选项类型定义
export interface Option {
  label: string
  value: string
}

// 题目 DTO 类型定义
export interface QuestionsDTO {
  id: string | number
  bankId: string | number
  type: "single" | "multiple" | "fill" | "judge" | "short"
  title: string
  options: Option[]
  answer: any
  analysis: string
  difficulty: number
  sortOrder: number
  createdAt: string
  updatedAt: string
}

// 分页查询参数
export interface QuestionsPageParams {
  current?: number
  size?: number
  title?: string
  bankId?: string | number
  type?: "single" | "multiple" | "fill" | "judge" | "short"
  difficulty?: number
}

// 题目表单类型
export interface QuestionsForm {
  id?: string | number
  bankId: string | number
  type: "single" | "multiple" | "fill" | "judge" | "short"
  title: string
  options: Option[]
  answer: any
  analysis: string
  difficulty: number
  sortOrder: number
}
