/**
 * 将后端返回的相对路径转为可访问的 URL（依赖 Vite 代理 /files）。
 */
export function fileUrl(relative) {
  if (relative == null || relative === '') return ''
  const path = String(relative).replace(/^\/+/, '')
  return `/files/${path}`
}
