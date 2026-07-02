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
        <button class="sidebar-item active" type="button">
          <span>{{ ADMIN_CONFIG.TEXT.SECTION_ITEM }}</span>
        </button>
      </div>
    </aside>

    <section class="admin-main">
      <header class="admin-toolbar">
        <div class="toolbar-title">
          <h2>{{ ADMIN_CONFIG.TEXT.RESOURCE_TITLE }}</h2>
          <p>{{ ADMIN_CONFIG.TEXT.RESOURCE_SUBTITLE }}</p>
        </div>
        <button class="logout-button" type="button" @click="handleLogout">{{ ADMIN_CONFIG.TEXT.LOGOUT_LABEL }}</button>
      </header>

      <main class="admin-content">
        <div v-if="loading" class="admin-state">{{ ADMIN_CONFIG.TEXT.LOADING_TEXT }}</div>
        <div v-else-if="resourceCards.length === 0" class="admin-state">{{ ADMIN_CONFIG.TEXT.EMPTY_TEXT }}</div>
        <div v-else class="resource-panel">
          <a
            v-for="item in resourceCards"
            :key="item.code || `${item.name}-${item.url}`"
            class="resource-link"
            :href="item.url"
            target="_blank"
            rel="noopener noreferrer"
          >
            <span class="resource-text">{{ item.name }}{{ ADMIN_CONFIG.TEXT.RESOURCE_SEPARATOR }}</span>
            <span class="resource-url">{{ item.url }}</span>
          </a>
        </div>
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogout, getAdminResources } from '@/api/index'
import { ADMIN_CONFIG, ROUTE_PATHS, STORAGE_KEYS } from '@/config'

const router = useRouter()
const loading = ref(true)
const resources = ref([])

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

const resourceCards = computed(() => {
  return resources.value.filter(item => item?.name && item?.url)
})

const loadResources = async () => {
  loading.value = true
  try {
    const res = await getAdminResources()
    resources.value = res.data?.items || []
  } finally {
    loading.value = false
  }
}

const clearAuth = () => {
  localStorage.removeItem(STORAGE_KEYS.TOKEN)
  localStorage.removeItem(STORAGE_KEYS.LOGIN_MODE)
}

const handleLogout = async () => {
  try {
    await adminLogout()
  } catch (error) {
    console.error(error)
  } finally {
    clearAuth()
    router.replace(ROUTE_PATHS.LOGIN)
  }
}

onMounted(() => {
  loadResources()
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

.logout-button {
  min-width: var(--admin-button-min-width);
  height: var(--admin-button-height);
  padding: 0 var(--admin-button-padding-x);
  border: 1px solid var(--admin-button-border);
  border-radius: 7px;
  background: var(--admin-button-bg);
  color: #d7dae0;
  transition: background-color 0.18s ease, border-color 0.18s ease;
}

.logout-button:hover {
  background: var(--admin-button-bg-hover);
  border-color: var(--admin-button-border-hover);
}

.admin-content {
  flex: 1;
  min-width: 0;
  padding: var(--admin-content-padding-top) var(--admin-content-padding-x) var(--admin-content-padding-bottom);
}

.admin-state,
.resource-panel {
  min-height: 280px;
  border: 1px solid var(--admin-panel-border);
  border-radius: 10px;
  background: var(--admin-panel-bg);
}

.admin-state {
  display: grid;
  place-items: center;
  color: var(--admin-text-secondary);
}

.resource-panel {
  display: flex;
  flex-direction: column;
  width: min(var(--admin-panel-max-width), 100%);
  padding: var(--admin-panel-padding-y) var(--admin-panel-padding-x);
}

.resource-link {
  display: flex;
  align-items: baseline;
  gap: 0;
  padding: var(--admin-item-padding-y) 0;
  border-bottom: 1px solid var(--admin-divider);
  color: var(--admin-text-link);
  text-decoration: underline;
  text-decoration-color: var(--admin-text-link-muted);
  text-underline-offset: 4px;
}

.resource-link:last-child {
  border-bottom: none;
}

.resource-link:hover {
  color: var(--admin-text-link-hover);
  text-decoration-color: var(--admin-text-link-hover);
}

.resource-link:hover .resource-text,
.resource-link:hover .resource-url {
  color: var(--admin-text-link-hover);
}

.resource-text {
  flex: 0 0 auto;
  font-size: var(--admin-resource-name-size);
  font-weight: 500;
  color: var(--admin-text-link-strong);
  transition: color 0.18s ease;
}

.resource-url {
  flex: 1 1 auto;
  font-size: var(--admin-resource-url-size);
  color: var(--admin-text-link-muted);
  line-height: 1.5;
  word-break: break-all;
  transition: color 0.18s ease;
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

  .resource-link {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .resource-text {
    font-size: var(--admin-mobile-resource-name-size);
  }

  .resource-url {
    font-size: var(--admin-mobile-resource-url-size);
  }
}
</style>
