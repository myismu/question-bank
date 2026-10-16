import { request } from "@/http/axios"
import type { ActivityRecords } from "./type"

/** 分页获取活动记录列表 */
export function getActivityRecordsListApi(params: ActivityRecords.ActivityRecordsQuery) {
  return request<ApiResponseData<ActivityRecords.PageResult<ActivityRecords.ActivityRecordsInfo>>>(
    {
      url: "/api/admin/activity-records/page",
      method: "get",
      params
    }
  )
}

/** 获取所有活动记录 */
export function getAllActivityRecordsApi() {
  return request<ApiResponseData<ActivityRecords.ActivityRecordsInfo[]>>({
    url: "/api/admin/activity-records",
    method: "get"
  })
}

/** 根据ID获取活动记录 */
export function getActivityRecordsDetailApi(id: number | string) {
  return request<ApiResponseData<ActivityRecords.ActivityRecordsInfo>>({
    url: `/api/admin/activity-records/${id}`,
    method: "get"
  })
}

/** 创建活动记录 */
export function createActivityRecordsApi(data: ActivityRecords.ActivityRecordsForm) {
  return request<ApiResponseData<ActivityRecords.ActivityRecordsInfo>>({
    url: "/api/admin/activity-records",
    method: "post",
    data
  })
}

/** 更新活动记录 */
export function updateActivityRecordsApi(id: number | string, data: ActivityRecords.ActivityRecordsForm) {
  return request<ApiResponseData<ActivityRecords.ActivityRecordsInfo>>({
    url: `/api/admin/activity-records/${id}`,
    method: "put",
    data
  })
}

/** 删除活动记录 */
export function deleteActivityRecordsApi(id: number | string) {
  return request<ApiResponseData<string>>({
    url: `/api/admin/activity-records/${id}`,
    method: "delete"
  })
}
