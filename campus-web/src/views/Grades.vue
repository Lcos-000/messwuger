<template>
  <div class="grades-root" :style="gradesRootStyle">
    <div class="grades-backdrop" :style="gradesBackdropStyle"></div>
    <div class="grades-shell">
      <header class="grades-header">
        <div>
          <h1 class="grades-title">{{ gradesText.PAGE_TITLE }}</h1>
          <p class="grades-subtitle">{{ gradesText.PAGE_SUBTITLE }}</p>
        </div>
      </header>

      <section class="filter-panel">
        <div class="field-group">
          <label class="field-label">{{ gradesText.ACADEMIC_YEAR_LABEL }}</label>
          <select v-model="filters.academicYear" class="field-select">
            <option v-for="option in academicYearOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </div>

        <div class="field-group">
          <label class="field-label">{{ gradesText.SEMESTER_LABEL }}</label>
          <div class="segmented-control segmented-control--semester">
            <button
              v-for="option in semesterOptions"
              :key="option.value"
              type="button"
              class="segment-btn segment-btn--rich"
              :class="{ active: filters.semester === option.value }"
              @click="filters.semester = option.value"
            >
              <span class="segment-btn__main">学期 {{ option.label }}</span>
              <span class="segment-btn__sub">{{ option.description }}</span>
            </button>
          </div>
        </div>

        <div class="view-toolbar">
          <div class="field-group field-group--toolbar">
            <label class="field-label">{{ gradesText.VIEW_LABEL }}</label>
            <div class="segmented-control segmented-control--compact">
              <button
                v-for="option in viewOptions"
                :key="option.value"
                type="button"
                class="segment-btn"
                :class="{ active: viewMode === option.value }"
                @click="viewMode = option.value"
              >
                {{ option.label }}
              </button>
            </div>
          </div>

          <div class="field-group field-group--toolbar">
            <label class="field-label">{{ gradesText.SORT_LABEL }}</label>
            <div class="segmented-control segmented-control--compact">
              <button
                v-for="option in sortOptions"
                :key="option.value"
                type="button"
                class="segment-btn"
                :class="{ active: sortMode === option.value }"
                @click="sortMode = option.value"
              >
                {{ option.label }}
              </button>
            </div>
          </div>
        </div>

        <div class="action-row">
          <button type="button" class="primary-btn" :disabled="loading" @click="fetchGradesData">
            {{ loading ? '查询中...' : GRADES_CONFIG.QUERY_BUTTON_TEXT }}
          </button>
          <button type="button" class="secondary-btn" :disabled="submitting" @click="submitTask">
            {{ submitting ? '提交中...' : GRADES_CONFIG.SUBMIT_BUTTON_TEXT }}
          </button>
        </div>
      </section>

      <section class="summary-panel">
        <div class="summary-item">
          <span class="summary-label">{{ gradesText.CURRENT_FILTER_LABEL }}</span>
          <strong class="summary-value">{{ currentSummary }}</strong>
        </div>
        <div class="summary-item">
          <span class="summary-label">{{ gradesText.RECORD_COUNT_LABEL }}</span>
          <strong class="summary-value">{{ displayGrades.length }}</strong>
        </div>
      </section>

      <section v-if="loading" class="state-panel">
        <div class="loader"></div>
        <p class="state-text">{{ gradesText.LOADING }}</p>
      </section>

      <section v-else-if="displayGrades.length === 0" class="state-panel state-panel--empty">
        <p class="state-title">{{ gradesText.EMPTY_TITLE }}</p>
        <p class="state-text">{{ gradesText.EMPTY_DESCRIPTION }}</p>
      </section>

      <section v-else-if="viewMode === gradesViewMode.TABLE" class="grade-table-card">
        <div class="grade-table-wrap">
          <table class="grade-table">
            <thead>
              <tr>
                <th
                  v-for="column in tableColumns"
                  :key="column.key"
                  :class="column.className"
                >
                  {{ column.label }}
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in displayGrades" :key="buildGradeKey(item)">
                <td
                  v-for="column in tableColumns"
                  :key="`${buildGradeKey(item)}-${column.key}`"
                  :class="column.className"
                >
                  {{ formatGradeValue(item[column.key]) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-else class="grades-list">
        <article v-for="item in displayGrades" :key="buildGradeKey(item)" class="grade-card">
          <div class="grade-card__main">
            <div class="grade-card__header">
              <div class="grade-card__title-wrap">
                <h2 class="course-name">{{ item.courseName || '-' }}</h2>
                <p class="course-code">{{ item.courseCode || gradesText.COURSE_CODE_EMPTY }}</p>
              </div>
              <div class="score-pill">
                <span class="score-pill__label">{{ gradesText.SCORE_LABEL }}</span>
                <span class="score-pill__value">{{ formatGradeValue(item.score) }}</span>
              </div>
            </div>
            <div class="meta-grid">
              <div
                v-for="field in cardMetaFields"
                :key="field.key"
                class="meta-item"
              >
                <span class="meta-label">{{ field.label }}</span>
                <span class="meta-value">{{ formatGradeValue(item[field.key]) }}</span>
              </div>
            </div>
          </div>
        </article>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { getGrades, getProfileStyle, submitGradesTask } from '@/api/index'
import { GRADES_CONFIG, HTTP_STATUS, PROFILE_VIEW_CONFIG, STORAGE_KEYS } from '@/config'
import {
  clampWallpaperMaskValue,
  loadWallpaperMaskPreference,
  loadWallpaperPreference,
  resolveAssetUrl,
  saveWallpaperMaskPreference,
  saveWallpaperPreference
} from '@/utils/profileAssets'

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

const loadSavedFilters = () => {
  const fallback = buildDefaultFilters()
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.GRADES_FILTERS)
    if (!raw) return fallback
    const parsed = JSON.parse(raw)
    return {
      academicYear: getValidOptionValue(academicYearOptions, parsed.academicYear, fallback.academicYear),
      semester: getValidOptionValue(semesterOptions, parsed.semester, fallback.semester)
    }
  } catch {
    return fallback
  }
}

