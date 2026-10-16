import type { Router } from "vue-router"
import { getToken, getUserInfoCache, removeUserInfoCache, setUserInfoCache } from "@@/utils/cache/cookies"
import NProgress from "nprogress"
import { usePermissionStore } from "@/pinia/stores/permission"
import { useUserStore } from "@/pinia/stores/user"

import { routerConfig } from "@/router/config"
import { isWhiteList } from "@/router/whitelist"
import { ElMessage } from "element-plus"

NProgress.configure({ showSpinner: false })

const LOGIN_PATH = "/"// 异常和未登录时默认进入的页面

export function registerNavigationGuard(router: Router) {
  // 全局前置守卫
  router.beforeEach(async (to, _from) => {
    NProgress.start()
    const userStore = useUserStore()
    const permissionStore = usePermissionStore()
    // 如果没有登录
    if (!getToken()) {
      // 如果在免登录的白名单中，则直接进入
      if (isWhiteList(to)) return true
      // 其他没有访问权限的页面将被重定向到登录页面
      // return LOGIN_PATH
      ElMessage.warning("请先登录")
      return LOGIN_PATH
    }
    // 如果已经登录，并准备进入 Login 页面，则重定向到主页
    if (to.path === LOGIN_PATH) return "/"
    // 如果用户已经获得其权限角色
    if (userStore.roles.length !== 0) return true

    // 尝试从本地缓存恢复用户信息
    const cachedInfo = getUserInfoCache()
    if (cachedInfo && cachedInfo.roles && cachedInfo.roles.length > 0) {
      userStore.username = cachedInfo.username
      userStore.avatar = cachedInfo.avatar
      userStore.userId = cachedInfo.userId
      userStore.roles = cachedInfo.roles

      routerConfig.dynamic ? permissionStore.setRoutes(cachedInfo.roles) : permissionStore.setAllRoutes()
      permissionStore.addRoutes.forEach(route => router.addRoute(route))
      return { ...to, replace: true }
    }

    // 否则要重新获取权限角色
    try {
      await userStore.getInfo()

      // 缓存用户信息到 Cookie
      setUserInfoCache({
        username: userStore.username,
        avatar: userStore.avatar,
        userId: userStore.userId,
        roles: userStore.roles
      })

      // 注意：角色必须是一个数组！ 例如: ["admin"] 或 ["developer", "editor"]
      const roles = userStore.roles
      // 生成可访问的 Routes
      routerConfig.dynamic ? permissionStore.setRoutes(roles) : permissionStore.setAllRoutes()
      // 将 "有访问权限的动态路由" 添加到 Router 中
      permissionStore.addRoutes.forEach(route => router.addRoute(route))
      // 设置 replace: true, 因此导航将不会留下历史记录
      return { ...to, replace: true }
    } catch (error: any) {
      const status = error?.response?.status

      // 401 未授权：token 过期或无效，需要重新登录
      if (status === 401) {
        userStore.logout()
        removeUserInfoCache()
        ElMessage.warning("登录已过期，请重新登录")
        return LOGIN_PATH
      }

      // 网络错误或服务器错误（5xx）：允许继续访问，显示提示
      if (!status || status >= 500) {
        ElMessage.warning("网络连接不稳定，部分功能可能受限")
        // 使用默认角色允许访问
        const defaultRoles = routerConfig.defaultRoles
        userStore.roles = defaultRoles
        routerConfig.dynamic ? permissionStore.setRoutes(defaultRoles) : permissionStore.setAllRoutes()
        permissionStore.addRoutes.forEach(route => router.addRoute(route))
        return { ...to, replace: true }
      }

      // 其他错误：显示错误信息，允许继续尝试访问
      ElMessage.error((error as Error).message || "获取用户信息失败")
      return { ...to, replace: true }
    }
  })

  // 全局后置钩子
  router.afterEach(() => {
    NProgress.done()
  })
}
