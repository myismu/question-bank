<script lang="ts" setup>
import type { FormInstance, FormRules } from "element-plus"
import type { LoginRequestData } from "./apis/type"
import { Lock, User, Message, Phone, Right } from "@element-plus/icons-vue"
import { useRoute, useRouter } from "vue-router"
import { useUserStore } from "@/pinia/stores/user"
import { getCaptchaApi, loginApi, registerApi } from "./apis"
import Owl from "./components/Owl.vue"
import { useFocus } from "./composables/useFocus"
import { reactive, ref, watch } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"

const router = useRouter()
const route = useRoute()

const userStore = useUserStore()

const { isFocus, handleBlur, handleFocus } = useFocus()
const { isFocus: regIsFocus, handleBlur: regHandleBlur, handleFocus: regHandleFocus } = useFocus()

/** 登录表单元素的引用 */
const loginFormRef = ref<FormInstance | null>(null)

/** 登录按钮 Loading */
const loading = ref(false)
const regLoading = ref(false)

/** 验证码图片 URL */
const codeUrl = ref("")

/** 登录表单数据 */
const loginFormData: LoginRequestData = reactive({
  username: "", 
  password: "", 
  // code: "",
  systemCode: ""
})

/** 登录表单校验规则 */
const loginFormRules: FormRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" }
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 8, max: 16, message: "长度在 8 到 16 个字符", trigger: "blur" }
  ]
}

/** 注册表单数据 */
const registerFormData = ref({
  username: "",
  password: "",
  email: "",
  phoneNumber: ""
})
const registerFormRef = ref<FormInstance | null>(null)
const registerRules: FormRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 20, message: "用户名长度应在3-20个字符之间", trigger: "blur" }
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 8, max: 16, message: "密码长度应在8-16个字符之间", trigger: "blur" }
  ],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" }
  ],
  phoneNumber: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号格式", trigger: "blur" }
  ]
}

// 根据路由参数设置初始状态
const showLogin = ref(route.query.type !== "register")

// 监听路由参数变化
watch(() => route.query.type, (newType) => {
  showLogin.value = newType !== "register"
})

// 监听 showLogin 变化，切换时重置表单
watch(showLogin, (newVal: boolean) => {
  if (newVal) {
    loginFormRef.value?.resetFields()
  } else {
    registerFormRef.value?.resetFields()
  }
})

