<template>
  <header class="top-bar">
    <div class="top-bar__left">
      <button class="icon-btn" :title="text.PREV_WEEK_TITLE" @click="emit('prev')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
    </div>
    <div class="top-bar__center">
      <span class="week-label">{{ currentWeekLabel }}</span>
    </div>
    <div class="top-bar__right">
      <button class="text-chip" @click="emit('this-week')">{{ text.THIS_WEEK_BUTTON }}</button>
      <button class="text-chip" @click="emit('full-term')">{{ text.FULL_TERM_BUTTON }}</button>
      <button class="text-chip refresh-chip" @click="emit('refresh')">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
        {{ text.REFRESH_BUTTON }}
      </button>
      <button class="icon-btn" :title="text.NEXT_WEEK_TITLE" @click="emit('next')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
      </button>
    </div>
  </header>
</template>

<script setup>
defineProps({
  text: { type: Object, required: true },
  currentWeekLabel: { type: String, required: true }
})

const emit = defineEmits(['prev', 'next', 'this-week', 'full-term', 'refresh'])
</script>

<style scoped>
.top-bar {
  display: flex;
  align-items: center;
  padding: 0 12px;
  height: 52px;
  background: rgba(255, 255, 255, 0.78);
  border-bottom: 1px solid #e8edf5;
  box-shadow: 0 1px 8px rgba(79, 134, 247, 0.08);
  flex-shrink: 0;
  gap: 8px;
  backdrop-filter: blur(14px) saturate(1.05);
}

.top-bar__left,
.top-bar__right {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.top-bar__center {
  flex: 1;
  text-align: center;
}

.week-label {
  font-size: 15px;
  font-weight: 700;
  color: #1a2540;
  letter-spacing: 0.3px;
}

.icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4f86f7;
  background: #eef3ff;
  transition: background 0.15s;
}

.icon-btn:active {
  background: #d6e4ff;
}

.text-chip {
  height: 28px;
  padding: 0 10px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 500;
  color: #4f86f7;
  background: #eef3ff;
  transition: background 0.15s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.text-chip:active {
  background: #d6e4ff;
}

.refresh-chip {
  color: #4f86f7;
}

@media screen and (max-width: 360px) {
  .top-bar {
    height: 48px;
    padding: 0 8px;
  }

  .week-label {
    font-size: 14px;
  }

  .icon-btn {
    width: 28px;
    height: 28px;
  }

  .text-chip {
    height: 24px;
    padding: 0 7px;
    font-size: 11px;
  }
}
</style>
