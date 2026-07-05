<template>
  <section class="filter-panel">
    <div class="field-group">
      <label class="field-label">{{ gradesText.ACADEMIC_YEAR_LABEL }}</label>
      <select :value="academicYear" class="field-select" @change="emit('update:academic-year', $event.target.value)">
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
          :class="{ active: semester === option.value }"
          @click="emit('update:semester', option.value)"
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
            @click="emit('update:view-mode', option.value)"
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
            @click="emit('update:sort-mode', option.value)"
          >
            {{ option.label }}
          </button>
        </div>
      </div>
    </div>

    <div class="action-row">
      <button type="button" class="primary-btn" :disabled="loading" @click="emit('fetch')">
        {{ loading ? gradesText.QUERYING_BUTTON_TEXT : queryButtonText }}
      </button>
      <button type="button" class="secondary-btn" :disabled="submitting" @click="emit('submit')">
        {{ submitting ? gradesText.SUBMITTING_BUTTON_TEXT : submitButtonText }}
      </button>
    </div>
  </section>
</template>

<script setup>
defineProps({
  gradesText: { type: Object, required: true },
  academicYear: { type: String, required: true },
  semester: { type: String, required: true },
  academicYearOptions: { type: Array, required: true },
  semesterOptions: { type: Array, required: true },
  viewOptions: { type: Array, required: true },
  sortOptions: { type: Array, required: true },
  viewMode: { type: String, required: true },
  sortMode: { type: String, required: true },
  loading: { type: Boolean, required: true },
  submitting: { type: Boolean, required: true },
  queryButtonText: { type: String, required: true },
  submitButtonText: { type: String, required: true }
})

const emit = defineEmits([
  'update:academic-year',
  'update:semester',
  'update:view-mode',
  'update:sort-mode',
  'fetch',
  'submit'
])
</script>

<style scoped>
.filter-panel {
  background: var(--grades-panel-bg);
  border: 1px solid var(--grades-panel-border);
  box-shadow: var(--grades-panel-shadow);
  backdrop-filter: blur(var(--grades-panel-blur));
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
  background: rgba(255, 255, 255, 0.92);
  padding: 0 14px;
  font-size: 14px;
  color: var(--grades-text-main);
  outline: none;
}

.field-select:focus {
  border-color: var(--grades-primary);
  box-shadow: 0 0 0 3px rgba(79, 134, 247, 0.12);
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

.segment-btn:hover {
  border-color: #bfd0f3;
  background: #f2f7ff;
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
  transition: transform 0.18s ease, box-shadow 0.18s ease, opacity 0.18s ease;
}

.primary-btn:hover,
.secondary-btn:hover {
  transform: translateY(-1px);
}

.primary-btn {
  background: var(--grades-primary);
  color: #fff;
  box-shadow: 0 8px 18px rgba(79, 134, 247, 0.24);
}

.secondary-btn {
  background: var(--grades-secondary-surface);
  color: var(--grades-secondary-text);
}

.primary-btn:disabled,
.secondary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

@media screen and (max-width: 767px) {
  .view-toolbar,
  .segmented-control--semester,
  .action-row {
    grid-template-columns: 1fr;
  }

  .action-row {
    display: grid;
  }
}
</style>
