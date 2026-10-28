export const modes = { BUS: '公交', METRO: '地铁', BIKE: '骑行' }
export const number = value => new Intl.NumberFormat('zh-CN').format(value ?? 0)
export const carbon = value => Number(value || 0).toFixed(3)
export const dateTime = value => new Intl.DateTimeFormat('zh-CN', {
  timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit',
}).format(new Date(value))

export function localInput(date = new Date(Date.now() - 60_000)) {
  const d = new Date(date.getTime() - date.getTimezoneOffset() * 60_000)
  return d.toISOString().slice(0, 16)
}

export function validDistance(value) {
  return /^(?:0|[1-9]\d{0,2})(?:\.\d{1,2})?$/.test(value) && Number(value) >= 0.01 && Number(value) <= 100
}

// 页面预览也使用整数缩放计算，实际记账始终以后端结果为准。
export function estimate(value, mode, rule) {
  if (!validDistance(value) || !rule) return null
  const scaled = factor => BigInt(String(factor).split('.')[0]) * 1_000_000n + BigInt((String(factor).split('.')[1] || '').padEnd(6, '0').slice(0, 6))
  const fields = { BUS: 'busFactor', METRO: 'metroFactor', BIKE: 'bikeFactor' }
  if (!fields[mode]) return null
  const hundredths = BigInt(Math.round(Number(value) * 100))
  const difference = scaled(rule.carFactor) - scaled(rule[fields[mode]])
  const saved = hundredths * (difference > 0n ? difference : 0n)
  return { carbon: Number(saved) / 100_000_000, points: Number(saved * BigInt(rule.pointsPerKg) / 100_000_000n) }
}
