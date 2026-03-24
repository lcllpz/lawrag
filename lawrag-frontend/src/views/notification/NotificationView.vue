<template>
  <div class="notification-page">
    <!-- 顶部操作栏 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">通知中心</h2>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="unread-badge">
          <span class="unread-text">{{ unreadCount }} 条未读</span>
        </el-badge>
      </div>
      <div class="header-right">
        <el-button v-if="userStore.isAdmin" type="primary" size="small" @click="sendDialogVisible = true">
          <el-icon><Promotion /></el-icon> 发送通知
        </el-button>
        <el-button size="small" @click="markAllRead" :disabled="unreadCount === 0">
          <el-icon><Select /></el-icon> 全部已读
        </el-button>
        <el-button size="small" :icon="Refresh" @click="loadData" :loading="loading" circle />
      </div>
    </div>

    <!-- 发送通知 Dialog（管理员） -->
    <el-dialog v-model="sendDialogVisible" title="发送系统通知" width="500px" @close="resetSendForm">
      <el-form :model="sendForm" label-width="80px">
        <el-form-item label="通知类型">
          <el-select v-model="sendForm.type" style="width:100%">
            <el-option label="系统通知" value="system" />
            <el-option label="安全通知" value="security" />
            <el-option label="活动消息" value="activity" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知标题">
          <el-input v-model="sendForm.title" placeholder="请输入通知标题" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="通知内容">
          <el-input v-model="sendForm.content" type="textarea" :rows="4" placeholder="请输入通知内容" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="发送对象">
          <el-radio-group v-model="sendForm.targetType">
            <el-radio value="all">全体用户</el-radio>
            <el-radio value="user">指定用户ID</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="sendForm.targetType === 'user'" label="用户ID">
          <el-input-number v-model="sendForm.targetUserId" :min="1" placeholder="输入用户ID" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="sendLoading" @click="doSendNotification">发送</el-button>
      </template>
    </el-dialog>

    <el-card class="notification-card" shadow="never">
      <!-- Tab过滤 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="系统通知" name="system" />
        <el-tab-pane label="安全通知" name="security" />
        <el-tab-pane label="活动消息" name="activity" />
      </el-tabs>

      <!-- 通知列表 -->
      <div v-loading="loading" class="notif-list">
        <div
          v-for="notif in notifications"
          :key="notif.id"
          class="notif-item"
          :class="{ unread: notif.isRead === 0 }"
        >
          <div class="notif-icon" :class="notif.type">
            <el-icon size="18">
              <Bell v-if="notif.type === 'system'" />
              <Lock v-else-if="notif.type === 'security'" />
              <Star v-else />
            </el-icon>
          </div>
          <div class="notif-body">
            <div class="notif-title">
              {{ notif.title }}
              <el-tag v-if="notif.isRead === 0" type="danger" size="small" effect="plain">未读</el-tag>
            </div>
            <div class="notif-content">{{ notif.content }}</div>
            <div class="notif-time">{{ notif.createTime }}</div>
          </div>
          <div class="notif-actions">
            <el-button v-if="notif.isRead === 0" text size="small" @click="markRead(notif)">已读</el-button>
            <el-button text size="small" type="danger" @click="deleteNotif(notif.id)">删除</el-button>
          </div>
        </div>

        <el-empty v-if="!loading && notifications.length === 0" description="暂无通知" :image-size="80" />
      </div>

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
          small
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Refresh, Bell, Lock, Star, Select, Promotion } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { notificationApi, type Notification } from '@/api/notification'
import { useUserStore } from '@/stores/user'

const loading = ref(false)
const notifications = ref<Notification[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const unreadCount = ref(0)
const activeTab = ref('all')
const userStore = useUserStore()

// 发送通知
const sendDialogVisible = ref(false)
const sendLoading = ref(false)
const sendForm = ref({
  type: 'system',
  title: '',
  content: '',
  targetType: 'all',
  targetUserId: null as number | null
})

const resetSendForm = () => {
  sendForm.value = { type: 'system', title: '', content: '', targetType: 'all', targetUserId: null }
}

const doSendNotification = async () => {
  if (!sendForm.value.title.trim()) { ElMessage.warning('请输入通知标题'); return }
  if (!sendForm.value.content.trim()) { ElMessage.warning('请输入通知内容'); return }
  sendLoading.value = true
  try {
    const msg = await notificationApi.send({
      title: sendForm.value.title,
      content: sendForm.value.content,
      type: sendForm.value.type,
      targetUserId: sendForm.value.targetType === 'user' ? sendForm.value.targetUserId : null
    })
    ElMessage.success(msg || '发送成功')
    sendDialogVisible.value = false
    resetSendForm()
  } finally {
    sendLoading.value = false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (activeTab.value !== 'all') {
      params.type = activeTab.value
    }
    const res = await notificationApi.getList(params)
    notifications.value = res.records || []
    total.value = res.total || 0
  } catch {
    notifications.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
  try {
    const count = await notificationApi.getUnreadCount()
    unreadCount.value = count
  } catch {}
}

const handleTabChange = () => {
  currentPage.value = 1
  loadData()
}

const markRead = async (notif: Notification) => {
  try {
    await notificationApi.markRead(notif.id)
    notif.isRead = 1
    if (unreadCount.value > 0) unreadCount.value--
  } catch {}
}

const markAllRead = async () => {
  try {
    await notificationApi.markAllRead()
    notifications.value.forEach(n => (n.isRead = 1))
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch {}
}

const deleteNotif = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该通知？', '提示', { type: 'warning' })
    await notificationApi.deleteNotification(id)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

onMounted(loadData)
</script>

<style scoped>
.notification-page {
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
  gap: 12px;
}

.page-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}

.unread-text {
  font-size: 13px;
  color: #64748b;
}

.header-right {
  display: flex;
  gap: 8px;
}

.notification-card {
  border: 1px solid #ede8de;
  border-radius: 12px;
}

.notif-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  min-height: 200px;
}

.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px;
  border-bottom: 1px solid #f1f5f9;
  transition: background 0.2s;
  position: relative;
}

.notif-item:last-child {
  border-bottom: none;
}

.notif-item:hover {
  background: #faf9f6;
}

.notif-item.unread {
  background: #fffdf7;
}

.notif-item.unread::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #b8943f;
  border-radius: 0 2px 2px 0;
}

.notif-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notif-icon.system {
  background: #eff6ff;
  color: #3b82f6;
}

.notif-icon.security {
  background: #fef2f2;
  color: #ef4444;
}

.notif-icon.activity {
  background: rgba(184, 148, 63, 0.1);
  color: #b8943f;
}

.notif-body {
  flex: 1;
  min-width: 0;
}

.notif-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.notif-content {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.notif-time {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}

.notif-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0;
}
</style>
