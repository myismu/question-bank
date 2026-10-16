import { request } from "@/http/axios"
import type { BankTags } from "./type"

/** 分页获取标签列表 */
export function getBankTagListApi(params: BankTags.BankTagQuery) {
  return request<BankTags.ApiResponse<BankTags.PageResult<BankTags.BankTagInfo>>>(
    {
      url: "/api/admin/bank-tags/page",
      method: "get",
      params
    }
  )
}

/** 获取所有标签 */
export function getAllBankTagsApi() {
  return request<BankTags.ApiResponse<BankTags.BankTagInfo[]>>({
    url: "/api/admin/bank-tags/all",
    method: "get"
  })
}

/** 创建标签 */
export function createBankTagApi(data: BankTags.BankTagForm) {
  return request<BankTags.ApiResponse<string>>({
    url: "/api/admin/bank-tags",
    method: "post",
    data
  })
}

/** 更新标签 */
export function updateBankTagApi(id: number | string, data: BankTags.BankTagForm) {
  return request<BankTags.ApiResponse<string>>({
    url: `/api/admin/bank-tags/${id}`,
    method: "put",
    data
  })
}

/** 删除标签 */
export function deleteBankTagApi(id: number | string) {
  return request<BankTags.ApiResponse<string>>({
    url: `/api/admin/bank-tags/${id}`,
    method: "delete"
  })
}
