<template>
  <div class="app-container">
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition :name="transitionName" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <NavBar v-if="!isLoginPage && !isAdminPage" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from './components/NavBar.vue'
import { ROUTE_NAMES, ROUTE_PATHS } from '@/config'
import { fetchAndApplyGlobalFontPreference, loadAndApplyGlobalFontPreference } from '@/utils/globalFont'
import { hasUserSession } from '@/utils/auth'

const route = useRoute()

const isLoginPage = computed(() => {
  return route.path === ROUTE_PATHS.LOGIN || route.name === ROUTE_NAMES.LOGIN
})

const isAdminPage = computed(() => {
  return route.path === ROUTE_PATHS.ADMIN || route.name === ROUTE_NAMES.ADMIN
})

const transitionName = ref('slide-left')
let globalFontRequesting = false
let globalFontSyncedFromServer = false

const syncGlobalFontPreference = async () => {
  loadAndApplyGlobalFontPreference()
  if (!hasUserSession()) {
    globalFontSyncedFromServer = false
    return
  }
  if (isLoginPage.value || isAdminPage.value || globalFontRequesting || globalFontSyncedFromServer) return

  globalFontRequesting = true
  try {
    await fetchAndApplyGlobalFontPreference()
    globalFontSyncedFromServer = true
  } catch (error) {
    console.error('同步全局字体偏好失败', error)
  } finally {
    globalFontRequesting = false
  }
}

onMounted(syncGlobalFontPreference)

watch(
  () => route.meta.index,
  (toIndex, fromIndex) => {
    if (toIndex == null || fromIndex == null) {
      transitionName.value = 'fade'
      return
    }
    transitionName.value = toIndex > fromIndex ? 'slide-left' : 'slide-right'
  }
)

watch(
  () => route.fullPath,
  () => {
    syncGlobalFontPreference()
  }
)
</script>

<style>
.app-container {
  display: flex;
  flex-direction: column;
  height: 100dvh;
  width: 100vw;
  overflow: hidden;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
  min-height: 0;
}

@media screen and (min-width: 768px) {
  .app-container {
    flex-direction: row-reverse;
    height: 100vh;
  }

  .main-content {
    flex: 1;
    height: 100vh;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.28s cubic-bezier(0.32, 0.72, 0, 1), opacity 0.22s ease;
}

.slide-left-enter-from {
  opacity: 0;
  transform: translateX(30px);
}
.slide-left-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

.slide-right-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}
.slide-right-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
