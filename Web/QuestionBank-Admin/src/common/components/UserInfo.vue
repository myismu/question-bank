<template>
  <div class="user-info">
    <!-- 用户信息显示 -->
    <div v-if="isLoggedIn" class="user-details">
      <el-avatar :size="40" :src="avatarUrl" />
      <div class="user-text">
        <p class="username">{{ userInfo.username }}</p>
        <p class="roles">{{ userInfo.roles.join(', ') }}</p>
      </div>

      <!-- 权限检查示例 -->
      <div class="permissions">
        <el-tag v-if="hasPermission('admin')" type="success">管理员</el-tag>
        <el-tag v-if="hasAnyPermission(['editor', 'viewer'])" type="info">内容权限</el-tag>
      </div>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button size="small" @click="handleProfile">个人资料</el-button>
        <el-button size="small" type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <!-- 未登录状态 -->
    <div v-else class="login-prompt">
      <p>请先登录</p>
      <el-button type="primary" @click="goToLogin">立即登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { useUser } from '@@/composables/useUser'

const {
  isLoggedIn,
  userInfo,
  hasPermission,
  hasAnyPermission,
  safeLogout
} = useUser()

const router = useRouter()

// 头像URL（可以根据实际情况调整）
const avatarUrl = computed(() => {
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${userInfo.value.username}`
})

// 查看个人资料
const handleProfile = () => {
  ElMessage.info('打开个人资料页面')
  // router.push('/profile')
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    safeLogout()
    ElMessage.success('已退出登录')
  })
}

// 前往登录页
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.user-info {
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fafafa;
}

.user-details {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-text {
  flex: 1;
}

.username {
  font-weight: bold;
  margin: 0;
  font-size: 16px;
}

.roles {
  margin: 4px 0 0 0;
  color: #666;
  font-size: 14px;
}

.permissions {
  display: flex;
  gap: 8px;
}

.actions {
  display: flex;
  gap: 8px;
}

.login-prompt {
  text-align: center;
  padding: 20px;
}

.login-prompt p {
  margin-bottom: 16px;
  color: #666;
}
</style>
