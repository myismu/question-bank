<script lang="ts" setup>
import defaultAvatar from "@@/assets/images/layouts/user_logo.png"
import { Camera, Message, Phone, User, Edit, Promotion, ArrowDown } from "@element-plus/icons-vue"
import { ElMessage, ElNotification } from "element-plus"
import { ref, onMounted, computed } from "vue"
import { useRoute, useRouter } from "vue-router"
import { modifyPersonalInfo as apiModifyPersonalInfo, deleteImageApi, getPersonalInfoApi, OutLogin, uploadImageApi } from "@/common/apis/users"
import { useUserStore } from "@/pinia/stores/user"

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 个人信息相关
const personalInfoDialogVisible = ref(false)
const userInfo = ref({
  id: "",
  username: "",
  email: "",
  phoneNumber: "",
  avatar: ""
})
const userInfoLoading = ref(false)
const avatarError = ref(false)
const isEditing = ref(false)
const editUserInfo = ref({
  id: "",
  username: "",
  email: "",
  phoneNumber: "",
  avatar: ""
})

// 计算当前激活的菜单项
const activeIndex = computed(() => {
  if (route.path === "/" || route.path === "/home") return "1"
  if (route.path.startsWith("/question-bank")) return "2"
  return ""
})

function showNotification() {
  ElNotification({
    title: "✨ 更新提示",
    message: "错题本功能已上线！练习完成后，系统会自动为您整理错题，助您高效复习。",
    type: "success",
    position: "bottom-right",
    duration: 5000
  })
}

onMounted(() => {
  showNotification()
})

// 获取用户头像
function getUserAvatar() {
  const imageBaseUrl = import.meta.env.VITE_IMAGE_URL
  if (avatarError.value) return defaultAvatar
  
  const avatar = isEditing.value ? editUserInfo.value.avatar : (userInfo.value.avatar || userStore.avatar)
  return avatar ? imageBaseUrl + avatar : defaultAvatar
}

// 获取个人信息
async function fetchPersonalInfo() {
  userInfoLoading.value = true
  try {
    const { data } = await getPersonalInfoApi()
    userInfo.value = { ...data }
    Object.assign(editUserInfo.value, userInfo.value)
  } catch (error) {
    ElMessage.error("获取个人信息失败")
  } finally {
    userInfoLoading.value = false
  }
}

function showPersonalInfo() {
  personalInfoDialogVisible.value = true
  fetchPersonalInfo()
  isEditing.value = false
}

function handleSelect(key: string) {
  const routes: Record<string, string> = {
    "1": "/",
    "2": "/question-bank"
  }
  if (routes[key]) router.push(routes[key])
}

async function handleLogout() {
  try {
    await OutLogin()
    userStore.logout()
    router.push("/user/login")
    ElMessage.success("已退出登录")
  } catch (error) {
    console.error("退出登录失败:", error)
  }
}

const goToLogin = (type: 'login' | 'register') => {
  router.push({ path: "/user/login", query: { type } })
}

// 处理头像上传
async function handleAvatarUpload(file: File) {
  try {
    const oldAvatar = isEditing.value ? editUserInfo.value.avatar : userInfo.value.avatar
    if (oldAvatar) {
      await deleteImageApi(oldAvatar).catch(() => {})
    }

    const response = await uploadImageApi(file)
    const imageUrl = response.data
    
    if (isEditing.value) {
      editUserInfo.value.avatar = imageUrl
    } else {
      userInfo.value.avatar = imageUrl
      userStore.avatar = imageUrl
      await modifyPersonalInfo()
    }
    userStore.avatar = imageUrl
  } catch (error) {
    ElMessage.error("头像上传失败")
  }
}

