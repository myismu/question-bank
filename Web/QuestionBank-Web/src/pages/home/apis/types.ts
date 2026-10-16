export interface QuestionOption {
  key: string
  content: string
}

export interface PreviewQuestion {
  type: string
  difficulty: string
  content: string
  options: QuestionOption[]
}

export interface QuestionBank {
  id: string
  name: string
  userName: string
  description: string
  difficulty: string
  questionCount: number
  viewCount: number
  starCount: number
  isPublic: boolean
  image: string
  previewQuestions: PreviewQuestion[]
}

// 后端返回的公共题库类型
export interface PublicQuestionBankResponse {
  id: string
  userId: string
  name: string
  userName: string
  description: string
  coverImage: string
  type: string
  difficulty: number // 1=简单, 2=中等, 3=困难, 4=困难
  questionCount: number | null
}

// 后端返回的题目类型
export interface PublicQuestionsDTO {
  id: string
  bankId: string
  type: string // single, multiple, judge, fill, short
  title: string
  options: string | null // 可能是JSON字符串或null
  answer: string
  analysis: string
  difficulty: number
  sortOrder: number
}

// 难度数字转字符串
export function mapDifficulty(num: number): string {
  switch (num) {
    case 1:
      return "简单"
    case 2:
      return "中等"
    case 3:
    case 4:
      return "困难"
    default:
      return "未知"
  }
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

// 后端数据转前端QuestionBank类型
export function mapToQuestionBank(
  data: PublicQuestionBankResponse
): QuestionBank {
  return {
    id: String(data.id),
    name: data.name,
    userName: data.userName,
    description: data.description,
    difficulty: mapDifficulty(data.difficulty),
    questionCount: data.questionCount ?? 0,
    viewCount: 0, // 后端无此字段
    starCount: 0, // 后端无此字段
    isPublic: true, // 固定为true
    image: data.coverImage,
    previewQuestions: [] // 后端无此字段
  }
}

// 题目数据转PreviewQuestion
export function mapToPreviewQuestion(data: PublicQuestionsDTO): PreviewQuestion {
  let options: QuestionOption[] = []
  if (data.options) {
    try {
      const parsed = JSON.parse(data.options)
      if (Array.isArray(parsed)) {
        options = parsed.map((item: any) => ({
          key: item.key || "",
          content: item.content || ""
        }))
      }
    } catch {}
  }
  return {
    type: mapQuestionType(data.type),
    difficulty: mapDifficulty(data.difficulty),
    content: data.title,
    options
  }
}

// 分页返回类型
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface PublicWrongQuestionsDTO {
  id: string // 雪花ID
  userId: string // 用户ID
  bankId: string // 题库ID
  activityAnswerId: string // 答题记录ID
  status: string // 复习状态 （ 待复习： pending 已复习： reviewed 忽略： ignored）
  questionId: string // 题目ID
  errorCount: string // 错误次数
  lastErrorTime: string // 最近错误时间（ISO 8601 格式）
  questions: Question // 题目对象
  wrongAnswer: string[] // 错误答案
}
export interface Question {
  id: string
  bankId: string
  chapterId: string | null
  type: string
  title: string
  options: Record<string, string>
  answer: string[][]
  analysis: string
  difficulty: number
  sortOrder: number
}

export interface WrongQuestionStatsVO {
  total: number // 总错题数
  today: number // 今日错题数
  reviewed: number // 已复习错题数
  unreviewed: number // 未复习错题数
}

export interface WrongQuestionsPageResult {
  records: PublicWrongQuestionsDTO[]
  total: number
  size: number
  current: number
  pages: number
}

export interface BankSimpleInfoDTO {
  bankId: string
  bankName: string
}

// 学习统计概览DTO
export interface StudyStatisticsDTO {
  totalPracticeCount: number // 总练习次数
  totalExamCount: number // 总考试次数
  correctCount: number // 正确题目数
  wrongCount: number // 错误题目数
  accuracyRate: number // 正确率
}

// 学习趋势DTO
export interface StudyTrendDTO {
  date: string // 日期（yyyy-MM-dd）
  practiceCount: number // 练习次数
  examCount: number // 考试次数
  questionCount: number // 题目数量
  correctCount: number // 正确题目数
}

// 题库进度DTO
export interface BankProgressDTO {
  bankId: string // 题库ID
  bankName: string // 题库名称
  progressPercentage: number // 进度百分比
  completedCount: number // 已完成题目数
  totalCount: number // 总题目数
  progressColor: string // 进度条颜色
  lastStudyTime?: string // 最近学习时间（可选）
}
