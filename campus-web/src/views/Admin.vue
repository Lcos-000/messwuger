<template>
  <div class="admin-page" :style="pageStyleVars">
    <aside class="admin-sidebar">
      <div class="brand-block">
        <div class="brand-texts">
          <h1>{{ ADMIN_CONFIG.TEXT.PAGE_TITLE }}</h1>
          <p>{{ ADMIN_CONFIG.TEXT.PAGE_SUBTITLE }}</p>
        </div>
      </div>

      <div class="sidebar-section">
        <span class="sidebar-label">{{ ADMIN_CONFIG.TEXT.SECTION_LABEL }}</span>
        <button
          class="sidebar-item"
          :class="{ active: activeSection === 'resources' }"
          type="button"
          @click="activeSection = 'resources'"
        >
          <span>{{ ADMIN_CONFIG.TEXT.SECTION_RESOURCES }}</span>
        </button>
        <button
          class="sidebar-item"
          :class="{ active: activeSection === 'logs' }"
          type="button"
          @click="activeSection = 'logs'"
        >
          <span>{{ ADMIN_CONFIG.TEXT.SECTION_LOGS }}</span>
        </button>
      </div>
    </aside>

    <section class="admin-main">
      <header class="admin-toolbar">
        <div class="toolbar-title">
          <h2>{{ currentTitle }}</h2>
          <p>{{ currentSubtitle }}</p>
        </div>
        <button class="logout-button" type="button" @click="handleLogout">{{ ADMIN_CONFIG.TEXT.LOGOUT_LABEL }}</button>
      </header>

      <main class="admin-content">
        <template v-if="activeSection === 'resources'">
          <AdminResourcePanel
            :admin-config="ADMIN_CONFIG"
            :loading="resourcesLoading"
            :resource-cards="resourceCards"
          />
        </template>

        <template v-else>
          <div class="log-layout">
            <AdminLogFilePanel
              :admin-config="ADMIN_CONFIG"
              :loading="logsLoading"
              :log-files="logFiles"
              :active-file-name="activeFileName"
              @select="selectLogFile"
            />

            <section class="log-viewer-panel">
              <div class="log-viewer-toolbar">
                <div class="log-viewer-summary">
                  <div class="log-viewer-file">{{ activeFileName || ADMIN_CONFIG.TEXT.LOG_PLACEHOLDER }}</div>
                  <div class="log-viewer-hint">{{ currentLogHint }}</div>
                </div>
                <div class="log-viewer-actions">
                  <button
                    v-if="selectedLogFile && !selectedLogFile.compressed"
                    class="log-action-button log-action-button-wide"
                    type="button"
                    @click="scrollConsoleToTop"
                  >
                    {{ ADMIN_CONFIG.TEXT.LOG_SCROLL_TOP }}
                  </button>
                  <button
                    v-if="selectedLogFile && !selectedLogFile.compressed"
                    class="log-action-button log-action-button-wide"
                    type="button"
                    :disabled="!canLoadHistory"
                    @click="loadHistoryOnce"
                  >
                    {{ canLoadHistory ? historyButtonText : ADMIN_CONFIG.TEXT.LOG_NO_MORE_HISTORY }}
                  </button>
                  <button
                    v-if="selectedLogFile && !selectedLogFile.compressed"
                    class="log-action-button log-action-button-wide"
                    type="button"
                    @click="resetConsole"
                  >
                    {{ ADMIN_CONFIG.TEXT.LOG_RESET_CONSOLE }}
                  </button>
                  <button
                    v-if="selectedLogFile && !selectedLogFile.compressed"
                    class="log-action-button log-action-button-wide"
                    type="button"
                    @click="refreshOnce"
                  >
                    {{ ADMIN_CONFIG.TEXT.LOG_REFRESH_ONCE }}
                  </button>
                  <button
                    v-if="selectedLogFile && !selectedLogFile.compressed"
                    class="log-action-button log-action-button-wide"
                    type="button"
                    @click="scrollConsoleToBottom"
                  >
                    {{ ADMIN_CONFIG.TEXT.LOG_SCROLL_BOTTOM }}
                  </button>
                  <label
                    v-if="selectedLogFile && !selectedLogFile.compressed"
                    class="polling-toggle"
                    @click.stop
                  >
                    <span class="polling-toggle-label">{{ ADMIN_CONFIG.TEXT.LOG_POLLING_LABEL }}</span>
                    <span class="polling-toggle-status">{{ pollingButtonText }}</span>
                    <span class="toggle-switch">
                      <input
                        type="checkbox"
                        :checked="pollingEnabled"
                        @change="togglePolling"
                      />
                      <span class="toggle-slider"></span>
                    </span>
                  </label>
                  <button
                    v-if="selectedLogFile"
                    class="log-action-button log-action-button-wide"
                    type="button"
                    @click="handleDownload(selectedLogFile.fileName)"
                  >
                    {{ ADMIN_CONFIG.TEXT.LOG_DOWNLOAD }}
                  </button>
                </div>
              </div>

              <div v-if="selectedLogFile?.compressed" class="log-placeholder">
                {{ ADMIN_CONFIG.TEXT.LOG_ARCHIVE_HINT }}
              </div>

              <div v-else-if="!selectedLogFile" class="log-placeholder">
                {{ ADMIN_CONFIG.TEXT.LOG_PLACEHOLDER }}
              </div>

              <div v-else class="log-console-wrap">
                <div class="log-console-meta">
                  <span>{{ ADMIN_CONFIG.TEXT.LOG_LAST_UPDATE }}：{{ lastUpdateText }}</span>
                  <span>{{ consoleMetaStatusText }}</span>
                </div>
                <pre ref="logConsoleRef" class="log-console">{{ logContent }}</pre>
              </div>
            </section>
          </div>
        </template>
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AdminLogFilePanel from '@/components/admin/AdminLogFilePanel.vue'
import AdminResourcePanel from '@/components/admin/AdminResourcePanel.vue'
import {
  adminLogout,
  downloadAdminLog,
  getAdminLogFiles,
  getAdminResources,
  historyAdminLogTail,
  initAdminLogTail,
  pollAdminLogTail
} from '@/api/index'
import { ADMIN_CONFIG, ROUTE_PATHS } from '@/config'
import { clearAdminSession } from '@/utils/auth'

