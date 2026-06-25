export const PROFILE_VIEW_CONFIG = {
  // 个人页主体区域的最大展开高度（px）
  HERO_EXPANDED_HEIGHT: 320,
  // 个人页主体区域滚动到折叠后的高度（px）
  HERO_COLLAPSED_HEIGHT: 160,
  // 个人页在多远滚动距离内完成折叠动画（px）
  HERO_COLLAPSE_DISTANCE: 180,
  // 内容区与顶部背景之间的叠压距离，展开态（px）
  CONTENT_OVERLAY_EXPANDED: 56,
  // 内容区与顶部背景之间的叠压距离，折叠态（px）
  CONTENT_OVERLAY_COLLAPSED: 112,
  // 资料卡透明度滑块的最小值
  CARD_BG_OPACITY_MIN: 0.2,
  // 资料卡透明度随滚动变化的衰减值
  CARD_OPACITY_SCROLL_DELTA: 0.08,
  // 卡片透明度滑块的默认值（注册/首次加载时兜底）
  CARD_BG_OPACITY_DEFAULT: 1.00,
  // 卡片背景模糊强度（px）
  CARD_BLUR: 14,
  // 页面墙纸层不透明度
  PAGE_WALLPAPER_OPACITY: 0.4,
  // 页面墙纸层遮罩透明度
  PAGE_WALLPAPER_MASK_ALPHA: 0.04,
  // 资料卡透明度自动保存的防抖时间（ms）
  CARD_OPACITY_SAVE_DEBOUNCE: 350,
  // 默认图库卡片展开/收起过渡时长（ms）
  GALLERY_TOGGLE_DURATION: 240,
  // 默认图库卡片过渡允许的最大展开高度（px）
  GALLERY_MAX_HEIGHT: 720,
  // 默认图库中头像缩略图建议尺寸（px）
  AVATAR_THUMB_SIZE: 64,
  // 默认图库中卡片缩略图的宽高比
  COVER_THUMB_RATIO: '16 / 10',
  // 默认图库帮助提示文字
  GALLERY_HELP_TEXT: '点击此卡片可展开选择默认头像、背景和墙纸，后续自定义上传也会收纳在这里。',
  // 资料卡透明度帮助提示文字
  CARD_OPACITY_HELP_TEXT: '拖动滑块可调整资料卡的透明度。',
  // 个性化设置卡片标题
  GALLERY_TITLE: '个性化设置',
  // 资料卡透明度标题
  CARD_OPACITY_TITLE: '资料卡透明度',
  // 默认图库分组标题与辅助文案
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
      title: '默认墙纸',
      optionField: 'wallpapers',
      optionClass: 'cover-option',
      gridClass: 'cover-gallery',
      imageAlt: '默认墙纸'
    }
  ],
  OPTION_GROUP_HINT: '点击即可切换',
  // 资料卡透明度滑块步进与上限
  CARD_OPACITY_MAX: 1,
  CARD_OPACITY_STEP: 0.01,
  // 默认图库提示气泡的宽度范围（px）
  TOOLTIP_MIN_WIDTH: 220,
  TOOLTIP_MAX_WIDTH: 280,
  // 默认图库缩略图网格最小宽度（px）
  AVATAR_GRID_MIN: 64,
  COVER_GRID_MIN: 96,
  // 背景图/墙纸缩略图的最大宽度（px）
  COVER_CARD_MAX_WIDTH: 160,
  // 信息卡与操作区文案配置
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
  // 信息卡、操作卡使用的图标模板
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
