<template>
  <div class="profile-root" :style="profileRootStyle">
    <div class="wallpaper-layer" :style="wallpaperLayerStyle"></div>
    <div class="profile-shell">
      <div class="hero-shell" :style="heroShellStyle">
        <div class="hero-section" :style="heroSectionStyle">
          <div class="hero-bg" :style="heroBackgroundStyle"></div>

          <div class="avatar-ring" :style="avatarRingStyle">
            <div class="avatar">
              <img
                v-if="profileStyle.avatar"
                class="avatar-image"
                :src="profileStyle.avatar"
                alt="用户头像"
              />
              <span v-else>{{ firstChar }}</span>
            </div>
          </div>

          <h2 class="user-name" :style="heroTextStyle">
            {{ personalInfo.name || APP_CONFIG.DEFAULT_USER_NAME }}
          </h2>
          <p class="user-sub" :style="heroSubStyle">
            {{ personalInfo.studentId || tokenInfo?.studentID || APP_CONFIG.UNKNOWN_STUDENT_ID }}
          </p>
        </div>
      </div>

      <div class="profile-content" :style="profileContentStyle">
        <ProfileInfoCard
          :profile-view-config="PROFILE_VIEW_CONFIG"
          :app-config="APP_CONFIG"
          :icon-map="iconMap"
          :personal-info="personalInfo"
          :user-info-loaded="userInfoLoaded"
          :sync-icon-style="syncIconStyle"
          :status-class="statusClass"
          :status-text="statusText"
          :punch-icon-style="punchIconStyle"
          :punch-class="punchClass"
          :punch-text="punchText"
        />

        <ProfilePersonalizationCard
          :profile-view-config="PROFILE_VIEW_CONFIG"
          :display-settings="displaySettings"
          :gallery-groups="galleryGroups"
          :format-display-setting-value="formatDisplaySettingValue"
          :is-gallery-option-active="isGalleryOptionActive"
          @reset-profile="resetProfileToMinimalDefault"
          @update-display-setting="updateDisplaySetting"
          @gallery-option-click="handleGalleryOptionClick"
          @file-input-ref="setFileInputRef"
          @local-file-change="handleLocalFileChange"
        />

        <ProfileActionCards
          :profile-view-config="PROFILE_VIEW_CONFIG"
          :action-toggles="actionToggles"
          :deleting-account="deletingAccount"
          @update-action-toggle="updateActionToggle"
          @logout="handleLogout"
          @delete-account="handleDeleteAccount"
        />
      </div>

      <ImageCropUploadModal
        :visible="cropModalVisible"
        :source-url="cropSourceUrl"
        :title="`${PROFILE_VIEW_CONFIG.CROP_MODAL_TITLE} · ${getCropPreset(cropTargetType).label}`"
        :hint="PROFILE_VIEW_CONFIG.CROP_MODAL_HINT"
        :aspect-ratio="getCropPreset(cropTargetType).aspectRatio"
        :max-viewport-width="getCropPreset(cropTargetType).maxWidth"
        :max-viewport-height="getCropPreset(cropTargetType).maxHeight"
        :output-width="getCropPreset(cropTargetType).outputWidth"
        :output-height="getCropPreset(cropTargetType).outputHeight"
        :file-name="cropSourceFileName"
        :confirming="cropUploading"
        @cancel="closeCropModal"
        @confirm="handleCropConfirm"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import ImageCropUploadModal from '@/components/ImageCropUploadModal.vue'
import ProfilePersonalizationCard from '@/components/ProfilePersonalizationCard.vue'
import ProfileInfoCard from '@/components/ProfileInfoCard.vue'
import ProfileActionCards from '@/components/ProfileActionCards.vue'
import {
  deleteAccount,
  getPersonalInfo,
  getUserStatus,
  logout,
  updateAutoPunch
} from '@/api/index'
import {
  APP_CONFIG,
  HTTP_STATUS,
  POLLING_CONFIG,
  PROFILE_VIEW_CONFIG,
  PUNCH_STATUS,
  PUNCH_STATUS_TEXT,
  ROUTE_PATHS,
  STATUS_CLASS,
  STATUS_ICON_STYLE,
  SYNC_STATUS,
  SYNC_STATUS_TEXT,
  isUserStatusProcessing
} from '@/config'
import { clearUserSession, getUserToken } from '@/utils/auth'
import { useProfileStyle } from '@/composables/useProfileStyle'
import { useProfileAssets } from '@/composables/useProfileAssets'

