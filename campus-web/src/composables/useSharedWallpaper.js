import { computed, ref } from 'vue'
import { getProfileStyle } from '@/api'
import { HTTP_STATUS, PROFILE_VIEW_CONFIG, STORAGE_KEYS } from '@/config'
import {
  clampWallpaperMaskValue,
  loadWallpaperMaskPreference,
  loadWallpaperPreference,
  resolveAssetUrl,
  saveWallpaperMaskPreference,
  saveWallpaperPreference
} from '@/utils/profileAssets'

export const useSharedWallpaper = () => {
  const wallpaper = ref('')
  const wallpaperMask = ref(PROFILE_VIEW_CONFIG.WALLPAPER_MASK_DEFAULT)

  const wallpaperRootStyle = computed(() => ({
    '--page-wallpaper-mask-alpha': wallpaperMask.value
  }))

  const wallpaperBackdropStyle = computed(() => ({
    backgroundImage: wallpaper.value ? `url(${wallpaper.value})` : 'none',
    backgroundColor: wallpaper.value ? 'transparent' : PROFILE_VIEW_CONFIG.EMPTY_WALLPAPER_BG,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    backgroundRepeat: 'no-repeat',
    opacity: wallpaper.value ? 1 - wallpaperMask.value : 1
  }))

  const fetchWallpaper = async () => {
    wallpaper.value = loadWallpaperPreference(STORAGE_KEYS)
    wallpaperMask.value = loadWallpaperMaskPreference(STORAGE_KEYS, PROFILE_VIEW_CONFIG)

    try {
      const res = await getProfileStyle()
      if (res.code === HTTP_STATUS.SUCCESS && res.data) {
        wallpaper.value = resolveAssetUrl(res.data.wallpaper)
        saveWallpaperPreference(STORAGE_KEYS, wallpaper.value)

        if (res.data.wallpaperMask !== null && res.data.wallpaperMask !== undefined) {
          wallpaperMask.value = clampWallpaperMaskValue(res.data.wallpaperMask, PROFILE_VIEW_CONFIG)
          saveWallpaperMaskPreference(STORAGE_KEYS, wallpaperMask.value, PROFILE_VIEW_CONFIG)
        }
      }
    } catch (error) {
      console.error('获取共享墙纸失败:', error)
    }
  }

  return {
    wallpaper,
    wallpaperMask,
    wallpaperRootStyle,
    wallpaperBackdropStyle,
    fetchWallpaper
  }
}
