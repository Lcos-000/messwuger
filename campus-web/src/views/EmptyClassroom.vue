<template>
  <div class="shared-page-root empty-classroom-root" :style="pageStyle">
    <div class="shared-page-backdrop empty-classroom-backdrop" :style="backdropStyle"></div>
    <div class="shared-page-shell empty-classroom-shell">
      <header class="shared-page-header page-header">
        <div>
          <h1 class="shared-page-title page-title">{{ text.PAGE_TITLE }}</h1>
          <p class="shared-page-subtitle page-subtitle">{{ text.PAGE_SUBTITLE }}</p>
        </div>
      </header>

      <EmptyClassroomQueryPanel
        :text="text"
        :academic-year-options="academicYearOptions"
        :semester-options="semesterOptions"
        :day-options="dayOptions"
        :campus-options="campusOptions"
        :room-type-options="roomTypeOptions"
        :week-options="weekOptions"
        :period-options="periodOptions"
        :filters="filters"
        :selected-weeks="selectedWeeks"
        :selected-periods="selectedPeriods"
        :building-options="buildingOptions"
        :submitting="submitting"
        :result-loading="resultLoading"
        :can-submit="canSubmit"
        @update:academic-year="filters.academicYear = $event"
        @update:semester="filters.semester = $event"
        @update:day-of-week="filters.dayOfWeek = $event"
        @update:campus-id="filters.campusId = $event"
        @update:building="filters.building = $event"
        @update:room-type="filters.roomType = $event"
        @toggle-week="toggleWeek"
        @toggle-period="togglePeriod"
        @select-current-week="selectCurrentWeek"
        @submit="submitTask"
        @fetch-result="fetchResult"
        @reset="resetFilters"
      />

      <EmptyClassroomSummaryPanel
        :text="text"
        :summary-text="summaryText"
        :selected-week-label="selectedWeekLabel"
        :selected-period-label="selectedPeriodLabel"
      />

      <EmptyClassroomStatusPanel
        :text="text"
        :status-meta="statusMeta"
        :status-tone-class="statusToneClass"
        :status-icon="statusIcon"
      />

      <EmptyClassroomResultPanel
        :text="text"
        :result-loading="resultLoading"
        :result-rows="resultRows"
        :table-columns="tableColumns"
        :empty-result-text="emptyResultText"
        :build-row-key="buildRowKey"
        :format-value="formatValue"
      />
    </div>
  </div>
</template>

<script setup>
import EmptyClassroomQueryPanel from '@/components/emptyClassroom/EmptyClassroomQueryPanel.vue'
import EmptyClassroomSummaryPanel from '@/components/emptyClassroom/EmptyClassroomSummaryPanel.vue'
import EmptyClassroomStatusPanel from '@/components/emptyClassroom/EmptyClassroomStatusPanel.vue'
import EmptyClassroomResultPanel from '@/components/emptyClassroom/EmptyClassroomResultPanel.vue'
import { useEmptyClassroom } from '@/composables/useEmptyClassroom'

const {
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
} = useEmptyClassroom()
</script>

<style scoped>
.empty-classroom-root {
  position: relative;
  min-height: 100%;
}

.empty-classroom-shell {
  max-width: var(--empty-shell-max-width);
}

.page-title {
  color: var(--empty-text-main);
}

.page-subtitle {
  color: var(--empty-text-sub);
}
</style>
