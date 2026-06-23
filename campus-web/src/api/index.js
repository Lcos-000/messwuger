import request from '../utils/request'

export const login = (data) => request({ url: '/auth/login', method: 'post', data })
export const register = (data) => request({ url: '/auth/register', method: 'post', data })
export const logout = () => request({ url: '/auth/logout', method: 'post' })
export const refreshSchedule = () => request({ url: '/auth/refresh', method: 'post' })
export const getSchedule = () => request({ url: '/user/schedule/get', method: 'get' })

export const getPersonalInfo = () => request({ url: '/user/personal', method: 'get' })

export const getUserStatus = () => request({ url: '/user/status', method: 'get' })

export const deleteAccount = () => request({ url: '/user/delete', method: 'delete' })

