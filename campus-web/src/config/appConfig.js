export const APP_CONFIG = {
  APP_TITLE: '校园课表查询',
  DEFAULT_COLLEGE: '西南大学',
  DEFAULT_USER_NAME: '暂未同步',
  UNKNOWN_STUDENT_ID: '学号未知',
  DEFAULT_AVATAR_TEXT: '?',
  AUTO_PUNCH_TIP: '当前测试阶段，自动打卡功能已默认全部开启，暂不支持手动切换。',
  LOGOUT_CONFIRM: '确定要退出登录吗？',
  DELETE_ACCOUNT_CONFIRM: '警告：注销账号将删除所有数据且无法恢复！确定继续吗？',
  DELETE_ACCOUNT_SUCCESS: '账号已注销',
  REGISTER_SUCCESS: '注册成功，数据同步需要一点时间',
  FORM_INCOMPLETE_TIP: '请填写完整信息',
  SCHEDULE_SYNCING_TIP: '已触发刷新，正在拉取最新数据',
  SCHEDULE_SYNC_FAILED_TIP: '数据同步失败，请稍后再试'
}

export const HTTP_CONFIG = {
  BASE_URL: '/api',
  TIMEOUT: 10000,
  AUTH_HEADER: 'Authorization',
  AUTH_PREFIX: 'Bearer'
}

export const REQUEST_MESSAGES = {
  DEFAULT_ERROR: 'Error',
  UNAUTHORIZED: '登录状态已过期，请重新登录',
  SERVER_ERROR: '网络或服务器错误',
  NETWORK_ERROR: '网络异常，请稍后再试'
}
