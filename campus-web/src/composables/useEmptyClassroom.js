import { computed, onMounted, reactive, ref, watch } from 'vue'
import { getEmptyClassroomResult, submitEmptyClassroomTask } from '@/api'
import {
  EMPTY_CLASSROOM_CONFIG,
  STORAGE_KEYS
} from '@/config'
import { useSharedWallpaper } from '@/composables/useSharedWallpaper'
import {
  buildAcademicYearOptions,
  encodeMask,
  formatSequentialLabel,
  getBuildingOptions,
  getCurrentWeek,
  getDefaultBuildingValue,
  getPeriodOptions,
  getValidOptionValue,
  getWeekOptions,
  toggleNumberSelection
} from '@/utils/emptyClassroom'

const text = EMPTY_CLASSROOM_CONFIG.TEXT
const ui = EMPTY_CLASSROOM_CONFIG.UI
const semesterOptions = EMPTY_CLASSROOM_CONFIG.SEMESTER_OPTIONS
const dayOptions = EMPTY_CLASSROOM_CONFIG.DAY_OPTIONS
const campusOptions = EMPTY_CLASSROOM_CONFIG.CAMPUS_OPTIONS
const roomTypeOptions = EMPTY_CLASSROOM_CONFIG.ROOM_TYPE_OPTIONS
const weekOptions = getWeekOptions()
const periodOptions = getPeriodOptions()
const tableColumns = EMPTY_CLASSROOM_CONFIG.TABLE_COLUMNS
const academicYearOptions = buildAcademicYearOptions()

const STATUS_ICONS = {
  success: '✓',
  primary: '↗',
  info: '…',
  warn: '!',
  danger: '×',
  default: '·'
}

const buildDefaultFilters = () => ({
  academicYear: academicYearOptions[0]?.value || String(new Date().getFullYear()),
  semester: semesterOptions[EMPTY_CLASSROOM_CONFIG.DEFAULT_SEMESTER_INDEX]?.value || '12',
  dayOfWeek: '1',
  campusId: '2',
  building: getDefaultBuildingValue('2'),
  roomType: '',
  selectedWeeks: [getCurrentWeek()],
  selectedPeriods: []
})

const resolveBuildingValue = (campusId, building, fallbackValue) => {
  const validBuilding = getValidOptionValue(getBuildingOptions(campusId), building, '')
  return validBuilding || fallbackValue
}

const loadSavedFilters = () => {
  const fallback = buildDefaultFilters()
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.EMPTY_CLASSROOM_FILTERS)
    if (!raw) return fallback

    const parsed = JSON.parse(raw)
    const campusId = getValidOptionValue(campusOptions, parsed.campusId, fallback.campusId)
    const defaultBuilding = getDefaultBuildingValue(campusId)
    return {
      academicYear: getValidOptionValue(academicYearOptions, parsed.academicYear, fallback.academicYear),
      semester: getValidOptionValue(semesterOptions, parsed.semester, fallback.semester),
      dayOfWeek: getValidOptionValue(dayOptions, parsed.dayOfWeek, fallback.dayOfWeek),
      campusId,
      building: resolveBuildingValue(campusId, parsed.building, defaultBuilding),
      roomType: getValidOptionValue(roomTypeOptions, parsed.roomType, fallback.roomType),
      selectedWeeks: Array.isArray(parsed.selectedWeeks) && parsed.selectedWeeks.length
        ? parsed.selectedWeeks.map(Number)
        : fallback.selectedWeeks,
      selectedPeriods: Array.isArray(parsed.selectedPeriods)
        ? parsed.selectedPeriods.map(Number)
        : fallback.selectedPeriods
    }
  } catch {
    return fallback
  }
}

const buildSubmitPayload = (filters, selectedWeeks, selectedPeriods) => ({
  academicYear: filters.academicYear,
  semester: filters.semester,
  dayOfWeek: filters.dayOfWeek,
  periodsMask: encodeMask(selectedPeriods),
  weeksMask: encodeMask(selectedWeeks),
  campusId: filters.campusId,
  building: filters.building,
  roomType: filters.roomType
})

