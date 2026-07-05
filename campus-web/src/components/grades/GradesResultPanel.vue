<template>
  <section v-if="loading" class="state-panel">
    <div class="loader"></div>
    <p class="state-text">{{ gradesText.LOADING }}</p>
  </section>

  <section v-else-if="grades.length === 0" class="state-panel state-panel--empty">
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
          <tr v-for="item in grades" :key="buildGradeKey(item)">
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
    <article v-for="item in grades" :key="buildGradeKey(item)" class="grade-card">
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
</template>

<script setup>
defineProps({
  loading: { type: Boolean, required: true },
  grades: { type: Array, required: true },
  viewMode: { type: String, required: true },
  gradesViewMode: { type: Object, required: true },
  gradesText: { type: Object, required: true },
  tableColumns: { type: Array, required: true },
  cardMetaFields: { type: Array, required: true },
  buildGradeKey: { type: Function, required: true },
  formatGradeValue: { type: Function, required: true }
})
</script>

<style scoped>
.state-panel,
.grade-card,
.grade-table-card {
  background: var(--grades-panel-bg);
  border: 1px solid var(--grades-panel-border);
  box-shadow: var(--grades-panel-shadow);
  backdrop-filter: blur(var(--grades-panel-blur));
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
.col-score,
.col-gpa,
.col-credit {
  min-width: 90px;
}

.col-teacher,
.col-nature,
.col-exam {
  min-width: 120px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media screen and (max-width: 767px) {
  .meta-grid {
    grid-template-columns: 1fr;
  }

  .grade-card__header {
    flex-direction: column;
  }

  .score-pill {
    width: 100%;
  }
}
</style>