async function modifyPersonalInfo() {
  const { phoneNumber, email } = editUserInfo.value
  if (phoneNumber && !/^1[3-9]\d{9}$/.test(phoneNumber)) return ElMessage.error("请输入有效的手机号")
  if (email && !/^[\w.%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i.test(email)) return ElMessage.error("请输入有效的邮箱")

  try {
    await apiModifyPersonalInfo(editUserInfo.value)
    isEditing.value = false
    await fetchPersonalInfo()
    ElMessage.success("个人信息已更新")
  } catch (error) {
    ElMessage.error("修改失败，请稍后再试")
  }
}

const startEdit = () => {
  isEditing.value = true
  Object.assign(editUserInfo.value, userInfo.value)
}

const cancelEdit = () => {
  isEditing.value = false
}

const avatarUploadRef = ref()
const triggerAvatarUpload = () => {
  avatarUploadRef.value?.$el.querySelector("input[type=file]")?.click()
}
</script>

<template>
  <div class="app-layout">
    <!-- 顶部导航栏 -->
    <header class="main-header">
      <div class="header-content">
        <div class="logo-area" @click="router.push('/')">
          <span class="logo-text">学习题库</span>
          <span class="logo-badge">Beta</span>
        </div>

        <nav class="nav-area">
          <el-menu
            :default-active="activeIndex"
            mode="horizontal"
            :ellipsis="false"
            class="custom-menu"
            @select="handleSelect"
          >
            <el-menu-item index="1">首页</el-menu-item>
            <el-menu-item index="2">我的题库</el-menu-item>
          </el-menu>
        </nav>

        <div class="user-area">
          <template v-if="userStore.token">
            <el-dropdown trigger="click" class="user-dropdown">
              <div class="user-profile">
                <el-avatar :size="36" :src="getUserAvatar()" @error="avatarError = true" />
                <span class="username-text">{{ userStore.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="showPersonalInfo">
                    <el-icon><User /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout" class="logout-item">
                    <el-icon><Promotion /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <div v-else class="auth-buttons">
            <el-button @click="goToLogin('login')" text>登录</el-button>
            <el-button @click="goToLogin('register')" type="primary" round>注册</el-button>
          </div>
        </div>
      </div>
    </header>

    <!-- 主体内容 -->
    <main class="main-body">
      <div class="content-wrapper">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="main-footer">
      <div class="footer-inner">
        <div class="footer-links">
          <p>© 2026 大学生个性化学习题库 · 见证成长每一刻</p>
          <div class="footer-divider"></div>
          <a href="https://beian.miit.gov.cn/" target="_blank">桂ICP备2024025252号</a>
        </div>
      </div>
    </footer>

    <!-- 个人信息弹窗 -->
    <el-dialog
      v-model="personalInfoDialogVisible"
      class="modern-profile-dialog"
      width="480px"
      destroy-on-close
      align-center
    >
      <div class="profile-card" v-loading="userInfoLoading">
        <div class="profile-cover"></div>
        <div class="profile-header">
          <div class="avatar-edit-wrapper">
            <el-upload
              v-if="isEditing"
              class="avatar-upload"
              :show-file-list="false"
              :before-upload="handleAvatarUpload"
              ref="avatarUploadRef"
            >
              <div class="avatar-container" @click.stop="triggerAvatarUpload">
                <el-image :src="getUserAvatar()" class="avatar-img" fit="cover" />
                <div class="avatar-mask"><el-icon><Camera /></el-icon></div>
              </div>
            </el-upload>
            <el-image v-else :src="getUserAvatar()" class="avatar-img" :preview-src-list="[getUserAvatar()]" fit="cover" />
          </div>
          <div class="header-actions">
            <el-button v-if="!isEditing" @click="startEdit" plain round :icon="Edit">编辑</el-button>
            <template v-else>
              <el-button @click="cancelEdit" text round>取消</el-button>
              <el-button type="primary" @click="modifyPersonalInfo" round>保存</el-button>
            </template>
          </div>
        </div>

        <div class="profile-body">
          <div class="user-title">
            <h2 v-if="!isEditing">{{ userInfo.username }}</h2>
            <el-input v-else v-model="editUserInfo.username" placeholder="设置新昵称" />
            <span class="user-id">UID: {{ userInfo.id }}</span>
          </div>

          <div class="info-list">
            <div class="info-item">
              <label><el-icon><Message /></el-icon> 电子邮箱</label>
              <p v-if="!isEditing">{{ userInfo.email || '未绑定' }}</p>
              <el-input v-else v-model="editUserInfo.email" placeholder="请输入邮箱" />
            </div>
            <div class="info-item">
              <label><el-icon><Phone /></el-icon> 手机号码</label>
              <p v-if="!isEditing">{{ userInfo.phoneNumber || '未绑定' }}</p>
              <el-input v-else v-model="editUserInfo.phoneNumber" placeholder="请输入手机号" maxlength="11" />
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc;
  position: relative;
  overflow-x: hidden;
}

/* 全局背景装饰 */
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
  pointer-events: none;
}

.blob {
  position: absolute;
  filter: blur(80px);
  opacity: 0.4;
  border-radius: 50%;
  animation: float 12s ease-in-out infinite alternate;
}

.blob-1 {
  top: -100px;
  left: -100px;
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #e0e7ff 0%, #bae6fd 100%);
}

.blob-2 {
  top: 200px;
  right: -100px;
  width: 450px;
  height: 450px;
  background: linear-gradient(135deg, #fce7f3 0%, #ddd6fe 100%);
  animation-delay: -5s;
}

.blob-3 {
  bottom: -150px;
  left: 20%;
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #fef3c7 0%, #e0e7ff 100%);
  animation-delay: -8s;
  opacity: 0.2;
}

@keyframes float {
  0% { transform: translateY(0) scale(1); }
  100% { transform: translateY(40px) scale(1.1); }
}

/* Header 样式 */
.main-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  z-index: 1000;
  display: flex;
  justify-content: center;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);

  .header-content {
    width: 100%;
    max-width: 1280px;
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  
  .logo-text {
    font-size: 20px;
    font-weight: 800;
    background: linear-gradient(135deg, #409eff, #36cfc9);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  .logo-badge {
    font-size: 10px;
    background: #ecf5ff;
    color: #409eff;
    padding: 2px 6px;
    border-radius: 4px;
    font-weight: bold;
  }
}

.nav-area {
  flex: 1;
  margin: 0 40px;
  
  .custom-menu {
    border-bottom: none;
    background: transparent;
    height: 64px;

    :deep(.el-menu-item) {
      height: 64px;
      line-height: 64px;
      font-size: 15px;
      color: #606266;
      border-bottom-width: 3px;

      &:hover, &.is-active {
        color: #409eff !important;
        background: transparent !important;
      }
    }
  }
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 20px;
  transition: all 0.3s;

  &:hover {
    background: rgba(0, 0, 0, 0.04);
  }

  .username-text {
    font-weight: 500;
    font-size: 14px;
    color: #303133;
  }
}

/* 主体样式 */
.main-body {
  flex: 1;
  padding-top: 64px;
  display: flex;
  justify-content: center;
  position: relative;
  z-index: 1;

  .content-wrapper {
    width: 100%;
    max-width: 1280px;
    padding: 24px;
    box-sizing: border-box;
  }
}

/* Footer 样式 */
.main-footer {
  background: transparent;
  padding: 60px 0 40px;

  .footer-inner {
    max-width: 1280px;
    margin: 0 auto;
    text-align: center;
    color: #909399;
    font-size: 14px;
  }

  .footer-links {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
  }

  .footer-divider {
    width: 40px;
    height: 2px;
    background: #409eff;
    border-radius: 2px;
    opacity: 0.3;
  }

  a {
    color: #606266;
    text-decoration: none;
    transition: color 0.3s;
    &:hover { color: #409eff; }
  }
}

/* 个人信息弹窗优化 */
:deep(.modern-profile-dialog) {
  border-radius: 16px;
  overflow: hidden;
  .el-dialog__header { display: none; }
  .el-dialog__body { padding: 0; }
}

.profile-card {
  .profile-cover {
    height: 120px;
    background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
  }

  .profile-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    padding: 0 24px;
    margin-top: -40px;
  }

  .avatar-edit-wrapper {
    background: #fff;
    padding: 4px;
    border-radius: 50%;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    
    .avatar-img {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      cursor: pointer;
    }

    .avatar-container {
      position: relative;
      .avatar-mask {
        position: absolute;
        inset: 0;
        background: rgba(0,0,0,0.4);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        opacity: 0;
        transition: 0.3s;
      }
      &:hover .avatar-mask { opacity: 1; }
    }
  }

  .profile-body {
    padding: 24px;
    
    .user-title {
      margin-bottom: 24px;
      h2 { margin: 0; font-size: 22px; color: #1f2f3d; }
      .user-id { font-size: 12px; color: #909399; }
    }

    .info-list {
      display: flex;
      flex-direction: column;
      gap: 16px;

      .info-item {
        label {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 13px;
          color: #606266;
          margin-bottom: 6px;
        }
        p {
          margin: 0;
          padding: 10px 14px;
          background: #f5f7fa;
          border-radius: 8px;
          color: #303133;
        }
      }
    }
  }
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .header-content {
    padding: 0 16px !important;
  }
  
  .nav-area {
    margin: 0 10px !important;
    .el-menu-item { padding: 0 12px !important; }
  }

  .logo-text { display: none; }
  
  .main-body .content-wrapper {
    padding: 12px;
  }

  .username-text { display: none; }
}

/* 动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-15px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(15px);
}
</style>
