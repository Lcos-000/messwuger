<template>
  <div class="schedule-root">
    <!-- ===== 顶栏 ===== -->
    <header class="top-bar">
      <div class="top-bar__left">
        <button class="icon-btn" @click="changeWeek(-1)" title="上一周">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>
        </button>
      </div>
      <div class="top-bar__center">
        <span class="week-label">{{ currentWeek === 0 ? '全学期课表' : `第 ${currentWeek} 周` }}</span>
      </div>
      <div class="top-bar__right">
        <button class="text-chip" @click="currentWeek = getCurrentWeek()">本周</button>
        <button class="text-chip" @click="currentWeek = 0">全期</button>
        <button class="text-chip refresh-chip" @click="handleManualRefresh">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
          刷新
        </button>
        <button class="icon-btn" @click="changeWeek(1)" title="下一周">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
        </button>
      </div>
    </header>

    <!-- ===== 加载中 ===== -->
    <div v-if="loading" class="state-screen">
      <div class="pulse-ring"></div>
      <p class="state-text">加载课表中…</p>
    </div>

    <!-- ===== 同步中 ===== -->
    <div v-else-if="isSyncing" class="state-screen">
      <div class="sync-icon-wrap pulse-ring">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#4f86f7" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>
      </div>
      <p class="state-title">数据同步中</p>
      <p class="state-sub">正在为您拉取最新课表，请稍候...</p>
    </div>

    <!-- ===== 课表主体 ===== -->
    <div v-else class="schedule-wrap">
      <!-- 表头：星期 -->
      <div class="grid-header">
        <div class="th-corner">
          <span>节</span>
        </div>
        <div class="th-day" v-for="(day, i) in days" :key="i" :class="{ today: isTodayColumn(i + 1) }">
          <span class="day-zh">{{ day }}</span>
        </div>
      </div>

      <!-- 表体 -->
      <div class="grid-body">
        <!-- 节次列 -->
        <div class="period-col">
          <div class="period-cell" v-for="p in 14" :key="p">
            <span class="period-num">{{ p }}</span>
            <span class="period-time">{{ periodTime[p - 1] }}</span>
          </div>
        </div>

        <!-- 课程区域：绝对定位布局 -->
        <div class="courses-area" ref="coursesArea">
          <transition name="week-switch" mode="out-in">
            <div :key="currentWeek" class="courses-layer">
          <!-- 背景格线 -->
          <template v-for="row in 14" :key="'r'+row">
            <div
              v-for="col in 7"
              :key="'c'+col"
              class="grid-cell"
              :class="{ 'today-col': isTodayColumn(col), 'alt-row': row % 2 === 0 }"
              :style="{ gridColumn: col, gridRow: row }"
            ></div>
          </template>

          <!-- 空状态 -->
          <div v-if="courseGroups.length === 0" class="empty-overlay">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#c7d2e8" stroke-width="1.3"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            <p>{{ currentWeek === 0 ? '本学期暂无课程' : '本周暂无课程' }}</p>
          </div>

          <!-- 课程块 -->
          <div
            v-for="(group, gIdx) in courseGroups"
            :key="gIdx"
            class="course-slot"
            :style="getSlotStyle(group)"
          >
            <!-- 移动端：同一 slot 多课程合并成一个色块，内部上下堆叠 -->
            <div
              v-if="isMobile && group.courses.length > 1"
              class="course-card course-card--stack"
              :style="{ backgroundColor: getCourseColors(group.courses[0].courseName)[1] }"
              @click="openStackDetail(group)"
            >
              <div class="card-accent" :style="{ background: getCourseColor(group.courses[0].courseName) }"></div>
              <div class="card-body">
                <div
                  v-for="(course, cIdx) in group.courses"
                  :key="cIdx"
                  class="stack-item"
                  :class="{ 'stack-sep': cIdx > 0 }"
                >
                  <div class="card-name">{{ course.courseName }}</div>
                  <div class="card-meta">
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
                    {{ course.classroom }}
                  </div>
                  <div class="card-meta" v-if="course.teachers && course.teachers.length">
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                    <template v-for="(t, ti) in course.teachers" :key="ti">
                      {{ t.name }}<span v-if="currentWeek === 0 && t.weeks" class="weeks-badge">{{ t.weeks }}</span>
                    </template>
                  </div>
                </div>
              </div>
            </div>

            <!-- PC 端或单课程：保持原卡片 -->
            <div
              v-for="(course, cIdx) in (isMobile && group.courses.length > 1 ? [] : group.courses)"
              :key="cIdx"
              class="course-card"
              :style="getCardStyle(course, group.courses.length, cIdx)"
              @click="openDetail(course)"
            >
              <div class="card-accent" :style="{ background: getCourseColor(course.courseName) }"></div>
              <div class="card-body">
                <div class="card-name">{{ course.courseName }}</div>
                <div class="card-meta">
                  <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
                  {{ course.classroom }}
                </div>
                <div class="card-meta" v-if="course.teachers && course.teachers.length">
                  <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  <template v-for="(t, ti) in course.teachers" :key="ti">
                    {{ t.name }}<span v-if="currentWeek === 0 && t.weeks" class="weeks-badge">{{ t.weeks }}</span>
                  </template>
                </div>
              </div>
            </div>
          </div>
            </div>
          </transition>
        </div>
      </div>
    </div>

    <!-- ===== 课程详情弹窗 ===== -->
    <transition name="modal-fade">
      <div v-if="detailCourse" class="modal-overlay" @click.self="detailCourse = null">
        <div class="modal-card">
          <div class="modal-accent" :style="{ background: getCourseColor(detailCourse.courseName) }"></div>
          <div class="modal-content">
            <h3 class="modal-title">{{ detailCourse.courseName }}</h3>
            <div class="modal-row">
              <span class="modal-label">教室</span><span>{{ detailCourse.classroom }}</span>
            </div>
            <div class="modal-row">
              <span class="modal-label">教师</span>
              <span>{{ detailCourse.teachers?.map(t => t.name).join('、') }}</span>
            </div>
            <div class="modal-row">
              <span class="modal-label">节次</span><span>{{ detailCourse.periods }}</span>
            </div>
            <div class="modal-row">
              <span class="modal-label">周次</span><span>{{ detailCourse.teachers?.map(t => t.weeks).join(' / ') || detailCourse.weeks }}</span>
            </div>
            <div class="modal-row">
              <span class="modal-label">校区</span><span>{{ detailCourse.campus }}</span>
            </div>
          </div>
          <button class="modal-close" @click="detailCourse = null">关闭</button>
        </div>
      </div>
    </transition>

    <!-- ===== 堆叠课程选择弹窗 ===== -->
    <transition name="modal-fade">
      <div v-if="stackGroup" class="modal-overlay" @click.self="closeStackDetail">
        <div class="modal-card">
          <div class="modal-accent" :style="{ background: getCourseColor(stackGroup.courses[0].courseName) }"></div>
          <div class="modal-content">
            <h3 class="modal-title">该时段有 {{ stackGroup.courses.length }} 门课</h3>
            <div class="stack-list">
              <div
                v-for="(course, idx) in stackGroup.courses"
                :key="idx"
                class="stack-list-item"
                @click="detailCourse = course; closeStackDetail()"
              >
                <div class="stack-dot" :style="{ background: getCourseColor(course.courseName) }"></div>
                <div class="stack-info">
                  <div class="stack-name">{{ course.courseName }}</div>
                  <div class="stack-meta">{{ course.classroom }} · {{ course.teachers?.map(t => t.name).join('、') }}</div>
                </div>
              </div>
            </div>
          </div>
          <button class="modal-close" @click="closeStackDetail">关闭</button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { getSchedule, refreshSchedule, getUserStatus } from '@/api/index'