/** 登录 */
function handleLogin() {
  loginFormRef.value?.validate((valid) => {
    if (!valid) return
    loading.value = true
    loginFormData.systemCode = "examBaby-web"
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

/** 创建验证码 */
function createCode() {
  codeUrl.value = ""
  getCaptchaApi().then((res) => {
    codeUrl.value = res.data
  })
}

async function handleRegister() {
  if (!registerFormRef.value) return
  try {
    await registerFormRef.value.validate()
    ElMessageBox.confirm(
      "确定要使用提供的信息注册吗？",
      "注册确认",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info",
        customClass: "modern-msgbox"
      }
    )
      .then(async () => {
        regLoading.value = true
        try {
          const res = await registerApi({
            username: registerFormData.value.username,
            password: registerFormData.value.password,
            email: registerFormData.value.email,
            phoneNumber: registerFormData.value.phoneNumber
          })
          if (res.code === 200) {
            ElMessage.success("注册成功！赶快登录开启体验。")
            showLogin.value = true
          }
        } catch (e: any) {
          ElMessage.error(e?.message || "注册失败")
        } finally {
          regLoading.value = false
        }
      })
      .catch(() => {})
  } catch {
    ElMessage.error("请检查表单填写是否正确")
  }
}
function handleRegisterReset() {
  if (!registerFormRef.value) return
  registerFormRef.value.resetFields()
}
</script>

<template>
  <div class="auth-layout">
    <div class="back-home" @click.prevent="router.push('/')">
      <el-icon><Back /></el-icon> 返回首页
    </div>

    <!-- 背景装饰网格与模糊渐变效 -->
    <div class="auth-bg-decor">
      <div class="blur-blob blob-1"></div>
      <div class="blur-blob blob-2"></div>
      <div class="grid-pattern"></div>
    </div>

    <div class="auth-card-wrapper">
      <transition name="auth-flip" mode="out-in">
        <!-- 登录面板 -->
        <div v-if="showLogin" key="login" class="auth-panel">
          <div class="auth-hero bg-indigo">
            <div class="hero-content">
              <h1>探索个性化<br>学习新境界</h1>
              <p>大学生专属题库，构建属于您的知识图谱与智能错题分析体系。</p>
            </div>
            <div class="hero-pattern"></div>
          </div>
          <div class="auth-form-side">
            <div class="form-header">
              <h2>欢迎回来</h2>
              <p>登录您的账号继续学习之旅</p>
            </div>
            
            <div class="owl-container">
              <Owl :close-eyes="isFocus" />
            </div>

            <el-form ref="loginFormRef" :model="loginFormData" :rules="loginFormRules" class="modern-form" @keyup.enter="handleLogin">
              <el-form-item prop="username">
                <el-input v-model.trim="loginFormData.username" placeholder="请输入用户名" size="large" tabindex="1">
                  <template #prefix><el-icon><User /></el-icon></template>
                </el-input>
              </el-form-item>
              
              <el-form-item prop="password">
                <el-input
                  v-model.trim="loginFormData.password"
                  placeholder="请输入您的密码"
                  type="password"
                  size="large"
                  tabindex="2"
                  show-password
                  @blur="handleBlur"
                  @focus="handleFocus"
                >
                  <template #prefix><el-icon><Lock /></el-icon></template>
                </el-input>
              </el-form-item>

              <div class="form-options">
                <el-checkbox>自动登录</el-checkbox>
                <el-link :underline="false" class="forget-link">忘记密码？</el-link>
              </div>

              <el-button class="submit-btn" :loading="loading" type="primary" size="large" @click="handleLogin">
                <span>登 录</span>
                <el-icon class="ml-1"><Right /></el-icon>
              </el-button>
            </el-form>

            <div class="auth-switch">
              还没有账号？ 
              <span class="switch-link" @click="showLogin = false">立即注册</span>
            </div>
          </div>
        </div>

        <!-- 注册面板 -->
        <div v-else key="register" class="auth-panel register-panel">
          <div class="auth-hero bg-emerald">
            <div class="hero-content">
              <h1>加入我们<br>开启成长</h1>
              <p>注册属于您的学习账号，即刻享受海量题库、智能解析与学习统计。</p>
            </div>
            <div class="hero-pattern"></div>
          </div>
          <div class="auth-form-side">
            <div class="form-header">
              <h2>创建新账号</h2>
              <p>填写以下信息，仅需半分种即可完成</p>
            </div>

            <div class="owl-container">
              <Owl :close-eyes="regIsFocus" />
            </div>

            <el-form ref="registerFormRef" :model="registerFormData" :rules="registerRules" class="modern-form">
              <el-form-item prop="username">
                <el-input v-model="registerFormData.username" placeholder="设置一个好记的用户名" size="large" clearable>
                  <template #prefix><el-icon><User /></el-icon></template>
                </el-input>
              </el-form-item>
              
              <el-form-item prop="password">
                <el-input v-model="registerFormData.password" type="password" placeholder="设置安全密码 (8-16位)" size="large" show-password clearable @blur="regHandleBlur" @focus="regHandleFocus">
                  <template #prefix><el-icon><Lock /></el-icon></template>
                </el-input>
              </el-form-item>

              <el-form-item prop="email">
                <el-input v-model="registerFormData.email" placeholder="常用电子邮箱" size="large" clearable>
                  <template #prefix><el-icon><Message /></el-icon></template>
                </el-input>
              </el-form-item>

              <el-form-item prop="phoneNumber">
                <el-input v-model="registerFormData.phoneNumber" placeholder="11位手机号码" size="large" clearable>
                  <template #prefix><el-icon><Phone /></el-icon></template>
                </el-input>
              </el-form-item>

              <div class="action-buttons mt-4">
                <el-button class="submit-btn register-btn" :loading="regLoading" type="primary" size="large" @click="handleRegister">
                  完成注册
                </el-button>
                <el-button size="large" @click="handleRegisterReset">重置</el-button>
              </div>
            </el-form>

            <div class="auth-switch mt-4">
              已有账号？ 
              <span class="switch-link" @click="showLogin = true">返回登录</span>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<style scoped>
/* ====== 布局与背景 ====== */
.auth-layout {
  position: relative;
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f5f9;
  overflow: hidden;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.back-home {
  position: absolute;
  top: 40px;
  left: 40px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  padding: 10px 16px;
  border-radius: 20px;
  background: rgba(255,255,255,0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.8);
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.back-home:hover {
  transform: translateY(-2px);
  color: #3b82f6;
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.15);
  background: rgba(255,255,255,0.9);
}

.auth-bg-decor {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}
.grid-pattern {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(#cbd5e1 1px, transparent 1px);
  background-size: 32px 32px;
  opacity: 0.3;
}
.blur-blob {
  position: absolute;
  filter: blur(100px);
  border-radius: 50%;
  opacity: 0.4;
  animation: float 20s infinite ease-in-out alternate;
}
.blob-1 {
  width: 600px;
  height: 600px;
  background: #60a5fa;
  top: -100px;
  left: -100px;
}
.blob-2 {
  width: 500px;
  height: 500px;
  background: #c084fc;
  bottom: -50px;
  right: -50px;
  animation-delay: -10s;
}
@keyframes float {
  0% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(50px, 30px) scale(1.05); }
  100% { transform: translate(-30px, 50px) scale(0.95); }
}

/* ====== 表单面板容器 ====== */
.auth-card-wrapper {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  padding: 24px;
}

.auth-panel {
  display: flex;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 1);
  overflow: hidden;
  min-height: 600px;
}
.register-panel {
  flex-direction: row-reverse;
}

/* 左侧/视觉展示区域 */
.auth-hero {
  flex: 4;
  position: relative;
  overflow: hidden;
  color: #fff;
  display: flex;
  align-items: center;
  padding: 60px 48px;
}
.bg-indigo {
  background: linear-gradient(145deg, #4f46e5 0%, #3b82f6 100%);
}
.bg-emerald {
  background: linear-gradient(145deg, #059669 0%, #10b981 100%);
}
.hero-pattern {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.1'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.5;
}
.hero-content {
  position: relative;
  z-index: 1;
}
.hero-content h1 {
  font-size: 42px;
  font-weight: 800;
  line-height: 1.2;
  margin: 0 0 24px 0;
  letter-spacing: -0.02em;
}
.hero-content p {
  font-size: 16px;
  line-height: 1.6;
  opacity: 0.9;
  margin: 0;
  max-width: 90%;
}

/* 右侧/表单区域 */
.auth-form-side {
  flex: 5;
  padding: 60px 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
}
.form-header {
  margin-bottom: 24px;
}
.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 8px 0;
}
.form-header p {
  color: #64748b;
  font-size: 15px;
  margin: 0;
}

.owl-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80px;
  margin-bottom: 24px;
}

/* 现代表单样式 */
.modern-form {
  width: 100%;
}
:deep(.modern-form .el-input__wrapper) {
  background-color: #f8fafc;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 12px;
  padding: 0 16px;
  transition: all 0.2s;
}
:deep(.modern-form .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #cbd5e1 inset;
}
:deep(.modern-form .el-input__wrapper.is-focus) {
  background-color: #fff;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.4) inset, 0 4px 12px rgba(59, 130, 246, 0.1);
}
:deep(.modern-form .el-input__inner) {
  height: 48px;
  font-size: 15px;
}
:deep(.modern-form .el-form-item) {
  margin-bottom: 24px;
}
:deep(.modern-form .el-input__prefix) {
  color: #94a3b8;
  font-size: 18px;
  margin-right: 8px;
}
:deep(.modern-form .el-input__wrapper.is-focus .el-input__prefix) {
  color: #3b82f6;
}

/* 选项与按钮 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  margin-top: -8px;
}
.forget-link {
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
}
.forget-link:hover {
  color: #3b82f6;
}
.submit-btn {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(to right, #3b82f6, #2563eb);
  border: none;
  box-shadow: 0 8px 16px rgba(37, 99, 235, 0.2);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.3);
}
.register-btn {
  background: linear-gradient(to right, #10b981, #059669);
  box-shadow: 0 8px 16px rgba(16, 185, 129, 0.2);
}
.register-btn:hover {
  box-shadow: 0 12px 24px rgba(16, 185, 129, 0.3);
}
.action-buttons {
  display: flex;
  gap: 16px;
}
.action-buttons .el-button {
  flex: 1;
  height: 52px;
  border-radius: 12px;
  margin: 0;
}

/* 切换链接 */
.auth-switch {
  text-align: center;
  margin-top: 32px;
  font-size: 14px;
  color: #64748b;
}
.switch-link {
  color: #3b82f6;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.2s;
}
.switch-link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}
.mt-4 { margin-top: 16px; }
.ml-1 { margin-left: 6px; }

/* 转场动画 */
.auth-flip-enter-active,
.auth-flip-leave-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}
.auth-flip-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.98);
}
.auth-flip-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.98);
}

/* 响应式 */
@media screen and (max-width: 992px) {
  .auth-card-wrapper {
    padding: 16px;
  }
  .auth-panel {
    flex-direction: column;
  }
  .register-panel {
    flex-direction: column;
  }
  .auth-hero {
    flex: none;
    padding: 40px 32px;
  }
  .hero-content h1 {
    font-size: 32px;
  }
  .auth-form-side {
    padding: 40px 32px;
  }
}
@media screen and (max-width: 480px) {
  .back-home {
    top: 20px;
    left: 20px;
    padding: 8px 12px;
    font-size: 13px;
  }
  .auth-form-side {
    padding: 32px 20px;
  }
}
</style>
