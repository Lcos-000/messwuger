<template>
  <transition name="crop-fade">
    <div v-if="visible" class="crop-modal" @click.self="emit('cancel')">
      <div class="crop-dialog">
        <div class="crop-header">
          <div>
            <h3 class="crop-title">{{ title }}</h3>
            <p class="crop-hint">{{ hint }}</p>
          </div>
          <button type="button" class="crop-close" @click="emit('cancel')">✕</button>
        </div>

        <div class="crop-body">
          <div
            ref="viewportRef"
            class="crop-viewport"
            :style="viewportStyle"
            @pointerdown="startDrag"
            @wheel.prevent="handleWheel"
          >
            <img
              v-if="sourceUrl"
              ref="imageRef"
              class="crop-image"
              :src="sourceUrl"
              alt="待裁剪图片"
              draggable="false"
              @load="handleImageLoad"
              :style="imageStyle"
            />
          </div>

          <div class="crop-controls">
            <div class="crop-scale-row">
              <span class="crop-scale-label">缩放</span>
              <input
                v-model.number="currentScale"
                class="crop-scale-slider"
                type="range"
                :min="minScale"
                :max="maxScale"
                :step="0.01"
                @input="handleScaleInput"
              />
              <span class="crop-scale-value">{{ Math.round(currentScale * 100) }}%</span>
            </div>
          </div>
        </div>

        <div class="crop-actions">
          <button type="button" class="crop-btn crop-btn-secondary" @click="emit('cancel')">取消</button>
          <button type="button" class="crop-btn crop-btn-primary" :disabled="confirming || !imageReady" @click="confirmCrop">
            {{ confirming ? '处理中...' : '确认上传' }}
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  sourceUrl: { type: String, default: '' },
  title: { type: String, default: '裁剪图片' },
  hint: { type: String, default: '拖动图片调整位置，滑动缩放控制取景范围。' },
  aspectRatio: { type: Number, default: 1 },
  maxViewportWidth: { type: Number, default: 280 },
  maxViewportHeight: { type: Number, default: 280 },
  outputWidth: { type: Number, default: 720 },
  outputHeight: { type: Number, default: 720 },
  fileName: { type: String, default: 'cropped-image.jpg' },
  mimeType: { type: String, default: 'image/jpeg' },
  confirming: { type: Boolean, default: false }
})

const emit = defineEmits(['cancel', 'confirm'])

const viewportRef = ref(null)
const imageRef = ref(null)
const naturalWidth = ref(0)
const naturalHeight = ref(0)
const viewportWidth = ref(240)
const viewportHeight = ref(240)
const currentScale = ref(1)
const minScale = ref(1)
const maxScale = ref(3)
const offsetX = ref(0)
const offsetY = ref(0)
const imageReady = ref(false)

const dragState = {
  active: false,
  startX: 0,
  startY: 0,
  originX: 0,
  originY: 0
}

const viewportStyle = computed(() => ({
  width: `${viewportWidth.value}px`,
  height: `${viewportHeight.value}px`
}))

const displayedWidth = computed(() => naturalWidth.value * currentScale.value)
const displayedHeight = computed(() => naturalHeight.value * currentScale.value)

const imageStyle = computed(() => ({
  width: `${displayedWidth.value}px`,
  height: `${displayedHeight.value}px`,
  left: `calc(50% - ${displayedWidth.value / 2}px + ${offsetX.value}px)`,
  top: `calc(50% - ${displayedHeight.value / 2}px + ${offsetY.value}px)`
}))

const updateViewportSize = () => {
  const maxWidth = Math.min(props.maxViewportWidth, window.innerWidth - 56)
  const maxHeight = Math.min(props.maxViewportHeight, window.innerHeight - 260)

  let width = maxWidth
  let height = width / props.aspectRatio

  if (height > maxHeight) {
    height = maxHeight
    width = height * props.aspectRatio
  }

  viewportWidth.value = Math.max(160, width)
  viewportHeight.value = Math.max(160 / props.aspectRatio, height)
}

const clampOffsets = () => {
  const overflowX = Math.max(0, (displayedWidth.value - viewportWidth.value) / 2)
  const overflowY = Math.max(0, (displayedHeight.value - viewportHeight.value) / 2)

  offsetX.value = Math.min(overflowX, Math.max(-overflowX, offsetX.value))
  offsetY.value = Math.min(overflowY, Math.max(-overflowY, offsetY.value))
}

const initializeCrop = () => {
  if (!naturalWidth.value || !naturalHeight.value) return

  const scaleX = viewportWidth.value / naturalWidth.value
  const scaleY = viewportHeight.value / naturalHeight.value
  minScale.value = Math.max(scaleX, scaleY)
  maxScale.value = Math.max(minScale.value * 3, minScale.value + 1)
  currentScale.value = minScale.value
  offsetX.value = 0
  offsetY.value = 0
  clampOffsets()
  imageReady.value = true
}

const handleImageLoad = async (event) => {
  naturalWidth.value = event.target.naturalWidth
  naturalHeight.value = event.target.naturalHeight
  await nextTick()
  initializeCrop()
}

