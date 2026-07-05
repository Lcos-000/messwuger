<template>
  <section class="query-panel">
    <div class="field-grid field-grid--two">
      <div class="field-group">
        <label class="field-label">{{ text.ACADEMIC_YEAR_LABEL }}</label>
        <select :value="filters.academicYear" class="field-select" @change="emit('update:academic-year', $event.target.value)">
          <option v-for="option in academicYearOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </div>

      <div class="field-group">
        <label class="field-label">{{ text.CAMPUS_LABEL }}</label>
        <select :value="filters.campusId" class="field-select" @change="emit('update:campus-id', $event.target.value)">
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
          @click="emit('update:semester', option.value)"
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
          @click="emit('update:day-of-week', option.value)"
        >
          {{ option.label }}
        </button>
      </div>
    </div>

    <div class="field-group">
      <div class="field-label-row">
        <label class="field-label">{{ text.WEEK_LABEL }}</label>
        <button type="button" class="mini-link" @click="emit('select-current-week')">{{ text.CURRENT_WEEK_BUTTON }}</button>
      </div>
      <div class="chip-grid chip-grid--week">
        <button
          v-for="option in weekOptions"
          :key="option.value"
          type="button"
          class="chip-btn"
          :class="{ active: selectedWeeks.includes(option.value) }"
          @click="emit('toggle-week', option.value)"
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
          @click="emit('toggle-period', option.value)"
        >
          {{ option.value }}
        </button>
      </div>
    </div>

    <div class="field-grid field-grid--two">
      <div class="field-group">
        <label class="field-label">{{ text.BUILDING_LABEL }}</label>
        <select :value="filters.building" class="field-select" @change="emit('update:building', $event.target.value)">
          <option v-for="option in buildingOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </div>

      <div class="field-group">
        <label class="field-label">{{ text.ROOM_TYPE_LABEL }}</label>
        <select :value="filters.roomType" class="field-select" @change="emit('update:room-type', $event.target.value)">
          <option v-for="option in roomTypeOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </div>
    </div>

    <div class="action-row">
      <div class="action-item">
        <button type="button" class="primary-btn" :disabled="submitting || !canSubmit" @click="emit('submit')">
          {{ submitting ? text.SUBMITTING_BUTTON_TEXT : text.SUBMIT_BUTTON }}
        </button>
      </div>
      <div class="action-item">
        <button type="button" class="secondary-btn" :disabled="resultLoading || !canSubmit" @click="emit('fetch-result')">
          {{ resultLoading ? text.QUERYING_RESULT_BUTTON_TEXT : text.RESULT_QUERY_BUTTON }}
        </button>
      </div>
      <div class="action-item">
        <button type="button" class="secondary-btn" @click="emit('reset')">
          {{ text.RESET_BUTTON }}
        </button>
        <InfoTooltip :text="text.ACTION_BUTTON_TIP" />
      </div>
    </div>
  </section>
</template>

<script setup>
import InfoTooltip from '@/components/common/InfoTooltip.vue'

defineProps({
  text: { type: Object, required: true },
  academicYearOptions: { type: Array, required: true },
  semesterOptions: { type: Array, required: true },
  dayOptions: { type: Array, required: true },
  campusOptions: { type: Array, required: true },
  roomTypeOptions: { type: Array, required: true },
  weekOptions: { type: Array, required: true },
  periodOptions: { type: Array, required: true },
  filters: { type: Object, required: true },
  selectedWeeks: { type: Array, required: true },
  selectedPeriods: { type: Array, required: true },
  buildingOptions: { type: Array, required: true },
  submitting: { type: Boolean, required: true },
  resultLoading: { type: Boolean, required: true },
  canSubmit: { type: Boolean, required: true }
})

const emit = defineEmits([
  'update:academic-year',
  'update:semester',
  'update:day-of-week',
  'update:campus-id',
  'update:building',
  'update:room-type',
  'toggle-week',
  'toggle-period',
  'select-current-week',
  'submit',
  'fetch-result',
  'reset'
])
</script>

<style scoped>
.query-panel {
  background: var(--empty-panel-bg);
  border: 1px solid var(--empty-panel-border);
  box-shadow: var(--empty-panel-shadow);
  backdrop-filter: blur(var(--empty-panel-blur));
  border-radius: var(--empty-panel-radius);
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
  background: transparent;
  border: none;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.field-select {
  width: 100%;
  height: 44px;
  border-radius: var(--empty-control-radius);
  border: 1px solid #d6deed;
  background: rgba(255, 255, 255, 0.92);
  padding: 0 14px;
  font-size: 14px;
  color: var(--empty-text-main);
  outline: none;
}

.field-select:focus {
  border-color: var(--empty-primary);
  box-shadow: 0 0 0 3px rgba(79, 134, 247, 0.12);
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

.segment-btn:hover,
.chip-btn:hover {
  border-color: #bfd0f3;
  background: #f2f7ff;
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

.action-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
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
  transition: transform 0.18s ease, box-shadow 0.18s ease, opacity 0.18s ease;
}

.primary-btn:hover,
.secondary-btn:hover {
  transform: translateY(-1px);
}

.primary-btn {
  background: var(--empty-primary);
  color: #fff;
  box-shadow: 0 8px 18px rgba(79, 134, 247, 0.24);
}

.secondary-btn {
  background: var(--empty-secondary-surface);
  color: var(--empty-secondary-text);
}

.primary-btn:disabled,
.secondary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

@media screen and (max-width: 767px) {
  .field-grid--two,
  .segment-grid--three,
  .segment-grid--day,
  .chip-grid--week,
  .chip-grid--period,
  .action-row {
    grid-template-columns: 1fr;
  }

  .action-row {
    display: grid;
  }

  .action-item {
    display: flex;
  }

  .primary-btn,
  .secondary-btn {
    flex: 1;
  }
}
</style>
