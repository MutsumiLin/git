<script setup>
import { computed, onMounted, ref } from 'vue'
import { auth } from '../stores/auth.js'
import { workspaceApi } from '../api/workspace.js'
import IterationScope from '../components/IterationScope.vue'
import GreenIcon from '../components/GreenIcon.vue'

const products = ref([]), rule = ref(null), health = ref(null), loading = ref(false), error = ref('')
const admin = computed(() => auth.user.role === 'ADMIN')
const statusName = { PUBLISHED: '已上架', DRAFT: '草稿', OFF_SHELF: '已下架' }
async function load() {
  loading.value = true; error.value = ''
  try { const [p, r, h] = await Promise.all([workspaceApi.products(auth.user.role), workspaceApi.rule(), workspaceApi.health()]); products.value = p; rule.value = r.rule; health.value = h }
  catch (e) { error.value = e.message }
  finally { loading.value = false }
}
onMounted(load)
</script>

<template>
  <div class="page-title"><div><span class="eyebrow">{{ admin ? '平台运营空间' : '入驻商户空间' }}</span><h1>{{ admin ? '运营工作台' : '商户工作台' }}</h1><p>{{ admin ? '查看平台商品目录与当前积分规则。' : '查看本商户的商品与后续运营安排。' }}</p></div><button class="button secondary" :disabled="loading" @click="load"><GreenIcon name="refresh" />刷新数据</button></div>
  <IterationScope /><p v-if="error" class="notice error" role="alert">{{ error }}</p>
  <section class="workspace-overview"><article class="card"><span class="eyebrow green">当前身份</span><h2>{{ admin ? '运营管理员' : `商户 #${auth.user.merchantId}` }}</h2><p class="muted">{{ auth.user.username }} · {{ admin ? '全部商户商品只读视图' : '仅展示所属商户数据' }}</p></article><article class="card"><span class="eyebrow green">规则版本</span><h2>{{ rule?.versionName || '读取中…' }}</h2><p class="muted">教学模拟 · {{ rule?.pointsPerKg || '—' }} 积分 / kgCO2e</p></article><article class="card"><span class="eyebrow green">服务状态</span><h2>{{ health?.status === 'UP' ? '运行正常' : '读取中…' }}</h2><p class="muted">{{ health?.iteration || 'Sprint 1' }}</p></article></section>
  <article class="card"><div class="section-heading"><div><span class="eyebrow green">商品目录</span><h2>{{ admin ? '平台商品' : '本店商品' }}</h2></div><button class="button secondary" disabled>商品维护 · 第二轮开放</button></div><div v-if="!products.length" class="empty-state"><p>{{ loading ? '正在读取商品…' : '暂无商品数据' }}</p></div><div v-else class="table-scroll"><table><thead><tr><th>商品</th><th>商户</th><th>积分价格</th><th>库存</th><th>状态</th></tr></thead><tbody><tr v-for="item in products" :key="item.id"><td><strong>{{ item.name }}</strong><small class="table-subtitle">{{ item.sku }}</small></td><td>{{ item.merchantName }}</td><td>{{ item.pointsPrice }} 分</td><td>{{ item.stock }}</td><td><span class="pill">{{ statusName[item.status] }}</span></td></tr></tbody></table></div></article>
  <section class="workspace-bottom"><article class="card"><div class="section-heading"><h2>当前模拟碳系数</h2><span class="pill">kgCO2e / 人公里</span></div><dl v-if="rule" class="detail-list"><div><dt>私家车比较基准</dt><dd>{{ rule.carFactor }}</dd></div><div><dt>公交</dt><dd>{{ rule.busFactor }}</dd></div><div><dt>地铁</dt><dd>{{ rule.metroFactor }}</dd></div><div><dt>骑行</dt><dd>{{ rule.bikeFactor }}</dd></div></dl><p class="small muted">参数仅用于课程实验估算。规则发布入口将在后续迭代开放。</p></article><article class="card"><h2>后续功能原型</h2><div class="planned-feature" v-for="name in admin ? ['商户账号管理', '规则版本发布', '模拟清算与统计'] : ['商品新增与库存维护', '兑换券核销', '本店清算记录']" :key="name"><span>{{ name }}</span><span class="pill">待后续迭代</span></div></article></section>
</template>

<style scoped>.workspace-overview{display:grid;grid-template-columns:repeat(3,1fr);gap:18px;margin-bottom:24px}.workspace-overview h2{margin:14px 0 8px}.workspace-overview p{font-size:12px}.workspace-bottom{display:grid;grid-template-columns:1fr 1fr;gap:22px;margin-top:24px}.planned-feature{display:flex;align-items:center;justify-content:space-between;gap:16px;padding:20px 0;border-bottom:1px solid var(--line);font-size:13px}.table-subtitle{display:block;color:var(--muted);font-size:10px;margin-top:5px}@media(max-width:1100px){.workspace-overview{grid-template-columns:1fr}.workspace-bottom{grid-template-columns:1fr}}</style>
