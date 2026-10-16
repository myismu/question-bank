<script lang="ts" setup>
import { ref, reactive, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import type { GlobalLogDTO, GlobalLogPageParams } from "./apis/type"
import {
  getGlobalLogPage,
  getGlobalLogById,
  createGlobalLog,
  updateGlobalLog,
  deleteGlobalLog
} from "./apis"

// 分页数据
const tableData = ref<GlobalLogDTO[]>([])
const total = ref(0)
const loading = ref(false)
const pageParams = reactive<GlobalLogPageParams>({ current: 1, size: 10 })

// 查询表单
const searchForm = reactive({
  requestUrl: "",
  userName: "",
  responseStatusCode: undefined as number | undefined
})

// 详情弹窗
const detailDialogVisible = ref(false)
const detailData = ref<GlobalLogDTO | null>(null)

// 编辑弹窗
const editDialogVisible = ref(false)
const editData = ref<Partial<GlobalLogDTO>>({})
const isEdit = ref(false)

// 获取分页数据
function fetchTableData() {
  loading.value = true
  getGlobalLogPage({
    ...pageParams,
    ...searchForm
  })
    .then(res => {
      tableData.value = res.data.records
      total.value = res.data.total
    })
    .finally(() => {
      loading.value = false
    })
}

// 搜索
function handleSearch() {
  pageParams.current = 1
  fetchTableData()
}

// 重置
function handleReset() {
  searchForm.requestUrl = ""
  searchForm.userName = ""
  searchForm.responseStatusCode = undefined
  handleSearch()
}

// 分页切换
function handleCurrentChange(val: number) {
  pageParams.current = val
  fetchTableData()
}

// 查看详情
function handleView(row: GlobalLogDTO) {
  getGlobalLogById(row.id).then(res => {
    detailData.value = res.data
    detailDialogVisible.value = true
  })
}

// 新增
function handleAdd() {
  isEdit.value = false
  editData.value = {}
  editDialogVisible.value = true
}

// 编辑
function handleEdit(row: GlobalLogDTO) {
  isEdit.value = true
  editData.value = { ...row }
  editDialogVisible.value = true
}

// 删除
function handleDelete(row: GlobalLogDTO) {
  ElMessageBox.confirm("确定要删除这条日志吗？", "提示", { type: "warning" })
    .then(() => {
      deleteGlobalLog(row.id).then(() => {
        ElMessage.success("删除成功")
        fetchTableData()
      })
    })
    .catch(() => {})
}

// 保存（新增/编辑）
function handleSave() {
  const api = isEdit.value ? updateGlobalLog : createGlobalLog
  const id = editData.value.id
  api(id, editData.value).then(() => {
    ElMessage.success(isEdit.value ? "修改成功" : "新增成功")
    editDialogVisible.value = false
    fetchTableData()
  })
}

onMounted(() => {
  fetchTableData()
})
</script>

<template>
  <div class="global-log-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="请求URL">
          <el-input v-model="searchForm.requestUrl" placeholder="请求URL" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.userName" placeholder="用户名" />
        </el-form-item>
        <el-form-item label="状态码">
          <el-input v-model.number="searchForm.responseStatusCode" placeholder="状态码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增日志</el-button>
    </div>
    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="120" />
      <el-table-column prop="requestUrl" label="请求URL" />
      <el-table-column prop="userName" label="用户名" width="120" />
      <el-table-column prop="responseStatusCode" label="状态码" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button size="small" @click="handleView(scope.row)">详情</el-button>
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="pageParams.current"
      :page-size="pageParams.size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="handleCurrentChange"
      class="pagination"
    />

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="日志详情" width="600px">
      <el-descriptions v-if="detailData" :column="1" border>
        <el-descriptions-item label="ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ detailData.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ detailData.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailData.userName }}</el-descriptions-item>
        <el-descriptions-item label="状态码">{{ detailData.responseStatusCode }}</el-descriptions-item>
        <el-descriptions-item label="IP">{{ detailData.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="请求头">{{ detailData.requestHeaders }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">{{ detailData.requestParams }}</el-descriptions-item>
        <el-descriptions-item label="响应头">{{ detailData.responseHeaders }}</el-descriptions-item>
        <el-descriptions-item label="响应体">{{ detailData.responseBody }}</el-descriptions-item>
        <el-descriptions-item label="异常信息">{{ detailData.exceptionMessage }}</el-descriptions-item>
        <el-descriptions-item label="异常堆栈">{{ detailData.exceptionStackTrace }}</el-descriptions-item>
        <el-descriptions-item label="User-Agent">{{ detailData.userAgent }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑日志' : '新增日志'" width="600px">
      <el-form :model="editData" label-width="100px">
        <el-form-item label="请求URL">
          <el-input v-model="editData.requestUrl" />
        </el-form-item>
        <el-form-item label="请求方法">
          <el-input v-model="editData.requestMethod" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="editData.userName" />
        </el-form-item>
        <el-form-item label="状态码">
          <el-input v-model.number="editData.responseStatusCode" />
        </el-form-item>
        <el-form-item label="IP">
          <el-input v-model="editData.ipAddress" />
        </el-form-item>
        <el-form-item label="请求头">
          <el-input v-model="editData.requestHeaders" />
        </el-form-item>
        <el-form-item label="请求参数">
          <el-input v-model="editData.requestParams" />
        </el-form-item>
        <el-form-item label="响应头">
          <el-input v-model="editData.responseHeaders" />
        </el-form-item>
        <el-form-item label="响应体">
          <el-input v-model="editData.responseBody" />
        </el-form-item>
        <el-form-item label="异常信息">
          <el-input v-model="editData.exceptionMessage" />
        </el-form-item>
        <el-form-item label="异常堆栈">
          <el-input v-model="editData.exceptionStackTrace" />
        </el-form-item>
        <el-form-item label="User-Agent">
          <el-input v-model="editData.userAgent" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.global-log-container {
  padding: 30px;
  .search-bar {
    margin-bottom: 20px;
  }
  .toolbar {
    margin-bottom: 10px;
  }
  .pagination {
    margin-top: 20px;
    text-align: right;
  }
}
</style>
