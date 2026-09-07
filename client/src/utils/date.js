/**
 * 格式化为 YYYY-MM-DD HH:mm:ss
 */
export function formatDateTime(val) {
  if (!val) return ''
  const d = typeof val === 'string' ? new Date(val.replace(' ', 'T')) : val
  if (Number.isNaN(d.getTime())) return String(val)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

/**
 * 格式化为 YYYY-MM-DD
 */
export function formatDate(val) {
  if (!val) return ''
  const d = typeof val === 'string' ? new Date(val.replace(' ', 'T')) : val
  if (Number.isNaN(d.getTime())) return String(val).slice(0, 10)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}
