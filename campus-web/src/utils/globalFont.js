import { getProfileStyle } from '@/api/index'
import { HTTP_STATUS, PROFILE_VIEW_CONFIG, STORAGE_KEYS } from '@/config'

export const SYSTEM_FONT_FAMILY = "-apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif"
export const GLOBAL_FONT_FAMILY = `'${PROFILE_VIEW_CONFIG.FONT_FACE.family}', ${SYSTEM_FONT_FAMILY}`

const GLOBAL_FONT_ENABLED_CLASS = 'app-global-font-enabled'
const GLOBAL_FONT_DISABLED_CLASS = 'app-global-font-disabled'

export const normalizeGlobalFontEnabled = (value) => {
  if (value === true || value === 1 || value === '1') return true
  if (value === false || value === 0 || value === '0') return false
  return Boolean(value)
}

export const loadGlobalFontPreference = () => {
  const savedValue = localStorage.getItem(STORAGE_KEYS.PROFILE_GLOBAL_FONT_ENABLED)
  if (savedValue === null) return true
  return savedValue === '1'
}

export const saveGlobalFontPreference = (enabled) => {
  localStorage.setItem(STORAGE_KEYS.PROFILE_GLOBAL_FONT_ENABLED, enabled ? '1' : '0')
}

export const applyGlobalFontPreference = (enabled) => {
  const normalized = normalizeGlobalFontEnabled(enabled)
  if (typeof document === 'undefined') return normalized

  const root = document.documentElement
  root.style.setProperty('--app-font-family', normalized ? GLOBAL_FONT_FAMILY : SYSTEM_FONT_FAMILY)
  root.classList.toggle(GLOBAL_FONT_ENABLED_CLASS, normalized)
  root.classList.toggle(GLOBAL_FONT_DISABLED_CLASS, !normalized)
  return normalized
}

export const setGlobalFontPreference = (enabled, { persist = true } = {}) => {
  const normalized = applyGlobalFontPreference(enabled)
  if (persist) {
    saveGlobalFontPreference(normalized)
  }
  return normalized
}

export const loadAndApplyGlobalFontPreference = () => {
  return setGlobalFontPreference(loadGlobalFontPreference(), { persist: false })
}

export const fetchAndApplyGlobalFontPreference = async () => {
  const res = await getProfileStyle()
  if (res.code !== HTTP_STATUS.SUCCESS || !res.data) {
    return loadAndApplyGlobalFontPreference()
  }

  if (res.data.globalFontEnabled === null || res.data.globalFontEnabled === undefined) {
    return loadAndApplyGlobalFontPreference()
  }

  return setGlobalFontPreference(Number(res.data.globalFontEnabled) === 1)
}
