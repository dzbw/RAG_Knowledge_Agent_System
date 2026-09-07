import request from '../utils/request'

/** 问答/流式生成：向量检索 + Ollama 推理可能较慢（本地大模型常超过 2 分钟） */
const CHAT_ASK_TIMEOUT_MS = 600000

export function ask(data) {
  return request.post('/api/chat/ask', data, { timeout: CHAT_ASK_TIMEOUT_MS })
}

export function listSessions() {
  return request.get('/api/chat/sessions')
}

export function listMessages(sessionId) {
  return request.get(`/api/chat/sessions/${sessionId}/messages`)
}

export function deleteSession(sessionId) {
  return request.delete(`/api/chat/sessions/${sessionId}`)
}
