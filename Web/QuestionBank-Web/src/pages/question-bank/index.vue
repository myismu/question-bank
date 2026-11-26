<script setup lang="ts">
import type { UpdateQuestionBankDTO } from "./apis/type"
import defaultCover from "@@/assets/images/questionBank/f-head05.png"
import { ArrowDown, ArrowRight, Plus, Search } from "@element-plus/icons-vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { computed, onMounted, ref } from "vue"
import { useRouter } from "vue-router"
import {
  deleteQuestionBankById,
  deleteUserQuestionBank,
  getQuestionBanksByCreatorId,
  getQuestionBanksByUserId,
  updateQuestionBank
} from "./apis/QuestionBank"

const router = useRouter()
const IMAGE_BASE_URL = import.meta.env.VITE_IMAGE_URL

// 题库列表项数据结构
interface BankItem {
  id: string
  userId: string
  name: string
  description: string
  difficulty: string
  questionCount: number
  isPublic: boolean
  image: string
}

// 搜索、难度与可见性筛选状态
const searchQuery = ref("")
const selectedDifficulty = ref("全部")
const viewMode = ref<"all" | "public" | "private">("all")
const activeTab = ref<"favorite" | "created">("favorite")
const difficultyOptions = ["全部", "简单", "中等", "困难"]

// 列表数据与加载状态
const favoriteBanks = ref<BankItem[]>([])
const createdBanks = ref<BankItem[]>([])
const loading = ref(true)

// 拼接封面图片完整路径
function getFullImageUrl(url: string | undefined) {
  if (!url) return defaultCover
  if (url.startsWith("http://") || url.startsWith("https://")) return url
  if (url.startsWith("/img/")) return IMAGE_BASE_URL + url
  return defaultCover
}

// 图片加载失败时降级到默认封面
function onImgError(event: Event) {
  const target = event.target as HTMLImageElement
  if (target.src !== defaultCover) {
    target.src = defaultCover
  }
}

// 后端难度数字映射为中文标签
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

// 难度样式类（与首页保持统一的低饱和度浅底徽章）
function getDifficultyClass(difficulty: string): string {
  const map: Record<string, string> = {
    简单: "diff-easy",
    中等: "diff-medium",
    困难: "diff-hard"
  }
  return map[difficulty] || "diff-default"
}

// 加载收藏题库与自建题库列表
onMounted(async () => {
  loading.value = true
  try {
    const [favoriteData, createdData] = await Promise.all([
      getQuestionBanksByUserId(),
      getQuestionBanksByCreatorId()
    ])

    favoriteBanks.value = (favoriteData || []).map(item => ({
      id: String(item.id),
      userId: item.userId,
      name: item.name,
      description: item.description,
      difficulty: mapDifficulty(item.difficulty),
      questionCount: item.questionCount ?? 0,
      isPublic: item.type === "public",
      image: item.coverImage
    }))

    createdBanks.value = (createdData || []).map(item => ({
      id: String(item.id),
      userId: item.userId,
      name: item.name,
      description: item.description,
      difficulty: mapDifficulty(item.difficulty),
      questionCount: item.questionCount ?? 0,
      isPublic: item.type === "public",
      image: item.coverImage
    }))
  } catch (error) {
    console.error("获取我的题库列表失败:", error)
    ElMessage.error("获取题库列表失败")
  } finally {
    loading.value = false
  }
})

// 按关键字和难度过滤收藏的题库
const filteredFavoriteBanks = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  return favoriteBanks.value.filter((bank) => {
    const matchKeyword = !keyword
      || bank.name.toLowerCase().includes(keyword)
      || (bank.description || "").toLowerCase().includes(keyword)
    const matchDifficulty = selectedDifficulty.value === "全部" || bank.difficulty === selectedDifficulty.value
    return matchKeyword && matchDifficulty
  })
})

// 按关键字、难度和可见性过滤创建的题库
const filteredCreatedBanks = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  return createdBanks.value.filter((bank) => {
    const matchKeyword = !keyword
      || bank.name.toLowerCase().includes(keyword)
      || (bank.description || "").toLowerCase().includes(keyword)
    const matchDifficulty = selectedDifficulty.value === "全部" || bank.difficulty === selectedDifficulty.value
    const matchVisibility = viewMode.value === "all"
      || (viewMode.value === "public" && bank.isPublic)
      || (viewMode.value === "private" && !bank.isPublic)
    return matchKeyword && matchDifficulty && matchVisibility
  })
})

