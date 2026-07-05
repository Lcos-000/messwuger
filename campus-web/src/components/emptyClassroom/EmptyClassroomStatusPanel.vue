<template>
  <section class="status-panel" :class="statusToneClass">
    <div class="status-icon">{{ statusIcon }}</div>
    <div class="status-content">
      <span class="status-kicker">{{ text.STATUS_LABEL }}</span>
      <h2 class="status-title">{{ statusMeta.title }}</h2>
      <p class="status-desc">{{ statusMeta.description }}</p>
    </div>
  </section>
</template>

<script setup>
defineProps({
  text: { type: Object, required: true },
  statusMeta: { type: Object, required: true },
  statusToneClass: { type: String, required: true },
  statusIcon: { type: String, required: true }
})
</script>

<style scoped>
.status-panel {
  margin-top: 14px;
  padding: 18px;
  display: flex;
  gap: 14px;
  align-items: flex-start;
  background: var(--empty-panel-bg);
  border: 1px solid var(--empty-panel-border);
  box-shadow: var(--empty-panel-shadow);
  backdrop-filter: blur(var(--empty-panel-blur));
  border-radius: var(--empty-panel-radius);
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

.status-kicker {
  font-size: 12px;
  color: var(--empty-text-muted);
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

@media screen and (max-width: 767px) {
  .status-panel {
    flex-direction: column;
  }
}
</style>
