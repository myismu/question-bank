import type { WrongQuestionsDTO, WrongQuestionsForm, WrongQuestionsPageParams } from "./type"
import { request } from "@/http/axios"

// 错题-分页查询
export function getWrongQuestionsPage(params: WrongQuestionsPageParams) {
  return request<{ data: { records: WrongQuestionsDTO[]; total: number } }>({
    url: "/api/admin/wrong-questions/page",
    method: "get",
    params
  })
}

// 错题-获取全部
export function getAllWrongQuestions() {
  return request<{ data: WrongQuestionsDTO[] }>({
    url: "/api/admin/wrong-questions/all",
    method: "get"
  })
}

// 错题-获取详情
export function getWrongQuestionById(id: string | number) {
  return request<{ data: WrongQuestionsDTO }>({
    url: `/api/admin/wrong-questions/${id}`,
    method: "get"
  })
}

// 错题-新增
export function createWrongQuestion(data: WrongQuestionsForm) {
  return request<{ data: WrongQuestionsDTO }>({
    url: "/api/admin/wrong-questions",
    method: "post",
    data
  })
}

// 错题-修改
export function updateWrongQuestion(id: string | number, data: WrongQuestionsForm) {
  return request<{ data: WrongQuestionsDTO }>({
    url: `/api/admin/wrong-questions/${id}`,
    method: "put",
    data
  })
}

// 错题-删除
export function deleteWrongQuestion(id: string | number) {
  return request<{ data: null }>({
    url: `/api/admin/wrong-questions/${id}`,
    method: "delete"
  })
}