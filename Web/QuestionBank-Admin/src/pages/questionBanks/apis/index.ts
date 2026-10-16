import type { QuestionBanksDTO, QuestionBanksForm, QuestionBanksPageParams } from "./type"
import { request } from "@/http/axios"

// 题库-分页查询
export function getQuestionBanksPage(params: QuestionBanksPageParams) {
  return request<{ data: { records: QuestionBanksDTO[]; total: number } }>({
    url: "/api/admin/question-banks/page",
    method: "get",
    params,
  })
}

// 题库-获取全部
export function getAllQuestionBanks() {
  return request<{ data: QuestionBanksDTO[] }>({
    url: "/api/admin/question-banks/all",
    method: "get",
  })
}

// 题库-获取详情
export function getQuestionBankById(id: string | number) {
  return request<{ data: QuestionBanksDTO }>({
    url: `/api/admin/question-banks/${id}`,
    method: "get",
  })
}

// 题库-新增
export function createQuestionBank(data: QuestionBanksForm) {
  return request<{ data: QuestionBanksDTO }>({
    url: "/api/admin/question-banks",
    method: "post",
    data,
  })
}

// 题库-修改
export function updateQuestionBank(id: string | number, data: QuestionBanksForm) {
  return request<{ data: QuestionBanksDTO }>({
    url: `/api/admin/question-banks/${id}`,
    method: "put",
    data,
  })
}

// 题库-删除
export function deleteQuestionBank(id: string | number) {
  return request<{ data: null }>({
    url: `/api/admin/question-banks/${id}`,
    method: "delete",
  })
}
