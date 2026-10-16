import type { QuestionsDTO, QuestionsForm, QuestionsPageParams } from "./type"
import { request } from "@/http/axios"

// 题目-分页查询
export function getQuestionsPage(params: QuestionsPageParams) {
  return request<{ data: { records: QuestionsDTO[]; total: number } }>({
    url: "/api/admin/questions/page",
    method: "get",
    params
  })
}

// 题目-获取全部
export function getAllQuestions() {
  return request<{ data: QuestionsDTO[] }>({
    url: "/api/admin/questions/all",
    method: "get"
  })
}

// 题目-获取详情
export function getQuestionById(id: string | number) {
  return request<{ data: QuestionsDTO }>({
    url: `/api/admin/questions/${id}`,
    method: "get"
  })
}

// 题目-新增
export function createQuestion(data: QuestionsForm) {
  return request<{ data: QuestionsDTO }>({
    url: "/api/admin/questions",
    method: "post",
    data
  })
}

// 题目-修改
export function updateQuestion(id: string | number, data: QuestionsForm) {
  return request<{ data: QuestionsDTO }>({
    url: `/api/admin/questions/${id}`,
    method: "put",
    data
  })
}

// 题目-删除
export function deleteQuestion(id: string | number) {
  return request<{ data: null }>({
    url: `/api/admin/questions/${id}`,
    method: "delete"
  })
}