const filters = reactive(loadSavedFilters())
const loading = ref(false)
const submitting = ref(false)
const grades = ref([])
const viewMode = ref(viewOptions[0]?.value || gradesViewMode.CARD)
const sortMode = ref(sortOptions[0]?.value || gradesSortMode.DEFAULT)
const gradesWallpaper = ref('')
const wallpaperMask = ref(PROFILE_VIEW_CONFIG.WALLPAPER_MASK_DEFAULT)

const gradesRootStyle = computed(() => ({
  '--page-wallpaper-mask-alpha': wallpaperMask.value,
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
  backgroundImage: gradesWallpaper.value
    ? `linear-gradient(180deg, rgba(244, 247, 252, ${wallpaperMask.value}) 0%, rgba(236, 241, 249, ${wallpaperMask.value}) 100%), url(${gradesWallpaper.value})`
    : 'linear-gradient(180deg, #f6f8fc 0%, #edf2f9 100%)',
  backgroundSize: 'cover',
  backgroundPosition: 'center',
  backgroundRepeat: 'no-repeat'
}))

const currentSummary = computed(() => {
  const selectedYear = academicYearOptions.find(item => item.value === filters.academicYear)
  const selectedSemester = semesterOptions.find(item => item.value === filters.semester)
  return `${selectedYear?.label || filters.academicYear} · 学期 ${filters.semester}${selectedSemester?.description ? `（${selectedSemester.description}）` : ''}`
})

const normalizeScoreValue = (value) => {
  const numericValue = Number.parseFloat(value)
  return Number.isFinite(numericValue) ? numericValue : null
}

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

const saveFilters = () => {
  localStorage.setItem(
    STORAGE_KEYS.GRADES_FILTERS,
    JSON.stringify({
      academicYear: filters.academicYear,
      semester: filters.semester
    })
  )
}

