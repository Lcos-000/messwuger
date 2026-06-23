/**
 * Course schedule utilities
 */

// Generate deterministic color from course name
const PALETTE = [
  '#4f86f7','#f97316','#10b981','#8b5cf6',
  '#ec4899','#06b6d4','#f59e0b','#ef4444',
  '#14b8a6','#a855f7','#6366f1','#84cc16'
]

export function courseColor(name) {
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = (hash << 5) - hash + name.charCodeAt(i)
    hash |= 0
  }
  return PALETTE[Math.abs(hash) % PALETTE.length]
}

/**
 * Parse "periods" string like "1-3节" -> [1, 2, 3]
 */
export function parsePeriods(periodsStr) {
  if (!periodsStr) return []
  const m = periodsStr.match(/(\d+)-(\d+)节/)
  if (m) {
    const start = parseInt(m[1]), end = parseInt(m[2])
    return Array.from({ length: end - start + 1 }, (_, i) => start + i)
  }
  const single = periodsStr.match(/(\d+)节/)
  if (single) return [parseInt(single[1])]
  return []
}

/**
 * Parse weeks string like "1-12周", "4-6周(双)", "13周" -> { start, end, parity }
 * parity: 'all' | 'odd' | 'even'
 */
export function parseWeeks(weeksStr) {
  if (!weeksStr) return { start: 0, end: 0, parity: 'all' }
  let parity = 'all'
  if (weeksStr.includes('单')) parity = 'odd'
  if (weeksStr.includes('双')) parity = 'even'
  const m = weeksStr.match(/(\d+)-(\d+)周/)
  if (m) return { start: parseInt(m[1]), end: parseInt(m[2]), parity }
  const single = weeksStr.match(/(\d+)周/)
  if (single) return { start: parseInt(single[1]), end: parseInt(single[1]), parity }
  return { start: 0, end: 0, parity }
}

export function isActiveInWeek(course, week) {
  const { start, end, parity } = parseWeeks(course.weeks)
  if (week < start || week > end) return false
  if (parity === 'odd' && week % 2 === 0) return false
  if (parity === 'even' && week % 2 !== 0) return false
  return true
}

/**
 * All periods 1-14 (typical college schedule)
 */
export const ALL_PERIODS = Array.from({ length: 14 }, (_, i) => i + 1)

export const DAYS = ['一', '二', '三', '四', '五', '六', '日']
