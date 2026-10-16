import type { Router } from "vue-router"
import { usePermissionStore } from "@/pinia/stores/permission"
import { useUserStore } from "@/pinia/stores/user"
import { routerConfig } from "@/router/config"
import { isWhiteList } from "@/router/whitelist"
import { setRouteChange } from "@@/composables/useRouteListener"
import { useTitle } from "@@/composables/useTitle"
import { getToken, removeToken } from "@@/utils/cache/cookies"
import NProgress from "nprogress"

NProgress.configure({ showSpinner: false })

const { setTitle } = useTitle()

const LOGIN_PATH = "/login"

let isFetchingUser = false

export function registerNavigationGuard(router: Router) {
  router.beforeEach(async (to, _from) => {
    NProgress.start()
    const userStore = useUserStore()
    const permissionStore = usePermissionStore()
    
    if (!getToken()) {
      if (isWhiteList(to)) return true
      return LOGIN_PATH
    }
    
    if (to.path === LOGIN_PATH) return "/"
    
    if (userStore.roles.length !== 0) return true
    
    if (isFetchingUser) {
      return { ...to, replace: true }
    }
    
    try {
      isFetchingUser = true
      await userStore.getInfo()
      const roles = userStore.roles
      routerConfig.dynamic ? permissionStore.setRoutes(roles) : permissionStore.setAllRoutes()
      permissionStore.addRoutes.forEach(route => router.addRoute(route))
      isFetchingUser = false
      return { ...to, replace: true }
    } catch (error) {
      isFetchingUser = false
      removeToken()
      userStore.roles = []
      userStore.token = ""
      ElMessage.error("获取用户信息失败，请重新登录")
      return LOGIN_PATH
    }
  })

  // 全局后置钩子
  router.afterEach((to) => {
    setRouteChange(to)
    setTitle(to.meta.title)
    NProgress.done()
  })
}