const router = useRouter()
const activeSection = ref('resources')
const resourcesLoading = ref(true)
const logsLoading = ref(false)
const resources = ref([])
const logFiles = ref([])
const selectedLogFile = ref(null)
const activeFileName = ref('')
const logLines = ref([])
const currentStartOffset = ref(0)
const currentOffset = ref(0)
const isPolling = ref(false)
const isDisplayTrimmed = ref(false)
const isHistoryLoading = ref(false)
const pollingEnabled = ref(false)
const lastUpdateAt = ref(null)
const logConsoleRef = ref(null)
let pollTimer = null
let pollInFlight = false

const pageStyleVars = computed(() => ({
  '--admin-sidebar-width': `${ADMIN_CONFIG.LAYOUT.SIDEBAR_WIDTH}px`,
  '--admin-sidebar-padding-y': `${ADMIN_CONFIG.LAYOUT.SIDEBAR_PADDING_Y}px`,
  '--admin-sidebar-padding-x': `${ADMIN_CONFIG.LAYOUT.SIDEBAR_PADDING_X}px`,
  '--admin-content-padding-top': `${ADMIN_CONFIG.LAYOUT.CONTENT_PADDING_TOP}px`,
  '--admin-content-padding-x': `${ADMIN_CONFIG.LAYOUT.CONTENT_PADDING_X}px`,
  '--admin-content-padding-bottom': `${ADMIN_CONFIG.LAYOUT.CONTENT_PADDING_BOTTOM}px`,
  '--admin-panel-max-width': `${ADMIN_CONFIG.LAYOUT.PANEL_MAX_WIDTH}px`,
  '--admin-toolbar-min-height': `${ADMIN_CONFIG.LAYOUT.TOOLBAR_MIN_HEIGHT}px`,
  '--admin-toolbar-padding-y': `${ADMIN_CONFIG.LAYOUT.TOOLBAR_PADDING_Y}px`,
  '--admin-toolbar-padding-x': `${ADMIN_CONFIG.LAYOUT.TOOLBAR_PADDING_X}px`,
  '--admin-button-min-width': `${ADMIN_CONFIG.LAYOUT.BUTTON_MIN_WIDTH}px`,
  '--admin-button-height': `${ADMIN_CONFIG.LAYOUT.BUTTON_HEIGHT}px`,
  '--admin-button-padding-x': `${ADMIN_CONFIG.LAYOUT.BUTTON_PADDING_X}px`,
  '--admin-item-padding-y': `${ADMIN_CONFIG.LAYOUT.ITEM_PADDING_Y}px`,
  '--admin-panel-padding-y': `${ADMIN_CONFIG.LAYOUT.PANEL_PADDING_Y}px`,
  '--admin-panel-padding-x': `${ADMIN_CONFIG.LAYOUT.PANEL_PADDING_X}px`,
  '--admin-resource-name-size': `${ADMIN_CONFIG.LAYOUT.RESOURCE_NAME_SIZE}px`,
  '--admin-resource-url-size': `${ADMIN_CONFIG.LAYOUT.RESOURCE_URL_SIZE}px`,
  '--admin-mobile-resource-name-size': `${ADMIN_CONFIG.LAYOUT.MOBILE_RESOURCE_NAME_SIZE}px`,
  '--admin-mobile-resource-url-size': `${ADMIN_CONFIG.LAYOUT.MOBILE_RESOURCE_URL_SIZE}px`,
  '--admin-log-file-panel-width': `${ADMIN_CONFIG.LAYOUT.LOG_FILE_PANEL_WIDTH}px`,
  '--admin-log-viewer-height': `${ADMIN_CONFIG.LAYOUT.LOG_VIEWER_HEIGHT}px`,
  '--admin-log-viewer-min-height': `${ADMIN_CONFIG.LAYOUT.LOG_VIEWER_MIN_HEIGHT}px`,
  '--admin-page-bg': ADMIN_CONFIG.THEME.PAGE_BG,
  '--admin-sidebar-bg': ADMIN_CONFIG.THEME.SIDEBAR_BG,
  '--admin-main-bg': ADMIN_CONFIG.THEME.MAIN_BG,
  '--admin-toolbar-bg': ADMIN_CONFIG.THEME.TOOLBAR_BG,
  '--admin-panel-bg': ADMIN_CONFIG.THEME.PANEL_BG,
  '--admin-sidebar-active-bg': ADMIN_CONFIG.THEME.SIDEBAR_ACTIVE_BG,
  '--admin-sidebar-border': ADMIN_CONFIG.THEME.SIDEBAR_BORDER,
  '--admin-panel-border': ADMIN_CONFIG.THEME.PANEL_BORDER,
  '--admin-button-border': ADMIN_CONFIG.THEME.BUTTON_BORDER,
  '--admin-button-border-hover': ADMIN_CONFIG.THEME.BUTTON_BORDER_HOVER,
  '--admin-button-bg': ADMIN_CONFIG.THEME.BUTTON_BG,
  '--admin-button-bg-hover': ADMIN_CONFIG.THEME.BUTTON_BG_HOVER,
  '--admin-text-primary': ADMIN_CONFIG.THEME.TEXT_PRIMARY,
  '--admin-text-secondary': ADMIN_CONFIG.THEME.TEXT_SECONDARY,
  '--admin-text-muted': ADMIN_CONFIG.THEME.TEXT_MUTED,
  '--admin-text-link': ADMIN_CONFIG.THEME.TEXT_LINK,
  '--admin-text-link-strong': ADMIN_CONFIG.THEME.TEXT_LINK_STRONG,
  '--admin-text-link-muted': ADMIN_CONFIG.THEME.TEXT_LINK_MUTED,
  '--admin-text-link-hover': ADMIN_CONFIG.THEME.TEXT_LINK_HOVER,
  '--admin-divider': ADMIN_CONFIG.THEME.DIVIDER
}))