const days = ['一', '二', '三', '四', '五', '六', '日']
const periodTime = [
  '08:00','08:55','09:50','10:55','11:50',
  '13:30','14:25','15:20','16:25','17:20',
  '18:30','19:25','20:20','21:15'
]

function getCurrentWeek() {
  const semesterStart = new Date('2025-02-17')
  const now = new Date()
  const diff = Math.floor((now - semesterStart) / (7 * 24 * 60 * 60 * 1000))
  return Math.max(1, Math.min(diff + 1, 25))
}

function isTodayColumn(dayIndex) {
  const d = new Date().getDay()
  const today = d === 0 ? 7 : d
  return today === dayIndex
}

const currentWeek = ref(0)
const loading = ref(true)
const isSyncing = ref(false)
const rawScheduleData = ref([])
const detailCourse = ref(null)
const stackGroup = ref(null)
const rowHeight = ref(58)
const isMobile = ref(false)

let statusTimer = null

function updateLayout() {
  const w = window.innerWidth
  isMobile.value = w < 768
  rowHeight.value = w < 360 ? 52 : 58
}

onMounted(() => {
  updateLayout()
  window.addEventListener('resize', updateLayout)
  fetchSchedule()
})

onUnmounted(() => {
  stopStatusPolling()
})

const stopStatusPolling = () => {
  if (statusTimer) {
    clearInterval(statusTimer)
    statusTimer = null
  }
}

