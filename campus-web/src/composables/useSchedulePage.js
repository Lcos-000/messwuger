import { computed, onMounted, onUnmounted, ref } from 'vue'
import { getSchedule, getUserStatus, refreshSchedule } from '@/api'
import { HTTP_STATUS, POLLING_CONFIG, SCHEDULE_CONFIG, SYNC_STATUS } from '@/config'
import { useSharedWallpaper } from '@/composables/useSharedWallpaper'
import {
  buildCourseGroups,
  buildTeacherListLabel,
  getCourseColor,
  getCourseColorPair,
  getCurrentWeek,
  getTodayColumn
} from '@/utils/schedule'

const text = SCHEDULE_CONFIG.TEXT

export const useSchedulePage = () => {
  const currentWeek = ref(0)
  const loading = ref(true)
  const isSyncing = ref(false)
  const rawScheduleData = ref([])
  const detailCourse = ref(null)
  const stackGroup = ref(null)
  const rowHeight = ref(SCHEDULE_CONFIG.DESKTOP_ROW_HEIGHT)
  const isMobile = ref(false)
  const {
    wallpaperRootStyle,
    wallpaperBackdropStyle,
    fetchWallpaper
  } = useSharedWallpaper()

  let statusTimer = null

  const days = SCHEDULE_CONFIG.DAYS
  const periodTime = SCHEDULE_CONFIG.PERIOD_TIME
  const totalPeriods = SCHEDULE_CONFIG.TOTAL_PERIODS

  const scheduleRootStyle = computed(() => ({
    ...wallpaperRootStyle.value,
    '--schedule-row-height': `${rowHeight.value}px`
  }))

  const scheduleWallpaperStyle = computed(() => ({
    ...wallpaperBackdropStyle.value
  }))

  const currentWeekLabel = computed(() => {
    if (currentWeek.value === 0) {
      return text.FULL_TERM_LABEL
    }
    return text.CURRENT_WEEK_LABEL.replace('{week}', currentWeek.value)
  })

  const courseGroups = computed(() => {
    return buildCourseGroups(rawScheduleData.value, currentWeek.value)
  })

  const emptyText = computed(() => {
    return currentWeek.value === 0 ? text.EMPTY_TERM_TEXT : text.EMPTY_WEEK_TEXT
  })

  const updateLayout = () => {
    const width = window.innerWidth
    isMobile.value = width < SCHEDULE_CONFIG.MOBILE_BREAKPOINT
    rowHeight.value = isMobile.value
      ? SCHEDULE_CONFIG.MOBILE_ROW_HEIGHT
      : SCHEDULE_CONFIG.DESKTOP_ROW_HEIGHT
  }

  const stopStatusPolling = () => {
    if (statusTimer) {
      clearInterval(statusTimer)
      statusTimer = null
    }
  }

  const showToast = (message) => {
    window.alert(message)
  }

  const setCurrentWeekToNow = () => {
    currentWeek.value = getCurrentWeek()
  }

  const showFullTerm = () => {
    currentWeek.value = 0
  }

  const fetchScheduleData = async () => {
    loading.value = true
    try {
      const res = await getSchedule()
      const scheduleJson = res.data?.scheduleJson
      if (!scheduleJson || SCHEDULE_CONFIG.EMPTY_SCHEDULE_VALUES.includes(scheduleJson)) {
        isSyncing.value = true
        startStatusPolling()
        return
      }

      try {
        const parsed = JSON.parse(scheduleJson)
        rawScheduleData.value = Array.isArray(parsed) ? parsed : (parsed.courses || [])
        isSyncing.value = false
        stopStatusPolling()
      } catch (error) {
        console.error('课表 JSON 解析失败:', error)
        isSyncing.value = true
        startStatusPolling()
      }
    } catch (error) {
      console.error('获取课表失败:', error)
      isSyncing.value = true
      startStatusPolling()
    } finally {
      loading.value = false
    }
  }

  const checkSyncStatus = async () => {
    try {
      const res = await getUserStatus()
      if (res.code === HTTP_STATUS.SUCCESS && res.data) {
        if (res.data.syncStatus === SYNC_STATUS.SUCCESS) {
          stopStatusPolling()
          isSyncing.value = false
          fetchScheduleData()
        } else if (res.data.syncStatus === SYNC_STATUS.FAILED) {
          stopStatusPolling()
          isSyncing.value = false
          showToast('课表同步失败，请稍后再试')
        }
      }
    } catch (error) {
      console.error('查询同步状态失败:', error)
    }
  }

  const startStatusPolling = () => {
    if (statusTimer) return
    isSyncing.value = true
    statusTimer = window.setInterval(checkSyncStatus, POLLING_CONFIG.SCHEDULE_STATUS_INTERVAL)
  }

  const handleManualRefresh = async () => {
    try {
      await refreshSchedule()
      showToast('已触发刷新，正在拉取最新课表')
      startStatusPolling()
    } catch (error) {
      console.error('手动刷新课表失败:', error)
    }
  }

  const changeWeek = (delta) => {
    let nextWeek = currentWeek.value + delta
    if (currentWeek.value === 0 && delta > 0) nextWeek = 1
    else if (currentWeek.value === 0 && delta < 0) nextWeek = SCHEDULE_CONFIG.MAX_WEEK
    if (nextWeek > SCHEDULE_CONFIG.MAX_WEEK) nextWeek = 0
    if (nextWeek < 0) nextWeek = SCHEDULE_CONFIG.MAX_WEEK
    currentWeek.value = nextWeek
  }

  const isTodayColumn = (dayIndex) => {
    return getTodayColumn() === dayIndex
  }

  const getSlotStyle = (group) => {
    const top = (group.start - 1) * rowHeight.value
    const span = group.end - group.start + 1
    return {
      top: `${top}px`,
      left: `${(group.dayOfWeek - 1) * (100 / 7)}%`,
      width: `${100 / 7}%`,
      height: `${span * rowHeight.value}px`
    }
  }

  const getCardStyle = (course, total, index) => {
    const [accent, background] = getCourseColorPair(course.courseName)
    const width = total > 1 ? `calc(${100 / total}% - 2px)` : '100%'
    const left = total > 1 ? `calc(${(100 / total) * index}% + 1px)` : '0'
    return {
      backgroundColor: background,
      borderLeft: `3px solid ${accent}`,
      position: 'absolute',
      top: '0',
      left,
      width,
      height: '100%'
    }
  }

  const openDetail = (course) => {
    detailCourse.value = course
  }

  const closeDetail = () => {
    detailCourse.value = null
  }

  const openStackDetail = (group) => {
    stackGroup.value = group
  }

  const closeStackDetail = () => {
    stackGroup.value = null
  }

  const selectStackCourse = (course) => {
    detailCourse.value = course
    closeStackDetail()
  }

  const buildTeacherNames = (teachers) => {
    return buildTeacherListLabel(teachers)
  }

  onMounted(() => {
    updateLayout()
    window.addEventListener('resize', updateLayout)
    fetchWallpaper()
    fetchScheduleData()
  })

  onUnmounted(() => {
    stopStatusPolling()
    window.removeEventListener('resize', updateLayout)
  })

  return {
    text,
    days,
    periodTime,
    totalPeriods,
    currentWeek,
    currentWeekLabel,
    loading,
    isSyncing,
    courseGroups,
    detailCourse,
    stackGroup,
    isMobile,
    scheduleRootStyle,
    scheduleWallpaperStyle,
    emptyText,
    changeWeek,
    setCurrentWeekToNow,
    showFullTerm,
    handleManualRefresh,
    isTodayColumn,
    getSlotStyle,
    getCardStyle,
    getCourseColor,
    getCourseColorPair,
    openDetail,
    closeDetail,
    openStackDetail,
    closeStackDetail,
    selectStackCourse,
    buildTeacherNames
  }
}
