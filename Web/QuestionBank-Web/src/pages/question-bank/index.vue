<script setup lang="ts">
import type { UpdateQuestionBankDTO } from "./apis/type"
import defaulttupian from "@@/assets/images/questionBank/f-head05.png"
import { ArrowDown, Document, Plus, Search, Star, View } from "@element-plus/icons-vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { computed, onMounted, ref } from "vue"
import { useRouter } from "vue-router"
import { deleteQuestionBankById, deleteUserQuestionBank, getQuestionBanksByCreatorId, getQuestionBanksByUserId, updateQuestionBank } from "./apis/QuestionBank"

const router = useRouter()
// 搜索和筛选
const searchQuery = ref("")
const selectedDifficulty = ref("")
const viewMode = ref("all")

const IMAGE_BASE_URL = import.meta.env.VITE_IMAGE_URL

function getFullImageUrl(url: string | undefined) {
  if (!url) return defaulttupian
  if (url.startsWith("http://") || url.startsWith("https://")) return url
  if (url.startsWith("/img/")) return IMAGE_BASE_URL + url
  return defaulttupian
}

// 题库数据
interface BankItem {
  id: string
  userId: string
  name: string
  description: string
  difficulty: string
  questionCount: number
  viewCount: number
  starCount: number
  isPublic: boolean
  image: string
}

const favoriteBanks = ref<BankItem[]>([]) // 收藏的题库
const createdBanks = ref<BankItem[]>([]) // 创建的题库

// 当前激活的标签页
const activeTab = ref("favorite")

function mapDifficulty(num: number): string {
  switch (num) {
    case 1:
      return "简单"
    case 2:
      return "中等"
    case 3:
    case 4:
      return "困难"
    default:
      return "未知"
  }
}

onMounted(async () => {
  try {
    // 获取收藏的题库
    const favoriteData = await getQuestionBanksByUserId()
    favoriteBanks.value = (favoriteData || []).map(item => ({
      id: String(item.id),
      userId: item.userId,
      name: item.name,
      description: item.description,
      difficulty: mapDifficulty(item.difficulty),
      questionCount: item.questionCount ?? 0,
      viewCount: 0,
      starCount: 0,
      isPublic: item.type === "public",
      image: item.coverImage
    }))

    // 获取创建的题库
    const createdData = await getQuestionBanksByCreatorId()
    createdBanks.value = (createdData || []).map(item => ({
      id: String(item.id),
      userId: item.userId,
      name: item.name,
      description: item.description,
      difficulty: mapDifficulty(item.difficulty),
      questionCount: item.questionCount ?? 0,
      viewCount: 0,
      starCount: 0,
      isPublic: item.type === "public",
      image: item.coverImage
    }))
  } catch {
    ElMessage.error("获取题库列表失败")
  }
})

// 过滤后的题库列表
const filteredFavoriteBanks = computed(() => {
  return favoriteBanks.value.filter((bank: BankItem) => {
    // 搜索过滤
    if (searchQuery.value && !bank.name.toLowerCase().includes(searchQuery.value.toLowerCase())) {
      return false
    }
    // 难度过滤
    if (selectedDifficulty.value && bank.difficulty !== selectedDifficulty.value) {
      return false
    }
    return true
  })
})

const filteredCreatedBanks = computed(() => {
  return createdBanks.value.filter((bank: BankItem) => {
    // 搜索过滤
    if (searchQuery.value && !bank.name.toLowerCase().includes(searchQuery.value.toLowerCase())) {
      return false
    }
    // 难度过滤
    if (selectedDifficulty.value && bank.difficulty !== selectedDifficulty.value) {
      return false
    }
    // 视图模式过滤
    if (viewMode.value === "public" && !bank.isPublic) {
      return false
    }
    if (viewMode.value === "private" && bank.isPublic) {
      return false
    }
    return true
  })
})

// 搜索处理
function handleSearch() {
  // 可添加防抖处理
}

// 筛选处理
function handleFilter() {
  // 可添加其他筛选逻辑
}

// 取消收藏处理
async function handleUnfavorite(bank: BankItem) {
  try {
    await ElMessageBox.confirm(
      "确定要取消收藏这个题库吗？",
      "提示",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
        customClass: "modern-msgbox"
      }
    )
    await deleteUserQuestionBank(bank.id)
    const index = favoriteBanks.value.findIndex((b: BankItem) => b.id === bank.id)
    if (index !== -1) {
      favoriteBanks.value.splice(index, 1)
    }
    ElMessage.success("取消收藏成功")
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("取消收藏失败，请稍后重试")
    }
  }
}

