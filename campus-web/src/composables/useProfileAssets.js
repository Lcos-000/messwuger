import { computed, ref } from 'vue'
import {
  getProfileCustomAssets,
  getProfileDefaultOptions,
  uploadProfileCustomAsset
} from '@/api/index'
import { HTTP_STATUS, PROFILE_VIEW_CONFIG, STORAGE_KEYS } from '@/config'
import {
  resolveAssetUrl,
  saveWallpaperPreference as saveSharedWallpaperPreference
} from '@/utils/profileAssets'
import { useProfileGalleryGroups } from '@/composables/useProfileGalleryGroups'

const DEFAULT_OPTION_FALLBACKS = {
  avatars: [
    '/avatar/avatar_default_1.jpg',
    '/avatar/avatar_default_2.jpg'
  ],
  backgrounds: [
    '/background/background_default_1.jpg',
    '/background/background_default_2.jpg'
  ],
  wallpapers: [
    '/wallpaper/wallpaper_default_1.jpg',
    '/wallpaper/wallpaper_default_2.jpg'
  ]
}

const normalizeAssetValue = (value) => {
  if (!value) return ''
  const normalized = String(value).trim()
  return normalized ? resolveAssetUrl(normalized) : ''
}

const normalizeOptionList = (values = [], fallbackValues = []) => {
  const source = Array.isArray(values) && values.length ? values : fallbackValues
  return source
    .map(normalizeAssetValue)
    .filter(Boolean)
}

const normalizeComparableAsset = (value) => {
  if (!value) return ''

  try {
    const parsed = new URL(value, window.location.origin)
    if (/^\/(avatar|background|wallpaper)\//.test(parsed.pathname)) {
      return parsed.pathname
    }
    return parsed.toString()
  } catch {
    return String(value).trim()
  }
}

export const useProfileAssets = ({ profileStyle, persistProfileStylePatch }) => {
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

  const { galleryGroups } = useProfileGalleryGroups({
    customAssets,
    profileDefaultOptions
  })

  const ensureDefaultProfileAssets = () => {
    PROFILE_VIEW_CONFIG.OPTION_GROUPS.forEach((group) => {
      const currentValue = profileStyle.value[group.key]
      if (currentValue) return

      const defaultOptions = profileDefaultOptions.value[group.optionField] || []
      if (defaultOptions.length > 0) {
        profileStyle.value[group.key] = defaultOptions[0]
      }
    })
  }

  const fetchProfileDefaultOptions = async () => {
    try {
      const res = await getProfileDefaultOptions()
      if (res.code === HTTP_STATUS.SUCCESS) {
        const data = res.data || {}
        profileDefaultOptions.value = {
          avatars: normalizeOptionList(data.avatars, DEFAULT_OPTION_FALLBACKS.avatars),
          backgrounds: normalizeOptionList(data.backgrounds, DEFAULT_OPTION_FALLBACKS.backgrounds),
          wallpapers: normalizeOptionList(data.wallpapers, DEFAULT_OPTION_FALLBACKS.wallpapers)
        }
        ensureDefaultProfileAssets()
      }
    } catch (error) {
      console.error('获取默认图库失败', error)
      profileDefaultOptions.value = {
        avatars: normalizeOptionList([], DEFAULT_OPTION_FALLBACKS.avatars),
        backgrounds: normalizeOptionList([], DEFAULT_OPTION_FALLBACKS.backgrounds),
        wallpapers: normalizeOptionList([], DEFAULT_OPTION_FALLBACKS.wallpapers)
      }
      ensureDefaultProfileAssets()
    }
  }

  const fetchProfileCustomAssets = async () => {
    try {
      const res = await getProfileCustomAssets()
      if (res.code === HTTP_STATUS.SUCCESS && res.data) {
        const assetData = Array.isArray(res.data) ? (res.data[0] || {}) : res.data
        customAssets.value = {
          customAvatar: normalizeAssetValue(assetData.customAvatar || assetData.avatar),
          customBackground: normalizeAssetValue(assetData.customBackground || assetData.background),
          customWallpaper: normalizeAssetValue(assetData.customWallpaper || assetData.wallpaper)
        }
      }
    } catch (error) {
      console.error('获取自定义图库失败', error)
    }
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

  const persistProfileSelection = async (field, url) => {
    const previousValue = profileStyle.value[field]
    profileStyle.value[field] = url

    if (field === 'wallpaper') {
      saveSharedWallpaperPreference(STORAGE_KEYS, url)
    }

    try {
      await persistProfileStylePatch({ [field]: toRelativeAssetPath(url) })
    } catch (error) {
      profileStyle.value[field] = previousValue
      if (field === 'wallpaper') {
        saveSharedWallpaperPreference(STORAGE_KEYS, previousValue)
      }
      window.alert('保存个性化设置失败，请稍后重试')
      throw error
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
      window.alert('请选择图片文件')
      return
    }
    openCropModal(type, file)
  }

  const handleCropConfirm = async (file) => {
    cropUploading.value = true
    try {
      const res = await uploadProfileCustomAsset(cropTargetType.value, file)
      if (res.code === HTTP_STATUS.SUCCESS && res.data?.url) {
        const url = normalizeAssetValue(res.data.url)
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
      window.alert('上传失败，请稍后重试')
      cropUploading.value = false
    }
  }

  const handleGalleryOptionClick = (groupKey, option) => {
    if (option.type === 'upload') {
      if (option.previewUrl && !isGalleryOptionActive(groupKey, option)) {
        persistProfileSelection(groupKey, option.previewUrl)
        return
      }
      openUploadPicker(groupKey)
      return
    }

    persistProfileSelection(groupKey, option.url)
  }

  const isGalleryOptionActive = (groupKey, option) => {
    const currentValue = normalizeComparableAsset(profileStyle.value[groupKey])

    if (option.type === 'upload') {
      return Boolean(option.previewUrl) && currentValue === normalizeComparableAsset(option.previewUrl)
    }

    return currentValue === normalizeComparableAsset(option.url)
  }

  return {
    profileDefaultOptions,
    customAssets,
    cropModalVisible,
    cropUploading,
    cropTargetType,
    cropSourceUrl,
    cropSourceFileName,
    galleryGroups,
    fetchProfileDefaultOptions,
    fetchProfileCustomAssets,
    persistProfileSelection,
    setFileInputRef,
    getCropPreset,
    closeCropModal,
    handleLocalFileChange,
    handleCropConfirm,
    handleGalleryOptionClick,
    isGalleryOptionActive,
    ensureDefaultProfileAssets
  }
}