const personalInfo = ref({})
const userStatus = ref({})
const tokenInfo = ref(null)
const userInfoLoaded = ref(false)
const deletingAccount = ref(false)
const autoPunchEnabled = ref(true)
const scrollTop = ref(0)

const profileStyle = ref({
  avatar: '',
  background: '',
  wallpaper: ''
})


const iconMap = PROFILE_VIEW_CONFIG.ICONS

let statusTimer = null
let scrollContainer = null

const {
  displaySettings,
  profileRootStyle,
  heroShellStyle,
  heroSectionStyle,
  heroBackgroundStyle,
  wallpaperLayerStyle,
  avatarRingStyle,
  heroTextStyle,
  heroSubStyle,
  profileContentStyle,
  formatDisplaySettingValue,
  updateDisplaySetting,
  fetchProfileStyle,
  persistProfileStylePatch,
  resetProfileToMinimalDefault,
  clearProfileStyleTimers,
  markProfileStyleReady
} = useProfileStyle({
  profileStyle,
  scrollTop
})


const decodeBase64Url = (value = '') => {
  try {
    const normalized = value.replace(/-/g, '+').replace(/_/g, '/')
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')
    return window.atob(padded)
  } catch {
    return ''
  }
}

const getUserInfoFromToken = () => {
  const token = getUserToken()
  if (!token) return null

  const parts = token.split('.')
  if (parts.length < 2) return null

  try {
    const payload = JSON.parse(decodeBase64Url(parts[1]))
    return payload?.student_login || payload || null
  } catch (error) {
    console.error('解析用户令牌失败', error)
    return null
  }
}

const actionToggles = computed(() => {
  return PROFILE_VIEW_CONFIG.ACTION_TOGGLES.map((toggle) => ({
    ...toggle,
    currentValue: autoPunchEnabled.value,
    description: autoPunchEnabled.value ? toggle.enabledText : toggle.disabledText
  }))
})
const {
  cropModalVisible,
  cropUploading,
  cropTargetType,
  cropSourceUrl,
  cropSourceFileName,
  galleryGroups,
  fetchProfileDefaultOptions,
  fetchProfileCustomAssets,
  setFileInputRef,
  getCropPreset,
  closeCropModal,
  handleLocalFileChange,
  handleCropConfirm,
  handleGalleryOptionClick,
  isGalleryOptionActive,
  ensureDefaultProfileAssets
} = useProfileAssets({
  profileStyle,
  persistProfileStylePatch
})
const firstChar = computed(() => {
  return personalInfo.value.name
    ? personalInfo.value.name.charAt(0)
    : APP_CONFIG.DEFAULT_AVATAR_TEXT
})

const statusText = computed(() => {
  const syncStatus = userStatus.value.syncStatus
  return SYNC_STATUS_TEXT[syncStatus] || SYNC_STATUS_TEXT.DEFAULT
})

const statusClass = computed(() => {
  const syncStatus = userStatus.value.syncStatus
  if (syncStatus === SYNC_STATUS.SUCCESS) return STATUS_CLASS.SUCCESS
  if (syncStatus === SYNC_STATUS.SYNCING) return STATUS_CLASS.PROCESSING
  if (syncStatus === SYNC_STATUS.FAILED) return STATUS_CLASS.FAILED
  return STATUS_CLASS.DEFAULT
})

const syncIconStyle = computed(() => {
  const syncStatus = userStatus.value.syncStatus
  if (syncStatus === SYNC_STATUS.SUCCESS) return STATUS_ICON_STYLE.SUCCESS
  if (syncStatus === SYNC_STATUS.SYNCING) return STATUS_ICON_STYLE.PROCESSING
  if (syncStatus === SYNC_STATUS.FAILED) return STATUS_ICON_STYLE.FAILED
  return STATUS_ICON_STYLE.DEFAULT
})

const punchText = computed(() => {
  const punchStatus = userStatus.value.punchStatus
  return PUNCH_STATUS_TEXT[punchStatus] || PUNCH_STATUS_TEXT.DEFAULT
})

const punchClass = computed(() => {
  const punchStatus = userStatus.value.punchStatus
  if (punchStatus === PUNCH_STATUS.SUCCESS) return STATUS_CLASS.SUCCESS
  if (punchStatus === PUNCH_STATUS.PUNCHING) return STATUS_CLASS.PROCESSING
  if (punchStatus === PUNCH_STATUS.FAILED) return STATUS_CLASS.FAILED
  return STATUS_CLASS.DEFAULT
})

