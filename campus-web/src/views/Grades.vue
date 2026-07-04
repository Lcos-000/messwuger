<template>
  <div class="grades-root">
    <div class="grades-backdrop"></div>
    <div class="grades-shell">
      <header class="grades-header">
        <div>
          <h1 class="grades-title">成绩查询</h1>
          <p class="grades-subtitle">选择学年与学期后查询，必要时可重新触发拉取</p>
        </div>
      </header>

      <section class="filter-panel">
        <div class="field-group">
          <label class="field-label">学年</label>
          <select v-model="filters.academicYear" class="field-select">
            <option v-for="option in academicYearOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </div>

        <div class="field-group">
          <label class="field-label">学期</label>
          <div class="segmented-control">
            <button
              v-for="option in semesterOptions"
              :key="option.value"
              type="button"
              class="segment-btn"
              :class="{ active: filters.semester === option.value }"
              @click="filters.semester = option.value"
            >
              {{ option.label }}
            </button>
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
          <span class="summary-label">当前筛选</span>
          <strong class="summary-value">{{ currentSummary }}</strong>
        </div>
        <div class="summary-item">
          <span class="summary-label">记录数</span>
          <strong class="summary-value">{{ grades.length }}</strong>
        </div>
      </section>

      <section v-if="loading" class="state-panel">
        <div class="loader"></div>
        <p class="state-text">正在加载成绩...</p>
      </section>

      <section v-else-if="grades.length === 0" class="state-panel state-panel--empty">
        <p class="state-title">暂无成绩</p>
        <p class="state-text">{{ GRADES_CONFIG.EMPTY_TEXT }}</p>
      </section>

      <section v-else class="grades-list">
        <article v-for="item in grades" :key="buildGradeKey(item)" class="grade-card">
          <div class="grade-card__main">
            <div class="grade-card__header">
              <h2 class="course-name">{{ item.courseName || '-' }}</h2>
              <span class="score-badge">{{ item.score || '-' }}</span>
            </div>
            <div class="meta-grid">
              <div class="meta-item">
                <span class="meta-label">课程代码</span>
                <span class="meta-value">{{ item.courseCode || '-' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">绩点</span>
                <span class="meta-value">{{ item.gpa || '-' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">学分</span>
                <span class="meta-value">{{ item.credit || '-' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">教师</span>
                <span class="meta-value">{{ item.teacher || '-' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">课程性质</span>
                <span class="meta-value">{{ item.courseNature || '-' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">考核性质</span>
                <span class="meta-value">{{ item.examNature || '-' }}</span>
              </div>
            </div>
          </div>
        </article>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { getGrades, submitGradesTask } from '@/api/index'
import { GRADES_CONFIG } from '@/config'

const now = new Date()
const currentYear = now.getMonth() >= 7 ? now.getFullYear() : now.getFullYear() - 1

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

const academicYearOptions = buildAcademicYearOptions()
const semesterOptions = GRADES_CONFIG.SEMESTER_OPTIONS

const filters = reactive({
  academicYear: academicYearOptions[0]?.value || String(currentYear),
  semester: semesterOptions[2]?.value || '12'
})

const loading = ref(false)
const submitting = ref(false)
const grades = ref([])

const currentSummary = computed(() => {
  const selectedYear = academicYearOptions.find(item => item.value === filters.academicYear)
  return `${selectedYear?.label || filters.academicYear} · 学期 ${filters.semester}`
})

const fetchGradesData = async () => {
  loading.value = true
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

const buildGradeKey = (item) => [
  item.courseCode,
  item.courseName,
  item.academicYear,
  item.semester,
  item.teacher
].join('-')

onMounted(() => {
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
  max-width: 1080px;
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
  font-size: 28px;
  font-weight: 700;
  color: #142033;
  margin: 0;
}

.grades-subtitle {
  margin: 6px 0 0;
  font-size: 14px;
  color: #6f7f99;
}

.filter-panel,
.summary-panel,
.grade-card,
.state-panel {
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(225, 232, 244, 0.9);
  box-shadow: 0 10px 30px rgba(20, 32, 51, 0.06);
  backdrop-filter: blur(14px);
}

.filter-panel {
  border-radius: 18px;
  padding: 18px;
  display: grid;
  gap: 16px;
}

.field-group {
  display: grid;
  gap: 10px;
}

.field-label {
  font-size: 13px;
  font-weight: 600;
  color: #56657f;
}

.field-select {
  width: 100%;
  height: 44px;
  border-radius: 12px;
  border: 1px solid #d6deed;
  background: #fff;
  padding: 0 14px;
  font-size: 14px;
  color: #142033;
}

.segmented-control {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.segment-btn {
  height: 42px;
  border-radius: 12px;
  border: 1px solid #d6deed;
  background: #f8faff;
  color: #516079;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.18s ease;
}

.segment-btn.active {
  border-color: #4f86f7;
  background: #eef4ff;
  color: #2f67da;
  box-shadow: inset 0 0 0 1px rgba(79, 134, 247, 0.12);
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
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
}

.primary-btn {
  background: #4f86f7;
  color: #fff;
}

.secondary-btn {
  background: #edf3ff;
  color: #376ddc;
}

.primary-btn:disabled,
.secondary-btn:disabled {
  opacity: 0.6;
}

.summary-panel {
  margin-top: 14px;
  border-radius: 18px;
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
  color: #7f8da5;
}

.summary-value {
  font-size: 16px;
  color: #142033;
}

.state-panel {
  margin-top: 14px;
  border-radius: 18px;
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
  color: #6f7f99;
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
  border-top-color: #4f86f7;
  animation: spin 0.8s linear infinite;
}

.grades-list {
  margin-top: 14px;
  display: grid;
  gap: 12px;
}

.grade-card {
  border-radius: 18px;
  padding: 18px;
}

.grade-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.course-name {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #142033;
}

.score-badge {
  flex-shrink: 0;
  min-width: 56px;
  height: 36px;
  padding: 0 12px;
  border-radius: 999px;
  background: #eef4ff;
  color: #2f67da;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
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
}

.meta-label {
  font-size: 12px;
  color: #7f8da5;
}

.meta-value {
  font-size: 14px;
  color: #23324d;
  word-break: break-word;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media screen and (max-width: 767px) {
  .grades-shell {
    padding: 16px 12px 20px;
  }

  .grades-title {
    font-size: 24px;
  }

  .summary-panel,
  .meta-grid {
    grid-template-columns: 1fr;
  }

  .grade-card__header {
    flex-direction: column;
  }
}
</style>
