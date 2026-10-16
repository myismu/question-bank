<template>
  <div class="activity-answers-manage">
    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="活动记录ID" prop="recordId">
          <el-input v-model="queryParams.recordId" placeholder="请输入活动记录ID" clearable />
        </el-form-item>
        <el-form-item label="题目ID" prop="questionId">
          <el-input v-model="queryParams.questionId" placeholder="请输入题目ID" clearable />
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
        <el-button type="primary" @click="handleAdd">新增答题记录</el-button>
      </template>

      <!-- 答题记录列表 -->
      <el-table v-loading="loading" :data="answersList">
        <el-table-column type="index" label="序号" width="50" />
        <el-table-column prop="id" label="记录ID" />
        <el-table-column prop="recordId" label="活动记录ID" />
        <el-table-column prop="questionId" label="题目ID" />
        <el-table-column prop="answer" label="答案" />
        <el-table-column prop="isCorrect" label="是否正确">
          <template #default="{ row }">
            <el-tag :type="row.isCorrect ? 'success' : 'danger'">
              {{ row.isCorrect ? '正确' : '错误' }}
            </el-tag>
          </template>
        </el-table-column>
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

    <!-- 答题记录表单对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" append-to-body>
      <el-form ref="answersFormRef" :model="answersForm" :rules="rules" label-width="100px">
        <el-form-item label="活动记录ID" prop="recordId">
          <el-input v-model="answersForm.recordId" placeholder="请输入活动记录ID" />
        </el-form-item>
        <el-form-item label="题目ID" prop="questionId">
          <el-input v-model="answersForm.questionId" placeholder="请输入题目ID" />
        </el-form-item>
        <el-form-item label="答案" prop="answer">
          <el-input v-model="answersForm.answer" placeholder="请输入答案" />
        </el-form-item>
        <el-form-item label="是否正确" prop="isCorrect">
          <el-radio-group v-model="answersForm.isCorrect">
            <el-radio :label="true">正确</el-radio>
            <el-radio :label="false">错误</el-radio>
          </el-radio-group>
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
import {
  getActivityAnswersListApi,
  addActivityAnswersApi,
  updateActivityAnswersApi,
  deleteActivityAnswersApi
} from './apis/ActivityAnswers'
import type { ActivityAnswersInfo, ActivityAnswersQuery, ActivityAnswersForm, PageResult } from './apis/type'

// 查询参数
const queryParams = reactive<ActivityAnswersQuery>({
  current: 1,
  size: 10,
  recordId: '',
  questionId: undefined
})

// 答题记录列表数据
const answersList = ref<ActivityAnswersInfo[]>([])
const total = ref(0)
const loading = ref(false)

// 表单相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const answersFormRef = ref<FormInstance>()
const answersForm = reactive<ActivityAnswersForm>({
  recordId: '',
  questionId: undefined,
  answer: '',
  isCorrect: true,
  score: 0
})

// 表单校验规则
const rules = {
  recordId: [
    { required: true, message: "请输入活动记录ID", trigger: "blur" }
  ] as FormItemRule[],
  questionId: [
    { required: true, message: "请输入题目ID", trigger: "blur" }
  ] as FormItemRule[],
  answer: [
    { required: true, message: "请输入答案", trigger: "blur" }
  ] as FormItemRule[],
  isCorrect: [
    { required: true, message: "请选择是否正确", trigger: "change" }
  ] as FormItemRule[]
}

// 获取答题记录列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getActivityAnswersListApi(queryParams)
    if (res.code === 200) {
      answersList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error("获取答题记录列表失败:", error)
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
  queryParams.recordId = ''
  queryParams.questionId = undefined
  handleQuery()
}

// 新增按钮
const handleAdd = () => {
  dialogTitle.value = '新增答题记录'
  dialogVisible.value = true
  answersForm.id = undefined
  answersForm.recordId = ''
  answersForm.questionId = undefined
  answersForm.answer = ''
  answersForm.isCorrect = true
  answersForm.score = 0
}

// 修改按钮
const handleUpdate = (row: ActivityAnswersInfo) => {
  dialogTitle.value = '编辑答题记录'
  dialogVisible.value = true
  Object.assign(answersForm, row)
}

// 删除按钮
const handleDelete = (row: ActivityAnswersInfo) => {
  ElMessageBox.confirm("确认要删除该答题记录吗？", "警告", {
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
      const res = await deleteActivityAnswersApi(row.id!)
      if (res.code === 200) {
        ElMessage.success(res.message)
        getList()
      }
    } catch (error) {
      console.error("删除答题记录失败:", error)
    }
  }).catch(() => {
    ElMessage.info("已取消删除")
  })
}

// 提交表单
const submitForm = async () => {
  if (!answersFormRef.value) return
  await answersFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (answersForm.id) {
          const res = await updateActivityAnswersApi(answersForm.id, answersForm)
          if (res.code === 200) {
            ElMessage.success(res.message)
            dialogVisible.value = false
            getList()
          }
        } else {
          const res = await addActivityAnswersApi(answersForm)
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
.activity-answers-manage {
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
