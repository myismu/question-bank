import type { StudyStatisticsDTO, StudyTrendDTO, BankProgressDTO } from "./types"
import { request } from "@/http/axios"

// 获取用户学习统计概览
export async function getStudyStatistics(): Promise<StudyStatisticsDTO> {
  const res = await request<ApiResponseData<StudyStatisticsDTO>>({
    url: "/api/public/activityRecords/getStudyStatistics",
    method: "GET"
  })
  return res.data
}

// 获取用户学习趋势数据
export async function getStudyTrend(): Promise<StudyTrendDTO[]> {
  const res = await request<ApiResponseData<StudyTrendDTO[]>>({
    url: "/api/public/activityRecords/getStudyTrend",
    method: "GET"
  })
  return res.data
}

// 获取用户题库学习进度
export async function getBankProgress(): Promise<BankProgressDTO[]> {
  const res = await request<ApiResponseData<BankProgressDTO[]>>({
    url: "/api/public/activityRecords/getBankProgress",
    method: "GET"
  })
  return res.data
}
