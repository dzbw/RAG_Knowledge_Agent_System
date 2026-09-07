<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Odometer,
  User,
  FolderOpened,
  Document,
  ChatLineRound,
  SwitchButton,
  Postcard,
  ArrowDown,
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { fileUrl } from '../utils/files'
import { avatarFallbackBg } from '../utils/avatarFallback'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const active = computed(() => route.path)
const avatarSrc = computed(() => fileUrl(store.user?.avatar))
const avatarText = computed(() => (store.user?.realName || store.user?.username || '?').slice(0, 1))
const headerAvStyle = computed(() =>
  store.user?.avatar ? undefined : avatarFallbackBg(store.user?.username),
)

async function safeLogout() {
  try {
    await ElMessageBox.confirm(
      '确定要安全退出登录吗？退出后将清除本地登录状态。',
      '安全退出',
      {
        confirmButtonText: '退出',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
  } catch {
    return
  }
  store.logout()
  router.push('/login')
}

function onHeaderCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/admin/profile')
    return
  }
  if (cmd === 'logout') {
    safeLogout()
  }
}
</script>

<template>
  <el-container class="admin-shell">
    <el-aside width="232px" class="aside">
      <div class="brand">
        <span class="dot" />
        <div>
          <div class="t1">Java1234 RAG</div>
          <div class="t2">管理后台</div>
        </div>
      </div>
      <el-menu
        :default-active="active"
        class="menu"
        background-color="#0f172a"
        text-color="#94a3b8"
        active-text-color="#fff"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><FolderOpened /></el-icon>
          <span>知识分类</span>
        </el-menu-item>
        <el-menu-item index="/admin/documents">
          <el-icon><Document /></el-icon>
          <span>文档管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/chat-test">
          <el-icon><ChatLineRound /></el-icon>
          <span>问答测试</span>
        </el-menu-item>
        <el-menu-item index="/admin/profile">
          <el-icon><Postcard /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
      <div class="aside-foot">
        <el-button text type="primary" @click="safeLogout">
          <el-icon><SwitchButton /></el-icon>
          安全退出
        </el-button>
      </div>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="crumb">{{ route.meta?.title || '控制台' }}</div>
        <el-dropdown class="who-dd" trigger="click" @command="onHeaderCommand">
          <span class="who who-trigger">
            <el-avatar
              :size="32"
              :src="store.user?.avatar ? avatarSrc : undefined"
              class="hdr-av hdr-av-text"
              :style="headerAvStyle"
            >
              <span class="hdr-av-t">{{ avatarText }}</span>
            </el-avatar>
            <span class="who-name">{{ store.user?.realName || store.user?.username }}</span>
            <el-icon class="who-caret"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <span class="dd-item"><el-icon><Postcard /></el-icon> 个人中心</span>
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <span class="dd-item dd-exit"><el-icon><SwitchButton /></el-icon> 安全退出</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-shell {
  min-height: 100vh;
}
.aside {
  background: #0f172a;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(148, 163, 184, 0.12);
}
.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 22px 18px;
  color: #fff;
}
.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #34d399, #6366f1);
  box-shadow: 0 0 12px #6366f1;
}
.t1 {
  font-weight: 800;
  font-size: 14px;
}
.t2 {
  font-size: 11px;
  opacity: 0.65;
}
.menu {
  border-right: none;
  flex: 1;
}
.menu :deep(.el-menu-item) {
  border-radius: 10px;
  margin: 4px 10px;
}
.aside-foot {
  padding: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.1);
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.06);
  height: 56px !important;
}
.crumb {
  font-weight: 600;
  color: #334155;
}
.who-dd {
  line-height: 1;
}
.who-trigger {
  cursor: pointer;
  outline: none;
}
.who-caret {
  font-size: 12px;
  color: #94a3b8;
}
.dd-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.dd-exit {
  color: #c2410c;
}
.who {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #475569;
}
.who-name {
  font-weight: 600;
}
.hdr-av {
  flex-shrink: 0;
  border: 2px solid rgba(255, 255, 255, 0.65);
  font-family: ui-sans-serif, system-ui, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.hdr-av-text :deep(img) {
  object-fit: cover;
}
.hdr-av-t {
  font-size: 14px;
  font-weight: 800;
  color: #fff;
  line-height: 1;
  letter-spacing: 0.03em;
  text-shadow: 0 1px 2px rgba(15, 23, 42, 0.2);
}
</style>