// 更多操作处理
async function handleCommand(command: string, bank: BankItem) {
  switch (command) {
    case "edit":
      router.push({ path: "bankManage", query: { bankId: bank.id } })
      break
    case "share":
      ElMessage.info("分享题库功能开发中...")
      break
    case "delete":
      try {
        await ElMessageBox.confirm(
          "确定要删除这个题库吗？",
          "警告",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
            customClass: "modern-msgbox"
          }
        )
        const success = await deleteQuestionBankById(bank.id)
        if (success) {
          const index = createdBanks.value.findIndex((b: BankItem) => b.id === bank.id)
          if (index !== -1) {
            createdBanks.value.splice(index, 1)
          }
          ElMessage.success("删除成功")
        } else {
          ElMessage.error("删除失败，请稍后重试")
        }
      } catch (error) {
        if (error !== "cancel") {
          ElMessage.error("删除失败，请稍后重试")
        }
      }
      break
  }
}

// 创建题库
function createBank() {
  router.push("/question-bank/createBank")
}

function goToQuestionDetail(id: string) {
  console.log("题库管理跳转到题目详情：", id)
  router.push({ name: "BankDetail", query: { id } })
}

function startPractice(bank: string) {
  router.push({ path: "/question-bank/practice", query: { bank } })
}

function onImgError(event: Event) {
  const target = event.target as HTMLImageElement
  if (target.src !== defaulttupian) {
    target.src = defaulttupian
  }
}

// 处理题库类型修改
async function handleTypeChange(bank: BankItem, newValue: string | number | boolean) {
  const boolValue = newValue === true || newValue === "true" || newValue === 1
  try {
    const updatedBank: UpdateQuestionBankDTO = {
      id: bank.id,
      userId: bank.userId,
      name: bank.name,
      description: bank.description,
      coverImage: bank.image,
      type: boolValue ? "public" : "private",
      difficulty: bank.difficulty === "简单" ? 1 : bank.difficulty === "中等" ? 2 : 3,
      questionCount: bank.questionCount,
      isCollected: false
    }
    await updateQuestionBank(updatedBank)
    bank.isPublic = boolValue
    ElMessage.success("可见性修改成功")
  } catch (error) {
    console.log(error)
    ElMessage.error("修改失败，请稍后重试")
  }
}
</script>

