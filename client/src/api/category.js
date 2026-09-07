import request from '../utils/request'

export function listCategories() {
  return request.get('/api/categories')
}

export function saveCategory(data) {
  return request.post('/api/categories', data)
}

export function deleteCategory(id) {
  return request.delete(`/api/categories/${id}`)
}
