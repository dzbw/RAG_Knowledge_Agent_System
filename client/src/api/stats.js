import request from '../utils/request'

export function fetchOverview() {
  return request.get('/api/stats/overview')
}
