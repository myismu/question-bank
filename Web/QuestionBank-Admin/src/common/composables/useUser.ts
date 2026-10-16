/**
 * 用户相关的组合式函数
 * 封装了 user store 的常用操作
 */

export function useUser() {
  const userStore = useUserStore()
  const router = useRouter()
  const route= useRoute()
  // 用户登录状态
  const isLoggedIn = computed(() => !!userStore.token)

  // 用户信息
  const userInfo = computed(() => ({
    username: userStore.username,
    roles: userStore.roles,
    token: userStore.token
  }))

  // 检查用户权限
  const hasPermission = (permission: string) => {
    return userStore.roles.includes(permission)
  }

  // 检查用户是否有任一权限
  const hasAnyPermission = (permissions: string[]) => {
    return permissions.some(permission => userStore.roles.includes(permission))
  }

  // 检查用户是否有所有权限
  const hasAllPermissions = (permissions: string[]) => {
    return permissions.every(permission => userStore.roles.includes(permission))
  }

  // 登录后重定向
  const redirectAfterLogin = (defaultPath = '/') => {
    const redirect = router.currentRoute.value.query.redirect as string
    router.push(redirect || defaultPath)
  }

  // 安全登出
  const safeLogout = () => {
    userStore.logout()
    router.push('/login')
  }

  return {
    // 状态
    isLoggedIn,
    userInfo,

    // 权限检查
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,

    // 操作方法
    redirectAfterLogin,
    safeLogout,

    // 原始 store（用于特殊操作）
    userStore
  }
}
