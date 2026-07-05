<template>
  <div class="shared-page-root grades-root" :style="gradesRootStyle">
    <div class="shared-page-backdrop grades-backdrop" :style="gradesBackdropStyle"></div>
    <div class="shared-page-shell grades-shell">
      <header class="shared-page-header grades-header">
        <div>
          <h1 class="shared-page-title grades-title">{{ gradesText.PAGE_TITLE }}</h1>
          <p class="shared-page-subtitle grades-subtitle">{{ gradesText.PAGE_SUBTITLE }}</p>
        </div>
      </header>

      <GradesFilterPanel
        :grades-text="gradesText"
        :academic-year="filters.academicYear"
        :semester="filters.semester"
        :academic-year-options="academicYearOptions"
        :semester-options="semesterOptions"
        :view-options="viewOptions"
        :sort-options="sortOptions"
        :view-mode="viewMode"
        :sort-mode="sortMode"
        :loading="loading"
        :submitting="submitting"
        :query-button-text="GRADES_CONFIG.QUERY_BUTTON_TEXT"
        :submit-button-text="GRADES_CONFIG.SUBMIT_BUTTON_TEXT"
        @update:academic-year="filters.academicYear = $event"
        @update:semester="filters.semester = $event"
        @update:view-mode="viewMode = $event"
        @update:sort-mode="sortMode = $event"
        @fetch="fetchGradesData"
        @submit="submitTask"
      />

      <GradesSummaryPanel
        :grades-text="gradesText"
        :current-summary="currentSummary"
        :record-count="displayGrades.length"
      />

      <GradesResultPanel
        :loading="loading"
        :grades="displayGrades"
        :view-mode="viewMode"
        :grades-view-mode="gradesViewMode"
        :grades-text="gradesText"
        :table-columns="tableColumns"
        :card-meta-fields="cardMetaFields"
        :build-grade-key="buildGradeKey"
        :format-grade-value="formatGradeValue"
      />
    </div>
  </div>
</template>

<script setup>
import GradesFilterPanel from '@/components/grades/GradesFilterPanel.vue'
import GradesSummaryPanel from '@/components/grades/GradesSummaryPanel.vue'
import GradesResultPanel from '@/components/grades/GradesResultPanel.vue'
import { useGradesPage } from '@/composables/useGradesPage'
import { GRADES_CONFIG } from '@/config'

const {
  gradesText,
  gradesViewMode,
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
} = useGradesPage()
</script>

<style scoped>
.grades-root {
  position: relative;
  min-height: 100%;
}

.grades-backdrop {
  background:
    radial-gradient(circle at top left, rgba(79, 134, 247, 0.16), transparent 28%),
    linear-gradient(180deg, #f6f8fc 0%, #edf2f9 100%);
}

.grades-shell {
  max-width: var(--grades-shell-max-width);
}

.grades-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.grades-title {
  color: var(--grades-text-main);
}

.grades-subtitle {
  color: var(--grades-text-sub);
}
</style>
