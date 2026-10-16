import type * as Users from "./type"
import { request } from "@/http/axios"

/** 获取当前登录用户详情 */
export function getCurrentUserApi() {
  return request<Users.CurrentUserResponseData>({
    url: "/api/auth/getUserInfo",
    method: "get"
  })
}

/** 获取个人信息 */
export function getPersonalInfoApi() {
  return request<Users.PersonalInfoResponseData>({
    url: "/api/auth/getPersonalInfo",
    method: "get"
  })
}

// 退出登录
export function OutLogin() {
  return request<Users.OutLogin>({
    url: "/api/auth/OutLogin",
    method: "get"
  })
}

/** 上传图片 */
export function uploadImageApi(file: File) {
  const formData = new FormData()
  formData.append("file", file)

  return request<ApiResponseData<string>>({
    url: "/api/file/upload",
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data"
    }
  })
}

/** 修改个人信息 */
export function modifyPersonalInfo(usersDTO: Users.UserInfo) {
  return request<ApiResponseData<boolean>>({
    url: "/api/auth/modifyPersonalInfo", // 这里是你修改个人信息的接口
    method: "post",
    data: usersDTO
  })
}

/** 删除图片 */
export function deleteImageApi(url: string) {
  return request<ApiResponseData<string>>({
    url: "/api/file/deleteImage",
    method: "delete",
    params: { url }
  })
}