const fetchGradesWallpaper = async () => {
  gradesWallpaper.value = loadWallpaperPreference(STORAGE_KEYS)
  wallpaperMask.value = loadWallpaperMaskPreference(STORAGE_KEYS, PROFILE_VIEW_CONFIG)
  try {
    const res = await getProfileStyle()
    if (res.code === HTTP_STATUS.SUCCESS && res.data) {
      gradesWallpaper.value = resolveAssetUrl(res.data.wallpaper)
      saveWallpaperPreference(STORAGE_KEYS, gradesWallpaper.value)
      if (res.data.wallpaperMask !== null && res.data.wallpaperMask !== undefined) {
        wallpaperMask.value = clampWallpaperMaskValue(res.data.wallpaperMask, PROFILE_VIEW_CONFIG)
        saveWallpaperMaskPreference(STORAGE_KEYS, wallpaperMask.value, PROFILE_VIEW_CONFIG)
      }
    }
  } catch (error) {
    console.error('获取成绩页墙纸失败:', error)
  }
}

const fetchGradesData = async () => {
  loading.value = true
  saveFilters()
  try {
    const res = await getGrades({
      academicYear: filters.academicYear,
      semester: filters.semester
    })
    grades.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error('获取成绩失败:', error)
    grades.value = []
  } finally {
    loading.value = false
  }
}

