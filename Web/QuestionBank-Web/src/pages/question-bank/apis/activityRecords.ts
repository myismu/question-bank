import type { ActivityAnswerDTO, ActivityRecordDTO } from "./type"
import { request } from "@/http/axios"

// 创建活动记录
export async function createActivityRecord(record: ActivityRecordDTO): Promise<string> {
  const res = await request<ApiResponseData<string>>({
    url: "/api/public/activityRecords/createActivityRecord",
    method: "POST",
    data: record
  })
  return res.data
}

// 完成/放弃活动
export async function finishActivityRecord(record: ActivityRecordDTO): Promise<void> {
  await request<ApiResponseData<void>>({
    url: "/api/public/activityRecords/finishActivityRecord",
    method: "POST",
    data: record
  })
}

// 查询用户活动历史
export async function selectActivityRecords(bankId: string, type: string): Promise<ActivityRecordDTO> {
  const res = await request<ApiResponseData<ActivityRecordDTO>>({
    url: "/api/public/activityRecords/selectActivityRecords",
    method: "GET",
    params: { bankId, type }
  })
  return res.data
}

// 批量保存/更新答题记录
export async function saveOrUpdateBatch(answers: ActivityAnswerDTO[]): Promise<void> {
  await request<ApiResponseData<void>>({
    url: "/api/public/activityAnswers/saveOrUpdateBatch",
    method: "POST",
    data: answers
  })
}

// 设置定时改卷任务
export async function startAutoSubmit(recordId: string, durationInSeconds: number): Promise<string> {
  const res = await request<ApiResponseData<string>>({
    url: "/api/public/activityRecords/startAutoSubmit",
    method: "GET",
    params: { recordId, durationInSeconds }
  })
  return res.data
}

// 删除活动答题记录
export async function deleteActivityAnswersByRecordId(recordId: string): Promise<void> {
  await request<ApiResponseData<void>>({
    url: "/api/public/activityAnswers/deleteActivityAnswersByRecordId",
    method: "DELETE",
    params: { recordId }
  })
}

// 修改活动记录
export async function updateActivityRecord(record: ActivityRecordDTO): Promise<void> {
  await request<ApiResponseData<void>>({
    url: "/api/public/activityRecords/updateActivityRecord",
    method: "PUT",
    data: record
  })
}
