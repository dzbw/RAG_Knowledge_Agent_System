<script setup>
import { ref, watch, onMounted, computed, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { Plus, Delete, ChatDotRound } from '@element-plus/icons-vue'
import ChatMessage from '../../components/ChatMessage.vue'
import { listCategories } from '../../api/category'
import { listSessions, listMessages, ask, deleteSession } from '../../api/chat'
import { formatDateTime } from '../../utils/date'
import { useUserStore } from '../../stores/user'
import { fileUrl } from '../../utils/files'
import { avatarFallbackBg } from '../../utils/avatarFallback'

const route = useRoute()
const userStore = useUserStore()

const chatUserAvatarSrc = computed(() =>
  userStore.user?.avatar ? fileUrl(userStore.user.avatar) : '',
)
const chatUserAvatarText = computed(() =>
  (userStore.user?.realName || userStore.user?.username || '?').slice(0, 1),
)
const chatUserAvatarStyle = computed(() =>
  userStore.user?.avatar ? undefined : avatarFallbackBg(userStore.user?.username),
)

const msgsEl = ref(null)

async function scrollMsgsToBottom() {
  await nextTick()
  const el = msgsEl.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}
const sessions = ref([])
const messages = ref([])
const activeId = ref(null)
const categoryIds = ref([])
const categories = ref([])
const input = ref('')
const sending = ref(false)

function parseRefs(refs) {
  if (!refs) return []
  if (typeof refs === 'string') {
    try {
      return JSON.parse(refs)
    } catch {
      return []
    }
  }
  return Array.isArray(refs) ? refs : []
}

async function loadSessions() {
  const res = await listSessions()
  sessions.value = res.data
}

async function selectSession(id) {
  activeId.value = id
  const res = await listMessages(id)
  messages.value = res.data
  scrollMsgsToBottom()
}

async function newChat() {
  activeId.value = null
  messages.value = []
}

async function send() {
  const q = input.value.trim()
  if (!q || sending.value) return
  sending.value = true
  const sid = activeId.value
  messages.value = [...messages.value, { role: 'USER', content: q, refs: null }]
  input.value = ''
  scrollMsgsToBottom()
  try {
    const ids = categoryIds.value.map((id) => Number(id)).filter((n) => !Number.isNaN(n))
    const res = await ask({
      question: q,
      sessionId: sid,
      categoryIds: ids.length ? ids : null,
    })
    activeId.value = res.data.sessionId
    const ans = res.data.answer
    const refs = res.data.references || []
    messages.value = [
      ...messages.value,
      { role: 'ASSISTANT', content: ans, refs: refs },
    ]
    await loadSessions()
    scrollMsgsToBottom()
  } finally {
    sending.value = false
    scrollMsgsToBottom()
  }
}

watch(
  () => messages.value.length,
  () => scrollMsgsToBottom(),
)
watch(sending, (v) => {
  if (v) scrollMsgsToBottom()
})

async function removeSession(id, ev) {
  ev.stopPropagation()
  await deleteSession(id)
  if (activeId.value === id) newChat()
  loadSessions()
}

onMounted(async () => {
  const c = await listCategories()
  categories.value = c.data
  await loadSessions()
  const qid = route.query.sessionId
  if (qid) {
    selectSession(Number(qid))
  }
})

watch(
  () => route.query.sessionId,
  (v) => {
    if (v) selectSession(Number(v))
  }
)
</script>

<template>
  <div class="chat-wrap">
    <el-card class="card" shadow="hover">
      <div class="layout">
        <aside class="side">
          <div class="side-head">
            <el-button type="primary" :icon="Plus" round size="small" @click="newChat">新对话</el-button>
          </div>
          <div class="sess-list">
            <div
              v-for="s in sessions"
              :key="s.id"
              class="sess"
              :class="{ active: activeId === s.id }"
              @click="selectSession(s.id)"
            >
              <div class="t">{{ s.title || '未命名' }}</div>
              <div class="d">{{ formatDateTime(s.updateTime) }}</div>
              <el-button class="del" text type="danger" :icon="Delete" @click="removeSession(s.id, $event)" />
            </div>
          </div>
        </aside>
        <main class="main">
          <div class="filters">
            <el-select
              v-model="categoryIds"
              multiple
              clearable
              collapse-tags
              placeholder="限定分类（可选）"
              style="width: min(360px, 100%)"
            >
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </div>
          <div ref="msgsEl" class="msgs">
            <div v-if="!messages.length && !sending" class="empty">
              向企业知识库提问，答案将基于已上传文档生成。
            </div>
            <ChatMessage
              v-for="(m, idx) in messages"
              :key="idx"
              :role="m.role"
              :content="m.content"
              :refs="parseRefs(m.refs)"
              :user-avatar-src="chatUserAvatarSrc"
              :user-avatar-text="chatUserAvatarText"
              :user-avatar-style="chatUserAvatarStyle"
            />
            <div v-if="sending" class="pending-row" aria-live="polite">
              <div class="pending-sys-icon">
                <el-icon class="pending-sys-ic"><ChatDotRound /></el-icon>
              </div>
              <div class="pending-bubble">
                <span class="pending-label">系统正在检索中，请稍等</span>
                <span class="pending-dots" aria-hidden="true"><span>.</span><span>.</span><span>.</span></span>
              </div>
            </div>
          </div>
          <div class="input-bar">
            <el-input
              v-model="input"
              type="textarea"
              :rows="3"
              placeholder="输入问题，Enter 发送（Shift+Enter 换行）"
              @keydown.enter.exact.prevent="send"
            />
            <el-button type="primary" class="send" :loading="sending" @click="send">发送</el-button>
          </div>
        </main>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.chat-wrap {
  width: 100%;
}
.card {
  border-radius: 20px;
  border: none;
  overflow: hidden;
}
.layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  min-height: calc(100vh - 140px);
}
.side {
  background: linear-gradient(180deg, #0f172a, #1e293b);
  color: #e2e8f0;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
}
.side-head {
  margin-bottom: 10px;
}
.sess-list {
  overflow-y: auto;
  flex: 1;
}
.sess {
  position: relative;
  padding: 10px 30px 10px 10px;
  border-radius: 12px;
  cursor: pointer;
  margin-bottom: 6px;
}
.sess:hover {
  background: rgba(255, 255, 255, 0.06);
}
.sess.active {
  background: rgba(99, 102, 241, 0.35);
}
.sess .t {
  font-size: 13px;
  font-weight: 600;
}
.sess .d {
  font-size: 11px;
  opacity: 0.65;
  margin-top: 4px;
}
.sess .del {
  position: absolute;
  right: 4px;
  top: 8px;
}
.main {
  display: flex;
  flex-direction: column;
  background: #f8fafc;
}
.filters {
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
  background: #fff;
}
.msgs {
  flex: 1;
  overflow-y: auto;
  padding: 16px 18px 100px;
}
.empty {
  text-align: center;
  color: #94a3b8;
  padding: 48px 16px;
  font-size: 14px;
}
.pending-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 14px;
}
.pending-sys-icon {
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(145deg, #6366f1, #7c3aed);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
  margin-top: 2px;
  animation: pending-pulse 1.6s ease-in-out infinite;
}
.pending-sys-ic {
  font-size: 22px;
}
.pending-bubble {
  max-width: min(420px, 88%);
  padding: 12px 16px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid #e2e8f0;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.06);
  font-size: 14px;
  color: #475569;
  display: flex;
  align-items: center;
  gap: 4px;
}
.pending-label {
  font-weight: 500;
}
.pending-dots span {
  animation: dot-fade 1.2s ease-in-out infinite;
}
.pending-dots span:nth-child(2) {
  animation-delay: 0.2s;
}
.pending-dots span:nth-child(3) {
  animation-delay: 0.4s;
}
@keyframes pending-pulse {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.92;
    transform: scale(0.97);
  }
}
@keyframes dot-fade {
  0%,
  100% {
    opacity: 0.25;
  }
  50% {
    opacity: 1;
  }
}
.input-bar {
  display: flex;
  gap: 12px;
  padding: 12px 16px 16px;
  background: #fff;
  border-top: 1px solid #e2e8f0;
  align-items: flex-end;
}
.send {
  height: 44px;
}
</style>
