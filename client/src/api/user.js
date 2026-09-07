import request from '../utils/request'

export function fetchUserPage(params) {
  return request.get('/api/users/page', { params })
}

export function saveUser(data) {
  return request.post('/api/users', data)
}

export function deleteUser(id) {
  return request.delete(`/api/users/${id}`)
}

export function fetchMe() {
  return request.get('/api/users/me')
}

export function updateProfile(data) {
  return request.put('/api/users/me/profile', data)
}

export function changePassword(oldPassword, newPassword) {
  return request.put('/api/users/me/password', null, {
    params: { oldPassword, newPassword },
  })
}

/** @param {FormData} formData 含字段 file */
export function uploadAvatar(formData) {
  return request.post('/api/users/me/avatar', formData)
}
