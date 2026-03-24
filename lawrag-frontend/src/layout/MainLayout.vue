<template>
  <div class="main-layout">

    <!-- ===== Sidebar ===== -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">

      <!-- Logo -->
      <div class="sidebar-logo" @click="router.push('/cases')">
        <div class="logo-scales-wrap">
          <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg" width="24" height="24">
            <line x1="20" y1="4" x2="20" y2="36" stroke="#b8943f" stroke-width="2"/>
            <line x1="6"  y1="8" x2="34" y2="8"  stroke="#b8943f" stroke-width="2"/>
            <path d="M6 8 L2 20 Q6 25 10 20 L6 8Z"   fill="none" stroke="#b8943f" stroke-width="1.6" stroke-linejoin="round"/>
            <path d="M34 8 L30 20 Q34 25 38 20 L34 8Z" fill="none" stroke="#b8943f" stroke-width="1.6" stroke-linejoin="round"/>
            <line x1="14" y1="36" x2="26" y2="36" stroke="#b8943f" stroke-width="2"/>
          </svg>
        </div>
        <span class="logo-text" v-show="!isCollapsed"><em>AdmissionRAG</em></span>
      </div>

      <!-- Nav -->
      <nav class="sidebar-nav">
        <div class="nav-section-label" v-show="!isCollapsed">主要功能</div>
        <router-link
          v-for="item in mainMenu"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          <el-icon size="18"><component :is="item.icon" /></el-icon>
          <span class="nav-label" v-show="!isCollapsed">{{ item.label }}</span>
          <span v-if="item.badge && !isCollapsed" class="nav-badge">{{ item.badge }}</span>
        </router-link>

        <template v-if="userStore.isAdmin">
          <div class="nav-divider"></div>
          <div class="nav-section-label" v-show="!isCollapsed">管理后台</div>
          <router-link
            v-for="item in adminMenu"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            <el-icon size="18"><component :is="item.icon" /></el-icon>
            <span class="nav-label" v-show="!isCollapsed">{{ item.label }}</span>
          </router-link>
        </template>
      </nav>

      <!-- User info -->
      <div class="sidebar-user" v-show="!isCollapsed">
        <el-avatar :size="34" :src="userStore.userInfo?.avatar" class="user-avatar-el">
          {{ userStore.userInfo?.nickname?.charAt(0) }}
        </el-avatar>
        <div class="user-info">
          <div class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</div>
          <div class="user-role-tag" :class="userStore.isAdmin ? 'admin' : 'normal'">
            {{ userStore.isAdmin ? '管理员' : '普通用户' }}
          </div>
        </div>
        <el-tooltip content="退出登录" placement="top">
          <button class="user-logout-btn" @click="handleLogout">
            <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
              <path fill-rule="evenodd" d="M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z" clip-rule="evenodd"/>
            </svg>
          </button>
        </el-tooltip>
      </div>

      <!-- Collapse btn -->
      <button class="collapse-btn" @click="isCollapsed = !isCollapsed">
        <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14"
          :style="{ transform: isCollapsed ? 'rotate(180deg)' : 'none', transition: 'transform 0.25s' }">
          <path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd"/>
        </svg>
      </button>
    </aside>

    <!-- ===== Main Content ===== -->
    <main class="main-content">

      <!-- Top Header -->
      <header class="top-header">
        <div class="header-left">
          <div class="header-page-title">
            <span class="header-title-s" v-if="currentMeta.showS">§</span>
            {{ currentMeta.label }}
          </div>
          <div class="header-breadcrumb">
            <span>AdmissionRAG</span>
            <svg viewBox="0 0 20 20" fill="currentColor" width="12" height="12"><path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/></svg>
            <span class="header-breadcrumb-current">{{ currentMeta.label }}</span>
          </div>
        </div>

        <div class="header-right">
          <!-- Search hint -->
          <div class="header-search-hint" v-if="currentMeta.searchHint">
            <svg viewBox="0 0 20 20" fill="currentColor" width="13" height="13"><path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/></svg>
            {{ currentMeta.searchHint }}
          </div>

          <!-- Notification bell -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
            <el-tooltip content="通知中心" placement="bottom">
              <button class="header-icon-btn" @click="router.push('/notification')">
                <svg viewBox="0 0 20 20" fill="currentColor" width="18" height="18">
                  <path d="M10 2a6 6 0 00-6 6v3.586l-.707.707A1 1 0 004 14h12a1 1 0 00.707-1.707L16 11.586V8a6 6 0 00-6-6zM10 18a3 3 0 01-2.83-2h5.66A3 3 0 0110 18z"/>
                </svg>
              </button>
            </el-tooltip>
          </el-badge>

          <!-- User dropdown -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="header-user">
              <el-avatar :size="30" :src="userStore.userInfo?.avatar" class="header-avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) }}
              </el-avatar>
              <span class="header-username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14" style="color:#94a3b8">
                <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd"/>
              </svg>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- Page content -->
      <div class="page-body">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { notificationApi } from '@/api/notification'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapsed = ref(false)
