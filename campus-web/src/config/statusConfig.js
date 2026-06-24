export const HTTP_STATUS = {
  SUCCESS: 200,
  NO_CONTENT: 204,
  UNAUTHORIZED: 401
}

export const SYNC_STATUS = {
  NOT_SYNCED: 0,
  SYNCING: 1,
  SUCCESS: 2,
  FAILED: 3
}

export const PUNCH_STATUS = {
  NOT_PUNCHED: 0,
  PUNCHING: 1,
  SUCCESS: 2,
  FAILED: 3
}

export const STATUS_CLASS = {
  SUCCESS: 'status-ok',
  PROCESSING: 'status-syncing',
  FAILED: 'status-err',
  DEFAULT: ''
}

export const SYNC_STATUS_TEXT = {
  [SYNC_STATUS.NOT_SYNCED]: '未同步',
  [SYNC_STATUS.SYNCING]: '同步中',
  [SYNC_STATUS.SUCCESS]: '同步成功',
  [SYNC_STATUS.FAILED]: '同步失败',
  DEFAULT: '未知状态'
}

export const PUNCH_STATUS_TEXT = {
  [PUNCH_STATUS.NOT_PUNCHED]: '未打卡',
  [PUNCH_STATUS.PUNCHING]: '打卡中',
  [PUNCH_STATUS.SUCCESS]: '打卡成功',
  [PUNCH_STATUS.FAILED]: '打卡失败',
  DEFAULT: '未知状态'
}

export const STATUS_ICON_STYLE = {
  SUCCESS: 'background:#f0fdf4; color:#10b981',
  PROCESSING: 'background:#eef3ff; color:#4f86f7',
  FAILED: 'background:#fee2e2; color:#ef4444',
  DEFAULT: 'background:#f5f5f5; color:#b0bdd4'
}

export const isUserStatusProcessing = (status = {}) => {
  return status.syncStatus === SYNC_STATUS.SYNCING || status.punchStatus === PUNCH_STATUS.PUNCHING
}
