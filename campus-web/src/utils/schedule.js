import { SCHEDULE_CONFIG } from '@/config'

export const getCurrentWeek = () => {
  const semesterStart = new Date(SCHEDULE_CONFIG.SEMESTER_START_DATE)
  const now = new Date()
  const diff = Math.floor((now - semesterStart) / (7 * 24 * 60 * 60 * 1000))
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

  const normalized = String(weeks)
    .replace(/周/g, '')
    .replace(/[（(](单|双)[）)]/g, '')
    .trim()

  const result = []
  normalized.split(/[，,、]/).forEach(part => {
    const value = part.trim()
    if (!value) return

    if (value.includes('-')) {
      const [start, end] = value.split('-').map(Number)
      if (Number.isFinite(start) && Number.isFinite(end) && end >= start) {
        for (let current = start; current <= end; current += 1) {
          result.push(current)
        }
      }
      return
    }

    const single = Number(value)
    if (Number.isFinite(single) && single > 0) {
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
