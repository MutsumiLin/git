import { request } from './client.js'

export const workspaceApi = {
  products: role => request(role === 'ADMIN' ? '/api/admin/products' : '/api/merchant/products'),
  rule: () => request('/api/carbon-rules/current'),
  health: () => request('/api/health'),
}
