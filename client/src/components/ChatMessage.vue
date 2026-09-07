<script setup>
import { computed, ref } from 'vue'
import { marked } from 'marked'
import { ArrowDown, ChatDotRound, Document } from '@element-plus/icons-vue'

marked.setOptions({ breaks: true })

const props = defineProps({
  role: String,
  content: String,
  refs: { type: Array, default: () => [] },
  /** 用户消息右侧头像 */
  userAvatarSrc: { type: String, default: '' },
  userAvatarText: { type: String, default: '?' },
  userAvatarStyle: { type: Object, default: null },
})

const html = computed(() => marked.parse(props.content || ''))

/** 助手引用默认收起 */
const refsOpen = ref(false)

function toggleRefs() {
  refsOpen.value = !refsOpen.value
}

const refsCount = computed(() => props.refs?.length ?? 0)
</script>

<template>
  <div class="row" :class="role === 'USER' ? 'user' : 'bot'">
    <template v-if="role === 'USER'">
      <div class="bubble">
        <div class="body" v-html="html" />
        <div v-if="refs?.length" class="refs">
          <div class="r-title">引用片段</div>
          <div v-for="(r, i) in refs" :key="i" class="ref-item">
            <b>{{ r.title || '文档' }}</b>
            <span class="sub">#{{ r.docId }}</span>
            <p>{{ r.snippet }}</p>
          </div>
        </div>
      </div>
      <el-avatar
        :size="38"
        class="side-av side-av-user"
        :src="userAvatarSrc || undefined"
        :style="userAvatarStyle || undefined"
      >
        <span class="side-av-letter">{{ userAvatarText }}</span>
      </el-avatar>
    </template>
    <template v-else>
      <div class="sys-icon-wrap" title="知识库助手">
        <el-icon class="sys-icon"><ChatDotRound /></el-icon>
      </div>
      <div class="bubble">
        <div class="body" v-html="html" />
        <div v-if="refs?.length" class="refs-bot">
          <button
            type="button"
            class="refs-trigger"
            :aria-expanded="refsOpen"
            @click="toggleRefs"
          >
            <el-icon class="refs-trigger-doc"><Document /></el-icon>
            <span class="refs-trigger-text">{{ refsOpen ? '收起引用' : '查看引用' }}</span>
            <span class="refs-trigger-badge">{{ refsCount }}</span>
            <el-icon class="refs-trigger-chevron" :class="{ 'is-open': refsOpen }">
              <ArrowDown />
            </el-icon>
          </button>
          <transition name="refs-fold">
            <div v-show="refsOpen" class="refs-panel">
              <div class="refs-panel-head">以下为检索到的文档片段，供核对。</div>
              <div v-for="(r, i) in refs" :key="i" class="ref-item">
                <div class="ref-item-head">
                  <b>{{ r.title || '文档' }}</b>
                  <span class="sub">#{{ r.docId }}</span>
                </div>
                <p class="ref-snippet">{{ r.snippet }}</p>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 14px;
}
.row.user {
  justify-content: flex-end;
}
.row.bot {
  justify-content: flex-start;
}
.sys-icon-wrap {
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(145deg, #6366f1, #7c3aed);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.38);
  margin-top: 2px;
}
.sys-icon {
  font-size: 22px;
}
.side-av {
  flex-shrink: 0;
  margin-top: 2px;
  border: 2px solid rgba(255, 255, 255, 0.65);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.12);
}
.side-av-user :deep(img) {
  object-fit: cover;
}
.side-av-letter {
  font-size: 15px;
  font-weight: 800;
  color: #fff;
  line-height: 1;
  letter-spacing: 0.02em;
  text-shadow: 0 1px 2px rgba(15, 23, 42, 0.2);
}
.bubble {
  max-width: min(680px, calc(92% - 48px));
  border-radius: 18px;
  padding: 12px 16px;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.08);
}
.user .bubble {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
}
.bot .bubble {
  background: #fff;
  border: 1px solid #e2e8f0;
}
.body :deep(h1),
.body :deep(h2),
.body :deep(p),
.body :deep(li) {
  margin: 0.4em 0;
}
.user .body :deep(a) {
  color: #e0e7ff;
}
.refs {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed rgba(148, 163, 184, 0.5);
  font-size: 13px;
}
.user .refs {
  border-top-color: rgba(255, 255, 255, 0.35);
}
.r-title {
  font-weight: 700;
  margin-bottom: 6px;
}
.ref-item {
  margin-bottom: 8px;
}
.sub {
  margin-left: 8px;
  opacity: 0.75;
  font-size: 12px;
}
.ref-item p {
  margin: 4px 0 0;
  opacity: 0.9;
}

/* —— 助手引用：默认折叠 —— */
.refs-bot {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed rgba(148, 163, 184, 0.55);
}
.refs-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  background: linear-gradient(180deg, #fafbff, #f1f5f9);
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition:
    border-color 0.18s ease,
    background 0.18s ease,
    box-shadow 0.18s ease,
    color 0.18s ease;
}
.refs-trigger:hover {
  border-color: #c7d2fe;
  background: linear-gradient(180deg, #eef2ff, #e0e7ff);
  color: #4338ca;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.15);
}
.refs-trigger:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.35);
}
.refs-trigger-doc {
  font-size: 16px;
  opacity: 0.85;
}
.refs-trigger-badge {
  min-width: 22px;
  height: 22px;
  padding: 0 7px;
  border-radius: 999px;
  background: rgba(99, 102, 241, 0.12);
  color: #5b21b6;
  font-size: 12px;
  font-weight: 700;
  line-height: 22px;
  text-align: center;
}
.refs-trigger:hover .refs-trigger-badge {
  background: rgba(99, 102, 241, 0.22);
}
.refs-trigger-chevron {
  font-size: 14px;
  margin-left: 2px;
  transition: transform 0.22s ease;
  opacity: 0.75;
}
.refs-trigger-chevron.is-open {
  transform: rotate(180deg);
}
.refs-panel {
  margin-top: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: linear-gradient(165deg, rgba(248, 250, 252, 0.95), rgba(239, 246, 255, 0.65));
  border: 1px solid rgba(226, 232, 240, 0.95);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}
.refs-panel-head {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 12px;
  line-height: 1.45;
}
.bot .refs-panel .ref-item {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.85);
}
.bot .refs-panel .ref-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}
.bot .refs-panel .ref-item-head {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 6px 10px;
}
.bot .refs-panel .sub {
  margin-left: 0;
}
.bot .refs-panel .ref-snippet {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.55;
  color: #334155;
  white-space: pre-wrap;
}
.refs-fold-enter-active,
.refs-fold-leave-active {
  transition:
    opacity 0.22s ease,
    transform 0.22s ease;
}
.refs-fold-enter-from,
.refs-fold-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
