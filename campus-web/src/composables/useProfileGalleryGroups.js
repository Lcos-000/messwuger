import { computed } from 'vue'
import { PROFILE_VIEW_CONFIG } from '@/config'

export const useProfileGalleryGroups = ({ customAssets, profileDefaultOptions }) => {
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
          style: customUrl
            ? {
                backgroundImage: `linear-gradient(rgba(15, 23, 42, 0.18), rgba(15, 23, 42, 0.18)), url(${customUrl})`,
                backgroundSize: 'cover',
                backgroundPosition: 'center'
              }
            : null
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

  return {
    galleryGroups
  }
}