const submitTask = async () => {
  submitting.value = true
  saveFilters()
  try {
    await submitGradesTask({
      academicYear: filters.academicYear,
      semester: filters.semester
    })
    alert(GRADES_CONFIG.SUBMITTING_TEXT)
  } catch (error) {
    console.error('提交成绩任务失败:', error)
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
  () => [filters.academicYear, filters.semester],
  () => {
    saveFilters()
  }
)

onMounted(() => {
  fetchGradesWallpaper()
  fetchGradesData()
})
</script>

<style scoped>
.grades-root {
  position: relative;
  min-height: 100%;
  background: #eef2f9;
}

.grades-backdrop {
  position: fixed;
  inset: 0;
  background:
    radial-gradient(circle at top left, rgba(79, 134, 247, 0.16), transparent 28%),
    linear-gradient(180deg, #f6f8fc 0%, #edf2f9 100%);
  pointer-events: none;
}

.grades-shell {
  position: relative;
  z-index: 1;
  max-width: var(--grades-shell-max-width);
  margin: 0 auto;
  padding: 20px 16px 28px;
}

.grades-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.grades-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: var(--grades-text-main);
}

.grades-subtitle {
  margin: 6px 0 0;
  font-size: 14px;
  color: var(--grades-text-sub);
}

.filter-panel,
.summary-panel,
.grade-card,
.state-panel,
.grade-table-card {
  background: var(--grades-panel-bg);
  border: 1px solid var(--grades-panel-border);
  box-shadow: var(--grades-panel-shadow);
  backdrop-filter: blur(var(--grades-panel-blur));
}

.filter-panel {
  border-radius: var(--grades-panel-radius);
  padding: 18px;
  display: grid;
  gap: 16px;
}

.field-group {
  display: grid;
  gap: 10px;
}

.field-group--toolbar {
  min-width: 0;
}

.field-label {
  font-size: 13px;
  font-weight: 600;
  color: #56657f;
}

.field-select {
  width: 100%;
  height: 44px;
  border-radius: var(--grades-control-radius);
  border: 1px solid #d6deed;
  background: #fff;
  padding: 0 14px;
  font-size: 14px;
  color: var(--grades-text-main);
}

.view-toolbar {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.segmented-control {
  display: grid;
  gap: 8px;
}

.segmented-control--semester {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.segmented-control--compact {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.segment-btn {
  height: 42px;
  border-radius: var(--grades-control-radius);
  border: 1px solid #d6deed;
  background: #f8faff;
  color: #516079;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.18s ease;
}

.segment-btn--rich {
  height: auto;
  min-height: 62px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  gap: 4px;
  text-align: left;
}

.segment-btn__main {
  font-size: 14px;
  font-weight: 700;
  color: inherit;
}

.segment-btn__sub {
  font-size: 12px;
  font-weight: 500;
  color: var(--grades-text-muted);
}

.segment-btn.active {
  border-color: var(--grades-primary);
  background: var(--grades-primary-surface);
  color: var(--grades-primary-text);
  box-shadow: inset 0 0 0 1px rgba(79, 134, 247, 0.12);
}

.segment-btn.active .segment-btn__sub {
  color: #5381de;
}

.action-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.primary-btn,
.secondary-btn {
  height: 42px;
  padding: 0 16px;
  border: none;
  border-radius: var(--grades-control-radius);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.primary-btn {
  background: var(--grades-primary);
  color: #fff;
}

.secondary-btn {
  background: var(--grades-secondary-surface);
  color: var(--grades-secondary-text);
}

.primary-btn:disabled,
.secondary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.summary-panel {
  margin-top: 14px;
  border-radius: var(--grades-panel-radius);
  padding: 14px 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.summary-item {
  display: grid;
  gap: 6px;
}

.summary-label {
  font-size: 12px;
  color: var(--grades-text-muted);
}

.summary-value {
  font-size: 16px;
  color: var(--grades-text-main);
}

.state-panel {
  margin-top: 14px;
  border-radius: var(--grades-panel-radius);
  min-height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  text-align: center;
  padding: 24px;
}

.state-panel--empty {
  color: var(--grades-text-sub);
}

.state-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1a2740;
}

.state-text {
  margin: 0;
  font-size: 14px;
  color: #74839c;
}

.loader {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 3px solid #dce7fb;
  border-top-color: var(--grades-primary);
  animation: spin 0.8s linear infinite;
}

.grades-list {
  margin-top: 14px;
  display: grid;
  gap: 12px;
}

.grade-card {
  border-radius: var(--grades-panel-radius);
  padding: 18px;
}

.grade-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.grade-card__title-wrap {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.course-name {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--grades-text-main);
}

.course-code {
  margin: 0;
  font-size: 13px;
  color: var(--grades-text-muted);
}

.score-pill {
  flex-shrink: 0;
  min-width: 92px;
  padding: 8px 12px;
  border-radius: var(--grades-score-radius);
  background: var(--grades-score-pill-bg);
  display: grid;
  gap: 2px;
  text-align: center;
}

.score-pill__label {
  font-size: 11px;
  color: var(--grades-score-label-color);
}

.score-pill__value {
  font-size: 20px;
  font-weight: 700;
  line-height: 1.1;
  color: var(--grades-primary-text);
}

.meta-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.meta-item {
  display: grid;
  gap: 4px;
  padding: 10px 12px;
  border-radius: 14px;
  background: var(--grades-meta-item-bg);
  border: 1px solid var(--grades-meta-item-border);
}

.meta-label {
  font-size: 12px;
  color: var(--grades-text-muted);
}

.meta-value {
  font-size: 14px;
  color: #23324d;
  word-break: break-word;
}

.grade-table-card {
  margin-top: 14px;
  border-radius: var(--grades-panel-radius);
  overflow: hidden;
}

.grade-table-wrap {
  width: 100%;
  overflow-x: auto;
}

.grade-table {
  width: 100%;
  border-collapse: collapse;
  min-width: var(--grades-table-min-width);
}

.grade-table thead th {
  padding: 14px 12px;
  background: var(--grades-table-head-bg);
  border-bottom: 1px solid #dfe8f7;
  color: #52627d;
  font-size: 13px;
  font-weight: 700;
  text-align: left;
  white-space: nowrap;
}

.grade-table tbody td {
  padding: 14px 12px;
  border-bottom: 1px solid #e8eef8;
  border-right: 1px solid #edf2fb;
  font-size: 14px;
  color: var(--grades-text-table);
  background: var(--grades-table-row-bg);
  vertical-align: middle;
}

.grade-table tbody tr:last-child td {
  border-bottom: none;
}

.grade-table th:last-child,
.grade-table td:last-child {
  border-right: none;
}

.grade-table tbody tr:hover td {
  background: var(--grades-table-row-hover-bg);
}

.col-course {
  min-width: 180px;
}

.col-code,
.col-nature,
.col-exam,
.col-teacher {
  min-width: 120px;
}

.col-score,
.col-gpa,
.col-credit {
  min-width: 88px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media screen and (max-width: 767px) {
  .grades-shell {
    padding: 16px 12px 20px;
  }

  .grades-title {
    font-size: 24px;
  }

  .view-toolbar,
  .summary-panel,
  .meta-grid,
  .segmented-control--semester {
    grid-template-columns: 1fr;
  }

  .grade-card__header {
    flex-direction: column;
  }

  .score-pill {
    width: 100%;
  }

  .action-row {
    display: grid;
    grid-template-columns: 1fr;
  }
}
</style>
