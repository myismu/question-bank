import { request } from "@/http/axios"
import type { GlobalLogDTO, GlobalLogPageParams } from "./type"

// 全局日志-分页查询
export function getGlobalLogPage(params: GlobalLogPageParams) {
  return request<{ data: { records: GlobalLogDTO[]; total: number } }>({
    url: "/api/admin/global-log/page",
    method: "get",
    params
  })
}

// 全局日志-获取全部
export function getAllGlobalLogs() {
  return request<GlobalLogDTO[]>({
    url: "/api/admin/global-log/all",
    method: "get"
  })
}

// 全局日志-获取详情
export function getGlobalLogById(id: string | number) {
  return request<{ data: GlobalLogDTO }>({
    url: `/api/admin/global-log/${id}`,
    method: "get"
  })
}

// 全局日志-新增
export function createGlobalLog(data: Partial<GlobalLogDTO>) {
  return request({
    url: "/api/admin/global-log",
    method: "post",
    data
  })
}

// 全局日志-修改
export function updateGlobalLog(id: string | number, data: Partial<GlobalLogDTO>) {
  return request({
    url: `/api/admin/global-log/${id}`,
    method: "put",
    data
  })
}

// 全局日志-删除
export function deleteGlobalLog(id: string | number) {
  return request({
    url: `/api/admin/global-log/${id}`,
    method: "delete"
  })
}
