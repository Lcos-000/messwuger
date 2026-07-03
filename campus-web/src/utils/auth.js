import { ADMIN_CONFIG, ROUTE_PATHS, STORAGE_KEYS } from '@/config'

const ADMIN_PATH_PREFIX = `${ROUTE_PATHS.ADMIN}`

export const isAdminPath = (path = '') => path === ROUTE_PATHS.ADMIN || path.startsWith(`${ADMIN_PATH_PREFIX}/`)

export const getUserToken = () => localStorage.getItem(STORAGE_KEYS.USER_TOKEN)

export const getAdminToken = () => localStorage.getItem(STORAGE_KEYS.ADMIN_TOKEN)

export const getTokenByPath = (path = window.location.pathname) => (
  isAdminPath(path) ? getAdminToken() : getUserToken()
)

export const hasUserSession = () => Boolean(getUserToken())

export const hasAdminSession = () => Boolean(getAdminToken())

export const hasSessionForPath = (path) => (isAdminPath(path) ? hasAdminSession() : hasUserSession())

export const persistLoginState = (token, adminMode) => {
  if (adminMode) {
    localStorage.setItem(STORAGE_KEYS.ADMIN_TOKEN, token)
    localStorage.setItem(STORAGE_KEYS.LOGIN_MODE, ADMIN_CONFIG.LOGIN_MODE.ADMIN)
    return
  }

  localStorage.setItem(STORAGE_KEYS.USER_TOKEN, token)
  localStorage.setItem(STORAGE_KEYS.LOGIN_MODE, ADMIN_CONFIG.LOGIN_MODE.USER)
}

export const clearUserSession = () => {
  localStorage.removeItem(STORAGE_KEYS.USER_TOKEN)
  if (!hasAdminSession()) {
    localStorage.removeItem(STORAGE_KEYS.LOGIN_MODE)
  }
}

export const clearAdminSession = () => {
  localStorage.removeItem(STORAGE_KEYS.ADMIN_TOKEN)
  if (!hasUserSession()) {
    localStorage.removeItem(STORAGE_KEYS.LOGIN_MODE)
  }
}

export const clearSessionByPath = (path = window.location.pathname) => {
  if (isAdminPath(path)) {
    clearAdminSession()
    return
  }
  clearUserSession()
}

export const getDefaultAuthedRoute = () => {
  const loginMode = localStorage.getItem(STORAGE_KEYS.LOGIN_MODE)

  if (loginMode === ADMIN_CONFIG.LOGIN_MODE.ADMIN && hasAdminSession()) {
    return ROUTE_PATHS.ADMIN
  }
  if (loginMode === ADMIN_CONFIG.LOGIN_MODE.USER && hasUserSession()) {
    return ROUTE_PATHS.SCHEDULE
  }
  if (hasUserSession()) {
    return ROUTE_PATHS.SCHEDULE
  }
  if (hasAdminSession()) {
    return ROUTE_PATHS.ADMIN
  }
  return ROUTE_PATHS.LOGIN
}