// 是否存在激活中的筛选条件
const hasActiveFilters = computed(() =>
  searchQuery.value.trim().length > 0
  || selectedDifficulty.value !== "全部"
  || (activeTab.value === "created" && viewMode.value !== "all")
)

// 重置全部筛选条件
function resetFilters() {
  searchQuery.value = ""
  selectedDifficulty.value = "全部"
  viewMode.value = "all"
}

// 取消收藏题库
async function handleUnfavorite(bank: BankItem) {
  try {
    await ElMessageBox.confirm(
      `确定要取消收藏「${bank.name}」吗？`,
      "取消收藏",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }
    )
    await deleteUserQuestionBank(bank.id)
    const index = favoriteBanks.value.findIndex(b => b.id === bank.id)
    if (index !== -1) {
      favoriteBanks.value.splice(index, 1)
    }
    ElMessage.success("已取消收藏")
  } catch (error) {
    if (error !== "cancel") {
      console.error("取消收藏失败:", error)
      ElMessage.error("取消收藏失败，请稍后重试")
    }
  }
}

// 自建题库更多操作（编辑、分享、删除）
async function handleCommand(command: string, bank: BankItem) {
  switch (command) {
    case "edit":
      router.push({ path: "bankManage", query: { bankId: bank.id } })
      break
    case "share":
      ElMessage.info("分享题库功能开发中")
      break
    case "delete":
      try {
        await ElMessageBox.confirm(
          `确定要删除题库「${bank.name}」吗？删除后将无法恢复。`,
          "删除确认",
          {
            confirmButtonText: "确认删除",
            cancelButtonText: "取消",
            type: "warning"
          }
        )
        const success = await deleteQuestionBankById(bank.id)
        if (success) {
          const index = createdBanks.value.findIndex(b => b.id === bank.id)
          if (index !== -1) {
            createdBanks.value.splice(index, 1)
          }
          ElMessage.success("题库已删除")
        } else {
          ElMessage.error("删除失败，请稍后重试")
        }
      } catch (error) {
        if (error !== "cancel") {
          console.error("删除题库失败:", error)
          ElMessage.error("删除失败，请稍后重试")
        }
      }
      break
  }
}

// 切换自建题库公开/私有可见性
async function handleTypeChange(bank: BankItem, newValue: string | number | boolean) {
  const boolValue = newValue === true || newValue === "true" || newValue === 1
  const previousValue = !boolValue
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
    ElMessage.success(boolValue ? "已设为公开题库" : "已设为私有题库")
  } catch (error) {
    // 接口失败时回滚开关状态，保持界面与后端一致
    bank.isPublic = previousValue
    console.error("修改题库可见性失败:", error)
    ElMessage.error("修改失败，请稍后重试")
  }
}

// 页面跳转
function createBank() {
  router.push("/question-bank/createBank")
}

function goHome() {
  router.push("/home")
}

function goToQuestionDetail(id: string) {
  router.push({ name: "BankDetail", query: { id } })
}

function startPractice(bankId: string) {
  router.push({ path: "/question-bank/practice", query: { bank: bankId } })
}
</script>

