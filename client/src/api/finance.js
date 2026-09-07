import request from '../utils/request'

// ==================== 部门 ====================

export function listDepartments() {
  return request.get('/api/departments')
}

export function saveDepartment(data) {
  return request.post('/api/departments', data)
}

export function deleteDepartment(id) {
  return request.delete(`/api/departments/${id}`)
}

// ==================== 进项发票 ====================

export function fetchInvoicePage(params) {
  return request.get('/api/invoices-in/page', { params })
}

export function statInvoicesByDepartment() {
  return request.get('/api/invoices-in/stat-by-department')
}

export function saveInvoice(data) {
  return request.post('/api/invoices-in', data)
}

export function deleteInvoice(id) {
  return request.delete(`/api/invoices-in/${id}`)
}
