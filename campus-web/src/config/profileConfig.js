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
  CARD_OPACITY_TITLE: '资料卡透明度',
  CARD_OPACITY_HELP_TEXT: '拖动滑块可以调整资料卡的透明度。',

  CARD_BLUR: 14,
  CARD_BLUR_MIN: 0,
  CARD_BLUR_MAX: 30,
  CARD_BLUR_DEFAULT: 30,
  CARD_BLUR_STEP: 1,
  CARD_BLUR_TITLE: '资料卡模糊度',
  CARD_BLUR_HELP_TEXT: '拖动滑块可以调整资料卡背景的模糊程度。',

  GLOBAL_FONT_ENABLED_TITLE: '全局字体',
  GLOBAL_FONT_ENABLED_HELP_TEXT: '开启后页面将使用自定义全局字体，关闭则使用系统默认字体。',
  FONT_FACE: {
    family: 'SourceHanSerifCN',
    path: '/fonts/SourceHanSerifCN-Regular.ttf',
    format: 'truetype'
  },

  PAGE_WALLPAPER_OPACITY: 0.4,
  PAGE_WALLPAPER_MASK_ALPHA: 0.04,

  GALLERY_TOGGLE_DURATION: 240,
  GALLERY_MAX_HEIGHT: 720,
  GALLERY_TITLE: '个性化设置',
  GALLERY_HELP_TEXT: '点击此卡片可展开选择默认头像、背景和壁纸，后续自定义上传也会收纳在这里。',
  OPTION_GROUP_HINT: '点击即可切换',

  AVATAR_THUMB_SIZE: 64,
  COVER_THUMB_RATIO: '16 / 10',
  TOOLTIP_MIN_WIDTH: 220,
  TOOLTIP_MAX_WIDTH: 280,
  AVATAR_GRID_MIN: 64,
  COVER_GRID_MIN: 96,
  COVER_CARD_MAX_WIDTH: 160,

  OPTION_GROUPS: [
    {
      key: 'avatar',
      title: '默认头像',
      optionField: 'avatars',
      optionClass: 'avatar-option',
      gridClass: 'avatar-gallery',
      imageAlt: '默认头像'
    },
    {
      key: 'background',
      title: '默认顶部背景',
      optionField: 'backgrounds',
      optionClass: 'cover-option',
      gridClass: 'cover-gallery',
      imageAlt: '默认顶部背景'
    },
    {
      key: 'wallpaper',
      title: '默认壁纸',
      optionField: 'wallpapers',
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
    autoPunch: {
      label: '开启自动打卡',
      iconStyle: 'background:#f0fdf4; color:#10b981'
    },
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
