import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 登录态：Token + 用户信息（本地持久化）。
 */
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  /**
   * 登录成功后写入。
   */
  function setLogin(t, u) {
    token.value = t
    user.value = u
    localStorage.setItem('token', t)
    localStorage.setItem('user', JSON.stringify(u))
  }

  /**
   * 清除登录态。
   */
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, setLogin, logout }
})
