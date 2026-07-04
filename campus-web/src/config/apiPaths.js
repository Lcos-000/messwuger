export const API_PATHS = {
  AUTH: {
    LOGIN: '/auth/login',
    ADMIN_LOGIN: '/admin/login',
    REGISTER: '/auth/register',
    LOGOUT: '/auth/logout',
    ADMIN_LOGOUT: '/admin/logout',
    REFRESH: '/auth/refresh'
  },
  ADMIN: {
    RESOURCES: '/admin/resources'
  },
  ADMIN_LOGS: {
    FILES: '/admin/logs/files',
    TAIL_INIT: '/admin/logs/tail/init',
    TAIL_POLL: '/admin/logs/tail/poll',
    TAIL_HISTORY: '/admin/logs/tail/history',
    DOWNLOAD: '/admin/logs/download'
  },
  USER: {
    STATUS: '/user/status',
    PERSONAL: '/user/personal',
    DELETE: '/user/delete',
    AUTO_PUNCH: '/user/auto-punch',
    GRADES_TASK: '/user/grades/task',
    GRADES: '/user/grades'
  },
  PERSONALIZATION: {
    GET_PROFILE: '/personalization/get-profile',
    UPDATE_PROFILE: '/personalization/update-profile',
    GET_DEFAULT_OPTIONS: '/personalization/get-default-options',
    GET_CUSTOM_ASSETS: '/personalization/get-custom-assets',
    UPLOAD_CUSTOM_ASSET: '/personalization/upload-custom-asset'
  },
  SCHEDULE: {
    GET: '/user/schedule/get'
  }
}
