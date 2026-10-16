// 统一处理 Cookie

import { CacheKey } from "@@/constants/cache-key"
import Cookies from "js-cookie"

export function getToken() {
  return Cookies.get(CacheKey.TOKEN)
}

export function setToken(token: string) {
  Cookies.set(CacheKey.TOKEN, token, { expires: 1 }) // 设置token过期时间1天
}

export function removeToken() {
  Cookies.remove(CacheKey.TOKEN)
}

interface UserInfoCache {
  username: string
  avatar: string
  userId: string
  roles: string[]
}

export function getUserInfoCache(): UserInfoCache | null {
  const data = Cookies.get(CacheKey.USER_INFO)
  if (!data) return null
  try {
    return JSON.parse(data)
  } catch {
    return null
  }
}

export function setUserInfoCache(info: UserInfoCache) {
  Cookies.set(CacheKey.USER_INFO, JSON.stringify(info), { expires: 1 }) // 与token保持一致的过期时间
}

export function removeUserInfoCache() {
  Cookies.remove(CacheKey.USER_INFO)
}