<template>
  <div class="my-banks-page">
    <!-- 顶部页眉 -->
    <header class="page-header">
      <div class="header-text">
        <h1 class="page-title">
          我的题库
        </h1>
        <p class="page-subtitle">
          集中管理你收藏与自建的题库，随时进入专项练习或调整题目内容
        </p>
      </div>
      <button type="button" class="btn-primary" @click="createBank">
        <el-icon><Plus /></el-icon>
        <span>创建新题库</span>
      </button>
    </header>

    <!-- 一体化控制栏：Tab 切换 + 难度/可见性筛选 + 搜索 -->
    <section class="control-bar">
      <div class="control-top">
        <div class="tab-switcher" role="tablist" aria-label="题库分类">
          <button
            type="button"
            role="tab"
            class="tab-btn"
            :class="{ 'is-active': activeTab === 'favorite' }"
            @click="activeTab = 'favorite'"
          >
            <span>我收藏的题库</span>
            <span class="tab-count">{{ favoriteBanks.length }}</span>
          </button>
          <button
            type="button"
            role="tab"
            class="tab-btn"
            :class="{ 'is-active': activeTab === 'created' }"
            @click="activeTab = 'created'"
          >
            <span>我创建的题库</span>
            <span class="tab-count">{{ createdBanks.length }}</span>
          </button>
        </div>

        <div class="filter-group">
          <div class="filter-pills" role="group" aria-label="按难度筛选">
            <button
              v-for="diff in difficultyOptions"
              :key="diff"
              type="button"
              class="filter-pill"
              :class="{ 'is-active': selectedDifficulty === diff }"
              @click="selectedDifficulty = diff"
            >
              {{ diff }}
            </button>
          </div>

          <el-input
            v-model="searchQuery"
            placeholder="搜索题库名称或简介"
            clearable
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 仅在“我创建的题库”下展示紧凑的可见性筛选行 -->
      <div v-if="activeTab === 'created'" class="control-sub">
        <span class="sub-label">可见性</span>
        <div class="sub-pills">
          <button
            type="button"
            class="sub-pill"
            :class="{ 'is-active': viewMode === 'all' }"
            @click="viewMode = 'all'"
          >
            全部
          </button>
          <button
            type="button"
            class="sub-pill"
            :class="{ 'is-active': viewMode === 'public' }"
            @click="viewMode = 'public'"
          >
            已公开
          </button>
          <button
            type="button"
            class="sub-pill"
            :class="{ 'is-active': viewMode === 'private' }"
            @click="viewMode = 'private'"
          >
            仅自己可见
          </button>
        </div>
      </div>
    </section>

    <!-- 加载骨架屏 -->
    <section v-if="loading" class="bank-grid">
      <div v-for="n in 4" :key="n" class="bank-skeleton-card">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item variant="image" style="width: 100%; height: 150px;" />
            <div style="padding: 16px;">
              <el-skeleton-item variant="text" style="width: 45%; margin-bottom: 10px;" />
              <el-skeleton-item variant="h3" style="width: 80%; margin-bottom: 8px;" />
              <el-skeleton-item variant="text" style="width: 100%;" />
            </div>
          </template>
        </el-skeleton>
      </div>
    </section>

    <!-- 我收藏的题库列表 -->
    <section v-else-if="activeTab === 'favorite'">
      <div v-if="filteredFavoriteBanks.length > 0" class="bank-grid">
        <article
          v-for="bank in filteredFavoriteBanks"
          :key="bank.id"
          class="bank-card"
          @click="goToQuestionDetail(bank.id)"
        >
          <div class="bank-cover">
            <img :src="getFullImageUrl(bank.image)" :alt="bank.name" class="bank-cover-img" @error="onImgError">
          </div>

          <div class="bank-body">
            <div class="bank-meta-row">
              <div class="meta-badges">
                <span class="diff-badge" :class="getDifficultyClass(bank.difficulty)">
                  {{ bank.difficulty }}
                </span>
                <span class="vis-badge" :class="bank.isPublic ? 'vis-public' : 'vis-private'">
                  {{ bank.isPublic ? "公开" : "私有" }}
                </span>
              </div>
              <span class="bank-qcount">{{ bank.questionCount }} 题</span>
            </div>

            <h3 class="bank-name" :title="bank.name">
              {{ bank.name }}
            </h3>
            <p class="bank-desc" :title="bank.description">
              {{ bank.description || "暂无题库简介" }}
            </p>

            <div class="bank-footer" @click.stop>
              <button type="button" class="btn-primary btn-sm" @click="startPractice(bank.id)">
                <span>练习</span>
                <el-icon><ArrowRight /></el-icon>
              </button>
              <div class="footer-right-actions">
                <button type="button" class="btn-ghost btn-sm" @click="goToQuestionDetail(bank.id)">
                  详情
                </button>
                <button type="button" class="btn-ghost-danger btn-sm" @click="handleUnfavorite(bank)">
                  取消
                </button>
              </div>
            </div>
          </div>
        </article>
      </div>

      <div v-else class="empty-panel">
        <el-empty
          :description="favoriteBanks.length === 0 ? '你还没有收藏任何题库，去公共题库广场挑一套吧' : '没有符合当前筛选条件的收藏题库'"
          :image-size="76"
        >
          <button v-if="hasActiveFilters" type="button" class="btn-secondary" @click="resetFilters">
            重置筛选条件
          </button>
          <button v-else type="button" class="btn-primary" @click="goHome">
            去首页逛逛公共题库
          </button>
        </el-empty>
      </div>
    </section>

    <!-- 我创建的题库列表 -->
    <section v-else>
      <div v-if="filteredCreatedBanks.length > 0" class="bank-grid">
        <article
          v-for="bank in filteredCreatedBanks"
          :key="bank.id"
          class="bank-card"
          @click="goToQuestionDetail(bank.id)"
        >
          <div class="bank-cover">
            <img :src="getFullImageUrl(bank.image)" :alt="bank.name" class="bank-cover-img" @error="onImgError">
          </div>

          <div class="bank-body">
            <div class="bank-meta-row">
              <div class="meta-badges">
                <span class="diff-badge" :class="getDifficultyClass(bank.difficulty)">
                  {{ bank.difficulty }}
                </span>
                <span class="vis-badge" :class="bank.isPublic ? 'vis-public' : 'vis-private'">
                  {{ bank.isPublic ? "公开" : "私有" }}
                </span>
              </div>
              <span class="bank-qcount">{{ bank.questionCount }} 题</span>
            </div>

            <h3 class="bank-name" :title="bank.name">
              {{ bank.name }}
            </h3>
            <p class="bank-desc" :title="bank.description">
              {{ bank.description || "暂无题库简介" }}
            </p>

            <div class="bank-footer" @click.stop>
              <button type="button" class="btn-primary btn-sm" @click="startPractice(bank.id)">
                <span>练习</span>
                <el-icon><ArrowRight /></el-icon>
              </button>

              <div class="created-controls">
                <el-switch
                  v-model="bank.isPublic"
                  :active-value="true"
                  :inactive-value="false"
                  active-text="公开"
                  inactive-text="私有"
                  inline-prompt
                  style="--el-switch-on-color: #10b981; --el-switch-off-color: #94a3b8;"
                  @change="handleTypeChange(bank, $event as boolean)"
                />
                <el-dropdown trigger="click" @command="handleCommand($event, bank)">
                  <button type="button" class="more-btn" aria-label="更多操作">
                    <el-icon><ArrowDown /></el-icon>
                  </button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit">
                        管理与编辑题目
                      </el-dropdown-item>
                      <el-dropdown-item command="share">
                        分享题库链接
                      </el-dropdown-item>
                      <el-dropdown-item command="delete" divided>
                        <span class="text-danger">删除题库</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </article>
      </div>

      <div v-else class="empty-panel">
        <el-empty
          :description="createdBanks.length === 0 ? '还没有创建过专属题库，点击下方按钮立刻新建一套' : '没有符合当前筛选条件的自建题库'"
          :image-size="76"
        >
          <button v-if="hasActiveFilters" type="button" class="btn-secondary" @click="resetFilters">
            重置筛选条件
          </button>
          <button v-else type="button" class="btn-primary" @click="createBank">
            <el-icon><Plus /></el-icon>
            <span>创建新题库</span>
          </button>
        </el-empty>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* ======== 页面整体布局（直接融入 Layout 的 #f8fafc 底色） ======== */
