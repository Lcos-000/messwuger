import { EMPTY_CLASSROOM_CONFIG, SCHEDULE_CONFIG } from '@/config'

const now = new Date()
const currentYear = now.getMonth() >= 7 ? now.getFullYear() : now.getFullYear() - 1

export const buildAcademicYearOptions = () => {
  return [currentYear, currentYear + 1].map(year => ({
    label: `${year}-${year + 1}`,
    value: String(year)
  }))
}

export const getCurrentWeek = () => {
  const semesterStart = new Date(SCHEDULE_CONFIG.SEMESTER_START_DATE)
  const diff = Math.floor((new Date() - semesterStart) / (7 * 24 * 60 * 60 * 1000))
  return Math.max(1, Math.min(diff + 1, EMPTY_CLASSROOM_CONFIG.MAX_WEEK))
}

export const getWeekOptions = () => {
  return Array.from({ length: EMPTY_CLASSROOM_CONFIG.MAX_WEEK }, (_, index) => ({
    label: `第 ${index + 1} 周`,
    value: index + 1
  }))
}

export const getPeriodOptions = () => {
  return Array.from({ length: EMPTY_CLASSROOM_CONFIG.MAX_PERIOD }, (_, index) => ({
    label: `第 ${index + 1} 节`,
    value: index + 1
  }))
}

export const encodeMask = (values) => {
  const uniqueSorted = [...new Set(values.map(Number).filter(Number.isFinite))].sort((left, right) => left - right)
  const mask = uniqueSorted.reduce((total, value) => total + Math.pow(2, value - 1), 0)
  return String(mask)
}

export const toggleNumberSelection = (source, value) => {
  const numericValue = Number(value)
  const current = Array.isArray(source) ? [...source] : []
  const exists = current.includes(numericValue)
  const next = exists
    ? current.filter(item => item !== numericValue)
    : [...current, numericValue]
  return next.sort((left, right) => left - right)
}

export const formatSequentialLabel = (values, unit) => {
  if (!values.length) {
    return '未选择'
  }
  const sorted = [...values].sort((left, right) => left - right)
  const ranges = []
  let start = sorted[0]
  let end = sorted[0]

  for (let index = 1; index < sorted.length; index += 1) {
    const value = sorted[index]
    if (value === end + 1) {
      end = value
      continue
    }
    ranges.push(start === end ? `${start}${unit}` : `${start}-${end}${unit}`)
    start = value
    end = value
  }

  ranges.push(start === end ? `${start}${unit}` : `${start}-${end}${unit}`)
  return ranges.join('、')
}

export const getValidOptionValue = (options, candidateValue, fallbackValue) => {
  return options.some(option => option.value === candidateValue) ? candidateValue : fallbackValue
}

export const getBuildingOptions = (campusId) => {
  return EMPTY_CLASSROOM_CONFIG.BUILDING_OPTIONS[campusId] || []
}

export const getDefaultBuildingValue = (campusId) => {
  return getBuildingOptions(campusId)[0]?.value || ''
}
