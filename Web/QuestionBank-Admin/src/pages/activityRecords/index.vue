<template>
  <div class="activity-records-manage">
    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="题库ID" prop="bankId">
          <el-input v-model="queryParams.bankId" placeholder="请输入题库ID" clearable />
        </el-form-item>
        <el-form-item label="活动类型" prop="type">
          <el-select v-model="queryParams.type" placeholder="请选择活动类型" clearable>
            <el-option label="练习" value="practice" />
            <el-option label="考试" value="exam" />
            <el-option label="学习" value="study" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
            <el-option label="超时" value="timeout" />
            <el-option label="已放弃" value="abandoned" />
          </el-select>
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
        <el-button type="primary" @click="handleAdd">新增记录</el-button>
      </template>

      <!-- 记录列表 -->
      <el-table v-loading="loading" :data="recordsList">
        <el-table-column type="index" label="序号" width="50" />
        <el-table-column prop="userId" label="用户ID" />
        <el-table-column prop="bankId" label="题库ID" />
        <el-table-column prop="type" label="活动类型">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" />
        <el-table-column prop="endTime" label="结束时间" />
        <el-table-column prop="userScore" label="用户得分" />
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

    <!-- 记录表单对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" append-to-body>
      <el-form ref="recordsFormRef" :model="recordsForm" :rules="rules" label-width="100px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="recordsForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="题库ID" prop="bankId">
          <el-input v-model="recordsForm.bankId" placeholder="请输入题库ID" />
        </el-form-item>
        <el-form-item label="活动类型" prop="type">
          <el-select v-model="recordsForm.type" placeholder="请选择活动类型">
            <el-option label="练习" value="practice" />
            <el-option label="考试" value="exam" />
            <el-option label="学习" value="study" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="recordsForm.startTime" type="datetime" placeholder="请选择开始时间" />
        </el-form-item>

        <el-form-item label="用户得分" prop="userScore">
          <el-input v-model.number="recordsForm.userScore" placeholder="请输入用户得分" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="recordsForm.status" placeholder="请选择状态">
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
            <el-option label="超时" value="timeout" />
            <el-option label="已放弃" value="abandoned" />
          </el-select>
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
import { getActivityRecordsListApi, createActivityRecordsApi, updateActivityRecordsApi, deleteActivityRecordsApi } from './apis/ActivityRecords'
import type { ActivityRecords } from './apis/type'

// 查询参数
const queryParams = reactive<ActivityRecords.ActivityRecordsQuery>({
  current: 1,
  size: 10,
  userId: '',
  bankId: '',
  type: '',
  status: ''
})

// 记录列表数据
const recordsList = ref<ActivityRecords.ActivityRecordsInfo[]>([])
const total = ref(0)
const loading = ref(false)

// 表单相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const recordsFormRef = ref<FormInstance>()
const recordsForm = reactive<ActivityRecords.ActivityRecordsForm>({
  userId: '',
  bankId: '',
  type: '',
  startTime: '',
  endTime: '',
  totalScore: 0,
  userScore: 0,
  status: ''
})

// 活动类型标签映射
const getTypeTag = (type: string) => {
  const typeMap: Record<string, 'primary' | 'warning' | 'success' | 'info' | 'danger'> = {
    practice: 'primary',
    exam: 'warning',
    study: 'success'
  }
  return typeMap[type] || 'info'
}

// 活动类型文本映射
const getTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    practice: '练习',
    exam: '考试',
    study: '学习'
  }
  return typeMap[type] || '未知'
}

// 状态标签映射
const getStatusTag = (status: 'in_progress' | 'completed' | 'timeout' | 'abandoned') => {
  const statusMap: Record<string, 'primary' | 'warning' | 'success' | 'info' | 'danger'> = {
    in_progress: 'primary',
    completed: 'success',
    timeout: 'warning',
    abandoned: 'danger'
  }
  return statusMap[status] || 'info'
}

// 状态文本映射
const getStatusText = (status: 'in_progress' | 'completed' | 'timeout' | 'abandoned') => {
  const statusMap: Record<string, string> = {
    in_progress: '进行中',
    completed: '已完成',
    timeout: '超时',
    abandoned: '已放弃'
  }
  return statusMap[status] || '未知'
}

// 表单校验规则
const rules = {
  userId: [
    { required: true, message: "请输入用户ID", trigger: "blur" }
  ] as FormItemRule[],
  bankId: [
    { required: true, message: "请输入题库ID", trigger: "blur" }
  ] as FormItemRule[],
  type: [
    { required: true, message: "请选择活动类型", trigger: "change" }
  ] as FormItemRule[],
  startTime: [
    { required: true, message: "请输入开始时间", trigger: "blur" }
  ] as FormItemRule[],
  endTime: [
    { required: false, message: "请输入结束时间", trigger: "blur" }
  ] as FormItemRule[],
  totalScore: [
    { required: false, message: "请输入总分", trigger: "blur" }
  ] as FormItemRule[],
  userScore: [
    { required: false, message: "请输入用户得分", trigger: "blur" }
  ] as FormItemRule[],
  status: [
    { required: true, message: "请选择状态", trigger: "change" }
  ] as FormItemRule[]
}

// 获取记录列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getActivityRecordsListApi(queryParams)
    if (res.code === 200) {
      recordsList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error("获取记录列表失败:", error)
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
  queryParams.type = ''
  queryParams.status = ''
  handleQuery()
}

// 新增按钮
const handleAdd = () => {
  dialogTitle.value = '新增记录'
  dialogVisible.value = true
  recordsForm.id = undefined
  recordsForm.userId = ''
  recordsForm.bankId = ''
  recordsForm.type = ''
  recordsForm.startTime = ''
  recordsForm.endTime = ''
  recordsForm.totalScore = 0
  recordsForm.userScore = 0
  recordsForm.status = ''
}

// 修改按钮
const handleUpdate = (row: ActivityRecords.ActivityRecordsInfo) => {
  dialogTitle.value = '编辑记录'
  dialogVisible.value = true
  Object.assign(recordsForm, row)
}

// 删除按钮
const handleDelete = (row: ActivityRecords.ActivityRecordsInfo) => {
  ElMessageBox.confirm("确认要删除该记录吗？", "警告", {
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
      const res = await deleteActivityRecordsApi(row.id)
      if (res.code === 200) {
        ElMessage.success(res.message)
        getList()
      }
    } catch (error) {
      console.error("删除记录失败:", error)
    }
  }).catch(() => {
    ElMessage.info("已取消删除")
  })
}

// 提交表单
const submitForm = async () => {
  if (!recordsFormRef.value) return
  await recordsFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (recordsForm.id) {
          const res = await updateActivityRecordsApi(recordsForm.id, recordsForm)
          if (res.code === 200) {
            ElMessage.success(res.message)
            dialogVisible.value = false
            getList()
          }
        } else {
          const res = await createActivityRecordsApi(recordsForm)
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
.activity-records-manage {
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
