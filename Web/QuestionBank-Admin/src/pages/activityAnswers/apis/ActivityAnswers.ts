import { request } from "@/http/axios"
import type { ActivityAnswersInfo, ActivityAnswersQuery, ActivityAnswersForm, PageResult } from './type'

/** 分页获取答题记录列表 */
export function getActivityAnswersListApi(params: ActivityAnswersQuery) {
  return request<ApiResponseData<PageResult<ActivityAnswersInfo>>>({
    url: '/api/admin/activity-answers/page',
    method: 'get',
    params
  })
}

/** 新增答题记录 */
export function addActivityAnswersApi(data: ActivityAnswersForm) {
  return request<ApiResponseData<string>>({
    url: '/api/admin/activity-answers',
    method: 'post',
    data
  })
}

/** 修改答题记录 */
export function updateActivityAnswersApi(id: number | string, data: ActivityAnswersForm) {
  return request<ApiResponseData<string>>({
    url: `/api/admin/activity-answers/${id}`,
    method: 'put',
    data
  })
}

/** 删除答题记录 */
export function deleteActivityAnswersApi(id: number | string) {
  return request<ApiResponseData<string>>({
    url: `/api/admin/activity-answers/${id}`,
    method: 'delete'
  })
}

/** 获取答题记录详情 */
export function getActivityAnswersDetailApi(id: number | string) {
  return request<ApiResponseData<ActivityAnswersInfo>>({
    url: `/api/admin/activity-answers/${id}`,
    method: 'get'
  })
}

/** 获取所有答题记录 */
export function getAllActivityAnswersApi() {
  return request<ApiResponseData<ActivityAnswersInfo[]>>({
    url: '/api/admin/activity-answers/all',
    method: 'get'
  })
}