const checkSyncStatus = async () => {
  try {
    const res = await getUserStatus()
    if (res.code === 200 && res.data) {
      if (res.data.syncStatus === 2) {
        // 同步成功，停止轮询并刷新课表
        stopStatusPolling()
        fetchSchedule()
      } else if (res.data.syncStatus === 3) {
        // 同步失败
        stopStatusPolling()
        showToast('数据同步失败，请稍后再试')
      }
    }
  } catch (error) {
    console.error('查询状态失败:', error)
  }
}

const startStatusPolling = () => {
  if (statusTimer) return
  isSyncing.value = true
  statusTimer = setInterval(checkSyncStatus, 3000)
}

const fetchSchedule = async () => {
  loading.value = true
  try {
    const res = await getSchedule()
    const scheduleJson = res.data?.scheduleJson
    if (!scheduleJson || scheduleJson === '[]' || scheduleJson === '{}') { 
      startStatusPolling()
      return 
    }
    try {
      const parsed = JSON.parse(scheduleJson)
      rawScheduleData.value = Array.isArray(parsed) ? parsed : (parsed.courses || [])
      isSyncing.value = false
      stopStatusPolling()
    } catch (e) {
      console.error('scheduleJson 解析失败', e)
      startStatusPolling()
    }
  } catch (error) {
    console.error('获取课表失败:', error)
    startStatusPolling()
  } finally {
    loading.value = false
  }
}

const handleManualRefresh = async () => {
  try {
    await refreshSchedule()
    showToast('已触发刷新，正在拉取最新数据')
    startStatusPolling()
  } catch (error) {
    console.error(error)
  }
}

function showToast(msg) {
  alert(msg)
}

const changeWeek = (delta) => {
  let v = currentWeek.value + delta
  if (currentWeek.value === 0 && delta > 0) v = 1
  else if (currentWeek.value === 0 && delta < 0) v = 25
  if (v > 25) v = 0
  if (v < 0) v = 25
  currentWeek.value = v
}

const parseWeeks = (weeks) => {
  if (!weeks) return []
  if (Array.isArray(weeks)) return weeks
  const str = String(weeks).replace(/周/g, '').trim()
  const result = []
  str.split(',').forEach(part => {
    if (part.includes('-')) {
      const [s, e] = part.split('-').map(Number)
      for (let i = s; i <= e; i++) result.push(i)
    } else {
      const n = Number(part)
      if (!isNaN(n) && n > 0) result.push(n)
    }
  })
  return result
}

const parsePeriods = (periodStr) => {
  if (!periodStr) return { start: 0, end: 0 }
  const m = periodStr.match(/(\d+)-(\d+)/)
  if (m) return { start: parseInt(m[1]), end: parseInt(m[2]) }
  const s = periodStr.match(/(\d+)/)
  if (s) { const n = parseInt(s[1]); return { start: n, end: n } }
  return { start: 0, end: 0 }
}

const courseGroups = computed(() => {
  const courses = rawScheduleData.value.filter(c => c.dayOfWeek && c.dayOfWeek > 0)
  const filtered = currentWeek.value === 0
    ? courses
    : courses.filter(c => parseWeeks(c.weeks).includes(currentWeek.value))

  const groupsMap = {}
  filtered.forEach(c => {
    const { start, end } = parsePeriods(c.periods)
    if (!start) return
    const key = `${c.dayOfWeek}-${start}-${end}`
    if (!groupsMap[key]) {
      groupsMap[key] = { dayOfWeek: c.dayOfWeek, start, end, courses: [] }
    }
    const existing = groupsMap[key].courses.find(
      ec => ec.courseName === c.courseName && ec.classroom === c.classroom
    )
    if (existing) {
      existing.teachers.push({ name: c.teacher, weeks: c.weeks })
    } else {
      groupsMap[key].courses.push({ ...c, teachers: [{ name: c.teacher, weeks: c.weeks }] })
    }
  })
  return Object.values(groupsMap)
})