const resourceCards = computed(() => resources.value.filter(item => item?.name && item?.url))
const currentTitle = computed(() => activeSection.value === 'logs' ? ADMIN_CONFIG.TEXT.LOG_TITLE : ADMIN_CONFIG.TEXT.RESOURCE_TITLE)
const currentSubtitle = computed(() => activeSection.value === 'logs' ? ADMIN_CONFIG.TEXT.LOG_SUBTITLE : ADMIN_CONFIG.TEXT.RESOURCE_SUBTITLE)
const logContent = computed(() => logLines.value.join('\n'))
const currentLogHint = computed(() => {
  if (!selectedLogFile.value) return ADMIN_CONFIG.TEXT.LOG_PLACEHOLDER
  return selectedLogFile.value.compressed ? ADMIN_CONFIG.TEXT.LOG_ARCHIVE_HINT : ADMIN_CONFIG.TEXT.LOG_HISTORY_HINT
})
const lastUpdateText = computed(() => {
  if (!lastUpdateAt.value) return '--'
  return new Date(lastUpdateAt.value).toLocaleString()
})
const pollingButtonText = computed(() => (
  pollingEnabled.value ? ADMIN_CONFIG.TEXT.LOG_STOP_POLLING : ADMIN_CONFIG.TEXT.LOG_START_POLLING
))
const pollingStatusText = computed(() => {
  if (isPolling.value) return ADMIN_CONFIG.TEXT.LOG_REFRESHING
  return pollingEnabled.value ? ADMIN_CONFIG.TEXT.LOG_REFRESHING : ADMIN_CONFIG.TEXT.LOG_POLLING_IDLE
})
const consoleMetaStatusText = computed(() => (
  isDisplayTrimmed.value
    ? `${pollingStatusText.value} · ${ADMIN_CONFIG.TEXT.LOG_LIMIT_HINT}`
    : pollingStatusText.value
))
const canLoadHistory = computed(() => (
  !!selectedLogFile.value && !selectedLogFile.value.compressed && currentStartOffset.value > 0 && !isHistoryLoading.value
))
const historyButtonText = computed(() => (
  isHistoryLoading.value ? ADMIN_CONFIG.TEXT.LOG_LOADING_HISTORY : ADMIN_CONFIG.TEXT.LOG_LOAD_HISTORY
))

