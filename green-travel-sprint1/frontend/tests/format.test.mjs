import { test } from 'node:test'
import assert from 'node:assert/strict'
import { estimate, validDistance, carbon, modes } from '../src/utils/format.js'

const rule = { carFactor: 0.2, busFactor: 0.08, metroFactor: 0.05, bikeFactor: 0, pointsPerKg: 100 }
test('三种交通方式预览与后端规则一致', () => {
  assert.equal(estimate('10.00', 'BUS', rule).points, 120)
  assert.equal(estimate('10.00', 'METRO', rule).points, 150)
  assert.equal(estimate('10.00', 'BIKE', rule).points, 200)
})
test('小数预览不因二进制浮点误差少发一分', () => {
  assert.deepEqual(estimate('0.25', 'BUS', rule), { carbon: 0.03, points: 3 })
  assert.equal(estimate('0.01', 'BUS', rule).points, 0)
  assert.equal(estimate('0.09', 'BUS', rule).points, 1)
})
test('里程有效等价类及边界', () => {
  for (const input of ['0.01', '1', '10.00', '100.00']) assert.ok(validDistance(input))
  for (const input of ['', '0', '-1', '100.01', '1.001', 'abc', '1e2', '01']) assert.equal(validDistance(input), false)
})
test('没有规则或参数非法时不展示虚构奖励', () => {
  assert.equal(estimate('10', 'BUS', null), null)
  assert.equal(estimate('0', 'BUS', rule), null)
  assert.equal(estimate('10', 'CAR', rule), null)
})
test('格式化明确单位和交通名称', () => {
  assert.equal(carbon(1.2), '1.200')
  assert.equal(modes.METRO, '地铁')
})
