<template>
  <div class="profile-root" :style="profileRootStyle">
    <div v-if="profileStyle.wallpaper" class="wallpaper-layer" :style="wallpaperLayerStyle"></div>
    <!-- 顶部背景渐变 + 用户信息 -->
    <div class="hero-shell" :style="heroShellStyle">
      <div class="hero-section" :style="heroSectionStyle">
        <div class="hero-bg" :style="heroBackgroundStyle"></div>
        <div class="avatar-ring" :style="avatarRingStyle">
          <div class="avatar">
            <img v-if="profileStyle.avatar" class="avatar-image" :src="profileStyle.avatar" alt="用户头像" />
            <span v-else>{{ firstChar }}</span>
          </div>
        </div>
        <h2 class="user-name" :style="heroTextStyle">{{ personalInfo.name || APP_CONFIG.DEFAULT_USER_NAME }}</h2>
        <p class="user-sub" :style="heroSubStyle">{{ personalInfo.studentId || tokenInfo?.studentID || APP_CONFIG.UNKNOWN_STUDENT_ID }}</p>
      </div>
    </div>

    <div class="profile-content" :style="profileContentStyle">
      <!-- 信息卡片 -->
      <div class="section-card info-card">
      <div class="info-row" v-show="userInfoLoaded">
        <div class="info-icon" style="background:#fef3c7; color:#f59e0b">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21v-8M21 21v-8M3 13L12 3l9 10M12 3v18"/></svg>
        </div>
        <div class="info-content">
          <span class="info-label">学校/学院</span>
          <span class="info-value">{{ personalInfo.college || APP_CONFIG.DEFAULT_COLLEGE }}</span>
        </div>
      </div>
      <div class="info-row" v-show="userInfoLoaded">
        <div class="info-icon" style="background:#eef3ff; color:#4f86f7">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg>
        </div>
        <div class="info-content">
          <span class="info-label">专业</span>
          <span class="info-value">{{ personalInfo.major || APP_CONFIG.DEFAULT_USER_NAME }}</span>
        </div>
      </div>
      <div class="info-row" v-show="userInfoLoaded">
        <div class="info-icon" style="background:#f0fdf4; color:#10b981">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
        </div>
        <div class="info-content">
          <span class="info-label">班级</span>
          <span class="info-value">{{ personalInfo.className || APP_CONFIG.DEFAULT_USER_NAME }}</span>
        </div>
      </div>
      <div class="info-row" v-show="userInfoLoaded">
        <div class="info-icon" :style="syncIconStyle">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
        </div>
        <div class="info-content">
          <span class="info-label">同步状态</span>
          <span class="info-value" :class="statusClass">{{ statusText }}</span>
        </div>
      </div>
      <div class="info-row" v-show="userInfoLoaded">
        <div class="info-icon" :style="punchIconStyle">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        </div>
        <div class="info-content">
          <span class="info-label">{{ PROFILE_VIEW_CONFIG.STATUS_FIELDS[1].label }}</span>
          <span class="info-value" :class="punchClass">{{ punchText }}</span>
        </div>
      </div>
    </div>

    <div class="section-card setting-card">
      <div class="setting-block">
        <div class="setting-head">
          <div class="setting-title-row">
            <span class="setting-title">{{ PROFILE_VIEW_CONFIG.CARD_OPACITY_TITLE }}</span>
            <span class="help-icon-wrap">
              <span class="help-icon" aria-label="提示">i</span>
              <span class="help-tooltip">{{ PROFILE_VIEW_CONFIG.CARD_OPACITY_HELP_TEXT }}</span>
            </span>
          </div>
          <span class="setting-value">{{ Math.round(cardOpacityControl * 100) }}%</span>
        </div>
        <input
          v-model.number="cardOpacityControl"
          class="setting-slider"
          type="range"
          :min="PROFILE_VIEW_CONFIG.CARD_BG_OPACITY_MIN"
          :max="PROFILE_VIEW_CONFIG.CARD_OPACITY_MAX"
          :step="PROFILE_VIEW_CONFIG.CARD_OPACITY_STEP"
        />
      </div>
    </div>

    <div
      v-if="galleryGroups.length"
      class="section-card gallery-card"
      :class="{ expanded: galleryExpanded }"
      @click="toggleGalleryExpanded"
    >
      <div class="gallery-toggle-main">
        <div class="gallery-title-row">
          <span class="gallery-toggle-title">{{ PROFILE_VIEW_CONFIG.GALLERY_TITLE }}</span>
          <span class="help-icon-wrap">
            <span class="help-icon" aria-label="提示">i</span>
            <span class="help-tooltip">{{ PROFILE_VIEW_CONFIG.GALLERY_HELP_TEXT }}</span>
          </span>
        </div>
      </div>
      <span class="gallery-toggle-arrow" :class="{ expanded: galleryExpanded }">⌄</span>

      <transition name="gallery-collapse">
        <div v-if="galleryExpanded" class="gallery-panel" @click.stop>
          <div
            v-for="group in galleryGroups"
            :key="group.key"
            class="gallery-block"
          >
            <div class="gallery-head">
              <span class="gallery-title">{{ group.title }}</span>
              <span class="gallery-tip">{{ PROFILE_VIEW_CONFIG.OPTION_GROUP_HINT }}</span>
            </div>
            <div class="gallery-grid" :class="group.gridClass">
              <button
                v-for="option in group.options"
                :key="option"
                type="button"
                class="gallery-option"
                :class="[group.optionClass, { active: profileStyle[group.key] === option }]"
                @click="persistProfileSelection(group.key, option)"
              >
                <img :src="option" :alt="group.imageAlt" />
              </button>
            </div>
          </div>
        </div>
      </transition>
    </div>

    <!-- 操作区 -->
    <div class="section-card action-card">
      <!-- 测试阶段未开启开关功能，当前全部默认自动打卡 -->
      <button class="action-row" @click="handleAutoPunch">
        <div class="action-icon" :style="PROFILE_VIEW_CONFIG.ACTIONS.autoPunch.iconStyle">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        </div>
        <span class="action-label">{{ PROFILE_VIEW_CONFIG.ACTIONS.autoPunch.label }}</span>
        <svg class="chevron" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="9 18 15 12 9 6"/></svg>
      </button>
    </div>

    <div class="section-card action-card danger-card">
      <button class="action-row" @click="handleLogout">
        <div class="action-icon" :style="PROFILE_VIEW_CONFIG.ACTIONS.logout.iconStyle">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
        </div>
        <span class="action-label" :style="{ color: PROFILE_VIEW_CONFIG.ACTIONS.logout.color }">{{ PROFILE_VIEW_CONFIG.ACTIONS.logout.label }}</span>
        <svg class="chevron" width="16" height="16" viewBox="0 0 24 24" fill="none" :stroke="PROFILE_VIEW_CONFIG.ACTIONS.logout.color" stroke-width="2" stroke-linecap="round"><polyline points="9 18 15 12 9 6"/></svg>
      </button>
      <div class="divider"></div>
      <button class="action-row" :disabled="deletingAccount" @click="handleDeleteAccount">
        <div class="action-icon" style="background:#fee2e2; color:#ef4444">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>
        </div>
        <span class="action-label" style="color:#ef4444">{{ deletingAccount ? '注销中...' : '注销账号' }}</span>
        <svg class="chevron" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2" stroke-linecap="round"><polyline points="9 18 15 12 9 6"/></svg>
      </button>
    </div>

      <p class="footer-hint">{{ PROFILE_VIEW_CONFIG.FOOTER_HINT }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { logout, deleteAccount, getPersonalInfo, getUserStatus, getProfileStyle, updateProfileStyle, getProfileDefaultOptions } from '@/api/index'
import {
  APP_CONFIG,
  HTTP_STATUS,
  POLLING_CONFIG,
  PROFILE_THEME_CONFIG,
  PROFILE_VIEW_CONFIG,
  PUNCH_STATUS,
  PUNCH_STATUS_TEXT,
  ROUTE_PATHS,
  STATUS_CLASS,
  STATUS_ICON_STYLE,
  STORAGE_KEYS,
  SYNC_STATUS,
  SYNC_STATUS_TEXT,
  isUserStatusProcessing
} from '@/config'

const personalInfo = ref({})
const userStatus = ref({})
const tokenInfo = ref(null)
const userInfoLoaded = ref(false)
const deletingAccount = ref(false)
const scrollTop = ref(0)
const profileStyle = ref({
  avatar: '',
  background: '',
  wallpaper: ''
})
const cardOpacityControl = ref(PROFILE_VIEW_CONFIG.CARD_BG_OPACITY_DEFAULT)
const profileDefaultOptions = ref({
  avatars: [],
  backgrounds: [],
  wallpapers: []
})
const iconMap = PROFILE_VIEW_CONFIG.ICONS
const galleryExpanded = ref(false)
const profileStyleLoaded = ref(false)
const galleryGroups = computed(() => {
  return PROFILE_VIEW_CONFIG.OPTION_GROUPS.map((group) => ({
    ...group,
    options: profileDefaultOptions.value[group.optionField] || []
  })).filter((group) => group.options.length)
})
let statusTimer = null
let scrollContainer = null
let cardOpacitySaveTimer = null

const getUserInfoFromToken = () => {
  const token = localStorage.getItem(STORAGE_KEYS.TOKEN)
  if (!token) return null
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.student_login
  } catch {
    return null
  }
}

