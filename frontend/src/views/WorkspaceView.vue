<script setup>
import {computed,onMounted,ref} from 'vue'
import {auth} from '../stores/auth.js'
import {workspaceApi as api} from '../api/workspace.js'
import ProductEditor from '../components/ProductEditor.vue'
import RulesPanel from '../components/RulesPanel.vue'
import FulfillmentPanel from '../components/FulfillmentPanel.vue'
const admin=computed(()=>auth.user.role==='ADMIN'),tab=ref('products'),rule=ref(null),health=ref(null),error=ref('')
const tabs=computed(()=>admin.value?[['products','商品运营'],['rules','积分规则']]:[['products','本店商品'],['orders','订单核销']])
async function refresh(){error.value='';try{const [r,h]=await Promise.all([api.rule(),api.health()]);rule.value=r.rule;health.value=h}catch(e){error.value=e.message}}
onMounted(refresh)
</script>
<template><div class="page-title"><div><span class="eyebrow">{{admin?'平台运营空间':'入驻商户空间'}}</span><h1>{{admin?'运营工作台':'商户工作台'}}</h1><p>{{admin?'管理积分商城商品并发布新的模拟积分规则。':'维护本店商品，核对市民兑换券并确认领取。'}}</p></div><button class="button secondary" @click="refresh">刷新规则</button></div><p v-if="error" class="notice error" role="alert">{{error}}</p><section class="workspace-bottom"><article class="card"><span class="eyebrow green">当前模拟规则</span><h2>{{rule?.versionName||'读取中…'}}</h2><p class="muted small">每 kgCO2e {{rule?.pointsPerKg||'—'}} 分 · 新规则不重算历史行程</p></article><article class="card"><span class="eyebrow green">服务状态</span><h2>{{health?.status==='UP'?'运行正常':'读取中…'}}</h2><p class="muted small">{{admin?'平台全部商品':'仅本商户商品与订单'}} · 积分清算与统计将在下一轮开放</p></article></section><nav class="workspace-tabs" aria-label="工作台功能"><button v-for="t in tabs" :key="t[0]" :class="['button',tab===t[0]?'primary':'secondary']" @click="tab=t[0]">{{t[1]}}</button></nav><ProductEditor v-if="tab==='products'" :role="auth.user.role" :merchant-id="auth.user.merchantId"/><RulesPanel v-if="tab==='rules'&&admin"/><FulfillmentPanel v-if="tab==='orders'&&!admin"/></template>
