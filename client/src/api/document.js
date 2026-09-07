import request from '../utils/request'

export function fetchDocumentPage(params) {
  return request.get('/api/documents/page', { params })
}

export function uploadDocument(formData) {
  return request.post('/api/documents', formData)
}

export function deleteDocument(id) {
  return request.delete(`/api/documents/${id}`)
}