const punchIconStyle = computed(() => {
  const punchStatus = userStatus.value.punchStatus
  if (punchStatus === PUNCH_STATUS.SUCCESS) return STATUS_ICON_STYLE.SUCCESS
  if (punchStatus === PUNCH_STATUS.PUNCHING) return STATUS_ICON_STYLE.PROCESSING
  if (punchStatus === PUNCH_STATUS.FAILED) return STATUS_ICON_STYLE.FAILED
  return STATUS_ICON_STYLE.DEFAULT
})

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

      if (res.data.autoPunchEnabled !== null && res.data.autoPunchEnabled !== undefined) {
        autoPunchEnabled.value = Number(res.data.autoPunchEnabled) === 1
      }

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

const updateAutoPunchEnabled = async (enabled) => {
  const previousValue = autoPunchEnabled.value
  autoPunchEnabled.value = enabled

  try {
    await updateAutoPunch({ autoPunchEnabled: enabled ? 1 : 0 })
    userStatus.value = {
      ...userStatus.value,
      autoPunchEnabled: enabled ? 1 : 0
    }
  } catch (error) {
    autoPunchEnabled.value = previousValue
    console.error('更新自动打卡开关失败', error)
  }
}

const updateActionToggle = async (key, value) => {
  if (key === 'autoPunchEnabled') {
    await updateAutoPunchEnabled(value)
  }
}

const startStatusPolling = () => {
  if (statusTimer) return

  statusTimer = setInterval(() => {
    fetchUserStatus()
  }, POLLING_CONFIG.USER_STATUS_INTERVAL)
}

const stopStatusPolling = () => {
  if (!statusTimer) return

  clearInterval(statusTimer)
  statusTimer = null
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

const clearAuthAndGoLogin = () => {
  stopStatusPolling()
  clearUserSession()
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
  } catch (_) {
  }

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
    } catch (_) {
    }
    clearAuthAndGoLogin()
  } catch (error) {
    console.error(error)
  } finally {
    deletingAccount.value = false
  }
}

onMounted(() => {
  tokenInfo.value = getUserInfoFromToken()
  const token = getUserToken()
  bindScrollContainer()
  fetchProfileDefaultOptions()
  fetchProfileCustomAssets()

  if (token) {
    fetchPersonalInfo()
    fetchUserStatus()
    fetchProfileStyle().finally(() => ensureDefaultProfileAssets())
  } else {
    userInfoLoaded.value = true
    ensureDefaultProfileAssets()
    markProfileStyleReady()
  }
})

onUnmounted(() => {
  stopStatusPolling()
  unbindScrollContainer()
  clearProfileStyleTimers()
})
</script>

<style scoped>
.profile-root {
  --hero-height: 320px;
  --content-overlay: 56px;
  --card-opacity: 0.88;
  --wallpaper-scale: 1;
  --page-wallpaper-mask-alpha: 0.14;
  --hero-frame-opacity: 0.24;
  --hero-section-padding-top: 40px;
  --hero-section-padding-x: 24px;
  --hero-section-padding-bottom: 94px;
  --hero-bg-side-inset: 18px;
  --hero-bg-bottom-inset: 14px;
  --hero-bg-radius-top: 34px;
  --hero-bg-radius-bottom: 40px;
  --hero-bg-shadow: 0 12px 28px rgba(15, 25, 50, 0.06);
  --hero-frame-side-inset: 10px;
  --hero-frame-extra-height: 68px;
  --hero-frame-radius-top: 40px;
  --hero-frame-radius-bottom: 44px;
  --hero-frame-bg: #ffffff;
  --hero-frame-shadow: 0 10px 22px rgba(15, 25, 50, 0.04);
  --hero-blend-height: 174px;
  --hero-glow-size: 220px;
  --hero-glow-top: -12%;
  --hero-glow-right: -12%;
  --hero-theme-start: #4f86f7;
  --hero-theme-end: #6366f1;
  --hero-theme-glow: rgba(255, 255, 255, 0.16);
  background: #e9eef8;
  min-height: 100vh;
  padding-bottom: 40px;
  position: relative;
  width: 100%;
  overflow-x: hidden;
}

.wallpaper-layer {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  opacity: 1;
  will-change: transform;
  transform-origin: center top;
  transition: transform 0.18s ease-out;
  filter: saturate(1.04);
}

