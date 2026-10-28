import { reactive } from 'vue'
import { api, refreshCsrf } from '../api/client.js'

export const auth = reactive({ user: null, initialized: false, connectionError: '' })
let loading = null

export async function ensureSession() {
  if (auth.initialized) return auth.user
  if (!loading) {
    loading = api.me().then(user => { auth.user = user; auth.connectionError = '' })
      .catch(error => { auth.user = null; if (error.status !== 401) auth.connectionError = error.message })
      .finally(() => { auth.initialized = true; loading = null })
  }
  await loading
  return auth.user
}

export async function signIn(username, password) {
  auth.user = await api.login(username, password)
  auth.initialized = true
  auth.connectionError = ''
  await refreshCsrf()
  return auth.user
}

export async function signOut() {
  // 只有后端退出成功后才清除本地身份，网络错误时保留可重试状态。
  await api.logout()
  auth.user = null
  await refreshCsrf()
}

export function homeFor(user) { return user?.role === 'USER' ? '/' : '/workspace' }
