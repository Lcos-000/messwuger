import axios from 'axios'
import router from '@/router'
import { HTTP_CONFIG, HTTP_STATUS, ROUTE_PATHS, STORAGE_KEYS, REQUEST_MESSAGES } from '@/config'

const service = axios.create({
  baseURL: HTTP_CONFIG.BASE_URL,
  timeout: HTTP_CONFIG.TIMEOUT
})

service.interceptors.request.use(
  config => {
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN)
    if (token) {
      config.headers[HTTP_CONFIG.AUTH_HEADER] = `${HTTP_CONFIG.AUTH_PREFIX} ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

service.interceptors.response.use(
  response => {
    const res = response.data
    const successCodes = [HTTP_STATUS.SUCCESS, HTTP_STATUS.NO_CONTENT]
    if (!successCodes.includes(res.code)) {
      alert(res.message || REQUEST_MESSAGES.DEFAULT_ERROR)
      return Promise.reject(new Error(res.message || REQUEST_MESSAGES.DEFAULT_ERROR))
    } else {
      return res
    }
  },
  error => {
    console.error('Response Error:', error)
    if (error.response) {
      if (error.response.status === HTTP_STATUS.UNAUTHORIZED) {
        alert(REQUEST_MESSAGES.UNAUTHORIZED)
        localStorage.removeItem(STORAGE_KEYS.TOKEN)
        router.replace(ROUTE_PATHS.LOGIN)
      } else {
        alert(error.response.data?.message || REQUEST_MESSAGES.SERVER_ERROR)
      }
    } else {
      alert(REQUEST_MESSAGES.NETWORK_ERROR)
    }
    return Promise.reject(error)
  }
)

export default service
