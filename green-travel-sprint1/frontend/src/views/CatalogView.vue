<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api/client.js'
import GreenIcon from '../components/GreenIcon.vue'

const items = ref([]), selected = ref(null), error = ref(''), busy = ref(false)
async function load() {
  busy.value = true; error.value = ''
  try { items.value = (await api.products()).items }
  catch (e) { error.value = e.message }
  finally { busy.value = false }
}
async function detail(id) {
  error.value = ''
  try { selected.value = await api.product(id) }
  catch (e) { error.value = e.message }
}
onMounted(load)
</script>
<template>
  <div class="page-title"><div><span class="eyebrow">绿色好物</span><h1>积分商城</h1><p>浏览商户提供的商品，了解积分可以带来的下一种可能。</p></div><span class="pill">第一轮 · 商品浏览</span></div>
  <p class="notice">当前开放商品列表和详情，积分兑换将在第二轮上线。</p><p v-if="error" class="notice error" role="alert">{{ error }}</p>
  <div v-if="!items.length" class="card empty-state"><p>{{ busy ? '正在加载商品…' : '暂无上架商品' }}</p></div>
  <section class="product-grid"><article v-for="(item, index) in items" :key="item.id" class="product-card"><div :class="['product-art', `art-${index % 3}`]"><GreenIcon :name="item.sku.includes('BIKE') ? 'bike' : item.sku.includes('CUP') ? 'cup' : 'bag'" /><span>{{ item.category }}</span></div><div class="product-content"><span class="muted small">{{ item.merchantName }}</span><h2>{{ item.name }}</h2><p>{{ item.description }}</p><div class="product-price"><strong>{{ item.pointsPrice }}<small> 积分</small></strong><span class="muted small">库存 {{ item.stock }}</span></div><button class="button secondary full" @click="detail(item.id)">查看商品详情</button></div></article></section>
  <div v-if="selected" class="modal-backdrop" @click.self="selected = null"><section class="modal" role="dialog" aria-modal="true" aria-labelledby="product-title" tabindex="-1" @keydown.esc="selected = null"><button class="modal-close text-button" aria-label="关闭商品详情" @click="selected = null">×</button><span class="eyebrow green">{{ selected.category }}</span><h2 id="product-title">{{ selected.name }}</h2><p>{{ selected.description }}</p><dl class="detail-list"><div><dt>提供商户</dt><dd>{{ selected.merchantName }}</dd></div><div><dt>所需积分</dt><dd>{{ selected.pointsPrice }} 分</dd></div><div><dt>当前库存</dt><dd>{{ selected.stock }}</dd></div></dl><button class="button primary full" disabled>兑换功能将在第二轮开放</button></section></div>
</template>