const unreadCount = ref(0)

const loadUnreadCount = async () => {
  try {
    unreadCount.value = await notificationApi.getUnreadCount()
  } catch {}
}

onMounted(loadUnreadCount)

const mainMenu = [
  { path: '/cases',     label: '典型案例', icon: 'Memo' },
  { path: '/chat',      label: '智能问答', icon: 'ChatDotRound' },
  { path: '/favorites', label: '我的收藏', icon: 'Star' },
  { path: '/profile',   label: '个人中心', icon: 'UserFilled' },
]

const adminMenu = [
  { path: '/knowledge',  label: '知识库管理',  icon: 'Reading' },
  { path: '/dashboard',  label: '数据大屏',    icon: 'DataAnalysis' },
  { path: '/users',      label: '用户管理',    icon: 'User' },
  { path: '/ai-config',  label: 'AI 配置中心', icon: 'SetUp' },
  { path: '/templates',  label: '问题模板',    icon: 'List' },
  { path: '/audit-log',  label: '审计日志',    icon: 'Document' },
  { path: '/notification', label: '通知管理',  icon: 'Bell' },
]

const allMenu = [...mainMenu, ...adminMenu]

const routeMeta: Record<string, { label: string; showS?: boolean; searchHint?: string }> = {
  '/cases':        { label: '典型案例',    showS: true,  searchHint: '搜索案例关键词' },
  '/chat':         { label: '智能问答',    showS: false },
  '/favorites':    { label: '我的收藏',    showS: false },
  '/profile':      { label: '个人中心',    showS: false },
  '/knowledge':    { label: '知识库管理',  showS: false },
  '/dashboard':    { label: '数据大屏',    showS: false },
  '/users':        { label: '用户管理',    showS: false },
  '/ai-config':    { label: 'AI 配置中心', showS: false },
  '/templates':    { label: '问题模板',    showS: false },
  '/audit-log':    { label: '审计日志',    showS: false },
  '/notification': { label: '通知中心',    showS: false },
}

const isActive = (path: string) => route.path.startsWith(path)

const currentMeta = computed(() => {
  const key = Object.keys(routeMeta).find(k => route.path.startsWith(k))
  return key ? routeMeta[key] : { label: 'AdmissionRAG', showS: false }
})

const handleCommand = async (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    handleLogout()
  }
}

const handleLogout = async () => {
  await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
/* ===== Layout ===== */
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--bg-color, #f5f3ef);
}

/* ===== Sidebar ===== */
.sidebar {
  width: var(--sidebar-width, 220px);
  background: var(--navy, #0c1322);
  display: flex;
  flex-direction: column;
  transition: width 0.25s ease;
  position: relative;
  flex-shrink: 0;
  border-right: 1px solid rgba(184,148,63,0.12);
}
.sidebar.collapsed { width: 64px; }

/* Logo */
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 16px 16px;
  border-bottom: 1px solid rgba(184,148,63,0.1);
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  flex-shrink: 0;
}
.logo-scales-wrap {
  width: 36px; height: 36px;
  background: rgba(184,148,63,0.1);
  border: 1px solid rgba(184,148,63,0.2);
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  transition: background 0.2s;
}
.sidebar-logo:hover .logo-scales-wrap {
  background: rgba(184,148,63,0.18);
}
.logo-text {
  font-family: Palatino, "Palatino Linotype", Georgia, serif;
  font-size: 19px;
  font-style: italic;
  color: rgba(255,255,255,0.92);
  letter-spacing: 0.5px;
  white-space: nowrap;
}

/* Nav */
.sidebar-nav {
  flex: 1;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
  overflow-x: hidden;
}
.sidebar-nav::-webkit-scrollbar { width: 0; }

.nav-section-label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1.5px;
  color: rgba(255,255,255,0.2);
  text-transform: uppercase;
  padding: 8px 12px 4px;
  white-space: nowrap;
}

