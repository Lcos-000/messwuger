import { SCHEDULE_CONFIG } from '@/config'

const DAY_MS = 24 * 60 * 60 * 1000

const buildLocalDate = (year, rule) => {
  return new Date(year, rule.month - 1, rule.day, 0, 0, 0, 0)
}

const toLocalDateStart = (date) => {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0, 0)
}

export const getActiveSemesterStartDate = (referenceDate = new Date()) => {
  const currentDate = toLocalDateStart(referenceDate)
  const currentYear = currentDate.getFullYear()
  const { SPRING, AUTUMN } = SCHEDULE_CONFIG.SEMESTER_START_RULES
  const springStart = buildLocalDate(currentYear, SPRING)
  const autumnStart = buildLocalDate(currentYear, AUTUMN)

  if (currentDate >= autumnStart) return autumnStart
  if (currentDate >= springStart) return springStart
  return buildLocalDate(currentYear - 1, AUTUMN)
}

export const getCurrentWeek = () => {
  const now = toLocalDateStart(new Date())
  const semesterStart = getActiveSemesterStartDate(now)
  const diff = Math.floor((now - semesterStart) / (7 * DAY_MS))
  return Math.max(1, Math.min(diff + 1, SCHEDULE_CONFIG.MAX_WEEK))
}

export const getTodayColumn = () => {
  const day = new Date().getDay()
  return day === 0 ? 7 : day
}

export const parseWeeksList = (weeks) => {
  if (!weeks) return []
  if (Array.isArray(weeks)) {
    return weeks.map(Number).filter(Number.isFinite)
  }

  const result = []
  String(weeks).split(/[，,、]/).forEach(part => {
    const normalized = part.replace(/周/g, '').trim()
    const parityMatch = normalized.match(/[（(]\s*(单|双)\s*[）)]\s*$/)
    const parity = parityMatch?.[1] === '单' ? 1 : parityMatch?.[1] === '双' ? 0 : null
    const value = parityMatch ? normalized.slice(0, parityMatch.index).trim() : normalized
    if (!value) return

    const rangeMatch = value.match(/^(\d+)\s*-\s*(\d+)$/)
    if (rangeMatch) {
      const start = Number(rangeMatch[1])
      const end = Number(rangeMatch[2])
      if (Number.isFinite(start) && Number.isFinite(end) && end >= start) {
        for (let week = start; week <= end; week += 1) {
          if (parity === null || week % 2 === parity) {
            result.push(week)
          }
        }
      }
      return
    }

    const single = Number(value)
    if (Number.isFinite(single) && single > 0 && (parity === null || single % 2 === parity)) {
      result.push(single)
    }
  })

  return [...new Set(result)].sort((left, right) => left - right)
}

export const parsePeriodRange = (periodStr) => {
  if (!periodStr) return { start: 0, end: 0 }
  const rangeMatch = String(periodStr).match(/(\d+)\D+(\d+)/)
  if (rangeMatch) {
    return {
      start: Number.parseInt(rangeMatch[1], 10),
      end: Number.parseInt(rangeMatch[2], 10)
    }
  }

  const singleMatch = String(periodStr).match(/(\d+)/)
  if (singleMatch) {
    const value = Number.parseInt(singleMatch[1], 10)
    return { start: value, end: value }
  }

  return { start: 0, end: 0 }
}

export const getCourseColorPair = (name) => {
  if (!name) return SCHEDULE_CONFIG.COURSE_COLORS[0]

  let hash = 0
  for (let index = 0; index < name.length; index += 1) {
    hash = (hash << 5) - hash + name.charCodeAt(index)
    hash |= 0
  }

  return SCHEDULE_CONFIG.COURSE_COLORS[Math.abs(hash) % SCHEDULE_CONFIG.COURSE_COLORS.length]
}

export const getCourseColor = (name) => {
  return getCourseColorPair(name)[0] || SCHEDULE_CONFIG.DEFAULT_COURSE_COLOR
}

export const buildTeacherListLabel = (teachers = []) => {
  return teachers.map(item => item.name).join('、')
}

export const buildCourseGroups = (courses, currentWeek) => {
  const source = Array.isArray(courses) ? courses : []
  const validCourses = source.filter(item => item.dayOfWeek && item.dayOfWeek > 0)
  const filteredCourses = currentWeek === 0
    ? validCourses
    : validCourses.filter(item => parseWeeksList(item.weeks).includes(currentWeek))

  const groupsMap = {}

  filteredCourses.forEach(course => {
    const { start, end } = parsePeriodRange(course.periods)
    if (!start) return

    const key = `${course.dayOfWeek}-${start}-${end}`
    if (!groupsMap[key]) {
      groupsMap[key] = {
        dayOfWeek: course.dayOfWeek,
        start,
        end,
        courses: []
      }
    }

    const existingCourse = groupsMap[key].courses.find(item => {
      return item.courseName === course.courseName && item.classroom === course.classroom && item.campus === course.campus
    })

    if (existingCourse) {
      existingCourse.teachers.push({
        name: course.teacher,
        weeks: course.weeks
      })
      return
    }

    groupsMap[key].courses.push({
      ...course,
      teachers: [{
        name: course.teacher,
        weeks: course.weeks
      }]
    })
  })

  return Object.values(groupsMap)
}