const stopPolling = () => {
  if (pollTimer) {
    clearTimeout(pollTimer)
    pollTimer = null
  }
  isPolling.value = false
  pollInFlight = false
}

const scrollConsoleToTop = async () => {
  await nextTick()
  if (logConsoleRef.value) {
    logConsoleRef.value.scrollTop = 0
  }
}

const scrollConsoleToBottom = async () => {
  await nextTick()
  if (logConsoleRef.value) {
    logConsoleRef.value.scrollTop = logConsoleRef.value.scrollHeight
  }
}

const scrollLogToBottom = async () => {
  await nextTick()
  if (logConsoleRef.value) {
    logConsoleRef.value.scrollTop = logConsoleRef.value.scrollHeight
  }
}

const trimLogLines = (mode = 'append') => {
  const maxLines = ADMIN_CONFIG.LAYOUT.LOG_MAX_DISPLAY_LINES
  if (!maxLines || logLines.value.length <= maxLines) {
    return
  }
  isDisplayTrimmed.value = true
  if (mode === 'prepend') {
    logLines.value = logLines.value.slice(0, maxLines)
    return
  }
  logLines.value = logLines.value.slice(-maxLines)
  currentStartOffset.value = 0
}

const loadResources = async () => {
  resourcesLoading.value = true
  try {
    const res = await getAdminResources()
    resources.value = res.data?.items || []
  } finally {
    resourcesLoading.value = false
  }
}

