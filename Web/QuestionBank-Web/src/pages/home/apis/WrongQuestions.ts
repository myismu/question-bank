import type { BankSimpleInfoDTO, WrongQuestionsPageResult, WrongQuestionStatsVO } from "./types"
import { request } from "@/http/axios"

// 获取错题列表
export function getPageWrongQuestionsList(pageNum: number, pageSize: number, bankId?: string, type?: string, status?: string): Promise<ApiResponseData<WrongQuestionsPageResult>> {
  return request({
    url: "/api/public/wrongQuestions/getPageWrongQuestionsList",
    method: "get",
    params: { pageNum, pageSize, bankId, type, status }
  })
}

// 获取错题统计信息
export function getWrongQuestionStats(): Promise<ApiResponseData<WrongQuestionStatsVO>> {
  return request({
    url: "/api/public/wrongQuestions/getWrongQuestionStats",
    method: "get"
  })
}

// 移除错题
export function removeWrongQuestion(wrongQuestionId: string): Promise<ApiResponseData<string>> {
  return request({
    url: "/api/public/wrongQuestions/removeWrongQuestion",
    method: "put",
    params: { wrongQuestionId }
  })
}

// 获取个人的错题题库列表
export function getDistinctBanksByUserId(): Promise<ApiResponseData<BankSimpleInfoDTO[]>> {
  return request({
    url: "/api/public/wrongQuestions/getDistinctBanksByUserId",
    method: "get"
  })
}
