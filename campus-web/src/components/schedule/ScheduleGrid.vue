<template>
  <div class="schedule-wrap">
    <div class="grid-header">
      <div class="th-corner">
        <span>{{ text.PERIOD_HEADER }}</span>
      </div>
      <div v-for="(day, index) in days" :key="index" class="th-day" :class="{ today: isTodayColumn(index + 1) }">
        <span class="day-zh">{{ day }}</span>
      </div>
    </div>

    <div class="grid-body">
      <div class="period-col">
        <div v-for="period in totalPeriods" :key="period" class="period-cell">
          <span class="period-num">{{ period }}</span>
          <span class="period-time">{{ periodTime[period - 1] }}</span>
        </div>
      </div>

      <div class="courses-area">
        <transition name="week-switch" mode="out-in">
          <div :key="currentWeek" class="courses-layer">
            <template v-for="row in totalPeriods" :key="`row-${row}`">
              <div
                v-for="col in 7"
                :key="`cell-${row}-${col}`"
                class="grid-cell"
                :class="{ 'today-col': isTodayColumn(col), 'alt-row': row % 2 === 0 }"
                :style="{ gridColumn: col, gridRow: row }"
              ></div>
            </template>

            <div v-if="courseGroups.length === 0" class="empty-overlay">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#c7d2e8" stroke-width="1.3"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
              <p>{{ emptyText }}</p>
            </div>

            <div v-for="(group, groupIndex) in courseGroups" :key="groupIndex" class="course-slot" :style="getSlotStyle(group)">
              <div
                v-if="isMobile && group.courses.length > 1"
                class="course-card course-card--stack"
                :style="{ backgroundColor: getCourseColorPair(group.courses[0].courseName)[1] }"
                @click="emit('open-stack', group)"
              >
                <div class="card-accent" :style="{ background: getCourseColor(group.courses[0].courseName) }"></div>
                <div class="card-body">
                  <div v-for="(course, courseIndex) in group.courses" :key="courseIndex" class="stack-item" :class="{ 'stack-sep': courseIndex > 0 }">
                    <div class="card-name">{{ course.courseName }}</div>
                    <div class="card-meta">
                      <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
                      {{ course.classroom }}
                    </div>
                    <div v-if="course.teachers && course.teachers.length" class="card-meta">
                      <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                      <template v-for="(teacher, teacherIndex) in course.teachers" :key="teacherIndex">
                        {{ teacher.name }}<span v-if="currentWeek === 0 && teacher.weeks" class="weeks-badge">{{ teacher.weeks }}</span>
                      </template>
                    </div>
                  </div>
                </div>
              </div>

              <div
                v-for="(course, courseIndex) in (isMobile && group.courses.length > 1 ? [] : group.courses)"
                :key="courseIndex"
                class="course-card"
                :style="getCardStyle(course, group.courses.length, courseIndex)"
                @click="emit('open-detail', course)"
              >
                <div class="card-accent" :style="{ background: getCourseColor(course.courseName) }"></div>
                <div class="card-body">
                  <div class="card-name">{{ course.courseName }}</div>
                  <div class="card-meta">
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
                    {{ course.classroom }}
                  </div>
                  <div v-if="course.teachers && course.teachers.length" class="card-meta">
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                    <span>{{ buildTeacherNames(course.teachers) }}</span>
                    <span v-if="currentWeek === 0 && course.teachers.some(item => item.weeks)" class="weeks-badge">
                      {{ course.teachers.map(item => item.weeks).filter(Boolean).join(' / ') }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  text: { type: Object, required: true },
  days: { type: Array, required: true },
  periodTime: { type: Array, required: true },
  totalPeriods: { type: Number, required: true },
  currentWeek: { type: Number, required: true },
  courseGroups: { type: Array, required: true },
  isMobile: { type: Boolean, required: true },
  emptyText: { type: String, required: true },
  isTodayColumn: { type: Function, required: true },
  getSlotStyle: { type: Function, required: true },
  getCardStyle: { type: Function, required: true },
  getCourseColor: { type: Function, required: true },
  getCourseColorPair: { type: Function, required: true },
  buildTeacherNames: { type: Function, required: true }
})

const emit = defineEmits(['open-detail', 'open-stack'])
</script>

<style scoped>
.schedule-wrap {
  display: flex;
  flex-direction: column;
  flex: 1;
  margin: 12px 16px 16px;
  overflow: hidden;
  position: relative;
  z-index: 1;
  border: 1px solid rgba(214, 222, 235, 0.36);
  border-radius: 18px;
  background: transparent;
  box-shadow: none;
}

.grid-header {
  display: flex;
  background: transparent;
  border-bottom: 1px solid rgba(218, 226, 239, 0.52);
  flex-shrink: 0;
}

.th-corner {
  width: 64px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #9aa8bd;
  border-right: 1px solid rgba(218, 226, 239, 0.52);
}

.th-day {
  flex: 1;
  padding: 10px 0 9px;
  text-align: center;
  font-size: 12.5px;
  font-weight: 600;
  color: #6f7f96;
  letter-spacing: 0.4px;
  position: relative;
  transition: color 0.2s;
}

