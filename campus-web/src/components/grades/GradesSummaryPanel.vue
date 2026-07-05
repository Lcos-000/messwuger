<template>
  <section class="summary-panel">
    <div class="summary-item">
      <span class="summary-label">{{ gradesText.CURRENT_FILTER_LABEL }}</span>
      <strong class="summary-value">{{ currentSummary }}</strong>
    </div>
    <div class="summary-item summary-item--count">
      <span class="summary-label">{{ gradesText.RECORD_COUNT_LABEL }}</span>
      <strong class="summary-value summary-value--count">{{ recordCount }}</strong>
    </div>
  </section>
</template>

<script setup>
defineProps({
  gradesText: { type: Object, required: true },
  currentSummary: { type: String, required: true },
  recordCount: { type: Number, required: true }
})
</script>

<style scoped>
.summary-panel {
  margin-top: 14px;
  background: var(--grades-panel-bg);
  border: 1px solid var(--grades-panel-border);
  box-shadow: var(--grades-panel-shadow);
  backdrop-filter: blur(var(--grades-panel-blur));
  border-radius: var(--grades-panel-radius);
  padding: 14px 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.summary-item {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.summary-item--count {
  justify-items: end;
}

.summary-label {
  font-size: 12px;
  color: var(--grades-text-muted);
}

.summary-value {
  font-size: 16px;
  color: var(--grades-text-main);
  line-height: 1.4;
  word-break: break-word;
}

.summary-value--count {
  font-size: 24px;
  font-weight: 700;
  color: var(--grades-primary-text);
}

@media screen and (max-width: 767px) {
  .summary-panel {
    grid-template-columns: 1fr;
  }

  .summary-item--count {
    justify-items: start;
  }
}
</style>
