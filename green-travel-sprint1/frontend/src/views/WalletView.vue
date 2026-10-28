<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api/client.js'
import { dateTime, number } from '../utils/format.js'

const summary = ref(null), records = ref(null), page = ref(0), error = ref(''), busy = ref(false)
async function load(next = page.value) {
  busy.value = true; error.value = ''
  try { const [s, r] = await Promise.all([api.summary(), api.ledger(next)]); summary.value = s; records.value = r; page.value = next }
  catch (e) { error.value = e.message }
  finally { busy.value = false }
}
onMounted(() => load())
</script>
<template>
  <div class="page-title"><div><span class="eyebrow">积分账户</span><h1>我的绿色积分</h1><p>查看每一笔积分的来源与入账记录。</p></div></div>
  <p v-if="error" class="notice error" role="alert">{{ error }}</p>
  <section class="wallet-banner"><span>当前可用积分</span><strong>{{ summary ? number(summary.balance) : '—' }}<small> 分</small></strong><p>累计获得 {{ summary ? number(summary.totalEarned) : '—' }} 分 · 第一轮仅开放行程奖励</p></section>
  <article class="card"><div class="section-heading"><h2>积分流水</h2><span class="pill">真实入账记录</span></div><div v-if="!records?.items.length" class="empty-state"><p>{{ busy ? '正在读取…' : '暂无积分流水' }}</p></div><div v-else class="table-scroll"><table><thead><tr><th>入账时间</th><th>业务来源</th><th>关联行程</th><th>变动积分</th><th>变动后余额</th></tr></thead><tbody><tr v-for="item in records.items" :key="item.id"><td>{{ dateTime(item.createdAt) }}</td><td>绿色出行奖励</td><td>#{{ item.bizId }}</td><td class="positive">+{{ item.delta }}</td><td>{{ number(item.balanceAfter) }}</td></tr></tbody></table></div><div class="pagination"><button class="button secondary" :disabled="busy || page === 0" @click="load(page - 1)">上一页</button><span>第 {{ page + 1 }} 页</span><button class="button secondary" :disabled="busy || !records || (page + 1) * records.size >= records.total" @click="load(page + 1)">下一页</button></div></article>
</template>