<template>
  <el-main class="page-main">
    <div class="page-container">
      
      <!-- 现代化页眉/头部控制栏 -->
      <div class="page-header">
        <div class="header-content">
          <h2 class="page-title">我的题库</h2>
          <p class="page-subtitle">管理、收藏与分享您的个性化学习资源</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" size="large" class="create-btn" @click="createBank">
            <el-icon class="mr-1"><Plus /></el-icon> 创建新题库
          </el-button>
        </div>
      </div>

      <!-- 高级检索栏 -->
      <div class="filter-section">
        <el-input 
          v-model="searchQuery" 
          placeholder="输入题库名称快速检索..." 
          class="modern-input search-input" 
          clearable 
          @input="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select 
          v-model="selectedDifficulty" 
          placeholder="所有难度" 
          clearable 
          class="modern-input filter-select" 
          @change="handleFilter"
        >
          <el-option label="简单" value="简单" />
          <el-option label="中等" value="中等" />
          <el-option label="困难" value="困难" />
        </el-select>
      </div>

      <!-- 主题选项卡与内容区 -->
      <div class="content-panel">
        <el-tabs v-model="activeTab" class="modern-tabs">
          <!-- 收藏的题库 -->
          <el-tab-pane name="favorite">
            <template #label>
              <span class="tab-label"><el-icon><Star /></el-icon> 我收藏的题库</span>
            </template>
            <div class="bank-grid" v-if="filteredFavoriteBanks.length > 0">
              <div v-for="bank in filteredFavoriteBanks" :key="bank.id" class="modern-bank-card" @click="goToQuestionDetail(bank.id)">
                <div class="card-hero">
                  <img :src="getFullImageUrl(bank.image)" class="card-img" :alt="bank.name" @error="onImgError">
                  <div class="card-badges">
                    <span class="badge" :class="bank.isPublic ? 'badge-public' : 'badge-private'">
                      {{ bank.isPublic ? '公开' : '私有' }}
                    </span>
                  </div>
                </div>
                <div class="card-body">
                  <h3 class="card-title" :title="bank.name">{{ bank.name }}</h3>
                  <p class="card-desc" :title="bank.description">{{ bank.description || '暂无描述' }}</p>
                  <div class="card-meta">
                    <span class="meta-tag"><el-icon><Document /></el-icon> {{ bank.questionCount }} 题</span>
                    <span class="meta-tag"><el-icon><Star /></el-icon> {{ bank.starCount }} 收藏</span>
                  </div>
                  <div class="card-footer">
                    <el-button class="action-btn" size="small" round @click.stop="goToQuestionDetail(bank.id)">详情</el-button>
                    <el-button type="primary" class="action-btn practice-btn" size="small" round @click.stop="startPractice(bank.id)">练习</el-button>
                    <el-button type="danger" plain class="action-btn" size="small" round @click.stop="handleUnfavorite(bank)">取消</el-button>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <el-empty description="暂未收藏任何题库，快去题库市场看看吧！" />
            </div>
          </el-tab-pane>

          <!-- 创建的题库 -->
          <el-tab-pane name="created">
            <template #label>
              <span class="tab-label"><el-icon><Document /></el-icon> 我创建的题库</span>
            </template>
            <div class="sub-filter-row mb-4">
              <span class="sub-filter-title">可见性筛选：</span>
              <el-radio-group v-model="viewMode" size="small" class="modern-radio">
                <el-radio-button value="all">全部题库</el-radio-button>
                <el-radio-button value="public">已公开</el-radio-button>
                <el-radio-button value="private">私有记录</el-radio-button>
              </el-radio-group>
            </div>
            <div class="bank-grid" v-if="filteredCreatedBanks.length > 0">
              <div v-for="bank in filteredCreatedBanks" :key="bank.id" class="modern-bank-card" @click="goToQuestionDetail(bank.id)">
                <div class="card-hero">
                  <img :src="getFullImageUrl(bank.image)" class="card-img" :alt="bank.name" @error="onImgError">
                  <div class="card-badges">
                    <span class="badge" :class="bank.isPublic ? 'badge-public' : 'badge-private'">
                      {{ bank.isPublic ? '公开' : '私有' }}
                    </span>
                  </div>
                </div>
                <div class="card-body">
                  <h3 class="card-title" :title="bank.name">{{ bank.name }}</h3>
                  <p class="card-desc" :title="bank.description">{{ bank.description || '暂无描述' }}</p>
                  <div class="card-meta">
                    <span class="meta-tag"><el-icon><Document /></el-icon> {{ bank.questionCount }} 题</span>
                    <span class="meta-tag"><el-icon><View /></el-icon> {{ bank.viewCount }} 浏览</span>
                  </div>
                  <div class="card-footer card-footer-created">
                    <el-button type="primary" size="small" round @click.stop="startPractice(bank.id)" class="mr-auto action-btn practice-btn">
                      挑战练习
                    </el-button>
                    
                    <div class="action-controls" @click.stop>
                      <el-switch
                        v-model="bank.isPublic"
                        :active-value="true"
                        :inactive-value="false"
                        active-text="公开"
                        inactive-text="私密"
                        inline-prompt
                        style="--el-switch-on-color: #10b981; --el-switch-off-color: #94a3b8; margin-right: 12px; margin-top:2px;"
                        @change="handleTypeChange(bank, $event as boolean)"
                      />
                      <el-dropdown trigger="click" @command="handleCommand($event, bank)">
                        <div class="more-opts-trigger"><el-icon><ArrowDown /></el-icon></div>
                        <template #dropdown>
                          <el-dropdown-menu class="modern-dropdown">
                            <el-dropdown-item command="edit"><span class="opt-text">编辑题库</span></el-dropdown-item>
                            <el-dropdown-item command="share"><span class="opt-text">分享链接</span></el-dropdown-item>
                            <el-dropdown-item command="delete" divided><span class="opt-text text-red">删除库</span></el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <el-empty description="你还没有创建过题库，点击右上角新建一个专属题库吧！" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </el-main>
</template>

