// 公共题库详情类型
export interface PublicQuestionBankDetail {
  id: string
  userId: string
  name: string
  userName: string
  description: string
  coverImage: string
  type: string
  difficulty: number
  questionCount: number | null
  isCollected: boolean
  questionsDTOList: PublicQuestionDTO[]
}

// 公共题库题目类型
export interface PublicQuestionDTO {
  id: string
  bankId: string
  type: string // single, multiple, judge, fill, short
  title: string
  options: Record<string, string> // Map<String,String>
  answer: string[][]// List< List<String>>
  analysis: string
  chapterId: string
  difficulty: number
  sortOrder: number
}

// 题型映射
function mapQuestionType(type: string): string {
  switch (type) {
    case "single": return "单选题"
    case "multiple": return "多选题"
    case "judge": return "判断题"
    case "fill": return "填空题"
    case "short": return "简答题"
    default: return "未知题型"
  }
}

// 题目数据转页面Question类型
export function mapToQuestion(data: PublicQuestionDTO) {
  let options: { key: string, content: string }[] = []
  if (data.options && typeof data.options === "object") {
    options = Object.entries(data.options).map(([key, content]) => ({ key, content }))
  }
  return {
    id: data.id,
    type: mapQuestionType(data.type),
    content: data.title,
    options,
    answer: Array.isArray(data.answer) ? data.answer : [],
    analysis: data.analysis
  }
}

// 用户题库列表项类型
export interface UserQuestionBankItem {
  id: string
  userId: string
  name: string
  description: string
  coverImage: string
  type: string
  difficulty: number
  questionCount: number | null
}

// 前端练习记录DTO（与后端PublicPracticeRecordsDTO一致）
export interface PracticeRecordDTO {
  id?: string
  userId: string
  bankId: string
  startTime: string
  endTime: string
  totalQuestions: number
  correctCount: number
  wrongCount: number
  status: string // in_progress/completed/abandoned

}

// 练习明细DTO（可选，便于扩展）
export interface PracticeRecordDetailDTO {
  questionId: string
  userAnswer: string[][]
  correctAnswer: string[]
  isCorrect: boolean
}

// 章节DTO类型
export interface ChapterDTO {
  id?: string // 雪花算法生成，前端可不传
  bankId?: string // 所属题库ID，前端可不传
  title: string // 章节标题
  sortOrder: number // 排序字段
}

// 创建题库DTO
export interface CreateQuestionDTO {
  id?: string
  bankId?: string
  type: string
  title: string
  options: Record<string, string> | null
  answer: string[][]
  analysis: string
  difficulty: number
  sortOrder: number
  chapterId: string // 新增，默认为 '0'
}

export interface CreateQuestionBankDTO {
  userId: string
  name: string
  description: string
  coverImage: string
  type: string
  difficulty: number
  questionsDTOList: CreateQuestionDTO[]
  chaptersDTO?: ChapterDTO // 新增，题库下的章节列表
}

// 分页响应类型
export interface PageResponse<T> {
  code: number
  message: string
  data: {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
  }
}

// 活动记录DTO（对应后端PublicActivityRecordsDTO）
export interface ActivityRecordDTO {
  id?: string
  userId: string
  bankId: string
  type: string // practice/exam
  startTime: string
  endTime: string
  totalScore?: number // 考试用
  userScore?: number // 考试用
  status: string // in_progress/completed/timeout/abandoned
  activityAnswersList?: ActivityAnswerDTO[]
  answers?: any[][] // 用于提交考试答案数据
  questionCount?: number // 题目数量
  currentQuestionIndex?: number // 当前答到第几题（从0开始）
  chapterIds?: string[] // 本次练习选择的章节ID
  questionIds?: string[] // 本次练习生成的题目ID列表（有顺序）
  questionTypes?: string[] // 题型配置，如["single","multiple"]
}

// 活动答题记录DTO（对应后端PublicActivityAnswersDTO）
export interface ActivityAnswerDTO {
  id?: string
  recordId: string
  questionId: string
  userAnswer: string[]
  isCorrect: number // 0-错误，1-正确
  score?: number // 考试用
  answerTime?: string
}

// 更新题库信息DTO
export interface UpdateQuestionBankDTO {
  id: string
  userId: string
  name: string
  description: string
  coverImage: string
  type: string
  difficulty: number
  questionCount: number | null
  isCollected: boolean
}

// 分页返回类型
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