const loadLogFiles = async () => {
  logsLoading.value = true
  try {
    const res = await getAdminLogFiles()
    logFiles.value = res.data?.files || []
  } finally {
    logsLoading.value = false
  }
}

const resetLogViewerState = () => {
  logLines.value = []
  currentStartOffset.value = 0
  currentOffset.value = 0
  isDisplayTrimmed.value = false
  lastUpdateAt.value = null
}

const applyLogTailData = async (data) => {
  if (data?.reset) {
    alert(ADMIN_CONFIG.TEXT.LOG_RESET_HINT)
    logLines.value = data.lines || []
  } else if (data?.lines?.length) {
    logLines.value = [...logLines.value, ...data.lines]
  }
  if (typeof data?.startOffset === 'number') {
    currentStartOffset.value = data.startOffset
  }
  if (!data?.reset) {
    trimLogLines('append')
  }
  currentOffset.value = data?.offset ?? currentOffset.value
  lastUpdateAt.value = Date.now()
  if (data?.lines?.length || data?.reset) {
    await scrollLogToBottom()
  }
}

const pollOnce = async () => {
  if (!activeFileName.value || !selectedLogFile.value || selectedLogFile.value.compressed || pollInFlight) return
  pollInFlight = true
  isPolling.value = true
  try {
    const res = await pollAdminLogTail(activeFileName.value, currentOffset.value)
    await applyLogTailData(res.data)
  } finally {
    pollInFlight = false
    isPolling.value = false
  }
}

const scheduleNextPoll = () => {
  if (!pollingEnabled.value || !activeFileName.value) return
  pollTimer = setTimeout(async () => {
    try {
      await pollOnce()
    } catch (error) {
      console.error(error)
      pollingEnabled.value = false
      stopPolling()
      return
    }
    scheduleNextPoll()
  }, ADMIN_CONFIG.LAYOUT.LOG_POLLING_INTERVAL)
}

const startPolling = () => {
  stopPolling()
  if (!pollingEnabled.value || !selectedLogFile.value || selectedLogFile.value.compressed) return
  scheduleNextPoll()
}

const togglePolling = () => {
  if (!selectedLogFile.value || selectedLogFile.value.compressed) return
  pollingEnabled.value = !pollingEnabled.value
  if (pollingEnabled.value) {
    startPolling()
    return
  }
  stopPolling()
}

const refreshOnce = async () => {
  if (!selectedLogFile.value || selectedLogFile.value.compressed) return
  try {
    stopPolling()
    pollingEnabled.value = false
    await pollOnce()
  } catch (error) {
    console.error(error)
  }
}

const resetConsole = async () => {
  if (!selectedLogFile.value || selectedLogFile.value.compressed) return
  try {
    stopPolling()
    pollingEnabled.value = false
    resetLogViewerState()
    const res = await initAdminLogTail(selectedLogFile.value.fileName)
    const data = res.data
    logLines.value = data?.lines || []
    currentStartOffset.value = data?.startOffset || 0
    currentOffset.value = data?.offset || 0
    lastUpdateAt.value = Date.now()
    await scrollLogToBottom()
  } catch (error) {
    console.error(error)
  }
}