.th-day.today {
  color: #375f98;
}

.th-day.today::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 34%;
  right: 34%;
  height: 3px;
  background: #54719f;
  border-radius: 999px 999px 0 0;
}

.day-zh::before {
  content: '周';
}

.grid-body {
  display: flex;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  background: transparent;
}

.period-col {
  width: 64px;
  flex-shrink: 0;
  background: transparent;
  border-right: 1px solid rgba(218, 226, 239, 0.52);
}

.period-cell {
  height: var(--schedule-row-height);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(226, 232, 242, 0.48);
}

.period-num {
  font-size: 12px;
  font-weight: 700;
  color: #4c6488;
  line-height: 1;
}

.period-time {
  font-size: 8.5px;
  color: #a4afc1;
  margin-top: 2px;
  line-height: 1;
  white-space: nowrap;
}

.courses-area {
  flex: 1;
  position: relative;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-template-rows: repeat(14, var(--schedule-row-height));
}

.courses-layer {
  position: absolute;
  inset: 0;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-template-rows: repeat(14, var(--schedule-row-height));
}

.grid-cell {
  border-right: 1px solid rgba(226, 232, 242, 0.48);
  border-bottom: 1px solid rgba(226, 232, 242, 0.48);
  transition: background 0.2s;
}

.grid-cell.alt-row {
  background: transparent;
}

.grid-cell.today-col {
  background: rgba(84, 113, 159, 0.035);
}

.grid-cell.alt-row.today-col {
  background: rgba(84, 113, 159, 0.035);
}

.empty-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #a4afc1;
  font-size: 13px;
  pointer-events: none;
  z-index: 2;
}

.course-slot {
  padding: 3px;
  position: absolute;
  display: flex;
  z-index: 1;
  min-height: 0;
  overflow: visible;
}

.course-card {
  border-radius: 9px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: row;
  border: 1px solid rgba(100, 116, 139, 0.13);
  transition: border-color 0.18s, box-shadow 0.18s, transform 0.15s;
  box-shadow: 0 4px 10px rgba(30, 47, 78, 0.045);
}

.course-card:hover {
  border-color: rgba(84, 113, 159, 0.24);
  box-shadow: 0 10px 22px rgba(30, 47, 78, 0.1);
  transform: translateY(-1px);
  z-index: 3;
}

.course-card:active {
  transform: scale(0.985);
}

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
  padding: 4px 5px;
  gap: 0;
  overflow-y: auto;
}

.stack-item {
  flex-shrink: 0;
  padding: 2px 0;
}

.stack-sep {
  border-top: 1px dashed rgba(100, 116, 139, 0.16);
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
  width: 4px;
  flex-shrink: 0;
  border-radius: 9px 0 0 9px;
}

.card-body {
  flex: 1;
  padding: 6px 7px 6px 6px;
  display: flex;
  flex-direction: column;
  gap: 3px;
  overflow-y: auto;
  overflow-x: hidden;
  min-width: 0;
}

.card-name {
  font-size: 10px;
  font-weight: 650;
  color: #223047;
  line-height: 1.25;
  word-break: break-word;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 9.5px;
  color: #64748b;
  line-height: 1.2;
  word-break: break-word;
}

.card-meta svg {
  flex-shrink: 0;
  opacity: 0.58;
}

.weeks-badge {
  background: rgba(84, 113, 159, 0.1);
  color: #54719f;
  border-radius: 999px;
  padding: 0 4px;
  font-size: 8.5px;
  margin-left: 2px;
  flex-shrink: 0;
}

.week-switch-enter-active,
.week-switch-leave-active {
  transition: opacity 0.22s cubic-bezier(0.25, 0.1, 0.25, 1), transform 0.28s cubic-bezier(0.32, 0.72, 0, 1);
}

.week-switch-enter-from {
  opacity: 0;
  transform: scale(0.985);
}

.week-switch-leave-to {
  opacity: 0;
  transform: scale(1.015);
}

.grid-body::-webkit-scrollbar {
  width: 4px;
}

.grid-body::-webkit-scrollbar-thumb {
  background: #cfd8e8;
  border-radius: 999px;
}

.grid-body::-webkit-scrollbar-track {
  background: transparent;
}

@media screen and (min-width: 768px) {
  .card-name {
    font-size: 11.2px;
  }
}

@media screen and (max-width: 768px) {
  .schedule-wrap {
    margin: 10px 10px 12px;
    border-radius: 16px;
  }

  .th-corner,
  .period-col {
    width: 56px;
  }

  .course-slot {
    padding: 2px;
  }
}

@media screen and (max-width: 360px) {
  .th-day {
    font-size: 10px;
    padding: 6px 0;
  }

  .period-num {
    font-size: 10px;
  }

  .period-time {
    font-size: 7.5px;
  }

  .card-name {
    font-size: 9px;
    -webkit-line-clamp: 3;
  }

  .card-meta {
    font-size: 8.5px;
  }
}
</style>
