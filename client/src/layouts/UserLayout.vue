<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Clock, User, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { fileUrl } from '../utils/files'
import { avatarFallbackBg } from '../utils/avatarFallback'

const router = useRouter()
const store = useUserStore()
const avatarSrc = computed(() => fileUrl(store.user?.avatar))
const avatarText = computed(() => (store.user?.realName || store.user?.username || '?').slice(0, 1))
const headerAvStyle = computed(() =>
  store.user?.avatar ? undefined : avatarFallbackBg(store.user?.username),
)

function go(p) {
  router.push(p)
}

function logout() {
  store.logout()
  router.push('/login')
}
</script>

<template>
  <el-container direction="vertical" class="user-shell">
    <el-header class="top">
      <div class="logo-line" @click="go('/user/chat')">
        <span class="badge">RAG</span>
        <span class="ttl">Java1234 企业知识库</span>
      </div>
      <el-menu mode="horizontal" class="nav" :ellipsis="false" background-color="transparent">
        <el-menu-item index="1" @click="go('/user/chat')">
          <el-icon><ChatDotRound /></el-icon>
          知识问答
        </el-menu-item>
        <el-menu-item index="2" @click="go('/user/history')">
          <el-icon><Clock /></el-icon>
          会话历史
        </el-menu-item>
        <el-menu-item index="3" @click="go('/user/profile')">
          <el-icon><User /></el-icon>
          个人中心
        </el-menu-item>
      </el-menu>
      <div class="right">
        <el-avatar
          :size="36"
          :src="store.user?.avatar ? avatarSrc : undefined"
          class="hdr-av hdr-av-text"
          :style="headerAvStyle"
        >
          <span class="hdr-av-t">{{ avatarText }}</span>
        </el-avatar>
        <span class="name">{{ store.user?.realName || store.user?.username }}</span>
        <el-button type="danger" link @click="logout">
          <el-icon><SwitchButton /></el-icon>
          退出
        </el-button>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.user-shell {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #e0e7ff 100%);
}
.top {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(99, 102, 241, 0.12);
  height: 58px !important;
}
.logo-line {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  margin-right: 24px;
}
.badge {
  background: linear-gradient(135deg, #6366f1, #a855f7);
  color: #fff;
  font-size: 11px;
  font-weight: 800;
  padding: 4px 8px;
  border-radius: 8px;
}
.ttl {
  font-weight: 800;
  color: #1e293b;
  font-size: 15px;
}
.nav {
  flex: 1;
  border-bottom: none !important;
}
.nav :deep(.el-menu-item) {
  font-weight: 600;
}
.right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.name {
  color: #475569;
  font-size: 14px;
}
.hdr-av {
  flex-shrink: 0;
  border: 2px solid rgba(255, 255, 255, 0.55);
  font-family: ui-sans-serif, system-ui, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.hdr-av-text :deep(img) {
  object-fit: cover;
}
.hdr-av-t {
  font-size: 15px;
  font-weight: 800;
  color: #fff;
  line-height: 1;
  letter-spacing: 0.03em;
  text-shadow: 0 1px 2px rgba(15, 23, 42, 0.2);
}
.main {
  padding: 20px 24px 32px;
  max-width: 1180px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}
</style>
