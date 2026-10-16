import { request } from "@/http/axios"
import type { QuestionChapters } from "./type"

/** 分页获取章节列表 */
export function getChapterListApi(params: QuestionChapters.ChapterQuery) {
  return request<ApiResponseData<QuestionChapters.PageResult<QuestionChapters.ChapterInfo>>>(
    {
      url: "/api/admin/question-chapters/page",
      method: "get",
      params
    }
  )
}

/** 获取所有章节 */
export function getAllChaptersApi() {
  return request<ApiResponseData<QuestionChapters.ChapterInfo[]>>({
    url: "/api/admin/question-chapters/all",
    method: "get"
  })
}

/** 创建章节 */
export function createChapterApi(data: QuestionChapters.ChapterForm) {
  return request<ApiResponseData<string>>({
    url: "/api/admin/question-chapters",
    method: "post",
    data
  })
}

/** 更新章节 */
export function updateChapterApi(id: number | string, data: QuestionChapters.ChapterForm) {
  return request<ApiResponseData<string>>({
    url: `/api/admin/question-chapters/${id}`,
    method: "put",
    data
  })
}

/** 删除章节 */
export function deleteChapterApi(id: number | string) {
  return request<ApiResponseData<string>>({
    url: `/api/admin/question-chapters/${id}`,
    method: "delete"
  })
}