const PALETTE = [
  ['#4f86f7','#dbeafe'],['#10b981','#d1fae5'],['#f97316','#ffedd5'],
  ['#8b5cf6','#ede9fe'],['#ec4899','#fce7f3'],['#06b6d4','#cffafe'],
  ['#f59e0b','#fef3c7'],['#ef4444','#fee2e2'],['#14b8a6','#ccfbf1'],
  ['#6366f1','#e0e7ff'],['#84cc16','#ecfccb'],['#a855f7','#f3e8ff']
]

function getCourseColor(name) {
  if (!name) return '#4f86f7'
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = (hash << 5) - hash + name.charCodeAt(i)
    hash |= 0
  }
  return PALETTE[Math.abs(hash) % PALETTE.length][0]
}

function getCourseColors(name) {
  if (!name) return PALETTE[0]
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = (hash << 5) - hash + name.charCodeAt(i)
    hash |= 0
  }
  return PALETTE[Math.abs(hash) % PALETTE.length]
}

// 每节课格子高度 px
const ROW_H = 58

const getSlotStyle = (group) => {
  const top = (group.start - 1) * rowHeight.value
  const span = group.end - group.start + 1
  return {
    position: 'absolute',
    top: `${top}px`,
    left: `${(group.dayOfWeek - 1) * (100 / 7)}%`,
    width: `${100 / 7}%`,
    height: `${span * rowHeight.value}px`,
    zIndex: 1,
  }
}

const getCardStyle = (course, total, idx) => {
  const [accent, bg] = getCourseColors(course.courseName)
  const width = total > 1 ? `calc(${100 / total}% - 2px)` : '100%'
  const left = total > 1 ? `calc(${(100 / total) * idx}% + 1px)` : '0'
  return {
    backgroundColor: bg,
    borderLeft: `3px solid ${accent}`,
    position: 'absolute',
    top: '0',
    left,
    width,
    height: '100%',
  }
}

const openDetail = (course) => {
  detailCourse.value = course
}

const openStackDetail = (group) => {
  stackGroup.value = group
}

const closeStackDetail = () => {
  stackGroup.value = null
}
</script>

<style scoped>
/* ============================================================
   Layout
   ============================================================ */
.schedule-root {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f0f4fb;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', sans-serif;
}

/* ============================================================
   Top Bar
   ============================================================ */