.nav-divider {
  height: 1px;
  background: rgba(184,148,63,0.1);
  margin: 8px 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 8px;
  color: rgba(255,255,255,0.45);
  text-decoration: none;
  transition: var(--transition, all 0.22s ease);
  white-space: nowrap;
  overflow: hidden;
  position: relative;
}
.nav-item:hover {
  color: rgba(255,255,255,0.8);
  background: rgba(255,255,255,0.05);
}
.nav-item.active {
  color: #fff;
  background: rgba(184,148,63,0.15);
  border-left: 2px solid var(--gold, #b8943f);
  padding-left: 10px;
}
.nav-item.active .el-icon {
  color: var(--gold, #b8943f);
}

.nav-label {
  font-size: 13px;
  font-weight: 500;
  flex: 1;
}
.nav-badge {
  font-size: 10px;
  font-weight: 700;
  background: var(--gold, #b8943f);
  color: var(--navy, #0c1322);
  padding: 1px 6px;
  border-radius: 8px;
  flex-shrink: 0;
}

/* Sidebar user */
.sidebar-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  border-top: 1px solid rgba(184,148,63,0.1);
  overflow: hidden;
  flex-shrink: 0;
}
.user-avatar-el {
  flex-shrink: 0;
  background: rgba(184,148,63,0.2) !important;
  color: var(--gold, #b8943f) !important;
  font-weight: 700 !important;
  border: 1px solid rgba(184,148,63,0.25) !important;
}
.user-info { flex: 1; min-width: 0; }
.user-name {
  font-size: 13px;
  font-weight: 600;
  color: rgba(255,255,255,0.8);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.user-role-tag {
  display: inline-block;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 8px;
  margin-top: 2px;
  letter-spacing: 0.3px;
}
.user-role-tag.admin  { background: rgba(220,38,38,0.2);  color: #fca5a5; }
.user-role-tag.normal { background: rgba(184,148,63,0.15); color: var(--gold, #b8943f); }

.user-logout-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: rgba(255,255,255,0.2);
  padding: 4px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  transition: color 0.2s, background 0.2s;
  flex-shrink: 0;
}
.user-logout-btn:hover {
  color: #ef4444;
  background: rgba(239,68,68,0.1);
}

/* Collapse btn */
.collapse-btn {
  position: absolute;
  bottom: 72px;
  right: -11px;
  width: 22px;
  height: 22px;
  background: var(--navy, #0c1322);
  border: 1px solid rgba(184,148,63,0.25);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: rgba(255,255,255,0.3);
  transition: var(--transition);
  z-index: 10;
}
.collapse-btn:hover {
  color: var(--gold, #b8943f);
  border-color: var(--gold, #b8943f);
}

/* ===== Main Content ===== */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--bg-color, #f5f3ef);
  min-width: 0;
}

/* Header */
.top-header {
  height: var(--header-height, 60px);
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
  border-bottom: 1px solid #ede8de;
  position: relative;
}
.top-header::after {
  content: '';
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--gold, #b8943f) 0%, transparent 40%);
  opacity: 0.4;
}

.header-left { display: flex; flex-direction: column; gap: 2px; }

.header-page-title {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 15px;
  font-weight: 700;
  color: #1a1a2e;
  letter-spacing: 0.3px;
}
.header-title-s {
  font-family: Palatino, "Palatino Linotype", Georgia, serif;
  font-size: 18px;
  color: var(--gold, #b8943f);
  line-height: 1;
}

.header-breadcrumb {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #94a3b8;
}
.header-breadcrumb-current { color: var(--gold, #b8943f); font-weight: 500; }

.header-right { display: flex; align-items: center; gap: 16px; }

.header-search-hint {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #94a3b8;
  background: #faf9f6;
  border: 1px solid #ede8de;
  padding: 5px 12px;
  border-radius: 16px;
}

.header-icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #64748b;
  padding: 6px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s, background 0.2s;
}
.header-icon-btn:hover { color: #b8943f; background: #faf9f6; }

.header-user {
  display: flex;
  align-items: center;
  gap: 7px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 8px;
  transition: background 0.2s;
}
.header-user:hover { background: #faf9f6; }

.header-avatar {
  background: rgba(184,148,63,0.15) !important;
  color: var(--gold, #b8943f) !important;
  font-weight: 700 !important;
}

.header-username {
  font-size: 13px;
  color: #334155;
  font-weight: 500;
}

/* Page body */
.page-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}
.page-body::-webkit-scrollbar { width: 6px; }
.page-body::-webkit-scrollbar-thumb { background: #d4c9b4; border-radius: 3px; }
</style>
