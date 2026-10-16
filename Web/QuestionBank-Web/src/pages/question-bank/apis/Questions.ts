import type { PageResult, PublicQuestionDTO } from "./type"
import { request } from "@/http/axios"

export async function getQuestionsByBankId(bankId: string): Promise<PublicQuestionDTO[]> {
  const res = await request<ApiResponseData<PublicQuestionDTO[]>>({
    url: "/api/public/questions/getQuestionsById",
    method: "GET",
    params: { id: bankId }
  })
  return res.data
}

// 添加题目
export async function addQuestion(question: PublicQuestionDTO): Promise<boolean> {
  const res = await request<ApiResponseData<boolean>>({
    url: "/api/public/questions/addQuestion",
    method: "POST",
    data: question
  })
  return res.data
}

// 删除题目
export async function deleteQuestion(questionId: string): Promise<boolean> {
  const res = await request<ApiResponseData<boolean>>({
    url: "/api/public/questions/deleteQuestion",
    method: "DELETE",
    params: { questionId }
  })
  return res.data
}

// 修改题目
export async function updateQuestion(question: PublicQuestionDTO): Promise<boolean> {
  const res = await request<ApiResponseData<boolean>>({
    url: "/api/public/questions/updateQuestion",
    method: "PUT",
    data: question
  })
  return res.data
}

// 批量添加题目
export async function batchAddQuestions(publicQuestionsDTOList: PublicQuestionDTO[]): Promise<boolean> {
  const res = await request<ApiResponseData<boolean>>({
    url: "/api/public/questions/BatchAddQuestion",
    method: "POST",
    data: publicQuestionsDTOList
  })
  return res.data
}

// 分页查询题目
export async function getQuestionsByPage(bankId: string, page = 1, size = 10): Promise<PageResult<PublicQuestionDTO>> {
  const res = await request<{ code: number, message: string, data: PageResult<PublicQuestionDTO> }>({
    url: "/api/public/questions/getQuestionsByPage",
    method: "GET",
    params: { id: bankId, page, size }
  })
  return res.data
}
