import request from '../utils/request'

/** 登录 */
export function login(data) {
  return request.post('/api/auth/login', data)
}
