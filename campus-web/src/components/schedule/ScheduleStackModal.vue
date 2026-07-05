<template>
  <transition name="modal-fade">
    <div v-if="group" class="modal-overlay" @click.self="emit('close')">
      <div class="modal-card">
        <div class="modal-accent" :style="{ background: getCourseColor(group.courses[0].courseName) }"></div>
        <div class="modal-content">
          <h3 class="modal-title">{{ text.STACK_TITLE.replace('{count}', group.courses.length) }}</h3>
          <div class="stack-list">
            <div v-for="(course, index) in group.courses" :key="index" class="stack-list-item" @click="emit('select-course', course)">
              <div class="stack-dot" :style="{ background: getCourseColor(course.courseName) }"></div>
              <div class="stack-info">
                <div class="stack-name">{{ course.courseName }}</div>
                <div class="stack-meta">{{ course.classroom }} · {{ buildTeacherNames(course.teachers) }}</div>
              </div>
            </div>
          </div>
        </div>
        <button class="modal-close" @click="emit('close')">{{ text.CLOSE_BUTTON }}</button>
      </div>
    </div>
  </transition>
</template>

<script setup>
defineProps({
  group: { type: Object, default: null },
  text: { type: Object, required: true },
  getCourseColor: { type: Function, required: true },
  buildTeacherNames: { type: Function, required: true }
})

const emit = defineEmits(['close', 'select-course'])
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 25, 50, 0.45);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 200;
  padding: 0 0 env(safe-area-inset-bottom, 0) 0;
}

.modal-card {
  width: 100%;
  max-width: 480px;
  background: #fff;
  border-radius: 20px 20px 0 0;
  overflow: hidden;
  box-shadow: 0 -8px 40px rgba(0, 0, 0, 0.15);
}

.modal-accent {
  height: 4px;
  width: 100%;
}

.modal-content {
  padding: 20px 24px 12px;
}

.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a2540;
  margin-bottom: 16px;
  line-height: 1.3;
}

.stack-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}

.stack-list-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: #f7f9fc;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.15s, transform 0.12s;
}

.stack-list-item:active {
  background: #eef3ff;
  transform: scale(0.98);
}

.stack-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.stack-info {
  flex: 1;
  min-width: 0;
}

.stack-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a2540;
  line-height: 1.3;
}

.stack-meta {
  font-size: 13px;
  color: #8a9bc0;
  margin-top: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.modal-close {
  display: block;
  width: calc(100% - 48px);
  margin: 8px 24px 20px;
  padding: 13px;
  background: linear-gradient(135deg, #4f86f7 0%, #6366f1 100%);
  color: #fff;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 600;
  text-align: center;
  letter-spacing: 0.3px;
  box-shadow: 0 4px 14px rgba(79, 134, 247, 0.3);
  transition: transform 0.15s;
}

.modal-close:active {
  transform: scale(0.97);
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.25s;
}

.modal-fade-enter-active .modal-card,
.modal-fade-leave-active .modal-card {
  transition: transform 0.25s cubic-bezier(0.32, 1.28, 0.52, 1);
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from .modal-card,
.modal-fade-leave-to .modal-card {
  transform: translateY(100%);
}
</style>