.my-banks-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  color: #0f172a;
}

/* 通用按钮体系（与首页一致：8px 圆角 + 靛蓝主色） */
.btn-primary,
.btn-secondary,
.btn-ghost,
.btn-ghost-danger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 9px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  line-height: 1.2;
  transition:
    background-color 0.16s ease,
    border-color 0.16s ease,
    color 0.16s ease;
}

.btn-primary {
  background-color: #2563eb;
  color: #ffffff;
  border: 1px solid #2563eb;
}

.btn-primary:hover {
  background-color: #1d4ed8;
  border-color: #1d4ed8;
}

.btn-secondary {
  background-color: #ffffff;
  color: #334155;
  border: 1px solid #cbd5e1;
}

.btn-secondary:hover {
  background-color: #f8fafc;
  border-color: #94a3b8;
  color: #0f172a;
}

.btn-ghost {
  background-color: transparent;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.btn-ghost:hover {
  background-color: #f8fafc;
  border-color: #cbd5e1;
  color: #0f172a;
}

.btn-ghost-danger {
  background-color: transparent;
  color: #64748b;
  border: 1px solid transparent;
}

.btn-ghost-danger:hover {
  background-color: #fef2f2;
  border-color: #fecaca;
  color: #dc2626;
}

.btn-sm {
  padding: 5px 10px;
  font-size: 12px;
}

/* ======== 页眉区 ======== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 16px;
}

.page-title {
  margin: 0 0 6px 0;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #0f172a;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

/* ======== 一体化控制栏 ======== */
.control-bar {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.control-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
}

.tab-switcher {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  border-radius: 8px;
  border: 1px solid transparent;
  background-color: transparent;
  color: #475569;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.16s ease;
}

.tab-btn:hover {
  color: #0f172a;
  background-color: #f8fafc;
}

.tab-btn.is-active {
  background-color: #eff6ff;
  border-color: #dbeafe;
  color: #1d4ed8;
  font-weight: 600;
}

.tab-count {
  font-size: 12px;
  padding: 1px 7px;
  border-radius: 999px;
  background-color: #f1f5f9;
  color: #64748b;
  font-variant-numeric: tabular-nums;
}

.tab-btn.is-active .tab-count {
  background-color: #dbeafe;
  color: #1d4ed8;
}

.filter-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-pills {
  display: inline-flex;
  align-items: center;
  background-color: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 3px;
  gap: 2px;
}

.filter-pill {
  border: none;
  background: transparent;
  padding: 5px 11px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #475569;
  cursor: pointer;
  transition: all 0.15s ease;
}

.filter-pill:hover {
  color: #0f172a;
}

.filter-pill.is-active {
  background-color: #ffffff;
  color: #2563eb;
  font-weight: 600;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
}

.search-input {
  width: 230px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #cbd5e1 inset;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #2563eb inset;
}

.control-sub {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.sub-label {
  font-size: 13px;
  color: #64748b;
}

.sub-pills {
  display: inline-flex;
  gap: 6px;
}

.sub-pill {
  border: 1px solid #e2e8f0;
  background-color: #ffffff;
  color: #475569;
  font-size: 12px;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.sub-pill:hover {
  border-color: #cbd5e1;
  color: #0f172a;
}

.sub-pill.is-active {
  background-color: #eff6ff;
  border-color: #bfdbfe;
  color: #1d4ed8;
  font-weight: 600;
}

/* ======== 题库卡片网格（桌面端一行 4 列，紧凑展示） ======== */
.bank-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.bank-skeleton-card,
.bank-card {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.bank-card {
  cursor: pointer;
  transition:
    border-color 0.18s ease,
    box-shadow 0.18s ease,
    transform 0.18s ease;
}

.bank-card:hover {
  border-color: #93c5fd;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
  transform: translateY(-2px);
}

.bank-cover {
  width: 100%;
  aspect-ratio: 16 / 9;
  background-color: #f1f5f9;
  overflow: hidden;
  border-bottom: 1px solid #f1f5f9;
}

.bank-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.35s ease;
}

.bank-card:hover .bank-cover-img {
  transform: scale(1.03);
}

.bank-body {
  padding: 14px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.bank-meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.meta-badges {
  display: flex;
  align-items: center;
  gap: 6px;
}

.diff-badge,
.vis-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.4;
}

.diff-easy {
  background-color: #ecfdf5;
  color: #047857;
}

.diff-medium {
  background-color: #fffbeb;
  color: #b45309;
}

.diff-hard {
  background-color: #fef2f2;
  color: #b91c1c;
}

.diff-default {
  background-color: #f1f5f9;
  color: #475569;
}

.vis-public {
  background-color: #eff6ff;
  color: #1d4ed8;
}

.vis-private {
  background-color: #f1f5f9;
  color: #64748b;
}

.bank-qcount {
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  font-variant-numeric: tabular-nums;
}

.bank-name {
  margin: 0 0 6px 0;
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bank-desc {
  margin: 0 0 16px 0;
  font-size: 13px;
  line-height: 1.55;
  color: #64748b;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
  flex: 1;
}

.bank-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.footer-right-actions,
.created-controls {
  display: flex;
  align-items: center;
  gap: 6px;
}

.created-controls {
  gap: 10px;
}

.more-btn {
  width: 30px;
  height: 30px;
  border-radius: 7px;
  border: 1px solid #e2e8f0;
  background-color: #ffffff;
  color: #64748b;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.16s ease;
}

.more-btn:hover {
  border-color: #cbd5e1;
  background-color: #f8fafc;
  color: #0f172a;
}

.text-danger {
  color: #dc2626;
}

.empty-panel {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 48px 20px;
}

/* ======== 响应式适配 ======== */
@media screen and (max-width: 1120px) {
  .bank-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media screen and (max-width: 820px) {
  .bank-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media screen and (max-width: 560px) {
  .bank-grid {
    grid-template-columns: 1fr;
  }

  .search-input {
    width: 100%;
  }

  .filter-group {
    width: 100%;
  }
}
</style>
