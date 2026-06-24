import request from '../utils/request'
import { API_PATHS } from '@/config'

export const login = (data) => request({ url: API_PATHS.AUTH.LOGIN, method: 'post', data })
export const register = (data) => request({ url: API_PATHS.AUTH.REGISTER, method: 'post', data })
export const logout = () => request({ url: API_PATHS.AUTH.LOGOUT, method: 'post' })
export const refreshSchedule = () => request({ url: API_PATHS.AUTH.REFRESH, method: 'post' })
export const getSchedule = () => request({ url: API_PATHS.SCHEDULE.GET, method: 'get' })
export const getPersonalInfo = () => request({ url: API_PATHS.USER.PERSONAL, method: 'get' })
export const getUserStatus = () => request({ url: API_PATHS.USER.STATUS, method: 'get' })
export const deleteAccount = () => request({ url: API_PATHS.USER.DELETE, method: 'delete' })

