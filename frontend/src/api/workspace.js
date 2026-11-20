import { request } from './client.js'
const root = role => role === 'ADMIN' ? '/api/admin' : '/api/merchant'
export const workspaceApi = {
 products: role => request(`${root(role)}/products`),
 saveProduct: (role,id,body) => request(`${root(role)}/products${id?`/${id}`:''}`, {method:id?'PUT':'POST',body}),
 rule: () => request('/api/carbon-rules/current'),
 publishRule: body => request('/api/admin/carbon-rules',{method:'POST',body}),
 orders: () => request('/api/merchant/redemptions'),
 verify: verifyCode => request('/api/merchant/redemptions/verify',{method:'POST',body:{verifyCode}}),
 health: () => request('/api/health'),
}
