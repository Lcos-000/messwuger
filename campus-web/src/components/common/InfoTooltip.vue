<template>
  <span class="info-tooltip-root">
    <button
      ref="triggerRef"
      type="button"
      class="info-tooltip-trigger"
      :aria-label="text"
      @mouseenter="showTooltip"
      @mouseleave="hideTooltip"
      @focus="showTooltip"
      @blur="hideTooltip"
      @click.stop="toggleTooltip"
      @keydown.esc="hideTooltip"
    >i</button>

    <Teleport to="body">
      <div
        v-if="visible"
        ref="tooltipRef"
        class="info-tooltip-floating"
        :style="tooltipStyle"
        role="tooltip"
      >{{ text }}</div>
    </Teleport>
  </span>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'

defineProps({
  text: {
    type: String,
    required: true
  }
})

const TOOLTIP_MARGIN = 12
const TOOLTIP_GAP = 10
const TOOLTIP_MAX_WIDTH = 300

const visible = ref(false)
const triggerRef = ref(null)
const tooltipRef = ref(null)
const tooltipStyle = ref({})

const clamp = (value, min, max) => Math.min(Math.max(value, min), max)

const updatePosition = async () => {
  if (!visible.value || !triggerRef.value) return

  await nextTick()
  const triggerRect = triggerRef.value.getBoundingClientRect()
  const tooltipRect = tooltipRef.value?.getBoundingClientRect()
  if (!tooltipRect) return

  const viewportWidth = window.innerWidth
  const viewportHeight = window.innerHeight
  const maxWidth = Math.min(TOOLTIP_MAX_WIDTH, viewportWidth - TOOLTIP_MARGIN * 2)
  const tooltipWidth = Math.min(tooltipRect.width, maxWidth)
  const hasTopSpace = triggerRect.top >= tooltipRect.height + TOOLTIP_GAP + TOOLTIP_MARGIN
  const top = hasTopSpace
    ? triggerRect.top - tooltipRect.height - TOOLTIP_GAP
    : Math.min(triggerRect.bottom + TOOLTIP_GAP, viewportHeight - tooltipRect.height - TOOLTIP_MARGIN)
  const left = clamp(
    triggerRect.left + triggerRect.width / 2 - tooltipWidth / 2,
    TOOLTIP_MARGIN,
    viewportWidth - tooltipWidth - TOOLTIP_MARGIN
  )

  tooltipStyle.value = {
    top: `${Math.max(TOOLTIP_MARGIN, top)}px`,
    left: `${left}px`,
    maxWidth: `${maxWidth}px`
  }
}

const showTooltip = () => {
  visible.value = true
  updatePosition()
}

const hideTooltip = () => {
  visible.value = false
}

const toggleTooltip = () => {
  visible.value = !visible.value
  if (visible.value) updatePosition()
}

const handleOutsidePointer = (event) => {
  if (!visible.value) return
  if (triggerRef.value?.contains(event.target)) return
  if (tooltipRef.value?.contains(event.target)) return
  hideTooltip()
}

onMounted(() => {
  document.addEventListener('pointerdown', handleOutsidePointer, true)
  window.addEventListener('resize', updatePosition)
  window.addEventListener('scroll', updatePosition, true)
})

onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', handleOutsidePointer, true)
  window.removeEventListener('resize', updatePosition)
  window.removeEventListener('scroll', updatePosition, true)
})
</script>

<style scoped>
.info-tooltip-root {
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
}

.info-tooltip-trigger {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #2f67da;
  background: rgba(79, 134, 247, 0.1);
  border: 1px solid rgba(79, 134, 247, 0.18);
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  cursor: help;
  transition: background 0.15s ease, border-color 0.15s ease, color 0.15s ease;
}

.info-tooltip-trigger:hover,
.info-tooltip-trigger:focus-visible {
  border-color: rgba(79, 134, 247, 0.28);
  background: rgba(79, 134, 247, 0.14);
}

.info-tooltip-trigger:focus-visible {
  outline: 2px solid rgba(79, 134, 247, 0.28);
  outline-offset: 2px;
}

.info-tooltip-floating {
  position: fixed;
  z-index: 9999;
  width: max-content;
  padding: 9px 11px;
  border-radius: 10px;
  background: rgba(20, 32, 51, 0.94);
  color: #fff;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.55;
  white-space: normal;
  box-shadow: 0 10px 24px rgba(20, 32, 51, 0.18);
  pointer-events: none;
}
</style>
