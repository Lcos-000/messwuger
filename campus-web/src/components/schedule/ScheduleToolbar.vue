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
  height: 54px;
  margin: 14px 16px 0;
  padding: 0 14px;
  background: transparent;
  border: 1px solid rgba(214, 222, 235, 0.36);
  border-radius: 18px;
  box-shadow: none;
  flex-shrink: 0;
  gap: 8px;
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
  color: #223047;
  letter-spacing: 0.3px;
}

.icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #54719f;
  background: rgba(84, 113, 159, 0.1);
  border: 1px solid rgba(84, 113, 159, 0.08);
  transition: background 0.15s, border-color 0.15s, transform 0.12s;
}

.icon-btn:hover {
  background: rgba(84, 113, 159, 0.14);
  border-color: rgba(84, 113, 159, 0.14);
}

.icon-btn:active {
  transform: scale(0.96);
}

.text-chip {
  height: 28px;
  padding: 0 11px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 650;
  color: #54719f;
  background: rgba(84, 113, 159, 0.1);
  border: 1px solid rgba(84, 113, 159, 0.08);
  transition: background 0.15s, border-color 0.15s, transform 0.12s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.text-chip:hover {
  background: rgba(84, 113, 159, 0.14);
  border-color: rgba(84, 113, 159, 0.14);
}

.text-chip:active {
  transform: scale(0.98);
}

.refresh-chip {
  color: #54719f;
}

@media screen and (max-width: 360px) {
  .top-bar {
    height: 48px;
    margin: 10px 10px 0;
    padding: 0 8px;
    border-radius: 16px;
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
