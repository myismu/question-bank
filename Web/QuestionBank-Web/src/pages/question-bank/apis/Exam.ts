// 定义ExamAnswerDTO类型
import type { PracticeRecordDTO } from "./type"
import { request } from "@/http/axios"

export interface ExamAnswerDTO {
  id?: string
  examId?: string
  questionId: string
  userAnswer: string
  isCorrect: number
  score: number
}

export async function saveExamRecord(
  record: PracticeRecordDTO,
  details: ExamAnswerDTO[]
): Promise<void> {
  await request({
    url: "/api/public/examRecords/save",
    method: "POST",
    data: { record, details }
  })
}