const loadHistoryOnce = async () => {
  if (!selectedLogFile.value || selectedLogFile.value.compressed || currentStartOffset.value <= 0 || isHistoryLoading.value) return
  isHistoryLoading.value = true
  const previousScrollHeight = logConsoleRef.value?.scrollHeight || 0
  const previousScrollTop = logConsoleRef.value?.scrollTop || 0
  try {
    const res = await historyAdminLogTail(activeFileName.value, currentStartOffset.value)
    const data = res.data
    if (data?.reset) {
      alert(ADMIN_CONFIG.TEXT.LOG_RESET_HINT)
      logLines.value = data.lines || []
      currentOffset.value = data?.offset ?? currentOffset.value
      currentStartOffset.value = data?.startOffset ?? 0
      await scrollLogToBottom()
      return
    }
    if (data?.lines?.length) {
      logLines.value = [...data.lines, ...logLines.value]
      currentStartOffset.value = data?.startOffset ?? currentStartOffset.value
      trimLogLines('prepend')
      lastUpdateAt.value = Date.now()
      await nextTick()
      if (logConsoleRef.value) {
        const nextScrollHeight = logConsoleRef.value.scrollHeight
        logConsoleRef.value.scrollTop = previousScrollTop + (nextScrollHeight - previousScrollHeight)
      }
    }
  } finally {
    isHistoryLoading.value = false
  }
}

const selectLogFile = async (file) => {
  stopPolling()
  pollingEnabled.value = false
  selectedLogFile.value = file
  activeFileName.value = file.fileName
  resetLogViewerState()

  if (file.compressed) return

  const res = await initAdminLogTail(file.fileName)
  const data = res.data
  logLines.value = data?.lines || []
  currentStartOffset.value = data?.startOffset || 0
  currentOffset.value = data?.offset || 0
  lastUpdateAt.value = Date.now()
  await scrollLogToBottom()
}

const handleDownload = async (fileName) => {
  const response = await downloadAdminLog(fileName)
  const blob = new Blob([response.data])
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

const clearAuth = () => {
  clearAdminSession()
}

const handleLogout = async () => {
  try {
    await adminLogout()
  } catch (error) {
    console.error(error)
  } finally {
    stopPolling()
    clearAuth()
    router.replace(ROUTE_PATHS.LOGIN)
  }
}

watch(activeSection, async (value) => {
  if (value === 'logs') {
    await loadLogFiles()
  } else {
    stopPolling()
    pollingEnabled.value = false
  }
})

onMounted(() => {
  loadResources()
})

onBeforeUnmount(() => {
  stopPolling()
  pollingEnabled.value = false
})
</script>

<style scoped>
.admin-page {
  display: grid;
  grid-template-columns: var(--admin-sidebar-width) minmax(0, 1fr);
  min-height: 100vh;
  background: var(--admin-page-bg);
  color: #d7dae0;
}

.admin-sidebar {
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding: var(--admin-sidebar-padding-y) var(--admin-sidebar-padding-x);
  background: var(--admin-sidebar-bg);
  border-right: 1px solid var(--admin-sidebar-border);
}

.brand-block {
  display: flex;
  align-items: center;
}

.brand-texts h1 {
  font-size: 15px;
  font-weight: 600;
  color: var(--admin-text-primary);
}

.brand-texts p {
  margin-top: 2px;
  font-size: 12px;
  color: var(--admin-text-secondary);
}

.sidebar-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sidebar-label {
  padding: 0 10px;
  font-size: 11px;
  color: var(--admin-text-muted);
  text-transform: uppercase;
}

.sidebar-item {
  display: flex;
  align-items: center;
  min-height: 36px;
  padding: 0 12px;
  border-radius: 7px;
  color: #c9ccd3;
  background: transparent;
  text-align: left;
}

.sidebar-item.active {
  background: var(--admin-sidebar-active-bg);
  color: #f4f7fb;
}

.admin-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--admin-main-bg);
}

.admin-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: var(--admin-toolbar-min-height);
  padding: var(--admin-toolbar-padding-y) var(--admin-toolbar-padding-x);
  border-bottom: 1px solid var(--admin-panel-border);
  background: var(--admin-toolbar-bg);
}

.toolbar-title h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--admin-text-primary);
}

.toolbar-title p {
  margin-top: 3px;
  font-size: 12px;
  color: var(--admin-text-secondary);
}