.top-bar {
  display: flex;
  align-items: center;
  padding: 0 12px;
  height: 52px;
  background: #fff;
  border-bottom: 1px solid #e8edf5;
  box-shadow: 0 1px 8px rgba(79,134,247,.08);
  flex-shrink: 0;
  gap: 8px;
}
.top-bar__left, .top-bar__right {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}
.top-bar__center {
  flex: 1;
  text-align: center;
}
.week-label {
  font-size: 15px;
  font-weight: 700;
  color: #1a2540;
  letter-spacing: .3px;
}
.icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4f86f7;
  background: #eef3ff;
  transition: background .15s;
}
.icon-btn:active { background: #d6e4ff; }
.text-chip {
  height: 28px;
  padding: 0 10px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 500;
  color: #4f86f7;
  background: #eef3ff;
  transition: background .15s;
  display: flex;
  align-items: center;
  gap: 4px;
}
.text-chip:active { background: #d6e4ff; }
.refresh-chip { color: #4f86f7; }

/* ============================================================
   State Screens
   ============================================================ */
.state-screen {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: #f0f4fb;
}
.pulse-ring {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 3px solid #e0e7ff;
  border-top-color: #4f86f7;
  animation: spin .8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.state-text { font-size: 14px; color: #8a9bc0; }
.sync-icon-wrap {
  width: 72px; height: 72px;
  background: #eef3ff;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.state-title { font-size: 16px; font-weight: 600; color: #1a2540; }
.state-sub { font-size: 13px; color: #8a9bc0; }
.pill-btn {
  padding: 10px 28px;
  background: linear-gradient(135deg, #4f86f7 0%, #6366f1 100%);
  color: #fff;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: .3px;
  box-shadow: 0 4px 14px rgba(79,134,247,.35);
  transition: transform .15s, box-shadow .15s;
}
.pill-btn:active { transform: scale(.96); box-shadow: 0 2px 8px rgba(79,134,247,.25); }

/* ============================================================
   Schedule Grid
   ============================================================ */
.schedule-wrap {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

.grid-header {
  display: flex;
  background: #fff;
  border-bottom: 1px solid #e8edf5;
  flex-shrink: 0;
}
.th-corner {
  width: 36px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #b0bdd4;
  border-right: 1px solid #e8edf5;
}
.th-day {
  flex: 1;
  padding: 8px 0;
  text-align: center;
  font-size: 12px;
  font-weight: 600;
  color: #8a9bc0;
  letter-spacing: .5px;
  position: relative;
  transition: color .2s;
}
.th-day.today { color: #4f86f7; }
.th-day.today::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 20%;
  right: 20%;
  height: 2px;
  background: #4f86f7;
  border-radius: 2px 2px 0 0;
}
.day-zh::before { content: '周'; }

.grid-body {
  display: flex;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 节次列 */
.period-col {
  width: 36px;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid #e8edf5;
}
.period-cell {
  height: 58px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #f0f4fb;
}
.period-num {
  font-size: 12px;
  font-weight: 700;
  color: #4f86f7;
  line-height: 1;
}
.period-time {
  font-size: 9px;
  color: #b0bdd4;
  margin-top: 2px;
  line-height: 1;
}

/* 课程主区域 */
.courses-area {
  flex: 1;
  position: relative;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-template-rows: repeat(14, 58px);
}

.courses-layer {
  position: absolute;
  inset: 0;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-template-rows: repeat(14, 58px);
}

/* 背景格线 */
.grid-cell {
  border-right: 1px solid #e8edf5;
  border-bottom: 1px solid #e8edf5;
  transition: background .2s;
}
.grid-cell.alt-row { background: rgba(240,244,251,.5); }
.grid-cell.today-col { background: rgba(79,134,247,.04); }
.grid-cell.alt-row.today-col { background: rgba(79,134,247,.07); }

/* 空状态 */
.empty-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #b0bdd4;
  font-size: 13px;
  pointer-events: none;
  z-index: 2;
}

/* ============================================================
   Course Slot & Card
   ============================================================ */
.course-slot {
  padding: 1px;
  position: absolute;
  top: 0;
  left: 0;
  display: flex;
  z-index: 1;
  min-height: 0;
  overflow: hidden;
}

.course-card {
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: row;
  transition: box-shadow .18s, transform .15s;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.course-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,.12);
  transform: translateY(-1px);
  z-index: 3;
}
.course-card:active { transform: scale(.97); }

.course-card--stack {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  flex-direction: row;
  overflow: hidden;
}
.course-card--stack .card-body {
  padding: 3px 4px 3px 4px;
  gap: 0;
  overflow-y: auto;
}
.stack-item {
  flex-shrink: 0;
  padding: 2px 0;
}
.stack-sep {
  border-top: 1px dashed rgba(0,0,0,.08);
  margin-top: 2px;
}
.course-card--stack .card-name {
  font-size: 9px;
  -webkit-line-clamp: 2;
}
.course-card--stack .card-meta {
  font-size: 8px;
}

.card-accent {
  width: 3px;
  flex-shrink: 0;
  border-radius: 8px 0 0 8px;
}

.card-body {
  flex: 1;
  padding: 5px 5px 5px 4px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
  overflow-x: hidden;
  min-width: 0;
}
.card-name {
  font-size: 10px;
  font-weight: 700;
  color: #1a2540;
  line-height: 1.25;
  word-break: break-word;
}

@media screen and (min-width: 768px) {
  .card-name { font-size: 11px; }
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 9.5px;
  color: #6b7a99;
  line-height: 1.2;
  word-break: break-word;
}
.card-meta svg { flex-shrink: 0; opacity: .65; }
.weeks-badge {
  background: rgba(79,134,247,.12);
  color: #4f86f7;
  border-radius: 4px;
  padding: 0 3px;
  font-size: 8.5px;
  margin-left: 2px;
  flex-shrink: 0;
}

/* ============================================================
   Detail Modal
   ============================================================ */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15,25,50,.45);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 200;
  padding: 0 0 env(safe-area-inset-bottom, 0) 0;
}
.modal-card {
  width: 100%;
  max-width: 480px;
  background: #fff;
  border-radius: 20px 20px 0 0;
  overflow: hidden;
  box-shadow: 0 -8px 40px rgba(0,0,0,.15);
}
.modal-accent {
  height: 4px;
  width: 100%;
}
.modal-content {
  padding: 20px 24px 12px;
}
.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a2540;
  margin-bottom: 16px;
  line-height: 1.3;
}
.modal-row {
  display: flex;
  align-items: flex-start;
  padding: 9px 0;
  border-bottom: 1px solid #f0f4fb;
  font-size: 14px;
  color: #2d3a56;
  gap: 12px;
}
.modal-row:last-child { border-bottom: none; }
.modal-label {
  width: 36px;
  flex-shrink: 0;
  color: #8a9bc0;
  font-size: 13px;
}
.modal-close {
  display: block;
  width: calc(100% - 48px);
  margin: 8px 24px 20px;
  padding: 13px;
  background: linear-gradient(135deg, #4f86f7 0%, #6366f1 100%);
  color: #fff;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 600;
  text-align: center;
  letter-spacing: .3px;
  box-shadow: 0 4px 14px rgba(79,134,247,.3);
  transition: transform .15s;
}
.modal-close:active { transform: scale(.97); }

.stack-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}
.stack-list-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: #f7f9fc;
  border-radius: 12px;
  cursor: pointer;
  transition: background .15s, transform .12s;
}
.stack-list-item:active {
  background: #eef3ff;
  transform: scale(.98);
}
.stack-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.stack-info {
  flex: 1;
  min-width: 0;
}
.stack-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a2540;
  line-height: 1.3;
}
.stack-meta {
  font-size: 13px;
  color: #8a9bc0;
  margin-top: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ============================================================
   Modal Transition
   ============================================================ */
.modal-fade-enter-active, .modal-fade-leave-active {
  transition: opacity .25s;
}
.modal-fade-enter-active .modal-card,
.modal-fade-leave-active .modal-card {
  transition: transform .25s cubic-bezier(.32,1.28,.52,1);
}
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }
.modal-fade-enter-from .modal-card { transform: translateY(100%); }
.modal-fade-leave-to .modal-card { transform: translateY(100%); }

/* ============================================================
   Scrollbar
   ============================================================ */
.grid-body::-webkit-scrollbar { width: 4px; }
.grid-body::-webkit-scrollbar-thumb { background: #d0d9ee; border-radius: 2px; }
.grid-body::-webkit-scrollbar-track { background: transparent; }

/* ============================================================
   Week switch transition
   ============================================================ */
.week-switch-enter-active,
.week-switch-leave-active {
  transition: opacity 0.22s cubic-bezier(0.25, 0.1, 0.25, 1),
              transform 0.28s cubic-bezier(0.32, 0.72, 0, 1);
}
.week-switch-enter-from {
  opacity: 0;
  transform: scale(0.985);
}
.week-switch-leave-to {
  opacity: 0;
  transform: scale(1.015);
}

/* ============================================================
   Mobile tweaks
   ============================================================ */
@media screen and (max-width: 360px) {
  .top-bar { height: 48px; padding: 0 8px; }
  .week-label { font-size: 14px; }
  .icon-btn { width: 28px; height: 28px; }
  .text-chip { height: 24px; padding: 0 7px; font-size: 11px; }
  .th-day { font-size: 10px; padding: 6px 0; }
  .period-cell { height: 52px; }
  .period-num { font-size: 10px; }
  .period-time { font-size: 8px; }
  .courses-area { grid-template-rows: repeat(14, 52px); }
  .card-name { font-size: 9px; -webkit-line-clamp: 3; }
  .card-meta { font-size: 8.5px; }
}
</style>
