/**
 * 根据种子字符串生成稳定的渐变背景（用于无图时的文字头像）。
 */
const PAIRS = [
  ['#5b4fd8', '#9d6af0'],
  ['#0d9488', '#0ea5e9'],
  ['#c026d3', '#7c3aed'],
  ['#ea580c', '#eab308'],
  ['#047857', '#14b8a6'],
  ['#2563eb', '#06b6d4'],
  ['#be185d', '#f97316'],
  ['#4338ca', '#6366f1'],
]

export function avatarFallbackBg(seed) {
  const s = String(seed || '?')
  let h = 0
  for (let i = 0; i < s.length; i += 1) {
    h = s.charCodeAt(i) + ((h << 5) - h)
  }
  const idx = Math.abs(h) % PAIRS.length
  const [a, b] = PAIRS[idx]
  return {
    background: `linear-gradient(148deg, ${a} 0%, ${b} 100%)`,
    boxShadow: '0 2px 12px rgba(15, 23, 42, 0.18)',
  }
}