.logout-button,
.log-action-button {
  min-width: var(--admin-button-min-width);
  height: var(--admin-button-height);
  padding: 0 var(--admin-button-padding-x);
  border: 1px solid var(--admin-button-border);
  border-radius: 7px;
  background: var(--admin-button-bg);
  color: #d7dae0;
  transition: background-color 0.18s ease, border-color 0.18s ease, opacity 0.18s ease;
}

.log-action-button-wide {
  min-width: 96px;
}

.log-action-button:disabled {
  opacity: 0.56;
  cursor: not-allowed;
}

.logout-button:hover,
.log-action-button:hover {
  background: var(--admin-button-bg-hover);
  border-color: var(--admin-button-border-hover);
}

.admin-content {
  flex: 1;
  min-width: 0;
  padding: var(--admin-content-padding-top) var(--admin-content-padding-x) var(--admin-content-padding-bottom);
}

.log-layout {
  display: grid;
  grid-template-columns: var(--admin-log-file-panel-width) minmax(0, 1fr);
  min-height: 280px;
  height: min(72vh, var(--admin-log-viewer-height));
  overflow: hidden;
  border: 1px solid var(--admin-panel-border);
  border-radius: 10px;
  background: var(--admin-panel-bg);
}

.log-placeholder {
  display: grid;
  place-items: center;
  color: var(--admin-text-secondary);
  min-height: 180px;
}

.log-viewer-panel {
  min-width: 0;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.log-viewer-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border-bottom: 1px solid var(--admin-panel-border);
}

.log-viewer-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.polling-toggle {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: var(--admin-button-height);
  padding: 0 4px 0 6px;
  border: 1px solid var(--admin-button-border);
  border-radius: 999px;
  background: var(--admin-button-bg);
  color: #d7dae0;
}

.polling-toggle-label {
  font-size: 12px;
  color: var(--admin-text-secondary);
}

.polling-toggle-status {
  font-size: 12px;
  color: var(--admin-text-primary);
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
  flex-shrink: 0;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #5b6470;
  transition: 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 999px;
}

.toggle-slider::before {
  position: absolute;
  content: '';
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 50%;
}

input:checked + .toggle-slider {
  background-color: #4e78ab;
}

input:checked + .toggle-slider::before {
  transform: translateX(20px);
}

.log-viewer-file {
  font-size: 14px;
  color: var(--admin-text-primary);
}

.log-viewer-hint,
.log-console-meta {
  font-size: 12px;
  color: var(--admin-text-secondary);
}

.log-console-wrap {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.log-console-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 18px 0;
}

.log-console {
  flex: 1;
  min-height: 0;
  margin: 12px 18px 18px;
  padding: 16px;
  overflow: auto;
  border-radius: 8px;
  background: #17181b;
  border: 1px solid rgba(255, 255, 255, 0.05);
  color: #dbe4ff;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre;
  word-break: normal;
  overscroll-behavior: contain;
  font-family: Consolas, 'Courier New', monospace;
}

.log-console::-webkit-scrollbar {
  width: 14px;
  height: 14px;
}

.log-console::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.04);
  border-radius: 999px;
}

.log-console::-webkit-scrollbar-thumb {
  background: rgba(122, 162, 255, 0.42);
  border-radius: 999px;
  border: 3px solid #17181b;
}

.log-console::-webkit-scrollbar-thumb:hover {
  background: rgba(122, 162, 255, 0.62);
}

.log-console::-webkit-scrollbar-corner {
  background: transparent;
}

@media screen and (max-width: 960px) {
  .admin-page {
    grid-template-columns: 1fr;
  }

  .admin-sidebar {
    gap: 18px;
    padding-bottom: 14px;
    border-right: none;
    border-bottom: 1px solid var(--admin-sidebar-border);
  }

  .log-layout {
    grid-template-columns: 1fr;
    height: auto;
  }

  .log-console {
    min-height: 320px;
  }
}
</style>