<style scoped>
/* ====== 布局与主容器 ====== */
.page-main {
  background-color: var(--el-bg-color-page, #f3f5f9);
  min-height: 100vh;
  padding: 24px;
}
.page-container {
  max-width: 1280px;
  margin: 0 auto;
}
.mr-1 { margin-right: 4px; }
.mr-auto { margin-right: auto; }
.mb-4 { margin-bottom: 24px; }

/* ======== 页眉区 ======== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}
.header-content .page-title {
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 8px 0;
  letter-spacing: -0.02em;
}
.header-content .page-subtitle {
  font-size: 15px;
  color: #64748b;
  margin: 0;
}
.create-btn {
  border-radius: 12px;
  font-weight: 600;
  padding: 0 24px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.3);
}

/* ======== 工具栏/检索区 ======== */
.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  background: #fff;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.02);
  border: 1px solid var(--el-border-color-light, #e2e8f0);
}
.search-input {
  max-width: 360px;
  flex: 1;
}
.filter-select {
  width: 160px;
}
:deep(.modern-input .el-input__wrapper) {
  background-color: #f8fafc;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 10px;
  height: 44px;
  padding: 0 16px;
  transition: all 0.2s;
}
:deep(.modern-input .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #cbd5e1 inset;
}
:deep(.modern-input .el-input__wrapper.is-focus) {
  background-color: #fff;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.4) inset !important;
}

/* ======== 子检索栏 (我创建的题库) ======== */
.sub-filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  padding: 12px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.02);
  border: 1px solid #e2e8f0;
}
.sub-filter-title {
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
}

/* ======== 单选按钮组重置 ======== */
.modern-radio :deep(.el-radio-button__inner) {
  border: none !important;
  background: transparent !important;
  color: #64748b;
  font-weight: 500;
  border-radius: 8px !important;
  padding: 6px 16px;
  box-shadow: none !important;
  transition: all 0.2s;
}
.modern-radio :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #f1f5f9 !important;
  color: #3b82f6 !important;
  font-weight: 600;
}

/* ======== 现代标签页样式 ======== */
.content-panel {
  background: transparent;
}
:deep(.modern-tabs .el-tabs__header) {
  margin-bottom: 24px;
}
:deep(.modern-tabs .el-tabs__item) {
  font-size: 16px !important;
  font-weight: 500;
  color: #64748b;
  height: 48px;
  line-height: 48px;
  padding: 0 24px !important;
}
:deep(.modern-tabs .el-tabs__item.is-active) {
  font-weight: 700;
  color: #3b82f6;
}
:deep(.modern-tabs .el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #e2e8f0;
}
:deep(.modern-tabs .el-tabs__active-bar) {
  background-color: #3b82f6;
  border-radius: 2px;
  height: 3px;
}
.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* ======== 现代风题库卡片网格 ======== */
.bank-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}
.modern-bank-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.02);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid #e2e8f0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  height: 340px;
}
.modern-bank-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.08);
  border-color: #cbd5e1;
}

.card-hero {
  position: relative;
  width: 100%;
  height: 140px;
  background: #f1f5f9;
  overflow: hidden;
}
.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.modern-bank-card:hover .card-img {
  transform: scale(1.06);
}
.card-badges {
  position: absolute;
  top: 12px;
  left: 12px;
  display: flex;
  gap: 8px;
}
.badge {
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  color: #64748b;
}
.badge-public { color: #10b981; }

.card-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  flex: 1;
}
.card-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-desc {
  font-size: 13.5px;
  color: #64748b;
  line-height: 1.5;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 40px;
  flex-shrink: 0;
}
.card-meta {
  display: flex;
  gap: 12px;
  margin-bottom: auto; /* Push footer down */
}
.meta-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  background: #f8fafc;
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid #f1f5f9;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
  flex-shrink: 0;
}
.action-btn {
  font-weight: 600;
  padding: 8px 16px;
  border: none;
}
.practice-btn {
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.card-footer-created {
  display: flex;
  justify-content: space-between;
}
.action-controls {
  display: flex;
  align-items: center;
}
.more-opts-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f1f5f9;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.more-opts-trigger:hover {
  background: #fff;
  border-color: #cbd5e1;
  color: #0f172a;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

/* ======== 其他细节 ======== */
.empty-state {
  background: #fff;
  border-radius: 16px;
  padding: 80px 20px;
  text-align: center;
  border: 1px dashed #cbd5e1;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.02);
}
.text-red { color: #ef4444; }
.opt-text { font-weight: 500; }

@media screen and (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .filter-section {
    flex-direction: column;
  }
  .search-input, .filter-select {
    max-width: 100%;
    width: 100%;
  }
  .sub-filter-row {
    flex-direction: column;
    align-items: flex-start;
    padding: 16px;
  }
}
</style>
