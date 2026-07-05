import { computed, onMounted, reactive, ref, watch } from 'vue'
import { getGrades, submitGradesTask } from '@/api'
import { GRADES_CONFIG, STORAGE_KEYS } from '@/config'
import { useSharedWallpaper } from '@/composables/useSharedWallpaper'

const now = new Date()
const currentYear = now.getMonth() >= 7 ? now.getFullYear() : now.getFullYear() - 1

const gradesText = GRADES_CONFIG.TEXT
const gradesViewMode = GRADES_CONFIG.VIEW_MODE
const gradesSortMode = GRADES_CONFIG.SORT_MODE
const gradesUi = GRADES_CONFIG.UI

const buildAcademicYearOptions = () => {
  const options = []
  for (let year = currentYear + GRADES_CONFIG.YEAR_RANGE_AFTER; year >= currentYear - GRADES_CONFIG.YEAR_RANGE_BEFORE; year -= 1) {
    options.push({
      label: `${year}-${year + 1}`,
      value: String(year)
    })
  }
  return options
}

const getValidOptionValue = (options, candidateValue, fallbackValue) => {
  return options.some(item => item.value === candidateValue) ? candidateValue : fallbackValue
}

const academicYearOptions = buildAcademicYearOptions()
const semesterOptions = GRADES_CONFIG.SEMESTER_OPTIONS
const viewOptions = GRADES_CONFIG.VIEW_OPTIONS
const sortOptions = GRADES_CONFIG.SORT_OPTIONS
const tableColumns = GRADES_CONFIG.TABLE_COLUMNS
const cardMetaFields = GRADES_CONFIG.CARD_META_FIELDS

const buildDefaultFilters = () => ({
  academicYear: academicYearOptions[0]?.value || String(currentYear),
  semester: semesterOptions[GRADES_CONFIG.DEFAULT_SEMESTER_INDEX]?.value || '12'
})

const loadSavedState = () => {
  const fallbackFilters = buildDefaultFilters()
  const fallbackViewMode = viewOptions[0]?.value || gradesViewMode.CARD
  const fallbackSortMode = sortOptions[0]?.value || gradesSortMode.DEFAULT

  try {
    const raw = localStorage.getItem(STORAGE_KEYS.GRADES_FILTERS)
    if (!raw) {
      return {
        filters: fallbackFilters,
        viewMode: fallbackViewMode,
        sortMode: fallbackSortMode
      }
    }

    const parsed = JSON.parse(raw)
    return {
      filters: {
        academicYear: getValidOptionValue(academicYearOptions, parsed.academicYear, fallbackFilters.academicYear),
        semester: getValidOptionValue(semesterOptions, parsed.semester, fallbackFilters.semester)
      },
      viewMode: getValidOptionValue(viewOptions, parsed.viewMode, fallbackViewMode),
      sortMode: getValidOptionValue(sortOptions, parsed.sortMode, fallbackSortMode)
    }
  } catch {
    return {
      filters: fallbackFilters,
      viewMode: fallbackViewMode,
      sortMode: fallbackSortMode
    }
  }
}

const normalizeScoreValue = (value) => {
  const numericValue = Number.parseFloat(value)
  return Number.isFinite(numericValue) ? numericValue : null
}

