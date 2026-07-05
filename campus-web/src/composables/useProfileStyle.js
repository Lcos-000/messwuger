import { computed, ref, watch } from 'vue'
import { getProfileStyle, updateProfileStyle } from '@/api/index'
import {
  HTTP_STATUS,
  PROFILE_THEME_CONFIG,
  PROFILE_VIEW_CONFIG,
  STORAGE_KEYS
} from '@/config'
import {
  GLOBAL_FONT_FAMILY,
  loadAndApplyGlobalFontPreference,
  SYSTEM_FONT_FAMILY,
  setGlobalFontPreference
} from '@/utils/globalFont'
import {
  clampWallpaperMaskValue as clampSharedWallpaperMaskValue,
  loadWallpaperMaskPreference as loadSharedWallpaperMaskPreference,
  resolveAssetUrl,
  saveWallpaperMaskPreference as saveSharedWallpaperMaskPreference,
  saveWallpaperPreference as saveSharedWallpaperPreference
} from '@/utils/profileAssets'

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
  return loadSharedWallpaperMaskPreference(STORAGE_KEYS, PROFILE_VIEW_CONFIG)
}

const saveWallpaperMaskPreference = (value) => {
  saveSharedWallpaperMaskPreference(STORAGE_KEYS, value, PROFILE_VIEW_CONFIG)
}

const saveWallpaperPreference = (value) => {
  saveSharedWallpaperPreference(STORAGE_KEYS, value)
}

const buildDisplaySettingValue = (setting, controls) => {
  if (setting.key === 'cardOpacity') {
    return controls.cardOpacityControl.value
  }
  if (setting.key === 'cardBlur') {
    return controls.cardBlurControl.value
  }
  if (setting.key === 'wallpaperMask') {
    return controls.wallpaperMaskControl.value
  }
  return controls.globalFontEnabled.value
}

const createStyleSaveScheduler = ({ saveProfileStyle, timers }) => {
  return (timerKey) => {
    if (timers[timerKey]) {
      clearTimeout(timers[timerKey])
    }

    timers[timerKey] = setTimeout(() => {
      saveProfileStyle()
    }, PROFILE_VIEW_CONFIG.CARD_OPACITY_SAVE_DEBOUNCE)
  }
}

const clearStyleSaveTimers = (timers) => {
  Object.keys(timers).forEach((key) => {
    if (timers[key]) {
      clearTimeout(timers[key])
      timers[key] = null
    }
  })
}

export const useProfileStyle = ({ profileStyle, scrollTop }) => {
  const profileStyleLoaded = ref(false)
  const cardOpacityControl = ref(PROFILE_VIEW_CONFIG.CARD_BG_OPACITY_DEFAULT)
  const cardBlurControl = ref(PROFILE_VIEW_CONFIG.CARD_BLUR_DEFAULT)
  const wallpaperMaskControl = ref(PROFILE_VIEW_CONFIG.WALLPAPER_MASK_DEFAULT)
  const globalFontEnabled = ref(loadAndApplyGlobalFontPreference())

  const styleSaveTimers = {
    opacity: null,
    blur: null,
    mask: null,
    font: null
  }

  const displaySettings = computed(() => {
    return PROFILE_VIEW_CONFIG.DISPLAY_SETTINGS.map((setting) => ({
      ...setting,
      currentValue: buildDisplaySettingValue(setting, {
        cardOpacityControl,
        cardBlurControl,
        wallpaperMaskControl,
        globalFontEnabled
      })
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

    style.fontFamily = globalFontEnabled.value ? GLOBAL_FONT_FAMILY : SYSTEM_FONT_FAMILY

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

  const formatDisplaySettingValue = (setting) => {
    if (setting.format === 'percent') {
      return `${Math.round(Number(setting.currentValue) * 100)}%`
    }
    if (setting.format === 'pixel') {
      return `${Number(setting.currentValue)}px`
    }
    return String(setting.currentValue)
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

  const scheduleProfileStyleSave = createStyleSaveScheduler({
    saveProfileStyle,
    timers: styleSaveTimers
  })

  const clearProfileStyleTimers = () => {
    clearStyleSaveTimers(styleSaveTimers)
  }

  const fetchProfileStyle = async () => {
    try {
      cardOpacityControl.value = loadCardOpacityPreference()
      cardBlurControl.value = loadCardBlurPreference()
      wallpaperMaskControl.value = loadWallpaperMaskPreference()
      const res = await getProfileStyle()
      if (res.code === HTTP_STATUS.SUCCESS && res.data) {
        profileStyle.value = {
          avatar: res.data.avatar ? resolveAssetUrl(res.data.avatar) : '',
          background: res.data.background ? resolveAssetUrl(res.data.background) : '',
          wallpaper: res.data.wallpaper ? resolveAssetUrl(res.data.wallpaper) : ''
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
          wallpaperMaskControl.value = clampSharedWallpaperMaskValue(res.data.wallpaperMask, PROFILE_VIEW_CONFIG)
          saveWallpaperMaskPreference(wallpaperMaskControl.value)
        }
        if (res.data.globalFontEnabled !== null && res.data.globalFontEnabled !== undefined) {
          globalFontEnabled.value = setGlobalFontPreference(Number(res.data.globalFontEnabled) === 1)
        }
      }
    } catch (error) {
      console.error('获取个性化样式失败', error)
    } finally {
      profileStyleLoaded.value = true
    }
  }

  const persistProfileStylePatch = async (payload) => {
    try {
      await updateProfileStyle(payload)
    } catch (error) {
      console.error('更新个性化样式失败', error)
      throw error
    }
  }

  const resetProfileToMinimalDefault = async () => {
    if (!window.confirm(PROFILE_VIEW_CONFIG.RESET_PROFILE_CONFIRM)) return

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
    globalFontEnabled.value = setGlobalFontPreference(true)
    saveWallpaperPreference('')
    saveCardOpacityPreference(cardOpacityControl.value)
    saveCardBlurPreference(cardBlurControl.value)
    saveWallpaperMaskPreference(wallpaperMaskControl.value)

    try {
      await persistProfileStylePatch({
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
      globalFontEnabled.value = setGlobalFontPreference(previousGlobalFontEnabled)
      saveWallpaperPreference(previousProfileStyle.wallpaper)
      saveCardOpacityPreference(cardOpacityControl.value)
      saveCardBlurPreference(cardBlurControl.value)
      saveWallpaperMaskPreference(wallpaperMaskControl.value)
      window.alert('重置失败，请稍后重试')
    }
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
      wallpaperMaskControl.value = clampSharedWallpaperMaskValue(value, PROFILE_VIEW_CONFIG)
      saveWallpaperMaskPreference(wallpaperMaskControl.value)
      return
    }

    globalFontEnabled.value = setGlobalFontPreference(value)
    saveProfileStyle()
  }

  const markProfileStyleReady = () => {
    profileStyleLoaded.value = true
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

  return {
    profileStyleLoaded,
    cardOpacityControl,
    cardBlurControl,
    wallpaperMaskControl,
    globalFontEnabled,
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
  }
}
