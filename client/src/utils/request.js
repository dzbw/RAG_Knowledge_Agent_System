import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import router from '../router'

/**
 * Axios 实例：开发环境通过 Vite 代理访问后端。
 */
const request = axios.create({
  baseURL: '',
  timeout: 120000,
})

request.interceptors.request.use((config) => {
  const store = useUserStore()
  if (store.token) {
    config.headers.Authorization = `Bearer ${store.token}`
  }
  return config
})

request.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body.code !== 200) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(body)
    }
    return body
  },
  (err) => {
    if (err.response?.status === 401 || err.response?.status === 403) {
      useUserStore().logout()
      router.push('/login')
    }
    const isTimeout = err.code === 'ECONNABORTED' || err.message?.includes('timeout')
    if (isTimeout) {
      ElMessage.error('请求超时：模型或知识库检索耗时过长，请稍后再试或缩短问题 / 检查 Ollama 是否正常运行')
    }
    else {
      ElMessage.error(err.response?.data?.message || err.message || '网络错误')
    }
    return Promise.reject(err)
  }
)

export default request
