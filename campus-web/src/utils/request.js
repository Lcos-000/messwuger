import axios from 'axios'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('campus_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      alert(res.message || 'Error')
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res
    }
  },
  error => {
    console.error('Response Error:', error)
    if (error.response) {
      if (error.response.status === 401) {
        alert('登录状态已过期，请重新登录')
        localStorage.removeItem('campus_token')
        router.replace('/login')
      } else {
        alert(error.response.data?.message || '网络或服务器错误')
      }
    } else {
      alert('网络异常，请稍后再试')
    }
    return Promise.reject(error)
  }
)

export default service
