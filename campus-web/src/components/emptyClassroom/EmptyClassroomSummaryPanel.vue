<template>
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
</template>

<script setup>
defineProps({
  text: { type: Object, required: true },
  summaryText: { type: String, required: true },
  selectedWeekLabel: { type: String, required: true },
  selectedPeriodLabel: { type: String, required: true }
})
</script>

<style scoped>
.summary-panel,
.detail-panel {
  margin-top: 14px;
  padding: 14px 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  background: var(--empty-panel-bg);
  border: 1px solid var(--empty-panel-border);
  box-shadow: var(--empty-panel-shadow);
  backdrop-filter: blur(var(--empty-panel-blur));
  border-radius: var(--empty-panel-radius);
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
.detail-label {
  font-size: 12px;
  color: var(--empty-text-muted);
}

.summary-value,
.detail-value {
  font-size: 16px;
  color: var(--empty-text-main);
  line-height: 1.5;
}

@media screen and (max-width: 767px) {
  .detail-panel {
    grid-template-columns: 1fr;
  }
}
</style>