const resolveAssetUrl = (path) => {
  if (!path) return ''
  if (/^https?:\/\//.test(path)) return path
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return `${window.location.protocol}//${window.location.hostname}:8000${normalizedPath}`
}

const toRelativeAssetPath = (url) => {
  if (!url) return ''
  try {
    const parsed = new URL(url)
    return `${parsed.pathname}${parsed.search}`
  } catch {
    return url
  }
}

const fetchPersonalInfo = async () => {
  try {
    const res = await getPersonalInfo()
    if (res.code === HTTP_STATUS.SUCCESS && res.data) {
      personalInfo.value = res.data
    }
  } catch (error) {
    console.error('获取个人信息失败', error)
  } finally {
    userInfoLoaded.value = true
  }
}

const fetchUserStatus = async () => {
  try {
    const res = await getUserStatus()
    if (res.code === HTTP_STATUS.SUCCESS && res.data) {
      userStatus.value = res.data
      
      // 如果处于进行中状态，开启轮询
      if (isUserStatusProcessing(userStatus.value)) {
        startStatusPolling()
      } else {
        stopStatusPolling()
      }
    }
  } catch (error) {
    console.error('获取用户状态失败', error)
    stopStatusPolling()
  }
}

const fetchProfileStyle = async () => {
  try {
    const res = await getProfileStyle()
    if (res.code === HTTP_STATUS.SUCCESS && res.data) {
      profileStyle.value = {
        avatar: resolveAssetUrl(res.data.avatar),
        background: resolveAssetUrl(res.data.background),
        wallpaper: resolveAssetUrl(res.data.wallpaper)
      }
      if (res.data.cardOpacity !== null && res.data.cardOpacity !== undefined) {
        cardOpacityControl.value = Number(res.data.cardOpacity)
      }
    }
  } catch (error) {
    console.error('获取个性化样式失败', error)
  } finally {
    profileStyleLoaded.value = true
  }
}

const fetchProfileDefaultOptions = async () => {
  try {
    const res = await getProfileDefaultOptions()
    if (res.code === HTTP_STATUS.SUCCESS && res.data) {
      profileDefaultOptions.value = {
        avatars: (res.data.avatars || []).map(resolveAssetUrl),
        backgrounds: (res.data.backgrounds || []).map(resolveAssetUrl),
        wallpapers: (res.data.wallpapers || []).map(resolveAssetUrl)
      }
    }
  } catch (error) {
    console.error('获取默认图库失败', error)
  }
}

const saveProfileStyle = async () => {
  try {
    await updateProfileStyle({
      cardOpacity: Number(cardOpacityControl.value.toFixed(2))
    })
  } catch (error) {
    console.error('更新个性化样式失败', error)
  }
}

const persistProfileStyle = async (payload) => {
  try {
    await updateProfileStyle(payload)
  } catch (error) {
    console.error('更新个性化样式失败', error)
  }
}

const persistProfileSelection = async (field, url) => {
  const previousValue = profileStyle.value[field]
  profileStyle.value[field] = url

  try {
    await persistProfileStyle({ [field]: toRelativeAssetPath(url) })
  } catch (error) {
    profileStyle.value[field] = previousValue
    alert('保存个性化设置失败，请稍后重试')
  }
}

const toggleGalleryExpanded = () => {
  galleryExpanded.value = !galleryExpanded.value
}

const startStatusPolling = () => {
  if (statusTimer) return
  statusTimer = setInterval(() => {
    fetchUserStatus()
  }, POLLING_CONFIG.USER_STATUS_INTERVAL)
}

const stopStatusPolling = () => {
  if (statusTimer) {
    clearInterval(statusTimer)
    statusTimer = null
  }
}

const onScroll = () => {
  if (!scrollContainer) return
  scrollTop.value = scrollContainer.scrollTop || 0
}

const bindScrollContainer = () => {
  scrollContainer = document.querySelector('.main-content')
  if (!scrollContainer) return
  scrollContainer.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
}

const unbindScrollContainer = () => {
  if (!scrollContainer) return
  scrollContainer.removeEventListener('scroll', onScroll)
  scrollContainer = null
}

onMounted(() => {
  tokenInfo.value = getUserInfoFromToken()
  const userId = tokenInfo.value?.userid
  bindScrollContainer()
  fetchProfileDefaultOptions()
  if (userId) {
    fetchPersonalInfo()
    fetchUserStatus()
    fetchProfileStyle()
  } else {
    userInfoLoaded.value = true
    profileStyleLoaded.value = true
  }
})

onUnmounted(() => {
  stopStatusPolling()
  unbindScrollContainer()
  if (cardOpacitySaveTimer) {
    clearTimeout(cardOpacitySaveTimer)
    cardOpacitySaveTimer = null
  }
})

const scrollProgress = computed(() => {
  return Math.min(scrollTop.value / PROFILE_VIEW_CONFIG.HERO_COLLAPSE_DISTANCE, 1)
})

const heroHeight = computed(() => {
  const diff = PROFILE_VIEW_CONFIG.HERO_EXPANDED_HEIGHT - PROFILE_VIEW_CONFIG.HERO_COLLAPSED_HEIGHT
  return PROFILE_VIEW_CONFIG.HERO_EXPANDED_HEIGHT - diff * scrollProgress.value
})

const contentOverlay = computed(() => {
  const diff = PROFILE_VIEW_CONFIG.CONTENT_OVERLAY_COLLAPSED - PROFILE_VIEW_CONFIG.CONTENT_OVERLAY_EXPANDED
  return PROFILE_VIEW_CONFIG.CONTENT_OVERLAY_EXPANDED + diff * scrollProgress.value
})

const cardOpacity = computed(() => {
  return Math.max(cardOpacityControl.value - scrollProgress.value * PROFILE_VIEW_CONFIG.CARD_OPACITY_SCROLL_DELTA, PROFILE_VIEW_CONFIG.CARD_BG_OPACITY_MIN)
})

const profileRootStyle = computed(() => {
  const style = {
    '--hero-height': `${heroHeight.value}px`,
    '--content-overlay': `${contentOverlay.value}px`,
    '--card-opacity': cardOpacity.value,
    '--card-blur': `${PROFILE_VIEW_CONFIG.CARD_BLUR}px`,
    '--hero-theme-start': PROFILE_THEME_CONFIG.START,
    '--hero-theme-end': PROFILE_THEME_CONFIG.END,
    '--hero-theme-glow': PROFILE_THEME_CONFIG.GLOW,
    '--tooltip-min-width': `${PROFILE_VIEW_CONFIG.TOOLTIP_MIN_WIDTH}px`,
    '--tooltip-max-width': `${PROFILE_VIEW_CONFIG.TOOLTIP_MAX_WIDTH}px`,
    '--avatar-grid-min': `${PROFILE_VIEW_CONFIG.AVATAR_GRID_MIN}px`,
    '--cover-grid-min': `${PROFILE_VIEW_CONFIG.COVER_GRID_MIN}px`,
    '--cover-card-max-width': `${PROFILE_VIEW_CONFIG.COVER_CARD_MAX_WIDTH}px`
  }

  if (profileStyle.value.wallpaper) {
    style['--page-wallpaper'] = `linear-gradient(rgba(240, 244, 251, ${PROFILE_VIEW_CONFIG.PAGE_WALLPAPER_MASK_ALPHA}), rgba(240, 244, 251, ${PROFILE_VIEW_CONFIG.PAGE_WALLPAPER_MASK_ALPHA})), url(${profileStyle.value.wallpaper})`
  }

  return style
})

const heroShellStyle = computed(() => ({
  height: `${PROFILE_VIEW_CONFIG.HERO_EXPANDED_HEIGHT}px`
}))

const heroSectionStyle = computed(() => ({
  height: `${heroHeight.value}px`
}))

const heroBackgroundStyle = computed(() => {
  if (!profileStyle.value.background) {
    return {}
  }

  return {
    backgroundImage: `url(${profileStyle.value.background})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    backgroundRepeat: 'no-repeat'
  }
})

const wallpaperLayerStyle = computed(() => ({
  backgroundImage: `url(${profileStyle.value.wallpaper})`,
  backgroundSize: 'cover',
  backgroundPosition: 'center',
  backgroundRepeat: 'no-repeat',
  opacity: PROFILE_VIEW_CONFIG.PAGE_WALLPAPER_OPACITY
}))

const avatarRingStyle = computed(() => ({
  transform: `translateY(${scrollProgress.value * -8}px) scale(${1 - scrollProgress.value * 0.06})`
}))

const heroTextStyle = computed(() => ({
  transform: `translateY(${scrollProgress.value * -12}px)`
}))

const heroSubStyle = computed(() => ({
  transform: `translateY(${scrollProgress.value * -12}px)`,
  opacity: 1 - scrollProgress.value * 0.12
}))

const profileContentStyle = computed(() => ({
  marginTop: `calc(var(--content-overlay) * -1)`
}))

watch(cardOpacityControl, (value, oldValue) => {
  if (!profileStyleLoaded.value || value === oldValue) return
  if (cardOpacitySaveTimer) {
    clearTimeout(cardOpacitySaveTimer)
  }
  cardOpacitySaveTimer = setTimeout(() => {
    saveProfileStyle()
  }, PROFILE_VIEW_CONFIG.CARD_OPACITY_SAVE_DEBOUNCE)
})

const firstChar = computed(() => personalInfo.value.name ? personalInfo.value.name.charAt(0) : APP_CONFIG.DEFAULT_AVATAR_TEXT)

const statusText = computed(() => {
  const s = userStatus.value.syncStatus
  return SYNC_STATUS_TEXT[s] || SYNC_STATUS_TEXT.DEFAULT
})

const statusClass = computed(() => {
  const s = userStatus.value.syncStatus
  if (s === SYNC_STATUS.SUCCESS) return STATUS_CLASS.SUCCESS
  if (s === SYNC_STATUS.SYNCING) return STATUS_CLASS.PROCESSING
  if (s === SYNC_STATUS.FAILED) return STATUS_CLASS.FAILED
  return STATUS_CLASS.DEFAULT
})

const syncIconStyle = computed(() => {
  const s = userStatus.value.syncStatus
  if (s === SYNC_STATUS.SUCCESS) return STATUS_ICON_STYLE.SUCCESS
  if (s === SYNC_STATUS.SYNCING) return STATUS_ICON_STYLE.PROCESSING
  if (s === SYNC_STATUS.FAILED) return STATUS_ICON_STYLE.FAILED
  return STATUS_ICON_STYLE.DEFAULT
})

const punchText = computed(() => {
  const p = userStatus.value.punchStatus
  return PUNCH_STATUS_TEXT[p] || PUNCH_STATUS_TEXT.DEFAULT
})

const punchClass = computed(() => {
  const p = userStatus.value.punchStatus
  if (p === PUNCH_STATUS.SUCCESS) return STATUS_CLASS.SUCCESS
  if (p === PUNCH_STATUS.PUNCHING) return STATUS_CLASS.PROCESSING
  if (p === PUNCH_STATUS.FAILED) return STATUS_CLASS.FAILED
  return STATUS_CLASS.DEFAULT
})

const punchIconStyle = computed(() => {
  const p = userStatus.value.punchStatus
  if (p === PUNCH_STATUS.SUCCESS) return STATUS_ICON_STYLE.SUCCESS
  if (p === PUNCH_STATUS.PUNCHING) return STATUS_ICON_STYLE.PROCESSING
  if (p === PUNCH_STATUS.FAILED) return STATUS_ICON_STYLE.FAILED
  return STATUS_ICON_STYLE.DEFAULT
})

// 测试阶段未开启开关功能，当前全部默认自动打卡
const handleAutoPunch = () => {
  alert(APP_CONFIG.AUTO_PUNCH_TIP)
}

const clearAuthAndGoLogin = () => {
  stopStatusPolling()
  localStorage.removeItem(STORAGE_KEYS.TOKEN)
  personalInfo.value = {}
  userStatus.value = {}
  tokenInfo.value = null
  userInfoLoaded.value = false
  window.location.replace(ROUTE_PATHS.LOGIN)
}

const handleLogout = async () => {
  if (!confirm(APP_CONFIG.LOGOUT_CONFIRM)) return
  try {
    await logout()
  } catch (_) {}
  clearAuthAndGoLogin()
}

const handleDeleteAccount = async () => {
  if (deletingAccount.value) return
  if (!confirm(APP_CONFIG.DELETE_ACCOUNT_CONFIRM)) return

  deletingAccount.value = true
  try {
    await deleteAccount()
    try {
      await logout()
    } catch (_) {}
    clearAuthAndGoLogin()
  } catch (error) {
    console.error(error)
  } finally {
    deletingAccount.value = false
  }
}
</script>

<style scoped>
.profile-root {
  --hero-height: 320px;
  --content-overlay: 56px;
  --card-opacity: 0.88;
  --card-blur: 14px;
  --hero-theme-start: #4f86f7;
  --hero-theme-end: #6366f1;
  --hero-theme-glow: rgba(255,255,255,0.16);
  background: #f0f4fb;
  min-height: 100%;
  padding-bottom: 40px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', sans-serif;
  position: relative;
  overflow: hidden;
}
.wallpaper-layer {
  position: absolute;
  top: calc(var(--hero-height) - 36px);
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  pointer-events: none;
  opacity: 0.95;
}

/* ---- Hero ---- */
.hero-shell {
  position: relative;
  overflow: visible;
  z-index: 2;
}
.hero-section {
  position: sticky;
  top: 0;
  z-index: 0;
  padding: 42px 24px 72px;
  text-align: center;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  transition: height 0.18s ease-out;
}
.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(145deg, var(--hero-theme-start) 0%, var(--hero-theme-end) 100%);
  z-index: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}
.hero-bg::before {
  content: '';
  position: absolute;
  top: -12%;
  right: -12%;
  width: 220px;
  height: 220px;
  background: radial-gradient(circle, var(--hero-theme-glow) 0%, rgba(255,255,255,0) 72%);
}
.hero-bg::after {
  content: '';
  position: absolute;
  bottom: -34px;
  left: -12%;
  right: -12%;
  height: 76px;
  background: #f0f4fb;
  border-radius: 50%;
}
.avatar-ring {
  position: relative;
  z-index: 1;
  display: inline-block;
  padding: 4px;
  background: rgba(255,255,255,.24);
  border-radius: 50%;
  margin-bottom: 14px;
  transition: transform 0.16s ease-out;
}
.avatar {
  width: 78px;
  height: 78px;
  background: rgba(255,255,255,0.92);
  color: var(--hero-theme-start);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  font-weight: 800;
  box-shadow: 0 10px 24px rgba(16, 36, 84, 0.12);
  overflow: hidden;
  position: relative;
}
.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.avatar > span {
  position: relative;
  z-index: 1;
}
.user-name {
  position: relative;
  z-index: 1;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 6px;
  transition: transform 0.16s ease-out;
}
.user-sub {
  position: relative;
  z-index: 1;
  font-size: 13px;
  color: rgba(255,255,255,.8);
  transition: transform 0.16s ease-out, opacity 0.16s ease-out;
}

.profile-content {
  position: relative;
  z-index: 2;
  padding: 0 16px 8px;
}

/* ---- Cards ---- */
.section-card {
  margin: 0 0 14px;
  background: rgba(255, 255, 255, var(--card-opacity));
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(15,25,50,.08);
  overflow: hidden;
  backdrop-filter: blur(var(--card-blur));
  -webkit-backdrop-filter: blur(var(--card-blur));
  border: 1px solid rgba(255,255,255,0.36);
}
.info-card {
  overflow: visible;
  padding-top: 10px;
}
.section-card + .section-card { margin-top: 14px; }

/* ---- Info rows ---- */
.info-row {
  display: flex;
  align-items: center;
  padding: 14px 18px;
  gap: 14px;
  border-bottom: 1px solid #f0f4fb;
}
.info-row:last-child { border-bottom: none; }
.info-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.info-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.info-label {
  font-size: 11px;
  color: #b0bdd4;
  letter-spacing: .3px;
}
.info-value {
  font-size: 14px;
  font-weight: 600;
  color: #1a2540;
}
.status-ok { color: #10b981; }
.status-syncing { color: #4f86f7; }
.status-err { color: #ef4444; }

/* ---- Setting card ---- */
.setting-card {
  padding: 18px 18px 20px;
}
.setting-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.gallery-card {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.gallery-toggle {
  width: 100%;
  border: none;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 4px 0;
  text-align: left;
  cursor: pointer;
}
.gallery-toggle-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.gallery-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.gallery-toggle-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}
.help-icon-wrap {
  position: relative;
  display: inline-flex;
  align-items: center;
}
.help-icon {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(100, 116, 139, 0.28);
  background: linear-gradient(180deg, #ffffff 0%, #f5f7fb 100%);
  color: #64748b;
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
  cursor: help;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.08);
  transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease, color 0.15s ease;
}
.help-icon-wrap:hover .help-icon {
  transform: scale(1.08);
  border-color: rgba(79, 134, 247, 0.45);
  color: #4f86f7;
  box-shadow: 0 4px 10px rgba(79, 134, 247, 0.14);
}
.help-tooltip {
  position: absolute;
  left: 50%;
  top: calc(100% + 10px);
  transform: translateX(-50%) translateY(-4px);
  min-width: 220px;
  max-width: 280px;
  padding: 8px 10px;
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.9);
  color: #fff;
  font-size: 12px;
  line-height: 1.5;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.18);
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
  transition: opacity 0.15s ease, transform 0.15s ease, visibility 0.15s ease;
  z-index: 10;
}
.help-tooltip::before {
  content: '';
  position: absolute;
  left: 50%;
  top: -5px;
  width: 10px;
  height: 10px;
  transform: translateX(-50%) rotate(45deg);
  background: rgba(15, 23, 42, 0.9);
}
.help-icon-wrap:hover .help-tooltip {
  opacity: 1;
  visibility: visible;
  transform: translateX(-50%) translateY(0);
}
.gallery-toggle-arrow {
  font-size: 18px;
  color: #64748b;
  transition: transform 0.2s ease;
}
.gallery-toggle-arrow.expanded {
  transform: rotate(180deg);
}
.gallery-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding-top: 6px;
  transform-origin: top center;
}
.gallery-collapse-enter-active,
.gallery-collapse-leave-active {
  transition: opacity 0.24s ease, transform 0.24s ease, max-height 0.28s ease;
  overflow: hidden;
}
.gallery-collapse-enter-from,
.gallery-collapse-leave-to {
  opacity: 0;
  transform: translateY(-8px);
  max-height: 0;
}
.gallery-collapse-enter-to,
.gallery-collapse-leave-from {
  opacity: 1;
  transform: translateY(0);
  max-height: 720px;
}
.gallery-block {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.gallery-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.gallery-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}
.gallery-tip {
  font-size: 12px;
  color: #94a3b8;
}
.gallery-grid {
  display: grid;
  gap: 12px;
}
.avatar-gallery {
  grid-template-columns: repeat(auto-fill, minmax(64px, 1fr));
}
.cover-gallery {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  justify-items: start;
}
.gallery-option {
  width: 100%;
  min-width: 0;
  border: 2px solid transparent;
  background: rgba(255,255,255,0.78);
  border-radius: 16px;
  padding: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
}
.gallery-option:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.1);
}
.gallery-option.active {
  border-color: #4f86f7;
  box-shadow: 0 0 0 3px rgba(79, 134, 247, 0.14);
}
.gallery-option img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  border-radius: 12px;
}
.avatar-option {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  padding: 3px;
}
.avatar-option img {
  border-radius: 50%;
}
.cover-option {
  width: min(100%, var(--cover-card-max-width));
  aspect-ratio: 16 / 10;
}

.setting-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.setting-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a2540;
}
.setting-value {
  font-size: 13px;
  font-weight: 700;
  color: var(--hero-theme-start);
}
.setting-slider {
  width: 100%;
  accent-color: var(--hero-theme-start);
}
/* ---- Action rows ---- */
.action-card { margin-top: 0; }
.action-row {
  width: 100%;
  display: flex;
  align-items: center;
  padding: 14px 18px;
  gap: 14px;
  text-align: left;
  transition: background .15s;
}
.action-row:active { background: #f8faff; }
.action-row:disabled {
  opacity: 0.68;
  cursor: not-allowed;
}
.action-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.action-label {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #1a2540;
}
.chevron { flex-shrink: 0; color: #b0bdd4; }
.divider {
  height: 1px;
  background: #f0f4fb;
  margin: 0 18px;
}

/* ---- Footer ---- */
.footer-hint {
  text-align: center;
  font-size: 11px;
  color: #b0bdd4;
  margin-top: 24px;
  letter-spacing: .3px;
}

@media (min-width: 768px) {
  .profile-root {
    max-width: 720px;
    margin: 0 auto;
    padding-bottom: 56px;
  }

  .hero-section {
    padding-top: 48px;
    padding-bottom: 84px;
  }

  .profile-content {
    padding: 0 24px 12px;
  }
}
</style>
