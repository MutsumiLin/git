<script setup>
import {onMounted,ref} from 'vue'
import {workspaceApi as api} from '../api/workspace.js'
const rule=ref(null),form=ref(null),busy=ref(false),error=ref(''),message=ref('')
async function load(){const r=await api.rule();rule.value=r.rule;form.value={versionName:'',carFactor:String(r.rule.carFactor),busFactor:String(r.rule.busFactor),metroFactor:String(r.rule.metroFactor),bikeFactor:String(r.rule.bikeFactor),pointsPerKg:r.rule.pointsPerKg}}
async function save(){busy.value=true;error.value='';try{await api.publishRule({...form.value,pointsPerKg:Number(form.value.pointsPerKg)});message.value='新规则已发布，历史行程保持原有计算快照';await load()}catch(e){error.value=e.message}finally{busy.value=false}}
onMounted(async()=>{try{await load()}catch(e){error.value=e.message}})
</script>
<template><article class="card"><h2>发布积分规则</h2><p class="muted small">碳系数仅用于教学模拟。新版本用于发布后的上报，历史行程与积分流水保留原值。</p><p v-if="error" class="notice error" role="alert">{{error}}</p><p v-if="message" class="notice success">{{message}}</p><form v-if="form" class="editor-form" @submit.prevent="save"><p>当前版本 {{rule.versionName}}</p><label>新版本名称<input v-model="form.versionName" pattern="[a-zA-Z0-9_-]{3,40}" maxlength="40" required placeholder="例如 simulation-v2"/></label><div class="form-columns"><label v-for="(title,key) in {carFactor:'私家车基准',busFactor:'公交系数',metroFactor:'地铁系数',bikeFactor:'骑行系数'}" :key="key">{{title}}<input v-model="form[key]" type="number" min="0" max="10" step="0.000001" required/></label></div><label>每 kgCO2e 奖励积分<input v-model.number="form.pointsPerKg" type="number" min="1" max="10000" required/></label><button class="button primary" :disabled="busy">{{busy?'正在发布…':'发布新版本'}}</button></form></article></template>
