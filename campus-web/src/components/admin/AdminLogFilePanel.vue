<template>
  <aside class="log-file-panel">
    <div class="log-file-panel-title">{{ adminConfig.TEXT.LOG_FILE_LIST_TITLE }}</div>
    <div v-if="loading" class="log-panel-state">{{ adminConfig.TEXT.LOADING_TEXT }}</div>
    <div v-else-if="logFiles.length === 0" class="log-panel-state">{{ adminConfig.TEXT.LOG_EMPTY_TEXT }}</div>
    <template v-else>
      <div v-if="activeLogFiles.length" class="log-file-group">
        <div class="log-file-group-title">{{ adminConfig.TEXT.LOG_FILE_GROUP_ACTIVE }}</div>
        <button
          v-for="file in activeLogFiles"
          :key="file.fileName"
          class="log-file-item"
          :class="{ active: activeFileName === file.fileName }"
          type="button"
          @click="$emit('select', file)"
        >
          <div class="log-file-name">{{ file.fileName }}</div>
          <div class="log-file-meta">
            <span>{{ adminConfig.TEXT.LOG_FILE_TYPE_ACTIVE }}</span>
            <span>{{ formatSize(file.size) }}</span>
          </div>
        </button>
      </div>

      <div v-if="archiveLogFiles.length" class="log-file-group">
        <div class="log-file-group-title">{{ adminConfig.TEXT.LOG_FILE_GROUP_ARCHIVE }}</div>
        <button
          v-for="file in archiveLogFiles"
          :key="file.fileName"
          class="log-file-item log-file-item-archive"
          :class="{ active: activeFileName === file.fileName }"
          type="button"
          @click="$emit('select', file)"
        >
          <div class="log-file-name">{{ file.fileName }}</div>
          <div class="log-file-meta">
            <span>{{ adminConfig.TEXT.LOG_FILE_TYPE_ARCHIVE }}</span>
            <span>{{ formatSize(file.size) }}</span>
          </div>
        </button>
      </div>
    </template>
  </aside>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  adminConfig: {
    type: Object,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  logFiles: {
    type: Array,
    default: () => []
  },
  activeFileName: {
    type: String,
    default: ''
  }
})

defineEmits(['select'])

const activeLogFiles = computed(() => props.logFiles.filter(file => !file?.compressed))
const archiveLogFiles = computed(() => props.logFiles.filter(file => file?.compressed))

const formatSize = (size = 0) => {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / (1024 * 1024)).toFixed(1)} MB`
}
</script>

<style scoped>
.log-file-panel {
  border-right: 1px solid var(--admin-panel-border);
  padding: 16px;
  display: flex;
  flex-direction: column;
  min-height: 0;
  gap: 10px;
  overflow-y: auto;
}

.log-file-panel-title {
  font-size: 13px;
  color: var(--admin-text-secondary);
  margin-bottom: 2px;
}

.log-file-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.log-file-group + .log-file-group {
  margin-top: 10px;
  padding-top: 12px;
  border-top: 1px solid var(--admin-divider);
}

.log-file-group-title {
  font-size: 12px;
  color: var(--admin-text-muted);
  letter-spacing: 0.02em;
}

.log-panel-state {
  display: grid;
  place-items: center;
  color: var(--admin-text-secondary);
  min-height: 180px;
}

.log-file-item {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--admin-panel-border);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.02);
  color: #d7dae0;
  text-align: left;
  transition: border-color 0.18s ease, background-color 0.18s ease, transform 0.18s ease;
}

.log-file-item.active {
  background: rgba(54, 88, 128, 0.34);
  border-color: #4e78ab;
  transform: translateY(-1px);
}

.log-file-item:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(122, 162, 255, 0.45);
}

.log-file-item-archive {
  background: rgba(255, 255, 255, 0.015);
}

.log-file-name {
  font-size: 13px;
  color: #f3f4f6;
  word-break: break-all;
  line-height: 1.45;
}

.log-file-meta {
  margin-top: 6px;
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 11px;
  color: var(--admin-text-muted);
}

@media screen and (max-width: 960px) {
  .log-file-panel {
    border-right: none;
    border-bottom: 1px solid var(--admin-panel-border);
  }
}
</style>
