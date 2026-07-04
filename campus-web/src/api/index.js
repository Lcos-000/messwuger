import axios from 'axios'
import request from '../utils/request'
import { API_PATHS, HTTP_CONFIG } from '@/config'
import { getAdminToken } from '@/utils/auth'

export const login = (data) => request({ url: API_PATHS.AUTH.LOGIN, method: 'post', data })
export const adminLogin = (data) => request({ url: API_PATHS.AUTH.ADMIN_LOGIN, method: 'post', data })
export const register = (data) => request({ url: API_PATHS.AUTH.REGISTER, method: 'post', data })
export const logout = () => request({ url: API_PATHS.AUTH.LOGOUT, method: 'post' })
export const adminLogout = () => request({ url: API_PATHS.AUTH.ADMIN_LOGOUT, method: 'post' })
export const refreshSchedule = () => request({ url: API_PATHS.AUTH.REFRESH, method: 'post' })
export const getAdminResources = () => request({ url: API_PATHS.ADMIN.RESOURCES, method: 'get' })
export const getAdminLogFiles = () => request({ url: API_PATHS.ADMIN_LOGS.FILES, method: 'get' })
export const initAdminLogTail = (fileName) => request({
  url: API_PATHS.ADMIN_LOGS.TAIL_INIT,
  method: 'get',
  params: { fileName }
})
export const pollAdminLogTail = (fileName, offset) => request({
  url: API_PATHS.ADMIN_LOGS.TAIL_POLL,
  method: 'get',
  params: { fileName, offset }
})
export const historyAdminLogTail = (fileName, beforeOffset) => request({
  url: API_PATHS.ADMIN_LOGS.TAIL_HISTORY,
  method: 'get',
  params: { fileName, beforeOffset }
})
export const downloadAdminLog = (fileName) => {
  const token = getAdminToken()
  return axios.get(`${HTTP_CONFIG.BASE_URL}${API_PATHS.ADMIN_LOGS.DOWNLOAD}`, {
    params: { fileName },
    responseType: 'blob',
    headers: token ? { [HTTP_CONFIG.AUTH_HEADER]: `${HTTP_CONFIG.AUTH_PREFIX} ${token}` } : {}
  })
}
export const getSchedule = () => request({ url: API_PATHS.SCHEDULE.GET, method: 'get' })
export const getPersonalInfo = () => request({ url: API_PATHS.USER.PERSONAL, method: 'get' })
export const getUserStatus = () => request({ url: API_PATHS.USER.STATUS, method: 'get' })
export const deleteAccount = () => request({ url: API_PATHS.USER.DELETE, method: 'delete' })
export const updateAutoPunch = (data) => request({ url: API_PATHS.USER.AUTO_PUNCH, method: 'put', data })
export const submitGradesTask = (data) => request({ url: API_PATHS.USER.GRADES_TASK, method: 'post', data })
export const getGrades = (params) => request({ url: API_PATHS.USER.GRADES, method: 'get', params })
export const getProfileStyle = () => request({ url: API_PATHS.PERSONALIZATION.GET_PROFILE, method: 'get' })
export const updateProfileStyle = (data) => request({ url: API_PATHS.PERSONALIZATION.UPDATE_PROFILE, method: 'put', data })
export const getProfileDefaultOptions = () => request({ url: API_PATHS.PERSONALIZATION.GET_DEFAULT_OPTIONS, method: 'get' })
export const getProfileCustomAssets = () => request({ url: API_PATHS.PERSONALIZATION.GET_CUSTOM_ASSETS, method: 'get' })
export const uploadProfileCustomAsset = (type, file) => {
  const formData = new FormData()
  formData.append('type', type)
  formData.append('file', file)
  return request({
    url: API_PATHS.PERSONALIZATION.UPLOAD_CUSTOM_ASSET,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
