let csrf = null

export class ApiError extends Error {
  constructor(message, status = 0, code = 'NETWORK_ERROR') {
    super(message); this.status = status; this.code = code
  }
}

export async function refreshCsrf() {
  let response
  try { response = await fetch('/api/auth/csrf', { credentials: 'same-origin' }) }
  catch { throw new ApiError('无法连接服务，请确认后端已启动') }
  const body = await response.json().catch(() => null)
  if (!response.ok || !body?.data?.token) throw new ApiError('无法获取会话校验信息，请刷新重试', response.status)
  csrf = body.data
  return csrf
}

export async function request(path, { method = 'GET', body, form, retry = true } = {}) {
  const unsafe = !['GET', 'HEAD', 'OPTIONS'].includes(method)
  if (unsafe && !csrf) await refreshCsrf()
  const headers = { Accept: 'application/json' }
  if (unsafe) headers[csrf.headerName] = csrf.token
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (form) headers['Content-Type'] = 'application/x-www-form-urlencoded'
  let response
  try {
    response = await fetch(path, {
      method, headers, credentials: 'same-origin',
      body: form ? new URLSearchParams(form) : body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch { throw new ApiError('连接中断，操作结果尚未确认，请使用原请求重试') }
  const result = await response.json().catch(() => null)
  if (response.status === 403 && result?.code === 'CSRF_INVALID' && retry) {
    await refreshCsrf()
    return request(path, { method, body, form, retry: false })
  }
  if (!response.ok) {
    if (response.status === 401) window.dispatchEvent(new Event('session-expired'))
    throw new ApiError(result?.message || `请求失败（${response.status}）`, response.status, result?.code)
  }
  return result?.data
}

export const api = {
  me: () => request('/api/me'),
  login: (username, password) => request('/api/auth/login', { method: 'POST', form: { username, password } }),
  logout: () => request('/api/auth/logout', { method: 'POST' }),
  rule: () => request('/api/carbon-rules/current'),
  summary: () => request('/api/me/points'),
  trips: (page = 0) => request(`/api/me/trips?page=${page}&size=10`),
  ledger: (page = 0) => request(`/api/me/point-ledger?page=${page}&size=10`),
  report: (payload) => request('/api/trips', { method: 'POST', body: payload }),
  products: () => request('/api/products'),
  product: (id) => request(`/api/products/${id}`),
}
