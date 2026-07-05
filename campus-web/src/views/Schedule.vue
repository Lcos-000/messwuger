<template>
  <div class="schedule-root" :style="scheduleRootStyle">
    <div class="schedule-wallpaper" :style="scheduleWallpaperStyle"></div>
    <div class="schedule-shell">
      <ScheduleToolbar
        :text="text"
        :current-week-label="currentWeekLabel"
        @prev="changeWeek(-1)"
        @next="changeWeek(1)"
        @this-week="setCurrentWeekToNow"
        @full-term="showFullTerm"
        @refresh="handleManualRefresh"
      />

      <div v-if="loading" class="state-screen">
        <div class="pulse-ring"></div>
        <p class="state-text">{{ text.LOADING_TEXT }}</p>
      </div>

      <div v-else-if="isSyncing" class="state-screen">
        <div class="sync-icon-wrap pulse-ring">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#4f86f7" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>
        </div>
        <p class="state-title">{{ text.SYNCING_TITLE }}</p>
        <p class="state-sub">{{ text.SYNCING_TEXT }}</p>
      </div>

      <ScheduleGrid
        v-else
        :text="text"
        :days="days"
        :period-time="periodTime"
        :total-periods="totalPeriods"
        :current-week="currentWeek"
        :course-groups="courseGroups"
        :is-mobile="isMobile"
        :empty-text="emptyText"
        :is-today-column="isTodayColumn"
        :get-slot-style="getSlotStyle"
        :get-card-style="getCardStyle"
        :get-course-color="getCourseColor"
        :get-course-color-pair="getCourseColorPair"
        :build-teacher-names="buildTeacherNames"
        @open-detail="openDetail"
        @open-stack="openStackDetail"
      />

      <ScheduleCourseDetailModal
        :course="detailCourse"
        :text="text"
        :get-course-color="getCourseColor"
        :build-teacher-names="buildTeacherNames"
        @close="closeDetail"
      />

      <ScheduleStackModal
        :group="stackGroup"
        :text="text"
        :get-course-color="getCourseColor"
        :build-teacher-names="buildTeacherNames"
        @close="closeStackDetail"
        @select-course="selectStackCourse"
      />
    </div>
  </div>
</template>

<script setup>
import ScheduleToolbar from '@/components/schedule/ScheduleToolbar.vue'
import ScheduleGrid from '@/components/schedule/ScheduleGrid.vue'
import ScheduleCourseDetailModal from '@/components/schedule/ScheduleCourseDetailModal.vue'
import ScheduleStackModal from '@/components/schedule/ScheduleStackModal.vue'
import { useSchedulePage } from '@/composables/useSchedulePage'

const {
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
} = useSchedulePage()
</script>

<style scoped>
.schedule-root {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #e9eef8;
  font-family: var(--app-font-family);
  min-height: 100vh;
  position: relative;
  width: 100%;
  overflow: hidden;
}

.schedule-shell {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 100vh;
}

.schedule-wallpaper {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.schedule-wallpaper::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(233, 238, 248, calc(var(--page-wallpaper-mask-alpha) * 0.9)) 0%,
    rgba(233, 238, 248, calc(var(--page-wallpaper-mask-alpha) * 0.36)) 32%,
    rgba(233, 238, 248, calc(var(--page-wallpaper-mask-alpha) * 0.72)) 100%
  );
}

.state-screen {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: transparent;
  position: relative;
  z-index: 1;
}

.pulse-ring {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 3px solid #e0e7ff;
  border-top-color: #4f86f7;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.state-text {
  font-size: 14px;
  color: #8a9bc0;
}

.sync-icon-wrap {
  width: 72px;
  height: 72px;
  background: #eef3ff;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.state-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a2540;
}

.state-sub {
  font-size: 13px;
  color: #8a9bc0;
}
</style>
