import axios from 'axios'
import router from '@/router'
import {
  HTTP_CONFIG,
  HTTP_STATUS,
  ROUTE_PATHS,
  STORAGE_KEYS,
  REQUEST_MESSAGES,
  isSpiderRemoteError,
  isCourseRemoteError,
  isRemoteFlowLimited
} from '@/config'

const service = axios.create({
  baseURL: HTTP_CONFIG.BASE_URL,
  timeout: HTTP_CONFIG.TIMEOUT
})

const resolveBusinessErrorMessage = (code, fallbackMessage) => {
  if (isSpiderRemoteError(code)) {
    return fallbackMessage || REQUEST_MESSAGES.SPIDER_REMOTE_ERROR
  }
  if (isCourseRemoteError(code)) {
    return fallbackMessage || REQUEST_MESSAGES.COURSE_REMOTE_ERROR
  }
  if (isRemoteFlowLimited(code)) {
    return fallbackMessage || REQUEST_MESSAGES.FLOW_LIMIT
  }
  return fallbackMessage || REQUEST_MESSAGES.DEFAULT_ERROR
}

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
    if (res.code === HTTP_STATUS.UNAUTHORIZED) {
      alert(res.message || REQUEST_MESSAGES.UNAUTHORIZED)
      localStorage.removeItem(STORAGE_KEYS.TOKEN)
      router.replace(ROUTE_PATHS.LOGIN)
      return Promise.reject(new Error(res.message || REQUEST_MESSAGES.UNAUTHORIZED))
    }
    if (!successCodes.includes(res.code)) {
      const errorMessage = resolveBusinessErrorMessage(res.code, res.message)
      alert(errorMessage)
      return Promise.reject(new Error(errorMessage))
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
        const responseCode = error.response.data?.code
        const responseMessage = error.response.data?.message
        alert(resolveBusinessErrorMessage(responseCode, responseMessage || REQUEST_MESSAGES.SERVER_ERROR))
      }
    } else {
      alert(REQUEST_MESSAGES.NETWORK_ERROR)
    }
    return Promise.reject(error)
  }
)

export default service
