<script lang="ts" setup>
import type { FormInstance, FormRules } from "element-plus"
import type { LoginRequestData } from "./apis/type"
import { useSettingsStore } from "@/pinia/stores/settings"
import { useUserStore } from "@/pinia/stores/user"
import { useTheme } from "@@/composables/useTheme"
import ThemeSwitch from "@@/components/ThemeSwitch/index.vue"
import { Key, Loading, Lock, Picture, User } from "@element-plus/icons-vue"
import { loginApi } from "./apis"
import Owl from "./components/Owl.vue"
import { useFocus } from "./composables/useFocus"

const router = useRouter()
const userStore = useUserStore()
const settingsStore = useSettingsStore()
const { activeThemeName } = useTheme()
const { isFocus, handleBlur, handleFocus } = useFocus()

/** 登录表单元素的引用 */
const loginFormRef = ref<FormInstance | null>(null)

/** 登录按钮 Loading */
const loading = ref(false)

/** 验证码图片 URL */
const codeUrl = ref("")

/** 登录表单数据 */
const loginFormData: LoginRequestData = reactive({
  username: "admin",
  password: "12345678",
  systemCode: ""
})



// ========== 使用 computed 优化 ==========

/** 登录按钮是否禁用 */
const loginButtonDisabled = computed(() => {
  return !loginFormData.username.trim() ||
         !loginFormData.password.trim() ||
         loading.value
})

/** 登录按钮文本 */
const loginButtonText = computed(() => {
  return loading.value ? "登录中..." : "题库管理系统 登 录"
})

/** 表单是否有效（实时验证） */
const isFormValid = computed(() => {
  const { username, password } = loginFormData
  return username.trim().length >= 1 &&
         password.trim().length >= 8
})

/** 用户名错误提示 */
const usernameError = computed(() => {
  if (!loginFormData.username.trim()) return "请输入用户名"
  return ""
})

/** 密码错误提示 */
const passwordError = computed(() => {
  if (!loginFormData.password.trim()) return "请输入密码"
  if (loginFormData.password.length < 8) return "密码至少8个字符"
  return ""
})

/** 登录卡片样式类 */
const loginCardClass = computed(() => {
  const classes = ['login-card']
  if (activeThemeName.value === 'dark') {
    classes.push('login-card--dark')
  }
  return classes
})

/** 标题文本样式 */
const titleTextStyle = computed(() => {
  return {
    fontSize: '30px',
    color: activeThemeName.value === 'dark' ? '#fff' : '#333'
  }
})

/** 登录 */
function handleLogin() {
  // 使用 computed 状态进行预验证
  if (loginButtonDisabled.value) {
    ElMessage.warning("请先填写完整的登录信息")
    return
  }

  loginFormRef.value?.validate((valid) => {
    if (!valid) {
      ElMessage.error("表单校验不通过")
      return
    }
    loading.value = true
    loginFormData.systemCode = "examBaby-admin"
    loginApi(loginFormData).then(({ data }) => {
      userStore.setToken(data.token)
      router.push("/")
    }).catch(() => {
      loginFormData.password = ""
    }).finally(() => {
      loading.value = false
    })
  })
}
</script>

<template>
  <div class="login-container">
    <ThemeSwitch v-if="settingsStore.showThemeSwitch" class="theme-switch" />
    <Owl :close-eyes="isFocus" />
    <div :class="loginCardClass">
      <div class="title">
         <div class="title-text" :style="titleTextStyle">大学生个性化学习题库管理@</div>
      </div>
      <div class="content">
        <el-form ref="loginFormRef" :model="loginFormData"  @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input
              v-model.trim="loginFormData.username"
              placeholder="用户名"
              type="text"
              tabindex="1"
              :prefix-icon="User"
              size="large"
            />
            <div v-if="usernameError" class="error-tip">{{ usernameError }}</div>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model.trim="loginFormData.password"
              placeholder="密码"
              type="password"
              tabindex="2"
              :prefix-icon="Lock"
              size="large"
              show-password
              @blur="handleBlur"
              @focus="handleFocus"
            />
            <div v-if="passwordError" class="error-tip">{{ passwordError }}</div>
          </el-form-item>
          <el-button
            :loading="loading"
            :disabled="loginButtonDisabled"
            type="primary"
            size="large"
            @click.prevent="handleLogin"
          >
            {{ loginButtonText }}
          </el-button>
          <div v-if="!isFormValid" class="form-tip">
            <el-icon><Loading /></el-icon>
            <span>请填写完整的登录信息</span>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  min-height: 100%;
  .theme-switch {
    position: fixed;
    top: 5%;
    right: 5%;
    cursor: pointer;
  }
  .login-card {
    width: 480px;
    max-width: 90%;
    border-radius: 20px;
    box-shadow: 0 0 10px #dcdfe6;
    background-color: var(--el-bg-color);
    overflow: hidden;
    transition: all 0.3s ease;

    &.login-card--dark {
      box-shadow: 0 0 10px rgba(255, 255, 255, 0.1);
    }

    .title {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 150px;
      .title-text{
        font-size: 30px;
        transition: color 0.3s ease;
      }
      img {
        height: 100%;
      }
    }
    .content {
      padding: 20px 50px 50px 50px;
      :deep(.el-input-group__append) {
        padding: 0;
        overflow: hidden;
        .el-image {
          width: 100px;
          height: 40px;
          border-left: 0px;
          user-select: none;
          cursor: pointer;
          text-align: center;
        }
      }

      .error-tip {
        color: var(--el-color-danger);
        font-size: 12px;
        margin-top: 4px;
        animation: fadeIn 0.3s ease;
      }

      .form-tip {
        display: flex;
        align-items: center;
        justify-content: center;
        margin-top: 12px;
        padding: 8px 12px;
        background-color: var(--el-color-warning-light-9);
        border-radius: 4px;
        color: var(--el-color-warning);
        font-size: 14px;

        .el-icon {
          margin-right: 6px;
        }
      }

      .el-button {
        width: 100%;
        margin-top: 10px;
        transition: all 0.3s ease;

        &:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }
      }
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
