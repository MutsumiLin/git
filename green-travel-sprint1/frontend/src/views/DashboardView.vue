<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api/client.js'
import { auth } from '../stores/auth.js'
import { carbon, dateTime, modes, number } from '../utils/format.js'
import TripForm from '../components/TripForm.vue'
import GreenIcon from '../components/GreenIcon.vue'

const summary = ref(null), rule = ref(null), recent = ref([]), error = ref(''), loading = ref(false)
async function load() {
  loading.value = true; error.value = ''
  try { const [s, r, t] = await Promise.all([api.summary(), api.rule(), api.trips()]); summary.value = s; rule.value = r.rule; recent.value = t.items.slice(0, 4) }
  catch (e) { error.value = e.message }
  finally { loading.value = false }
}
onMounted(load)
</script>

<template>
  <div class="page-title"><div><span class="eyebrow">个人概览</span><h1>我的绿色出行</h1><p>你好，{{ auth.user.username }}。每段行程，都可以从这里开始。</p></div><button class="button secondary" :disabled="loading" @click="load"><GreenIcon name="refresh" />刷新</button></div>
  <p v-if="error" class="notice error" role="alert">{{ error }}</p>
  <section class="stats-grid" aria-label="我的出行统计">
    <article class="stat-card featured"><span>可用积分</span><strong>{{ summary ? number(summary.balance) : '—' }}<small>分</small></strong><p>来自已保存的绿色出行记录</p><GreenIcon name="coins" /></article>
    <article class="stat-card"><span>累计模拟减排</span><strong>{{ summary ? carbon(summary.savedCarbonKg) : '—' }}<small>kgCO2e</small></strong><p>相对教学私家车基准估算</p><GreenIcon name="leaf" /></article>
    <article class="stat-card"><span>已记录行程</span><strong>{{ summary ? number(summary.tripCount) : '—' }}<small>次</small></strong><p>公交 · 地铁 · 骑行</p><GreenIcon name="route" /></article>
  </section>
  <section class="dashboard-columns"><article class="card"><TripForm :user-id="auth.user.id" :rule="rule" @reported="load" /></article><aside class="card guide-card"><span class="eyebrow green">积分从哪里来</span><h2>选择绿色，记录日常</h2><ol class="guide-steps"><li><b>01</b><div><strong>完成一次绿色出行</strong><p>使用公交、地铁或骑行到达目的地。</p></div></li><li><b>02</b><div><strong>上报方式与里程</strong><p>当前版本通过手工录入模拟采集。</p></div></li><li><b>03</b><div><strong>查看积分入账</strong><p>系统保存行程、计算结果与积分流水。</p></div></li></ol><div class="subtle-note"><GreenIcon name="info" /><p>当前为教学模拟系统，碳系数用于课程实验估算。商城兑换将在下一轮迭代开放。</p></div></aside></section>
  <article class="card"><div class="section-heading"><div><span class="eyebrow green">最近活动</span><h2>我的行程记录</h2></div><RouterLink to="/trips" class="text-link">查看全部 →</RouterLink></div><div v-if="!recent.length" class="empty-state"><GreenIcon name="route" /><p>{{ loading ? '正在读取行程…' : '还没有行程，提交第一段绿色出行吧。' }}</p></div><div v-else class="table-scroll"><table><thead><tr><th>出行方式</th><th>里程</th><th>完成时间</th><th>模拟减排</th><th>奖励积分</th></tr></thead><tbody><tr v-for="trip in recent" :key="trip.id"><td><span class="mode-tag">{{ modes[trip.mode] }}</span></td><td>{{ trip.distanceKm }} km</td><td>{{ dateTime(trip.completedAt) }}</td><td>{{ carbon(trip.savedCarbonKg) }} kgCO2e</td><td class="positive">+{{ trip.earnedPoints }}</td></tr></tbody></table></div></article>
</template>
