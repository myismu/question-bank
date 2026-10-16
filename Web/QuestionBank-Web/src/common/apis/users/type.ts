export type CurrentUserResponseData = ApiResponseData<{ username: string, roles: string[], avatar: string, userId: string }>

export type OutLogin = ApiResponseData<{ null: null }>

export interface UserInfo {
  id: string
  username: string
  password?: string
  email: string
  phoneNumber: string
  avatar: string
  userId?: string
}

export type PersonalInfoResponseData = ApiResponseData<UserInfo>
export interface UploadImageResponse {
  code: number // 响应状态码
  message: string // 响应消息
  data: string // 返回的图片 URL
}
