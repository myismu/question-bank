<template>
  <div class="learning-statistics-manage">
    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="题库ID" prop="bankId">
          <el-input v-model="queryParams.bankId" placeholder="请输入题库ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="table-card">
      <template #header>
        <el-button type="primary" @click="handleAdd">新增统计</el-button>
      </template>

      <!-- 统计列表 -->
      <el-table v-loading="loading" :data="statisticsList">
        <el-table-column type="index" label="序号" width="50" />
        <el-table-column prop="userId" label="用户ID" />
        <el-table-column prop="bankId" label="题库ID" />
        <el-table-column prop="practiceCount" label="练习次数" />
        <el-table-column prop="examCount" label="考试次数" />
        <el-table-column prop="totalQuestions" label="总题数" />
        <el-table-column prop="correctCount" label="正确数" />
        <el-table-column prop="wrongCount" label="错误数" />
        <el-table-column prop="averageScore" label="平均分" />
        <el-table-column prop="createdAt" label="创建时间" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleUpdate(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination v-model:current-page="queryParams.current" v-model:page-size="queryParams.size"
          :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 统计表单对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" append-to-body>
      <el-form ref="statisticsFormRef" :model="statisticsForm" :rules="rules" label-width="100px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="statisticsForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="题库ID" prop="bankId">
          <el-input v-model="statisticsForm.bankId" placeholder="请输入题库ID" />
        </el-form-item>
        <el-form-item label="练习次数" prop="practiceCount">
          <el-input v-model.number="statisticsForm.practiceCount" placeholder="请输入练习次数" />
        </el-form-item>
        <el-form-item label="考试次数" prop="examCount">
          <el-input v-model.number="statisticsForm.examCount" placeholder="请输入考试次数" />
        </el-form-item>
        <el-form-item label="总题数" prop="totalQuestions">
          <el-input v-model.number="statisticsForm.totalQuestions" placeholder="请输入总题数" />
        </el-form-item>
        <el-form-item label="正确数" prop="correctCount">
          <el-input v-model.number="statisticsForm.correctCount" placeholder="请输入正确数" />
        </el-form-item>
        <el-form-item label="错误数" prop="wrongCount">
          <el-input v-model.number="statisticsForm.wrongCount" placeholder="请输入错误数" />
        </el-form-item>
        <el-form-item label="平均分" prop="averageScore">
          <el-input v-model.number="statisticsForm.averageScore" placeholder="请输入平均分" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormItemRule } from 'element-plus'
import { getLearningStatisticsListApi, createLearningStatisticsApi, updateLearningStatisticsApi, deleteLearningStatisticsApi } from './apis/LearningStatistics'
import type { LearningStatistics } from './apis/type'

// 查询参数
const queryParams = reactive<LearningStatistics.LearningStatisticsQuery>({
  current: 1,
  size: 10,
  userId: '',
  bankId: ''
})

// 统计列表数据
const statisticsList = ref<LearningStatistics.LearningStatisticsInfo[]>([])
const total = ref(0)
const loading = ref(false)

// 表单相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const statisticsFormRef = ref<FormInstance>()
const statisticsForm = reactive<LearningStatistics.LearningStatisticsForm>({
  userId: '',
  bankId: '',
  practiceCount: 0,
  examCount: 0,
  totalQuestions: 0,
  correctCount: 0,
  wrongCount: 0,
  averageScore: 0
})

// 表单校验规则
const rules = {
  userId: [
    { required: true, message: "请输入用户ID", trigger: "blur" }
  ] as FormItemRule[],
  bankId: [
    { required: true, message: "请输入题库ID", trigger: "blur" }
  ] as FormItemRule[],
  practiceCount: [
    { required: true, message: "请输入练习次数", trigger: "blur" },
    { type: "number", message: "练习次数必须为数字", trigger: "blur" }
  ] as FormItemRule[],
  examCount: [
    { required: true, message: "请输入考试次数", trigger: "blur" },
    { type: "number", message: "考试次数必须为数字", trigger: "blur" }
  ] as FormItemRule[],
  totalQuestions: [
    { required: true, message: "请输入总题数", trigger: "blur" },
    { type: "number", message: "总题数必须为数字", trigger: "blur" }
  ] as FormItemRule[],
  correctCount: [
    { required: true, message: "请输入正确数", trigger: "blur" },
    { type: "number", message: "正确数必须为数字", trigger: "blur" }
  ] as FormItemRule[],
  wrongCount: [
    { required: true, message: "请输入错误数", trigger: "blur" },
    { type: "number", message: "错误数必须为数字", trigger: "blur" }
  ] as FormItemRule[],
  averageScore: [
    { required: true, message: "请输入平均分", trigger: "blur" },
    { type: "number", message: "平均分必须为数字", trigger: "blur" }
  ] as FormItemRule[]
}

// 获取统计列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getLearningStatisticsListApi(queryParams)
    if (res.code === 200) {
      statisticsList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error("获取统计列表失败:", error)
  } finally {
    loading.value = false
  }
}

// 查询按钮
const handleQuery = () => {
  queryParams.current = 1
  getList()
}

// 重置按钮
const resetQuery = () => {
  queryParams.userId = ''
  queryParams.bankId = ''
  handleQuery()
}

// 新增按钮
const handleAdd = () => {
  dialogTitle.value = '新增统计'
  dialogVisible.value = true
  statisticsForm.id = undefined
  statisticsForm.userId = ''
  statisticsForm.bankId = ''
  statisticsForm.practiceCount = 0
  statisticsForm.examCount = 0
  statisticsForm.totalQuestions = 0
  statisticsForm.correctCount = 0
  statisticsForm.wrongCount = 0
  statisticsForm.averageScore = 0
}

// 修改按钮
const handleUpdate = (row: LearningStatistics.LearningStatisticsInfo) => {
  dialogTitle.value = '编辑统计'
  dialogVisible.value = true
  Object.assign(statisticsForm, row)
}

// 删除按钮
const handleDelete = (row: LearningStatistics.LearningStatisticsInfo) => {
  ElMessageBox.confirm("确认要删除该统计吗？", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
    draggable: true,
    closeOnClickModal: false,
    closeOnPressEscape: false,
    showClose: true,
    center: true
  }).then(async () => {
    try {
      const res = await deleteLearningStatisticsApi(row.id)
      if (res.code === 200) {
        ElMessage.success(res.message)
        getList()
      }
    } catch (error) {
      console.error("删除统计失败:", error)
    }
  }).catch(() => {
    ElMessage.info("已取消删除")
  })
}

// 提交表单
const submitForm = async () => {
  if (!statisticsFormRef.value) return
  await statisticsFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (statisticsForm.id) {
          const res = await updateLearningStatisticsApi(statisticsForm.id, statisticsForm)
          if (res.code === 200) {
            ElMessage.success(res.message)
            dialogVisible.value = false
            getList()
          }
        } else {
          const res = await createLearningStatisticsApi(statisticsForm)
          if (res.code === 200) {
            ElMessage.success(res.message)
            dialogVisible.value = false
            getList()
          }
        }
      } catch (error) {
        console.error("提交表单失败:", error)
      }
    }
  })
}

// 分页大小改变
const handleSizeChange = (val: number) => {
  queryParams.size = val
  getList()
}

// 页码改变
const handleCurrentChange = (val: number) => {
  queryParams.current = val
  getList()
}

// 初始化
onMounted(() => {
  getList()
})
</script>

<style scoped>
.learning-statistics-manage {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  text-align: right;
}
</style>
