import assert from 'node:assert/strict'
import { randomUUID } from 'node:crypto'
import { readFile, writeFile, mkdir } from 'node:fs/promises'
import { dirname } from 'node:path'

const args = process.argv.slice(2)
const option = name => { const i = args.indexOf(name); return i < 0 ? undefined : args[i + 1] }
const base = (option('--base-url') || 'http://127.0.0.1:8080').replace(/\/$/, '')
const password = process.env.DEMO_PASSWORD || 'GreenDemo!2026'
const results = []

class Client {
  cookies = new Map()
  csrf = null
  async request(path, { method = 'GET', body, form, useCsrf = true, expected = 200 } = {}) {
    const headers = { Accept: 'application/json' }
    if (this.cookies.size) headers.Cookie = [...this.cookies].map(([k, v]) => `${k}=${v}`).join('; ')
    if (useCsrf && method !== 'GET' && this.csrf) headers[this.csrf.headerName] = this.csrf.token
    if (body !== undefined) headers['Content-Type'] = 'application/json'
    if (form) headers['Content-Type'] = 'application/x-www-form-urlencoded'
    const response = await fetch(base + path, {
      method, headers, redirect: 'manual',
      body: form ? new URLSearchParams(form) : body !== undefined ? JSON.stringify(body) : undefined,
      signal: AbortSignal.timeout(15000),
    })
    for (const cookie of response.headers.getSetCookie()) {
      const pair = cookie.split(';')[0], i = pair.indexOf('=')
      this.cookies.set(pair.slice(0, i), pair.slice(i + 1))
    }
    const responseBody = await response.json()
    assert.equal(response.status, expected, `${method} ${path}: ${responseBody.message}`)
    return responseBody.data
  }
  async login(username) {
    this.csrf = await this.request('/api/auth/csrf')
    const identity = await this.request('/api/auth/login', { method: 'POST', form: { username, password } })
    this.csrf = await this.request('/api/auth/csrf')
    return identity
  }
}

async function check(name, action) {
  await action()
  results.push({ name, result: 'PASS' })
  console.log(`PASS ${name}`)
}

async function main() {
  const user = new Client()
  const restartSource = option('--verify-restart')
  if (restartSource) {
    const original = JSON.parse(await readFile(restartSource, 'utf8'))
    await user.login('citizen')
    await check('后端重启后行程与积分仍存在', async () => {
      const trip = await user.request(`/api/me/trips/${original.tripId}`)
      assert.equal(trip.clientTripId, original.payload.clientTripId)
      assert.equal(trip.earnedPoints, 120)
      assert.equal((await user.request('/api/me/points')).balance, original.expectedBalance)
    })
    await check('后端重启后同编号仍然幂等', async () => {
      const replay = await user.request('/api/trips', { method: 'POST', body: original.payload })
      assert.equal(replay.duplicate, true)
      assert.equal((await user.request('/api/me/points')).balance, original.expectedBalance)
    })
    return { results, restartSource }
  }
  const guest = new Client()
  await check('健康检查和未登录边界', async () => {
    assert.equal((await guest.request('/api/health')).status, 'UP')
    await guest.request('/api/me', { expected: 401 })
  })
  const identity = await user.login('citizen')
  assert.equal(identity.role, 'USER')
  const before = await user.request('/api/me/points')
  const payload = { clientTripId: randomUUID(), mode: 'BUS', distanceKm: '10.00', completedAt: new Date(Date.now() - 300000).toISOString() }
  let tripId
  await check('公交十公里保存行程并发放一百二十分', async () => {
    const result = await user.request('/api/trips', { method: 'POST', body: payload, expected: 201 })
    tripId = result.trip.id
    assert.equal(result.trip.earnedPoints, 120)
    assert.equal(Number(result.trip.savedCarbonKg), 1.2)
    assert.equal((await user.request('/api/me/points')).balance, before.balance + 120)
  })
  await check('同一请求返回原行程且不重复发分', async () => {
    const replay = await user.request('/api/trips', { method: 'POST', body: payload })
    assert.equal(replay.duplicate, true); assert.equal(replay.trip.id, tripId)
    assert.equal((await user.request('/api/me/points')).balance, before.balance + 120)
  })
  await check('同编号修改内容产生冲突', () => user.request('/api/trips', { method: 'POST', body: { ...payload, distanceKm: '11.00' }, expected: 409 }))
  await check('缺少 CSRF 信息时无法上报', () => user.request('/api/trips', { method: 'POST', body: payload, useCsrf: false, expected: 403 }))
  await check('商品浏览只展示上架商品', async () => {
    const catalog = await user.request('/api/products')
    assert.ok(catalog.items.length >= 3)
    assert.ok(catalog.items.every(item => item.status === 'PUBLISHED'))
  })
  const other = new Client(); await other.login('citizen2')
  await check('他人无法读取本人的行程', () => other.request(`/api/me/trips/${tripId}`, { expected: 404 }))
  await check('普通用户不能访问运营后台', () => user.request('/api/admin/products', { expected: 403 }))
  const operator = new Client(); await operator.login('operator')
  await check('运营可以读取全部商品', async () => assert.ok((await operator.request('/api/admin/products')).length >= 4))
  const merchant = new Client(); const shop = await merchant.login('merchant')
  await check('商户只能读取本店商品', async () => assert.ok((await merchant.request('/api/merchant/products')).every(item => item.merchantId === shop.merchantId)))
  await check('积分流水对应本次行程', async () => {
    const ledger = await user.request('/api/me/point-ledger')
    assert.ok(ledger.items.some(item => item.bizId === tripId && item.delta === 120))
  })
  return { results, tripId, payload, expectedBalance: before.balance + 120 }
}

try {
  const report = { executedAt: new Date().toISOString(), baseUrl: base, ...(await main()) }
  const output = option('--output')
  if (output) { await mkdir(dirname(output), { recursive: true }); await writeFile(output, JSON.stringify(report, null, 2) + '\n') }
  console.log(`\n${results.length} 项 HTTP 验证通过`)
} catch (error) {
  console.error(`FAIL ${error.message}`)
  process.exitCode = 1
}
