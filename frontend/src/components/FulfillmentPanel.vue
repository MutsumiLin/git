<script setup>
import {onMounted,ref} from 'vue'
import {workspaceApi as api} from '../api/workspace.js'
const code=ref(''),rows=ref([]),error=ref(''),message=ref(''),busy=ref(false),names={PENDING:'待核销',REDEEMED:'已核销',CANCELLED:'已取消'}
async function load(){rows.value=await api.orders()}
async function verify(){busy.value=true;error.value='';message.value='';try{const o=await api.verify(code.value.trim());message.value=`订单 #${o.id} 已核销，重复核销不会重复消费积分`;code.value='';await load()}catch(e){error.value=e.message}finally{busy.value=false}}
onMounted(async()=>{try{await load()}catch(e){error.value=e.message}})
</script>
<template><article class="card"><h2>兑换券核销</h2><p class="muted small">核对市民出示的兑换码与商品，再确认发放。本店只能核销本商户订单。</p><p v-if="error" class="notice error" role="alert">{{error}}</p><p v-if="message" class="notice success">{{message}}</p><form class="inline-form" @submit.prevent="verify"><label>核销码<input v-model="code" required pattern="[a-f0-9-]{36}" placeholder="粘贴市民出示的核销码"/></label><button class="button primary" :disabled="busy">确认核销</button></form></article><article class="card spaced"><h2>本店最近 100 笔兑换</h2><div class="table-scroll"><table><thead><tr><th>订单</th><th>商品</th><th>数量</th><th>积分</th><th>状态</th><th>清算批次</th></tr></thead><tbody><tr v-for="o in rows" :key="o.id"><td>#{{o.id}}</td><td>{{o.productName}}</td><td>{{o.quantity}}</td><td>{{o.totalPoints}}</td><td>{{names[o.status]}}</td><td>{{o.settlementId||'—'}}</td></tr></tbody></table></div><p v-if="!rows.length" class="empty-state">暂无本店订单</p></article></template>
