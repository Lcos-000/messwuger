export const PROFILE_VIEW_CONFIG = {
  HERO_EXPANDED_HEIGHT: 320,
  HERO_COLLAPSED_HEIGHT: 160,
  HERO_COLLAPSE_DISTANCE: 180,
  CONTENT_OVERLAY_EXPANDED: 56,
  CONTENT_OVERLAY_COLLAPSED: 112,

  CARD_BG_OPACITY_MIN: 0,
  CARD_BG_OPACITY_DEFAULT: 1,
  CARD_OPACITY_SCROLL_DELTA: 0.08,
  CARD_OPACITY_SAVE_DEBOUNCE: 350,
  CARD_OPACITY_MAX: 1,
  CARD_OPACITY_STEP: 0.01,

  CARD_BLUR: 14,
  CARD_BLUR_MIN: 0,
  CARD_BLUR_MAX: 30,
  CARD_BLUR_DEFAULT: 14,
  CARD_BLUR_STEP: 1,

  PAGE_WALLPAPER_OPACITY: 0.4,
  PAGE_WALLPAPER_MASK_ALPHA: 0.04,
  WALLPAPER_MASK_MIN: 0,
  WALLPAPER_MASK_MAX: 1,
  WALLPAPER_MASK_DEFAULT: 1.00,
  WALLPAPER_MASK_STEP: 0.01,
  WALLPAPER_SCALE_MIN: 1,
  WALLPAPER_SCALE_MAX: 1.16,
  WALLPAPER_SCALE_DELTA: 0.16,
  HERO_SURFACE_HEIGHT_OFFSET: 28,
  HERO_SURFACE_MIN_HEIGHT_OFFSET: 20,
  HERO_SURFACE_SCROLL_FACTOR: -18,
  HERO_SURFACE_TOP_OFFSET: -54,
  HERO_SECTION_PADDING_TOP: 40,
  HERO_SECTION_PADDING_X: 24,
  HERO_SECTION_PADDING_BOTTOM: 94,
  HERO_BG_SIDE_INSET: 18,
  HERO_BG_BOTTOM_INSET: 14,
  HERO_BG_RADIUS_TOP: 34,
  HERO_BG_RADIUS_BOTTOM: 40,
  HERO_BG_SHADOW: '0 12px 28px rgba(15, 25, 50, 0.06)',
  HERO_FRAME_SIDE_INSET: 5,
  HERO_FRAME_EXTRA_HEIGHT: 80,
  HERO_FRAME_RADIUS_TOP: 40,
  HERO_FRAME_RADIUS_BOTTOM: 44,
  HERO_FRAME_BG: '#ffffff',
  HERO_FRAME_SHADOW: '0 10px 22px rgba(15, 25, 50, 0.04)',
  HERO_BLEND_HEIGHT: 174,
  HERO_GLOW_SIZE: 220,
  HERO_GLOW_TOP: '-12%',
  HERO_GLOW_RIGHT: '-12%',

  GALLERY_TOGGLE_DURATION: 240,
  GALLERY_MAX_HEIGHT: 720,
  GALLERY_TITLE: '个性化设置',
  GALLERY_HELP_TEXT: '点击此卡片可展开选择显示设置，以及头像、顶部背景和墙纸。你可以直接上传自定义图片，也可以继续使用系统预置图片。',
  OPTION_GROUP_HINT: '点击即可切换',

  DISPLAY_SETTINGS: [
    {
      key: 'cardOpacity',
      type: 'range',
      title: '资料卡不透明度',
      helpText: '拖动滑块可以调整资料卡的透明度。',
      min: 0,
      max: 1,
      step: 0.01,
      format: 'percent'
    },
    {
      key: 'cardBlur',
      type: 'range',
      title: '资料卡模糊度',
      helpText: '拖动滑块可以调整资料卡背景的模糊程度。',
      min: 0,
      max: 30,
      step: 1,
      format: 'pixel'
    },
    {
      key: 'wallpaperMask',
      type: 'range',
      title: '墙纸蒙版强度',
      helpText: '向右调大后，墙纸会更清淡、更简洁；调到 0 则保持最清晰的原图。推荐值为 0.5。',
      min: 0,
      max: 1,
      step: 0.01,
      format: 'percent'
    },
    {
      key: 'globalFontEnabled',
      type: 'toggle',
      title: '全局字体',
      helpText: '开启后页面将使用自定义全局字体，关闭则使用系统默认字体。'
    }
  ],

  ACTION_TOGGLES: [
    {
      key: 'autoPunchEnabled',
      label: '开启自动打卡',
      enabledText: '已开启，系统会参与定时打卡任务',
      disabledText: '已关闭，不会参与自动打卡',
      iconStyle: 'background:#f0fdf4; color:#10b981'
    }
  ],

  FONT_FACE: {
    family: 'SourceHanSerifCN',
    path: '/fonts/SourceHanSerifCN-Regular.ttf',
    format: 'truetype'
  },

  AVATAR_THUMB_SIZE: 64,
  COVER_THUMB_RATIO: '16 / 10',
  TOOLTIP_MIN_WIDTH: 220,
  TOOLTIP_MAX_WIDTH: 280,
  AVATAR_GRID_MIN: 64,
  COVER_GRID_MIN: 96,
  COVER_CARD_MAX_WIDTH: 160,
  CUSTOM_BADGE_TEXT: '自定义',
  REPLACE_TILE_TEXT: '点击替换',
  UPLOAD_TILE_TEXT: '上传图片',
  UPLOAD_TILE_SUBTEXT: '点击上传',
  CROP_MODAL_TITLE: '裁剪图片',
  CROP_MODAL_HINT: '拖动图片调整位置，滑动缩放控制取景范围。',
  CROP_PRESETS: {
    avatar: { aspectRatio: 1, maxWidth: 260, maxHeight: 260, outputWidth: 720, outputHeight: 720, label: '头像' },
    background: { aspectRatio: 16 / 10, maxWidth: 320, maxHeight: 220, outputWidth: 1600, outputHeight: 1000, label: '顶部背景' },
    wallpaper: { aspectRatio: 9 / 16, maxWidth: 240, maxHeight: 420, outputWidth: 1080, outputHeight: 1920, label: '墙纸' }
  },

  OPTION_GROUPS: [
    {
      key: 'avatar',
      title: '头像',
      optionField: 'avatars',
      customField: 'customAvatar',
      optionClass: 'avatar-option',
      gridClass: 'avatar-gallery',
      imageAlt: '默认头像'
    },
    {
      key: 'background',
      title: '顶部背景',
      optionField: 'backgrounds',
      customField: 'customBackground',
      optionClass: 'cover-option',
      gridClass: 'cover-gallery',
      imageAlt: '默认顶部背景'
    },
    {
      key: 'wallpaper',
      title: '墙纸',
      optionField: 'wallpapers',
      customField: 'customWallpaper',
      optionClass: 'cover-option',
      gridClass: 'cover-gallery',
      imageAlt: '默认壁纸'
    }
  ],

  INFO_FIELDS: [
    {
      key: 'college',
      label: '学校/学院',
      fallback: 'DEFAULT_COLLEGE',
      iconStyle: 'background:#fef3c7; color:#f59e0b',
      icon: 'college'
    },
    {
      key: 'major',
      label: '专业',
      fallback: 'DEFAULT_USER_NAME',
      iconStyle: 'background:#eef3ff; color:#4f86f7',
      icon: 'major'
    },
    {
      key: 'className',
      label: '班级',
      fallback: 'DEFAULT_USER_NAME',
      iconStyle: 'background:#f0fdf4; color:#10b981',
      icon: 'class'
    }
  ],

  STATUS_FIELDS: [
    { key: 'sync', label: '同步状态' },
    { key: 'punch', label: '打卡状态' }
  ],

  ACTIONS: {
    logout: {
      label: '退出登录',
      color: '#f97316',
      iconStyle: 'background:#fff7ed; color:#f97316'
    },
    deleteAccount: {
      label: '注销账号',
      loadingLabel: '注销中...',
      color: '#ef4444',
      iconStyle: 'background:#fee2e2; color:#ef4444'
    }
  },

  ICONS: {
    college: 'college',
    major: 'major',
    class: 'class',
    sync: 'sync',
    punch: 'punch',
    autoPunch: 'autoPunch',
    logout: 'logout',
    deleteAccount: 'deleteAccount'
  },

  FOOTER_HINT: '数据来源于教务系统，仅供参考'
}

export const PROFILE_THEME_CONFIG = {
  START: '#4f86f7',
  END: '#6366f1',
  GLOW: 'rgba(255,255,255,0.16)'
}

