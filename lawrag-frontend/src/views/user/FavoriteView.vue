<template>
  <div class="favorite-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">我的收藏</h2>
        <span class="total-hint">共 {{ total }} 条</span>
      </div>
      <div class="header-right">
        <el-input
          v-model="noteKeyword"
          placeholder="搜索收藏备注"
          :prefix-icon="Search"
          clearable
          style="width: 220px"
          @input="handleSearch"
        />
        <el-button :icon="Refresh" circle @click="loadData" :loading="loading" />
      </div>
    </div>

    <!-- 收藏卡片列表 -->
    <div v-loading="loading" class="favorite-list">
      <template v-if="favorites.length > 0">
        <div
          v-for="item in favorites"
          :key="item.id"
          class="favorite-card"
        >
          <!-- 卡片顶部：消息内容 -->
          <div class="card-content-wrap">
            <div class="content-icon">
              <el-icon size="16" color="#b8943f"><ChatDotRound /></el-icon>
            </div>
            <div class="content-text">
              {{ item.messageContent?.length > 100
                ? item.messageContent.substring(0, 100) + '...'
                : (item.messageContent || '（消息内容已删除）') }}
            </div>
          </div>

          <!-- 卡片底部：备注 + 时间 + 操作 -->
          <div class="card-footer">
            <div class="card-meta">
              <div class="card-note" v-if="item.note">
                <el-icon size="12" color="#94a3b8"><EditPen /></el-icon>
                <span>{{ item.note }}</span>
              </div>
              <div class="card-note no-note" v-else>
                <el-icon size="12" color="#cbd5e1"><Memo /></el-icon>
                <span>暂无备注</span>
              </div>
              <div class="card-time">
                <el-icon size="11" color="#cbd5e1"><Clock /></el-icon>
                <span>{{ formatDate(item.createTime) }}</span>
              </div>
            </div>
            <div class="card-actions">
              <el-button
                size="small"
                type="primary"
                plain
                @click="goToConversation(item.conversationId)"
              >
                查看对话
              </el-button>
              <el-button
                size="small"
                type="danger"
                plain
                @click="removeFavorite(item)"
              >
                取消收藏
              </el-button>
            </div>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <div v-if="!loading && favorites.length === 0" class="empty-wrap">
        <el-empty description="暂无收藏内容，在问答中点击收藏按钮即可保存">
          <el-button type="primary" @click="router.push('/chat')">去问答</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, ChatDotRound, EditPen, Memo, Clock } from '@element-plus/icons-vue'
import { favoriteApi, type Favorite } from '@/api/favorite'

const router = useRouter()
const loading = ref(false)
const favorites = ref<Favorite[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const noteKeyword = ref('')

let searchTimer: ReturnType<typeof setTimeout> | null = null

const loadData = async () => {
  loading.value = true
  try {
    const res = await favoriteApi.getList({
      current: currentPage.value,
      size: pageSize.value,
    })
    favorites.value = res.records || []
    total.value = res.total || 0
  } catch {
    favorites.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    loadData()
  }, 300)
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

const goToConversation = (conversationId: number) => {
  router.push(`/chat/${conversationId}`)
}

const removeFavorite = async (item: Favorite) => {
  try {
    await ElMessageBox.confirm('确认取消收藏？', '提示', { type: 'warning' })
    await favoriteApi.remove(item.id)
    ElMessage.success('已取消收藏')
    loadData()
  } catch {}
}

onMounted(loadData)
</script>

<style scoped>
.favorite-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}

.total-hint {
  font-size: 13px;
  color: #94a3b8;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 收藏列表 */
.favorite-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}

/* 收藏卡片 */
.favorite-card {
  background: #ffffff;
  border: 1px solid #ede8de;
  border-radius: 12px;
  padding: 18px 20px 14px;
  transition: box-shadow 0.2s, border-color 0.2s;
  position: relative;
}

.favorite-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 14px;
  bottom: 14px;
  width: 3px;
  background: linear-gradient(to bottom, #b8943f, rgba(184, 148, 63, 0.3));
  border-radius: 0 2px 2px 0;
}

.favorite-card:hover {
  border-color: rgba(184, 148, 63, 0.4);
  box-shadow: 0 4px 16px rgba(184, 148, 63, 0.1);
}

/* 消息内容区 */
.card-content-wrap {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 14px;
}

.content-icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: rgba(184, 148, 63, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 1px;
}

.content-text {
  flex: 1;
  font-size: 14px;
  color: #1e293b;
  line-height: 1.6;
  font-weight: 500;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-top: 1px solid #f8f9fa;
  padding-top: 12px;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.card-note {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
  overflow: hidden;
}

.card-note.no-note {
  color: #cbd5e1;
}

.card-note span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.card-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #94a3b8;
  white-space: nowrap;
  flex-shrink: 0;
}

.card-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* 空状态 */
.empty-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #ede8de;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
}
</style>