.profile-shell {
  position: relative;
  z-index: 2;
  width: min(100%, 720px);
  margin: 0 auto;
}

.wallpaper-layer::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(233, 238, 248, calc(var(--page-wallpaper-mask-alpha) * 0.9)) 0%,
    rgba(233, 238, 248, calc(var(--page-wallpaper-mask-alpha) * 0.36)) 32%,
    rgba(233, 238, 248, calc(var(--page-wallpaper-mask-alpha) * 0.72)) 100%
  );
}

.hero-shell {
  position: relative;
  overflow: visible;
  z-index: 1;
  pointer-events: none;
}

.hero-section {
  position: sticky;
  top: 0;
  z-index: 0;
  pointer-events: none;
  padding: var(--hero-section-padding-top) var(--hero-section-padding-x) var(--hero-section-padding-bottom);
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
  top: var(--hero-surface-offset, -54px);
  left: var(--hero-bg-side-inset);
  right: var(--hero-bg-side-inset);
  bottom: var(--hero-bg-bottom-inset);
  background: linear-gradient(145deg, var(--hero-theme-start) 0%, var(--hero-theme-end) 100%);
  z-index: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  border-radius: var(--hero-bg-radius-top) var(--hero-bg-radius-top) var(--hero-bg-radius-bottom) var(--hero-bg-radius-bottom);
  overflow: hidden;
  box-shadow: var(--hero-bg-shadow);
}

.hero-section::before {
  content: '';
  position: absolute;
  left: var(--hero-frame-side-inset);
  right: var(--hero-frame-side-inset);
  top: var(--hero-surface-offset, -54px);
  height: calc(var(--hero-surface-height, calc(var(--hero-height) - 28px)) + var(--hero-frame-extra-height));
  border-radius: var(--hero-frame-radius-top) var(--hero-frame-radius-top) var(--hero-frame-radius-bottom) var(--hero-frame-radius-bottom);
  background: var(--hero-frame-bg);
  box-shadow: var(--hero-frame-shadow);
  z-index: 0;
}

.hero-bg::before {
  content: '';
  position: absolute;
  top: var(--hero-glow-top);
  right: var(--hero-glow-right);
  width: var(--hero-glow-size);
  height: var(--hero-glow-size);
  background: radial-gradient(circle, var(--hero-theme-glow) 0%, rgba(255, 255, 255, 0) 72%);
}

.hero-bg::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: var(--hero-blend-height);
  background: linear-gradient(
    180deg,
    rgba(240, 244, 251, 0) 0%,
    rgba(240, 244, 251, calc(var(--page-wallpaper-mask-alpha) * 0.52)) 56%,
    rgba(240, 244, 251, calc(var(--page-wallpaper-mask-alpha) * 0.82)) 100%
  );
}

.avatar-ring {
  position: relative;
  z-index: 1;
  pointer-events: auto;
  display: inline-block;
  padding: 4px;
  background: rgba(255, 255, 255, 0.24);
  border-radius: 50%;
  margin-bottom: 14px;
  transition: transform 0.16s ease-out;
}

.avatar {
  width: 78px;
  height: 78px;
  background: rgba(255, 255, 255, 0.92);
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
  pointer-events: auto;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 6px;
  transition: transform 0.16s ease-out;
}

.user-sub {
  position: relative;
  z-index: 1;
  pointer-events: auto;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  transition: transform 0.16s ease-out, opacity 0.16s ease-out;
}

.profile-content {
  position: relative;
  z-index: 3;
  padding: 0 18px 12px;
}

.section-card {
  margin: 0 0 14px;
  background: rgba(255, 255, 255, var(--card-opacity));
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 14px 36px rgba(15, 25, 50, 0.08);
  backdrop-filter: blur(var(--card-blur)) saturate(1.06);
  -webkit-backdrop-filter: blur(var(--card-blur)) saturate(1.06);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.section-card + .section-card {
  margin-top: 14px;
}

@media (min-width: 768px) {
  .profile-root {
    padding-bottom: 56px;
  }

  .profile-shell {
    width: min(100%, 720px);
  }

  .hero-section {
    padding-top: 48px;
    padding-bottom: 84px;
  }

  .profile-content {
    padding: 0 24px 12px;
  }
}

@font-face {
  font-family: 'SourceHanSerifCN';
  src: url('/fonts/SourceHanSerifCN-Regular.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}
</style>