export const useEmptyClassroom = () => {
  const initialFilters = loadSavedFilters()
  const filters = reactive({
    academicYear: initialFilters.academicYear,
    semester: initialFilters.semester,
    dayOfWeek: initialFilters.dayOfWeek,
    campusId: initialFilters.campusId,
    building: initialFilters.building,
    roomType: initialFilters.roomType
  })
  const selectedWeeks = ref(initialFilters.selectedWeeks)
  const selectedPeriods = ref(initialFilters.selectedPeriods)
  const submitting = ref(false)
  const resultLoading = ref(false)
  const taskStatus = ref('')
  const resultRows = ref([])
  const {
    wallpaperRootStyle,
    wallpaperBackdropStyle,
    fetchWallpaper
  } = useSharedWallpaper()

  const buildingOptions = computed(() => getBuildingOptions(filters.campusId))
  const canSubmit = computed(() => {
    return selectedWeeks.value.length > 0 && selectedPeriods.value.length > 0 && Boolean(filters.dayOfWeek)
  })

  const pageStyle = computed(() => ({
    ...wallpaperRootStyle.value,
    '--empty-shell-max-width': `${ui.SHELL_MAX_WIDTH}px`,
    '--empty-panel-radius': `${ui.PANEL_RADIUS}px`,
    '--empty-control-radius': `${ui.CONTROL_RADIUS}px`,
    '--empty-table-min-width': `${ui.TABLE_MIN_WIDTH}px`,
    '--empty-panel-bg': ui.PANEL_BG,
    '--empty-panel-border': ui.PANEL_BORDER,
    '--empty-panel-shadow': ui.PANEL_SHADOW,
    '--empty-panel-blur': `${ui.PANEL_BLUR}px`,
    '--empty-primary': ui.PRIMARY_COLOR,
    '--empty-primary-surface': ui.PRIMARY_SURFACE,
    '--empty-primary-text': ui.PRIMARY_TEXT,
    '--empty-secondary-surface': ui.SECONDARY_SURFACE,
    '--empty-secondary-text': ui.SECONDARY_TEXT,
    '--empty-text-main': ui.TEXT_MAIN,
    '--empty-text-sub': ui.TEXT_SUB,
    '--empty-text-muted': ui.TEXT_MUTED,
    '--empty-chip-bg': ui.CHIP_BG,
    '--empty-chip-border': ui.CHIP_BORDER,
    '--empty-preview-bg': ui.PREVIEW_BG,
    '--empty-preview-border': ui.PREVIEW_BORDER,
    '--empty-table-head-bg': ui.TABLE_HEAD_BG,
    '--empty-table-row-bg': ui.TABLE_ROW_BG,
    '--empty-table-text': ui.TABLE_TEXT
  }))

  const backdropStyle = computed(() => ({
    ...wallpaperBackdropStyle.value
  }))

  const selectedWeekLabel = computed(() => formatSequentialLabel(selectedWeeks.value, '周'))
  const selectedPeriodLabel = computed(() => formatSequentialLabel(selectedPeriods.value, '节'))
  const selectedCampusLabel = computed(() => {
    return campusOptions.find(option => option.value === filters.campusId)?.label || '未选择'
  })
  const selectedBuildingLabel = computed(() => {
    return buildingOptions.value.find(option => option.value === filters.building)?.label || '未选择'
  })
  const selectedSemesterLabel = computed(() => {
    return semesterOptions.find(option => option.value === filters.semester)?.description || ''
  })
  const selectedAcademicYearLabel = computed(() => {
    return academicYearOptions.find(option => option.value === filters.academicYear)?.label || filters.academicYear
  })
  const selectedDayLabel = computed(() => {
    return dayOptions.find(option => option.value === filters.dayOfWeek)?.label || '未选择'
  })

  const summaryText = computed(() => {
    return `${selectedAcademicYearLabel.value} · 学期 ${filters.semester}${selectedSemesterLabel.value ? `（${selectedSemesterLabel.value}）` : ''} · ${selectedDayLabel.value} · ${selectedCampusLabel.value} / ${selectedBuildingLabel.value}`
  })

  const statusMeta = computed(() => {
    if (!taskStatus.value) {
      return {
        title: text.EMPTY_STATUS,
        description: text.EMPTY_STATUS_DESC,
        tone: 'default'
      }
    }
    return EMPTY_CLASSROOM_CONFIG.STATUS_META[taskStatus.value] || {
      title: taskStatus.value,
      description: text.EMPTY_STATUS_DESC,
      tone: 'default'
    }
  })

  const statusToneClass = computed(() => `status-panel--${statusMeta.value.tone}`)
  const statusIcon = computed(() => STATUS_ICONS[statusMeta.value.tone] || STATUS_ICONS.default)

  const emptyResultText = computed(() => {
    if (taskStatus.value === 'QUERYING' || taskStatus.value === 'SUBMITTED') {
      return text.PENDING_RESULT_TEXT
    }
    return EMPTY_CLASSROOM_CONFIG.RESULT_EMPTY_TEXT
  })

  const saveFilters = () => {
    localStorage.setItem(
      STORAGE_KEYS.EMPTY_CLASSROOM_FILTERS,
      JSON.stringify({
        ...filters,
        selectedWeeks: selectedWeeks.value,
        selectedPeriods: selectedPeriods.value
      })
    )
  }

  const submitTask = async () => {
    if (!canSubmit.value) {
      return
    }
    submitting.value = true
    saveFilters()
    try {
      const res = await submitEmptyClassroomTask(buildSubmitPayload(filters, selectedWeeks.value, selectedPeriods.value))
      taskStatus.value = res?.data?.queryStatus || 'SUBMITTED'
      if (taskStatus.value === 'RESULT_READY') {
        await fetchResult()
      }
    } catch (error) {
      console.error('提交空教室任务失败:', error)
      taskStatus.value = 'FAILED'
    } finally {
      submitting.value = false
    }
  }

  const fetchResult = async () => {
    if (!canSubmit.value) {
      return
    }
    resultLoading.value = true
    saveFilters()
    try {
      const res = await getEmptyClassroomResult(buildSubmitPayload(filters, selectedWeeks.value, selectedPeriods.value))
      taskStatus.value = res?.data?.queryStatus || taskStatus.value || 'FAILED'
      resultRows.value = Array.isArray(res?.data?.classrooms) ? res.data.classrooms : []
    } catch (error) {
      console.error('查询空教室结果失败:', error)
      taskStatus.value = 'FAILED'
      resultRows.value = []
    } finally {
      resultLoading.value = false
    }
  }

  const resetFilters = () => {
    const fallback = buildDefaultFilters()
    filters.academicYear = fallback.academicYear
    filters.semester = fallback.semester
    filters.dayOfWeek = fallback.dayOfWeek
    filters.campusId = fallback.campusId
    filters.building = fallback.building
    filters.roomType = fallback.roomType
    selectedWeeks.value = fallback.selectedWeeks
    selectedPeriods.value = fallback.selectedPeriods
    taskStatus.value = ''
    resultRows.value = []
    saveFilters()
  }

  const toggleWeek = (value) => {
    selectedWeeks.value = toggleNumberSelection(selectedWeeks.value, value)
  }

  const togglePeriod = (value) => {
    selectedPeriods.value = toggleNumberSelection(selectedPeriods.value, value)
  }

  const selectCurrentWeek = () => {
    selectedWeeks.value = [getCurrentWeek()]
  }

  const formatValue = (value) => {
    if (value === null || value === undefined || value === '') {
      return '-'
    }
    return value
  }

  const buildRowKey = (item) => {
    return [item.building, item.roomCode, item.roomName, item.campus, item.floor].join('-')
  }

  watch(
    () => filters.campusId,
    () => {
      const defaultBuilding = getDefaultBuildingValue(filters.campusId)
      filters.building = resolveBuildingValue(filters.campusId, filters.building, defaultBuilding)
    }
  )

  watch(
    () => [
      filters.academicYear,
      filters.semester,
      filters.dayOfWeek,
      filters.campusId,
      filters.building,
      filters.roomType,
      selectedWeeks.value.join(','),
      selectedPeriods.value.join(',')
    ],
    () => {
      saveFilters()
    }
  )

  onMounted(() => {
    fetchWallpaper()
  })

  return {
    text,
    academicYearOptions,
    semesterOptions,
    dayOptions,
    campusOptions,
    roomTypeOptions,
    weekOptions,
    periodOptions,
    tableColumns,
    filters,
    selectedWeeks,
    selectedPeriods,
    submitting,
    resultLoading,
    resultRows,
    buildingOptions,
    canSubmit,
    pageStyle,
    backdropStyle,
    selectedWeekLabel,
    selectedPeriodLabel,
    summaryText,
    statusMeta,
    statusToneClass,
    statusIcon,
    emptyResultText,
    submitTask,
    fetchResult,
    resetFilters,
    toggleWeek,
    togglePeriod,
    selectCurrentWeek,
    formatValue,
    buildRowKey
  }
}
