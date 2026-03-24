<template>
  <div class="audit-log-page">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-row :gutter="12" align="middle">
        <el-col :span="5">
          <el-input
            v-model="searchForm.username"
            placeholder="用户名"
            clearable
            :prefix-icon="User"
          />
        </el-col>
        <el-col :span="5">
          <el-input
            v-model="searchForm.operation"
            placeholder="操作类型"
            clearable
            :prefix-icon="Search"
          />
        </el-col>
        <el-col :span="8">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-col>
        <el-col :span="6">
          <div class="search-actions">
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="logs"
        v-loading="loading"
        stripe
        row-key="id"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column prop="username" label="用户名" width="120">
          <template #default="{ row }">
            <span class="username-cell">{{ row.username }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="operation" label="操作描述" min-width="160" />

        <el-table-column label="请求方法" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getMethodType(row.method)" size="small" effect="plain">
              {{ row.method }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="requestUrl" label="请求路径" min-width="200">
          <template #default="{ row }">
            <span class="url-cell" :title="row.requestUrl">
              {{ row.requestUrl?.length > 40 ? row.requestUrl.substring(0, 40) + '...' : row.requestUrl }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="ip" label="IP 地址" width="140" />

        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="操作时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, User } from '@element-plus/icons-vue'
import { auditLogApi, type AuditLog } from '@/api/auditLog'

const loading = ref(false)
const logs = ref<AuditLog[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dateRange = ref<[string, string] | null>(null)

const searchForm = reactive({
  username: '',
  operation: '',
})

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {
      current: currentPage.value,
      size: pageSize.value,
      username: searchForm.username || undefined,
      operation: searchForm.operation || undefined,
    }
    if (dateRange.value) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await auditLogApi.getList(params)
    logs.value = res.records || []
    total.value = res.total || 0
  } catch {
    logs.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.operation = ''
  dateRange.value = null
  currentPage.value = 1
  loadData()
}

const getMethodType = (method: string) => {
  const map: Record<string, string> = {
    GET: 'info',
    POST: 'success',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: '',
  }
  return map[method?.toUpperCase()] || ''
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(loadData)
</script>

<style scoped>
.audit-log-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card,
.table-card {
  border: 1px solid #ede8de;
  border-radius: 12px;
}

.search-actions {
  display: flex;
  gap: 8px;
}

.username-cell {
  font-weight: 600;
  color: #1e293b;
  font-size: 13px;
}

.url-cell {
  font-size: 12px;
  color: #64748b;
  font-family: monospace;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
