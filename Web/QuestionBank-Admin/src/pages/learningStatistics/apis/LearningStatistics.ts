import { request } from "@/http/axios"
import type { LearningStatistics } from "./type"

/** 分页获取学习统计列表 */
export function getLearningStatisticsListApi(params: LearningStatistics.LearningStatisticsQuery) {
  return request<LearningStatistics.ApiResponse<LearningStatistics.PageResult<LearningStatistics.LearningStatisticsInfo>>>(
    {
      url: "/api/admin/learning-statistics/page",
      method: "get",
      params
    }
  )
}

/** 获取所有学习统计 */
export function getAllLearningStatisticsApi() {
  return request<LearningStatistics.ApiResponse<LearningStatistics.LearningStatisticsInfo[]>>({
    url: "/api/admin/learning-statistics/all",
    method: "get"
  })
}

/** 创建学习统计 */
export function createLearningStatisticsApi(data: LearningStatistics.LearningStatisticsForm) {
  return request<LearningStatistics.ApiResponse<string>>({
    url: "/api/admin/learning-statistics",
    method: "post",
    data
  })
}

/** 更新学习统计 */
export function updateLearningStatisticsApi(id: number | string, data: LearningStatistics.LearningStatisticsForm) {
  return request<LearningStatistics.ApiResponse<string>>({
    url: `/api/admin/learning-statistics/${id}`,
    method: "put",
    data
  })
}

/** 删除学习统计 */
export function deleteLearningStatisticsApi(id: number | string) {
  return request<LearningStatistics.ApiResponse<string>>({
    url: `/api/admin/learning-statistics/${id}`,
    method: "delete"
  })
}