export const useGradesPage = () => {
  const savedState = loadSavedState()
  const filters = reactive(savedState.filters)
  const loading = ref(false)
  const submitting = ref(false)
  const grades = ref([])
  const viewMode = ref(savedState.viewMode)
  const sortMode = ref(savedState.sortMode)
  const {
    wallpaperRootStyle,
    wallpaperBackdropStyle,
    fetchWallpaper
  } = useSharedWallpaper()

  const saveState = () => {
    localStorage.setItem(
      STORAGE_KEYS.GRADES_FILTERS,
      JSON.stringify({
        academicYear: filters.academicYear,
        semester: filters.semester,
        viewMode: viewMode.value,
        sortMode: sortMode.value
      })
    )
  }

  const gradesRootStyle = computed(() => ({
    ...wallpaperRootStyle.value,
    '--grades-shell-max-width': `${gradesUi.SHELL_MAX_WIDTH}px`,
    '--grades-panel-radius': `${gradesUi.PANEL_RADIUS}px`,
    '--grades-control-radius': `${gradesUi.CONTROL_RADIUS}px`,
    '--grades-score-radius': `${gradesUi.SCORE_RADIUS}px`,
    '--grades-table-min-width': `${gradesUi.TABLE_MIN_WIDTH}px`,
    '--grades-panel-bg': gradesUi.PANEL_BG,
    '--grades-panel-border': gradesUi.PANEL_BORDER,
    '--grades-panel-shadow': gradesUi.PANEL_SHADOW,
    '--grades-panel-blur': `${gradesUi.PANEL_BLUR}px`,
    '--grades-primary': gradesUi.PRIMARY_COLOR,
    '--grades-primary-surface': gradesUi.PRIMARY_SURFACE,
    '--grades-primary-text': gradesUi.PRIMARY_TEXT,
    '--grades-secondary-surface': gradesUi.SECONDARY_SURFACE,
    '--grades-secondary-text': gradesUi.SECONDARY_TEXT,
    '--grades-text-main': gradesUi.TEXT_MAIN,
    '--grades-text-sub': gradesUi.TEXT_SUB,
    '--grades-text-muted': gradesUi.TEXT_MUTED,
    '--grades-text-table': gradesUi.TEXT_TABLE,
    '--grades-table-head-bg': gradesUi.TABLE_HEAD_BG,
    '--grades-table-row-bg': gradesUi.TABLE_ROW_BG,
    '--grades-table-row-hover-bg': gradesUi.TABLE_ROW_HOVER_BG,
    '--grades-meta-item-bg': gradesUi.META_ITEM_BG,
    '--grades-meta-item-border': gradesUi.META_ITEM_BORDER,
    '--grades-score-pill-bg': gradesUi.SCORE_PILL_BG,
    '--grades-score-label-color': gradesUi.SCORE_LABEL_COLOR
  }))

  const gradesBackdropStyle = computed(() => ({
    ...wallpaperBackdropStyle.value
  }))

  const currentSummary = computed(() => {
    const selectedYear = academicYearOptions.find(item => item.value === filters.academicYear)
    const selectedSemester = semesterOptions.find(item => item.value === filters.semester)
    return `${selectedYear?.label || filters.academicYear} · 学期 ${filters.semester}${selectedSemester?.description ? `（${selectedSemester.description}）` : ''}`
  })

  const displayGrades = computed(() => {
    const list = Array.isArray(grades.value) ? [...grades.value] : []
    if (sortMode.value !== gradesSortMode.SCORE_DESC) {
      return list
    }

    return list.sort((left, right) => {
      const rightScore = normalizeScoreValue(right.score)
      const leftScore = normalizeScoreValue(left.score)
      if (rightScore === null && leftScore === null) return 0
      if (rightScore === null) return -1
      if (leftScore === null) return 1
      return rightScore - leftScore
    })
  })

  const fetchGradesData = async () => {
    loading.value = true
    saveState()
    try {
      const res = await getGrades({
        academicYear: filters.academicYear,
        semester: filters.semester
      })
      grades.value = Array.isArray(res?.data) ? res.data : []
    } catch (error) {
      console.error('获取成绩失败:', error)
      grades.value = []
    } finally {
      loading.value = false
    }
  }

  const submitTask = async () => {
    submitting.value = true
    saveState()
    try {
      await submitGradesTask({
        academicYear: filters.academicYear,
        semester: filters.semester
      })
      window.alert(GRADES_CONFIG.SUBMITTING_TEXT)
    } catch (error) {
      console.error('提交成绩查询任务失败:', error)
    } finally {
      submitting.value = false
    }
  }

  const formatGradeValue = (value) => {
    if (value === null || value === undefined || value === '') {
      return '-'
    }
    return value
  }

  const buildGradeKey = (item) => [
    item.courseCode,
    item.courseName,
    item.academicYear,
    item.semester,
    item.teacher
  ].join('-')

  watch(
    () => [filters.academicYear, filters.semester, viewMode.value, sortMode.value],
    () => {
      saveState()
    }
  )

  onMounted(() => {
    fetchWallpaper()
    fetchGradesData()
  })

  return {
    gradesText,
    gradesViewMode,
    gradesSortMode,
    academicYearOptions,
    semesterOptions,
    viewOptions,
    sortOptions,
    tableColumns,
    cardMetaFields,
    filters,
    loading,
    submitting,
    viewMode,
    sortMode,
    gradesRootStyle,
    gradesBackdropStyle,
    currentSummary,
    displayGrades,
    fetchGradesData,
    submitTask,
    formatGradeValue,
    buildGradeKey
  }
}
