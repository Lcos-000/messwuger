<template>
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
</template>

<script setup>
defineProps({
  text: { type: Object, required: true },
  resultLoading: { type: Boolean, required: true },
  resultRows: { type: Array, required: true },
  tableColumns: { type: Array, required: true },
  emptyResultText: { type: String, required: true },
  buildRowKey: { type: Function, required: true },
  formatValue: { type: Function, required: true }
})
</script>

<style scoped>
.result-panel,
.result-table-card {
  margin-top: 14px;
  background: var(--empty-panel-bg);
  border: 1px solid var(--empty-panel-border);
  box-shadow: var(--empty-panel-shadow);
  backdrop-filter: blur(var(--empty-panel-blur));
  border-radius: var(--empty-panel-radius);
}

.result-panel {
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
  overflow: hidden;
}

.result-table-wrap {
  width: 100%;
  overflow-x: auto;
}

.result-table {
  width: 100%;
  border-collapse: collapse;
  min-width: var(--empty-table-min-width);
}

.result-table thead th {
  padding: 14px 12px;
  background: var(--empty-table-head-bg);
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
  color: var(--empty-table-text);
  background: var(--empty-table-row-bg);
  vertical-align: middle;
}

.result-table tbody tr:last-child td {
  border-bottom: none;
}

.result-table th:last-child,
.result-table td:last-child {
  border-right: none;
}

.col-building,
.col-campus,
.col-floor,
.col-capacity,
.col-real-capacity {
  white-space: nowrap;
}

.col-remark {
  min-width: 180px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
