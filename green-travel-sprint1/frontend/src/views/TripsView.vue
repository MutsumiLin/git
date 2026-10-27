<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api/client.js'
import { carbon, dateTime, modes } from '../utils/format.js'

const page = ref(0), result = ref(null), error = ref(''), busy = ref(false)
async function load(next = page.value) {
  busy.value = true; error.value = ''
  try { const data = await api.trips(next); result.value = data; page.value = next }
  catch (e) { error.value = e.message }
  finally { busy.value = false }
}
onMounted(() => load())
</script>
<template>
  <div class="page-title"><div><span class="eyebrow">出行档案</span><h1>我的行程</h1><p>每次上报都保留对应规则、模拟减排量和奖励积分。</p></div><RouterLink to="/" class="button primary">上报新行程</RouterLink></div>
  <p v-if="error" class="notice error" role="alert">{{ error }}</p>
  <article class="card"><div class="section-heading"><h2>全部记录</h2><span class="muted">共 {{ result?.total ?? '—' }} 次</span></div><div v-if="!result?.items.length" class="empty-state"><p>{{ busy ? '正在读取…' : '暂无行程记录' }}</p></div><div v-else class="table-scroll"><table><thead><tr><th>编号</th><th>方式</th><th>里程</th><th>完成时间</th><th>模拟减排</th><th>奖励</th><th>规则</th></tr></thead><tbody><tr v-for="trip in result.items" :key="trip.id"><td>#{{ trip.id }}</td><td>{{ modes[trip.mode] }}</td><td>{{ trip.distanceKm }} km</td><td>{{ dateTime(trip.completedAt) }}</td><td>{{ carbon(trip.savedCarbonKg) }} kgCO2e</td><td class="positive">+{{ trip.earnedPoints }}</td><td><span class="pill">{{ trip.ruleVersion }}</span></td></tr></tbody></table></div><div class="pagination"><button class="button secondary" :disabled="busy || page === 0" @click="load(page - 1)">上一页</button><span>第 {{ page + 1 }} 页</span><button class="button secondary" :disabled="busy || !result || (page + 1) * result.size >= result.total" @click="load(page + 1)">下一页</button></div></article>
</template>
