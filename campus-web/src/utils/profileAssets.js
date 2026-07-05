export const resolveAssetUrl = (path, backendPort = '8000') => {
  if (!path) return ''
  if (/^https?:\/\//.test(path)) return path

  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return `${window.location.protocol}//${window.location.hostname}:${backendPort}${normalizedPath}`
}

export const clampWallpaperMaskValue = (value, profileViewConfig) => {
  const normalized = Number(value)
  if (Number.isNaN(normalized)) {
    return profileViewConfig.WALLPAPER_MASK_DEFAULT
  }
  return Math.min(
    profileViewConfig.WALLPAPER_MASK_MAX,
    Math.max(profileViewConfig.WALLPAPER_MASK_MIN, normalized)
  )
}

export const loadWallpaperPreference = (storageKeys) => {
  return localStorage.getItem(storageKeys.PROFILE_WALLPAPER) || ''
}

export const loadWallpaperMaskPreference = (storageKeys, profileViewConfig) => {
  const savedValue = localStorage.getItem(storageKeys.PROFILE_WALLPAPER_MASK)
  if (savedValue === null) {
    return profileViewConfig.WALLPAPER_MASK_DEFAULT
  }
  return clampWallpaperMaskValue(savedValue, profileViewConfig)
}

export const saveWallpaperPreference = (storageKeys, value) => {
  if (!value) {
    localStorage.removeItem(storageKeys.PROFILE_WALLPAPER)
    return
  }
  localStorage.setItem(storageKeys.PROFILE_WALLPAPER, value)
}

export const saveWallpaperMaskPreference = (storageKeys, value, profileViewConfig) => {
  localStorage.setItem(
    storageKeys.PROFILE_WALLPAPER_MASK,
    clampWallpaperMaskValue(value, profileViewConfig).toFixed(2)
  )
}