const applyScaleAroundPoint = (nextScale, anchorX, anchorY) => {
  const previousScale = currentScale.value
  const clampedScale = Math.min(maxScale.value, Math.max(minScale.value, nextScale))
  if (clampedScale === previousScale) return

  const previousWidth = naturalWidth.value * previousScale
  const previousHeight = naturalHeight.value * previousScale
  const nextWidth = naturalWidth.value * clampedScale
  const nextHeight = naturalHeight.value * clampedScale
  const previousLeft = (viewportWidth.value - previousWidth) / 2 + offsetX.value
  const previousTop = (viewportHeight.value - previousHeight) / 2 + offsetY.value
  const pointRatioX = previousWidth === 0 ? 0.5 : (anchorX - previousLeft) / previousWidth
  const pointRatioY = previousHeight === 0 ? 0.5 : (anchorY - previousTop) / previousHeight
  const nextLeft = anchorX - nextWidth * pointRatioX
  const nextTop = anchorY - nextHeight * pointRatioY

  currentScale.value = clampedScale
  offsetX.value = nextLeft - (viewportWidth.value - nextWidth) / 2
  offsetY.value = nextTop - (viewportHeight.value - nextHeight) / 2
  clampOffsets()
}

const handleScaleInput = () => {
  applyScaleAroundPoint(currentScale.value, viewportWidth.value / 2, viewportHeight.value / 2)
}

const handleWheel = (event) => {
  if (!imageReady.value || !viewportRef.value) return
  const rect = viewportRef.value.getBoundingClientRect()
  const anchorX = event.clientX - rect.left
  const anchorY = event.clientY - rect.top
  const delta = event.deltaY < 0 ? 0.06 : -0.06
  applyScaleAroundPoint(currentScale.value + delta, anchorX, anchorY)
}

const startDrag = (event) => {
  if (!imageReady.value) return
  dragState.active = true
  dragState.startX = event.clientX
  dragState.startY = event.clientY
  dragState.originX = offsetX.value
  dragState.originY = offsetY.value
  window.addEventListener('pointermove', handleDrag)
  window.addEventListener('pointerup', stopDrag)
}

const handleDrag = (event) => {
  if (!dragState.active) return
  offsetX.value = dragState.originX + (event.clientX - dragState.startX)
  offsetY.value = dragState.originY + (event.clientY - dragState.startY)
  clampOffsets()
}

const stopDrag = () => {
  dragState.active = false
  window.removeEventListener('pointermove', handleDrag)
  window.removeEventListener('pointerup', stopDrag)
}

const confirmCrop = async () => {
  if (!imageRef.value || !viewportRef.value || !imageReady.value) return

  const canvas = document.createElement('canvas')
  canvas.width = props.outputWidth
  canvas.height = props.outputHeight
  const context = canvas.getContext('2d')
  if (!context) return

  const scaleRatio = naturalWidth.value / displayedWidth.value
  const sourceX = ((displayedWidth.value - viewportWidth.value) / 2 - offsetX.value) * scaleRatio
  const sourceY = ((displayedHeight.value - viewportHeight.value) / 2 - offsetY.value) * scaleRatio
  const sourceWidth = viewportWidth.value * scaleRatio
  const sourceHeight = viewportHeight.value * scaleRatio

  context.drawImage(
    imageRef.value,
    sourceX,
    sourceY,
    sourceWidth,
    sourceHeight,
    0,
    0,
    props.outputWidth,
    props.outputHeight
  )

  const blob = await new Promise((resolve) => canvas.toBlob(resolve, props.mimeType, 0.92))
  if (!blob) return

  const file = new File([blob], props.fileName, { type: props.mimeType })
  emit('confirm', file)
}

watch(
  () => props.visible,
  async (visible) => {
    if (!visible) {
      imageReady.value = false
      stopDrag()
      return
    }

    await nextTick()
    updateViewportSize()
    if (naturalWidth.value && naturalHeight.value) {
      initializeCrop()
    }
  }
)

watch(
  () => [props.aspectRatio, props.maxViewportWidth, props.maxViewportHeight],
  async () => {
    if (!props.visible) return
    await nextTick()
    updateViewportSize()
    initializeCrop()
  }
)

window.addEventListener('resize', updateViewportSize)

onBeforeUnmount(() => {
  stopDrag()
  window.removeEventListener('resize', updateViewportSize)
})
</script>

<style scoped>
.crop-fade-enter-active,
.crop-fade-leave-active {
  transition: opacity 0.2s ease;
}

.crop-fade-enter-from,
.crop-fade-leave-to {
  opacity: 0;
}

.crop-modal {
  position: fixed;
  inset: 0;
  z-index: 120;
  background: rgba(15, 23, 42, 0.58);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.crop-dialog {
  width: min(100%, 460px);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.22);
  overflow: hidden;
}

.crop-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 18px 12px;
}

.crop-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.crop-hint {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: #64748b;
}

.crop-close {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: #f1f5f9;
  color: #475569;
  cursor: pointer;
}

.crop-body {
  padding: 0 18px 14px;
}

.crop-viewport {
  margin: 0 auto;
  border-radius: 20px;
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.45);
  position: relative;
  overflow: hidden;
  touch-action: none;
  cursor: grab;
}

.crop-viewport:active {
  cursor: grabbing;
}

.crop-image {
  position: absolute;
  transform-origin: center center;
  user-select: none;
  max-width: none;
}

.crop-controls {
  margin-top: 14px;
}

.crop-scale-row {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 12px;
}

.crop-scale-label,
.crop-scale-value {
  font-size: 12px;
  font-weight: 600;
  color: #475569;
}

.crop-scale-slider {
  width: 100%;
}

.crop-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 0 18px 18px;
}

.crop-btn {
  min-width: 92px;
  height: 40px;
  border-radius: 999px;
  border: none;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.crop-btn-secondary {
  background: #e2e8f0;
  color: #334155;
}

.crop-btn-primary {
  background: linear-gradient(135deg, #4f86f7 0%, #6366f1 100%);
  color: #fff;
}

.crop-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
