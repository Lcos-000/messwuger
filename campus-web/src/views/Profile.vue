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
      <div class="section-card info-card">
        <div
          v-for="field in PROFILE_VIEW_CONFIG.INFO_FIELDS"
          :key="field.key"
          class="info-row"
          v-show="userInfoLoaded"
        >
          <div class="info-icon" :style="field.iconStyle">
            <svg
              v-if="field.icon === iconMap.college"
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M3 21v-8M21 21v-8M3 13L12 3l9 10M12 3v18" />
            </svg>
            <svg
              v-else-if="field.icon === iconMap.major"
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M22 10v6M2 10l10-5 10 5-10 5z" />
              <path d="M6 12v5c3 3 9 3 12 0v-5" />
            </svg>
            <svg
              v-else
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
              <polyline points="9 22 9 12 15 12 15 22" />
            </svg>
          </div>

          <div class="info-content">
            <span class="info-label">{{ field.label }}</span>
            <span class="info-value">{{ personalInfo[field.key] || APP_CONFIG[field.fallback] }}</span>
          </div>
        </div>

        <div class="info-row" v-show="userInfoLoaded">
          <div class="info-icon" :style="syncIconStyle">
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <polyline points="23 4 23 10 17 10" />
              <polyline points="1 20 1 14 7 14" />
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" />
            </svg>
          </div>
          <div class="info-content">
            <span class="info-label">同步状态</span>
            <span class="info-value" :class="statusClass">{{ statusText }}</span>
          </div>
        </div>

        <div class="info-row" v-show="userInfoLoaded">
          <div class="info-icon" :style="punchIconStyle">
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
          </div>
          <div class="info-content">
            <span class="info-label">{{ PROFILE_VIEW_CONFIG.STATUS_FIELDS[1].label }}</span>
            <span class="info-value" :class="punchClass">{{ punchText }}</span>
          </div>
        </div>
      </div>

      <div
        class="section-card gallery-card"
        :class="{ expanded: galleryExpanded }"
        @click="toggleGalleryExpanded"
      >
        <div class="gallery-toggle-main">
          <div class="gallery-title-row">
            <span class="gallery-toggle-title">{{ PROFILE_VIEW_CONFIG.GALLERY_TITLE }}</span>
            <button
              type="button"
              class="help-icon-wrap"
              :class="{ active: activeTooltipKey === 'gallery-help' }"
              aria-label="查看提示"
              @click.stop="toggleTooltip('gallery-help')"
            >
              <span class="help-icon" aria-hidden="true">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="9"></circle>
                  <path d="M9.6 9.2a2.5 2.5 0 0 1 4.8.9c0 1.6-1.4 2.3-2.2 2.9-.6.4-.9.8-.9 1.5"></path>
                  <circle cx="12" cy="17.2" r="0.8" fill="currentColor" stroke="none"></circle>
                </svg>
              </span>
              <span class="help-tooltip">{{ PROFILE_VIEW_CONFIG.GALLERY_HELP_TEXT }}</span>
            </button>
          </div>
        </div>
        <span class="gallery-toggle-arrow" :class="{ expanded: galleryExpanded }">⌄</span>

        <transition name="gallery-collapse">
          <div v-if="galleryExpanded" class="gallery-panel" @click.stop>
            <div class="gallery-settings">
              <div class="gallery-settings-actions">
                <button type="button" class="reset-profile-button" @click="resetProfileToMinimalDefault">
                  {{ PROFILE_VIEW_CONFIG.RESET_PROFILE_TEXT }}
                </button>
              </div>
              <div
                v-for="setting in displaySettings"
                :key="setting.key"
                class="setting-block"
                :class="{ 'font-setting-block': setting.type === 'toggle' }"
              >
                <div class="setting-head">
                  <div class="setting-title-row">
                    <span class="setting-title">{{ setting.title }}</span>
                    <button
                      type="button"
                      class="help-icon-wrap"
                      :class="{ active: activeTooltipKey === `setting-${setting.key}` }"
                      aria-label="查看提示"
                      @click.stop="toggleTooltip(`setting-${setting.key}`)"
                    >
                      <span class="help-icon" aria-hidden="true">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round" stroke-linejoin="round">
                          <circle cx="12" cy="12" r="9"></circle>
                          <path d="M9.6 9.2a2.5 2.5 0 0 1 4.8.9c0 1.6-1.4 2.3-2.2 2.9-.6.4-.9.8-.9 1.5"></path>
                          <circle cx="12" cy="17.2" r="0.8" fill="currentColor" stroke="none"></circle>
                        </svg>
                      </span>
                      <span class="help-tooltip">{{ setting.helpText }}</span>
                    </button>
                  </div>

                  <span v-if="setting.type === 'range'" class="setting-value">
                    {{ formatDisplaySettingValue(setting) }}
                  </span>
                  <label v-else class="toggle-switch">
                    <input
                      type="checkbox"
                      :checked="setting.currentValue"
                      @change="updateDisplaySetting(setting.key, $event.target.checked)"
                    />
                    <span class="toggle-slider"></span>
                  </label>
                </div>

                <input
                  v-if="setting.type === 'range'"
                  :value="setting.currentValue"
                  class="setting-slider"
                  type="range"
                  :min="setting.min"
                  :max="setting.max"
                  :step="setting.step"
                  @input="updateDisplaySetting(setting.key, $event.target.value)"
                />
              </div>
            </div>

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
                <div
                  v-for="option in group.options"
                  :key="option.key"
                  class="gallery-option-shell"
                  :class="{ 'gallery-option-shell--with-caption': !!option.caption }"
                >
                  <button
                    type="button"
                    class="gallery-option"
                    :style="option.style || null"
                    :data-upload-label="option.type === 'upload' && option.previewUrl && group.key !== 'avatar' ? PROFILE_VIEW_CONFIG.REPLACE_TILE_TEXT : ''"
                    :class="[
                      group.optionClass,
                      `gallery-option--${option.type}`,
                      { active: isGalleryOptionActive(group.key, option), 'gallery-option--custom': option.source === 'custom' }
                    ]"
                    @click="handleGalleryOptionClick(group.key, option)"
                  >
                    <template v-if="option.type === 'upload'">
                      <span class="upload-option-body">
                        <span class="upload-option-icon">+</span>
                        <span v-if="!option.previewUrl" class="upload-option-title">{{ PROFILE_VIEW_CONFIG.UPLOAD_TILE_TEXT }}</span>
                        <span v-if="!option.previewUrl" class="upload-option-subtitle">{{ PROFILE_VIEW_CONFIG.UPLOAD_TILE_SUBTEXT }}</span>
                      </span>
                    </template>
                    <template v-else>
                      <img :src="option.url" :alt="group.imageAlt" />
                      <span v-if="option.source === 'custom'" class="custom-badge">{{ PROFILE_VIEW_CONFIG.CUSTOM_BADGE_TEXT }}</span>
                    </template>
                  </button>
                  <span v-if="option.caption" class="gallery-option-caption">{{ option.caption }}</span>
                </div>
                <input
                  :ref="(element) => setFileInputRef(group.key, element)"
                  class="gallery-file-input"
                  type="file"
                  accept="image/*"
                  @change="handleLocalFileChange(group.key, $event)"
                />
              </div>
            </div>
          </div>
        </transition>
      </div>

      <div class="section-card action-card">
        <div
          v-for="toggle in actionToggles"
          :key="toggle.key"
          class="action-row auto-punch-row"
        >
          <div class="action-icon" :style="toggle.iconStyle">
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
          </div>

          <div class="action-main">
            <span class="action-label">{{ toggle.label }}</span>
            <span class="action-subtitle">{{ toggle.description }}</span>
          </div>

          <label class="toggle-switch" @click.stop>
            <input
              type="checkbox"
              :checked="toggle.currentValue"
              @change="updateActionToggle(toggle.key, $event.target.checked)"
            />
            <span class="toggle-slider"></span>
          </label>
        </div>
      </div>

      <div class="section-card action-card danger-card">
        <button class="action-row" @click="handleLogout">
          <div class="action-icon" :style="PROFILE_VIEW_CONFIG.ACTIONS.logout.iconStyle">
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
              <polyline points="16 17 21 12 16 7" />
              <line x1="21" y1="12" x2="9" y2="12" />
            </svg>
          </div>
          <span class="action-label" :style="{ color: PROFILE_VIEW_CONFIG.ACTIONS.logout.color }">
            {{ PROFILE_VIEW_CONFIG.ACTIONS.logout.label }}
          </span>
          <svg
            class="chevron"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            :stroke="PROFILE_VIEW_CONFIG.ACTIONS.logout.color"
            stroke-width="2"
            stroke-linecap="round"
          >
            <polyline points="9 18 15 12 9 6" />
          </svg>
        </button>

        <div class="divider"></div>

        <button class="action-row" :disabled="deletingAccount" @click="handleDeleteAccount">
          <div class="action-icon" style="background:#fee2e2; color:#ef4444">
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <polyline points="3 6 5 6 21 6" />
              <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
              <path d="M10 11v6" />
              <path d="M14 11v6" />
              <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
            </svg>
          </div>
          <span class="action-label" style="color:#ef4444">
            {{ deletingAccount ? '注销中...' : '注销账号' }}
          </span>
          <svg
            class="chevron"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="#ef4444"
            stroke-width="2"
            stroke-linecap="round"
          >
            <polyline points="9 18 15 12 9 6" />
          </svg>
        </button>
      </div>

      <p class="footer-hint">{{ PROFILE_VIEW_CONFIG.FOOTER_HINT }}</p>
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
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import ImageCropUploadModal from '@/components/ImageCropUploadModal.vue'
import {
  deleteAccount,
  getPersonalInfo,
  getProfileCustomAssets,
  getProfileDefaultOptions,
  getProfileStyle,
  getUserStatus,
  logout,
  uploadProfileCustomAsset,
  updateAutoPunch,
  updateProfileStyle
} from '@/api/index'
import {
  API_PATHS,
  APP_CONFIG,
  HTTP_CONFIG,
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
const autoPunchEnabled = ref(true)
const scrollTop = ref(0)
const galleryExpanded = ref(false)
const profileStyleLoaded = ref(false)
const activeTooltipKey = ref('')

const profileStyle = ref({
  avatar: '',
  background: '',
  wallpaper: ''
})

const profileDefaultOptions = ref({
  avatars: [],
  backgrounds: [],
  wallpapers: []
})

const customAssets = ref({
  customAvatar: '',
  customBackground: '',
  customWallpaper: ''
})

const cropModalVisible = ref(false)
const cropUploading = ref(false)
const cropTargetType = ref('avatar')
const cropSourceUrl = ref('')
const cropSourceFileName = ref('upload-image.jpg')
const fileInputRefs = {}

const cardOpacityControl = ref(PROFILE_VIEW_CONFIG.CARD_BG_OPACITY_DEFAULT)
const cardBlurControl = ref(PROFILE_VIEW_CONFIG.CARD_BLUR_DEFAULT)
const wallpaperMaskControl = ref(PROFILE_VIEW_CONFIG.WALLPAPER_MASK_DEFAULT)
const globalFontEnabled = ref(true)

const iconMap = PROFILE_VIEW_CONFIG.ICONS

let statusTimer = null
let scrollContainer = null
let cardOpacitySaveTimer = null
let cardBlurSaveTimer = null
let wallpaperMaskSaveTimer = null
let globalFontSaveTimer = null
const profileStyleDirty = ref(false)

const galleryGroups = computed(() => {
  return PROFILE_VIEW_CONFIG.OPTION_GROUPS.map((group) => {
    const customUrl = customAssets.value[group.customField] || ''
    const defaultOptions = profileDefaultOptions.value[group.optionField] || []
    const options = [
      {
        key: `${group.key}-upload`,
        type: 'upload',
        source: customUrl ? 'custom' : 'upload',
        previewUrl: customUrl,
        style: customUrl ? { backgroundImage: `linear-gradient(rgba(15, 23, 42, 0.18), rgba(15, 23, 42, 0.18)), url(${customUrl})`, backgroundSize: 'cover', backgroundPosition: 'center' } : null
      }
    ]

    defaultOptions.forEach((option, index) => {
      options.push({
        key: `${group.key}-default-${index}`,
        type: 'image',
        source: 'default',
        url: option,
        caption: group.key === 'avatar' ? (PROFILE_VIEW_CONFIG.AVATAR_CAPTIONS[index] || '') : ''
      })
    })

    return {
      ...group,
      options
    }
  })
})

const displaySettings = computed(() => {
  return PROFILE_VIEW_CONFIG.DISPLAY_SETTINGS.map((setting) => {
    if (setting.key === 'cardOpacity') {
      return { ...setting, currentValue: cardOpacityControl.value }
    }
    if (setting.key === 'cardBlur') {
      return { ...setting, currentValue: cardBlurControl.value }
    }
    if (setting.key === 'wallpaperMask') {
      return { ...setting, currentValue: wallpaperMaskControl.value }
    }
    return { ...setting, currentValue: globalFontEnabled.value }
  })
})

const actionToggles = computed(() => {
  return PROFILE_VIEW_CONFIG.ACTION_TOGGLES.map((toggle) => ({
    ...toggle,
    currentValue: autoPunchEnabled.value,
    description: autoPunchEnabled.value ? toggle.enabledText : toggle.disabledText
  }))
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

const wallpaperScale = computed(() => {
  const scale = PROFILE_VIEW_CONFIG.WALLPAPER_SCALE_MIN + scrollProgress.value * PROFILE_VIEW_CONFIG.WALLPAPER_SCALE_DELTA
  return Math.min(scale, PROFILE_VIEW_CONFIG.WALLPAPER_SCALE_MAX)
})

const cardOpacity = computed(() => {
  return Math.max(
    cardOpacityControl.value - scrollProgress.value * PROFILE_VIEW_CONFIG.CARD_OPACITY_SCROLL_DELTA,
    PROFILE_VIEW_CONFIG.CARD_BG_OPACITY_MIN
  )
})

const heroFrameOpacity = computed(() => {
  return 0.18 + wallpaperMaskControl.value * 0.34
})

const profileRootStyle = computed(() => {
  const style = {
    '--hero-height': `${heroHeight.value}px`,
    '--content-overlay': `${contentOverlay.value}px`,
    '--card-opacity': cardOpacity.value,
    '--card-blur': `${cardBlurControl.value}px`,
    '--wallpaper-scale': wallpaperScale.value,
    '--page-wallpaper-mask-alpha': wallpaperMaskControl.value,
    '--hero-frame-opacity': heroFrameOpacity.value,
    '--hero-theme-start': PROFILE_THEME_CONFIG.START,
    '--hero-theme-end': PROFILE_THEME_CONFIG.END,
    '--hero-theme-glow': PROFILE_THEME_CONFIG.GLOW,
    '--tooltip-min-width': `${PROFILE_VIEW_CONFIG.TOOLTIP_MIN_WIDTH}px`,
    '--tooltip-max-width': `${PROFILE_VIEW_CONFIG.TOOLTIP_MAX_WIDTH}px`,
    '--avatar-grid-min': `${PROFILE_VIEW_CONFIG.AVATAR_GRID_MIN}px`,
    '--cover-grid-min': `${PROFILE_VIEW_CONFIG.COVER_GRID_MIN}px`,
    '--cover-card-max-width': `${PROFILE_VIEW_CONFIG.COVER_CARD_MAX_WIDTH}px`
  }

  style.fontFamily = globalFontEnabled.value
    ? `'${PROFILE_VIEW_CONFIG.FONT_FACE.family}', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', sans-serif`
    : "-apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', sans-serif"

  style['--hero-section-padding-top'] = `${PROFILE_VIEW_CONFIG.HERO_SECTION_PADDING_TOP}px`
  style['--hero-section-padding-x'] = `${PROFILE_VIEW_CONFIG.HERO_SECTION_PADDING_X}px`
  style['--hero-section-padding-bottom'] = `${PROFILE_VIEW_CONFIG.HERO_SECTION_PADDING_BOTTOM}px`
  style['--hero-bg-side-inset'] = `${PROFILE_VIEW_CONFIG.HERO_BG_SIDE_INSET}px`
  style['--hero-bg-bottom-inset'] = `${PROFILE_VIEW_CONFIG.HERO_BG_BOTTOM_INSET}px`
  style['--hero-bg-radius-top'] = `${PROFILE_VIEW_CONFIG.HERO_BG_RADIUS_TOP}px`
  style['--hero-bg-radius-bottom'] = `${PROFILE_VIEW_CONFIG.HERO_BG_RADIUS_BOTTOM}px`
  style['--hero-bg-shadow'] = PROFILE_VIEW_CONFIG.HERO_BG_SHADOW
  style['--hero-frame-side-inset'] = `${PROFILE_VIEW_CONFIG.HERO_FRAME_SIDE_INSET}px`
  style['--hero-frame-extra-height'] = `${PROFILE_VIEW_CONFIG.HERO_FRAME_EXTRA_HEIGHT}px`
  style['--hero-frame-radius-top'] = `${PROFILE_VIEW_CONFIG.HERO_FRAME_RADIUS_TOP}px`
  style['--hero-frame-radius-bottom'] = `${PROFILE_VIEW_CONFIG.HERO_FRAME_RADIUS_BOTTOM}px`
  style['--hero-frame-bg'] = PROFILE_VIEW_CONFIG.HERO_FRAME_BG
  style['--hero-frame-shadow'] = PROFILE_VIEW_CONFIG.HERO_FRAME_SHADOW
  style['--hero-blend-height'] = `${PROFILE_VIEW_CONFIG.HERO_BLEND_HEIGHT}px`
  style['--hero-glow-size'] = `${PROFILE_VIEW_CONFIG.HERO_GLOW_SIZE}px`
  style['--hero-glow-top'] = PROFILE_VIEW_CONFIG.HERO_GLOW_TOP
  style['--hero-glow-right'] = PROFILE_VIEW_CONFIG.HERO_GLOW_RIGHT
  return style
})

const heroShellStyle = computed(() => ({
  height: `${PROFILE_VIEW_CONFIG.HERO_EXPANDED_HEIGHT}px`
}))

const heroSectionStyle = computed(() => ({
  height: `${heroHeight.value}px`,
  '--hero-surface-height': `${Math.max(
    heroHeight.value - PROFILE_VIEW_CONFIG.HERO_SURFACE_HEIGHT_OFFSET,
    PROFILE_VIEW_CONFIG.HERO_COLLAPSED_HEIGHT - PROFILE_VIEW_CONFIG.HERO_SURFACE_MIN_HEIGHT_OFFSET
  )}px`,
  '--hero-surface-offset': `${Math.min(
    scrollProgress.value * PROFILE_VIEW_CONFIG.HERO_SURFACE_SCROLL_FACTOR + PROFILE_VIEW_CONFIG.HERO_SURFACE_TOP_OFFSET,
    PROFILE_VIEW_CONFIG.HERO_SURFACE_TOP_OFFSET
  )}px`
}))

const heroBackgroundStyle = computed(() => {
  if (!profileStyle.value.background) {
    return {
      backgroundImage: 'none',
      backgroundColor: PROFILE_VIEW_CONFIG.EMPTY_HERO_BG
    }
  }

  return {
    backgroundImage: `url(${profileStyle.value.background})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    backgroundRepeat: 'no-repeat'
  }
})

const wallpaperLayerStyle = computed(() => ({
  backgroundImage: profileStyle.value.wallpaper ? `url(${profileStyle.value.wallpaper})` : 'none',
  backgroundColor: profileStyle.value.wallpaper ? 'transparent' : PROFILE_VIEW_CONFIG.EMPTY_WALLPAPER_BG,
  backgroundSize: 'cover',
  backgroundPosition: 'center',
  backgroundRepeat: 'no-repeat',
  opacity: profileStyle.value.wallpaper ? 1 - wallpaperMaskControl.value : 1,
  transform: `scale(${wallpaperScale.value})`
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
  marginTop: `calc(var(--content-overlay) * -1 - 22px)`
}))

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

const formatDisplaySettingValue = (setting) => {
  if (setting.format === 'percent') {
    return `${Math.round(Number(setting.currentValue) * 100)}%`
  }
  if (setting.format === 'pixel') {
    return `${Number(setting.currentValue)}px`
  }
  return String(setting.currentValue)
}

const scheduleProfileStyleSave = (timerKey) => {
  if (timerKey === 'opacity' && cardOpacitySaveTimer) {
    clearTimeout(cardOpacitySaveTimer)
  }
  if (timerKey === 'blur' && cardBlurSaveTimer) {
    clearTimeout(cardBlurSaveTimer)
  }
  if (timerKey === 'mask' && wallpaperMaskSaveTimer) {
    clearTimeout(wallpaperMaskSaveTimer)
  }
  if (timerKey === 'font' && globalFontSaveTimer) {
    clearTimeout(globalFontSaveTimer)
  }

  const timer = setTimeout(() => {
    saveProfileStyle()
  }, PROFILE_VIEW_CONFIG.CARD_OPACITY_SAVE_DEBOUNCE)

  if (timerKey === 'opacity') {
    cardOpacitySaveTimer = timer
  }
  if (timerKey === 'blur') {
    cardBlurSaveTimer = timer
  }
  if (timerKey === 'mask') {
    wallpaperMaskSaveTimer = timer
  }
  if (timerKey === 'font') {
    globalFontSaveTimer = timer
  }
}

const buildProfileStylePayload = () => ({
  cardOpacity: Number(cardOpacityControl.value.toFixed(2)),
  cardBlur: Number(cardBlurControl.value),
  wallpaperMask: Number(wallpaperMaskControl.value.toFixed(2)),
  globalFontEnabled: globalFontEnabled.value ? 1 : 0
})

const flushProfileStyleOnPageHide = () => {
  if (!profileStyleDirty.value) return
  const token = localStorage.getItem(STORAGE_KEYS.TOKEN)
  if (!token) return

  const payload = JSON.stringify(buildProfileStylePayload())
  const requestUrl = `${HTTP_CONFIG.BASE_URL}${API_PATHS.PERSONALIZATION.UPDATE_PROFILE}`

  try {
    fetch(requestUrl, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        [HTTP_CONFIG.AUTH_HEADER]: `${HTTP_CONFIG.AUTH_PREFIX} ${token}`
      },
      body: payload,
      keepalive: true
    })
    profileStyleDirty.value = false
  } catch (error) {
    console.error('页面退出时保存个性化设置失败', error)
  }
}

const handlePageHide = () => {
  flushProfileStyleOnPageHide()
}

const clearSaveTimers = () => {
  if (cardOpacitySaveTimer) {
    clearTimeout(cardOpacitySaveTimer)
    cardOpacitySaveTimer = null
  }
  if (cardBlurSaveTimer) {
    clearTimeout(cardBlurSaveTimer)
    cardBlurSaveTimer = null
  }
  if (wallpaperMaskSaveTimer) {
    clearTimeout(wallpaperMaskSaveTimer)
    wallpaperMaskSaveTimer = null
  }
  if (globalFontSaveTimer) {
    clearTimeout(globalFontSaveTimer)
    globalFontSaveTimer = null
  }
}

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

const clampWallpaperMaskValue = (value) => {
  const normalized = Number(value)
  if (Number.isNaN(normalized)) {
    return PROFILE_VIEW_CONFIG.WALLPAPER_MASK_DEFAULT
  }
  return Math.min(
    PROFILE_VIEW_CONFIG.WALLPAPER_MASK_MAX,
    Math.max(PROFILE_VIEW_CONFIG.WALLPAPER_MASK_MIN, normalized)
  )
}

const clampCardBlurValue = (value) => {
  const normalized = Number(value)
  if (Number.isNaN(normalized)) {
    return PROFILE_VIEW_CONFIG.CARD_BLUR_DEFAULT
  }
  return Math.min(
    PROFILE_VIEW_CONFIG.CARD_BLUR_MAX,
    Math.max(PROFILE_VIEW_CONFIG.CARD_BLUR_MIN, normalized)
  )
}

const clampCardOpacityValue = (value) => {
  const normalized = Number(value)
  if (Number.isNaN(normalized)) {
    return PROFILE_VIEW_CONFIG.CARD_BG_OPACITY_DEFAULT
  }
  return Math.min(
    PROFILE_VIEW_CONFIG.CARD_OPACITY_MAX,
    Math.max(PROFILE_VIEW_CONFIG.CARD_BG_OPACITY_MIN, normalized)
  )
}

const loadCardOpacityPreference = () => {
  const savedValue = localStorage.getItem(STORAGE_KEYS.PROFILE_CARD_OPACITY)
  if (savedValue === null) {
    return PROFILE_VIEW_CONFIG.CARD_BG_OPACITY_DEFAULT
  }
  return clampCardOpacityValue(savedValue)
}

const saveCardOpacityPreference = (value) => {
  localStorage.setItem(STORAGE_KEYS.PROFILE_CARD_OPACITY, clampCardOpacityValue(value).toFixed(2))
}

const loadCardBlurPreference = () => {
  const savedValue = localStorage.getItem(STORAGE_KEYS.PROFILE_CARD_BLUR)
  if (savedValue === null) {
    return PROFILE_VIEW_CONFIG.CARD_BLUR_DEFAULT
  }
  return clampCardBlurValue(savedValue)
}

const saveCardBlurPreference = (value) => {
  localStorage.setItem(STORAGE_KEYS.PROFILE_CARD_BLUR, clampCardBlurValue(value).toString())
}

const loadWallpaperMaskPreference = () => {
  const savedValue = localStorage.getItem(STORAGE_KEYS.PROFILE_WALLPAPER_MASK)
  if (savedValue === null) {
    return PROFILE_VIEW_CONFIG.WALLPAPER_MASK_DEFAULT
  }
  return clampWallpaperMaskValue(savedValue)
}

const saveWallpaperMaskPreference = (value = wallpaperMaskControl.value) => {
  localStorage.setItem(
    STORAGE_KEYS.PROFILE_WALLPAPER_MASK,
    clampWallpaperMaskValue(value).toFixed(2)
  )
}

const saveWallpaperPreference = (value) => {
  if (!value) return
  localStorage.setItem(STORAGE_KEYS.PROFILE_WALLPAPER, value)
}

const toRelativeAssetPath = (url) => {
  if (!url) return ''

  try {
    const parsed = new URL(url)
    const isSameHost = parsed.hostname === window.location.hostname
    const isBackendPort = parsed.port === '8000' || (parsed.port === '' && window.location.port === '8000')
    return isSameHost && isBackendPort ? `${parsed.pathname}${parsed.search}` : url
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

const fetchProfileStyle = async () => {
  try {
    cardOpacityControl.value = loadCardOpacityPreference()
    cardBlurControl.value = loadCardBlurPreference()
    wallpaperMaskControl.value = loadWallpaperMaskPreference()
    const res = await getProfileStyle()
    if (res.code === HTTP_STATUS.SUCCESS && res.data) {
      profileStyle.value = {
        avatar: resolveAssetUrl(res.data.avatar),
        background: resolveAssetUrl(res.data.background),
        wallpaper: resolveAssetUrl(res.data.wallpaper)
      }
      saveWallpaperPreference(profileStyle.value.wallpaper)

      if (res.data.cardOpacity !== null && res.data.cardOpacity !== undefined) {
        cardOpacityControl.value = clampCardOpacityValue(res.data.cardOpacity)
        saveCardOpacityPreference(cardOpacityControl.value)
      }
      if (res.data.cardBlur !== null && res.data.cardBlur !== undefined) {
        cardBlurControl.value = clampCardBlurValue(res.data.cardBlur)
        saveCardBlurPreference(cardBlurControl.value)
      }
      if (res.data.wallpaperMask !== null && res.data.wallpaperMask !== undefined) {
        wallpaperMaskControl.value = clampWallpaperMaskValue(res.data.wallpaperMask)
        saveWallpaperMaskPreference(wallpaperMaskControl.value)
      }
      if (res.data.globalFontEnabled !== null && res.data.globalFontEnabled !== undefined) {
        globalFontEnabled.value = Number(res.data.globalFontEnabled) === 1
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

const fetchProfileCustomAssets = async () => {
  try {
    const res = await getProfileCustomAssets()
    if (res.code === HTTP_STATUS.SUCCESS && res.data) {
      const assetData = Array.isArray(res.data) ? (res.data[0] || {}) : res.data
      customAssets.value = {
        customAvatar: resolveAssetUrl(assetData.customAvatar || assetData.avatar),
        customBackground: resolveAssetUrl(assetData.customBackground || assetData.background),
        customWallpaper: resolveAssetUrl(assetData.customWallpaper || assetData.wallpaper)
      }
    }
  } catch (error) {
    console.error('获取自定义图库失败', error)
  }
}

const saveProfileStyle = async () => {
  try {
    await updateProfileStyle({
      cardOpacity: Number(cardOpacityControl.value.toFixed(2)),
      cardBlur: Number(cardBlurControl.value),
      wallpaperMask: Number(wallpaperMaskControl.value.toFixed(2)),
      globalFontEnabled: globalFontEnabled.value ? 1 : 0
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

const resetProfileToMinimalDefault = async () => {
  if (!confirm(PROFILE_VIEW_CONFIG.RESET_PROFILE_CONFIRM)) return

  const previousProfileStyle = { ...profileStyle.value }
  const previousCardOpacity = cardOpacityControl.value
  const previousCardBlur = cardBlurControl.value
  const previousWallpaperMask = wallpaperMaskControl.value
  const previousGlobalFontEnabled = globalFontEnabled.value

  profileStyle.value = {
    avatar: '',
    background: '',
    wallpaper: ''
  }
  cardOpacityControl.value = PROFILE_VIEW_CONFIG.CARD_BG_OPACITY_DEFAULT
  cardBlurControl.value = PROFILE_VIEW_CONFIG.CARD_BLUR_DEFAULT
  wallpaperMaskControl.value = PROFILE_VIEW_CONFIG.WALLPAPER_MASK_DEFAULT
  globalFontEnabled.value = true
  saveWallpaperPreference('')
  saveCardOpacityPreference(cardOpacityControl.value)
  saveCardBlurPreference(cardBlurControl.value)
  saveWallpaperMaskPreference(wallpaperMaskControl.value)

  try {
    await persistProfileStyle({
      avatar: '',
      background: '',
      wallpaper: '',
      cardOpacity: Number(cardOpacityControl.value.toFixed(2)),
      cardBlur: Number(cardBlurControl.value),
      wallpaperMask: Number(wallpaperMaskControl.value.toFixed(2)),
      globalFontEnabled: 1
    })
  } catch (error) {
    profileStyle.value = previousProfileStyle
    cardOpacityControl.value = previousCardOpacity
    cardBlurControl.value = previousCardBlur
    wallpaperMaskControl.value = previousWallpaperMask
    globalFontEnabled.value = previousGlobalFontEnabled
    saveWallpaperPreference(previousProfileStyle.wallpaper)
    saveCardOpacityPreference(cardOpacityControl.value)
    saveCardBlurPreference(cardBlurControl.value)
    saveWallpaperMaskPreference(wallpaperMaskControl.value)
    alert('重置失败，请稍后重试')
  }
}

const persistProfileSelection = async (field, url) => {
  const previousValue = profileStyle.value[field]
  profileStyle.value[field] = url
  if (field === 'wallpaper') {
    saveWallpaperPreference(url)
  }

  try {
    await persistProfileStyle({ [field]: toRelativeAssetPath(url) })
  } catch (error) {
    profileStyle.value[field] = previousValue
    if (field === 'wallpaper' && previousValue) {
      saveWallpaperPreference(previousValue)
    }
    alert('保存个性化设置失败，请稍后重试')
  }
}

const setFileInputRef = (key, element) => {
  if (element) {
    fileInputRefs[key] = element
  }
}

const openUploadPicker = (type) => {
  fileInputRefs[type]?.click()
}

const getCropPreset = (type) => PROFILE_VIEW_CONFIG.CROP_PRESETS[type] || PROFILE_VIEW_CONFIG.CROP_PRESETS.avatar

const closeCropModal = () => {
  if (cropSourceUrl.value) {
    URL.revokeObjectURL(cropSourceUrl.value)
  }
  cropSourceUrl.value = ''
  cropSourceFileName.value = 'upload-image.jpg'
  cropModalVisible.value = false
  cropUploading.value = false
}

const openCropModal = (type, file) => {
  cropTargetType.value = type
  cropSourceFileName.value = file?.name || 'upload-image.jpg'
  cropSourceUrl.value = URL.createObjectURL(file)
  cropModalVisible.value = true
}

const handleLocalFileChange = (type, event) => {
  const [file] = event.target.files || []
  event.target.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }
  openCropModal(type, file)
}

const handleCropConfirm = async (file) => {
  cropUploading.value = true
  try {
    const res = await uploadProfileCustomAsset(cropTargetType.value, file)
    if (res.code === HTTP_STATUS.SUCCESS && res.data?.url) {
      const url = resolveAssetUrl(res.data.url)
      const customField = `custom${cropTargetType.value.charAt(0).toUpperCase()}${cropTargetType.value.slice(1)}`
      customAssets.value = {
        ...customAssets.value,
        [customField]: url
      }
      await persistProfileSelection(cropTargetType.value, url)
      closeCropModal()
    }
  } catch (error) {
    console.error('上传自定义图片失败', error)
    alert('上传失败，请稍后重试')
    cropUploading.value = false
  }
}

const handleGalleryOptionClick = (groupKey, option) => {
  if (option.type === 'upload') {
    if (option.previewUrl && profileStyle.value[groupKey] !== option.previewUrl) {
      persistProfileSelection(groupKey, option.previewUrl)
      return
    }
    openUploadPicker(groupKey)
    return
  }
  persistProfileSelection(groupKey, option.url)
}

const isGalleryOptionActive = (groupKey, option) => {
  if (option.type === 'upload') {
    return Boolean(option.previewUrl) && profileStyle.value[groupKey] === option.previewUrl
  }
  return profileStyle.value[groupKey] === option.url
}

const updateDisplaySetting = (key, value) => {
  if (key === 'cardOpacity') {
    cardOpacityControl.value = clampCardOpacityValue(value)
    saveCardOpacityPreference(cardOpacityControl.value)
    return
  }

  if (key === 'cardBlur') {
    cardBlurControl.value = clampCardBlurValue(value)
    saveCardBlurPreference(cardBlurControl.value)
    return
  }

  if (key === 'wallpaperMask') {
    wallpaperMaskControl.value = clampWallpaperMaskValue(value)
    saveWallpaperMaskPreference(wallpaperMaskControl.value)
    return
  }

  globalFontEnabled.value = Boolean(value)
  scheduleProfileStyleSave('font')
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

const toggleGalleryExpanded = () => {
  closeTooltip()
  galleryExpanded.value = !galleryExpanded.value
}

const toggleTooltip = (key) => {
  activeTooltipKey.value = activeTooltipKey.value === key ? '' : key
}

const closeTooltip = () => {
  activeTooltipKey.value = ''
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

watch(cardOpacityControl, (value, oldValue) => {
  if (!profileStyleLoaded.value || value === oldValue) return
  scheduleProfileStyleSave('opacity')
})

watch(cardBlurControl, (value, oldValue) => {
  if (!profileStyleLoaded.value || value === oldValue) return
  scheduleProfileStyleSave('blur')
})

watch(wallpaperMaskControl, (value, oldValue) => {
  if (!profileStyleLoaded.value || value === oldValue) return
  scheduleProfileStyleSave('mask')
})

onMounted(() => {
  tokenInfo.value = getUserInfoFromToken()
  const userId = tokenInfo.value?.userid
  cardOpacityControl.value = loadCardOpacityPreference()
  cardBlurControl.value = loadCardBlurPreference()
  wallpaperMaskControl.value = loadWallpaperMaskPreference()
  document.addEventListener('click', closeTooltip)

  bindScrollContainer()
  fetchProfileDefaultOptions()
  fetchProfileCustomAssets()

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
  clearSaveTimers()
  document.removeEventListener('click', closeTooltip)
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
}

.hero-section {
  position: sticky;
  top: 0;
  z-index: 0;
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

.info-card {
  overflow: visible;
  padding-top: 10px;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 14px 18px;
  gap: 14px;
  border-bottom: 1px solid #f0f4fb;
}

.info-row:last-child {
  border-bottom: none;
}

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
  letter-spacing: 0.3px;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: #1a2540;
}

.status-ok {
  color: #10b981;
}

.status-syncing {
  color: #4f86f7;
}

.status-err {
  color: #ef4444;
}

.setting-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.gallery-card {
  padding: 16px 18px 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow: visible;
}

.gallery-settings {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 4px 2px 6px;
  margin-bottom: 4px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
}

.gallery-settings-actions {
  display: flex;
  justify-content: flex-end;
}

.reset-profile-button {
  border: 1px solid rgba(148, 163, 184, 0.28);
  background: rgba(255, 255, 255, 0.88);
  color: #475569;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.15s ease;
}

.reset-profile-button:hover {
  color: #0f172a;
  border-color: rgba(79, 134, 247, 0.28);
  background: #ffffff;
}

.gallery-settings .setting-block {
  gap: 12px;
}

.gallery-toggle-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.gallery-title-row,
.setting-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.gallery-toggle-title,
.gallery-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.help-icon-wrap {
  position: relative;
  display: inline-flex;
  align-items: center;
  z-index: 12;
  flex-shrink: 0;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
}

.help-icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(148, 163, 184, 0.28);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(241, 245, 249, 0.96) 100%);
  color: #5b6b86;
  line-height: 1;
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.08);
  transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease, color 0.15s ease, background 0.15s ease;
}

.help-icon-wrap:hover .help-icon,
.help-icon-wrap.active .help-icon {
  transform: scale(1.08);
  border-color: rgba(79, 134, 247, 0.45);
  color: #4f86f7;
  background: linear-gradient(180deg, #ffffff 0%, #edf4ff 100%);
  box-shadow: 0 8px 18px rgba(79, 134, 247, 0.16);
}

.help-tooltip {
  position: absolute;
  left: 0;
  top: calc(100% + 12px);
  transform: translateY(-4px);
  width: min(280px, calc(100vw - 48px));
  min-width: 220px;
  padding: 8px 10px;
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.9);
  color: #fff;
  font-size: 12px;
  line-height: 1.5;
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.2);
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
  transition: opacity 0.15s ease, transform 0.15s ease, visibility 0.15s ease;
  z-index: 40;
  text-align: left;
}

.help-tooltip::before {
  content: '';
  position: absolute;
  left: 10px;
  top: -6px;
  width: 10px;
  height: 10px;
  transform: rotate(45deg);
  background: rgba(15, 23, 42, 0.9);
}

.help-icon-wrap:hover .help-tooltip,
.help-icon-wrap.active .help-tooltip {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

@media (max-width: 520px) {
  .help-tooltip {
    width: min(260px, calc(100vw - 40px));
    min-width: 0;
  }
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

.gallery-head .gallery-title {
  flex-shrink: 0;
}

.gallery-tip {
  font-size: 12px;
  color: #94a3b8;
}

.gallery-grid {
  display: grid;
  gap: 10px;
  align-items: start;
}

.gallery-option-shell {
  width: 100%;
}

.gallery-option-shell--with-caption {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.avatar-gallery {
  grid-template-columns: repeat(auto-fit, minmax(64px, 82px));
  justify-content: start;
}

.cover-gallery {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  justify-items: stretch;
}

.gallery-option {
  width: 100%;
  min-width: 0;
  border: 2px solid transparent;
  background: rgba(255, 255, 255, 0.78);
  border-radius: 16px;
  padding: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
  position: relative;
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

.gallery-file-input {
  display: none;
}

.gallery-option--upload {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(239, 246, 255, 0.92) 100%);
  border: 1.5px dashed rgba(79, 134, 247, 0.35);
}

.gallery-option--upload.gallery-option--custom {
  overflow: hidden;
  border-style: solid;
  border-color: rgba(79, 134, 247, 0.24);
}

.gallery-option--upload.gallery-option--custom::after {
  content: attr(data-upload-label);
  position: absolute;
  left: 50%;
  bottom: 10px;
  transform: translateX(-50%);
  font-size: 11px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.2px;
  z-index: 2;
  pointer-events: none;
  text-shadow: 0 1px 6px rgba(15, 23, 42, 0.35);
}

.upload-option-body {
  width: 100%;
  height: 100%;
  min-height: 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  border-radius: 12px;
  color: #4f86f7;
}

.gallery-option--upload.gallery-option--custom .upload-option-body {
  background: transparent;
  color: #ffffff;
  backdrop-filter: blur(1.5px);
  -webkit-backdrop-filter: blur(1.5px);
}

.gallery-option--upload.gallery-option--custom .upload-option-title,
.gallery-option--upload.gallery-option--custom .upload-option-subtitle {
  display: none;
}

.upload-option-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(79, 134, 247, 0.12);
  font-size: 18px;
  font-weight: 500;
}

.gallery-option--upload.gallery-option--custom .upload-option-icon {
  position: relative;
  background: rgba(255, 255, 255, 0.22);
  color: transparent;
}

.gallery-option--upload.gallery-option--custom .upload-option-icon::before {
  content: '↻';
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 15px;
  font-weight: 700;
}

.upload-option-title {
  font-size: 11px;
  font-weight: 700;
}

.upload-option-subtitle {
  font-size: 10px;
  color: #7c92b8;
}

.gallery-option--custom {
  border-color: rgba(16, 185, 129, 0.28);
}

.custom-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 3px 8px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.82);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.avatar-option {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  padding: 3px;
}

.avatar-option.gallery-option--upload {
  padding: 2px;
}

.avatar-option .upload-option-title,
.avatar-option .upload-option-subtitle {
  display: none;
}

.avatar-option.gallery-option--upload .upload-option-body {
  border-radius: 50%;
  min-height: 64px;
  width: 64px;
  height: 64px;
  margin: 0 auto;
  padding: 2px;
}

.avatar-option.gallery-option--upload .upload-option-icon {
  width: 28px;
  height: 28px;
  font-size: 20px;
}

.avatar-option .custom-badge {
  top: -2px;
  right: -6px;
}

.gallery-option-caption {
  display: block;
  width: 100%;
  padding: 0 1px;
  font-size: 11px;
  line-height: 1.35;
  color: #64748b;
  text-align: center;
  white-space: normal;
  word-break: break-word;
}

.avatar-option img {
  border-radius: 50%;
}

.cover-option {
  width: 100%;
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
  background-color: #cbd5e1;
  transition: 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 24px;
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
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

input:checked + .toggle-slider {
  background-color: var(--hero-theme-start);
}

input:checked + .toggle-slider::before {
  transform: translateX(20px);
}

.font-setting-block {
  padding-top: 4px;
}

.action-card {
  margin-top: 0;
}

.action-row {
  width: 100%;
  display: flex;
  align-items: center;
  padding: 14px 18px;
  gap: 14px;
  text-align: left;
  transition: background 0.15s;
}

.auto-punch-row {
  justify-content: space-between;
}

.action-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.action-subtitle {
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
}

.action-row:active {
  background: #f8faff;
}

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

.chevron {
  flex-shrink: 0;
  color: #b0bdd4;
}

.divider {
  height: 1px;
  background: #f0f4fb;
  margin: 0 18px;
}

.footer-hint {
  text-align: center;
  font-size: 11px;
  color: #b0bdd4;
  margin-top: 24px;
  letter-spacing: 0.3px;
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








