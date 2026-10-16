import type { CreateQuestionBankDTO, PageResponse, PublicQuestionBankDetail, UpdateQuestionBankDTO, UserQuestionBankItem } from "./type"
// 创建题库
import { request } from "@/http/axios"

export async function getPublicQuestionBankById(id: string): Promise<PublicQuestionBankDetail> {
  const res = await request<ApiResponseData<PublicQuestionBankDetail>>({
    url: "/api/public/questionBank/getPublicQuestionBankById",
    method: "GET",
    params: { id }
  })
  return res.data
}
export async function getQuestionBanksByUserId(): Promise<UserQuestionBankItem[]> {
  const res = await request<ApiResponseData<UserQuestionBankItem[]>>({
    url: "/api/public/questionBank/getQuestionBanksByUserId",
    method: "GET"
  })
  return res.data
}

// 获取用户创建的题库列表
export async function getQuestionBanksByCreatorId(): Promise<UserQuestionBankItem[]> {
  const res = await request<ApiResponseData<UserQuestionBankItem[]>>({
    url: "/api/public/questionBank/getQuestionBanksByCreatorId",
    method: "GET"
  })
  return res.data
}

export async function createQuestionBank(data: CreateQuestionBankDTO): Promise<void> {
  await request({
    url: "/api/public/questionBank/publicCreateQuestionBank",
    method: "POST",
    data
  })
}
export async function addUserQuestionBank(bankId: string): Promise<ApiResponseData<void>> {
  return await request<ApiResponseData<void>>({
    url: "/api/public/userQuestionBanks/addUserQuestionBank",
    method: "POST",
    params: { bankId }
  })
}

// 取消收藏题库
export async function deleteUserQuestionBank(bankId: string): Promise<string> {
  const res = await request<ApiResponseData<string>>({
    url: "/api/public/userQuestionBanks/deleteUserQuestionBank",
    method: "POST",
    params: { bankId }
  })
  return res.data
}

// 根据题库id获取题库详情
export async function getQuestionBankByIdAndAllQuestions(params: {
  bankId: string
  current?: number
  size?: number
  title?: string
  type?: string
  analysis?: string
  difficulty?: number
}): Promise<PageResponse<PublicQuestionBankDetail>> {
  const res = await request<PageResponse<PublicQuestionBankDetail>>({
    url: "/api/public/questionBank/getQuestionBankByIdAndAllQuestions",
    method: "GET",
    params: {
      current: params.current || 1,
      size: params.size || 10,
      bankId: String(params.bankId),
      title: params.title,
      type: params.type,
      analysis: params.analysis,
      difficulty: params.difficulty
    }
  })
  return res
}
// 根据题库id获取题库详情
export async function deleteQuestion(questionId: string): Promise<void> {
  await request({
    url: "/api/public/questionBank/deleteQuestion",
    method: "DELETE",
    params: { questionId }
  })
}

// 删除题库
export async function deleteQuestionBankById(bankId: string): Promise<boolean> {
  const res = await request<ApiResponseData<boolean>>({
    url: "/api/public/questionBank/deleteQuestionBankById",
    method: "DELETE",
    params: { bankId }
  })
  return res.data
}

// 修改题库信息
export async function updateQuestionBank(data: UpdateQuestionBankDTO): Promise<void> {
  await request({
    url: "/api/public/questionBank/PublicUpdateQuestionBank",
    method: "PUT",
    data
  })
}
