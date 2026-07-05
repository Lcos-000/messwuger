<template>
  <div class="empty-classroom-root" :style="pageStyle">
    <div class="empty-classroom-backdrop" :style="backdropStyle"></div>
    <div class="empty-classroom-shell">
      <header class="page-header">
        <div>
          <h1 class="page-title">{{ text.PAGE_TITLE }}</h1>
          <p class="page-subtitle">{{ text.PAGE_SUBTITLE }}</p>
        </div>
      </header>

      <section class="query-panel">
        <div class="field-grid field-grid--two">
          <div class="field-group">
            <label class="field-label">{{ text.ACADEMIC_YEAR_LABEL }}</label>
            <select v-model="filters.academicYear" class="field-select">
              <option v-for="option in academicYearOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>

          <div class="field-group">
            <label class="field-label">{{ text.CAMPUS_LABEL }}</label>
            <select v-model="filters.campusId" class="field-select">
              <option v-for="option in campusOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>
        </div>

        <div class="field-group">
          <label class="field-label">{{ text.SEMESTER_LABEL }}</label>
          <div class="segment-grid segment-grid--three">
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

        <div class="field-group">
          <label class="field-label">{{ text.DAY_LABEL }}</label>
          <div class="segment-grid segment-grid--day">
            <button
              v-for="option in dayOptions"
              :key="option.value"
              type="button"
              class="segment-btn"
              :class="{ active: filters.dayOfWeek === option.value }"
              @click="filters.dayOfWeek = option.value"
            >
              {{ option.label }}
            </button>
          </div>
        </div>

        <div class="field-group">
          <div class="field-label-row">
            <label class="field-label">{{ text.WEEK_LABEL }}</label>
            <button type="button" class="mini-link" @click="selectCurrentWeek">{{ text.CURRENT_WEEK_BUTTON }}</button>
          </div>
          <div class="chip-grid chip-grid--week">
            <button
              v-for="option in weekOptions"
              :key="option.value"
              type="button"
              class="chip-btn"
              :class="{ active: selectedWeeks.includes(option.value) }"
              @click="toggleWeek(option.value)"
            >
              {{ option.value }}
            </button>
          </div>
        </div>

        <div class="field-group">
          <label class="field-label">{{ text.PERIOD_LABEL }}</label>
          <div class="chip-grid chip-grid--period">
            <button
              v-for="option in periodOptions"
              :key="option.value"
              type="button"
              class="chip-btn"
              :class="{ active: selectedPeriods.includes(option.value) }"
              @click="togglePeriod(option.value)"
            >
              {{ option.value }}
            </button>
          </div>
        </div>

        <div class="field-grid field-grid--two">
          <div class="field-group">
            <label class="field-label">{{ text.BUILDING_LABEL }}</label>
            <select v-model="filters.building" class="field-select">
              <option v-for="option in buildingOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>

          <div class="field-group">
            <label class="field-label">{{ text.ROOM_TYPE_LABEL }}</label>
            <select v-model="filters.roomType" class="field-select">
              <option v-for="option in roomTypeOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>
        </div>

        <div class="action-row">
          <button type="button" class="primary-btn" :disabled="submitting || !canSubmit" @click="submitTask">
            {{ submitting ? '提交中...' : text.SUBMIT_BUTTON }}
          </button>
          <button type="button" class="secondary-btn" :disabled="resultLoading || !canSubmit" @click="fetchResult">
            {{ resultLoading ? '查询中...' : queryResultButtonText }}
          </button>
          <button type="button" class="secondary-btn" @click="resetFilters">
            {{ text.RESET_BUTTON }}
          </button>
        </div>
      </section>

      <section class="summary-panel">
        <div class="summary-card">
          <span class="summary-label">{{ text.SUMMARY_LABEL }}</span>
          <strong class="summary-value">{{ summaryText }}</strong>
        </div>
      </section>

      <section class="detail-panel">
        <div class="detail-card">
          <span class="detail-label">{{ text.WEEK_SELECTION_LABEL }}</span>
          <strong class="detail-value">{{ selectedWeekLabel }}</strong>
        </div>
        <div class="detail-card">
          <span class="detail-label">{{ text.PERIOD_SELECTION_LABEL }}</span>
          <strong class="detail-value">{{ selectedPeriodLabel }}</strong>
        </div>
      </section>

      <section class="status-panel" :class="statusToneClass">
        <div class="status-icon">{{ statusIcon }}</div>
        <div class="status-content">
          <span class="status-kicker">{{ text.STATUS_LABEL }}</span>
          <h2 class="status-title">{{ statusMeta.title }}</h2>
          <p class="status-desc">{{ statusMeta.description }}</p>
        </div>
      </section>

      <section v-if="resultLoading" class="result-panel result-panel--state">
        <div class="loader"></div>
        <p class="result-state-text">{{ text.RESULT_LOADING }}</p>
      </section>

      <section v-else-if="resultRows.length > 0" class="result-table-card">
        <div class="result-table-wrap">
          <table class="result-table">
            <thead>
              <tr>
                <th v-for="column in tableColumns" :key="column.key" :class="column.className">{{ column.label }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in resultRows" :key="buildRowKey(item)">
                <td v-for="column in tableColumns" :key="`${buildRowKey(item)}-${column.key}`" :class="column.className">
                  {{ formatValue(item[column.key]) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-else class="result-panel result-panel--empty">
        <p class="result-state-title">{{ text.EMPTY_RESULT_TITLE }}</p>
        <p class="result-state-text">{{ emptyResultText }}</p>
      </section>
    </div>
  </div>
</template>

<script setup>
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
  queryResultButtonText,
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
  background: #eef2f9;
}

.empty-classroom-backdrop {
  position: fixed;
  inset: 0;
  pointer-events: none;
}

.empty-classroom-shell {
  position: relative;
  z-index: 1;
  max-width: var(--empty-shell-max-width);
  margin: 0 auto;
  padding: 20px 16px 28px;
}

.page-header {
  margin-bottom: 16px;
}

.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: var(--empty-text-main);
}

.page-subtitle {
  margin: 6px 0 0;
  font-size: 14px;
  color: var(--empty-text-sub);
}

.query-panel,
.summary-panel,
.detail-panel,
.status-panel,
.result-panel,
.result-table-card {
  background: var(--empty-panel-bg);
  border: 1px solid var(--empty-panel-border);
  box-shadow: var(--empty-panel-shadow);
  backdrop-filter: blur(var(--empty-panel-blur));
  border-radius: var(--empty-panel-radius);
}

.query-panel {
  padding: 18px;
  display: grid;
  gap: 16px;
}

.field-grid {
  display: grid;
  gap: 14px;
}

.field-grid--two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.field-group {
  display: grid;
  gap: 10px;
}

.field-label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.field-label {
  font-size: 13px;
  font-weight: 600;
  color: #56657f;
}

.mini-link {
  color: var(--empty-primary);
  font-size: 12px;
  font-weight: 600;
}

.field-select {
  width: 100%;
  height: 44px;
  border-radius: var(--empty-control-radius);
  border: 1px solid #d6deed;
  background: #fff;
  padding: 0 14px;
  font-size: 14px;
  color: var(--empty-text-main);
}

.segment-grid,
.chip-grid {
  display: grid;
  gap: 8px;
}

.segment-grid--three {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.segment-grid--day {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.chip-grid--week {
  grid-template-columns: repeat(5, minmax(0, 1fr));
}

.chip-grid--period {
  grid-template-columns: repeat(7, minmax(0, 1fr));
}

.segment-btn,
.chip-btn {
  border: 1px solid var(--empty-chip-border);
  background: var(--empty-chip-bg);
  color: #516079;
  transition: all 0.18s ease;
}

.segment-btn {
  min-height: 42px;
  border-radius: var(--empty-control-radius);
  padding: 0 12px;
  font-size: 14px;
  font-weight: 600;
}

.segment-btn--rich {
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
}

.segment-btn__sub {
  font-size: 12px;
  color: var(--empty-text-muted);
}

.chip-btn {
  height: 38px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
}

.segment-btn.active,
.chip-btn.active {
  border-color: var(--empty-primary);
  background: var(--empty-primary-surface);
  color: var(--empty-primary-text);
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
  border-radius: var(--empty-control-radius);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.primary-btn {
  background: var(--empty-primary);
  color: #fff;
}

.secondary-btn {
  background: var(--empty-secondary-surface);
  color: var(--empty-secondary-text);
}

.primary-btn:disabled,
.secondary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.summary-panel,
.detail-panel {
  margin-top: 14px;
  padding: 14px 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.summary-panel {
  grid-template-columns: 1fr;
}

.summary-card,
.detail-card {
  display: grid;
  gap: 6px;
}

.summary-label,
.detail-label,
.status-kicker {
  font-size: 12px;
  color: var(--empty-text-muted);
}

.summary-value,
.detail-value {
  font-size: 16px;
  color: var(--empty-text-main);
}

.status-panel {
  margin-top: 14px;
  padding: 18px;
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.status-panel--default {
  background: rgba(255, 255, 255, 0.82);
}

.status-panel--success {
  background: rgba(242, 251, 246, 0.86);
}

.status-panel--primary {
  background: rgba(238, 244, 255, 0.86);
}

.status-panel--info {
  background: rgba(242, 247, 255, 0.86);
}

.status-panel--warn {
  background: rgba(255, 248, 237, 0.88);
}

.status-panel--danger {
  background: rgba(255, 243, 243, 0.88);
}

.status-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.72);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  color: var(--empty-primary);
  flex-shrink: 0;
}

.status-content {
  display: grid;
  gap: 4px;
}

.status-title {
  margin: 0;
  font-size: 18px;
  color: var(--empty-text-main);
}

.status-desc {
  margin: 0;
  font-size: 14px;
  color: var(--empty-text-sub);
  line-height: 1.6;
}

.result-panel {
  margin-top: 14px;
  min-height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 24px;
  text-align: center;
}

.result-state-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--empty-text-main);
}

.result-state-text {
  margin: 0;
  font-size: 14px;
  color: var(--empty-text-sub);
}

.loader {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 3px solid #dce7fb;
  border-top-color: var(--empty-primary);
  animation: spin 0.8s linear infinite;
}

.result-table-card {
  margin-top: 14px;
  overflow: hidden;
}

.result-table-wrap {
  width: 100%;
  overflow-x: auto;
}

.result-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 980px;
}

.result-table thead th {
  padding: 14px 12px;
  background: rgba(244, 247, 253, 0.96);
  border-bottom: 1px solid #dfe8f7;
  color: #52627d;
  font-size: 13px;
  font-weight: 700;
  text-align: left;
  white-space: nowrap;
}

.result-table tbody td {
  padding: 14px 12px;
  border-bottom: 1px solid #e8eef8;
  border-right: 1px solid #edf2fb;
  font-size: 14px;
  color: #22324b;
  background: rgba(255, 255, 255, 0.75);
  vertical-align: middle;
}

.result-table tbody tr:last-child td {
  border-bottom: none;
}

.result-table th:last-child,
.result-table td:last-child {
  border-right: none;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media screen and (max-width: 767px) {
  .empty-classroom-shell {
    padding: 16px 12px 20px;
  }

  .page-title {
    font-size: 24px;
  }

  .field-grid--two,
  .summary-panel,
  .detail-panel,
  .segment-grid--three,
  .segment-grid--day,
  .chip-grid--week,
  .chip-grid--period {
    grid-template-columns: 1fr;
  }

  .action-row {
    display: grid;
    grid-template-columns: 1fr;
  }

  .status-panel {
    flex-direction: column;
  }
}
</style>
