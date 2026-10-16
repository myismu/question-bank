<script setup lang="ts">
import type { FormRules } from "element-plus"
import { Lock, Message, Phone, User } from "@element-plus/icons-vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { ref } from "vue"
import { useRouter } from "vue-router"
import { registerApi } from "../apis/index"

const router = useRouter()

// 表单数据
const formData = ref({
  username: "",
  password: "",
  email: "",
  phoneNumber: ""
})

// 表单验证规则
const rules = ref<FormRules>({
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 20, message: "用户名长度应在3-20个字符之间", trigger: "blur" }
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "密码长度应在6-20个字符之间", trigger: "blur" }
  ],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" }
  ],
  phoneNumber: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号格式", trigger: "blur" }
  ]
})

// 表单引用
const formRef = ref()

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    ElMessageBox.confirm(
      "确定要注册吗？",
      "注册确认",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info"
      }
    )
      .then(async () => {
        // 调用注册API
        try {
          const res = await registerApi({
            username: formData.value.username,
            password: formData.value.password,
            email: formData.value.email,
            phoneNumber: formData.value.phoneNumber
          })
          if (res.code === 200) {
            ElMessage.success("注册成功")
            router.push("/user/login")
          }
        } catch (e: any) {
          ElMessage.error(e?.message || "注册失败")
        }
      })
      .catch(() => {
        ElMessage.info("已取消注册")
      })
  } catch {
    ElMessage.error("请检查表单填写是否正确")
  }
}

// 重置表单
function handleReset() {
  if (!formRef.value) return
  formRef.value.resetFields()
}

// 取消注册
function handleCancel() {
  ElMessageBox.confirm(
    "确定要取消注册吗？",
    "取消确认",
    {
      confirmButtonText: "确定",
      cancelButtonText: "继续注册",
      type: "warning"
    }
  )
    .then(() => {
      router.push("/")
    })
    .catch(() => {
      ElMessage.info("继续注册")
    })
}
</script>

<template>
  <div class="register-container">
    <div class="register-left">
      <img
        src="https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05"
        alt="风景图"
        class="background-image"
      >
      <div class="image-overlay">
        <h1>欢迎加入我们</h1>
        <p>开启您的学习之旅</p>
      </div>
    </div>
    <div class="register-right">
      <div class="register-box">
        <el-button
          @click="handleCancel"
          class="cancel-btn cancel-top-btn"
          size="small"
          type="text"
        >
          取消注册
        </el-button>
        <div class="register-header">
          <img
            src="https://img.icons8.com/fluency/96/000000/user-male-circle.png"
            alt="Logo"
            class="logo"
          >
          <h2>用户注册</h2>
        </div>
        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="100px"
          class="register-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="formData.username"
              placeholder="请输入用户名"
              clearable
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="formData.password"
              type="password"
              placeholder="请输入密码"
              show-password
              clearable
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="formData.email"
              placeholder="请输入邮箱"
              clearable
            >
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="手机号" prop="phoneNumber">
            <el-input
              v-model="formData.phoneNumber"
              placeholder="请输入手机号"
              clearable
            >
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSubmit" class="submit-btn">
              注册
            </el-button>
            <el-button @click="handleReset" class="reset-btn">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  background-color: #f5f7fa;
}
.register-left {
  flex: 7;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 0;
}
.background-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  top: 0;
  left: 0;
  filter: brightness(0.9);
  z-index: 0;
}
.image-overlay {
  position: relative;
  z-index: 1;
  color: white;
  text-align: center;
  padding: 30px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 15px;
  backdrop-filter: blur(8px);
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
}
.register-right {
  flex: 3;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
}
.register-box {
  width: 100%;
  max-width: 500px;
  padding: 40px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  position: relative;
}
.register-header {
  text-align: center;
  margin-bottom: 40px;
}
.logo {
  width: 100px;
  height: 100px;
  margin-bottom: 20px;
  transition: transform 0.3s ease;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}
.logo:hover {
  transform: scale(1.1) rotate(5deg);
}
h2 {
  color: #1890ff;
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
.register-form {
  max-width: 100%;
}
.el-form-item {
  margin-bottom: 25px;
}
.el-form-item:last-child {
  margin-bottom: 0;
  text-align: center;
}
.el-button {
  margin: 0 10px;
  padding: 12px 25px;
  font-size: 16px;
  border-radius: 6px;
  transition: all 0.3s ease;
}
.submit-btn {
  background: linear-gradient(45deg, #1890ff, #36cfc9);
  border: none;
}
.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}
.reset-btn:hover {
  background-color: #f5f7fa;
  transform: translateY(-2px);
}
.cancel-btn {
  color: #909399;
  border-color: #dcdfe6;
  background-color: #fff;
}
.cancel-btn:hover {
  color: #f56c6c;
  border-color: #f56c6c;
  background-color: #fef0f0;
  transform: translateY(-2px);
}
.el-input {
  --el-input-height: 45px;
}
.el-input :deep(.el-input__wrapper) {
  padding-left: 15px;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}
.el-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}
.el-input :deep(.el-input__prefix) {
  left: 12px;
  color: #909399;
}
.button-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.left-btns,
.right-btns {
  display: flex;
  gap: 10px;
}
.cancel-top-btn {
  position: absolute;
  top: 18px;
  left: 18px;
  z-index: 2;
  color: #909399;
  font-size: 14px;
  padding: 0 8px;
  background: none;
  border: none;
  box-shadow: none;
}
.cancel-top-btn:hover {
  color: #f56c6c;
  background: #fef0f0;
}
@media screen and (max-width: 768px) {
  .register-container {
    flex-direction: column;
  }
  .register-left,
  .register-right {
    flex: none;
    width: 100%;
    min-height: 300px;
  }
  .image-overlay h1 {
    font-size: 2em;
  }
  .image-overlay p {
    font-size: 1.2em;
  }
  .el-button {
    margin: 5px;
    padding: 10px 20px;
    font-size: 14px;
  }
  h2 {
    font-size: 24px;
  }
  .logo {
    width: 80px;
    height: 80px;
  }
}
</style>
